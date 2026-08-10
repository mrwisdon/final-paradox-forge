package io.github.finalparadox.arena;

import io.github.finalparadox.entity.KorosEchoEntity;
import io.github.finalparadox.entity.TharKrooBossEntity;
import io.github.finalparadox.item.ArenaCompassDestination;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Creates the B2 entrance: a waiting, AI-disabled Thar Kroo plus the Echo of
 * Koros. Entering the arena starts the pre-battle dialogue automatically.
 */
public final class B2ArenaStaging {
    private B2ArenaStaging() {
    }

    /**
     * Reconciles the B2 waiting batch to at most one waiting Thar Kroo plus
     * one B2 Echo of Koros. Loads the stage chunks first so a saved UUID is
     * never cleared merely because its chunk was unloaded. The saved UUID is
     * authoritative only when it resolves to a live Thar Kroo of this arena,
     * identified by its persisted anchor. A running fight is never disturbed
     * and never recreated: surplus waiting duplicates and matching Koros
     * echoes are pruned and the method returns empty.
     *
     * @return the retained waiting stage, or empty while a boss fight is
     *         active or the batch could not be completed
     */
    public static Optional<Stage> reconcile(ServerLevel level, ArenaDeploymentData data) {
        if (!ArenaWaitingBatch.shouldReconcile(level, data)) return Optional.empty();
        Optional<BlockPos> anchorResult = data.floorAnchor();
        if (anchorResult.isEmpty()) return Optional.empty();
        BlockPos anchor = anchorResult.get();
        loadStageChunks(level, anchor);

        AABB bounds = ArenaWaitingBatch.arenaBounds(ArenaDefinitions.B2, anchor);
        BlockPos expectedBoss = ArenaDefinitions.B2.bossSpawnBlock(anchor);
        List<TharKrooBossEntity> liveBosses = level.getEntities(
                ModEntities.THAR_KROO.get(), bounds,
                boss -> boss.isAlive() && boss.matchesAnchor(expectedBoss));
        List<TharKrooBossEntity> waitingBosses = liveBosses.stream()
                .filter(TharKrooBossEntity::isWaiting)
                .toList();
        List<TharKrooBossEntity> activeBosses = liveBosses.stream()
                .filter(boss -> !boss.isWaiting())
                .toList();
        List<KorosEchoEntity> korosCandidates = ArenaWaitingBatch.scanKoros(
                level, bounds, anchor, "b2");

        // A saved waiting boss wins only when it is an arena-filtered
        // candidate, so corrupt SavedData cannot pair unrelated entities.
        Optional<UUID> savedUuid = data.activeBossUuid();
        Optional<TharKrooBossEntity> savedWaiting = savedUuid.flatMap(uuid ->
                waitingBosses.stream()
                        .filter(boss -> boss.getUUID().equals(uuid))
                        .findFirst());
        if (savedWaiting.isPresent()) {
            TharKrooBossEntity boss = savedWaiting.get();
            ArenaWaitingBatch.pruneSurplus(waitingBosses, boss, TharKrooBossEntity::discard);
            return finishStage(level, data, anchor, boss, korosCandidates);
        }
        // The saved UUID may still resolve to this arena's live boss even when
        // it has left the scanning bounds, for example during a running fight:
        // never disturb it and never recreate the batch.
        Optional<TharKrooBossEntity> savedLive = savedUuid
                .map(level::getEntity)
                .filter(TharKrooBossEntity.class::isInstance)
                .map(TharKrooBossEntity.class::cast)
                .filter(boss -> boss.isAlive() && boss.matchesAnchor(expectedBoss));
        if (savedLive.isPresent()) {
            pruneWaitingBatch(waitingBosses, korosCandidates, data);
            return Optional.empty();
        }
        // A live fight exists but its saved UUID is stale or missing: persist
        // one active boss deterministically (nearest expected, UUID tie-break)
        // before returning empty. Active bosses are never discarded.
        if (!activeBosses.isEmpty()) {
            ArenaWaitingBatch.adopt(activeBosses, Optional.empty(), expectedBoss)
                    .ifPresent(boss -> data.setActiveBossUuid(boss.getUUID()));
            pruneWaitingBatch(waitingBosses, korosCandidates, data);
            return Optional.empty();
        }
        if (savedUuid.isPresent()) {
            // Unrelated or corrupt saved UUID: ignore and clear it.
            data.clearActiveBoss();
        }
        TharKrooBossEntity boss = adoptOrCreateBoss(level, data, waitingBosses, anchor, expectedBoss);
        if (boss == null || !boss.isWaiting()) return Optional.empty();
        return finishStage(level, data, anchor, boss, korosCandidates);
    }

