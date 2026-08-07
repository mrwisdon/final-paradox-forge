package io.github.finalparadox.network;

import io.github.finalparadox.entity.DroneEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Held-state update for the recon drone movement keys. */
public final class DroneInputPacket {
    private final int inputState;

    public DroneInputPacket(int inputState) {
        this.inputState = inputState;
    }

    public static void encode(DroneInputPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.inputState);
    }

    public static DroneInputPacket decode(FriendlyByteBuf buffer) {
        return new DroneInputPacket(buffer.readVarInt());
    }

    public static void handle(
            DroneInputPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            DroneEntity drone = DroneEntity.findFor(player);
            if (drone != null) {
                drone.onInput(player, packet.inputState);
            }
        }
        context.setPacketHandled(true);
    }
}
