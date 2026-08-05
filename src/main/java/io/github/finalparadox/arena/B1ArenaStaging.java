package io.github.finalparadox.arena;

import io.github.finalparadox.entity.ApigloBossEntity;
import io.github.finalparadox.entity.KorosEchoEntity;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/** Creates the B1 Echo of Koros and starts the Apiglo encounter from it. */
public final class B1ArenaStaging {
    private B1ArenaStaging() {
    }

    public static Optional<KorosEchoEntity> spawnEcho(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        if (data.activeBossUuid().isPresent()) return Optional.empty();
        BlockPos position = anchor.offset(ArenaDefinitions.B1_KOROS_OFFSET);
        level.getChunkAt(position);
        KorosEchoEntity koros = KorosEchoEntity.createB1(level, anchor);
        if (koros == null) return Optional.empty();
        koros.moveTo(position.getX(), position.getY(), position.getZ(), 0.0F, 0.0F);
        if (!level.addFreshEntity(koros)) return Optional.empty();
        data.setKorosUuid(koros.getUUID());
        koros.playArrivalEffects();
        return Optional.of(koros);
    }

    public static boolean beginEncounter(ServerPlayer initiator) {
        ServerLevel level = initiator.serverLevel();
        ArenaDeploymentData data = ArenaDeploymentData.get(level, ArenaDefinitions.B1);
        if (data.state() != ArenaDeploymentData.DeploymentState.READY
                || !ArenaDefinitions.B1.id().equals(data.arenaId())
                || data.floorAnchor().isEmpty()
                || data.activeBossUuid().isPresent()) {
            return false;
        }
        BlockPos anchor = data.floorAnchor().orElseThrow();
        if (!allPlayersInside(level, anchor)) return false;

        cleanupWaiting(level, data);
        BlockPos spawn = ArenaDefinitions.B1.bossSpawnBlock(anchor);
        level.getChunkAt(spawn);
        ApigloBossEntity boss = ModEntities.APIGLO.get().create(level);
        if (boss == null) return false;
        boss.moveTo(spawn.getX(), spawn.getY(), spawn.getZ(), 90.0F, 0.0F);
        if (!level.addFreshEntity(boss)) return false;
        data.setActiveBossUuid(boss.getUUID());
        return true;
    }

    public static boolean allPlayersInside(ServerLevel level, BlockPos anchor) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() != level
                    || player.getX() < anchor.getX() - 31.0D
                    || player.getX() > anchor.getX() + 13.0D
                    || player.getY() < anchor.getY() - 2.0D
                    || player.getY() > anchor.getY() + 18.0D
                    || player.getZ() < anchor.getZ() - 32.0D
                    || player.getZ() > anchor.getZ() + 10.0D) {
                return false;
            }
        }
        return true;
    }

    public static void cleanupWaiting(ServerLevel level, ArenaDeploymentData data) {
        data.korosUuid().map(level::getEntity)
                .filter(KorosEchoEntity.class::isInstance)
                .map(KorosEchoEntity.class::cast)
                .ifPresent(KorosEchoEntity::depart);
        data.clearKoros();
    }
}
