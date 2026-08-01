package io.github.finalparadox.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class PremiumSmugglingInsigniaItem extends Item {
    public PremiumSmugglingInsigniaItem(){super(new Properties().stacksTo(1));}
    @Override public boolean isFoil(ItemStack stack){return true;}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.premium_smuggling_insignia.lore.1"));tooltip.add(Component.translatable("item.finalparadox.premium_smuggling_insignia.lore.2"));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.exclusive"));}
}
