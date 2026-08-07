package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.DroneEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Lightweight quadcopter built from item models. The entity origin is the
 * camera position; the physical drone is drawn below it.
 */
public final class DroneRenderer extends EntityRenderer<DroneEntity> {
    private static final ItemStack BODY = new ItemStack(Items.IRON_BLOCK);
    private static final ItemStack ARM = new ItemStack(Items.IRON_BARS);
    private static final ItemStack ROTOR = new ItemStack(Items.IRON_TRAPDOOR);
    private static final ItemStack CAMERA = new ItemStack(Items.SPYGLASS);
    private static final ItemStack BOMB = new ItemStack(Items.TNT);

    private final ItemRenderer itemRenderer;

    public DroneRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(DroneEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(
            DroneEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffer,
            int packedLight) {
        pose.pushPose();
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));

        float rotorAngle = (entity.tickCount + partialTick) * 28.0F;
        renderItem(BODY, pose, buffer, packedLight, 0.0D, -0.62D, 0.0D,
                0.55F, 0.0F, 0.0F, entity);
        renderItem(ARM, pose, buffer, packedLight, -0.35D, -0.56D, 0.0D,
                0.5F, 0.0F, 90.0F, entity);
        renderItem(ARM, pose, buffer, packedLight, 0.35D, -0.56D, 0.0D,
                0.5F, 0.0F, 90.0F, entity);
        renderItem(ARM, pose, buffer, packedLight, 0.0D, -0.56D, -0.35D,
                0.5F, 0.0F, 0.0F, entity);
        renderItem(ARM, pose, buffer, packedLight, 0.0D, -0.56D, 0.35D,
                0.5F, 0.0F, 0.0F, entity);
        renderItem(ROTOR, pose, buffer, packedLight, -0.35D, -0.44D, 0.0D,
                0.45F, rotorAngle, 0.0F, entity);
        renderItem(ROTOR, pose, buffer, packedLight, 0.35D, -0.44D, 0.0D,
                0.45F, rotorAngle, 0.0F, entity);
        renderItem(ROTOR, pose, buffer, packedLight, 0.0D, -0.44D, -0.35D,
                0.45F, rotorAngle, 0.0F, entity);
        renderItem(ROTOR, pose, buffer, packedLight, 0.0D, -0.44D, 0.35D,
                0.45F, rotorAngle, 0.0F, entity);
        renderItem(CAMERA, pose, buffer, packedLight, 0.0D, -0.38D, 0.15D,
                0.5F, 0.0F, pitch, entity);

        int bombs = entity.getBombs();
        for (int i = 0; i < bombs; i++) {
            renderItem(BOMB, pose, buffer, packedLight, 0.0D, -0.88D - i * 0.07D, 0.0D,
                    0.14F, 0.0F, 0.0F, entity);
        }
        pose.popPose();
        super.render(entity, entityYaw, partialTick, pose, buffer, packedLight);
    }

    private void renderItem(
            ItemStack stack,
            PoseStack pose,
            MultiBufferSource buffer,
            int packedLight,
            double x,
            double y,
            double z,
            float scale,
            float yRot,
            float xRot,
            DroneEntity entity) {
        pose.pushPose();
        pose.translate(x, y, z);
        pose.mulPose(Axis.YP.rotationDegrees(yRot));
        pose.mulPose(Axis.XP.rotationDegrees(xRot));
        pose.scale(scale, scale, scale);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                packedLight, OverlayTexture.NO_OVERLAY, pose, buffer,
                entity.level(), entity.getId());
        pose.popPose();
    }
}
