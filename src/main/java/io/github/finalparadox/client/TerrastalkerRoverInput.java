package io.github.finalparadox.client;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.entity.TerrastalkerRoverEntity;
import io.github.finalparadox.network.ModNetwork;
import io.github.finalparadox.network.TerrastalkerDismountPacket;
import io.github.finalparadox.network.TerrastalkerExitPacket;
import io.github.finalparadox.network.TerrastalkerFireInputPacket;
import io.github.finalparadox.network.TerrastalkerGrenadePacket;
import io.github.finalparadox.network.TerrastalkerJumpPacket;
import io.github.finalparadox.network.TerrastalkerMissilePacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Separately synchronizes the Shift press edge and its physical held state. */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class TerrastalkerRoverInput {
    private static final TerrastalkerDismountInputState DISMOUNT_INPUT =
            new TerrastalkerDismountInputState();
    private static final TerrastalkerRoverTrackingState ROVER_TRACKING =
            new TerrastalkerRoverTrackingState();
    private static boolean wasAttackDown;
    private static boolean wasJumpDown;
    private static boolean wasUseDown;
    private static boolean sentFireInput;
    private static int controlledRoverId = -1;

    private TerrastalkerRoverInput() {
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            resetLocalState();
            return;
        }

        boolean shiftDown = minecraft.options.keyShift.isDown();
        TerrastalkerRoverEntity rover = minecraft.player.getVehicle()
                instanceof TerrastalkerRoverEntity mountedRover ? mountedRover : null;
        ROVER_TRACKING.observe(rover == null
                ? TerrastalkerRoverTrackingState.NO_ROVER : rover.getId());
        boolean controlsActive = minecraft.screen == null && rover != null;
        if (DISMOUNT_INPUT.tick(
                shiftDown, rover != null, minecraft.screen != null)) {
            ModNetwork.CHANNEL.sendToServer(new TerrastalkerDismountPacket(
                    ROVER_TRACKING.lastKnownRoverId()));
        }
        while (TerrastalkerKeyMappings.DISMOUNT.consumeClick()) {
            // Send even if the client has temporarily lost its vehicle link;
            // the server validates the actual mount and can recover a
            // client/server passenger desync with this dedicated key.
            if (minecraft.screen == null) {
                ModNetwork.CHANNEL.sendToServer(new TerrastalkerExitPacket(
                        ROVER_TRACKING.lastKnownRoverId()));
            }
        }
        boolean attackDown = minecraft.options.keyAttack.isDown();
        if (controlsActive && attackDown && !wasAttackDown) {
            ModNetwork.CHANNEL.sendToServer(new TerrastalkerMissilePacket());
        }
        boolean jumpDown = minecraft.options.keyJump.isDown();
        if (controlsActive && jumpDown && !wasJumpDown) {
            ModNetwork.CHANNEL.sendToServer(new TerrastalkerJumpPacket());
        }
        boolean useDown = minecraft.options.keyUse.isDown();
        if (controlsActive && useDown && !wasUseDown) {
            ModNetwork.CHANNEL.sendToServer(new TerrastalkerGrenadePacket());
        }

        boolean fireInput = controlsActive && shiftDown;
        if (rover != null) {
            if (controlledRoverId != rover.getId() || sentFireInput != fireInput) {
                ModNetwork.CHANNEL.sendToServer(new TerrastalkerFireInputPacket(fireInput));
                controlledRoverId = rover.getId();
                sentFireInput = fireInput;
            }
        } else {
            controlledRoverId = -1;
            sentFireInput = false;
        }
        wasAttackDown = attackDown;
        wasJumpDown = jumpDown;
        wasUseDown = useDown;
    }

    private static void resetLocalState() {
        DISMOUNT_INPUT.reset();
        ROVER_TRACKING.reset();
        wasAttackDown = false;
        wasJumpDown = false;
        wasUseDown = false;
        sentFireInput = false;
        controlledRoverId = -1;
    }

    static void onDismountConfirmed() {
        resetLocalState();
    }
}
