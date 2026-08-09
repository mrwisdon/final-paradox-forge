package io.github.finalparadox.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class DroneDismountPacketTest {
    @Test
    void preservesAuthorizedDroneEntityId() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        DroneDismountPacket.encode(new DroneDismountPacket(413), buffer);

        assertEquals(413, DroneDismountPacket.decode(buffer).droneEntityId());
    }
}
