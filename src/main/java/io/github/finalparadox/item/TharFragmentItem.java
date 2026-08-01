package io.github.finalparadox.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** The persistent reward produced by Thar Kroo's sacrifice. */
public final class TharFragmentItem extends Item {
    public TharFragmentItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        for (int line = 1; line <= 3; line++) {
            tooltip.add(Component.translatable("item.finalparadox.thar_fragment.lore." + line));
        }
    }
}
