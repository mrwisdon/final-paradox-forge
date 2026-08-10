package io.github.finalparadox.arena;

import io.github.finalparadox.entity.B5EncounterManager;
import io.github.finalparadox.entity.B5EncounterData;
import io.github.finalparadox.entity.GariBossEntity;
import io.github.finalparadox.entity.KorosEchoEntity;
import io.github.finalparadox.entity.KoyomiBossEntity;
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

/** Creates and owns the three waiting entities used by the original B5 introduction. */
public final class B5ArenaStaging {
    private B5ArenaStaging() {
    }

    /**
     * Deploys or restores the full B5 waiting batch. Delegates to
     * {@link #reconcile(ServerLevel, ArenaDeploymentData, BlockPos)} instead
     * of destroying and recreating the batch, and re-arms the pre-battle
     * dialogue after a fresh deployment or a respawn.
     */
    public static Optional<Stage> spawn(ServerLevel level, ArenaDeploymentData data, BlockPos anchor) {
        Optional<Stage> stage = reconcile(level, data, anchor);
        if (stage.isPresent()) {
            B5EncounterData.get(level).resetPreBattleDialogue();
        }
        return stage;
    }

    /**
     * Reconciles the B5 waiting batch to at most one arena-owned Koyomi, one
     * Gari and one b5 Echo of Koros. Loads the stage chunks first so a saved
     * UUID is never cleared merely because its chunk was unloaded. The saved
     * UUID is authoritative only when it resolves to a live candidate of this
     * arena, identified by its persisted anchor; surplus candidates are pruned
     * and missing members are created individually. A running fight is never
     * disturbed and never recreated: when the encounter is active the method
     * returns empty immediately without touching any combat entity.
     *
     * @return the retained waiting stage, or empty while the encounter is
     *         active or the batch could not be completed
     */
    public static Optional<Stage> reconcile(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        if (B5EncounterManager.isActive(level)) return Optional.empty();
        Optional<BlockPos> recordedAnchor = data.floorAnchor();
        if (recordedAnchor.isEmpty() || !recordedAnchor.get().equals(anchor)) return Optional.empty();
        if (!ArenaWaitingBatch.shouldReconcile(level, data)) return Optional.empty();
        loadStageChunks(level, anchor);

        AABB bounds = ArenaWaitingBatch.arenaBounds(ArenaDefinitions.B5, anchor);
        BlockPos koyoPos = anchor.offset(ArenaDefinitions.B5_KOYO_OFFSET);
        BlockPos gariPos = anchor.offset(ArenaDefinitions.B5_GARI_OFFSET);
        BlockPos korosPos = anchor.offset(ArenaDefinitions.B5_KOROS_HITBOX_OFFSET);

        // Koyomi and Gari are used only by the B5 arena, so every live
        // instance inside the B5 bounds belongs to this anchor. Instances
        // that predate the persisted anchor field (null) are still adopted so
        // an upgraded save never ends up with duplicated bosses.
        List<KoyomiBossEntity> koyoCandidates = level.getEntities(
                ModEntities.KOYOMI.get(), bounds,
                boss -> boss.isAlive() && ownsAnchor(boss.arenaAnchor(), anchor));
        List<GariBossEntity> gariCandidates = level.getEntities(
                ModEntities.GARI.get(), bounds,
                boss -> boss.isAlive() && ownsAnchor(boss.arenaAnchor(), anchor));
        List<KorosEchoEntity> korosCandidates = ArenaWaitingBatch.scanKoros(
                level, bounds, anchor, "b5");

        // Each slot keeps its saved UUID only when it is still a live
        // candidate; the persisted set is written once all three members are
        // resolved so a partially created batch never looks complete.
        KoyomiBossEntity koyo = ArenaWaitingBatch.resolveSlot(
                koyoCandidates, data.stagedKoyoUuid(), koyoPos,
                uuid -> {
                },
                () -> createKoyo(level, anchor),
                KoyomiBossEntity::discard).orElse(null);
        GariBossEntity gari = ArenaWaitingBatch.resolveSlot(
                gariCandidates, data.stagedGariUuid(), gariPos,
                uuid -> {
                },
                () -> createGari(level, anchor),
                GariBossEntity::discard).orElse(null);
        KorosEchoEntity koros = ArenaWaitingBatch.resolveSlot(
                korosCandidates, data.korosUuid(), korosPos,
                uuid -> {
                },
                () -> createKoros(level, anchor),
                KorosEchoEntity::depart).orElse(null);
        if (koyo == null || gari == null || koros == null) return Optional.empty();

        // Adopted legacy instances get their anchor written back so find()
        // can keep validating exact arena identity afterwards.
        koyo.setArenaAnchor(anchor);
        gari.setArenaAnchor(anchor);
        data.setB5Stage(koyo.getUUID(), gari.getUUID(), koros.getUUID());
        hold(koyo, gari);
        return Optional.of(new Stage(koyo, gari, koros));
    }

