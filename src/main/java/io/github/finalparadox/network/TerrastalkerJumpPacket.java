package io.github.finalparadox.network;

import io.github.finalparadox.entity.TerrastalkerRoverEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** One Jump-key press edge while riding a Terrastalker rover: jump 4 blocks. */
public final class TerrastalkerJumpPacket {
    public TerrastalkerJumpPacket() {
    }

    public static void encode(
            TerrastalkerJumpPacket packet,
            FriendlyByteBuf buffer
    ) {
    }

    public static TerrastalkerJumpPacket decode(FriendlyByteBuf buffer) {
        return new TerrastalkerJumpPacket();
    }

    public static void handle(
            TerrastalkerJumpPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null && player.getVehicle() instanceof TerrastalkerRoverEntity rover) {
            rover.doJump(player);
        }
        context.setPacketHandled(true);
    }
}
