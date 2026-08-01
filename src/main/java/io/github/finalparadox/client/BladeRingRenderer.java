package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.BladeRingEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BladeRingRenderer extends EntityRenderer<BladeRingEntity> {
    private final ItemRenderer items;
    public BladeRingRenderer(EntityRendererProvider.Context context) { super(context); items = context.getItemRenderer(); shadowRadius = 0; }
    @Override public ResourceLocation getTextureLocation(BladeRingEntity entity) { return InventoryMenu.BLOCK_ATLAS; }
    @Override public void render(BladeRingEntity entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        float rotation = (entity.tickCount + partialTicks) * 12.0F;
        for (int i = 0; i < 6; i++) {
            float angle = rotation + i * 60.0F;
            double radians = Math.toRadians(angle);
            pose.pushPose();
            pose.translate(Math.cos(radians) * 2.15D, 0.15D, Math.sin(radians) * 2.15D);
            // The sword texture runs diagonally from its handle at bottom-left to
            // its point at top-right. Flatten it into the horizontal plane, align
            // that diagonal with local +X, then yaw local +X radially outwards.
            // Only the ring angle changes: the blade never rolls around its own axis.
            pose.mulPose(Axis.YP.rotationDegrees(-angle));
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(-45.0F));
            pose.scale(1.4F, 1.4F, 1.4F);
            items.renderStatic(new ItemStack(Items.IRON_SWORD), ItemDisplayContext.FIXED, light,
                    OverlayTexture.NO_OVERLAY, pose, buffers, entity.level(), entity.getId() + i);
            pose.popPose();
        }
        super.render(entity, yaw, partialTicks, pose, buffers, light);
    }
}
