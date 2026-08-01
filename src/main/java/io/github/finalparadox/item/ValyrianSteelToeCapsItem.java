package io.github.finalparadox.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.UUID;

public final class ValyrianSteelToeCapsItem extends ArmorItem {
    private static final UUID SPEED_UUID=UUID.fromString("a9f9ea07-d74c-486b-b7da-27c4a2452c08");
    private final Multimap<Attribute,AttributeModifier> feetModifiers;
    public ValyrianSteelToeCapsItem(){super(ArmorMaterials.IRON,Type.BOOTS,new Properties().stacksTo(1));ImmutableMultimap.Builder<Attribute,AttributeModifier> builder=ImmutableMultimap.builder();builder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.FEET));builder.put(Attributes.MOVEMENT_SPEED,new AttributeModifier(SPEED_UUID,"Valyrian toe caps speed",0.20D,AttributeModifier.Operation.MULTIPLY_TOTAL));feetModifiers=builder.build();}
    @Override public ItemStack getDefaultInstance(){ItemStack stack=super.getDefaultInstance();prepare(stack);return stack;}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){prepare(stack);super.inventoryTick(stack,level,entity,slot,selected);}
    private static void prepare(ItemStack stack){stack.getOrCreateTag().putBoolean("Unbreakable",true);stack.getOrCreateTag().putInt("HideFlags",4);if(stack.getEnchantmentLevel(Enchantments.ALL_DAMAGE_PROTECTION)<4)stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION,4);if(stack.getEnchantmentLevel(Enchantments.BLAST_PROTECTION)<2)stack.enchant(Enchantments.BLAST_PROTECTION,2);if(stack.getEnchantmentLevel(Enchantments.FALL_PROTECTION)<2)stack.enchant(Enchantments.FALL_PROTECTION,2);}
    @Override public Multimap<Attribute,AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot){return slot==EquipmentSlot.FEET?feetModifiers:super.getDefaultAttributeModifiers(slot);}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.empty());for(int i=1;i<=7;i++)tooltip.add(Component.translatable("item.finalparadox.valyrian_steel_toe_caps.lore."+i));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.legendary"));}
}
