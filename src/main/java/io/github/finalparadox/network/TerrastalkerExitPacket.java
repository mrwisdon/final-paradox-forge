package io.github.finalparadox.network;

import io.github.finalparadox.entity.TerrastalkerRoverEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** One press of the dedicated Terrastalker dismount key. */
public final class TerrastalkerExitPacket {
    private final int roverEntityId;

    public TerrastalkerExitPacket(int roverEntityId) {
        this.roverEntityId = roverEntityId;
    }

    int roverEntityId() {
        return roverEntityId;
    }

    public static void encode(TerrastalkerExitPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.roverEntityId);
    }

    public static TerrastalkerExitPacket decode(FriendlyByteBuf buffer) {
        return new TerrastalkerExitPacket(buffer.readVarInt());
    }

    public static void handle(
            TerrastalkerExitPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            TerrastalkerRoverEntity rover = TerrastalkerRoverEntity.resolveDismountTarget(
                    player, packet.roverEntityId);
            if (rover != null) rover.onDismountKey(player);
        }
        context.setPacketHandled(true);
    }
}