    public static Optional<Stage> find(ServerLevel level, ArenaDeploymentData data) {
        Optional<BlockPos> anchorResult = data.floorAnchor();
        if (anchorResult.isEmpty()) return Optional.empty();
        BlockPos anchor = anchorResult.get();
        loadStageChunks(level, anchor);
        KoyomiBossEntity koyo = entity(level, data.stagedKoyoUuid(), KoyomiBossEntity.class);
        if (koyo == null || !anchor.equals(koyo.arenaAnchor())) return Optional.empty();
        GariBossEntity gari = entity(level, data.stagedGariUuid(), GariBossEntity.class);
        if (gari == null || !anchor.equals(gari.arenaAnchor())) return Optional.empty();
        KorosEchoEntity koros = entity(level, data.korosUuid(), KorosEchoEntity.class);
        if (koros == null || !anchor.equals(koros.arenaAnchor()) || !"b5".equals(koros.guideMode())) {
            return Optional.empty();
        }
        return Optional.of(new Stage(koyo, gari, koros));
    }

    public static boolean beginEncounter(ServerPlayer initiator) {
        ServerLevel level = initiator.serverLevel();
        ArenaDeploymentData data = ArenaDeploymentData.get(level, ArenaDefinitions.B5);
        if (data.state() != ArenaDeploymentData.DeploymentState.READY
                || !ArenaDefinitions.B5.id().equals(data.arenaId())
                || data.floorAnchor().isEmpty()
                || B5EncounterManager.isActive(level)) return false;

        Optional<Stage> result = find(level, data);
        if (result.isEmpty()) return false;
        BlockPos anchor = data.floorAnchor().orElseThrow();
        if (!allPlayersInside(level, anchor)) return false;

        B5EncounterData encounter = B5EncounterData.get(level);
        if (!encounter.preBattleDialoguePlayed() || encounter.preBattleDialogueTicks() >= 0) return false;

        Stage stage = result.get();
        hold(stage.koyo(), stage.gari());
        stage.koros().depart();
        data.clearKoros();
        ArenaFightParticipants.begin(level, ArenaDefinitions.B5, anchor);
        B5EncounterManager.beginCountdown(level, anchor, stage.koyo(), stage.gari());
        ArenaCompassDestination.setForArena(level, ArenaDefinitions.B5, anchor);
        return true;
    }

    public static void tryStartPreBattleDialogue(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        if (B5EncounterManager.isActive(level)) return;
        if (find(level, data).isEmpty()) return;
        B5EncounterData encounter = B5EncounterData.get(level);
        if (!encounter.preBattleDialoguePlayed() && anyPlayerInside(level, anchor)) {
            encounter.startPreBattleDialogue(level);
        }
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
        return player.getX() >= anchor.getX() - 51.0D
                && player.getX() <= anchor.getX() + 7.0D
                && player.getY() >= anchor.getY() + 1.0D
                && player.getY() <= anchor.getY() + 11.0D
                && player.getZ() >= anchor.getZ() - 19.0D
                && player.getZ() <= anchor.getZ() + 19.0D;
    }

