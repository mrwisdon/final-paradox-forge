package io.github.finalparadox.arena;

import io.github.finalparadox.entity.EotharEchoEntity;
import io.github.finalparadox.entity.MarawTharBossEntity;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.util.Optional;

public final class MarawTharArenaStaging {
    private MarawTharArenaStaging() {
    }

    public static Optional<EotharEchoEntity> spawnEothar(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        BlockPos position = anchor.offset(ArenaDefinitions.MARAWTHAR_EOTHAR_OFFSET);
        level.getChunkAt(position);
        EotharEchoEntity eothar = EotharEchoEntity.create(level, anchor);
        if (eothar == null) return Optional.empty();
        eothar.moveTo(position.getX(), position.getY(), position.getZ(), 0.0F, 0.0F);
        if (!level.addFreshEntity(eothar)) return Optional.empty();
        data.setEotharUuid(eothar.getUUID());
        eothar.playArrivalEffects();
        return Optional.of(eothar);
    }

    public static void cleanupWaiting(ServerLevel level, ArenaDeploymentData data) {
        data.eotharUuid().map(level::getEntity)
                .filter(EotharEchoEntity.class::isInstance)
                .map(EotharEchoEntity.class::cast)
                .ifPresent(EotharEchoEntity::depart);
        data.clearEothar();
    }

    public static Optional<MarawTharBossEntity> spawnBoss(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        BlockPos spawn = ArenaDefinitions.MARAWTHAR.bossSpawnBlock(anchor);
        level.getChunkAt(spawn);
        MarawTharBossEntity boss = ModEntities.MARAWTHAR.get().create(level);
        if (boss == null) return Optional.empty();
        boss.moveTo(spawn.getX(), spawn.getY(), spawn.getZ(), 0.0F, 0.0F);
        if (!level.addFreshEntity(boss)) return Optional.empty();
        data.setActiveBossUuid(boss.getUUID());
        data.setMarawTharTriggered(true);
        data.clearEothar();
        return Optional.of(boss);
    }

    public static boolean anyPlayerNear(ServerLevel level, BlockPos anchor, double radius) {
        double radiusSqr = radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR
                    && player.distanceToSqr(anchor.getX() + 0.5D,
                            anchor.getY() + 0.5D, anchor.getZ() + 0.5D) <= radiusSqr) {
                return true;
            }
        }
        return false;
    }
}