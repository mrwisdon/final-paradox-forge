package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.network.DroneTracerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Client-only, entity-free short tracer animation for double-Gatling salvos. */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class DroneTracerRenderer {
    private static final double SPEED_PER_TICK = 12.0D;
    private static final double TRAIL_LENGTH = 2.4D;
    private static final float HALF_WIDTH = 0.035F;
    private static final List<Tracer> TRACERS = new ArrayList<>();

    private static ClientLevel activeLevel;

    private DroneTracerRenderer() {
    }

    public static void add(DroneTracerPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            clear();
            return;
        }
        ensureLevel(level);
        addTrace(level, packet.left());
        addTrace(level, packet.right());
    }

    static void tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            clear();
            return;
        }
        ensureLevel(level);
        Iterator<Tracer> iterator = TRACERS.iterator();
        while (iterator.hasNext()) {
            Tracer tracer = iterator.next();
            ++tracer.age;
            if (tracer.age * SPEED_PER_TICK < tracer.length) {
                continue;
            }
            emitImpact(level, tracer);
            iterator.remove();
        }
    }

    private static void addTrace(ClientLevel level, DroneTracerPacket.Trace trace) {
        Vec3 start = new Vec3(trace.startX(), trace.startY(), trace.startZ());
        Vec3 end = new Vec3(trace.endX(), trace.endY(), trace.endZ());
        Vec3 segment = end.subtract(start);
        double length = segment.length();
        if (length < 1.0E-6D) {
            return;
        }
        Vec3 direction = segment.scale(1.0D / length);
        TRACERS.add(new Tracer(start, end, direction, length, trace.impactType()));
        for (int i = 0; i < 2; i++) {
            level.addParticle(ParticleTypes.FLAME,
                    start.x, start.y, start.z,
                    direction.x * 0.035D, direction.y * 0.035D, direction.z * 0.035D);
        }
        level.addParticle(ParticleTypes.SMOKE,
                start.x, start.y, start.z,
                direction.x * 0.012D, direction.y * 0.012D, direction.z * 0.012D);
    }

    private static void emitImpact(ClientLevel level, Tracer tracer) {
        if (tracer.impactType == DroneTracerPacket.IMPACT_BLOCK) {
            for (int i = 0; i < 3; i++) {
                level.addParticle(ParticleTypes.SMOKE,
                        tracer.end.x, tracer.end.y, tracer.end.z,
                        0.0D, 0.015D + i * 0.004D, 0.0D);
            }
        } else if (tracer.impactType == DroneTracerPacket.IMPACT_ENTITY) {
            for (int i = 0; i < 4; i++) {
                level.addParticle(ParticleTypes.CRIT,
                        tracer.end.x, tracer.end.y, tracer.end.z,
                        (level.random.nextDouble() - 0.5D) * 0.15D,
                        (level.random.nextDouble() - 0.5D) * 0.15D,
                        (level.random.nextDouble() - 0.5D) * 0.15D);
            }
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES
                || TRACERS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level != activeLevel) {
            clear();
            return;
        }

        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = pose.last().pose();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertices = buffers.getBuffer(RenderType.lightning());
        float partialTick = event.getPartialTick();
        for (Tracer tracer : TRACERS) {
            double frontDistance = Math.min(
                    tracer.length, (tracer.age + partialTick) * SPEED_PER_TICK);
            double tailDistance = Math.max(0.0D, frontDistance - TRAIL_LENGTH);
            if (frontDistance <= tailDistance) {
                continue;
            }
            Vec3 start = tracer.start.add(tracer.direction.scale(tailDistance));
            Vec3 end = tracer.start.add(tracer.direction.scale(frontDistance));
            Vec3 reference = Math.abs(tracer.direction.y) < 0.95D
                    ? new Vec3(0.0D, 1.0D, 0.0D) : new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 side = tracer.direction.cross(reference).normalize();
            Vec3 up = side.cross(tracer.direction).normalize();
            drawRibbon(vertices, matrix, start, end, side);
            drawRibbon(vertices, matrix, start, end, up);
        }
        buffers.endBatch(RenderType.lightning());
        pose.popPose();
    }

    private static void drawRibbon(
            VertexConsumer vertices, Matrix4f matrix,
            Vec3 start, Vec3 end, Vec3 normal) {
        Vec3 offset = normal.scale(HALF_WIDTH);
        vertex(vertices, matrix, start.subtract(offset));
        vertex(vertices, matrix, start.add(offset));
        vertex(vertices, matrix, end.add(offset));
        vertex(vertices, matrix, end.subtract(offset));
        vertex(vertices, matrix, end.subtract(offset));
        vertex(vertices, matrix, end.add(offset));
        vertex(vertices, matrix, start.add(offset));
        vertex(vertices, matrix, start.subtract(offset));
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, Vec3 point) {
        vertices.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(255, 214, 48, 230)
                .endVertex();
    }

    private static void ensureLevel(ClientLevel level) {
        if (activeLevel != level) {
            clear();
            activeLevel = level;
        }
    }

    private static void clear() {
        TRACERS.clear();
        activeLevel = null;
    }

    private static final class Tracer {
        private final Vec3 start;
        private final Vec3 end;
        private final Vec3 direction;
        private final double length;
        private final int impactType;
        private int age;

        private Tracer(Vec3 start, Vec3 end, Vec3 direction, double length, int impactType) {
            this.start = start;
            this.end = end;
            this.direction = direction;
            this.length = length;
            this.impactType = impactType;
        }
    }
}
