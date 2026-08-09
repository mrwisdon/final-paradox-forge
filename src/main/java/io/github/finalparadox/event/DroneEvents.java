package io.github.finalparadox.event;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.entity.DroneEntity;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Lifecycle and isolation rules for a player controlling a recon drone. */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID)
public final class DroneEvents {
    private DroneEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END
                && event.player instanceof ServerPlayer player) {
            DroneEntity.validateFor(player);
        }
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
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DroneEntity.cancelFor(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onOwnerAttacked(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && DroneEntity.shouldBlockOwnerDamage(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (!event.isDismounting()
                || !(event.getEntityMounting() instanceof Player player)
                || !(event.getEntityBeingMounted() instanceof DroneEntity drone)
                || !drone.shouldCancelDismount(player)) {
            return;
        }
        event.setCanceled(true);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetPassengersPacket(drone));
        }
    }

    @SubscribeEvent
    public static void onDimensionTravel(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && DroneEntity.isActiveFor(player)) {
            event.setCanceled(true);
            player.setPortalCooldown();
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
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && DroneEntity.isActiveFor(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player
                && DroneEntity.isActiveFor(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && DroneEntity.isActiveFor(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExperiencePickup(PlayerXpEvent.PickupXp event) {
        if (event.getEntity() instanceof ServerPlayer player
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
