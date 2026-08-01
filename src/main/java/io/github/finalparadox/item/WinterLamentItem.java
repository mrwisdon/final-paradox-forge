package io.github.finalparadox.item;
import net.minecraft.network.chat.Component;import net.minecraft.world.entity.Entity;import net.minecraft.world.item.*;import net.minecraft.world.item.enchantment.Enchantments;import net.minecraft.world.level.Level;import java.util.List;
public final class WinterLamentItem extends SwordItem{
 public static final String CRYSTALS_KEY="finalparadox.winter_crystals";public WinterLamentItem(){super(Tiers.DIAMOND,3,-2.4F,new Properties().stacksTo(1));}
 @Override public ItemStack getDefaultInstance(){ItemStack s=super.getDefaultInstance();prepare(s);return s;}@Override public void inventoryTick(ItemStack s,Level l,Entity e,int slot,boolean selected){prepare(s);super.inventoryTick(s,l,e,slot,selected);}private static void prepare(ItemStack s){s.getOrCreateTag().putBoolean("Unbreakable",true);s.getOrCreateTag().putInt("HideFlags",4);if(s.getEnchantmentLevel(Enchantments.SHARPNESS)<8)s.enchant(Enchantments.SHARPNESS,8);if(s.getEnchantmentLevel(Enchantments.SWEEPING_EDGE)<3)s.enchant(Enchantments.SWEEPING_EDGE,3);}
 @Override public void appendHoverText(ItemStack s,Level l,List<Component>t,TooltipFlag f){t.add(Component.empty());for(int i=1;i<=14;i++)t.add(Component.translatable("item.finalparadox.winter_lament.lore."+i));t.add(Component.empty());t.add(Component.translatable("item.finalparadox.legendary"));}
}
