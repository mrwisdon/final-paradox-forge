package io.github.finalparadox.network;

import io.github.finalparadox.client.DroneInput;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server authorization that lets the owning client leave the locked vehicle. */
public record DroneDismountPacket(int droneEntityId) {
    public static void encode(DroneDismountPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.droneEntityId);
    }

    public static DroneDismountPacket decode(FriendlyByteBuf buffer) {
        return new DroneDismountPacket(buffer.readVarInt());
    }

    public static void handle(
            DroneDismountPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> DroneInput.handleAuthorizedDismount(packet.droneEntityId));
        context.setPacketHandled(true);
    }
}
