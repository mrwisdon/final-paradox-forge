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

public final class SoullessEdgeItem extends SwordItem {
    public static final String SOULS_KEY="finalparadox.soulless_edge_souls";
    public static final String STORM_KEY="finalparadox.soulless_edge_storm";
    public static final String STORM_TICKS_KEY="finalparadox.soulless_edge_storm_ticks";
    public SoullessEdgeItem(){super(Tiers.IRON,3,-2.4F,new Properties().stacksTo(1));}
    @Override public ItemStack getDefaultInstance(){ItemStack stack=super.getDefaultInstance();prepare(stack);return stack;}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){prepare(stack);super.inventoryTick(stack,level,entity,slot,selected);}
    private static void prepare(ItemStack stack){stack.getOrCreateTag().putBoolean("Unbreakable",true);stack.getOrCreateTag().putInt("HideFlags",4);if(stack.getEnchantmentLevel(Enchantments.SHARPNESS)<4)stack.enchant(Enchantments.SHARPNESS,4);if(stack.getEnchantmentLevel(Enchantments.SWEEPING_EDGE)<1)stack.enchant(Enchantments.SWEEPING_EDGE,1);}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.empty());for(int i=1;i<=15;i++)tooltip.add(Component.translatable("item.finalparadox.soulless_edge.lore."+i));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.mythic"));}
}
