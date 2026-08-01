package io.github.finalparadox.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import java.util.List;

public final class LastSparkOfHopeItem extends ArmorItem {
    public static final String COOLDOWN_KEY="finalparadox.last_spark_cooldown";
    public LastSparkOfHopeItem(){super(ArmorMaterials.IRON,Type.LEGGINGS,new Properties().stacksTo(1));}
    @Override public ItemStack getDefaultInstance(){ItemStack stack=super.getDefaultInstance();prepare(stack);return stack;}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){prepare(stack);super.inventoryTick(stack,level,entity,slot,selected);}
    private static void prepare(ItemStack stack){stack.getOrCreateTag().putBoolean("Unbreakable",true);stack.getOrCreateTag().putInt("HideFlags",4);if(stack.getEnchantmentLevel(Enchantments.ALL_DAMAGE_PROTECTION)<3)stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION,3);if(stack.getEnchantmentLevel(Enchantments.PROJECTILE_PROTECTION)<2)stack.enchant(Enchantments.PROJECTILE_PROTECTION,2);}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.empty());for(int i=1;i<=8;i++)tooltip.add(Component.translatable("item.finalparadox.last_spark_of_hope.lore."+i));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.unique"));}
}
