package io.github.finalparadox.item;

import io.github.finalparadox.arena.ArenaDefinition;
import io.github.finalparadox.arena.ArenaDefinitions;
import io.github.finalparadox.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Writes the current arena's original boss-fight respawn point onto every
 * online Arena Compass once a boss battle starts.
 */
public final class ArenaCompassDestination {
    public static final String RESPAWN_TAG = "ArenaRespawn";
    public static final String RESPAWN_YAW_TAG = "ArenaRespawnYaw";

    private ArenaCompassDestination() {
    }

    public static void setForArena(
            ServerLevel level,
            ArenaDefinition definition,
            BlockPos anchor
    ) {
        ArenaDefinitions.ArenaRespawn respawn = ArenaDefinitions.respawnFor(definition).orElse(null);
        if (respawn == null) return;
        BlockPos destination = anchor.offset(respawn.offset());
        int[] position = {
                destination.getX(), destination.getY(), destination.getZ()
        };
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.is(ModItems.ARENA_COMPASS.get())) continue;
                stack.getOrCreateTag().putIntArray(RESPAWN_TAG, position);
                stack.getOrCreateTag().putFloat(RESPAWN_YAW_TAG, respawn.yaw());
            }
        }
    }
}
