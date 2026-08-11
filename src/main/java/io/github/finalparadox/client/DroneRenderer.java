package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.entity.DroneEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Renders the original quadcopter model. The entity origin is the camera
 * position; the physical drone is drawn below it and the model's camera
 * gimbal follows the pilot look pitch.
 */
public final class DroneRenderer extends EntityRenderer<DroneEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    FinalParadox.MOD_ID, "textures/entity/recon_drone.png");

    private final DroneModel model;

    public DroneRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new DroneModel(context.bakeLayer(DroneModel.DRONE_LAYER));
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(DroneEntity entity) {
        return TEXTURE;
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
        // The model's local nose/barrels point toward -z while entity yaw 0
        // faces +z, so the render rotation is 180 - yaw (see DroneModelMath);
        // modelYawDegrees is the exact function covered by unit tests.
        pose.mulPose(Axis.YP.rotationDegrees(DroneModelMath.modelYawDegrees(yaw)));

        model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTick, 0.0F, pitch);
        VertexConsumer consumer = buffer.getBuffer(model.renderType(TEXTURE));
        model.renderToBuffer(pose, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        int heat = entity.getGatlingHeat();
        if (heat > 0) {
            VertexConsumer heatConsumer = buffer.getBuffer(
                    RenderType.entityTranslucentEmissive(TEXTURE));
            model.renderHeatToBuffer(pose, heatConsumer, LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY, heat);
        }
        pose.popPose();
        super.render(entity, entityYaw, partialTick, pose, buffer, packedLight);
    }
}
