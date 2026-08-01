package io.github.finalparadox.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public enum EctronArmorMaterial implements ArmorMaterial { INSTANCE;
    @Override public int getDurabilityForType(ArmorItem.Type type){return switch(type){case BOOTS->195;case LEGGINGS->225;case CHESTPLATE->240;case HELMET->165;};}
    @Override public int getDefenseForType(ArmorItem.Type type){return switch(type){case BOOTS->2;case LEGGINGS->5;case CHESTPLATE->6;case HELMET->2;};}
    @Override public int getEnchantmentValue(){return 9;}
    @Override public SoundEvent getEquipSound(){return SoundEvents.ARMOR_EQUIP_IRON;}
    @Override public Ingredient getRepairIngredient(){return Ingredient.of(Items.IRON_INGOT);}
    @Override public String getName(){return "finalparadox:ectron";}
    @Override public float getToughness(){return 0;}
    @Override public float getKnockbackResistance(){return 0;}
}
