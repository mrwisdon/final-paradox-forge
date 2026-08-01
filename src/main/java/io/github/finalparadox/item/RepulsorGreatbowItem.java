package io.github.finalparadox.item;
import net.minecraft.network.chat.Component;import net.minecraft.world.entity.Entity;import net.minecraft.world.item.*;import net.minecraft.world.item.enchantment.Enchantments;import net.minecraft.world.level.Level;import java.util.List;
public final class RepulsorGreatbowItem extends BowItem{
 public static final String COOLDOWN_KEY="finalparadox.repulsor_bow_cooldown",ARROW_KEY="finalparadox.repulsor_arrow";
 public RepulsorGreatbowItem(){super(new Properties().stacksTo(1));}
 @Override public ItemStack getDefaultInstance(){ItemStack s=super.getDefaultInstance();prepare(s);return s;}@Override public void inventoryTick(ItemStack s,Level l,Entity e,int slot,boolean selected){prepare(s);super.inventoryTick(s,l,e,slot,selected);}private static void prepare(ItemStack s){s.getOrCreateTag().putBoolean("Unbreakable",true);s.getOrCreateTag().putInt("HideFlags",4);if(s.getEnchantmentLevel(Enchantments.POWER_ARROWS)<3)s.enchant(Enchantments.POWER_ARROWS,3);if(s.getEnchantmentLevel(Enchantments.PUNCH_ARROWS)<1)s.enchant(Enchantments.PUNCH_ARROWS,1);}
 @Override public void appendHoverText(ItemStack s,Level l,List<Component> t,TooltipFlag f){t.add(Component.empty());for(int i=1;i<=5;i++)t.add(Component.translatable("item.finalparadox.repulsor_greatbow.lore."+i));t.add(Component.empty());t.add(Component.translatable("item.finalparadox.mythic"));}
}
