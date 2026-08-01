package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Renders the custom Sacred Shield with the exact vanilla shield geometry and material. */
public final class SacredShieldRenderer extends BlockEntityWithoutLevelRenderer {
    private final ShieldModel model;

    public SacredShieldRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
        model = new ShieldModel(models.bakeLayer(ModelLayers.SHIELD));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
                             MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.scale(1.0F, -1.0F, -1.0F);
        Material material = ModelBakery.NO_PATTERN_SHIELD;
        VertexConsumer vertices = material.sprite().wrap(ItemRenderer.getFoilBufferDirect(
                buffers, model.renderType(material.atlasLocation()), true, stack.hasFoil()));
        model.handle().render(pose, vertices, light, overlay, 1, 1, 1, 1);
        model.plate().render(pose, vertices, light, overlay, 1, 1, 1, 1);
        pose.popPose();
    }
}
