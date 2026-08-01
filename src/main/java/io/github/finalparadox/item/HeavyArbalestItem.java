package io.github.finalparadox.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public final class HeavyArbalestItem extends CrossbowItem {
    public static final String COOLDOWN_KEY = "finalparadox.heavy_arbalest_cooldown";
    public static final String ARROW_KEY = "finalparadox.heavy_arbalest_arrow";
    private final Multimap<Attribute, AttributeModifier> modifiers = ImmutableMultimap.of(
            Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("3ecb71e3-250a-4f1c-bc59-1a301a2fddbb"),
                    "Heavy arbalest movement penalty", -0.1D, AttributeModifier.Operation.MULTIPLY_BASE));

    public HeavyArbalestItem() { super(new Properties().stacksTo(1)); }
    @Override public ItemStack getDefaultInstance(){ItemStack stack=super.getDefaultInstance();prepare(stack);return stack;}
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected){prepare(stack);}
    private static void prepare(ItemStack stack){stack.getOrCreateTag().putBoolean("Unbreakable",true);stack.getOrCreateTag().putInt("HideFlags",4);if(stack.getEnchantmentLevel(Enchantments.QUICK_CHARGE)<3)stack.enchant(Enchantments.QUICK_CHARGE,3);}
    @Override public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot){return slot==EquipmentSlot.MAINHAND?modifiers:super.getDefaultAttributeModifiers(slot);}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.translatable("item.finalparadox.heavy_arbalest.power"));tooltip.add(Component.empty());for(int i=1;i<=6;i++)tooltip.add(Component.translatable("item.finalparadox.heavy_arbalest.lore."+i));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.legendary"));}
}
