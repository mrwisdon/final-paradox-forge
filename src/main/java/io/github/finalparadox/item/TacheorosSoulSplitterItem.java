package io.github.finalparadox.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import java.util.List;

public final class TacheorosSoulSplitterItem extends SwordItem {
    public static final String HITS_KEY="finalparadox.tacheoros_hits";
    public TacheorosSoulSplitterItem(){super(Tiers.DIAMOND,3,-2.4F,new Properties().stacksTo(1));}
    @Override public ItemStack getDefaultInstance(){ItemStack stack=super.getDefaultInstance();prepare(stack);return stack;}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){prepare(stack);super.inventoryTick(stack,level,entity,slot,selected);}
    private static void prepare(ItemStack stack){stack.getOrCreateTag().putBoolean("Unbreakable",true);stack.getOrCreateTag().putInt("HideFlags",4);if(stack.getEnchantmentLevel(Enchantments.SHARPNESS)<4)stack.enchant(Enchantments.SHARPNESS,4);if(stack.getEnchantmentLevel(Enchantments.MOB_LOOTING)<1)stack.enchant(Enchantments.MOB_LOOTING,1);if(stack.getEnchantmentLevel(Enchantments.KNOCKBACK)<1)stack.enchant(Enchantments.KNOCKBACK,1);}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.empty());for(int i=1;i<=7;i++)tooltip.add(Component.translatable("item.finalparadox.tacheoros_soul_splitter.lore."+i));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.mythic"));}
}
