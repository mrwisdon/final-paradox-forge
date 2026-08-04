package io.github.finalparadox.network;

import io.github.finalparadox.client.TerrastalkerClientDismountHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Authoritative server confirmation that clears any client-only riding state. */
public record TerrastalkerDismountAckPacket(
        int roverEntityId,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {
    public static void encode(
            TerrastalkerDismountAckPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.roverEntityId);
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
        buffer.writeFloat(packet.yaw);
        buffer.writeFloat(packet.pitch);
    }

    public static TerrastalkerDismountAckPacket decode(FriendlyByteBuf buffer) {
        return new TerrastalkerDismountAckPacket(
                buffer.readVarInt(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readFloat(),
                buffer.readFloat());
    }

    public static void handle(
            TerrastalkerDismountAckPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> TerrastalkerClientDismountHandler.handle(packet));
        context.setPacketHandled(true);
    }
}
