package io.github.finalparadox.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TerrastalkerDismountAckPacketTest {
    @Test
    void preservesRoverAndAuthoritativeExitPose() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        TerrastalkerDismountAckPacket packet = new TerrastalkerDismountAckPacket(
                42, 10.25D, 64.0D, -7.5D, 135.0F, -12.0F);

        TerrastalkerDismountAckPacket.encode(packet, buffer);
        TerrastalkerDismountAckPacket decoded =
                TerrastalkerDismountAckPacket.decode(buffer);

        assertEquals(42, decoded.roverEntityId());
        assertEquals(10.25D, decoded.x());
        assertEquals(64.0D, decoded.y());
        assertEquals(-7.5D, decoded.z());
        assertEquals(135.0F, decoded.yaw());
        assertEquals(-12.0F, decoded.pitch());
    }
}
