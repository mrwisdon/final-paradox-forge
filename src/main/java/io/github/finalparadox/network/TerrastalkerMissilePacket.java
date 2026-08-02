package io.github.finalparadox.network;

import io.github.finalparadox.entity.TerrastalkerRoverEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** One Attack-key press edge while riding a Terrastalker rover: launch a missile. */
public final class TerrastalkerMissilePacket {
    public TerrastalkerMissilePacket() {
    }

    public static void encode(
            TerrastalkerMissilePacket packet,
            FriendlyByteBuf buffer
    ) {
    }

    public static TerrastalkerMissilePacket decode(FriendlyByteBuf buffer) {
        return new TerrastalkerMissilePacket();
    }

    public static void handle(
            TerrastalkerMissilePacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null && player.getVehicle() instanceof TerrastalkerRoverEntity rover) {
            rover.launchMissile(player);
        }
        context.setPacketHandled(true);
    }
}
