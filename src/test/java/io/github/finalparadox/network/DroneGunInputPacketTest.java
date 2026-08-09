package io.github.finalparadox.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DroneGunInputPacketTest {
    @Test
    void preservesHeldTrue() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        DroneGunInputPacket.encode(new DroneGunInputPacket(true), buffer);

        assertTrue(DroneGunInputPacket.decode(buffer).held());
    }

    @Test
    void preservesHeldFalse() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        DroneGunInputPacket.encode(new DroneGunInputPacket(false), buffer);

        assertFalse(DroneGunInputPacket.decode(buffer).held());
    }
}
