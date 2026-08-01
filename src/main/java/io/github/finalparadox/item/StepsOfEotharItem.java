package io.github.finalparadox.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.finalparadox.ability.EotharTimeStopState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class StepsOfEotharItem extends DyeableArmorItem {
    private static final int READY_COLOR = 0xFFFFFF;
    private static final int COOLDOWN_COLOR = 0x3A3A3A;
    private static final UUID ARMOR_UUID = UUID.fromString("fe4bd7fd-eac6-4b75-bd25-74a164a6e83f");
    private static final UUID TOUGHNESS_UUID = UUID.fromString("2ae3d994-89d6-4d3c-bf8e-d483aea35448");
    private static final UUID SPEED_UUID = UUID.fromString("c6c58d8d-9466-49ca-a798-842660cfbf2c");
    private final Multimap<Attribute, AttributeModifier> feetModifiers;

    public StepsOfEotharItem() {
        super(ArmorMaterials.LEATHER, ArmorItem.Type.BOOTS, new Properties().stacksTo(1).fireResistant());
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ARMOR, new AttributeModifier(ARMOR_UUID,
                "Steps of Eo'Thar armor", 3.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(TOUGHNESS_UUID,
                "Steps of Eo'Thar toughness", 2.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(SPEED_UUID,
                "Steps of Eo'Thar speed", 0.04D, AttributeModifier.Operation.MULTIPLY_BASE));
        feetModifiers = builder.build();
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        prepare(stack, false);
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        boolean cooldown = entity instanceof ServerPlayer player
                && player.getPersistentData().getInt(EotharTimeStopState.COOLDOWN_KEY) > 0;
        prepare(stack, cooldown);
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    private void prepare(ItemStack stack, boolean cooldown) {
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        stack.getOrCreateTag().putInt("HideFlags", 0);
        stack.getOrCreateTag().putInt("CustomModelData", cooldown ? 1 : 0);
        setColor(stack, cooldown ? COOLDOWN_COLOR : READY_COLOR);
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        enchantments.put(Enchantments.ALL_DAMAGE_PROTECTION,
                Math.max(3, enchantments.getOrDefault(Enchantments.ALL_DAMAGE_PROTECTION, 0)));
        enchantments.put(Enchantments.BLAST_PROTECTION,
                Math.max(2, enchantments.getOrDefault(Enchantments.BLAST_PROTECTION, 0)));
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.FEET ? feetModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty());
        for (int line = 1; line <= 12; line++) {
            tooltip.add(Component.translatable("item.finalparadox.steps_of_eothar.lore." + line));
            if (line == 8 || line == 10) {
                tooltip.add(Component.empty());
            }
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.finalparadox.legendary"));
    }
}
