package io.github.finalparadox.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.UUID;

public final class HarvesterScytheItem extends HoeItem {
    public static final String COOLDOWN_KEY="finalparadox.harvester_cooldown";
    private final Multimap<Attribute, AttributeModifier> modifiers;
    public HarvesterScytheItem(){super(Tiers.NETHERITE,-4,0.0F,new Properties().stacksTo(1).fireResistant());ImmutableMultimap.Builder<Attribute,AttributeModifier>b=ImmutableMultimap.builder();b.put(Attributes.ATTACK_DAMAGE,new AttributeModifier(UUID.fromString("690b8aa2-8bf6-4f07-a702-0e90fe8e6ed0"),"Harvester damage",7.0D,AttributeModifier.Operation.ADDITION));b.put(Attributes.ATTACK_SPEED,new AttributeModifier(UUID.fromString("9a262672-1536-4e2d-9192-63c7f2feae8b"),"Harvester speed",-1.3D,AttributeModifier.Operation.ADDITION));modifiers=b.build();}
    @Override public Multimap<Attribute,AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot){return slot==EquipmentSlot.MAINHAND?modifiers:super.getDefaultAttributeModifiers(slot);}
    @Override public ItemStack getDefaultInstance(){ItemStack stack=super.getDefaultInstance();prepare(stack);return stack;}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){prepare(stack);super.inventoryTick(stack,level,entity,slot,selected);}
    private static void prepare(ItemStack stack){stack.getOrCreateTag().putBoolean("Unbreakable",true);stack.getOrCreateTag().putInt("HideFlags",4);if(stack.getEnchantmentLevel(Enchantments.SHARPNESS)<4)stack.enchant(Enchantments.SHARPNESS,4);if(stack.getEnchantmentLevel(Enchantments.KNOCKBACK)<2)stack.enchant(Enchantments.KNOCKBACK,2);}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.empty());for(int i=1;i<=6;i++)tooltip.add(Component.translatable("item.finalparadox.harvester_scythe.lore."+i));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.harvester_scythe.lore.7"));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.mythic"));}
}
