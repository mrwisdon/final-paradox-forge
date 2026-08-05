package io.github.finalparadox.arena;

import io.github.finalparadox.entity.ApigloBossEntity;
import io.github.finalparadox.entity.KorosEchoEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;

/**
 * Creates the B1 entrance: a waiting, AI-disabled Apiglo plus the Echo of
 * Koros. Koros starts the Apiglo entrance sequence once everyone is inside.
 */
public final class B1ArenaStaging {
    private B1ArenaStaging() {
    }

    public static Optional<Stage> spawn(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        cleanupWaiting(level, data);

        BlockPos bossSpawn = ArenaDefinitions.B1.bossSpawnBlock(anchor);
        level.getChunkAt(bossSpawn);
        ApigloBossEntity boss = ApigloBossEntity.createPrepared(level, bossSpawn);
        KorosEchoEntity koros = KorosEchoEntity.createB1(level, anchor);
        if (boss == null || koros == null) {
            if (boss != null) boss.discard();
            if (koros != null) koros.discard();
            return Optional.empty();
        }

        BlockPos korosPos = anchor.offset(ArenaDefinitions.B1_KOROS_OFFSET);
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
        ApigloBossEntity boss = entity(level, data.activeBossUuid(), ApigloBossEntity.class);
        KorosEchoEntity koros = entity(level, data.korosUuid(), KorosEchoEntity.class);
        if (boss == null || koros == null || !boss.isWaiting()) return Optional.empty();
        return Optional.of(new Stage(boss, koros));
    }

    public static boolean hasWaitingBoss(ServerLevel level, ArenaDeploymentData data) {
        return data.activeBossUuid().map(level::getEntity)
                .filter(ApigloBossEntity.class::isInstance)
                .map(ApigloBossEntity.class::cast)
                .filter(ApigloBossEntity::isWaiting)
                .isPresent();
    }

    public static boolean beginEncounter(ServerPlayer initiator) {
        ServerLevel level = initiator.serverLevel();
        ArenaDeploymentData data = ArenaDeploymentData.get(level, ArenaDefinitions.B1);
        if (data.state() != ArenaDeploymentData.DeploymentState.READY
                || !ArenaDefinitions.B1.id().equals(data.arenaId())
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
        return stage.boss().beginEncounter();
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
        return player.getX() >= anchor.getX() - 31.0D
                && player.getX() <= anchor.getX() + 13.0D
                && player.getY() >= anchor.getY() - 2.0D
                && player.getY() <= anchor.getY() + 18.0D
                && player.getZ() >= anchor.getZ() - 32.0D
                && player.getZ() <= anchor.getZ() + 10.0D;
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
        if (existing instanceof ApigloBossEntity boss && boss.isWaiting()) {
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

    public record Stage(ApigloBossEntity boss, KorosEchoEntity koros) {
    }
}
