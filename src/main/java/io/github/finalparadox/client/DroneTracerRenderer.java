package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.network.DroneTracerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.BlockState;
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
    private static final double MUZZLE_FLASH_TICKS = 0.20D;
    private static final float CORE_HALF_WIDTH = 0.012F;
    private static final float GLOW_HALF_WIDTH = 0.027F;
    private static final List<Tracer> TRACERS = new ArrayList<>();

    private static ClientLevel activeLevel;
    private static int salvoSequence;

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
        boolean emitSmoke = (++salvoSequence & 3) == 0;
        double spawnTime = currentTime(level, minecraft.getFrameTime());
        addTrace(level, packet.left(), spawnTime, emitSmoke);
        addTrace(level, packet.right(), spawnTime, emitSmoke);
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
            double elapsed = DroneTracerTimeline.elapsed(
                    level.getGameTime(), tracer.spawnTime);
            if (!tracer.renderedOnce
                    && DroneTracerTimeline.isStaleWithoutRender(elapsed)) {
                iterator.remove();
                continue;
            }
            if (tracer.renderedOnce && !tracer.impactEmitted
                    && DroneTracerTimeline.hasArrived(elapsed, tracer.length)) {
                emitImpact(level, tracer);
                tracer.impactEmitted = true;
            }
            if (tracer.renderedOnce && tracer.impactEmitted
                    && DroneTracerTimeline.isExpired(elapsed, tracer.length)) {
                iterator.remove();
            }
        }
    }

    private static void addTrace(
            ClientLevel level, DroneTracerPacket.Trace trace,
            double spawnTime, boolean emitSmoke) {
        Vec3 start = new Vec3(trace.startX(), trace.startY(), trace.startZ());
        Vec3 end = new Vec3(trace.endX(), trace.endY(), trace.endZ());
        Vec3 segment = end.subtract(start);
        double length = segment.length();
        if (length < 1.0E-6D) {
            return;
        }
        Vec3 direction = segment.scale(1.0D / length);
        TRACERS.add(new Tracer(
                start, end, direction, length, trace.impactType(), spawnTime));
        if (emitSmoke) {
            level.addParticle(ParticleTypes.SMOKE,
                    start.x, start.y, start.z,
                    -direction.x * 0.008D, 0.008D,
                    -direction.z * 0.008D);
        }
    }

    private static void emitImpact(ClientLevel level, Tracer tracer) {
        if (tracer.impactType == DroneTracerPacket.IMPACT_BLOCK) {
            BlockPos insideBlock = BlockPos.containing(
                    tracer.end.add(tracer.direction.scale(0.02D)));
            BlockState state = level.getBlockState(insideBlock);
            if (!state.isAir()) {
                BlockParticleOption fragments = new BlockParticleOption(
                        ParticleTypes.BLOCK, state);
                for (int i = 0; i < 3; i++) {
                    level.addParticle(fragments,
                            tracer.end.x, tracer.end.y, tracer.end.z,
                            randomVelocity(level, 0.09D),
                            0.025D + level.random.nextDouble() * 0.07D,
                            randomVelocity(level, 0.09D));
                }
            }
            level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    tracer.end.x, tracer.end.y, tracer.end.z,
                    randomVelocity(level, 0.06D), 0.025D,
                    randomVelocity(level, 0.06D));
            level.addParticle(ParticleTypes.SMOKE,
                    tracer.end.x, tracer.end.y, tracer.end.z,
                    0.0D, 0.012D, 0.0D);
        } else if (tracer.impactType == DroneTracerPacket.IMPACT_ENTITY) {
            for (int i = 0; i < 2; i++) {
                level.addParticle(ParticleTypes.CRIT,
                        tracer.end.x, tracer.end.y, tracer.end.z,
                        randomVelocity(level, 0.08D),
                        randomVelocity(level, 0.08D),
                        randomVelocity(level, 0.08D));
            }
        }
    }

    private static double randomVelocity(ClientLevel level, double scale) {
        return (level.random.nextDouble() - 0.5D) * scale;
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
        double renderTime = currentTime(minecraft.level, partialTick);
        for (Tracer tracer : TRACERS) {
            double elapsed = DroneTracerTimeline.elapsed(renderTime, tracer.spawnTime);
            Vec3 reference = Math.abs(tracer.direction.y) < 0.95D
                    ? new Vec3(0.0D, 1.0D, 0.0D) : new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 side = tracer.direction.cross(reference).normalize();
            Vec3 up = side.cross(tracer.direction).normalize();

            drawMuzzleFlash(vertices, matrix, tracer, side, up, elapsed);

            double visualElapsed = DroneTracerTimeline.visualElapsed(
                    elapsed, tracer.length, tracer.renderedOnce);
            double frontDistance = DroneTracerTimeline.frontDistance(
                    visualElapsed, tracer.length);
            double tailDistance = DroneTracerTimeline.tailDistance(frontDistance);
            if (frontDistance > tailDistance) {
                Vec3 start = tracer.start.add(tracer.direction.scale(tailDistance));
                Vec3 end = tracer.start.add(tracer.direction.scale(frontDistance));
                float fade = (float) DroneTracerTimeline.arrivalFade(
                        visualElapsed, tracer.length);
                drawLayer(vertices, matrix, start, end, side, up,
                        GLOW_HALF_WIDTH, 255, 126, 24, 0, alpha(62, fade));
                drawLayer(vertices, matrix, start, end, side, up,
                        CORE_HALF_WIDTH, 255, 238, 164, alpha(20, fade), alpha(245, fade));
                tracer.renderedOnce = true;
            }
        }
        buffers.endBatch(RenderType.lightning());
        pose.popPose();
    }

    private static void drawMuzzleFlash(
            VertexConsumer vertices, Matrix4f matrix, Tracer tracer,
            Vec3 side, Vec3 up, double elapsed) {
        double flashAge = tracer.muzzleRendered
                ? elapsed : Math.min(elapsed, MUZZLE_FLASH_TICKS * 0.5D);
        float fade = (float) Math.max(0.0D,
                1.0D - flashAge / MUZZLE_FLASH_TICKS);
        if (fade <= 0.0F) {
            return;
        }
        Vec3 tip = tracer.start.add(tracer.direction.scale(0.16D));
        drawTaperedRibbon(vertices, matrix, tracer.start, tip, side,
                0.050F, 0.006F, 255, 203, 70,
                alpha(220, fade), alpha(35, fade));
        drawTaperedRibbon(vertices, matrix, tracer.start, tip, up,
                0.050F, 0.006F, 255, 238, 175,
                alpha(230, fade), alpha(45, fade));
        tracer.muzzleRendered = true;
    }

    private static void drawLayer(
            VertexConsumer vertices, Matrix4f matrix,
            Vec3 start, Vec3 end, Vec3 side, Vec3 up,
            float halfWidth, int red, int green, int blue,
            int tailAlpha, int frontAlpha) {
        drawTaperedRibbon(vertices, matrix, start, end, side,
                halfWidth, halfWidth, red, green, blue, tailAlpha, frontAlpha);
        drawTaperedRibbon(vertices, matrix, start, end, up,
                halfWidth, halfWidth, red, green, blue, tailAlpha, frontAlpha);
    }

    private static void drawTaperedRibbon(
            VertexConsumer vertices, Matrix4f matrix,
            Vec3 start, Vec3 end, Vec3 normal,
            float startHalfWidth, float endHalfWidth,
            int red, int green, int blue, int startAlpha, int endAlpha) {
        Vec3 startOffset = normal.scale(startHalfWidth);
        Vec3 endOffset = normal.scale(endHalfWidth);
        vertex(vertices, matrix, start.subtract(startOffset), red, green, blue, startAlpha);
        vertex(vertices, matrix, start.add(startOffset), red, green, blue, startAlpha);
        vertex(vertices, matrix, end.add(endOffset), red, green, blue, endAlpha);
        vertex(vertices, matrix, end.subtract(endOffset), red, green, blue, endAlpha);
        vertex(vertices, matrix, end.subtract(endOffset), red, green, blue, endAlpha);
        vertex(vertices, matrix, end.add(endOffset), red, green, blue, endAlpha);
        vertex(vertices, matrix, start.add(startOffset), red, green, blue, startAlpha);
        vertex(vertices, matrix, start.subtract(startOffset), red, green, blue, startAlpha);
    }

    private static int alpha(int base, float fade) {
        return Math.max(0, Math.min(255, Math.round(base * fade)));
    }

    private static void vertex(
            VertexConsumer vertices, Matrix4f matrix, Vec3 point,
            int red, int green, int blue, int alpha) {
        vertices.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    private static double currentTime(ClientLevel level, float partialTick) {
        return level.getGameTime() + Math.max(0.0F, Math.min(1.0F, partialTick));
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
        salvoSequence = 0;
    }

    private static final class Tracer {
        private final Vec3 start;
        private final Vec3 end;
        private final Vec3 direction;
        private final double length;
        private final int impactType;
        private final double spawnTime;
        private boolean renderedOnce;
        private boolean muzzleRendered;
        private boolean impactEmitted;

        private Tracer(
                Vec3 start, Vec3 end, Vec3 direction, double length,
                int impactType, double spawnTime) {
            this.start = start;
            this.end = end;
            this.direction = direction;
            this.length = length;
            this.impactType = impactType;
            this.spawnTime = spawnTime;
        }
    }
}
