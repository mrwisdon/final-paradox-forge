package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.DroneBombEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;

/** Small spinning TNT block used for the drone bomb payload. */
public final class DroneBombRenderer extends EntityRenderer<DroneBombEntity> {
    private final BlockRenderDispatcher blocks;

    public DroneBombRenderer(EntityRendererProvider.Context context) {
        super(context);
        blocks = context.getBlockRenderDispatcher();
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(DroneBombEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(
            DroneBombEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffer,
            int packedLight) {
        pose.pushPose();
        pose.translate(0.0D, 0.25D, 0.0D);
        pose.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * 12.0F));
        pose.scale(0.5F, 0.5F, 0.5F);
        pose.translate(-0.5D, -0.5D, -0.5D);
        blocks.renderSingleBlock(Blocks.TNT.defaultBlockState(),
                pose, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(entity, entityYaw, partialTick, pose, buffer, packedLight);
    }
}
