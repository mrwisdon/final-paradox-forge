package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.ability.NightfallAbilityState;
import io.github.finalparadox.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Draws the combo sword in the same player-local world frame used by the
 * original armor-stand animation instead of constraining it to a hand bone.
 */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID, value = Dist.CLIENT)
public final class NightfallComboAnimationRenderer {
    private static final float ARMOR_STAND_HEAD_HEIGHT = 1.5F;

    private NightfallComboAnimationRenderer() {
    }

    @SubscribeEvent
    public static void renderAfterPlayer(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        ItemStack stack;
        boolean offhand;
        if (isAnimatedNightfall(mainHand)) {
            stack = mainHand;
            offhand = false;
        } else if (isAnimatedNightfall(offHand)) {
            stack = offHand;
            offhand = true;
        } else if (mainHand.is(ModItems.NIGHTFALL.get())) {
            stack = mainHand;
            offhand = false;
        } else if (offHand.is(ModItems.NIGHTFALL.get())) {
            stack = offHand;
            offhand = true;
        } else {
            return;
        }

        CompoundTag tag = stack.getTag();
        boolean animated = tag != null
                && tag.contains(NightfallAbilityState.COMBO_ANIMATION_SCORE_TAG);
        float abilityYaw = animated
                ? tag.getFloat(NightfallAbilityState.COMBO_ANIMATION_YAW_TAG)
                : Mth.rotLerp(event.getPartialTick(), player.yBodyRotO, player.yBodyRot);
        InteractionHand renderedHand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        float attackProgress = animated
                ? -1.0F
                : NightfallNormalAttackAnimationState.progress(
                        player, renderedHand, event.getPartialTick());
        int normalAttack = animated
                ? -1
                : NightfallNormalAttackAnimationState.attackIndex(player, renderedHand);
        int chainFromAttack = animated
                ? -1
                : NightfallNormalAttackAnimationState.chainFromAttackIndex(
                        player, renderedHand);
        float chainFromProgress = animated
                ? -1.0F
                : NightfallNormalAttackAnimationState.chainFromProgress(
                        player, renderedHand);
        NightfallComboFrames.Frame frame;
        if (animated) {
            frame = NightfallComboFrames.sample(
                    tag.getInt(NightfallAbilityState.COMBO_ANIMATION_SCORE_TAG), event.getPartialTick());
        } else if (attackProgress >= 0.0F && normalAttack >= 0) {
            frame = NightfallNormalAttackFrames.sample(
                    normalAttack, attackProgress, chainFromAttack, chainFromProgress);
        } else {
            frame = NightfallNormalAttackFrames.IDLE_FRAME;
        }
        float side = offhand ? -1.0F : 1.0F;

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        // Command-local ^left/^up/^forward frame relative to the tracked target.
        pose.mulPose(Axis.YP.rotationDegrees(-abilityYaw));
        /*
         * Source coordinates locate armor-stand entities. Their helmet models
         * are rendered 1.5 blocks above the entity origin by the normal-sized
         * armor-stand base transform; direct compound rendering must restore
         * that common height explicitly.
         */
        pose.translate(frame.left() * side,
                frame.up() + ARMOR_STAND_HEAD_HEIGHT, frame.forward());
        pose.mulPose(Axis.YP.rotationDegrees(-frame.yaw() * side));
        pose.mulPose(Axis.XP.rotationDegrees(frame.pitch()));
        NightfallItemRenderer.renderVisual(
                pose, event.getMultiBufferSource(), event.getPackedLight(), player.level(), player.getId());
        pose.popPose();
    }

    private static boolean isAnimatedNightfall(ItemStack stack) {
        return stack.is(ModItems.NIGHTFALL.get()) && stack.hasTag()
                && stack.getTag().contains(NightfallAbilityState.COMBO_ANIMATION_SCORE_TAG);
    }
}
