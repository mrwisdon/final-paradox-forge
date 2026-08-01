package io.github.ragecraft4reforged.content.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public final class Rc4Enchantment extends Enchantment {
    private final Target target;
    private final int maxLevel;

    public Rc4Enchantment(Rarity rarity, Target target, int maxLevel) {
        super(rarity, target.category, target.slots);
        this.target = target;
        this.maxLevel = maxLevel;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 20;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return switch (target) {
            case MELEE -> stack.getItem() instanceof SwordItem
                    || stack.getItem() instanceof AxeItem
                    || stack.getItem() instanceof PickaxeItem
                    || stack.getItem() instanceof TridentItem;
            case BOW -> stack.getItem() instanceof BowItem;
            case HELMET -> stack.getItem() instanceof ArmorItem armor
                    && armor.getType() == ArmorItem.Type.HELMET;
        };
    }

    public enum Target {
        MELEE(EnchantmentCategory.WEAPON, EquipmentSlot.MAINHAND),
        BOW(EnchantmentCategory.BOW, EquipmentSlot.MAINHAND),
        HELMET(EnchantmentCategory.ARMOR_HEAD, EquipmentSlot.HEAD);

        private final EnchantmentCategory category;
        private final EquipmentSlot[] slots;

        Target(EnchantmentCategory category, EquipmentSlot... slots) {
            this.category = category;
            this.slots = slots;
        }
    }
}
