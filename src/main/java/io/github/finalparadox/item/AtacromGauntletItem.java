package io.github.finalparadox.item;

import io.github.finalparadox.entity.AtacromGauntletEntity;
import io.github.finalparadox.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Server-authoritative recreation of Final Paradox's Gauntlet of Atacrom.
 *
 * <p>The original datapack keeps the parry open for 20 ticks, then counts a
 * separate 20-tick recovery. A successful red parry restores the open window
 * to at least 10 ticks and clears the recovery immediately.</p>
 */
public final class AtacromGauntletItem extends Item {
    public static final int GUARD_TICKS = 20;
    public static final int RECOVERY_TICKS = 20;
    public static final int PARRY_REFRESH_TICKS = 10;

    public static final String GUARD_TICKS_KEY = "finalparadox.atacrom_gauntlet_guard_ticks";
    public static final String RECOVERY_TICKS_KEY = "finalparadox.atacrom_gauntlet_recovery_ticks";
    private static final String COOLDOWN_CLEARED_KEY =
            "finalparadox.atacrom_gauntlet_cooldown_cleared_by_parry";
    private static final String ANCHOR_X_KEY = "finalparadox.atacrom_gauntlet_anchor_x";
    private static final String ANCHOR_Y_KEY = "finalparadox.atacrom_gauntlet_anchor_y";
    private static final String ANCHOR_Z_KEY = "finalparadox.atacrom_gauntlet_anchor_z";
    private static final String ANCHOR_DIMENSION_KEY = "finalparadox.atacrom_gauntlet_anchor_dimension";

    private static final double DEFAULT_GUARD_HALF_ANGLE = 70.0D;

