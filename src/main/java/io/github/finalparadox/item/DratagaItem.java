package io.github.finalparadox.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import java.util.List;

public final class DratagaItem extends BowItem {
    public static final String COOLDOWN_KEY="finalparadox.drataga_cooldown";
    public DratagaItem(){super(new Properties().stacksTo(1));}
    @Override public ItemStack getDefaultInstance(){ItemStack stack=super.getDefaultInstance();prepare(stack);return stack;}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){prepare(stack);super.inventoryTick(stack,level,entity,slot,selected);}
    private static void prepare(ItemStack stack){stack.getOrCreateTag().putBoolean("Unbreakable",true);stack.getOrCreateTag().putInt("HideFlags",4);if(stack.getEnchantmentLevel(Enchantments.POWER_ARROWS)<5)stack.enchant(Enchantments.POWER_ARROWS,5);if(stack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS)<1)stack.enchant(Enchantments.FLAMING_ARROWS,1);if(stack.getEnchantmentLevel(Enchantments.INFINITY_ARROWS)<1)stack.enchant(Enchantments.INFINITY_ARROWS,1);}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.empty());for(int i=1;i<=6;i++)tooltip.add(Component.translatable("item.finalparadox.drataga.lore."+i));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.drataga.lore.7"));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.legendary"));}
}