    /**
     * Removes every arena-owned waiting duplicate after loading the expected
     * chunks. A running encounter is never touched.
     */
    public static void cleanupWaiting(ServerLevel level, ArenaDeploymentData data) {
        if (B5EncounterManager.isActive(level)) return;
        Optional<BlockPos> anchorResult = data.floorAnchor();
        if (anchorResult.isEmpty()) {
            data.clearB5Stage();
            return;
        }
        BlockPos anchor = anchorResult.get();
        loadStageChunks(level, anchor);
        AABB bounds = ArenaWaitingBatch.arenaBounds(ArenaDefinitions.B5, anchor);
        level.getEntities(ModEntities.KOYOMI.get(), bounds,
                        boss -> boss.isAlive() && ownsAnchor(boss.arenaAnchor(), anchor))
                .forEach(Entity::discard);
        level.getEntities(ModEntities.GARI.get(), bounds,
                        boss -> boss.isAlive() && ownsAnchor(boss.arenaAnchor(), anchor))
                .forEach(Entity::discard);
        ArenaWaitingBatch.scanKoros(level, bounds, anchor, "b5")
                .forEach(KorosEchoEntity::depart);
        data.clearB5Stage();
    }

    private static boolean ownsAnchor(BlockPos entityAnchor, BlockPos anchor) {
        return entityAnchor == null || anchor.equals(entityAnchor);
    }

    private static KoyomiBossEntity createKoyo(ServerLevel level, BlockPos anchor) {
        KoyomiBossEntity koyo = KoyomiBossEntity.createPrepared(level);
        if (koyo == null) return null;
        BlockPos pos = anchor.offset(ArenaDefinitions.B5_KOYO_OFFSET);
        koyo.moveTo(pos.getX(), pos.getY(), pos.getZ(), -90.0F, 0.0F);
        koyo.setArenaAnchor(anchor);
        return level.addFreshEntity(koyo) ? koyo : null;
    }

    private static GariBossEntity createGari(ServerLevel level, BlockPos anchor) {
        GariBossEntity gari = GariBossEntity.createPrepared(level);
        if (gari == null) return null;
        BlockPos pos = anchor.offset(ArenaDefinitions.B5_GARI_OFFSET);
        gari.moveTo(pos.getX(), pos.getY(), pos.getZ(), -90.0F, 0.0F);
        gari.setArenaAnchor(anchor);
        return level.addFreshEntity(gari) ? gari : null;
    }

    private static KorosEchoEntity createKoros(ServerLevel level, BlockPos anchor) {
        KorosEchoEntity koros = KorosEchoEntity.create(level, anchor);
        if (koros == null) return null;
        BlockPos pos = anchor.offset(ArenaDefinitions.B5_KOROS_HITBOX_OFFSET);
        koros.moveTo(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
        if (!level.addFreshEntity(koros)) return null;
        koros.playArrivalEffects();
        return koros;
    }

    private static void loadStageChunks(ServerLevel level, BlockPos anchor) {
        level.getChunkAt(anchor.offset(ArenaDefinitions.B5_KOYO_OFFSET));
        level.getChunkAt(anchor.offset(ArenaDefinitions.B5_GARI_OFFSET));
        level.getChunkAt(anchor.offset(ArenaDefinitions.B5_KOROS_HITBOX_OFFSET));
    }

    private static void hold(KoyomiBossEntity koyo, GariBossEntity gari) {
        koyo.setInvulnerable(true);
        koyo.setNoAi(true);
        koyo.setBossBarEnabled(false);
        gari.setInvulnerable(true);
        gari.setNoAi(true);
        gari.setBossBarEnabled(false);
    }

    private static <T extends Entity> T entity(ServerLevel level, Optional<UUID> uuid, Class<T> type) {
        if (uuid.isEmpty()) return null;
        Entity entity = level.getEntity(uuid.get());
        return type.isInstance(entity) && entity.isAlive() ? type.cast(entity) : null;
    }

    public record Stage(KoyomiBossEntity koyo, GariBossEntity gari, KorosEchoEntity koros) {
    }
}
