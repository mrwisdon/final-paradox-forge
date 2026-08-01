package io.github.finalparadox.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import java.util.List;

public final class PicomerangItem extends PickaxeItem {
    public static final String COOLDOWN_KEY = "finalparadox.picomerang_cooldown";
    public PicomerangItem() { super(Tiers.IRON, 1, -2.8F, new Properties().stacksTo(1)); }
    @Override public ItemStack getDefaultInstance(){ItemStack stack=super.getDefaultInstance();prepare(stack);return stack;}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){prepare(stack);super.inventoryTick(stack,level,entity,slot,selected);}
    private static void prepare(ItemStack stack){stack.getOrCreateTag().putBoolean("Unbreakable",true);stack.getOrCreateTag().putInt("HideFlags",4);if(stack.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY)<4)stack.enchant(Enchantments.BLOCK_EFFICIENCY,4);}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.empty());for(int i=1;i<=5;i++)tooltip.add(Component.translatable("item.finalparadox.picomerang.lore."+i));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.picomerang.lore.6"));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.legendary"));}
}
