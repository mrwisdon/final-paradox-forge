package io.github.finalparadox.ability;

import io.github.finalparadox.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class ParadoxicalTomeState {
    private static final String STATE_KEY = "finalparadox.paradoxical_tome";
    private static final String COOLDOWN_KEY = "finalparadox.paradoxical_tome_cooldown";
    private static final String DIMENSION = "Dimension";
    private static final String X = "X";
    private static final String Y = "Y";
    private static final String Z = "Z";
    private static final String INITIAL_HEALTH = "InitialHealth";
    private static final String REMAINING = "Remaining";
    private static final String DOUBLE_TAP_WINDOW = "DoubleTapWindow";
    private static final String WAS_SNEAKING = "WasSneaking";
    private static final DustParticleOptions PARADOX_DUST =
            new DustParticleOptions(new Vector3f(0.882F, 0.0F, 1.0F), 0.9F);

    private ParadoxicalTomeState() {
    }

    public static boolean activate(ServerPlayer player) {
        int cooldown = cooldownRemaining(player);
        if (cooldown > 0) {
            int seconds = (cooldown + 19) / 20;
            player.displayClientMessage(Component.translatable(
                    "message.finalparadox.paradoxical_tome.cooldown", seconds).withStyle(ChatFormatting.RED), true);
            player.level().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                    SoundSource.PLAYERS, 1.0F, 2.0F);
            return false;
        }

        CompoundTag state = new CompoundTag();
        state.putString(DIMENSION, player.level().dimension().location().toString());
        state.putDouble(X, player.getX());
        state.putDouble(Y, player.getY());
        state.putDouble(Z, player.getZ());
        state.putFloat(INITIAL_HEALTH, player.getHealth());
        state.putInt(REMAINING, ParadoxicalTomeRules.TIMELINE_TICKS);
        state.putInt(DOUBLE_TAP_WINDOW, 0);
        // A held key at the moment of activation is not a new press edge.
        state.putBoolean(WAS_SNEAKING, player.isShiftKeyDown());
        player.getPersistentData().put(STATE_KEY, state);
        player.getPersistentData().putInt(COOLDOWN_KEY, ParadoxicalTomeRules.COOLDOWN_TICKS);
        player.getCooldowns().addCooldown(ModItems.PARADOXICAL_TOME.get(), ParadoxicalTomeRules.COOLDOWN_TICKS);
        activationEffects(player);
        return true;
    }

    public static void tick(ServerPlayer player) {
        tickCooldown(player);
        CompoundTag root = player.getPersistentData();
        if (!root.contains(STATE_KEY, CompoundTag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag state = root.getCompound(STATE_KEY);
        if (!player.isAlive() || !ParadoxicalTomeRules.sameDimension(
                state.getString(DIMENSION), player.level().dimension().location().toString())) {
            clearTimeline(player, false);
            return;
        }

        ParadoxicalTomeRules.SneakStep sneak = ParadoxicalTomeRules.tickSneak(
                state.getInt(DOUBLE_TAP_WINDOW), state.getBoolean(WAS_SNEAKING), player.isShiftKeyDown());
        state.putInt(DOUBLE_TAP_WINDOW, sneak.windowTicks());
        state.putBoolean(WAS_SNEAKING, sneak.wasSneaking());
        if (sneak.triggered()) {
            regress(player, state);
            return;
        }

        int remaining = state.getInt(REMAINING);
        showFinalCountdown(player, remaining);
        timelineEffects(player, state, remaining);
        remaining = ParadoxicalTomeRules.tickTimeline(remaining);
        if (!ParadoxicalTomeRules.timelineActive(remaining)) {
            clearTimeline(player, true);
            return;
        }
        state.putInt(REMAINING, remaining);
    }

    public static void cancelTimeline(ServerPlayer player) {
        clearTimeline(player, false);
    }

    public static void copyCooldownToClone(ServerPlayer original, ServerPlayer replacement) {
        int remaining = cooldownRemaining(original);
        clearTimeline(replacement, false);
        if (remaining > 0) {
            replacement.getPersistentData().putInt(COOLDOWN_KEY, remaining);
            replacement.getCooldowns().addCooldown(ModItems.PARADOXICAL_TOME.get(), remaining);
        }
    }

    public static int cooldownRemaining(ServerPlayer player) {
        return Math.max(0, player.getPersistentData().getInt(COOLDOWN_KEY));
    }

    private static void tickCooldown(ServerPlayer player) {
        int remaining = cooldownRemaining(player);
        if (remaining <= 0) {
            return;
        }
        remaining--;
        if (remaining <= 0) {
            player.getPersistentData().remove(COOLDOWN_KEY);
            player.getCooldowns().removeCooldown(ModItems.PARADOXICAL_TOME.get());
            player.displayClientMessage(Component.translatable(
                    "message.finalparadox.paradoxical_tome.ready").withStyle(ChatFormatting.AQUA), true);
            player.level().playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.PLAYERS, 0.5F, 1.5F);
            return;
        }
        player.getPersistentData().putInt(COOLDOWN_KEY, remaining);
        // Reconnects and respawns lose the vanilla UI entry; restore it once, not every tick.
        if (!player.getCooldowns().isOnCooldown(ModItems.PARADOXICAL_TOME.get())) {
            player.getCooldowns().addCooldown(ModItems.PARADOXICAL_TOME.get(), remaining);
        }
    }

    private static void activationEffects(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.8F);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0F, 2.0F);
        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.5F, 2.0F);
        level.sendParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 1.0D, player.getZ(),
                25, 0.0D, 0.0D, 0.0D, 1.0D);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1));
        for (int index = 0; index < 32; index++) {
            double angle = index * Math.PI * 2.0D / 32.0D;
            level.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(),
                    0, Math.cos(angle), 0.0D, Math.sin(angle), 0.3D);
        }
    }

    private static void timelineEffects(ServerPlayer player, CompoundTag state, int remaining) {
        if (remaining % 10 != 0) {
            return;
        }
        ServerLevel level = player.serverLevel();
        double x = state.getDouble(X);
        double y = state.getDouble(Y);
        double z = state.getDouble(Z);
        level.sendParticles(ParticleTypes.PORTAL, x, y + 1.5D, z, 6, 0.3D, 0.6D, 0.3D, 0.15D);
        for (int index = 0; index < 12; index++) {
            double angle = index * Math.PI * 2.0D / 12.0D;
            level.sendParticles(PARADOX_DUST, x + Math.cos(angle) * 1.5D,
                    y + 1.4D + Math.sin(angle) * 1.5D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (remaining % 20 == 0) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 1.0D, z, 1,
                    0.6D, 0.6D, 0.6D, 0.0D);
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_AMBIENT,
                    SoundSource.PLAYERS, 0.2F, 0.8F);
        }
    }

    private static void regress(ServerPlayer player, CompoundTag state) {
        ServerLevel level = player.serverLevel();
        Vec3 from = player.position();
        Vec3 destination = new Vec3(state.getDouble(X), state.getDouble(Y), state.getDouble(Z));
        burst(level, from);
        teleportTrail(level, from, destination);

        float yaw = player.getYRot();
        float pitch = player.getXRot();
        player.connection.teleport(destination.x, destination.y, destination.z, yaw, pitch);
        player.fallDistance = 0.0F;
        burst(level, destination);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT,
                SoundSource.PLAYERS, 1.0F, 1.8F);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 99, false, false));

        float currentHealth = player.getHealth();
        ParadoxicalTomeRules.HealingPlan healing =
                ParadoxicalTomeRules.healingPlan(state.getFloat(INITIAL_HEALTH), currentHealth);
        float restored = ParadoxicalTomeRules.restoredHealth(
                state.getFloat(INITIAL_HEALTH), currentHealth, player.getMaxHealth());
        if (restored > currentHealth) {
            player.setHealth(restored);
        }
        if (healing.regeneration()) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 2));
        }
        clearTimeline(player, false);
        level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void burst(ServerLevel level, Vec3 position) {
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, position.x, position.y + 1.2D, position.z,
                25, 0.0D, 0.0D, 0.0D, 0.25D);
        level.sendParticles(ParticleTypes.EXPLOSION, position.x, position.y + 1.2D, position.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static void teleportTrail(ServerLevel level, Vec3 from, Vec3 destination) {
        double distance = from.distanceTo(destination);
        int points = Math.min(48, Math.max(1, (int) Math.ceil(distance)));
        for (int point = 1; point < points; point++) {
            Vec3 position = from.lerp(destination, point / (double) points);
            level.sendParticles(PARADOX_DUST, position.x, position.y + 1.0D, position.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, position.x, position.y + 1.0D, position.z,
                    1, 0.0D, 0.01D, 0.0D, 0.0D);
        }
    }

    private static void showFinalCountdown(ServerPlayer player, int remaining) {
        if (remaining > 100 || remaining <= 0 || remaining % 20 != 0) {
            return;
        }
        int seconds = remaining / 20;
        player.displayClientMessage(Component.translatable(
                "message.finalparadox.paradoxical_tome.countdown." + seconds), true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(),
                SoundSource.PLAYERS, 1.0F, 2.0F);
    }

    private static void clearTimeline(ServerPlayer player, boolean expired) {
        boolean hadTimeline = player.getPersistentData().contains(STATE_KEY, CompoundTag.TAG_COMPOUND);
        player.getPersistentData().remove(STATE_KEY);
        if (!expired || !hadTimeline) {
            return;
        }
        player.displayClientMessage(Component.translatable(
                "message.finalparadox.paradoxical_tome.expired").withStyle(ChatFormatting.RED), true);
        player.serverLevel().sendParticles(ParticleTypes.LARGE_SMOKE,
                player.getX(), player.getY() + 1.0D, player.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.1D);
        player.level().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                SoundSource.PLAYERS, 1.0F, 2.0F);
        player.level().playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
