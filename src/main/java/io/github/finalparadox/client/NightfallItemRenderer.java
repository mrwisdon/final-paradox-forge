package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.registry.ModItems;
import io.github.finalparadox.ability.NightfallAbilityState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Renders one coherent model based on MarawThar's white end-rod blade and conduit
 * core. The original armor-stand coordinates remain the animation driver, but
 * the visual pieces share one model origin so helmet transforms cannot separate
 * them.
 */
public final class NightfallItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final int FULL_BRIGHT = 15728880;

    public NightfallItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
                             MultiBufferSource buffers, int light, int overlay) {
        if (context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            /*
             * RenderPlayerEvent draws both the source idle pose and combo
             * frames in player-local world coordinates. A hand-bone transform
             * cannot reproduce the original armor-stand core placement.
             */
            return;
        }

        if (context == ItemDisplayContext.GUI) {
            /*
             * Inventory slots use the source reward's dedicated 16x16 sprite.
             * Reusing the world-sized compound sword here leaves only a dark,
             * sub-pixel sliver after GUI scaling. ItemRenderer has already
             * translated the outer custom model by (-0.5, -0.5, -0.5);
             * cancel that before recursively rendering the generated icon,
             * whose own render pass applies the required centering transform.
             */
            pose.pushPose();
            pose.translate(0.5D, 0.5D, 0.5D);
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            itemRenderer.renderStatic(new ItemStack(ModItems.NIGHTFALL_GUI_ICON.get()),
                    ItemDisplayContext.GUI, FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    pose, buffers, Minecraft.getInstance().level, 0);
            pose.popPose();
            return;
        }

        pose.pushPose();
        applyNormalAttackTransform(stack, context, pose);
        ItemDisplayContext proxyContext = applyDisplayTransform(context, pose);
        renderVisual(pose, buffers, light, Minecraft.getInstance().level, 0, proxyContext);
        pose.popPose();
    }

    public static void renderVisual(PoseStack pose, MultiBufferSource buffers, int light,
                                    Level level, int seed) {
        renderVisual(pose, buffers, light, level, seed, ItemDisplayContext.FIXED);
    }

    private static void renderVisual(PoseStack pose, MultiBufferSource buffers, int light,
                                     Level level, int seed, ItemDisplayContext context) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(new ItemStack(ModItems.NIGHTFALL_ICON.get()), context,
                FULL_BRIGHT, OverlayTexture.NO_OVERLAY, pose, buffers, level, seed);
    }

    private static ItemDisplayContext applyDisplayTransform(ItemDisplayContext context, PoseStack pose) {
        if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            /*
             * The coherent model's blade points along local +Y. Pitching that
             * axis toward -Z makes the idle blade project into the scene
             * instead of lying diagonally across the screen. The small yaw
             * aims the tip inward from the right hand toward the crosshair.
             */
            pose.translate(-0.03D, -0.02D, -0.10D);
            pose.mulPose(Axis.YP.rotationDegrees(15.0F));
            pose.mulPose(Axis.XP.rotationDegrees(-68.0F));
            pose.scale(0.62F, 0.62F, 0.62F);
            return ItemDisplayContext.FIXED;
        }

        if (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            pose.translate(0.03D, -0.02D, -0.10D);
            pose.mulPose(Axis.YP.rotationDegrees(-15.0F));
            pose.mulPose(Axis.XP.rotationDegrees(-68.0F));
            pose.scale(0.62F, 0.62F, 0.62F);
            return ItemDisplayContext.FIXED;
        }

        if (context == ItemDisplayContext.GROUND) {
            pose.translate(0.0D, 0.10D, 0.0D);
            pose.mulPose(Axis.XP.rotationDegrees(82.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(-42.0F));
            pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            pose.scale(0.42F, 0.42F, 0.42F);
            return ItemDisplayContext.FIXED;
        }

        // Item frames and fallback contexts keep the coherent model centered.
        pose.mulPose(Axis.ZP.rotationDegrees(-42.0F));
        pose.mulPose(Axis.YP.rotationDegrees(45.0F));
        pose.scale(0.42F, 0.42F, 0.42F);
        return ItemDisplayContext.FIXED;
    }

    private static void applyNormalAttackTransform(ItemStack stack, ItemDisplayContext context,
                                                   PoseStack pose) {
        boolean rightHand = context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean leftHand = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if (!rightHand && !leftHand) return;
        if (stack.hasTag()
                && stack.getTag().contains(NightfallAbilityState.COMBO_ANIMATION_SCORE_TAG)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        HumanoidArm physicalArm = rightHand ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        InteractionHand renderedHand = minecraft.player.getMainArm() == physicalArm
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;

        if (minecraft.player.swingingArm == renderedHand) {
            float vanillaProgress = minecraft.player.getAttackAnim(minecraft.getFrameTime());
            if (vanillaProgress > 0.001F) {
                cancelVanillaAttackTransform(pose, physicalArm, vanillaProgress);
            }
        }
        float progress = NightfallNormalAttackAnimationState.progress(
                minecraft.player, renderedHand, minecraft.getFrameTime());
        int attackIndex =
                NightfallNormalAttackAnimationState.attackIndex(minecraft.player, renderedHand);
        if (progress < 0.0F || attackIndex < 0) return;
        int chainFromAttack = NightfallNormalAttackAnimationState.chainFromAttackIndex(
                minecraft.player, renderedHand);
        float chainFromProgress = NightfallNormalAttackAnimationState.chainFromProgress(
                minecraft.player, renderedHand);
        NightfallNormalAttackFrames.FirstPersonPose attack =
                NightfallNormalAttackFrames.sampleFirstPerson(
                        attackIndex, progress, chainFromAttack, chainFromProgress);
        float side = rightHand ? 1.0F : -1.0F;
        pose.translate(attack.sideways() * side, -attack.down(), attack.forward());
        pose.mulPose(Axis.YP.rotationDegrees(attack.yaw() * side));
        pose.mulPose(Axis.ZP.rotationDegrees(attack.roll() * side));
        pose.mulPose(Axis.XP.rotationDegrees(attack.pitch()));
    }

    private static void cancelVanillaAttackTransform(PoseStack pose, HumanoidArm arm,
                                                     float attackProgress) {
        float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float squaredSwing = Mth.sin(attackProgress * attackProgress * (float) Math.PI);
        float rootSwing = Mth.sin(Mth.sqrt(attackProgress) * (float) Math.PI);
        /*
         * ItemInHandRenderer applies Y, Z, X, Y rotations before invoking the
         * custom renderer. Apply their exact inverse in reverse order so
         * repeated vanilla swings cannot restart Nightfall's visual chop.
         */
        pose.mulPose(Axis.YP.rotationDegrees(side * 45.0F));
        pose.mulPose(Axis.XP.rotationDegrees(rootSwing * 80.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(side * rootSwing * 20.0F));
        pose.mulPose(Axis.YP.rotationDegrees(-side * (45.0F - squaredSwing * 20.0F)));
    }
}
