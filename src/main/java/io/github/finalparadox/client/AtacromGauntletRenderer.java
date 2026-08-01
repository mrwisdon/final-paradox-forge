package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.AtacromGauntletEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

public final class AtacromGauntletRenderer extends EntityRenderer<AtacromGauntletEntity> {
    private final ItemRenderer itemRenderer;

    public AtacromGauntletRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
        shadowRadius = 0.0F;
    }

    @Override
    public void render(AtacromGauntletEntity entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffers, int packedLight) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - entity.getYRot()));
        pose.mulPose(Axis.XP.rotationDegrees(140.0F));
        pose.mulPose(Axis.YP.rotationDegrees(270.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(0.5F));
        pose.scale(1.0F, 1.0F, 1.0F);
        itemRenderer.renderStatic(entity.displayStack(), ItemDisplayContext.FIXED,
                packedLight, OverlayTexture.NO_OVERLAY, pose, buffers, entity.level(), entity.getId());
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AtacromGauntletEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
