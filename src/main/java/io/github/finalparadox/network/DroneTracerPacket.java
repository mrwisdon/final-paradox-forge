package io.github.finalparadox.network;

import io.github.finalparadox.client.DroneTracerRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** One compact S2C visual packet for both rays in a Gatling salvo. */
public record DroneTracerPacket(Trace left, Trace right) {
    public static final int IMPACT_MISS = 0;
    public static final int IMPACT_BLOCK = 1;
    public static final int IMPACT_ENTITY = 2;

    public static void encode(DroneTracerPacket packet, FriendlyByteBuf buffer) {
        encodeTrace(packet.left, buffer);
        encodeTrace(packet.right, buffer);
    }

    public static DroneTracerPacket decode(FriendlyByteBuf buffer) {
        return new DroneTracerPacket(decodeTrace(buffer), decodeTrace(buffer));
    }

    private static void encodeTrace(Trace trace, FriendlyByteBuf buffer) {
        buffer.writeDouble(trace.startX);
        buffer.writeDouble(trace.startY);
        buffer.writeDouble(trace.startZ);
        buffer.writeDouble(trace.endX);
        buffer.writeDouble(trace.endY);
        buffer.writeDouble(trace.endZ);
        buffer.writeByte(trace.impactType);
    }

    private static Trace decodeTrace(FriendlyByteBuf buffer) {
        return new Trace(
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                sanitizeImpact(buffer.readUnsignedByte()));
    }

    private static int sanitizeImpact(int impactType) {
        return impactType >= IMPACT_MISS && impactType <= IMPACT_ENTITY
                ? impactType : IMPACT_MISS;
    }

    public static void handle(
            DroneTracerPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> DroneTracerRenderer.add(packet));
        context.setPacketHandled(true);
    }

    public record Trace(
            double startX, double startY, double startZ,
            double endX, double endY, double endZ,
            int impactType) {
    }
}
