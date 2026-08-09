package io.github.finalparadox.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class DroneTracerPacketTest {
    @Test
    void roundTripsBothIndependentTracesAndImpactTypes() {
        DroneTracerPacket packet = new DroneTracerPacket(
                new DroneTracerPacket.Trace(
                        -0.3D, 2.0D, 3.0D, 10.0D, 11.0D, 12.0D,
                        DroneTracerPacket.IMPACT_ENTITY),
                new DroneTracerPacket.Trace(
                        0.3D, 4.0D, 5.0D, 20.0D, 21.0D, 22.0D,
                        DroneTracerPacket.IMPACT_BLOCK));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        DroneTracerPacket.encode(packet, buffer);

        assertEquals(packet, DroneTracerPacket.decode(buffer));
    }

    @Test
    void unknownImpactTypeDecodesAsMiss() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        writeTrace(buffer, 99);
        writeTrace(buffer, DroneTracerPacket.IMPACT_MISS);

        DroneTracerPacket decoded = DroneTracerPacket.decode(buffer);

        assertEquals(DroneTracerPacket.IMPACT_MISS, decoded.left().impactType());
    }

    private static void writeTrace(FriendlyByteBuf buffer, int impactType) {
        for (int coordinate = 0; coordinate < 6; coordinate++) {
            buffer.writeDouble(coordinate);
        }
        buffer.writeByte(impactType);
    }
}
