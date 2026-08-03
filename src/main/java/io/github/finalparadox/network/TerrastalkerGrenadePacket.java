package io.github.finalparadox.network;

import io.github.finalparadox.entity.TerrastalkerRoverEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** One Use-key press edge while riding a Terrastalker rover: launch the
 *  defensive grenade ring around the vehicle. */
public final class TerrastalkerGrenadePacket {
    public TerrastalkerGrenadePacket() {
    }

    public static void encode(
            TerrastalkerGrenadePacket packet,
            FriendlyByteBuf buffer
    ) {
    }

    public static TerrastalkerGrenadePacket decode(FriendlyByteBuf buffer) {
        return new TerrastalkerGrenadePacket();
    }

    public static void handle(
            TerrastalkerGrenadePacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null && player.getVehicle() instanceof TerrastalkerRoverEntity rover) {
            rover.launchDefensiveGrenades(player);
        }
        context.setPacketHandled(true);
    }
}
