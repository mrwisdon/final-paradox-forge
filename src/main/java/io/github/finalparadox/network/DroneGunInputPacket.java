package io.github.finalparadox.network;

import io.github.finalparadox.entity.DroneEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Synchronizes attack-key held changes for the server-authoritative drone Gatling gun. */
public record DroneGunInputPacket(boolean held) {
    public static void encode(DroneGunInputPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.held);
    }

    public static DroneGunInputPacket decode(FriendlyByteBuf buffer) {
        return new DroneGunInputPacket(buffer.readBoolean());
    }

    public static void handle(
            DroneGunInputPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            DroneEntity drone = DroneEntity.findFor(player);
            if (drone != null) {
                drone.setGatlingHeld(player, packet.held);
            }
        }
        context.setPacketHandled(true);
    }
}
