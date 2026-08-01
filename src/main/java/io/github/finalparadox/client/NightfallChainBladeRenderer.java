package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.NightfallChainBladeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public final class NightfallChainBladeRenderer extends EntityRenderer<NightfallChainBladeEntity> {
    public NightfallChainBladeRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(NightfallChainBladeEntity entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffers, int packedLight) {
        pose.pushPose();
        // The coherent blade points along local +Y; invert it so the blade is
        // buried in the floor and the conduit/handle remain above ground.
        pose.translate(0.0D, 2.0D, 0.0D);
        pose.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        pose.mulPose(Axis.XP.rotationDegrees(-10.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
        NightfallItemRenderer.renderVisual(
                pose, buffers, packedLight, entity.level(), entity.getId());
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(NightfallChainBladeEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
