package io.github.finalparadox.item;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.entity.SummonedBeeEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;

public final class QueenBeeGreatbowItem extends BowItem {
    public QueenBeeGreatbowItem() {
        super(new Properties().durability(384));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int remainingTicks) {
        int charge = getUseDuration(stack) - remainingTicks;
        super.releaseUsing(stack, level, living, remainingTicks);
        if (!(living instanceof ServerPlayer player) || !(level instanceof ServerLevel server)
                || getPowerForTime(charge) < 0.1F || player.getRandom().nextFloat() >= 0.4F) return;
        boolean hasTarget = !server.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(30.0D),
                target -> GlaivorusAbilityState.isHostileTarget(player, target)).isEmpty();
        boolean swarmExists = !server.getEntitiesOfClass(SummonedBeeEntity.class,
                new net.minecraft.world.phys.AABB(-3.0E7D, server.getMinBuildHeight(), -3.0E7D,
                        3.0E7D, server.getMaxBuildHeight(), 3.0E7D)).isEmpty();
        if (!hasTarget || swarmExists) return;
        for (int variant = 0; variant < 3; variant++) SummonedBeeEntity.spawn(server, player, variant);
        server.playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 2.0F);
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
        if (stack.getEnchantmentLevel(Enchantments.POWER_ARROWS) < 2) stack.enchant(Enchantments.POWER_ARROWS, 2);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty());
        for (int line = 1; line <= 4; line++) tooltip.add(Component.translatable("item.finalparadox.queen_bee_greatbow.lore." + line));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.finalparadox.unique"));
    }
}
