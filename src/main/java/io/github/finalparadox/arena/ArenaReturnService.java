package io.github.finalparadox.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/** Shared safe return path used by the compass and void rescue fallback. */
public final class ArenaReturnService {
    private ArenaReturnService() {
    }

    public static void returnToOverworld(ServerPlayer player, String messageKey) {
        ServerLevel overworld = player.getServer().overworld();
        ArenaPlayerRespawn.restore(player);

        BlockPos destination = null;
        float yaw = player.getYRot();
        if (Level.OVERWORLD.equals(player.getRespawnDimension())) {
            destination = player.getRespawnPosition();
            yaw = player.getRespawnAngle();
        }
        if (destination == null) {
            destination = overworld.getSharedSpawnPos();
        }
        double y = Math.max(destination.getY() + 1,
                overworld.getHeight(Heightmap.Types.MOTION_BLOCKING,
                        destination.getX(), destination.getZ()) + 1);

        ArenaPlayerState.clearPending(player);
        ArenaPlayerState.restoreOriginalGameMode(player);
        ArenaPlayerState.clearCurrent(player);
        player.teleportTo(overworld, destination.getX() + 0.5D, y,
                destination.getZ() + 0.5D, yaw, player.getXRot());
        player.setDeltaMovement(0.0D, 0.0D, 0.0D);
        player.fallDistance = 0.0F;
        overworld.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable(messageKey), true);
    }
}
