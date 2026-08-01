package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.ArcaneTechniqueEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

public final class ArcaneTechniqueRenderer extends EntityRenderer<ArcaneTechniqueEntity> {
    private static final float[] UPPER_POSE = {160,160,170,180,185,187,160,110,20,-30,-90,-130,-150,-155,-158,-160,-162,-164};
    private static final float[] COMBO_PITCH = {200,210,220,20,70,80,90,100,105,20,290,210,200,190,185,180,50,40,30,310,220,200,180,160,150,150,150,150,120,150,200,300,40,70,80,85,90,95,98,100};
    private final ItemRenderer items;

    public ArcaneTechniqueRenderer(EntityRendererProvider.Context context) {
        super(context);
        items = context.getItemRenderer();
        shadowRadius = 0;
    }

    @Override public ResourceLocation getTextureLocation(ArcaneTechniqueEntity entity) { return InventoryMenu.BLOCK_ATLAS; }

    @Override public void render(ArcaneTechniqueEntity entity, float yaw, float partial, PoseStack pose,
                                 MultiBufferSource buffers, int light) {
        if (entity.mode() == ArcaneTechniqueEntity.ARCANE_SLASH
                || entity.mode() == ArcaneTechniqueEntity.ARCANE_SLASH_REVERSE) {
            renderSweep(entity, partial, pose, buffers);
        } else if (entity.mode() == ArcaneTechniqueEntity.ARS_AERUM) {
            renderArsAerum(entity, partial, pose, buffers);
        }
        super.render(entity, yaw, partial, pose, buffers, light);
    }

    private void renderSweep(ArcaneTechniqueEntity entity, float partial, PoseStack pose, MultiBufferSource buffers) {
        boolean reverse = entity.mode() == ArcaneTechniqueEntity.ARCANE_SLASH_REVERSE;
        float age = entity.tickCount + partial;
        float rotation = ArcaneTechniqueEntity.sweepRotation(age) * (reverse ? -1 : 1);
        double angle = Math.toRadians(entity.getYRot() + (reverse ? 300 : 130) + rotation);
        double x = 1.2 * Math.cos(angle) + 1.5 * Math.sin(angle);
        double z = -1.2 * Math.sin(angle) + 1.5 * Math.cos(angle);

        pose.pushPose();
        pose.translate(x, ArcaneTechniqueEntity.sweepHeight(age, reverse), z);
        pose.mulPose(Axis.YP.rotationDegrees(-entity.getYRot() - (reverse ? 300 : 130)));
        pose.mulPose(Axis.XP.rotationDegrees(-10));
        pose.mulPose(Axis.YP.rotationDegrees(ArcaneTechniqueEntity.sweepArmYaw(age, reverse)));
        pose.mulPose(Axis.ZP.rotationDegrees(90));
        renderSword(entity, pose, buffers, 1.35F);
        pose.popPose();
    }

    private void renderArsAerum(ArcaneTechniqueEntity entity, float partial, PoseStack pose, MultiBufferSource buffers) {
        float age = entity.tickCount + partial;
        if (age < 15) renderUpper(entity, age, pose, buffers);
        else renderAirCombo(entity, age, pose, buffers);
    }

    private void renderUpper(ArcaneTechniqueEntity entity, float age, PoseStack pose, MultiBufferSource buffers) {
        int index = Math.max(0, Math.min(UPPER_POSE.length - 1, (int)age));
        double x = age < 7 ? 3 : -1.5;
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-entity.getYRot() - 245));
        pose.translate(x, -.45, .2);
        pose.mulPose(Axis.YP.rotationDegrees(-90));
        pose.mulPose(Axis.XP.rotationDegrees(UPPER_POSE[index]));
        renderSword(entity, pose, buffers, 1.45F);
        pose.popPose();
    }

    private void renderAirCombo(ArcaneTechniqueEntity entity, float age, PoseStack pose, MultiBufferSource buffers) {
        int score = Math.max(0, Math.min(39, (int)age - 19));
        float pitch = COMBO_PITCH[score];
        float roll = score < 16 ? 338 : score < 25 ? 292 : score == 25 ? 302 : score == 26 ? 310 : score == 27 ? 320 : 30;
        double x = 0, y = -.2, z = .1;
        if (score >= 1) { y = -2.3; z = .8; }
        if (score >= 9) { y = -.2; z = 0; }
        if (score >= 14) { y = -1.7; z = 1.5; }
        if (score >= 18) { y = -.8; z = -1.2; }
        if (score >= 23) z += Math.min(score - 22, 9) * .3;
        if (score >= 29) { y -= 1.5; z -= 1.4; }
        float extraYaw = Math.max(0, Math.min(score - 21, 5)) * 60 + Math.max(0, Math.min(score - 26, 3)) * 20;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-entity.getYRot() - 285 - extraYaw));
        pose.translate(x, y, z);
        pose.mulPose(Axis.YP.rotationDegrees(-90));
        pose.mulPose(Axis.XP.rotationDegrees(pitch));
        pose.mulPose(Axis.ZP.rotationDegrees(roll));
        renderSword(entity, pose, buffers, 1.4F);
        pose.popPose();
    }

    private void renderSword(ArcaneTechniqueEntity entity, PoseStack pose, MultiBufferSource buffers, float scale) {
        pose.scale(scale, scale, scale);
        items.renderStatic(entity.displayStack(), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                0xF000F0, OverlayTexture.NO_OVERLAY, pose, buffers, entity.level(), entity.getId());
    }
}
