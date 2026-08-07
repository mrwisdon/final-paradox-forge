package io.github.finalparadox.network;

import io.github.finalparadox.entity.DroneEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Held-state update for the recon drone movement keys. */
public final class DroneInputPacket {
    private final int inputState;
    private final float yRot;
    private final float xRot;

    public DroneInputPacket(int inputState, float yRot, float xRot) {
        this.inputState = inputState;
        this.yRot = yRot;
        this.xRot = xRot;
    }

    public static void encode(DroneInputPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.inputState);
        buffer.writeFloat(packet.yRot);
        buffer.writeFloat(packet.xRot);
    }

    public static DroneInputPacket decode(FriendlyByteBuf buffer) {
        return new DroneInputPacket(
                buffer.readVarInt(), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(
            DroneInputPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            DroneEntity drone = DroneEntity.findFor(player);
            if (drone != null) {
                drone.onInput(player, packet.inputState, packet.yRot, packet.xRot);
            }
        }
        context.setPacketHandled(true);
    }
}
