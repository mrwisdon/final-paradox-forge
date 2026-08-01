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

public final class ProfaneArcaneBladeItem extends SwordItem {
    public ProfaneArcaneBladeItem(){super(Tiers.NETHERITE,3,-2.4F,new Properties().stacksTo(1).fireResistant());}
    @Override public ItemStack getDefaultInstance(){ItemStack stack=super.getDefaultInstance();prepare(stack);return stack;}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){prepare(stack);super.inventoryTick(stack,level,entity,slot,selected);}
    private static void prepare(ItemStack stack){stack.getOrCreateTag().putBoolean("Unbreakable",true);stack.getOrCreateTag().putInt("HideFlags",4);if(stack.getEnchantmentLevel(Enchantments.SHARPNESS)<8)stack.enchant(Enchantments.SHARPNESS,8);}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.empty());for(int i=1;i<=19;i++){tooltip.add(Component.translatable("item.finalparadox.profane_arcane_blade.lore."+i));if(i==6||i==17)tooltip.add(Component.empty());}tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.legendary"));}
}
