package io.github.finalparadox.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;

public final class WindcutterItem extends SwordItem {
    public WindcutterItem() {
        super(Tiers.STONE, 3, -2.4F, new Properties());
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

    private static void prepareStack(ItemStack stack) {
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        stack.getOrCreateTag().putInt("HideFlags", 4);
        if (stack.getEnchantmentLevel(Enchantments.SHARPNESS) < 2) stack.enchant(Enchantments.SHARPNESS, 2);
        if (stack.getEnchantmentLevel(Enchantments.SWEEPING_EDGE) < 1) stack.enchant(Enchantments.SWEEPING_EDGE, 1);
        if (stack.getEnchantmentLevel(Enchantments.KNOCKBACK) < 1) stack.enchant(Enchantments.KNOCKBACK, 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty());
        for (int line = 1; line <= 3; line++) tooltip.add(Component.translatable("item.finalparadox.windcutter.lore." + line));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.finalparadox.unique"));
    }
}
