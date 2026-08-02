package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.finalparadox.entity.B8SniperBulletEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;

/** Renders the sniper conduit bullet as a small glowing conduit block. */
public final class B8SniperBulletRenderer extends EntityRenderer<B8SniperBulletEntity> {
    private final BlockRenderDispatcher blocks;

    public B8SniperBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        blocks = context.getBlockRenderDispatcher();
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(B8SniperBulletEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(B8SniperBulletEntity entity, float entityYaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffer, int light) {
        pose.pushPose();
        pose.translate(-0.3D, 1.2D, -0.3D);
        pose.scale(0.6F, 0.6F, 0.6F);
        blocks.renderSingleBlock(Blocks.CONDUIT.defaultBlockState(),
                pose, buffer, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(entity, entityYaw, partialTick, pose, buffer, light);
    }
}