    private static Optional<Stage> finishStage(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor,
            TharKrooBossEntity boss,
            List<KorosEchoEntity> korosCandidates
    ) {
        KorosEchoEntity koros = ArenaWaitingBatch.resolveSlot(
                korosCandidates, data.korosUuid(),
                anchor.offset(ArenaDefinitions.B2_KOROS_OFFSET),
                data::setKorosUuid,
                () -> createKoros(level, data, anchor),
                KorosEchoEntity::depart).orElse(null);
        if (koros == null) return Optional.empty();
        return Optional.of(new Stage(boss, koros));
    }

    /** Removes every residual waiting boss and matching Koros duplicate. */
    private static void pruneWaitingBatch(
            List<TharKrooBossEntity> waitingBosses,
            List<KorosEchoEntity> korosCandidates,
            ArenaDeploymentData data
    ) {
        waitingBosses.forEach(TharKrooBossEntity::discard);
        korosCandidates.forEach(KorosEchoEntity::depart);
        data.clearKoros();
    }

    private static TharKrooBossEntity adoptOrCreateBoss(
            ServerLevel level,
            ArenaDeploymentData data,
            List<TharKrooBossEntity> waitingBosses,
            BlockPos anchor,
            BlockPos expected
    ) {
        Optional<TharKrooBossEntity> adopted = ArenaWaitingBatch.adopt(
                waitingBosses, data.activeBossUuid(), expected);
        if (adopted.isPresent()) {
            TharKrooBossEntity boss = adopted.get();
            ArenaWaitingBatch.pruneSurplus(waitingBosses, boss, TharKrooBossEntity::discard);
            data.setActiveBossUuid(boss.getUUID());
            return boss;
        }
        return createBoss(level, data, anchor);
    }

    private static TharKrooBossEntity createBoss(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        BlockPos bossSpawn = ArenaDefinitions.B2.bossSpawnBlock(anchor);
        level.getChunkAt(bossSpawn);
        TharKrooBossEntity boss = TharKrooBossEntity.createPrepared(level, bossSpawn);
        if (boss == null) return null;
        if (!level.addFreshEntity(boss)) return null;
        data.setActiveBossUuid(boss.getUUID());
        return boss;
    }

    private static KorosEchoEntity createKoros(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        KorosEchoEntity koros = KorosEchoEntity.createB2(level, anchor);
        if (koros == null) return null;
        BlockPos korosPos = anchor.offset(ArenaDefinitions.B2_KOROS_OFFSET);
        koros.moveTo(korosPos.getX(), korosPos.getY(), korosPos.getZ(), 0.0F, 0.0F);
        if (!level.addFreshEntity(koros)) return null;
        data.setKorosUuid(koros.getUUID());
        koros.playArrivalEffects();
        return koros;
    }

    public static void loadStageChunks(ServerLevel level, BlockPos anchor) {
        level.getChunkAt(ArenaDefinitions.B2.bossSpawnBlock(anchor));
        level.getChunkAt(anchor.offset(ArenaDefinitions.B2_KOROS_OFFSET));
    }

    /**
     * Reconciles the batch instead of destroying it: a valid partial batch is
     * repaired, never deleted and recreated from scratch.
     */
    public static Optional<Stage> spawn(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        // The caller-supplied anchor must match the recorded floor anchor;
        // never reconcile against a silently different anchor.
        Optional<BlockPos> recordedAnchor = data.floorAnchor();
        if (recordedAnchor.isEmpty() || !recordedAnchor.get().equals(anchor)) return Optional.empty();
        return reconcile(level, data);
    }

    public static Optional<Stage> find(ServerLevel level, ArenaDeploymentData data) {
        Optional<BlockPos> anchorResult = data.floorAnchor();
        if (anchorResult.isEmpty()) return Optional.empty();
        BlockPos anchor = anchorResult.get();
        loadStageChunks(level, anchor);
        BlockPos expectedBoss = ArenaDefinitions.B2.bossSpawnBlock(anchor);
        TharKrooBossEntity boss = entity(level, data.activeBossUuid(), TharKrooBossEntity.class);
        if (boss == null || !boss.isWaiting() || !boss.matchesAnchor(expectedBoss)) {
            return Optional.empty();
        }
        KorosEchoEntity koros = entity(level, data.korosUuid(), KorosEchoEntity.class);
        if (koros == null || !anchor.equals(koros.arenaAnchor()) || !"b2".equals(koros.guideMode())) {
            return Optional.empty();
        }
        return Optional.of(new Stage(boss, koros));
    }

    public static boolean hasWaitingBoss(ServerLevel level, ArenaDeploymentData data) {
        Optional<BlockPos> anchorResult = data.floorAnchor();
        if (anchorResult.isEmpty()) return false;
        BlockPos anchor = anchorResult.get();
        loadStageChunks(level, anchor);
        BlockPos expectedBoss = ArenaDefinitions.B2.bossSpawnBlock(anchor);
        return data.activeBossUuid().map(level::getEntity)
                .filter(TharKrooBossEntity.class::isInstance)
                .map(TharKrooBossEntity.class::cast)
                .filter(TharKrooBossEntity::isWaiting)
                .filter(boss -> boss.matchesAnchor(expectedBoss))
                .isPresent();
    }

