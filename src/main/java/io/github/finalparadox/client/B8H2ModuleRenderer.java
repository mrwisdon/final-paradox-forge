package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.B8H2ModuleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;

/**
 * Renders the H2 gold-block module. The entity origin matches the source
 * armor-stand origin; the block is drawn centered at the stand head height
 * (+1.5) and rotated by the synced yaw.
 */
public final class B8H2ModuleRenderer extends EntityRenderer<B8H2ModuleEntity> {
    private final BlockRenderDispatcher blocks;

    public B8H2ModuleRenderer(EntityRendererProvider.Context context) {
        super(context);
        blocks = context.getBlockRenderDispatcher();
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(B8H2ModuleEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(B8H2ModuleEntity entity, float entityYaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffer, int light) {
        pose.pushPose();
        pose.translate(-0.5D, 1.0D, -0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(entity.moduleYaw()));
        blocks.renderSingleBlock(Blocks.GOLD_BLOCK.defaultBlockState(),
                pose, buffer, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(entity, entityYaw, partialTick, pose, buffer, light);
    }
}
