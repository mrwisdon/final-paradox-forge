package io.github.finalparadox.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public final class LethalBloodDaggerItem extends SwordItem {
    private final Multimap<Attribute, AttributeModifier> modifiers;
    public LethalBloodDaggerItem() {
        super(Tiers.IRON, 0, -2.4F, new Properties().stacksTo(1));
        modifiers = ImmutableMultimap.of(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("cc5ec830-0a3d-40c4-afd3-9736dfe47b7a"), "Lethal blood dagger damage", 6.0D, AttributeModifier.Operation.ADDITION),
                Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("d42a8b4e-66b9-4e37-bc48-e934f4d1aa3c"), "Lethal blood dagger speed", -2.1D, AttributeModifier.Operation.ADDITION));
    }
    @Override public ItemStack getDefaultInstance() { ItemStack stack = super.getDefaultInstance(); prepare(stack); return stack; }
    @Override public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected) { prepare(stack); }
    private static void prepare(ItemStack stack) { stack.getOrCreateTag().putBoolean("Unbreakable", true); stack.getOrCreateTag().putInt("HideFlags", 4); if (stack.getEnchantmentLevel(Enchantments.SHARPNESS) < 2) stack.enchant(Enchantments.SHARPNESS, 2); }
    @Override public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) { return slot == EquipmentSlot.MAINHAND ? modifiers : super.getDefaultAttributeModifiers(slot); }
    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) { tooltip.add(Component.empty()); for (int i = 1; i <= 6; i++) tooltip.add(Component.translatable("item.finalparadox.blood_daggers.lore." + i)); tooltip.add(Component.empty()); tooltip.add(Component.translatable("item.finalparadox.legendary")); }
}
