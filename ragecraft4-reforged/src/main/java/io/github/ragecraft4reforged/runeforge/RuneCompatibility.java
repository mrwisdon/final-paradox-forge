package io.github.ragecraft4reforged.runeforge;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public enum RuneCompatibility {
    SWORD,
    AXE,
    BOW,
    ARMOR,
    SWORD_OR_AXE;

    public boolean matches(ItemStack stack) {
        return switch (this) {
            case SWORD -> stack.getItem() instanceof SwordItem;
            case AXE -> stack.getItem() instanceof AxeItem;
            case BOW -> stack.getItem() instanceof BowItem;
            case ARMOR -> stack.getItem() instanceof ArmorItem;
            case SWORD_OR_AXE -> stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
        };
    }
}
