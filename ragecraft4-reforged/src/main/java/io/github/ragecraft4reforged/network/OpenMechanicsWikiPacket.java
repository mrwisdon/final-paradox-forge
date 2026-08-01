package io.github.ragecraft4reforged.network;

import net.minecraft.network.FriendlyByteBuf;

public record OpenMechanicsWikiPacket() {
    public static void encode(OpenMechanicsWikiPacket packet, FriendlyByteBuf buffer) {
    }

    public static OpenMechanicsWikiPacket decode(FriendlyByteBuf buffer) {
        return new OpenMechanicsWikiPacket();
    }
}
