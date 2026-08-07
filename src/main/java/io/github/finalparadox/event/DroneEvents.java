package io.github.finalparadox.event;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.entity.DroneEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Keeps the drone owner's body frozen at the deploy anchor and blocks body-side
 * interactions while the camera is on the drone.
 */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID)
public final class DroneEvents {
    private DroneEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        DroneEntity.freezeFor(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DroneEntity.cancelFor(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DroneEntity.cancelFor(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DroneEntity.cancelFor(player);
            DroneEntity.resetCameraFor(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DroneEntity.cancelFor(player);
            DroneEntity.resetCameraFor(player);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        cancelIfDrone(event);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        cancelIfDrone(event);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        cancelIfDrone(event);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        cancelIfDrone(event);
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player
                && DroneEntity.isActiveFor(player)) {
            event.setCanceled(true);
        }
    }

    private static void cancelIfDrone(PlayerInteractEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && DroneEntity.isActiveFor(player)) {
            event.setCanceled(true);
        }
    }
}
