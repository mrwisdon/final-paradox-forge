package io.github.finalparadox.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public final class BamboomerangItem extends Item {
    public static final String COOLDOWN_KEY="finalparadox.bamboomerang_cooldown";
    private final Multimap<Attribute,AttributeModifier> modifiers=ImmutableMultimap.<Attribute,AttributeModifier>builder()
            .put(Attributes.ATTACK_SPEED,new AttributeModifier(UUID.fromString("9a244b32-1535-4f2d-918f-695749db152b"),"Bamboomerang attack speed",-1.3D,AttributeModifier.Operation.ADDITION))
            .put(Attributes.ATTACK_DAMAGE,new AttributeModifier(UUID.fromString("690b7022-8bf1-45c7-a706-f42268fe8d50"),"Bamboomerang attack damage",5.0D,AttributeModifier.Operation.ADDITION)).build();
    public BamboomerangItem(){super(new Properties().stacksTo(1));}
    @Override public ItemStack getDefaultInstance(){ItemStack stack=super.getDefaultInstance();prepare(stack);return stack;}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){prepare(stack);}
    private static void prepare(ItemStack stack){stack.getOrCreateTag().putBoolean("Unbreakable",true);stack.getOrCreateTag().putInt("HideFlags",4);if(stack.getEnchantmentLevel(Enchantments.SHARPNESS)<3)stack.enchant(Enchantments.SHARPNESS,3);}
    @Override public Multimap<Attribute,AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot){return slot==EquipmentSlot.MAINHAND?modifiers:super.getDefaultAttributeModifiers(slot);}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.empty());for(int i=1;i<=3;i++)tooltip.add(Component.translatable("item.finalparadox.bamboomerang.lore."+i));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.bamboomerang.cooldown"));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.unique"));}
}
