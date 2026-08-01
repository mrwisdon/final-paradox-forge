package io.github.finalparadox.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;

public final class BouncingBootsItem extends ArmorItem implements DyeableLeatherItem {
    public static final int COLOR = 7533421;

    public BouncingBootsItem() {
        super(ArmorMaterials.LEATHER, Type.BOOTS, new Properties().durability(65));
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
        stack.getOrCreateTag().putInt("HideFlags", 4 | 64);
        stack.getOrCreateTagElement("display").putInt("color", COLOR);
        if (stack.getEnchantmentLevel(Enchantments.FALL_PROTECTION) < 4) stack.enchant(Enchantments.FALL_PROTECTION, 4);
        if (stack.getEnchantmentLevel(Enchantments.ALL_DAMAGE_PROTECTION) < 2) stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 2);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty());
        for (int line = 1; line <= 8; line++) tooltip.add(Component.translatable("item.finalparadox.bouncing_boots.lore." + line));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.finalparadox.unique"));
    }
}
