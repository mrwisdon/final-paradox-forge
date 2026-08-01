package io.github.finalparadox.item;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;

public final class GlaivorusItem extends SwordItem {
    public static final int COOLDOWN_TICKS = 20 * 60 * 4;
    // Hide only the vanilla "Unbreakable" line; enchantments remain visible.
    // In particular, do not hide TooltipPart.ADDITIONAL (bit 32), which contains our lore.
    private static final int HIDE_UNBREAKABLE = 4;

    public GlaivorusItem() {
        super(Tiers.IRON, 3, -2.4F, new Properties().stacksTo(1));
    }

    public static boolean activate(ServerPlayer player, ItemStack stack) {
        int remainingCooldown = GlaivorusAbilityState.cooldownRemaining(player);
        if (remainingCooldown > 0) {
            int seconds = (int) Math.ceil(remainingCooldown / 20.0D);
            player.displayClientMessage(Component.translatable("message.finalparadox.glaivorus.cooldown", seconds).withStyle(ChatFormatting.RED), true);
            player.level().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                    SoundSource.PLAYERS, 1.0F, 2.0F);
            return false;
        }

        if (!GlaivorusAbilityState.tryStart(player)) {
            player.displayClientMessage(Component.translatable("message.finalparadox.glaivorus.no_fuel").withStyle(ChatFormatting.RED), true);
            player.level().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                    SoundSource.PLAYERS, 1.0F, 2.0F);
            return false;
        }
        Level level = player.level();
        GlaivorusAbilityState.startCooldown(player);
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.5F);
        int phrase = player.getRandom().nextInt(3) + 1;
        player.displayClientMessage(Component.translatable("message.finalparadox.glaivorus.activated." + phrase), false);
        return true;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        prepareStack(stack);
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        prepareStack(stack);
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    public static void prepareStack(ItemStack stack) {
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        stack.getOrCreateTag().putInt("HideFlags", HIDE_UNBREAKABLE);
        if (stack.getEnchantmentLevel(Enchantments.SHARPNESS) < 4) stack.enchant(Enchantments.SHARPNESS, 4);
        if (stack.getEnchantmentLevel(Enchantments.SWEEPING_EDGE) < 3) stack.enchant(Enchantments.SWEEPING_EDGE, 3);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty());
        for (int line = 1; line <= 14; line++) {
            tooltip.add(Component.translatable("item.finalparadox.glaivorus.lore." + line));
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.finalparadox.glaivorus.legendary"));
    }
}
