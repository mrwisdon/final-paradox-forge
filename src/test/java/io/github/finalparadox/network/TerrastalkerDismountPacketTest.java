package io.github.finalparadox.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TerrastalkerDismountPacketTest {
    @Test
    void doubleShiftPacketPreservesRememberedRoverId() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        TerrastalkerDismountPacket.encode(new TerrastalkerDismountPacket(42), buffer);
        TerrastalkerDismountPacket decoded = TerrastalkerDismountPacket.decode(buffer);

        assertEquals(42, decoded.roverEntityId());
    }

    @Test
    void dedicatedKeyPacketPreservesRememberedRoverId() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        TerrastalkerExitPacket.encode(new TerrastalkerExitPacket(84), buffer);
        TerrastalkerExitPacket decoded = TerrastalkerExitPacket.decode(buffer);

        assertEquals(84, decoded.roverEntityId());
    }
}
