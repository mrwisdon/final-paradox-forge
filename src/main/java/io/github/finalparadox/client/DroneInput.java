package io.github.finalparadox.client;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.entity.DroneEntity;
import io.github.finalparadox.network.DroneBombPacket;
import io.github.finalparadox.network.DroneExitPacket;
import io.github.finalparadox.network.DroneInputPacket;
import io.github.finalparadox.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Reads vanilla movement keys while the camera is on a drone, sends held-state
 * updates to the server, and hides the local player's hands.
 */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class DroneInput {
    private static int lastInputState;
    private static float lastSentYRot;
    private static float lastSentXRot;

    private DroneInput() {
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            lastInputState = 0;
            lastSentYRot = 0.0F;
            lastSentXRot = 0.0F;
            return;
        }

        boolean controlling = minecraft.getCameraEntity() instanceof DroneEntity;
        float yRot = minecraft.player.getYRot();
        float xRot = minecraft.player.getXRot();
        if (controlling && minecraft.getCameraEntity() instanceof DroneEntity drone) {
            drone.yRotO = yRot;
            drone.setYRot(yRot);
            drone.xRotO = xRot;
            drone.setXRot(xRot);
        }
        int state = 0;
        if (controlling && minecraft.screen == null) {
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
        boolean rotationChanged = controlling
                && (Math.abs(Mth.wrapDegrees(yRot - lastSentYRot)) >= 0.25F
                || Math.abs(xRot - lastSentXRot) >= 0.25F);
        if (state != lastInputState || rotationChanged) {
            ModNetwork.CHANNEL.sendToServer(new DroneInputPacket(state, yRot, xRot));
            lastInputState = state;
            lastSentYRot = yRot;
            lastSentXRot = xRot;
        }
        if (controlling && minecraft.screen == null) {
            while (DroneKeyMappings.DROP_BOMB.consumeClick()) {
                ModNetwork.CHANNEL.sendToServer(new DroneBombPacket());
            }
            while (DroneKeyMappings.EXIT.consumeClick()) {
                ModNetwork.CHANNEL.sendToServer(new DroneExitPacket());
            }
        }
    }

    @SubscribeEvent
    public static void onInteractionInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (Minecraft.getInstance().getCameraEntity() instanceof DroneEntity) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (Minecraft.getInstance().getCameraEntity() instanceof DroneEntity) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getCameraEntity() instanceof DroneEntity && minecraft.player != null) {
            event.setYaw(minecraft.player.getYRot());
            event.setPitch(minecraft.player.getXRot());
        }
    }
}
