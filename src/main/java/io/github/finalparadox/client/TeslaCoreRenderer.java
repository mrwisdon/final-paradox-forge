package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.TeslaCoreEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;

public final class TeslaCoreRenderer extends EntityRenderer<TeslaCoreEntity> {
    private final BlockRenderDispatcher blocks;
    public TeslaCoreRenderer(EntityRendererProvider.Context context) { super(context); blocks = context.getBlockRenderDispatcher(); shadowRadius = 0; }
    @Override public ResourceLocation getTextureLocation(TeslaCoreEntity entity) { return InventoryMenu.BLOCK_ATLAS; }
    @Override public void render(TeslaCoreEntity entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.translate(0, 0.5D, 0);
        pose.mulPose(Axis.YP.rotationDegrees(entity.getYRot() + partialTicks * 5.0F));
        pose.scale(0.45F, 0.45F, 0.45F);
        pose.translate(-0.5D, -0.5D, -0.5D);
        blocks.renderSingleBlock(Blocks.BLACK_CONCRETE.defaultBlockState(), pose, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(entity, yaw, partialTicks, pose, buffers, light);
    }
}
