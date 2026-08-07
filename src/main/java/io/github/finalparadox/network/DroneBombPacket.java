package io.github.finalparadox.network;

import io.github.finalparadox.entity.DroneEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** One bomb-drop request from a recon drone controller. */
public final class DroneBombPacket {
    public DroneBombPacket() {
    }

    public static void encode(DroneBombPacket packet, FriendlyByteBuf buffer) {
    }

    public static DroneBombPacket decode(FriendlyByteBuf buffer) {
        return new DroneBombPacket();
    }

    public static void handle(
            DroneBombPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            DroneEntity drone = DroneEntity.findFor(player);
            if (drone != null) {
                drone.dropBomb(player);
            }
        }
        context.setPacketHandled(true);
    }
}
