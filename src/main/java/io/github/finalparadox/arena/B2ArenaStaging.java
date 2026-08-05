package io.github.finalparadox.arena;

import io.github.finalparadox.entity.KorosEchoEntity;
import io.github.finalparadox.entity.TharKrooBossEntity;
import io.github.finalparadox.item.ArenaCompassDestination;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;

/**
 * Creates the B2 entrance: a waiting, AI-disabled Thar Kroo plus the Echo of
 * Koros. Entering the arena starts the pre-battle dialogue automatically.
 */
public final class B2ArenaStaging {
    private B2ArenaStaging() {
    }

    public static Optional<Stage> spawn(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        cleanupWaiting(level, data);

        BlockPos bossSpawn = ArenaDefinitions.B2.bossSpawnBlock(anchor);
        level.getChunkAt(bossSpawn);
        TharKrooBossEntity boss = TharKrooBossEntity.createPrepared(level, bossSpawn);
        KorosEchoEntity koros = KorosEchoEntity.createB2(level, anchor);
        if (boss == null || koros == null) {
            if (boss != null) boss.discard();
            if (koros != null) koros.discard();
            return Optional.empty();
        }

        BlockPos korosPos = anchor.offset(ArenaDefinitions.B2_KOROS_OFFSET);
        koros.moveTo(korosPos.getX(), korosPos.getY(), korosPos.getZ(), 0.0F, 0.0F);
        if (!level.addFreshEntity(boss)) return Optional.empty();
        if (!level.addFreshEntity(koros)) {
            boss.discard();
            return Optional.empty();
        }

        data.setActiveBossUuid(boss.getUUID());
        data.setKorosUuid(koros.getUUID());
        koros.playArrivalEffects();
        return Optional.of(new Stage(boss, koros));
    }

    public static Optional<Stage> find(ServerLevel level, ArenaDeploymentData data) {
        TharKrooBossEntity boss = entity(level, data.activeBossUuid(), TharKrooBossEntity.class);
        KorosEchoEntity koros = entity(level, data.korosUuid(), KorosEchoEntity.class);
        if (boss == null || koros == null || !boss.isWaiting()) return Optional.empty();
        return Optional.of(new Stage(boss, koros));
    }

    public static boolean hasWaitingBoss(ServerLevel level, ArenaDeploymentData data) {
        return data.activeBossUuid().map(level::getEntity)
                .filter(TharKrooBossEntity.class::isInstance)
                .map(TharKrooBossEntity.class::cast)
                .filter(TharKrooBossEntity::isWaiting)
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
            result = spawn(level, data, anchor);
        }
        if (result.isEmpty() || !allPlayersInside(level, anchor)) return false;

        Stage stage = result.get();
        if (!stage.boss().preBattleDialoguePlayed()) {
            return stage.boss().startPreBattleDialogue();
        }
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

    public static void cleanupWaiting(ServerLevel level, ArenaDeploymentData data) {
        data.korosUuid().map(level::getEntity)
                .filter(KorosEchoEntity.class::isInstance)
                .map(KorosEchoEntity.class::cast)
                .ifPresent(KorosEchoEntity::depart);
        data.clearKoros();

        Optional<UUID> bossUuid = data.activeBossUuid();
        if (bossUuid.isEmpty()) return;
        Entity existing = level.getEntity(bossUuid.get());
        if (existing instanceof TharKrooBossEntity boss && boss.isWaiting()) {
            boss.discard();
            data.clearActiveBoss();
        } else if (existing == null || !existing.isAlive()) {
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
