package io.github.finalparadox.client;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.entity.DroneEntity;
import io.github.finalparadox.network.DroneBombPacket;
import io.github.finalparadox.network.DroneExitPacket;
import io.github.finalparadox.network.DroneInputPacket;
import io.github.finalparadox.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderHandEvent;
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
            return;
        }

        boolean controlling = minecraft.getCameraEntity() instanceof DroneEntity;
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
        if (state != lastInputState) {
            ModNetwork.CHANNEL.sendToServer(new DroneInputPacket(state));
            lastInputState = state;
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
}
