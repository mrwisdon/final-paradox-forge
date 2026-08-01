package io.github.ragecraft4reforged.network;

import net.minecraft.network.FriendlyByteBuf;

public record ManaSyncPacket(int mana, int maximum) {
    public static void encode(ManaSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.mana);
        buffer.writeVarInt(packet.maximum);
    }

    public static ManaSyncPacket decode(FriendlyByteBuf buffer) {
        return new ManaSyncPacket(buffer.readVarInt(), buffer.readVarInt());
    }
}
