package io.github.finalparadox.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.finalparadox.entity.ConsecrationEntity;
import io.github.finalparadox.entity.PaladinHammerSwingEntity;
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

public final class PaladinHammerItem extends Item {
    public static final String CHARGE_KEY = "finalparadox.paladin_hammer_charge";
    private static final UUID DAMAGE_UUID = UUID.fromString("e49c9df9-56bd-4f86-840d-16582e73ec9d");
    private final Multimap<Attribute, AttributeModifier> mainHandModifiers;

    public PaladinHammerItem() {
        super(new Properties().stacksTo(1));
        mainHandModifiers = ImmutableMultimap.of(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(DAMAGE_UUID, "Paladin Hammer attack damage", 5.0D, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.consume(stack);
        int charge = serverPlayer.getPersistentData().getInt(CHARGE_KEY);
        if (player.isCrouching() && charge >= 16) {
            serverPlayer.getPersistentData().putInt(CHARGE_KEY, 0);
            ConsecrationEntity.spawn(serverPlayer);
        } else if (!player.isCrouching() && !PaladinHammerSwingEntity.hasActive(serverPlayer)) PaladinHammerSwingEntity.spawn(serverPlayer);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, net.minecraft.world.entity.LivingEntity user, ItemStack stack, int remainingTicks) {
        if (!(user instanceof ServerPlayer player)) return;
        if (player.isCrouching() && player.getPersistentData().getInt(CHARGE_KEY) >= 16) {
            player.getPersistentData().putInt(CHARGE_KEY, 0);
            ConsecrationEntity.spawn(player);
        } else if (!player.isCrouching() && !PaladinHammerSwingEntity.hasActive(player)) PaladinHammerSwingEntity.spawn(player);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    public static void recordHit(ServerPlayer player) {
        int charge = Math.min(16, player.getPersistentData().getInt(CHARGE_KEY) + 1);
        player.getPersistentData().putInt(CHARGE_KEY, charge);
        player.displayClientMessage(Component.translatable("message.finalparadox.paladin_hammer.charge", charge), true);
        if (charge == 16) player.level().playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(),
                SoundSource.PLAYERS, 0.5F, 1.5F);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        prepareStack(stack);
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        prepareStack(stack);
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    private static void prepareStack(ItemStack stack) {
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        stack.getOrCreateTag().putInt("HideFlags", 4);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? mainHandModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty());
        for (int line = 1; line <= 5; line++) tooltip.add(Component.translatable("item.finalparadox.paladin_hammer.attack." + line));
        tooltip.add(Component.empty());
        for (int line = 1; line <= 11; line++) tooltip.add(Component.translatable("item.finalparadox.paladin_hammer.lore." + line));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.finalparadox.paladin_hammer.damage"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.finalparadox.unique"));
    }
}
