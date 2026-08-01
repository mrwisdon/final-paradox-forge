package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.finalparadox.entity.MarawTharBossEntity;
import io.github.finalparadox.entity.MarawTharH3Frames;
import io.github.finalparadox.entity.MarawTharH4Frames;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.IntToDoubleFunction;

/**
 * Source armor-stand geometry and poses translated to one coherent boss model.
 */
public final class MarawTharModel extends HumanoidModel<MarawTharBossEntity> {
    private static final float DEG = Mth.DEG_TO_RAD;
    private final ModelPart rightBodyStick;
    private final ModelPart leftBodyStick;
    private final ModelPart shoulderStick;

    public MarawTharModel(ModelPart root) {
        super(root);
        rightBodyStick = root.getChild("right_body_stick");
        leftBodyStick = root.getChild("left_body_stick");
        shoulderStick = root.getChild("shoulder_stick");
        root.getChild("base_plate").visible = false;
        hat.visible = false;
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return List.of(
                body, rightArm, leftArm, rightLeg, leftLeg,
                rightBodyStick, leftBodyStick, shoulderStick);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        syncArmorStandBodySticks();
        super.renderToBuffer(
                pose, consumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private void syncArmorStandBodySticks() {
        for (ModelPart stick : List.of(rightBodyStick, leftBodyStick, shoulderStick)) {
            stick.xRot = body.xRot;
            stick.yRot = body.yRot;
            stick.zRot = body.zRot;
        }
    }

    @Override
    public void setupAnim(MarawTharBossEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0.0F, 1.0F);
        float animationScore = entity.getAnimationTick() + partialTick;

        head.yRot = netHeadYaw * DEG;
        head.xRot = headPitch * DEG;
        hat.copyFrom(head);

        float walk = 0.0F;
        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_WALK) {
            float score = animationScore % 28.0F;
            walk = Mth.sin(score * Mth.TWO_PI / 28.0F) * 20.0F * DEG;
        }

        body.xRot = 6.0F * DEG;
        body.yRot = 0.0F;
        leftLeg.xRot = 8.0F * DEG + walk;
        rightLeg.xRot = -10.0F * DEG - walk;
        leftLeg.yRot = 0.0F;
        rightLeg.yRot = 0.0F;
        leftArm.xRot = -walk * 0.55F;
        leftArm.yRot = 0.0F;
        leftArm.zRot = -12.0F * DEG;
        rightArm.xRot = 22.0F * DEG + walk * 0.16F;
        rightArm.yRot = -44.0F * DEG;
        rightArm.zRot = 0.0F;

        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_H6_TRANSITION) {
            float score = animationScore - 2.0F;
            body.xRot = 0.0F;
            body.yRot = 0.0F;
            body.zRot = 0.0F;
            leftLeg.xRot = 0.0F;
            leftLeg.yRot = 0.0F;
            leftLeg.zRot = -6.0F * DEG;
            rightLeg.xRot = 0.0F;
            rightLeg.yRot = 0.0F;
            rightLeg.zRot = 6.0F * DEG;
            leftArm.xRot = interpolate(score, MarawTharModel::h6LeftArmX) * DEG;
            leftArm.yRot = 0.0F;
            rightArm.xRot = 0.0F;
            rightArm.yRot = 0.0F;
            head.xRot = interpolate(score, MarawTharModel::h6HeadX) * DEG;
            leftArm.zRot = interpolate(score, MarawTharModel::h6LeftArmZ) * DEG;
            rightArm.zRot = interpolate(score, MarawTharModel::h6RightArmZ) * DEG;
            head.yRot = 0.0F;
            head.zRot = 0.0F;
            hat.copyFrom(head);
            return;
        }

        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_H4_JUDGMENT) {
            float score = animationScore;
            head.xRot = MarawTharH4Frames.headX(score) * DEG;
            head.yRot = MarawTharH4Frames.headY(score) * DEG;
            head.zRot = 0.0F;
            hat.copyFrom(head);
            body.xRot = 0.0F;
            body.yRot = 6.0F * DEG;
            body.zRot = 0.0F;
            leftLeg.xRot = 4.0F * DEG;
            leftLeg.yRot = 0.0F;
            leftLeg.zRot = 0.0F;
            rightLeg.xRot = 10.0F * DEG;
            rightLeg.yRot = 0.0F;
            rightLeg.zRot = 0.0F;
            leftArm.xRot = 0.0F;
            leftArm.yRot = 0.0F;
            leftArm.zRot = MarawTharH4Frames.leftArmZ(score) * DEG;
            rightArm.xRot = 0.0F;
            rightArm.yRot = 0.0F;
            rightArm.zRot = MarawTharH4Frames.rightArmZ(score) * DEG;
            return;
        }

        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_H3_PLANT) {
            head.xRot = 0.0F;
            head.yRot = 70.0F * DEG;
            head.zRot = 0.0F;
            hat.copyFrom(head);
            body.xRot = -5.0F * DEG;
            body.yRot = 0.0F;
            body.zRot = 0.0F;
            leftLeg.xRot = -40.0F * DEG;
            leftLeg.yRot = 0.0F;
            leftLeg.zRot = 0.0F;
            rightLeg.xRot = -10.0F * DEG;
            rightLeg.yRot = 0.0F;
            rightLeg.zRot = 0.0F;
            leftArm.xRot = 0.0F;
            leftArm.yRot = 0.0F;
            leftArm.zRot = -32.0F * DEG;
            rightArm.xRot = -89.0F * DEG;
            rightArm.yRot = 76.0F * DEG;
            rightArm.zRot = 0.0F;
            return;
        }

        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_H3_SLAM) {
            float score = animationScore;
            head.xRot = 0.0F;
            head.yRot = 0.0F;
            head.zRot = 0.0F;
            hat.copyFrom(head);
            body.xRot = 0.0F;
            body.yRot = 0.0F;
            body.zRot = 0.0F;
            leftLeg.xRot = 0.0F;
            leftLeg.yRot = 0.0F;
            leftLeg.zRot = MarawTharH3Frames.leftLegZ(score) * DEG;
            rightLeg.xRot = 0.0F;
            rightLeg.yRot = 0.0F;
            rightLeg.zRot = MarawTharH3Frames.rightLegZ(score) * DEG;
            leftArm.xRot = MarawTharH3Frames.armX(score) * DEG;
            leftArm.yRot = 40.0F * DEG;
            leftArm.zRot = 0.0F;
            rightArm.xRot = MarawTharH3Frames.armX(score) * DEG;
            rightArm.yRot = -40.0F * DEG;
            rightArm.zRot = 0.0F;
            return;
        }

        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_DASH) {
            float score = animationScore;
            float windup = Mth.clamp((score - 18.0F) / 15.0F, 0.0F, 1.0F);
            float charge = Mth.clamp((score - 33.0F) / 3.0F, 0.0F, 1.0F);
            body.xRot = Mth.lerp(windup, 0.0F, -18.0F * DEG)
                    + charge * 46.0F * DEG;
            body.yRot = 0.0F;
            body.zRot = 0.0F;
            head.xRot = charge * -12.0F * DEG;
            head.zRot = 0.0F;
            hat.copyFrom(head);
            leftLeg.xRot = Mth.lerp(charge, 0.0F, -48.0F * DEG);
            leftLeg.yRot = 0.0F;
            leftLeg.zRot = Mth.lerp(windup, 14.0F * DEG, 5.0F * DEG);
            rightLeg.xRot = Mth.lerp(charge, 0.0F, 38.0F * DEG);
            rightLeg.yRot = 0.0F;
            rightLeg.zRot = Mth.lerp(windup, -14.0F * DEG, -5.0F * DEG);
            leftArm.xRot = Mth.lerp(windup, -45.0F * DEG, -82.0F * DEG);
            leftArm.yRot = 0.0F;
            leftArm.zRot = Mth.lerp(charge, 0.0F, -28.0F * DEG);
            rightArm.xRot = Mth.lerp(windup, 0.0F, -96.0F * DEG);
            rightArm.yRot = Mth.lerp(windup, 46.0F * DEG, -22.0F * DEG);
            rightArm.zRot = Mth.lerp(windup, 18.0F * DEG, 42.0F * DEG);
            if (score >= 33.0F) {
                float stride = Mth.sin((score - 33.0F) * Mth.PI);
                leftLeg.xRot += stride * 8.0F * DEG;
                rightLeg.xRot -= stride * 8.0F * DEG;
            }
            return;
        }

        if (MarawTharH1BodyFrames.supports(entity.getAnimation())) {
            applyH1BodyPose(entity.getAnimation(), animationScore);
            return;
        }

        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_LASER_COMBO) {
            float pulse = Mth.sin(animationScore * 0.38F);
            body.xRot = -5.0F * DEG;
            body.yRot = 12.0F * DEG;
            leftLeg.xRot = 34.0F * DEG;
            rightLeg.xRot = 12.0F * DEG;
            leftArm.xRot = -34.0F * DEG;
            leftArm.yRot = -34.0F * DEG;
            rightArm.xRot = (-20.0F + pulse * 8.0F) * DEG;
            rightArm.yRot = 0.0F;
            rightArm.zRot = 94.0F * DEG;
            return;
        }

        if (entity.getAnimation() == MarawTharBossEntity.ANIMATION_HURT) {
            float recoil = 1.0F - Mth.clamp(animationScore / 10.0F, 0.0F, 1.0F);
            body.yRot = 20.0F * DEG * recoil;
            head.xRot -= 40.0F * DEG * recoil;
            leftLeg.xRot += 32.0F * DEG * recoil;
            rightLeg.xRot -= 30.0F * DEG * recoil;
            leftArm.xRot -= 28.0F * DEG * recoil;
            rightArm.xRot += 30.0F * DEG * recoil;
            rightArm.yRot += 22.0F * DEG * recoil;
        }
    }

    private void applyH1BodyPose(int animation, float score) {
        MarawTharH1BodyFrames.Frame frame = MarawTharH1BodyFrames.sample(animation, score);
        applyPart(head, frame.head());
        applyPart(body, frame.body());
        applyPart(leftArm, frame.leftArm());
        applyPart(rightArm, frame.rightArm());
        applyPart(leftLeg, frame.leftLeg());
        applyPart(rightLeg, frame.rightLeg());
        hat.copyFrom(head);
    }

    private static void applyPart(ModelPart modelPart, MarawTharH1BodyFrames.Part pose) {
        modelPart.xRot = pose.x() * DEG;
        modelPart.yRot = pose.y() * DEG;
        modelPart.zRot = pose.z() * DEG;
    }

    private static float interpolate(float score, IntToDoubleFunction sampler) {
        int fromTick = Mth.floor(score);
        float delta = Mth.clamp(score - fromTick, 0.0F, 1.0F);
        return (float) Mth.lerp(
                delta, sampler.applyAsDouble(fromTick), sampler.applyAsDouble(fromTick + 1));
    }

    private static float h6HeadX(int score) {
        if (score < 6) return 35.0F;
        if (score < 8) return 30.0F;
        if (score < 10) return 18.0F;
        if (score < 32) return 15.0F;
        if (score < 33) return 0.0F;
        if (score < 35) return -10.0F;
        if (score < 37) return -20.0F;
        if (score < 39) return -24.0F;
        return -26.0F;
    }

    private static float h6LeftArmX(int score) {
        return score < 6 ? 20.0F : 12.0F;
    }

    private static float h6LeftArmZ(int score) {
        if (score < 8) return 20.0F;
        if (score < 10) return -18.0F;
        if (score < 32) return -28.0F;
        if (score < 33) return -90.0F;
        if (score < 35) return -130.0F;
        if (score < 37) return -150.0F;
        if (score < 39) return -155.0F;
        return -160.0F;
    }

    private static float h6RightArmZ(int score) {
        if (score < 6) return -30.0F;
        if (score < 8) return -22.0F;
        if (score < 10) return 12.0F;
        if (score < 31) return 22.0F;
        if (score < 32) return 28.0F;
        if (score < 33) return 90.0F;
        if (score < 35) return 130.0F;
        if (score < 37) return 150.0F;
        if (score < 39) return 155.0F;
        return 160.0F;
    }
}
