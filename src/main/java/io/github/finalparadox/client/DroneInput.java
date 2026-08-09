package io.github.finalparadox.client;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.entity.DroneEntity;
import io.github.finalparadox.network.DroneBombPacket;
import io.github.finalparadox.network.DroneExitPacket;
import io.github.finalparadox.network.DroneGunInputPacket;
import io.github.finalparadox.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Feeds movement keys into the local client-controlled drone vehicle. Vanilla
 * sends the resulting root-vehicle pose to the server each player tick.
 */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class DroneInput {
    private static boolean exitRequested;
    private static int lastGunDroneId = -1;
    private static boolean lastGunHeld;

    private DroneInput() {
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        DroneGatlingSoundManager.tick(minecraft);
        DroneTracerRenderer.tick(minecraft);
        if (minecraft.player == null) {
            exitRequested = false;
            lastGunDroneId = -1;
            lastGunHeld = false;
            return;
        }

        if (!(minecraft.player.getVehicle() instanceof DroneEntity drone)) {
            updateGunInput(-1, false);
            exitRequested = false;
            return;
        }
        if (exitRequested) {
            drone.setClientInput(0);
            updateGunInput(drone.getId(), false);
            return;
        }

        int state = 0;
        if (minecraft.screen == null && minecraft.isWindowActive()) {
            if (minecraft.options.keyUp.isDown()) {
                state |= DroneEntity.FLAG_FORWARD;
            }
            if (minecraft.options.keyDown.isDown()) {
                state |= DroneEntity.FLAG_BACK;
            }
            if (minecraft.options.keyLeft.isDown()) {
                state |= DroneEntity.FLAG_LEFT;
            }
            if (minecraft.options.keyRight.isDown()) {
                state |= DroneEntity.FLAG_RIGHT;
            }
            if (minecraft.options.keyJump.isDown()) {
                state |= DroneEntity.FLAG_UP;
            }
            if (minecraft.options.keyShift.isDown()) {
                state |= DroneEntity.FLAG_DOWN;
            }
        }
        drone.setClientInput(state);
        boolean gunHeld = minecraft.screen == null
                && minecraft.isWindowActive()
                && minecraft.options.keyAttack.isDown();
        updateGunInput(drone.getId(), gunHeld);
        if (minecraft.screen == null) {
            while (DroneKeyMappings.DROP_BOMB.consumeClick()) {
                ModNetwork.CHANNEL.sendToServer(new DroneBombPacket());
            }
            while (DroneKeyMappings.EXIT.consumeClick()) {
                drone.setClientInput(0);
                updateGunInput(drone.getId(), false);
                ModNetwork.CHANNEL.sendToServer(new DroneExitPacket());
                exitRequested = true;
            }
        }
    }

    @SubscribeEvent
    public static void onInteractionInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (isControllingDrone()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (isControllingDrone()) {
            event.setCanceled(true);
        }
    }

    /** Hides the real passenger, including armor and held items, on every client. */
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (event.getEntity().getVehicle() instanceof DroneEntity) {
            event.setCanceled(true);
        }
    }

    private static boolean isControllingDrone() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && minecraft.player.getVehicle() instanceof DroneEntity;
    }

    public static void handleAuthorizedDismount(int droneEntityId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null
                && minecraft.player.getVehicle() instanceof DroneEntity drone
                && drone.getId() == droneEntityId) {
            drone.setClientInput(0);
            updateGunInput(drone.getId(), false);
            drone.confirmClientDismount(minecraft.player);
        }
        exitRequested = false;
    }

    private static void updateGunInput(int droneEntityId, boolean held) {
        if (droneEntityId == lastGunDroneId && held == lastGunHeld) {
            return;
        }
        ModNetwork.CHANNEL.sendToServer(new DroneGunInputPacket(held));
        lastGunDroneId = droneEntityId;
        lastGunHeld = held;
    }
}
