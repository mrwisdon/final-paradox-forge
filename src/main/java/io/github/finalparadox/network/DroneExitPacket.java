package io.github.finalparadox.network;

import io.github.finalparadox.entity.DroneEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Dismisses the recon drone and returns its passenger to the anchor. */
public final class DroneExitPacket {
    public DroneExitPacket() {
    }

    public static void encode(DroneExitPacket packet, FriendlyByteBuf buffer) {
    }

    public static DroneExitPacket decode(FriendlyByteBuf buffer) {
        return new DroneExitPacket();
    }

    public static void handle(
            DroneExitPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            DroneEntity.endFor(player);
        }
        context.setPacketHandled(true);
    }
}
