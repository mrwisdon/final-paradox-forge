package io.github.finalparadox.network;

import io.github.finalparadox.entity.TerrastalkerRoverEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** One client-side Shift press while riding a Terrastalker rover. */
public final class TerrastalkerDismountPacket {
    public static void encode(TerrastalkerDismountPacket packet, FriendlyByteBuf buffer) {
    }

    public static TerrastalkerDismountPacket decode(FriendlyByteBuf buffer) {
        return new TerrastalkerDismountPacket();
    }

    public static void handle(
            TerrastalkerDismountPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null && player.getVehicle() instanceof TerrastalkerRoverEntity rover) {
            rover.onDismountAttempt(player);
        }
        context.setPacketHandled(true);
    }
}
