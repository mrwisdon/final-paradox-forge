package io.github.finalparadox.client;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.entity.TerrastalkerRoverEntity;
import io.github.finalparadox.network.ModNetwork;
import io.github.finalparadox.network.TerrastalkerDismountPacket;
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
    private static boolean wasShiftDown;
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
        boolean controlsActive = minecraft.screen == null && rover != null;
        if (controlsActive && shiftDown && !wasShiftDown) {
            ModNetwork.CHANNEL.sendToServer(new TerrastalkerDismountPacket());
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
        wasShiftDown = shiftDown;
        wasAttackDown = attackDown;
        wasJumpDown = jumpDown;
        wasUseDown = useDown;
    }

    private static void resetLocalState() {
        wasShiftDown = false;
        wasAttackDown = false;
        wasJumpDown = false;
        wasUseDown = false;
        sentFireInput = false;
        controlledRoverId = -1;
    }
}
