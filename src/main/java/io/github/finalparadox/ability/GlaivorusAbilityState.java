package io.github.finalparadox.ability;

import io.github.finalparadox.entity.GlaivorusBladeEntity;
import io.github.finalparadox.registry.ModTags;
import io.github.finalparadox.registry.ModItems;
import io.github.finalparadox.item.GlaivorusItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.UUID;

public final class GlaivorusAbilityState {
    private static final String KEY = "finalparadox.glaivorus";
    private static final String COOLDOWN_KEY = "finalparadox.glaivorus_cooldown";
    private static final int[] STRIKE_TICKS = {0, 40, 70, 100, 130, 160, 190, 220};
    private static final int FINISH_TICK = 270;
    private static final Item[] FUEL_PRIORITY = {Items.STONE_SWORD, Items.GOLDEN_SWORD, Items.IRON_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD};

    private GlaivorusAbilityState() {
    }

    public static boolean tryStart(ServerPlayer player) {
        if (player.getPersistentData().contains(KEY)) return false;
        if (!hasFuel(player)) return false;
        if (player.getRandom().nextBoolean()) consumeFuel(player);

        CompoundTag state = new CompoundTag();
        state.putInt("Elapsed", 0);
        state.putInt("StrikeIndex", 0);
        state.putInt("Hits", 0);
        state.put("SpentTargets", new ListTag());
        player.getPersistentData().put(KEY, state);
        return true;
    }

    public static void tick(ServerPlayer player) {
        tickCooldown(player);
        CompoundTag root = player.getPersistentData();
        if (!root.contains(KEY)) return;
        CompoundTag state = root.getCompound(KEY);
        int elapsed = state.getInt("Elapsed");
        int strikeIndex = state.getInt("StrikeIndex");
        if (strikeIndex < STRIKE_TICKS.length && elapsed == STRIKE_TICKS[strikeIndex]) {
            if (strikeIndex == 0) {
                GlaivorusBladeEntity.spawnInitial(player);
            } else {
                LivingEntity target = findTarget(player, state);
                if (target != null) GlaivorusBladeEntity.spawnForTarget(player, target);
            }
            state.putInt("StrikeIndex", strikeIndex + 1);
        }
        if (elapsed >= FINISH_TICK) {
            int hits = Math.min(state.getInt("Hits"), 25);
            if (hits > 0) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 0));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, hits * 20, 0));
            }
            player.displayClientMessage(Component.translatable("message.finalparadox.glaivorus.finished", state.getInt("Hits")), false);
            player.level().playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW,
                    SoundSource.PLAYERS, 1.0F, 1.5F);
            root.remove(KEY);
            return;
        }
        state.putInt("Elapsed", elapsed + 1);
    }

    public static void startCooldown(ServerPlayer player) {
        player.getPersistentData().putInt(COOLDOWN_KEY, GlaivorusItem.COOLDOWN_TICKS);
        player.getCooldowns().addCooldown(ModItems.GLAIVORUS.get(), GlaivorusItem.COOLDOWN_TICKS);
    }

    public static int cooldownRemaining(ServerPlayer player) {
        return player.getPersistentData().getInt(COOLDOWN_KEY);
    }

    private static void tickCooldown(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int remaining = data.getInt(COOLDOWN_KEY);
        if (remaining <= 0) return;
        boolean sanctuary = player.getTags().contains("finalparadox_sanctuary")
                || player.level().getBiome(player.blockPosition()).is(ModTags.Biomes.SANCTUARY);
        remaining -= sanctuary ? 12 : 1;
        if (remaining <= 0) {
            data.remove(COOLDOWN_KEY);
            player.getCooldowns().removeCooldown(ModItems.GLAIVORUS.get());
        } else {
            data.putInt(COOLDOWN_KEY, remaining);
            // Restore the visual cooldown after reconnecting without restarting its client animation every tick.
            if (!player.getCooldowns().isOnCooldown(ModItems.GLAIVORUS.get())) {
                player.getCooldowns().addCooldown(ModItems.GLAIVORUS.get(), remaining);
            }
        }
    }

    public static void recordHit(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        if (root.contains(KEY)) root.getCompound(KEY).putInt("Hits", root.getCompound(KEY).getInt("Hits") + 1);
    }

    private static LivingEntity findTarget(ServerPlayer player, CompoundTag state) {
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(20.0D),
                target -> isValidTarget(player, target));
        ListTag spent = state.getList("SpentTargets", 10);
        List<LivingEntity> fresh = targets.stream().filter(target -> !wasSpent(spent, target.getUUID())).toList();
        List<LivingEntity> choices = fresh.isEmpty() ? targets : fresh;
        if (choices.isEmpty()) return null;
        LivingEntity target = choices.get(player.getRandom().nextInt(choices.size()));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 4), player);
        CompoundTag entry = new CompoundTag();
        entry.putUUID("Id", target.getUUID());
        spent.add(entry);
        return target;
    }

    public static boolean isValidTarget(ServerPlayer player, LivingEntity target) {
        return isHostileTarget(player, target) && target.distanceToSqr(player) <= 400.0D;
    }

    public static boolean isHostileTarget(ServerPlayer player, LivingEntity target) {
        EntityType<?> type = target.getType();
        boolean hostile = type.getCategory() == MobCategory.MONSTER || type.is(ModTags.EntityTypes.GLAIVORUS_TARGETS);
        return target != player && target.isAlive() && hostile && !target.isInvulnerable()
                && type != EntityType.PHANTOM && type != EntityType.GHAST;
    }

    private static boolean wasSpent(ListTag spent, UUID id) {
        for (int i = 0; i < spent.size(); i++) if (spent.getCompound(i).hasUUID("Id") && spent.getCompound(i).getUUID("Id").equals(id)) return true;
        return false;
    }

    private static boolean hasFuel(ServerPlayer player) {
        return player.getInventory().items.stream().anyMatch(stack -> isFuel(stack.getItem()));
    }

    private static void consumeFuel(ServerPlayer player) {
        for (Item item : FUEL_PRIORITY) {
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.is(item)) continue;
                ItemStack eaten = stack.copyWithCount(1);
                stack.shrink(1);
                ServerLevel level = player.serverLevel();
                level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, eaten), player.getX(),
                        player.getY() + 1.6D, player.getZ(), 35, 0, 0, 0, 0.1D);
                level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EAT,
                        SoundSource.PLAYERS, 1.0F, 0.7F);
                return;
            }
        }
    }

    private static boolean isFuel(Item item) {
        for (Item fuel : FUEL_PRIORITY) if (item == fuel) return true;
        return false;
    }
}
