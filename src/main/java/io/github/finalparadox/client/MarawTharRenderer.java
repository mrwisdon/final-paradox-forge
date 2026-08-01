package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.MarawTharBossEntity;
import io.github.finalparadox.entity.MarawTharH3Frames;
import io.github.finalparadox.entity.MarawTharH4Frames;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * One renderer replaces the source hitbox/body/sword armor-stand stack.
 */
public final class MarawTharRenderer
        extends HumanoidMobRenderer<MarawTharBossEntity, MarawTharModel> {
    private static final ResourceLocation BODY_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/armorstand/wood.png");
    private static final float SOURCE_BODY_YAW_OFFSET = 50.0F;
    private static final float SWORD_HEAD_HEIGHT_CORRECTION = 1.5F;
    private final TharKrooRenderer tharKrooTransitionRenderer;
    private final MarawTharVictoryVisualRenderer victoryVisualRenderer;

    public MarawTharRenderer(EntityRendererProvider.Context context) {
        super(context, new MarawTharModel(context.bakeLayer(ModelLayers.ARMOR_STAND)), 0.5F);
        tharKrooTransitionRenderer = new TharKrooRenderer(context);
        victoryVisualRenderer = new MarawTharVictoryVisualRenderer(context);
        addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<MarawTharBossEntity>(
                        context.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR)),
                new HumanoidModel<MarawTharBossEntity>(
                        context.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR)),
                context.getModelManager()));
        addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
    }

    @Override
    public void render(MarawTharBossEntity entity, float entityYaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffers, int packedLight) {
        if (entity.isVictoryActive()) {
            if (entity.getVictoryTick() >= MarawTharVictoryVisualRenderer.STATUE_SPAWN_TICK) {
                victoryVisualRenderer.renderStatue(
                        entity, partialTick, pose, buffers, packedLight);
            } else {
                victoryVisualRenderer.renderHuman(
                        entity, partialTick, pose, buffers, packedLight);
                renderNightfall(
                        entity, entityYaw, partialTick, pose, buffers, packedLight);
            }
            return;
        }
        if (entity.usesTharKrooTransitionModel()) {
            tharKrooTransitionRenderer.renderMarawTharTransition(
                    entity, entityYaw, partialTick, pose, buffers, packedLight);
            return;
        }
        boolean scriptedAttack = entity.getAnimation() == MarawTharBossEntity.ANIMATION_TRIPLE_SLASH
                || entity.getAnimation() == MarawTharBossEntity.ANIMATION_BLUE_RUSH
                || entity.getAnimation() == MarawTharBossEntity.ANIMATION_H1_COMBO2
                || entity.getAnimation() == MarawTharBossEntity.ANIMATION_H1_COMBO3
                || entity.getAnimation() == MarawTharBossEntity.ANIMATION_H1_COMBO4
                || entity.getAnimation() == MarawTharBossEntity.ANIMATION_LASER_COMBO
                || entity.getAnimation() == MarawTharBossEntity.ANIMATION_DASH
                || entity.getAnimation() == MarawTharBossEntity.ANIMATION_H9_LASER
                || entity.getAnimation() == MarawTharBossEntity.ANIMATION_H3_PLANT
                || entity.getAnimation() == MarawTharBossEntity.ANIMATION_H3_SLAM
                || entity.getAnimation() == MarawTharBossEntity.ANIMATION_H4_JUDGMENT
                || entity.getAnimation() == MarawTharBossEntity.ANIMATION_H6_TRANSITION;
        float visualYaw = entityYaw + (scriptedAttack ? 0.0F : SOURCE_BODY_YAW_OFFSET);
        pose.pushPose();
        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_H9_LASER) {
            float scale = h9BodyScale(entity.getAnimationTick() + partialTick);
            pose.scale(scale, scale, scale);
        } else if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_H3_PLANT) {
            float score = entity.getAnimationTick() + partialTick;
            pose.translate(0.0D, 2.0D + Mth.clamp(score - 3.0F, 0.0F, 20.0F) * 0.02D, 0.0D);
        } else if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_H3_SLAM) {
            pose.translate(
                    0.0D, MarawTharH3Frames.bodyUp(entity.getAnimationTick() + partialTick), 0.0D);
        } else if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_H6_TRANSITION) {
            float score = entity.getAnimationTick() + partialTick - 2.0F;
            if (score <= 54) {
                pose.translate(0.0D, h6VisualHeight(score), 0.0D);
            }
        }
        super.render(entity, visualYaw, partialTick, pose, buffers, packedLight);
        pose.popPose();
        if (!entity.isInvisible()) {
            renderNightfall(entity, visualYaw, partialTick, pose, buffers, packedLight);
        }
    }

    private static float h9BodyScale(float score) {
        float beamTick = score - 73.0F;
        if (beamTick < 0.0F) {
            return 1.0F;
        }
        if (beamTick < 5.0F) {
            return 0.5F;
        }
        if (beamTick < 74.0F) {
            return (Mth.floor(beamTick - 5.0F) & 1) == 0 ? 1.0F : 0.8F;
        }
        if (beamTick < 77.0F) {
            return 0.5F;
        }
        return 1.0F;
    }

    private static double h6VisualHeight(float score) {
        double height = 0.5D;
        if (score <= 27) return height + Math.max(0, score + 2) * 0.02D;
        height += 29.0D * 0.02D;
        if (score <= 54) return height + (score - 27) * 0.04D;
        return 0.0D;
    }

    private static void renderNightfall(MarawTharBossEntity entity, float visualYaw, float partialTick,
                                        PoseStack pose, MultiBufferSource buffers, int packedLight) {
        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_VICTORY
                && entity.getVictoryTick() >= 9 * 20) {
            return;
        }
        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_H3_PLANT) {
            return;
        }
        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_H3_SLAM) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - visualYaw));
            pose.translate(
                    0.0D, MarawTharH3Frames.swordUp(entity.getAnimationTick() + partialTick), 0.4D);
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            NightfallItemRenderer.renderVisual(
                    pose, buffers, packedLight, entity.level(), entity.getId());
            pose.popPose();
            return;
        }
        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_H4_JUDGMENT) {
            float score = entity.getAnimationTick() + partialTick;
            if (!MarawTharH4Frames.swordVisible(entity.getAnimationTick())) {
                return;
            }
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - visualYaw));
            pose.translate(
                    MarawTharH4Frames.swordLeft(score),
                    MarawTharH4Frames.swordUp(score) + SWORD_HEAD_HEIGHT_CORRECTION,
                    MarawTharH4Frames.swordForward());
            pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
            pose.mulPose(Axis.XP.rotationDegrees(MarawTharH4Frames.swordPitch(score)));
            NightfallItemRenderer.renderVisual(
                    pose, buffers, packedLight, entity.level(), entity.getId());
            pose.popPose();
            return;
        }
        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_TRIPLE_SLASH) {
            NightfallComboFrames.Frame frame = NightfallComboFrames.sample(
                    entity.getAnimationTick(), partialTick);
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(-visualYaw));
            pose.translate(frame.left(), frame.up() + SWORD_HEAD_HEIGHT_CORRECTION, frame.forward());
            pose.mulPose(Axis.YP.rotationDegrees(-frame.yaw()));
            pose.mulPose(Axis.XP.rotationDegrees(frame.pitch()));
            NightfallItemRenderer.renderVisual(
                    pose, buffers, packedLight, entity.level(), entity.getId());
            pose.popPose();
            return;
        }
        if (MarawTharH1SwordFrames.supports(entity.getAnimation())) {
            MarawTharH1SwordFrames.Frame frame = MarawTharH1SwordFrames.sample(
                    entity.getAnimation(), entity.getAnimationTick(), partialTick);
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(-visualYaw));
            pose.translate(frame.left(), frame.up() + SWORD_HEAD_HEIGHT_CORRECTION, frame.forward());
            pose.mulPose(Axis.YP.rotationDegrees(-frame.yaw()));
            pose.mulPose(Axis.XP.rotationDegrees(frame.pitch()));
            NightfallItemRenderer.renderVisual(
                    pose, buffers, packedLight, entity.level(), entity.getId());
            pose.popPose();
            return;
        }
        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_DASH) {
            float score = entity.getAnimationTick() + partialTick;
            float windup = Mth.clamp((score - 18.0F) / 15.0F, 0.0F, 1.0F);
            float charge = Mth.clamp((score - 33.0F) / 3.0F, 0.0F, 1.0F);
            double left = Mth.lerp(windup, -0.50D, -0.16D);
            double up = Mth.lerp(windup, 0.90D, 1.34D) - charge * 0.20D;
            double forward = Mth.lerp(windup, -0.38D, 0.62D) + charge * 0.18D;
            float swordYaw = Mth.lerp(windup, 250.0F, 184.0F);
            float swordPitch = Mth.lerp(windup, -45.0F, -12.0F) + charge * 76.0F;
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - visualYaw));
            pose.translate(left, up, forward);
            pose.mulPose(Axis.YP.rotationDegrees(swordYaw));
            pose.mulPose(Axis.XP.rotationDegrees(swordPitch));
            NightfallItemRenderer.renderVisual(
                    pose, buffers, packedLight, entity.level(), entity.getId());
            pose.popPose();
            return;
        }

        float walk = 0.0F;
        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_WALK) {
            float score = entity.getAnimationTick() + partialTick;
            walk = Mth.sin(score * Mth.TWO_PI / 28.0F);
        }
        float recoil = 0.0F;
        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_HURT) {
            recoil = 1.0F - Mth.clamp((entity.getAnimationTick() + partialTick) / 10.0F, 0.0F, 1.0F);
        }

        double left = -0.75D + walk * 0.035D;
        double up = -0.50D + SWORD_HEAD_HEIGHT_CORRECTION
                + Math.abs(walk) * 0.025D + recoil * 0.08D;
        double forward = 0.10D + recoil * 0.10D;
        float swordYaw = -108.0F + recoil * 18.0F;
        float swordPitch = 20.0F + walk * 3.0F + recoil * 24.0F;

        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_LASER_COMBO) {
            float pulse = Mth.sin((entity.getAnimationTick() + partialTick) * 0.38F);
            left = -0.18D;
            up = 1.18D + pulse * 0.035D;
            forward = 0.52D;
            swordYaw = -92.0F;
            swordPitch = 88.0F + pulse * 2.0F;
        }

        pose.pushPose();
        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_H9_LASER) {
            float scale = h9BodyScale(entity.getAnimationTick() + partialTick);
            pose.scale(scale, scale, scale);
        } else if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_H6_TRANSITION) {
            float score = entity.getAnimationTick() + partialTick - 2.0F;
            if (score <= 54) {
                pose.translate(0.0D, h6VisualHeight(score), 0.0D);
            }
        }
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - visualYaw));
        pose.translate(left, up, forward);
        pose.mulPose(Axis.YP.rotationDegrees(swordYaw));
        pose.mulPose(Axis.XP.rotationDegrees(swordPitch));
        NightfallItemRenderer.renderVisual(
                pose, buffers, packedLight, entity.level(), entity.getId());
        pose.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(MarawTharBossEntity entity) {
        return BODY_TEXTURE;
    }
}
