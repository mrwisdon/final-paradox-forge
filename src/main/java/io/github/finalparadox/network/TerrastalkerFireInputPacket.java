package io.github.finalparadox.network;

import io.github.finalparadox.entity.TerrastalkerRoverEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Synchronizes the physical Shift held state independently of vanilla sneaking. */
public final class TerrastalkerFireInputPacket {
    private final boolean firing;

    public TerrastalkerFireInputPacket(boolean firing) {
        this.firing = firing;
    }

    public static void encode(
            TerrastalkerFireInputPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeBoolean(packet.firing);
    }

    public static TerrastalkerFireInputPacket decode(FriendlyByteBuf buffer) {
        return new TerrastalkerFireInputPacket(buffer.readBoolean());
    }

    public static void handle(
            TerrastalkerFireInputPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null && player.getVehicle() instanceof TerrastalkerRoverEntity rover) {
            rover.onFireInput(player, packet.firing);
        }
        context.setPacketHandled(true);
    }
}
