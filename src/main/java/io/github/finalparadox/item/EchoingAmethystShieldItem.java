package io.github.finalparadox.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.finalparadox.entity.EchoingShieldEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public final class EchoingAmethystShieldItem extends Item {
    public static final String COOLDOWN_KEY="finalparadox.echoing_shield_cooldown";
    public static final String READY_FOIL_KEY="finalparadox.echoing_shield_ready_foil";
    public static final String IN_FLIGHT_KEY="finalparadox.echoing_shield_in_flight";
    private final Multimap<Attribute,AttributeModifier> modifiers=ImmutableMultimap.<Attribute,AttributeModifier>builder()
            .put(Attributes.MOVEMENT_SPEED,new AttributeModifier(UUID.fromString("8bea7256-c83e-4c6b-b2cd-f44824b5b079"),"Echoing shield speed",.02D,AttributeModifier.Operation.MULTIPLY_BASE))
            .put(Attributes.ARMOR,new AttributeModifier(UUID.fromString("8a9eb956-70c4-4dc7-a1d4-e02c285beb92"),"Echoing shield armor",1D,AttributeModifier.Operation.ADDITION))
            .put(Attributes.ATTACK_DAMAGE,new AttributeModifier(UUID.fromString("fc79a9a9-bc3e-4213-9678-20a76dea75f1"),"Echoing shield damage",.05D,AttributeModifier.Operation.MULTIPLY_BASE)).build();
    public EchoingAmethystShieldItem(){super(new Properties().stacksTo(1));}
    @Override public InteractionResultHolder<ItemStack> use(Level level,net.minecraft.world.entity.player.Player player,InteractionHand hand){ItemStack stack=player.getItemInHand(hand);if(player instanceof ServerPlayer server)EchoingShieldEntity.launch(server);return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);}
    @Override public Multimap<Attribute,AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot){return slot==EquipmentSlot.OFFHAND?modifiers:super.getDefaultAttributeModifiers(slot);}
    @Override public ItemStack getDefaultInstance(){ItemStack stack=super.getDefaultInstance();prepare(stack);stack.getOrCreateTag().putBoolean(READY_FOIL_KEY,true);return stack;}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){prepare(stack);if(entity instanceof ServerPlayer player){if(player.getPersistentData().getInt(COOLDOWN_KEY)<=0&&!player.getPersistentData().getBoolean(IN_FLIGHT_KEY))stack.getOrCreateTag().putBoolean(READY_FOIL_KEY,true);else stack.getOrCreateTag().remove(READY_FOIL_KEY);}}
    private static void prepare(ItemStack stack){stack.getOrCreateTag().putBoolean("Unbreakable",true);stack.getOrCreateTag().putInt("HideFlags",4);}
    @Override public boolean isFoil(ItemStack stack){return stack.getOrCreateTag().getBoolean(READY_FOIL_KEY);}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.empty());for(int i=1;i<=12;i++){tooltip.add(Component.translatable("item.finalparadox.echoing_amethyst_shield.lore."+i));if(i==3||i==9)tooltip.add(Component.empty());}tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.legendary"));tooltip.add(Component.empty());}
}
