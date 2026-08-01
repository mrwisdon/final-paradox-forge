package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.finalparadox.entity.NightfallLaserEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class NightfallLaserRenderer extends EntityRenderer<NightfallLaserEntity> {
    private static final int SEGMENT_COUNT = 38;
    private static final double SEGMENT_SPACING = 0.6D;
    private static final double BEAM_HEIGHT = 1.3D;
    private static final double BEAM_START = 0.25D;
    private static final double BEAM_END = SEGMENT_COUNT * SEGMENT_SPACING + 0.35D;

    public NightfallLaserRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(NightfallLaserEntity entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffers, int packedLight) {
        Vec3 direction = entity.beamDirection().normalize();
        Vec3 reference = Math.abs(direction.y) < 0.95D
                ? new Vec3(0.0D, 1.0D, 0.0D)
                : new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 side = direction.cross(reference).normalize();
        Vec3 vertical = side.cross(direction).normalize();
        Vec3 diagonalA = side.add(vertical).normalize();
        Vec3 diagonalB = side.subtract(vertical).normalize();
        Vec3 height = new Vec3(0.0D, BEAM_HEIGHT, 0.0D);
        Vec3 start = direction.scale(BEAM_START).add(height);
        Vec3 end = direction.scale(BEAM_END).add(height);

        Matrix4f matrix = pose.last().pose();
        VertexConsumer vertices = buffers.getBuffer(RenderType.lightning());

        // A soft four-plane aura gives the sweep volume without the old row of cubes.
        drawRibbon(vertices, matrix, start, end, side, 0.52F, 18, 96, 255, 28);
        drawRibbon(vertices, matrix, start, end, vertical, 0.52F, 18, 96, 255, 28);
        drawRibbon(vertices, matrix, start, end, diagonalA, 0.38F, 40, 145, 255, 22);
        drawRibbon(vertices, matrix, start, end, diagonalB, 0.38F, 40, 145, 255, 22);

        // Saturated cyan body and a narrow white-hot core remain readable during rotation.
        drawRibbon(vertices, matrix, start, end, side, 0.27F, 30, 155, 255, 150);
        drawRibbon(vertices, matrix, start, end, vertical, 0.27F, 30, 155, 255, 150);
        drawRibbon(vertices, matrix, start, end, diagonalA, 0.18F, 80, 205, 255, 105);
        drawRibbon(vertices, matrix, start, end, diagonalB, 0.18F, 80, 205, 255, 105);
        drawRibbon(vertices, matrix, start, end, side, 0.075F, 235, 252, 255, 235);
        drawRibbon(vertices, matrix, start, end, vertical, 0.075F, 235, 252, 255, 235);

        // Preserve the source's eight-segment travelling pulse as smooth energy bands.
        int phase = entity.phase();
        for (int segment = 1; segment <= SEGMENT_COUNT; segment++) {
            if (Math.floorMod(segment - phase, 8) != 3) {
                continue;
            }
            double centerDistance = segment * SEGMENT_SPACING;
            Vec3 pulseStart = direction.scale(centerDistance - 0.22D).add(height);
            Vec3 pulseEnd = direction.scale(centerDistance + 0.22D).add(height);
            drawRibbon(vertices, matrix, pulseStart, pulseEnd, side,
                    0.39F, 205, 245, 255, 205);
            drawRibbon(vertices, matrix, pulseStart, pulseEnd, vertical,
                    0.39F, 205, 245, 255, 205);
            drawRibbon(vertices, matrix, pulseStart, pulseEnd, diagonalA,
                    0.28F, 255, 255, 255, 185);
            drawRibbon(vertices, matrix, pulseStart, pulseEnd, diagonalB,
                    0.28F, 255, 255, 255, 185);
        }
        super.render(entity, yaw, partialTick, pose, buffers, packedLight);
    }

    private static void drawRibbon(VertexConsumer vertices, Matrix4f matrix,
                                   Vec3 start, Vec3 end, Vec3 normal, float halfWidth,
                                   int red, int green, int blue, int alpha) {
        Vec3 offset = normal.scale(halfWidth);
        vertex(vertices, matrix, start.subtract(offset), red, green, blue, alpha);
        vertex(vertices, matrix, start.add(offset), red, green, blue, alpha);
        vertex(vertices, matrix, end.add(offset), red, green, blue, alpha);
        vertex(vertices, matrix, end.subtract(offset), red, green, blue, alpha);
        vertex(vertices, matrix, end.subtract(offset), red, green, blue, alpha);
        vertex(vertices, matrix, end.add(offset), red, green, blue, alpha);
        vertex(vertices, matrix, start.add(offset), red, green, blue, alpha);
        vertex(vertices, matrix, start.subtract(offset), red, green, blue, alpha);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, Vec3 position,
                               int red, int green, int blue, int alpha) {
        vertices.vertex(matrix, (float) position.x, (float) position.y, (float) position.z)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(NightfallLaserEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
