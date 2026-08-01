package io.github.finalparadox.item;

import io.github.finalparadox.entity.TerrastalkerRoverEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

/**
 * Adaptive Defense Matrix: placed in the leggings slot and dropped (Q) to summon
 * the improved Terrastalker, mirroring the original Final Paradox item flow.
 */
public final class AdaptiveDefenseMatrixItem extends Item {
    public static final String COOLDOWN_KEY = "finalparadox.adaptive_defense_matrix_cooldown";

    public AdaptiveDefenseMatrixItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (!(player instanceof ServerPlayer server)) return InteractionResultHolder.pass(stack);
        if (!player.getItemBySlot(EquipmentSlot.LEGS).isEmpty()) {
            server.displayClientMessage(
                    Component.translatable("message.finalparadox.defense_matrix.leggings_slot"), true);
            return InteractionResultHolder.fail(stack);
        }
        player.setItemSlot(EquipmentSlot.LEGS, stack.copy());
        stack.shrink(1);
        return InteractionResultHolder.success(stack);
    }

    /** Called from the toss handler once the matrix leaves the leggings slot. */
    public static boolean activate(ServerPlayer player) {
        return TerrastalkerRoverEntity.spawnImproved(player);
    }
}
