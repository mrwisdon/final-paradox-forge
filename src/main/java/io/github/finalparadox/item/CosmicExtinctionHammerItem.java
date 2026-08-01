package io.github.finalparadox.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.finalparadox.entity.CosmicHammerSwingEntity;
import io.github.finalparadox.entity.VoidSingularityEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.UUID;

public final class CosmicExtinctionHammerItem extends Item {
    public static final String CHARGE_KEY = "finalparadox.cosmic_hammer_charge";
    private final Multimap<Attribute, AttributeModifier> modifiers = ImmutableMultimap.of(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(UUID.fromString("e3a4c90f-20d7-4f6e-8d42-a117c27e1244"), "Cosmic extinction hammer damage", 5.0D, AttributeModifier.Operation.ADDITION));
    public CosmicExtinctionHammerItem() { super(new Properties().stacksTo(1)); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand); player.startUsingItem(hand);
        if (player instanceof ServerPlayer server) activate(server, player.isCrouching());
        return InteractionResultHolder.consume(stack);
    }
    @Override public void onUseTick(Level level, net.minecraft.world.entity.LivingEntity user, ItemStack stack, int remainingTicks) {
        if (!(user instanceof ServerPlayer player)) return;
        if (player.isCrouching() && player.getPersistentData().getInt(CHARGE_KEY) >= 16
                && level.getEntitiesOfClass(VoidSingularityEntity.class, player.getBoundingBox().inflate(16), entity -> entity.isOwnedBy(player)).isEmpty()) {
            player.getPersistentData().putInt(CHARGE_KEY, 0);
            VoidSingularityEntity.spawn(player);
        } else if (!player.isCrouching() && !CosmicHammerSwingEntity.hasActive(player)) {
            CosmicHammerSwingEntity.spawn(player);
        }
    }
    private static void activate(ServerPlayer player, boolean crouching) {
        if (crouching && player.getPersistentData().getInt(CHARGE_KEY) >= 16) {
            player.getPersistentData().putInt(CHARGE_KEY, 0);
            VoidSingularityEntity.spawn(player);
        } else if (!crouching && !CosmicHammerSwingEntity.hasActive(player)) {
            CosmicHammerSwingEntity.spawn(player);
        }
    }
    @Override public int getUseDuration(ItemStack stack) { return 72000; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }
    public static void recordHit(ServerPlayer player) { int charge=Math.min(16,player.getPersistentData().getInt(CHARGE_KEY)+1); player.getPersistentData().putInt(CHARGE_KEY,charge); player.displayClientMessage(Component.translatable("message.finalparadox.cosmic_extinction_hammer.charge",charge),true); if(charge==16)player.level().playSound(null,player.blockPosition(),SoundEvents.UI_BUTTON_CLICK.value(),SoundSource.PLAYERS,0.5F,1.5F); }
    @Override public ItemStack getDefaultInstance(){ItemStack stack=super.getDefaultInstance();prepare(stack);return stack;}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){prepare(stack);}
    private static void prepare(ItemStack stack){stack.getOrCreateTag().putBoolean("Unbreakable",true);stack.getOrCreateTag().putInt("HideFlags",4);}
    @Override public Multimap<Attribute,AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot){return slot==EquipmentSlot.MAINHAND?modifiers:super.getDefaultAttributeModifiers(slot);}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.empty());for(int i=1;i<=9;i++)tooltip.add(Component.translatable("item.finalparadox.cosmic_extinction_hammer.lore."+i));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.cosmic_extinction_hammer.damage"));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.unique"));}
}
