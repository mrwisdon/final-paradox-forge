package io.github.finalparadox.client;

import com.mojang.logging.LogUtils;
import io.github.finalparadox.entity.TerrastalkerRoverEntity;
import io.github.finalparadox.network.TerrastalkerDismountAckPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public final class TerrastalkerClientDismountHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private TerrastalkerClientDismountHandler() {
    }

    public static void handle(TerrastalkerDismountAckPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) return;

        int vehicleBefore = player.getVehicle() == null ? -1 : player.getVehicle().getId();
        Entity remembered = minecraft.level.getEntity(packet.roverEntityId());
        if (remembered instanceof TerrastalkerRoverEntity rover) {
            rover.confirmClientDismount(player);
        } else if (player.getVehicle() instanceof TerrastalkerRoverEntity rover) {
            rover.confirmClientDismount(player);
        }

        TerrastalkerRoverInput.onDismountConfirmed();
        player.setShiftKeyDown(false);
        player.setDeltaMovement(Vec3.ZERO);
        player.setPos(packet.x(), packet.y(), packet.z());
        player.setYRot(packet.yaw());
        player.setXRot(packet.pitch());
        LOGGER.info("Terrastalker client dismount ack for rover {}: vehicle {} -> {}",
                packet.roverEntityId(), vehicleBefore,
                player.getVehicle() == null ? -1 : player.getVehicle().getId());
    }
}