    public static boolean beginEncounter(ServerPlayer initiator) {
        ServerLevel level = initiator.serverLevel();
        ArenaDeploymentData data = ArenaDeploymentData.get(level, ArenaDefinitions.B2);
        if (data.state() != ArenaDeploymentData.DeploymentState.READY
                || !ArenaDefinitions.B2.id().equals(data.arenaId())
                || data.floorAnchor().isEmpty()) {
            return false;
        }
        BlockPos anchor = data.floorAnchor().orElseThrow();
        Optional<Stage> result = find(level, data);
        if (result.isEmpty()) {
            if (data.activeBossUuid().isPresent()) return false;
            result = reconcile(level, data);
        }
        if (result.isEmpty() || !allPlayersInside(level, anchor)) return false;

        Stage stage = result.get();
        if (!stage.boss().preBattleDialoguePlayed()) return false;
        stage.koros().depart();
        data.clearKoros();
        boolean started = stage.boss().beginEncounter();
        if (started) ArenaCompassDestination.setForArena(level, ArenaDefinitions.B2, anchor);
        return started;
    }

    public static boolean anyPlayerInside(ServerLevel level, BlockPos anchor) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() == level && !player.isSpectator() && inside(anchor, player)) {
                return true;
            }
        }
        return false;
    }

    public static boolean allPlayersInside(ServerLevel level, BlockPos anchor) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() != level || !inside(anchor, player)) {
                return false;
            }
        }
        return true;
    }

    private static boolean inside(BlockPos anchor, ServerPlayer player) {
        return player.getX() >= anchor.getX() - 53.0D
                && player.getX() <= anchor.getX() + 40.0D
                && player.getY() >= anchor.getY() - 5.0D
                && player.getY() <= anchor.getY() + 11.0D
                && player.getZ() >= anchor.getZ() - 52.0D
                && player.getZ() <= anchor.getZ() + 47.0D;
    }

    /**
     * Removes every arena-owned waiting duplicate after loading the expected
     * chunks. A non-waiting active boss and unrelated entities are never
     * touched.
     */
    public static void cleanupWaiting(ServerLevel level, ArenaDeploymentData data) {
        Optional<BlockPos> anchorResult = data.floorAnchor();
        if (anchorResult.isEmpty()) {
            data.clearKoros();
            data.clearActiveBoss();
            return;
        }
        BlockPos anchor = anchorResult.get();
        loadStageChunks(level, anchor);
        AABB bounds = ArenaWaitingBatch.arenaBounds(ArenaDefinitions.B2, anchor);
        BlockPos expectedBoss = ArenaDefinitions.B2.bossSpawnBlock(anchor);
        List<TharKrooBossEntity> waitingBosses = level.getEntities(
                ModEntities.THAR_KROO.get(), bounds,
                boss -> boss.isAlive() && boss.isWaitingAt(expectedBoss));
        boolean discardedRecorded = data.activeBossUuid()
                .map(uuid -> waitingBosses.stream()
                        .anyMatch(boss -> boss.getUUID().equals(uuid)))
                .orElse(false);
        waitingBosses.forEach(TharKrooBossEntity::discard);
        List<KorosEchoEntity> korosCandidates = ArenaWaitingBatch.scanKoros(
                level, bounds, anchor, "b2");
        korosCandidates.forEach(KorosEchoEntity::depart);
        data.clearKoros();
        // A live, non-waiting recorded boss must keep its active UUID: the
        // deploy/reset commands rely on the later active-boss check to refuse
        // while the fight is still running. The UUID is cleared only when its
        // waiting entity was discarded here, or when the recorded entity is
        // absent, dead, or unrelated to this arena.
        boolean recordedAlive = data.activeBossUuid()
                .map(uuid -> {
                    Entity existing = level.getEntity(uuid);
                    return existing instanceof TharKrooBossEntity boss
                            && boss.isAlive()
                            && boss.matchesAnchor(expectedBoss);
                })
                .orElse(false);
        if (discardedRecorded || !recordedAlive) {
            data.clearActiveBoss();
        }
    }

    private static <T extends Entity> T entity(
            ServerLevel level,
            Optional<UUID> uuid,
            Class<T> type
    ) {
        if (uuid.isEmpty()) return null;
        Entity entity = level.getEntity(uuid.get());
        return type.isInstance(entity) && entity.isAlive() ? type.cast(entity) : null;
    }

    public record Stage(TharKrooBossEntity boss, KorosEchoEntity koros) {
    }
}
