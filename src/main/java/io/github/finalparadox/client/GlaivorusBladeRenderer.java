package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.GlaivorusBladeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Recreates the map's 84 armor-stand block mosaic as one efficient renderer. */
public final class GlaivorusBladeRenderer extends EntityRenderer<GlaivorusBladeEntity> {
    private static final List<BladePart> PARTS = createParts();
    private static final float DISPLAY_SCALE = 0.61F;
    private final BlockRenderDispatcher blockRenderer;

    public GlaivorusBladeRenderer(EntityRendererProvider.Context context) {
        super(context);
        blockRenderer = context.getBlockRenderDispatcher();
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(GlaivorusBladeEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(GlaivorusBladeEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        for (BladePart part : PARTS) {
            poseStack.pushPose();
            poseStack.translate(part.x, part.y + 0.1D, part.z);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
            poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
            poseStack.translate(-0.5D, -0.5D, -0.5D);
            blockRenderer.renderSingleBlock(part.state, poseStack, buffers, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    private static List<BladePart> createParts() {
        List<BladePart> parts = new ArrayList<>(84);
        BlockState stone = Blocks.STONE_SLAB.defaultBlockState();
        BlockState smooth = Blocks.SMOOTH_STONE_SLAB.defaultBlockState();
        BlockState black = Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState();
        BlockState red = Blocks.REDSTONE_BLOCK.defaultBlockState();

        addVertical(parts, stone, 0.0D, 1.22D, 8);

        parts.add(new BladePart(0.0D, 0.61D, 0.0D, smooth));
        addVertical(parts, smooth, 0.305D, 0.915D, 8);
        addVertical(parts, smooth, -0.305D, 0.915D, 8);

        parts.add(new BladePart(-0.305D, 0.305D, 0.0D, black));
        parts.add(new BladePart(0.305D, 0.305D, 0.0D, black));
        parts.add(new BladePart(0.0D, 0.0D, 0.0D, black));
        parts.add(new BladePart(-0.61D, 0.61D, 0.0D, black));
        parts.add(new BladePart(0.61D, 0.61D, 0.0D, black));
        addVertical(parts, black, 0.61D, 1.22D, 8);
        addVertical(parts, black, -0.61D, 1.22D, 8);

        addPair(parts, black, 1.22D, 5.49D);
        addPair(parts, black, 1.83D, 5.49D);
        addPair(parts, black, 1.22D, 6.10D);
        addPair(parts, black, 1.83D, 6.10D);
        addPair(parts, black, 2.135D, 5.795D);
        addPair(parts, black, 0.915D, 6.405D);
        addPair(parts, black, 0.305D, 6.405D);

        parts.add(new BladePart(0.0D, 6.10D, -0.125D, red));
        addPair(parts, stone, 0.61D, 6.10D);
        addPair(parts, stone, 0.305D, 5.80D);
        addPair(parts, stone, 0.915D, 5.80D);
        addPair(parts, stone, 1.525D, 5.80D);

        for (double y : new double[]{7.015D, 7.625D, 8.235D, 8.845D}) {
            addPair(parts, black, 0.305D, y);
        }
        parts.add(new BladePart(0.0D, 9.15D, 0.0D, black));
        addPair(parts, black, 0.61D, 8.54D);
        parts.add(new BladePart(0.0D, 8.54D, -0.125D, red));
        parts.add(new BladePart(0.0D, 6.71D, 0.0D, smooth));
        parts.add(new BladePart(0.0D, 7.32D, 0.0D, smooth));
        parts.add(new BladePart(0.0D, 7.93D, 0.0D, smooth));

        if (parts.size() != 84) throw new IllegalStateException("Expected 84 Glaivorus parts, got " + parts.size());
        return List.copyOf(parts);
    }

    private static void addVertical(List<BladePart> parts, BlockState state, double x, double firstY, int count) {
        for (int index = 0; index < count; index++) {
            parts.add(new BladePart(x, firstY + index * 0.61D, 0.0D, state));
        }
    }

    private static void addPair(List<BladePart> parts, BlockState state, double x, double y) {
        parts.add(new BladePart(x, y, 0.0D, state));
        parts.add(new BladePart(-x, y, 0.0D, state));
    }

    private record BladePart(double x, double y, double z, BlockState state) {
    }
}