    public AtacromGauntletItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player,
                                                   InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (hand != InteractionHand.OFF_HAND || !serverPlayer.getOffhandItem().is(this)) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.finalparadox.atacrom_gauntlet.offhand"), true);
            return InteractionResultHolder.fail(stack);
        }

        int guardTicks = guardTicks(serverPlayer);
        if (guardTicks >= PARRY_REFRESH_TICKS) {
            return InteractionResultHolder.consume(stack);
        }
        int remaining = totalCooldownTicks(serverPlayer);
        if (remaining > 0) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.finalparadox.atacrom_gauntlet.cooldown",
                            Math.max(1, (remaining + 19) / 20)), true);
            serverPlayer.level().playSound(null, serverPlayer.blockPosition(),
                    SoundEvents.NOTE_BLOCK_PLING.get(), SoundSource.PLAYERS, 1.0F, 2.0F);
            return InteractionResultHolder.fail(stack);
        }

        beginGuard(serverPlayer);
        return InteractionResultHolder.consume(stack);
    }

    private static void beginGuard(ServerPlayer player) {
        player.getPersistentData().putInt(GUARD_TICKS_KEY, GUARD_TICKS);
        player.getPersistentData().putInt(RECOVERY_TICKS_KEY, RECOVERY_TICKS);
        player.getPersistentData().remove(COOLDOWN_CLEARED_KEY);
        player.getPersistentData().putDouble(ANCHOR_X_KEY, player.getX());
        player.getPersistentData().putDouble(ANCHOR_Y_KEY, player.getY());
        player.getPersistentData().putDouble(ANCHOR_Z_KEY, player.getZ());
        player.getPersistentData().putString(ANCHOR_DIMENSION_KEY,
                player.level().dimension().location().toString());
        player.getCooldowns().addCooldown(ModItems.ATACROM_GAUNTLET.get(), GUARD_TICKS + RECOVERY_TICKS);
        player.setDeltaMovement(Vec3.ZERO);
        AtacromGauntletEntity.ensure(player);

        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.get(),
                SoundSource.PLAYERS, 1.0F, 2.0F);
        level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON,
                SoundSource.PLAYERS, 1.0F, 1.2F);
        Vec3 center = player.getEyePosition().add(player.getLookAngle().scale(1.0D));
        level.sendParticles(ParticleTypes.SOUL, center.x, center.y, center.z,
                2, 0.2D, 0.2D, 0.2D, 0.0D);
    }

    public static void tick(ServerPlayer player) {
        int guardTicks = guardTicks(player);
        int recoveryTicks = recoveryTicks(player);
        if (guardTicks <= 0 && recoveryTicks <= 0) {
            clearState(player);
            return;
        }

        if (!player.getOffhandItem().is(ModItems.ATACROM_GAUNTLET.get())) {
            if (guardTicks > 0) {
                player.getPersistentData().remove(GUARD_TICKS_KEY);
            }
            if (recoveryTicks > 0 && !player.getCooldowns().isOnCooldown(ModItems.ATACROM_GAUNTLET.get())) {
                player.getCooldowns().addCooldown(ModItems.ATACROM_GAUNTLET.get(), recoveryTicks);
            }
            return;
        }

        int expectedCooldown = player.getPersistentData().getBoolean(COOLDOWN_CLEARED_KEY)
                ? 0 : guardTicks + recoveryTicks;
        if (expectedCooldown > 0 && !player.getCooldowns().isOnCooldown(ModItems.ATACROM_GAUNTLET.get())) {
            player.getCooldowns().addCooldown(ModItems.ATACROM_GAUNTLET.get(), expectedCooldown);
        }

        if (guardTicks > 0) {
            if (!lockToAnchor(player)) {
                return;
            }
            AtacromGauntletEntity.ensure(player);
            guardTicks--;
            if (guardTicks > 0) {
                player.getPersistentData().putInt(GUARD_TICKS_KEY, guardTicks);
            } else {
                player.getPersistentData().remove(GUARD_TICKS_KEY);
            }
            return;
        }

        recoveryTicks--;
        if (recoveryTicks > 0) {
            player.getPersistentData().putInt(RECOVERY_TICKS_KEY, recoveryTicks);
        } else {
            clearState(player);
        }
    }

    /**
     * Called by a blockable red MarawThar attack before it applies damage.
     *
     * @return true when the attack was in the active frontal guard and must be cancelled
     */
    public static boolean tryParryRedAttack(ServerPlayer player, Vec3 attackOrigin) {
        return tryParryRedAttack(player, attackOrigin, DEFAULT_GUARD_HALF_ANGLE);
    }

    public static boolean tryParryRedAttack(
            ServerPlayer player,
            Vec3 attackOrigin,
            double guardHalfAngle
    ) {
        if (!isGuarding(player) || attackOrigin == null
                || !isInFront(player, attackOrigin, guardHalfAngle)) {
            return false;
        }
        successfulParry(player);
        return true;
    }

    public static boolean isGuarding(ServerPlayer player) {
        return player.getOffhandItem().is(ModItems.ATACROM_GAUNTLET.get()) && guardTicks(player) > 0;
    }

    private static boolean isInFront(
            ServerPlayer player,
            Vec3 attackOrigin,
            double guardHalfAngle
    ) {
        Vec3 look = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        Vec3 toward = attackOrigin.subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        if (look.lengthSqr() < 1.0E-4D || toward.lengthSqr() < 1.0E-4D) {
            return true;
        }
        double clampedHalfAngle = Math.max(0.0D, Math.min(180.0D, guardHalfAngle));
        double minimumDot = Math.cos(Math.toRadians(clampedHalfAngle));
        return look.normalize().dot(toward.normalize()) >= minimumDot - 1.0E-6D;
    }

    private static void successfulParry(ServerPlayer player) {
        int refreshed = Math.max(PARRY_REFRESH_TICKS, guardTicks(player));
        player.getPersistentData().putInt(GUARD_TICKS_KEY, refreshed);
        player.getPersistentData().remove(RECOVERY_TICKS_KEY);
        player.getPersistentData().putBoolean(COOLDOWN_CLEARED_KEY, true);
        player.getCooldowns().removeCooldown(ModItems.ATACROM_GAUNTLET.get());
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 6, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 0, true, false));

        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 center = player.getEyePosition().add(look.scale(1.0D));
        Vec3 right = new Vec3(look.z, 0.0D, -look.x).normalize();
        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
        for (int i = 0; i < 24; i++) {
            double angle = i * Math.PI * 2.0D / 24.0D;
            Vec3 point = center.add(right.scale(Math.cos(angle) * 1.25D))
                    .add(up.scale(Math.sin(angle) * 1.25D));
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.sendParticles(ParticleTypes.FIREWORK, center.x, center.y, center.z,
                5, 0.0D, 0.0D, 0.0D, 1.0D);
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.get(),
                SoundSource.PLAYERS, 0.5F, 2.0F);
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.get(),
                SoundSource.PLAYERS, 0.5F, 2.0F);
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_XYLOPHONE.get(),
                SoundSource.PLAYERS, 0.5F, 2.0F);
        level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 0.5F, 2.0F);
    }

    private static boolean lockToAnchor(ServerPlayer player) {
        String dimension = player.getPersistentData().getString(ANCHOR_DIMENSION_KEY);
        if (!dimension.equals(player.level().dimension().location().toString())) {
            clearState(player);
            player.getCooldowns().removeCooldown(ModItems.ATACROM_GAUNTLET.get());
            return false;
        }
        double x = player.getPersistentData().getDouble(ANCHOR_X_KEY);
        double y = player.getPersistentData().getDouble(ANCHOR_Y_KEY);
        double z = player.getPersistentData().getDouble(ANCHOR_Z_KEY);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        if (player.distanceToSqr(x, y, z) > 0.01D) {
            player.connection.teleport(x, y, z, player.getYRot(), player.getXRot());
        }
        return true;
    }

    private static int guardTicks(ServerPlayer player) {
        return Math.max(0, player.getPersistentData().getInt(GUARD_TICKS_KEY));
    }

    private static int recoveryTicks(ServerPlayer player) {
        return Math.max(0, player.getPersistentData().getInt(RECOVERY_TICKS_KEY));
    }

    private static int totalCooldownTicks(ServerPlayer player) {
        return player.getPersistentData().getBoolean(COOLDOWN_CLEARED_KEY)
                ? 0 : guardTicks(player) + recoveryTicks(player);
    }

    private static void clearState(ServerPlayer player) {
        player.getPersistentData().remove(GUARD_TICKS_KEY);
        player.getPersistentData().remove(RECOVERY_TICKS_KEY);
        player.getPersistentData().remove(COOLDOWN_CLEARED_KEY);
        player.getPersistentData().remove(ANCHOR_X_KEY);
        player.getPersistentData().remove(ANCHOR_Y_KEY);
        player.getPersistentData().remove(ANCHOR_Z_KEY);
        player.getPersistentData().remove(ANCHOR_DIMENSION_KEY);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        prepare(stack);
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        prepare(stack);
    }

    private static void prepare(ItemStack stack) {
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        stack.getOrCreateTag().putInt("HideFlags", 4);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty());
        for (int i = 1; i <= 9; i++) {
            tooltip.add(Component.translatable("item.finalparadox.atacrom_gauntlet.lore." + i));
            if (i == 3 || i == 5 || i == 8) {
                tooltip.add(Component.empty());
            }
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.finalparadox.mythic"));
        tooltip.add(Component.empty());
    }
}
