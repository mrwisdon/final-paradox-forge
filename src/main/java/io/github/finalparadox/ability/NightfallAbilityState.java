package io.github.finalparadox.ability;

import io.github.finalparadox.entity.NightfallLaserEntity;
import io.github.finalparadox.entity.NightfallChainBladeEntity;
import io.github.finalparadox.registry.ModItems;
import io.github.finalparadox.registry.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Server-authoritative player adaptation of MarawThar's B9 sword attacks.
 *
 * <p>Timings, local-coordinate particle frames, movement increments and
 * collision samples are traced from the original mcfunction call chains. The
 * source reward item itself is deliberately powerless; this class restores
 * selected boss attacks as an explicit mod extension.</p>
 */
public final class NightfallAbilityState {
    public enum AbilityMode {
        COMBO,
        RIFT,
        LASER,
        SMALL_LASER,
        CHAIN_BLADE
    }

    public static final String COMBO_ANIMATION_SCORE_TAG = "finalparadox.nightfall_combo_score";
    public static final String COMBO_ANIMATION_YAW_TAG = "finalparadox.nightfall_combo_yaw";

    private static final String ACTIVE_KEY = "finalparadox.nightfall_active";
    private static final String COOLDOWN_KEY = "finalparadox.nightfall_cooldown";
    private static final String MODE_COMBO = "combo";
    private static final String MODE_RIFT = "rift";
    private static final String MODE_LASER = "laser";
    private static final String MODE_SMALL_LASER = "small_laser";
    private static final String MODE_CHAIN_BLADE = "chain_blade";
    private static final String PRIOR_RESISTANCE_KEY = "PriorResistance";
    private static final int NIGHTFALL_RESISTANCE_AMPLIFIER = 4;
    private static final int NIGHTFALL_RESISTANCE_REFRESH_TICKS = 6;
    private static final double TARGET_LOCK_RANGE = 30.0D;

    private static final int[] COMBO_MOVE_SCORES = {8, 18, 26};
    private static final int COMBO_END_SCORE = 38;
    private static final int COMBO_COOLDOWN = 80;

    private static final int RIFT_WARNING_SCORE = 2;
    private static final int RIFT_RELEASE_SCORE = 23;
    private static final int RIFT_CASTER_STEP_SCORE = 25;
    private static final int RIFT_TRAVEL_TICKS = 15;
    private static final int RIFT_END_SCORE = RIFT_RELEASE_SCORE + RIFT_TRAVEL_TICKS + 1;
    private static final int RIFT_COOLDOWN = 120;
    private static final double RIFT_SUBSTEP = 0.35D;
    private static final int RIFT_SUBSTEPS_PER_TICK = 6;
    private static final double RIFT_SPEED = RIFT_SUBSTEP * RIFT_SUBSTEPS_PER_TICK;

    private static final int LASER_LOCK_SCORE = 50;
    private static final int LASER_SECOND_WARNING_SCORE = LASER_LOCK_SCORE + 12;
    private static final int LASER_RELEASE_SCORE = LASER_SECOND_WARNING_SCORE + 12;
    private static final int LASER_DURATION = 80;
    private static final int LASER_END_SCORE = LASER_RELEASE_SCORE + LASER_DURATION - 1;
    private static final int LASER_COOLDOWN = 240;
    private static final int LASER_SEGMENTS = 38;
    private static final double LASER_SEGMENT_SPACING = 0.6D;
    private static final double LASER_ROTATION_PER_TICK = 2.2D;

    private static final int[] SMALL_LASER_FIRE_SCORES = {9, 11, 13, 15, 17, 19};
    /** Source choreography starts h2/combo1 twice, at 1.75s and 3.5s. */
    private static final int[] SMALL_LASER_BLUE_VOLLEY_STARTS = {35, 70};
    /**
     * Source choreography starts h2/combo3 at tick 160, after two omitted
     * attacks at ticks 105 and 125. The item timeline reuses the first omitted
     * slot so the retained orbit follows the second blue volley continuously.
     */
    private static final int SMALL_LASER_ORBIT_START_SCORE = 105;
    private static final int[] SMALL_LASER_ORBIT_WARNING_SCORES = {1, 6, 9, 12, 15, 18, 21, 24};
    private static final int SMALL_LASER_ORBIT_FIRST_FIRE_SCORE = 43;
    private static final int SMALL_LASER_ORBIT_FIRE_INTERVAL = 3;
    private static final double SMALL_LASER_ORBIT_RADIUS = 8.0D;
    private static final int SMALL_LASER_ORBIT_CASTER_HOLD_END_SCORE = 40;
    private static final double SMALL_LASER_ORBIT_CASTER_INSET = 1.0D;
    private static final double SMALL_LASER_ORBIT_CASTER_SIDE_OFFSET = -0.3D;
    private static final String SMALL_LASER_ORBIT_CASTER_POSITION = "SmallLaserOrbitCasterPosition";
    private static final int SMALL_LASER_INITIAL_SEQUENCE_END_SCORE = 37;
    /** The last orbit shot reaches source projectile age 20 at score 187. */
    private static final int SMALL_LASER_END_SCORE = 188;
    private static final int SMALL_LASER_COOLDOWN = 160;
    private static final int KILL_COOLDOWN_REDUCTION = 20;
    private static final int CHAIN_BLADE_COOLDOWN = 240;
    private static final int CHAIN_BLADE_PULL_SCORE = 60;
    private static final int CHAIN_BLADE_IMPACT_SCORE = 70;
    private static final int CHAIN_BLADE_END_SCORE = 85;
    private static final double CHAIN_BLADE_PULL_RADIUS = 20.0D;
    private static final double CHAIN_BLADE_DROP_HEIGHT = 10.0D;
    private static final float CHAIN_BLADE_DAMAGE = 96.0F;
    private static final int HIT_SATURATION_DURATION = 40;
    private static final int HIT_SATURATION_AMPLIFIER = 0;
    private static final int SMALL_LASER_BLUE_INITIAL_AGE = -3;
    private static final int SMALL_LASER_INITIAL_AGE = 2;
    private static final int SMALL_LASER_BACKSTEP_AGE = 6;
    private static final int SMALL_LASER_MOVE_START_AGE = 9;
    private static final int SMALL_LASER_MOVE_END_AGE = 20;
    private static final int SMALL_LASER_PARTS = 3;
    private static final double SMALL_LASER_PART_SPACING = 0.35D;
    private static final double SMALL_LASER_BACKSTEP = 0.2D;
    private static final int SMALL_LASER_SUBSTEPS = 4;
    private static final double SMALL_LASER_SUBSTEP = 1.0D;
    private static final float SMALL_LASER_DAMAGE = 48.0F;
    private static final String SMALL_LASER_RED_TEAM = "fp_b9_red";
    private static final String SMALL_LASER_BLUE_TEAM = "fp_b9_aqua";
    private static final Vec3[] SMALL_LASER_ORBIT_OFFSETS = {
            new Vec3(0.0D, 0.0D, SMALL_LASER_ORBIT_RADIUS),
            new Vec3(-5.65685424949238D, 0.0D, 5.65685424949238D),
            new Vec3(-SMALL_LASER_ORBIT_RADIUS, 0.0D, 0.0D),
            new Vec3(-5.65685424949238D, 0.0D, -5.65685424949238D),
            new Vec3(0.0D, 0.0D, -SMALL_LASER_ORBIT_RADIUS),
            new Vec3(5.65685424949238D, 0.0D, -5.65685424949238D),
            new Vec3(SMALL_LASER_ORBIT_RADIUS, 0.0D, 0.0D),
            new Vec3(5.65685424949238D, 0.0D, 5.65685424949238D)
    };

    private static final float SWORD_DAMAGE = 48.0F;
    private static final float LASER_DAMAGE = 96.0F;

    private static final DustParticleOptions[] NORMAL_SLASH_DUST = {
            dust(0.0F, 0.0F, 0.0F, 0.20F),
            dust(0.0F, 0.0F, 0.0F, 0.25F),
            dust(0.0F, 0.0F, 0.0F, 0.30F),
            dust(0.0F, 0.0F, 0.0F, 0.35F),
            dust(0.0F, 0.0F, 0.0F, 0.40F),
            dust(0.0F, 0.0F, 0.0F, 0.45F),
            dust(0.173F, 0.129F, 0.129F, 0.50F),
            dust(0.267F, 0.169F, 0.169F, 0.55F),
            dust(0.478F, 0.188F, 0.188F, 0.60F),
            dust(0.694F, 0.157F, 0.157F, 0.65F),
            dust(1.0F, 0.0F, 0.0F, 0.70F)
    };
    private static final double[] NORMAL_SLASH_DISTANCES =
            {0.9D, 1.1D, 1.3D, 1.5D, 1.7D, 1.9D, 2.1D, 2.3D, 2.5D, 2.7D, 2.9D};
    private static final DustParticleOptions RIFT_BLUE =
            dust(0.0F, 0.882F, 1.0F, 0.70F);
    private static final DustParticleOptions LASER_BLUE =
            dust(0.0F, 0.851F, 1.0F, 1.70F);
    private static final DustParticleOptions WARNING_RED =
            dust(1.0F, 0.0F, 0.0F, 2.0F);

    private static final List<Vec3> RIFT_WARNING_POINTS = buildRiftWarningPoints();
    private static final double[][] RIFT_PARTICLE_ARC = {
            {-2.26576946759163D, 1.05654565435175D},
            {-1.76776695296637D, 1.76776695296637D},
            {-1.05654565435175D, 2.26576946759163D},
            {-0.217889356869146D, 2.49048674522936D},
            {0.647047612756301D, 2.41481456572267D},
            {1.43394109087762D, 2.04788011072248D},
            {2.04788011072248D, 1.43394109087762D},
            {2.41481456572267D, 0.647047612756302D}
    };

    private NightfallAbilityState() {
    }

    public static boolean tryStart(ServerPlayer player, AbilityMode requestedMode, InteractionHand hand) {
        CompoundTag data = player.getPersistentData();
        if (data.contains(ACTIVE_KEY)) {
            player.displayClientMessage(Component.translatable("message.finalparadox.nightfall.busy"), true);
            return false;
        }
        int cooldown = data.getInt(COOLDOWN_KEY);
        if (cooldown > 0) {
            clearComboAnimation(player);
            player.displayClientMessage(
                    Component.translatable("message.finalparadox.nightfall.cooldown", (cooldown + 19) / 20), true);
            return false;
        }
        clearComboAnimation(player);

        CompoundTag state = new CompoundTag();
        String mode = switch (requestedMode) {
            case COMBO -> MODE_COMBO;
            case RIFT -> MODE_RIFT;
            case LASER -> MODE_LASER;
            case SMALL_LASER -> MODE_SMALL_LASER;
            case CHAIN_BLADE -> MODE_CHAIN_BLADE;
        };
        Vec3 chainBladeGround = MODE_CHAIN_BLADE.equals(mode) ? findCurrentFloor(player) : null;
        if (MODE_CHAIN_BLADE.equals(mode) && chainBladeGround == null) {
            player.displayClientMessage(
                    Component.translatable("message.finalparadox.nightfall.chain_blade_no_floor"), true);
            return false;
        }
        state.putString("Mode", mode);
        state.putString("Dimension", player.level().dimension().location().toString());
        state.putInt("Score", 0);
        state.putBoolean("Offhand", hand == InteractionHand.OFF_HAND);
        writeVec(state, "CasterPosition", player.position());
        capturePriorResistance(player, state);

        if (MODE_CHAIN_BLADE.equals(mode)) {
            writeVec(state, "ChainBladePosition", chainBladeGround);
            NightfallChainBladeEntity blade =
                    NightfallChainBladeEntity.spawn(player.serverLevel(), chainBladeGround, player.getYRot());
            state.putUUID("ChainBladeEntity", blade.getUUID());
            startCooldown(player, CHAIN_BLADE_COOLDOWN);
            player.displayClientMessage(
                    Component.translatable("message.finalparadox.nightfall.chain_blade_start"), true);
            player.level().playSound(null, blade.blockPosition(), SoundEvents.TRIDENT_HIT_GROUND,
                    SoundSource.PLAYERS, 2.0F, 0.7F);
            player.level().playSound(null, blade.blockPosition(), SoundEvents.CHAIN_PLACE,
                    SoundSource.PLAYERS, 1.5F, 0.5F);
        } else if (MODE_LASER.equals(mode)) {
            LivingEntity target = findLookTarget(player, TARGET_LOCK_RANGE);
            if (target != null) state.putUUID("LaserTarget", target.getUUID());
            state.put("HitTargets", new ListTag());
            startCooldown(player, LASER_COOLDOWN);
            player.displayClientMessage(Component.translatable("message.finalparadox.nightfall.laser_tracking"), true);
            player.level().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                    SoundSource.PLAYERS, 1.0F, 1.5F);
        } else if (MODE_RIFT.equals(mode)) {
            Vec3 direction = horizontalLook(player);
            writeVec(state, "Direction", direction);
            writeVec(state, "TrailBase", player.position().add(0.0D, 0.7D, 0.0D).subtract(direction.scale(4.0D)));
            writeVec(state, "WarningOrigin", player.position().add(0.0D, 1.0D, 0.0D));
            writeVec(state, "WarningTarget", player.getEyePosition());
            state.putIntArray("WarningPoints", chooseWarningPoints(player.getRandom()));
            state.put("HitTargets", new ListTag());
            startCooldown(player, RIFT_COOLDOWN);
            player.displayClientMessage(Component.translatable("message.finalparadox.nightfall.rift_start"), true);
        } else if (MODE_SMALL_LASER.equals(mode)) {
            LivingEntity target = findNearestComboTarget(player, TARGET_LOCK_RANGE);
            if (target != null) state.putUUID("SmallLaserTarget", target.getUUID());
            state.put("SmallLaserProjectiles", new ListTag());
            state.put("SmallLaserHitCooldowns", new ListTag());
            startCooldown(player, SMALL_LASER_COOLDOWN);
            player.displayClientMessage(
                    Component.translatable("message.finalparadox.nightfall.small_laser_start"), true);
        } else {
            Vec3 direction = horizontalLook(player);
            writeVec(state, "Direction", direction);
            state.putFloat("Yaw", yawFromDirection(direction));
            updateComboTracking(player, state);
            state.put("ComboHit0", new ListTag());
            state.put("ComboHit1", new ListTag());
            state.put("ComboHit2", new ListTag());
            startCooldown(player, COMBO_COOLDOWN);
            player.displayClientMessage(Component.translatable("message.finalparadox.nightfall.combo_start"), true);
        }
        maintainNightfallResistance(player);
        data.put(ACTIVE_KEY, state);
        if (MODE_COMBO.equals(mode)) {
            syncComboAnimation(player, state, 1);
        }
        return true;
    }

    public static void tick(ServerPlayer player) {
        tickCooldown(player);
        CompoundTag data = player.getPersistentData();
        if (!data.contains(ACTIVE_KEY)) {
            clearComboAnimation(player);
            return;
        }
        CompoundTag state = data.getCompound(ACTIVE_KEY);
        if (!player.isAlive()
                || !state.getString("Dimension").equals(player.level().dimension().location().toString())) {
            cancel(player);
            return;
        }
        maintainNightfallResistance(player);

        String mode = state.getString("Mode");
        /*
         * Preserve the established immobilization through the initial red
         * barrage, then release the item user. Source MarawThar relocates between
         * later choreography steps; pinning a player for the full 243 ticks is
         * neither source movement nor a usable player adaptation.
         */
        if (!MODE_CHAIN_BLADE.equals(mode)
                && (!MODE_SMALL_LASER.equals(mode)
                || state.getInt("Score") < SMALL_LASER_INITIAL_SEQUENCE_END_SCORE)) {
            lockCasterPosition(player, state);
        }
        int score = state.getInt("Score") + 1;
        state.putInt("Score", score);
        if (MODE_CHAIN_BLADE.equals(mode)) {
            tickChainBlade(player, state, score);
            if (score >= CHAIN_BLADE_END_SCORE) finish(player, state);
        } else if (MODE_LASER.equals(mode)) {
            tickLaser(player, state, score);
            if (score >= LASER_END_SCORE) finish(player, state);
        } else if (MODE_RIFT.equals(mode)) {
            tickRift(player, state, score);
            if (score >= RIFT_END_SCORE) finish(player, state);
        } else if (MODE_SMALL_LASER.equals(mode)) {
            tickSmallLaserBarrage(player, state, score);
            if (score >= SMALL_LASER_END_SCORE) finish(player, state);
        } else {
            tickCombo(player, state, score);
            if (score >= COMBO_END_SCORE) finish(player, state);
        }
    }

    public static void cancel(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(ACTIVE_KEY)) {
            clearComboAnimation(player);
            return;
        }
        CompoundTag state = data.getCompound(ACTIVE_KEY);
        discardLaserEntity(player, state);
        discardSmallLaserProjectiles(player, state);
        discardChainBladeEntity(player, state);
        restorePriorResistance(player, state);
        clearComboAnimation(player);
        data.remove(ACTIVE_KEY);
    }

    private static void finish(ServerPlayer player, CompoundTag state) {
        if (MODE_LASER.equals(state.getString("Mode"))) {
            laserEndEffects(player, state);
        }
        discardLaserEntity(player, state);
        discardSmallLaserProjectiles(player, state);
        discardChainBladeEntity(player, state);
        restorePriorResistance(player, state);
        clearComboAnimation(player);
        CompoundTag data = player.getPersistentData();
        data.remove(ACTIVE_KEY);
        if (!data.contains(COOLDOWN_KEY)) notifyReady(player);
    }

    private static void tickCombo(ServerPlayer player, CompoundTag state, int score) {
        updateComboTracking(player, state);
        syncComboAnimation(player, state, score);
        if (score == 1 || score == 37) teleportBurst(player.serverLevel(), player.position());

        for (int moveScore : COMBO_MOVE_SCORES) {
            if (score == moveScore) {
                moveLockedCaster(player, state, 1.0D);
                break;
            }
        }

        if (score == 9 || score == 19 || score == 29) {
            float pitch = score == 29 ? 0.0F : 0.6F;
            player.level().playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW,
                    SoundSource.PLAYERS, score == 29 ? 1.2F : 1.5F, pitch);
        }

        int frameIndex = comboFrameIndex(score);
        if (frameIndex < 0) return;
        int strikeIndex = frameIndex / 3;
        if (frameIndex % 3 == 0) player.swing(abilityHand(state), true);
        drawAndDamageSlashFrame(player, state, frameIndex, strikeIndex);
    }

    private static void tickChainBlade(ServerPlayer player, CompoundTag state, int score) {
        Vec3 blade = readVec(state, "ChainBladePosition");
        ServerLevel level = player.serverLevel();

        if (score < CHAIN_BLADE_PULL_SCORE) {
            drawChainBladeWarning(level, blade, score);
            return;
        }
        if (score == CHAIN_BLADE_PULL_SCORE) {
            pullChainBladeTargets(player, blade);
            player.swing(abilityHand(state), true);
            moveChainBladeCaster(player, blade, CHAIN_BLADE_DROP_HEIGHT);
            level.playSound(null, player.blockPosition(), SoundEvents.SHULKER_TELEPORT,
                    SoundSource.PLAYERS, 2.0F, 0.7F);
            return;
        }
        if (score < CHAIN_BLADE_IMPACT_SCORE) {
            double progress = (score - CHAIN_BLADE_PULL_SCORE)
                    / (double) (CHAIN_BLADE_IMPACT_SCORE - CHAIN_BLADE_PULL_SCORE);
            double height = CHAIN_BLADE_DROP_HEIGHT * (1.0D - progress);
            moveChainBladeCaster(player, blade, height);
            level.sendParticles(ParticleTypes.SQUID_INK,
                    player.getX(), player.getY() + 0.8D, player.getZ(),
                    8, 0.25D, 0.5D, 0.25D, 0.02D);
            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    player.getX(), player.getY() + 0.8D, player.getZ(),
                    5, 0.2D, 0.4D, 0.2D, 0.03D);
            return;
        }
        if (score == CHAIN_BLADE_IMPACT_SCORE) {
            moveChainBladeCaster(player, blade, 0.05D);
            impactChainBlade(player, blade);
            return;
        }
        drawChainBladeAftermath(level, blade, score - CHAIN_BLADE_IMPACT_SCORE);
    }

    private static void drawChainBladeWarning(ServerLevel level, Vec3 blade, int score) {
        double angle = Math.toRadians(score * 8.0D);
        for (int index = 0; index < 4; index++) {
            double direction = angle + index * Math.PI / 2.0D;
            Vec3 point = blade.add(Math.sin(direction) * 4.0D, 0.15D, Math.cos(direction) * 4.0D);
            level.sendParticles(ParticleTypes.END_ROD,
                    point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if ((score & 1) == 0) {
            level.sendParticles(ParticleTypes.SQUID_INK,
                    blade.x, blade.y + 0.25D, blade.z,
                    2, 0.35D, 0.1D, 0.35D, 0.02D);
        }
        if (score % 5 == 0) {
            for (int index = 0; index < 36; index++) {
                double ringAngle = index * Math.PI * 2.0D / 36.0D;
                Vec3 point = blade.add(
                        Math.sin(ringAngle) * CHAIN_BLADE_PULL_RADIUS,
                        0.08D,
                        Math.cos(ringAngle) * CHAIN_BLADE_PULL_RADIUS);
                level.sendParticles(ParticleTypes.SQUID_INK,
                        point.x, point.y, point.z,
                        1, 0.0D, 0.08D, 0.0D, 0.0D);
            }
        }
        if (score % 20 == 0) {
            level.playSound(null, net.minecraft.core.BlockPos.containing(blade),
                    SoundEvents.CHAIN_STEP, SoundSource.PLAYERS, 1.1F, 0.55F);
        }
    }

    private static void pullChainBladeTargets(ServerPlayer player, Vec3 blade) {
        ServerLevel level = player.serverLevel();
        Vec3 destination = blade.add(0.0D, 0.1D, 0.0D);
        AABB search = new AABB(blade, blade).inflate(CHAIN_BLADE_PULL_RADIUS);
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                search,
                target -> isValidTarget(player, target)
                        && target.position().distanceToSqr(blade)
                        <= CHAIN_BLADE_PULL_RADIUS * CHAIN_BLADE_PULL_RADIUS);
        for (LivingEntity target : targets) {
            drawChainLink(level, target.getBoundingBox().getCenter(), blade.add(0.0D, 0.7D, 0.0D));
            target.teleportTo(destination.x, destination.y, destination.z);
            target.setDeltaMovement(Vec3.ZERO);
            target.fallDistance = 0.0F;
        }
        level.playSound(null, net.minecraft.core.BlockPos.containing(blade),
                SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 3.0F, 0.4F);
        level.playSound(null, net.minecraft.core.BlockPos.containing(blade),
                SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 3.0F, 0.4F);
    }

    private static void drawChainLink(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        int segments = Math.max(1, Mth.ceil(delta.length() * 2.0D));
        for (int index = 0; index <= segments; index++) {
            Vec3 point = from.add(delta.scale(index / (double) segments));
            level.sendParticles(ParticleTypes.END_ROD,
                    point.x, point.y, point.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void moveChainBladeCaster(ServerPlayer player, Vec3 blade, double height) {
        player.teleportTo(blade.x, blade.y + height, blade.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
    }

    private static void impactChainBlade(ServerPlayer player, Vec3 blade) {
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                blade.x, blade.y + 0.2D, blade.z,
                2, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.FLASH,
                blade.x, blade.y + 0.3D, blade.z,
                2, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.SQUID_INK,
                blade.x, blade.y + 0.4D, blade.z,
                120, 6.0D, 1.0D, 6.0D, 0.15D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                blade.x, blade.y + 0.3D, blade.z,
                80, 5.0D, 0.8D, 5.0D, 0.08D);
        for (int index = 0; index < 72; index++) {
            double angle = index * Math.PI * 2.0D / 72.0D;
            Vec3 point = blade.add(Math.sin(angle) * 8.0D, 0.15D, Math.cos(angle) * 8.0D);
            level.sendParticles(ParticleTypes.SQUID_INK,
                    point.x, point.y, point.z,
                    1, 0.0D, 0.25D, 0.0D, 0.05D);
        }
        level.playSound(null, net.minecraft.core.BlockPos.containing(blade),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3.0F, 0.8F);
        level.playSound(null, net.minecraft.core.BlockPos.containing(blade),
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 3.0F, 0.1F);
        level.playSound(null, net.minecraft.core.BlockPos.containing(blade),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 2.0F, 1.4F);

        AABB damageArea = new AABB(blade, blade).inflate(CHAIN_BLADE_PULL_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                damageArea,
                target -> isValidTarget(player, target)
                        && target.position().distanceToSqr(blade)
                        <= CHAIN_BLADE_PULL_RADIUS * CHAIN_BLADE_PULL_RADIUS)) {
            target.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            hurtWithNightfall(player, target, CHAIN_BLADE_DAMAGE);
        }
    }

    private static void drawChainBladeAftermath(ServerLevel level, Vec3 blade, int age) {
        double radius = Math.min(8.0D, 1.0D + age * 0.55D);
        level.sendParticles(ParticleTypes.SQUID_INK,
                blade.x, blade.y + 0.25D, blade.z,
                12, radius, 0.45D, radius, 0.04D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                blade.x, blade.y + 0.2D, blade.z,
                8, radius * 0.7D, 0.3D, radius * 0.7D, 0.03D);
    }

    /**
     * Item adaptation of the source {@code coreografia/laseres_1} chain:
     * h2/combo2 red barrage, two h2/combo1 blue radial volleys, then the
     * h2/combo3 eight-position red orbit. Existing projectiles advance before
     * new ones are created because the source schedules run_proyectil for the
     * tick after each generator call.
     */
    private static void tickSmallLaserBarrage(ServerPlayer player, CompoundTag state, int score) {
        tickSmallLaserProjectiles(player, state, score);

        for (int fireScore : SMALL_LASER_FIRE_SCORES) {
            if (score == fireScore) {
                spawnTrackedRedSmallLaser(player, state);
                player.swing(abilityHand(state), true);
                break;
            }
        }

        for (int volleyIndex = 0; volleyIndex < SMALL_LASER_BLUE_VOLLEY_STARTS.length; volleyIndex++) {
            tickBlueSmallLaserVolley(player, state, score,
                    SMALL_LASER_BLUE_VOLLEY_STARTS[volleyIndex], volleyIndex);
        }
        tickRedSmallLaserOrbit(player, state, score);
    }

    private static void spawnTrackedRedSmallLaser(ServerPlayer player, CompoundTag state) {
        LivingEntity target = resolveSmallLaserTarget(player, state);
        Vec3 direction = horizontalLook(player);
        if (target != null) {
            Vec3 toward = target.getEyePosition()
                    .subtract(player.position())
                    .multiply(1.0D, 0.0D, 1.0D);
            if (toward.lengthSqr() > 1.0E-6D) direction = toward.normalize();
        }

        // The source creates the beam at the B9 body marker. The 0.5 vertical
        // offset is the player-body equivalent of its fixed Y=51.5 hit plane.
        Vec3 origin = player.position().add(0.0D, 0.5D, 0.0D).add(direction.scale(1.7D));
        spawnSmallLaserProjectile(player, state, origin, direction, true);
    }

    private static void tickBlueSmallLaserVolley(ServerPlayer player, CompoundTag state, int score,
                                                  int startScore, int volleyIndex) {
        int localScore = score - startScore;
        String directionKey = "SmallLaserBlueDirection" + volleyIndex;
        if (localScore == 0) {
            LivingEntity target = resolveSmallLaserTarget(player, state);
            Vec3 baseDirection = horizontalLook(player);
            if (target != null) {
                Vec3 toward = target.getEyePosition()
                        .subtract(player.position())
                        .multiply(1.0D, 0.0D, 1.0D);
                if (toward.lengthSqr() > 1.0E-6D) baseDirection = toward.normalize();
            }
            writeVec(state, directionKey, baseDirection);
            if (volleyIndex == 0) {
                player.displayClientMessage(
                        Component.translatable("message.finalparadox.nightfall.small_laser_blue_followup"), true);
            }
            return;
        }
        if (!state.contains(directionKey, CompoundTag.TAG_COMPOUND)) return;

        Vec3 baseDirection = readVec(state, directionKey);
        if (localScore >= 5 && localScore <= 10) {
            int angle = Math.floorMod(120 + (localScore - 5) * 60, 360);
            spawnBlueSmallLaserAtAngle(player, state, baseDirection, angle, true);
            if (localScore == 5) player.swing(abilityHand(state), true);
            return;
        }

        if (localScore < 19 || localScore > 27) return;
        for (int angle = 130; angle <= 470; angle += 10) {
            if (Math.floorMod(angle, 60) == 0) continue;
            int sourceFireScore = 19 + (angle - 120) / 40;
            if (sourceFireScore == localScore) {
                spawnBlueSmallLaserAtAngle(player, state, baseDirection, angle, false);
            }
        }
    }

    private static void spawnBlueSmallLaserAtAngle(ServerPlayer player, CompoundTag state,
                                                    Vec3 baseDirection, int angle, boolean warning) {
        float baseYaw = yawFromDirection(baseDirection);
        Vec3 direction = Vec3.directionFromRotation(0.0F, baseYaw + angle)
                .multiply(1.0D, 0.0D, 1.0D)
                .normalize();
        Vec3 origin = player.position().add(0.0D, 0.5D, 0.0D).add(direction.scale(1.7D));
        if (warning) drawBlueSmallLaserWarning(player.serverLevel(), origin, direction);
        spawnSmallLaserProjectile(player, state, origin, direction, false);
    }

    private static void drawBlueSmallLaserWarning(ServerLevel level, Vec3 origin, Vec3 direction) {
        Vec3 raised = origin.add(0.0D, 0.9D, 0.0D);
        for (int distance = 1; distance <= 45; distance++) {
            Vec3 point = raised.add(direction.scale(distance));
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void tickRedSmallLaserOrbit(ServerPlayer player, CompoundTag state, int score) {
        if (score == SMALL_LASER_ORBIT_START_SCORE) {
            player.displayClientMessage(
                    Component.translatable("message.finalparadox.nightfall.small_laser_orbit_followup"), true);
            LivingEntity target = resolveSmallLaserTarget(player, state);
            if (target != null) {
                moveSmallLaserCasterWithOrbit(player, state, target, 0);
            }
            return;
        }
        int localScore = score - SMALL_LASER_ORBIT_START_SCORE;
        if (localScore < 1 || localScore > 64) return;

        LivingEntity target = resolveSmallLaserTarget(player, state);
        if (target == null) return;
        Vec3 center = target.position().add(0.0D, 0.5D, 0.0D);
        moveSmallLaserCasterWithOrbit(player, state, target, localScore);

        for (int index = 0; index < SMALL_LASER_ORBIT_OFFSETS.length; index++) {
            int warningStart = SMALL_LASER_ORBIT_WARNING_SCORES[index];
            int warningEnd = index == 0 ? 43 : warningStart + 40;
            Vec3 origin = center.add(SMALL_LASER_ORBIT_OFFSETS[index]);
            if (localScore >= warningStart && localScore <= warningEnd) {
                drawRedSmallLaserOrbitWarning(player.serverLevel(), origin);
            }

            int fireScore = SMALL_LASER_ORBIT_FIRST_FIRE_SCORE
                    + index * SMALL_LASER_ORBIT_FIRE_INTERVAL;
            if (localScore != fireScore) continue;
            Vec3 direction = target.getEyePosition()
                    .subtract(origin)
                    .multiply(1.0D, 0.0D, 1.0D);
            if (direction.lengthSqr() < 1.0E-6D) direction = horizontalLook(player);
            else direction = direction.normalize();
            spawnSmallLaserProjectile(player, state, origin, direction, true);
            ServerLevel level = player.serverLevel();
            level.sendParticles(ParticleTypes.EXPLOSION,
                    origin.x, origin.y + 1.0D, origin.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.sendParticles(ParticleTypes.SQUID_INK,
                    origin.x, origin.y + 1.0D, origin.z,
                    10, 0.0D, 0.0D, 0.0D, 0.4D);
        }
    }

    /**
     * Reproduces the source h2/combo3 boss movement. MarawThar occupies the
     * radius-eight warning positions in order, one block inward and 0.3 blocks
     * to the local right, while facing the selected target. The stored
     * destination keeps the player fixed between source teleports without
     * making the client authoritative.
     */
    private static void moveSmallLaserCasterWithOrbit(ServerPlayer player, CompoundTag state,
                                                       LivingEntity target, int localScore) {
        int orbitIndex = smallLaserOrbitTeleportIndex(localScore);
        if (orbitIndex >= 0) {
            Vec3 offset = SMALL_LASER_ORBIT_OFFSETS[orbitIndex];
            Vec3 inward = offset.scale(-1.0D).normalize();
            Vec3 localLeft = inward.yRot((float) (Math.PI / 2.0D));
            Vec3 desired = target.position()
                    .add(offset)
                    .add(inward.scale(SMALL_LASER_ORBIT_CASTER_INSET))
                    .add(localLeft.scale(SMALL_LASER_ORBIT_CASTER_SIDE_OFFSET));
            Vec3 safeDestination = findSafeSmallLaserOrbitDestination(player, desired);
            if (safeDestination != null) {
                writeVec(state, SMALL_LASER_ORBIT_CASTER_POSITION, safeDestination);
            }
        }

        if (localScore < 0 || localScore > SMALL_LASER_ORBIT_CASTER_HOLD_END_SCORE
                || !state.contains(SMALL_LASER_ORBIT_CASTER_POSITION, CompoundTag.TAG_COMPOUND)) {
            if (localScore > SMALL_LASER_ORBIT_CASTER_HOLD_END_SCORE) {
                state.remove(SMALL_LASER_ORBIT_CASTER_POSITION);
            }
            return;
        }

        Vec3 destination = readVec(state, SMALL_LASER_ORBIT_CASTER_POSITION);
        Vec3 towardTarget = target.position()
                .subtract(destination)
                .multiply(1.0D, 0.0D, 1.0D);
        float yaw = towardTarget.lengthSqr() > 1.0E-6D
                ? yawFromDirection(towardTarget.normalize())
                : player.getYRot();
        player.teleportTo(player.serverLevel(),
                destination.x, destination.y, destination.z, yaw, player.getXRot());
        player.setYHeadRot(yaw);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
    }

    private static int smallLaserOrbitTeleportIndex(int localScore) {
        if (localScore >= 0 && localScore <= 3) return 0;
        for (int index = 1; index < SMALL_LASER_ORBIT_WARNING_SCORES.length; index++) {
            if (localScore == SMALL_LASER_ORBIT_WARNING_SCORES[index]) return index;
        }
        return localScore >= 27 && localScore <= SMALL_LASER_ORBIT_CASTER_HOLD_END_SCORE ? 0 : -1;
    }

    private static Vec3 findSafeSmallLaserOrbitDestination(ServerPlayer player, Vec3 desired) {
        ServerLevel level = player.serverLevel();
        double[] verticalOffsets = {0.0D, 1.0D, 2.0D, -1.0D, 3.0D, -2.0D};
        for (double verticalOffset : verticalOffsets) {
            Vec3 candidate = desired.add(0.0D, verticalOffset, 0.0D);
            AABB destinationBox = player.getBoundingBox().move(candidate.subtract(player.position()));
            if (level.getWorldBorder().isWithinBounds(destinationBox)
                    && level.noCollision(player, destinationBox)) {
                return candidate;
            }
        }
        return null;
    }

    private static void drawRedSmallLaserOrbitWarning(ServerLevel level, Vec3 origin) {
        level.sendParticles(WARNING_RED, origin.x, origin.y, origin.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.SQUID_INK, origin.x, origin.y, origin.z,
                0, 0.0D, -1.0D, 0.0D, 999999.0D);
    }

    private static void spawnSmallLaserProjectile(ServerPlayer player, CompoundTag state,
                                                   Vec3 origin, Vec3 direction, boolean red) {
        ServerLevel level = player.serverLevel();
        CompoundTag projectile = new CompoundTag();
        projectile.putInt("Age", red ? SMALL_LASER_INITIAL_AGE : SMALL_LASER_BLUE_INITIAL_AGE);
        projectile.putBoolean("Red", red);
        writeVec(projectile, "Origin", origin);
        writeVec(projectile, "Direction", direction);

        for (int index = 0; index < SMALL_LASER_PARTS; index++) {
            Vec3 partPosition = origin.add(direction.scale(index * SMALL_LASER_PART_SPACING));
            ArmorStand part = createSmallLaserPart(level, partPosition, direction, red);
            projectile.putUUID("Part" + index, part.getUUID());
        }

        state.getList("SmallLaserProjectiles", CompoundTag.TAG_COMPOUND).add(projectile);
        level.playSound(null, net.minecraft.core.BlockPos.containing(origin),
                SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0F, 1.4F);
        level.playSound(null, net.minecraft.core.BlockPos.containing(origin),
                 SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.5F, 1.7F);
    }

    private static ArmorStand createSmallLaserPart(ServerLevel level, Vec3 position,
                                                   Vec3 direction, boolean red) {
        ArmorStand part = new ArmorStand(level, position.x, position.y, position.z);
        part.setInvisible(true);
        part.setNoGravity(true);
        part.setInvulnerable(true);
        part.setSilent(true);
        CompoundTag markerData = new CompoundTag();
        part.saveWithoutId(markerData);
        markerData.putBoolean("Marker", true);
        markerData.putBoolean("Small", false);
        part.load(markerData);
        part.noPhysics = true;
        part.setGlowingTag(true);
        part.setRightArmPose(new Rotations(345.0F, 225.0F, 0.0F));
        part.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BLACK_CONCRETE));
        part.addTag("finalparadox_nightfall_small_laser");
        part.moveTo(position.x, position.y, position.z, yawFromDirection(direction), 0.0F);
        level.addFreshEntity(part);

        if (red) {
            addSmallLaserPartToTeam(level, part, SMALL_LASER_RED_TEAM, ChatFormatting.RED);
        }
        return part;
    }

    private static void addSmallLaserPartToTeam(ServerLevel level, Entity part,
                                                String teamName, ChatFormatting color) {
        PlayerTeam team = level.getScoreboard().getPlayerTeam(teamName);
        if (team == null) team = level.getScoreboard().addPlayerTeam(teamName);
        team.setColor(color);
        level.getScoreboard().addPlayerToTeam(part.getScoreboardName(), team);
    }

    private static LivingEntity resolveSmallLaserTarget(ServerPlayer player, CompoundTag state) {
        if (state.hasUUID("SmallLaserTarget")) {
            Entity raw = player.serverLevel().getEntity(state.getUUID("SmallLaserTarget"));
            if (raw instanceof LivingEntity living
                    && isValidTarget(player, living)
                    && withinTargetLockRange(player, living)) {
                return living;
            }
        }

        LivingEntity replacement = findNearestComboTarget(player, TARGET_LOCK_RANGE);
        if (replacement == null) {
            state.remove("SmallLaserTarget");
            return null;
        }
        state.putUUID("SmallLaserTarget", replacement.getUUID());
        return replacement;
    }

    private static void tickSmallLaserProjectiles(ServerPlayer player, CompoundTag state, int score) {
        ServerLevel level = player.serverLevel();
        ListTag projectiles = state.getList("SmallLaserProjectiles", CompoundTag.TAG_COMPOUND);
        for (int index = projectiles.size() - 1; index >= 0; index--) {
            CompoundTag projectile = projectiles.getCompound(index);
            int age = projectile.getInt("Age") + 1;
            projectile.putInt("Age", age);

            Vec3 origin = readVec(projectile, "Origin");
            Vec3 direction = readVec(projectile, "Direction");
            if (age == 4) {
                level.playSound(null, net.minecraft.core.BlockPos.containing(origin),
                        SoundEvents.PUFFER_FISH_BLOW_OUT, SoundSource.PLAYERS, 0.4F, 0.9F);
            }
            if (age == SMALL_LASER_BACKSTEP_AGE) {
                if (!projectile.getBoolean("Red")) {
                    addSmallLaserProjectileToTeam(level, projectile,
                            SMALL_LASER_BLUE_TEAM, ChatFormatting.AQUA);
                }
                origin = origin.subtract(direction.scale(SMALL_LASER_BACKSTEP));
                writeVec(projectile, "Origin", origin);
                moveSmallLaserParts(level, projectile, origin, direction);
            }
            if (age >= SMALL_LASER_MOVE_START_AGE && age <= SMALL_LASER_MOVE_END_AGE) {
                for (int substep = 0; substep < SMALL_LASER_SUBSTEPS; substep++) {
                    damageSmallLaserStep(player, state, origin, direction, score);
                    origin = origin.add(direction.scale(SMALL_LASER_SUBSTEP));
                    writeVec(projectile, "Origin", origin);
                    moveSmallLaserParts(level, projectile, origin, direction);
                }
            }

            if (age >= SMALL_LASER_MOVE_END_AGE) {
                discardSmallLaserProjectile(player, projectile);
                projectiles.remove(index);
            }
        }
    }

    private static void addSmallLaserProjectileToTeam(ServerLevel level, CompoundTag projectile,
                                                      String teamName, ChatFormatting color) {
        for (int index = 0; index < SMALL_LASER_PARTS; index++) {
            if (!projectile.hasUUID("Part" + index)) continue;
            Entity raw = level.getEntity(projectile.getUUID("Part" + index));
            if (raw != null) addSmallLaserPartToTeam(level, raw, teamName, color);
        }
    }

    private static void moveSmallLaserParts(ServerLevel level, CompoundTag projectile,
                                            Vec3 origin, Vec3 direction) {
        for (int index = 0; index < SMALL_LASER_PARTS; index++) {
            if (!projectile.hasUUID("Part" + index)) continue;
            Entity raw = level.getEntity(projectile.getUUID("Part" + index));
            if (!(raw instanceof ArmorStand part)) continue;
            Vec3 position = origin.add(direction.scale(index * SMALL_LASER_PART_SPACING));
            part.moveTo(position.x, position.y, position.z, yawFromDirection(direction), 0.0F);
        }
    }

    private static void damageSmallLaserStep(ServerPlayer player, CompoundTag state,
                                             Vec3 origin,
                                             Vec3 direction, int score) {
        ServerLevel level = player.serverLevel();
        ListTag cooldowns = state.getList("SmallLaserHitCooldowns", CompoundTag.TAG_COMPOUND);
        for (int part = 0; part < SMALL_LASER_PARTS; part++) {
            // mov_proyectil checks one block behind each part before advancing.
            Vec3 center = origin
                    .add(direction.scale(part * SMALL_LASER_PART_SPACING))
                    .subtract(direction.scale(SMALL_LASER_SUBSTEP));
            AABB hitBox = new AABB(center, center).inflate(1.0D);
            for (LivingEntity target : level.getEntitiesOfClass(
                    LivingEntity.class, hitBox,
                    target -> isValidTarget(player, target)
                            && !riftHitOnCooldown(cooldowns, target.getUUID(), score))) {
                setRiftHitCooldown(cooldowns, target.getUUID(), score + 2);
                target.removeEffect(MobEffects.DAMAGE_RESISTANCE);
                hurtWithNightfall(player, target, SMALL_LASER_DAMAGE);
                level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_HURT_ON_FIRE,
                        SoundSource.PLAYERS, 1.0F, 1.0F);
                level.sendParticles(ParticleTypes.SQUID_INK,
                        target.getX(), target.getY() + 1.0D, target.getZ(),
                        6, 0.0D, 0.0D, 0.0D, 0.2D);
            }
        }
    }

    private static void updateComboTracking(ServerPlayer player, CompoundTag state) {
        LivingEntity target = findNearestComboTarget(player, TARGET_LOCK_RANGE);
        if (target == null) {
            state.remove("ComboTarget");
            return;
        }

        state.putUUID("ComboTarget", target.getUUID());
        Vec3 direction = target.getBoundingBox().getCenter()
                .subtract(player.position())
                .multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() < 1.0E-6D) return;
        direction = direction.normalize();
        writeVec(state, "Direction", direction);
        state.putFloat("Yaw", yawFromDirection(direction));
    }

    private static LivingEntity findNearestComboTarget(ServerPlayer player, double range) {
        double rangeSqr = range * range;
        return player.serverLevel().getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(range),
                        target -> isComboTrackingEnemy(player, target)
                                && target.distanceToSqr(player) <= rangeSqr)
                .stream()
                .min(Comparator.comparingDouble(target -> target.distanceToSqr(player)))
                .orElse(null);
    }

    private static boolean isComboTrackingEnemy(ServerPlayer player, LivingEntity target) {
        if (!isValidTarget(player, target)) return false;
        if (target instanceof Player) return true;
        if (target.getType().getCategory() == MobCategory.MONSTER
                || target.getType().is(ModTags.EntityTypes.NIGHTFALL_TARGETS)) return true;
        return target.getLastHurtByMob() == player || player.getLastHurtByMob() == target;
    }

    private static int comboFrameIndex(int score) {
        if (score >= 12 && score <= 14) return score - 12;
        if (score >= 20 && score <= 22) return 3 + score - 20;
        if (score >= 30 && score <= 32) return 6 + score - 30;
        return -1;
    }

    private static void drawAndDamageSlashFrame(ServerPlayer player, CompoundTag state,
                                                int frameIndex, int strikeIndex) {
        ServerLevel level = player.serverLevel();
        float baseYaw = state.getFloat("Yaw");
        Vec3 forward = readVec(state, "Direction");
        Vec3 left = forward.yRot((float) (Math.PI / 2.0D));
        Vec3 origin = player.position();
        NightfallSlashFrames.Sample[] samples = NightfallSlashFrames.FRAMES[frameIndex];
        List<Vec3> hitCenters = new ArrayList<>(samples.length);

        for (NightfallSlashFrames.Sample sample : samples) {
            Vec3 base = origin
                    .add(left.scale(sample.left()))
                    .add(0.0D, sample.up(), 0.0D)
                    .add(forward.scale(sample.forward()));
            Vec3 ray = Vec3.directionFromRotation(sample.pitch(), baseYaw + sample.yaw());
            for (int index = 0; index < NORMAL_SLASH_DISTANCES.length; index++) {
                Vec3 point = base.add(ray.scale(NORMAL_SLASH_DISTANCES[index]));
                level.sendParticles(NORMAL_SLASH_DUST[index], point.x, point.y, point.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
            Vec3 tip = base.add(ray.scale(3.0D));
            level.sendParticles(ParticleTypes.END_ROD, tip.x, tip.y, tip.z,
                    0, 0.0D, -999999.0D, 0.0D, 1.0D);
            hitCenters.add(base.add(0.0D, -1.0D, 0.0D).add(ray.scale(2.0D)));
        }

        ListTag hitTargets = state.getList("ComboHit" + strikeIndex, CompoundTag.TAG_COMPOUND);
        AABB search = player.getBoundingBox().inflate(8.0D, 5.0D, 8.0D);
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class, search,
                target -> isValidTarget(player, target) && !wasHit(hitTargets, target.getUUID()))) {
            boolean inside = false;
            Vec3 feet = target.position();
            for (Vec3 center : hitCenters) {
                if (feet.distanceToSqr(center) <= 2.5D * 2.5D) {
                    inside = true;
                    break;
                }
            }
            if (!inside) continue;
            rememberHit(hitTargets, target.getUUID());
            applySwordHit(player, target);
        }
    }

    private static void tickRift(ServerPlayer player, CompoundTag state, int score) {
        ServerLevel level = player.serverLevel();
        if (score == 1 || score == 38) teleportBurst(level, player.position());
        if (score == RIFT_WARNING_SCORE) {
            level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL,
                    SoundSource.PLAYERS, 0.6F, 1.1F);
        }
        if (score > RIFT_WARNING_SCORE && score < RIFT_WARNING_SCORE + 14) {
            drawRiftWarning(level, state, score - RIFT_WARNING_SCORE - 1);
        }
        if (score == RIFT_RELEASE_SCORE) {
            player.swing(abilityHand(state), true);
            level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW,
                    SoundSource.PLAYERS, 1.5F, 0.6F);
            level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SHOOT,
                    SoundSource.PLAYERS, 0.7F, 1.0F);
            level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL,
                    SoundSource.PLAYERS, 0.9F, 1.0F);
        }
        if (score == RIFT_CASTER_STEP_SCORE) moveLockedCaster(player, state, 0.5D);
        if (score <= RIFT_RELEASE_SCORE || score > RIFT_RELEASE_SCORE + RIFT_TRAVEL_TICKS) return;

        int travelIndex = score - RIFT_RELEASE_SCORE - 1;
        Vec3 base = readVec(state, "TrailBase");
        Vec3 direction = readVec(state, "Direction");
        Vec3 left = direction.yRot((float) (Math.PI / 2.0D));
        double tickStart = travelIndex * RIFT_SPEED;
        for (int substep = 1; substep <= RIFT_SUBSTEPS_PER_TICK; substep++) {
            Vec3 point = base.add(direction.scale(tickStart + substep * RIFT_SUBSTEP));
            drawRiftBlueArc(level, point, left, direction);
        }
        Vec3 marker = base.add(direction.scale(tickStart + RIFT_SPEED));
        damageRiftAt(player, state, marker, score);
        drawRiftEndRodRing(level, marker, left, direction);
    }

    private static void drawRiftWarning(ServerLevel level, CompoundTag state, int age) {
        Vec3 center = readVec(state, "WarningOrigin");
        Vec3 target = readVec(state, "WarningTarget");
        int[] selected = state.getIntArray("WarningPoints");
        for (int index : selected) {
            if (index < 0 || index >= RIFT_WARNING_POINTS.size()) continue;
            // The source runner emits, advances 0.15 toward the caster's eyes,
            // emits, then advances another 0.15. Re-simulating all preceding
            // substeps also preserves its small overshoot/turnaround at center.
            Vec3 stand = advanceToward(center.add(RIFT_WARNING_POINTS.get(index)), target, age * 2);
            Vec3 before = stand.add(0.0D, -0.5D, 0.0D);
            level.sendParticles(RIFT_BLUE, before.x, before.y, before.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            Vec3 afterStand = advanceToward(stand, target, 1);
            Vec3 after = afterStand.add(0.0D, -0.5D, 0.0D);
            level.sendParticles(RIFT_BLUE, after.x, after.y, after.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static Vec3 advanceToward(Vec3 start, Vec3 target, int steps) {
        Vec3 current = start;
        for (int step = 0; step < steps; step++) {
            Vec3 delta = target.subtract(current);
            if (delta.lengthSqr() < 1.0E-12D) break;
            current = current.add(delta.normalize().scale(0.15D));
        }
        return current;
    }

    private static void drawRiftBlueArc(ServerLevel level, Vec3 center, Vec3 left, Vec3 forward) {
        for (double[] point : RIFT_PARTICLE_ARC) {
            Vec3 position = center.add(left.scale(point[0])).add(forward.scale(point[1]));
            level.sendParticles(RIFT_BLUE, position.x, position.y, position.z,
                    1, 0.15D, 0.0D, 0.15D, 0.0D);
        }
    }

    private static void drawRiftEndRodRing(ServerLevel level, Vec3 center, Vec3 left, Vec3 forward) {
        for (int index = 0; index < 24; index++) {
            double angle = Math.toRadians(245.0D + index * 10.0D);
            Vec3 point = center
                    .add(left.scale(Math.sin(angle) * 2.5D))
                    .add(forward.scale(Math.cos(angle) * 2.5D));
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z,
                    0, 0.0D, -1.0D, 0.0D, 999999.0D);
        }
    }

    private static void damageRiftAt(ServerPlayer player, CompoundTag state, Vec3 marker, int score) {
        ServerLevel level = player.serverLevel();
        ListTag hitTargets = state.getList("HitTargets", CompoundTag.TAG_COMPOUND);
        AABB search = new AABB(marker, marker).inflate(3.0D);
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class, search,
                target -> isValidTarget(player, target)
                        && !riftHitOnCooldown(hitTargets, target.getUUID(), score))) {
            if (target.position().distanceToSqr(marker) > 9.0D) continue;
            setRiftHitCooldown(hitTargets, target.getUUID(), score + 4);
            applySwordHit(player, target);
        }
    }

    private static void tickLaser(ServerPlayer player, CompoundTag state, int score) {
        ServerLevel level = player.serverLevel();
        if (score <= LASER_LOCK_SCORE) {
            Vec3 origin = player.position();
            Vec3 direction = trackedLaserDirection(player, state, origin);
            drawTrackingRails(level, origin, direction);
            if (score == LASER_LOCK_SCORE) {
                writeVec(state, "LaserMarkerOrigin", player.position().add(0.0D, -0.2D, 0.0D));
                writeVec(state, "LaserWarningOrigin", player.position().add(0.0D, 1.5D, 0.0D));
                writeVec(state, "LaserDirection", direction);
                player.displayClientMessage(Component.translatable("message.finalparadox.nightfall.laser_locked"), true);
                drawWarningLine(level, readVec(state, "LaserWarningOrigin"), direction);
                level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                        SoundSource.PLAYERS, 1.0F, 2.0F);
            }
            return;
        }

        Vec3 direction = readVec(state, "LaserDirection");
        if (score == LASER_SECOND_WARNING_SCORE) {
            drawWarningLine(level, readVec(state, "LaserWarningOrigin"), direction);
            level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                    SoundSource.PLAYERS, 1.0F, 2.0F);
            return;
        }
        if (score < LASER_RELEASE_SCORE) return;

        int beamTick = score - LASER_RELEASE_SCORE;
        if (beamTick == 0) {
            state.putInt("SpinDirection", chooseLaserSpin(player, state,
                    readVec(state, "LaserMarkerOrigin"), direction));
            level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_PREPARE_BLINDNESS,
                    SoundSource.PLAYERS, 0.8F, 0.0F);
            level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_PREPARE_BLINDNESS,
                    SoundSource.PLAYERS, 0.8F, 1.0F);
            level.playSound(null, player.blockPosition(), SoundEvents.END_GATEWAY_SPAWN,
                    SoundSource.PLAYERS, 0.8F, 1.5F);
        }

        int rotatingTicks = Mth.clamp(beamTick - 4, 0, 70);
        double degrees = state.getInt("SpinDirection") * rotatingTicks * LASER_ROTATION_PER_TICK;
        Vec3 rotatedDirection = direction.yRot((float) Math.toRadians(degrees)).normalize();
        Vec3 beamBase = readVec(state, "LaserMarkerOrigin").add(rotatedDirection.scale(0.4D));
        updateLaserEntity(player, state, beamBase, rotatedDirection, beamTick);
        drawLaserParticles(level, player, beamBase, rotatedDirection, beamTick);
        damageLaser(player, state, beamBase, rotatedDirection);
    }

    private static Vec3 trackedLaserDirection(ServerPlayer player, CompoundTag state, Vec3 origin) {
        if (state.hasUUID("LaserTarget")) {
            Entity entity = player.serverLevel().getEntity(state.getUUID("LaserTarget"));
            if (entity instanceof LivingEntity living
                    && isValidTarget(player, living)
                    && withinTargetLockRange(player, living)) {
                Vec3 toward = living.getEyePosition().subtract(origin).multiply(1.0D, 0.0D, 1.0D);
                if (toward.lengthSqr() > 1.0E-6D) return toward.normalize();
            }
        }
        return horizontalLook(player);
    }

    private static void drawTrackingRails(ServerLevel level, Vec3 origin, Vec3 direction) {
        Vec3 side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 1.0E-6D) side = new Vec3(1.0D, 0.0D, 0.0D);
        side = side.normalize().scale(0.5D);
        for (int index = 1; index <= 23; index++) {
            Vec3 center = origin.add(direction.scale(index * 0.9D));
            for (int sign : new int[]{-1, 1}) {
                Vec3 point = center.add(side.scale(sign));
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        point.x, point.y - 0.4D, point.z, 0,
                        0.0D, -1.0D, 0.0D, 1000.0D);
            }
        }
    }

    private static void drawWarningLine(ServerLevel level, Vec3 origin, Vec3 direction) {
        for (int distance = 1; distance <= 30; distance++) {
            Vec3 point = origin.add(direction.scale(distance));
            level.sendParticles(WARNING_RED, point.x, point.y, point.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static int chooseLaserSpin(ServerPlayer player, CompoundTag state,
                                       Vec3 origin, Vec3 direction) {
        LivingEntity target = null;
        if (state.hasUUID("LaserTarget")) {
            Entity entity = player.serverLevel().getEntity(state.getUUID("LaserTarget"));
            if (entity instanceof LivingEntity living
                    && isValidTarget(player, living)
                    && withinTargetLockRange(player, living)) {
                target = living;
            }
        }
        if (target == null) return (player.tickCount & 1) == 0 ? 1 : -1;
        Vec3 flatDirection = direction.multiply(1.0D, 0.0D, 1.0D);
        if (flatDirection.lengthSqr() < 1.0E-6D) flatDirection = horizontalLook(player);
        flatDirection = flatDirection.normalize();
        Vec3 toward = target.position().subtract(origin).multiply(1.0D, 0.0D, 1.0D);
        Vec3 left = flatDirection.yRot((float) (Math.PI / 2.0D));
        return toward.dot(left) >= 0.0D ? 1 : -1;
    }

    private static void updateLaserEntity(ServerPlayer player, CompoundTag state,
                                          Vec3 beamBase, Vec3 direction, int beamTick) {
        NightfallLaserEntity laser = null;
        if (state.hasUUID("LaserEntity")) {
            Entity raw = player.serverLevel().getEntity(state.getUUID("LaserEntity"));
            if (raw instanceof NightfallLaserEntity existing) laser = existing;
        }
        if (laser == null) {
            laser = NightfallLaserEntity.spawn(player.serverLevel(), beamBase, direction, beamTick);
            state.putUUID("LaserEntity", laser.getUUID());
        } else {
            laser.setBeam(beamBase, direction, beamTick);
        }
    }

    private static void drawLaserParticles(ServerLevel level, ServerPlayer player,
                                           Vec3 beamBase, Vec3 direction, int beamTick) {
        for (int index = 0; index < 3; index++) {
            int segment = 1 + player.getRandom().nextInt(LASER_SEGMENTS);
            Vec3 point = beamBase.add(direction.scale(segment * LASER_SEGMENT_SPACING)).add(0.0D, 1.3D, 0.0D);
            level.sendParticles(LASER_BLUE, point.x, point.y, point.z,
                    1, 0.1D, 0.1D, 0.1D, 0.0D);
        }
        for (int index = 0; index < 2; index++) {
            int segment = 1 + player.getRandom().nextInt(LASER_SEGMENTS);
            Vec3 point = beamBase.add(direction.scale(segment * LASER_SEGMENT_SPACING)).add(0.0D, 1.7D, 0.0D);
            level.sendParticles(ParticleTypes.SMOKE, point.x, point.y, point.z,
                    1, 0.1D, 0.1D, 0.1D, 0.0D);
        }
        boolean pulseNearCaster = false;
        for (int segment = 1; segment <= LASER_SEGMENTS; segment++) {
            if (laserPulseValue(segment, beamTick) != 1) continue;
            Vec3 marker = beamBase.add(direction.scale(segment * LASER_SEGMENT_SPACING));
            if (marker.distanceToSqr(player.position()) <= 4.0D) {
                pulseNearCaster = true;
                break;
            }
        }
        if (pulseNearCaster) {
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.AMBIENT, 2.0F, 1.2F);
            Vec3 flash = player.position().add(0.0D, 1.0D, 0.0D);
            level.sendParticles(ParticleTypes.FLASH, flash.x, flash.y, flash.z,
                    1, 0.0D, 0.0D, 0.0D, 1.0D);
        }
    }

    private static int laserPulseValue(int segment, int beamTick) {
        int initial = Math.floorMod(segment - 1, 8) + 1;
        return Math.floorMod(initial - beamTick - 2, 8) + 1;
    }

    private static void laserEndEffects(ServerPlayer player, CompoundTag state) {
        ServerLevel level = player.serverLevel();
        Vec3 base = null;
        Vec3 direction = null;
        if (state.hasUUID("LaserEntity")) {
            Entity raw = level.getEntity(state.getUUID("LaserEntity"));
            if (raw instanceof NightfallLaserEntity laser) {
                base = laser.position();
                direction = laser.beamDirection();
            }
        }
        if (base != null && direction != null) {
            for (int segment = 1; segment <= LASER_SEGMENTS; segment++) {
                Vec3 point = base.add(direction.scale(segment * LASER_SEGMENT_SPACING)).add(0.0D, 1.0D, 0.0D);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, point.x, point.y, point.z,
                        3, 0.2D, 0.2D, 0.2D, 0.0D);
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 1.0F, 0.6F);
    }

    private static void damageLaser(ServerPlayer player, CompoundTag state,
                                    Vec3 beamBase, Vec3 direction) {
        ServerLevel level = player.serverLevel();
        Vec3 beamEnd = beamBase.add(direction.scale(LASER_SEGMENTS * LASER_SEGMENT_SPACING));
        AABB search = new AABB(beamBase, beamEnd).inflate(2.0D, 10.0D, 2.0D);
        ListTag hitTargets = state.getList("HitTargets", CompoundTag.TAG_COMPOUND);
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class, search,
                target -> isValidTarget(player, target) && !wasHit(hitTargets, target.getUUID()))) {
            boolean hit = false;
            for (int verticalSample = 0; verticalSample <= 4 && !hit; verticalSample++) {
                Vec3 sample = target.position().add(0.0D, -verticalSample * 2.0D, 0.0D);
                for (int segment = 1; segment <= LASER_SEGMENTS; segment++) {
                    Vec3 marker = beamBase.add(direction.scale(segment * LASER_SEGMENT_SPACING));
                    if (sample.distanceToSqr(marker) <= 1.1D * 1.1D) {
                        hit = true;
                        break;
                    }
                }
            }
            if (!hit) continue;
            rememberHit(hitTargets, target.getUUID());
            target.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            hurtWithNightfall(player, target, LASER_DAMAGE);
            level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_HURT_ON_FIRE,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            level.sendParticles(ParticleTypes.LAVA,
                    target.getX(), target.getY(), target.getZ(),
                    6, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void applySwordHit(ServerPlayer player, LivingEntity target) {
        target.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        hurtWithNightfall(player, target, SWORD_DAMAGE);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), player);
        ServerLevel level = player.serverLevel();
        level.playSound(null, target.blockPosition(), SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,
                SoundSource.PLAYERS, 1.0F, 2.0F);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                target.getX(), target.getY() + 1.0D, target.getZ(),
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.REDSTONE_BLOCK)),
                target.getX(), target.getY() + 1.0D, target.getZ(),
                20, 0.0D, 0.0D, 0.0D, 0.3D);
    }

    private static void teleportBurst(ServerLevel level, Vec3 origin) {
        level.sendParticles(ParticleTypes.SQUID_INK,
                origin.x, origin.y + 1.2D, origin.z,
                15, 0.3D, 0.8D, 0.3D, 0.0D);
        level.playSound(null, net.minecraft.core.BlockPos.containing(origin),
                SoundEvents.SHULKER_TELEPORT, SoundSource.PLAYERS, 2.0F, 1.2F);
        for (int index = 0; index < 12; index++) {
            double angle = Math.toRadians(30.0D + index * 30.0D);
            double horizontal = Math.cos(angle) * 2.5D;
            double vertical = Math.sin(angle) * 2.5D;
            level.sendParticles(ParticleTypes.SQUID_INK,
                    origin.x + horizontal, origin.y + 1.4D + vertical, origin.z,
                    0, -Math.cos(angle), -Math.sin(angle), 0.0D, 0.45D);
            level.sendParticles(ParticleTypes.SQUID_INK,
                    origin.x, origin.y + 1.4D + vertical, origin.z + horizontal,
                    0, 0.0D, -Math.sin(angle), -Math.cos(angle), 0.45D);
        }
    }

    private static void lockCasterPosition(ServerPlayer player, CompoundTag state) {
        Vec3 locked = readVec(state, "CasterPosition");
        if (player.position().distanceToSqr(locked) > 1.0E-6D) {
            player.teleportTo(locked.x, locked.y, locked.z);
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
    }

    private static void moveLockedCaster(ServerPlayer player, CompoundTag state, double distance) {
        Vec3 direction = readVec(state, "Direction");
        Vec3 current = readVec(state, "CasterPosition");
        Vec3 destination = current.add(direction.scale(distance));
        writeVec(state, "CasterPosition", destination);
        player.teleportTo(destination.x, destination.y, destination.z);
    }

    private static LivingEntity findLookTarget(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        List<LivingEntity> targets = player.serverLevel().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(range),
                target -> isValidTarget(player, target) && player.hasLineOfSight(target));
        return targets.stream()
                .filter(target -> {
                    Vec3 toward = target.getBoundingBox().getCenter().subtract(eye);
                    return toward.lengthSqr() > 1.0E-4D
                            && toward.lengthSqr() <= range * range
                            && toward.normalize().dot(look) > 0.35D;
                })
                .min(Comparator.comparingDouble(target -> {
                    Vec3 toward = target.getBoundingBox().getCenter().subtract(eye);
                    double alignmentPenalty = 1.0D - toward.normalize().dot(look);
                    return alignmentPenalty * 24.0D + toward.length() * 0.08D;
                }))
                .orElse(null);
    }

    private static boolean withinTargetLockRange(ServerPlayer player, LivingEntity target) {
        return target.distanceToSqr(player) <= TARGET_LOCK_RANGE * TARGET_LOCK_RANGE;
    }

    private static Vec3 findCurrentFloor(ServerPlayer player) {
        Vec3 start = player.position().add(0.0D, 0.2D, 0.0D);
        double endY = Math.max(
                player.serverLevel().getMinBuildHeight() + 1.0D,
                start.y - 48.0D);
        BlockHitResult hit = player.serverLevel().clip(new ClipContext(
                start,
                new Vec3(start.x, endY, start.z),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player));
        if (hit.getType() == HitResult.Type.MISS) return null;
        return hit.getLocation().add(0.0D, 0.02D, 0.0D);
    }

    /**
     * The source selectors target players. The item adaptation intentionally
     * broadens this to all non-allied living entities, including modded bosses.
     */
    public static boolean isValidTarget(ServerPlayer owner, LivingEntity target) {
        if (target == owner || !target.isAlive() || target.isInvulnerable() || target instanceof ArmorStand) {
            return false;
        }
        if (owner.isAlliedTo(target) || target.isAlliedTo(owner)) return false;
        if (target instanceof Player other) {
            return !other.isCreative() && !other.isSpectator() && owner.canHarmPlayer(other);
        }
        return true;
    }

    private static void startCooldown(ServerPlayer player, int ticks) {
        player.getPersistentData().putInt(COOLDOWN_KEY, ticks);
        player.getCooldowns().addCooldown(ModItems.NIGHTFALL.get(), ticks);
    }

    private static void hurtWithNightfall(ServerPlayer player, LivingEntity target, float damage) {
        boolean wasAlive = target.isAlive();
        boolean damaged = target.hurt(player.damageSources().indirectMagic(player, player), damage);
        if (damaged) applyHitSaturation(player);
        if (wasAlive && damaged && !target.isAlive()) {
            reduceCooldownOnKill(player);
        }
    }

    public static void applyHitSaturation(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(
                MobEffects.SATURATION,
                HIT_SATURATION_DURATION,
                HIT_SATURATION_AMPLIFIER));
    }

    private static void reduceCooldownOnKill(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int remaining = data.getInt(COOLDOWN_KEY);
        if (remaining <= 0) return;

        int reduced = Math.max(0, remaining - KILL_COOLDOWN_REDUCTION);
        player.getCooldowns().removeCooldown(ModItems.NIGHTFALL.get());
        if (reduced > 0) {
            data.putInt(COOLDOWN_KEY, reduced);
            player.getCooldowns().addCooldown(ModItems.NIGHTFALL.get(), reduced);
        } else {
            data.remove(COOLDOWN_KEY);
        }
        player.displayClientMessage(
                Component.translatable(
                        "message.finalparadox.nightfall.kill_cooldown_reduction",
                        (reduced + 19) / 20),
                true);
    }

    private static void tickCooldown(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int remaining = data.getInt(COOLDOWN_KEY);
        if (remaining <= 0) return;
        remaining--;
        if (remaining <= 0) {
            data.remove(COOLDOWN_KEY);
            player.getCooldowns().removeCooldown(ModItems.NIGHTFALL.get());
            if (!data.contains(ACTIVE_KEY)) notifyReady(player);
        } else {
            data.putInt(COOLDOWN_KEY, remaining);
            if (!player.getCooldowns().isOnCooldown(ModItems.NIGHTFALL.get())) {
                player.getCooldowns().addCooldown(ModItems.NIGHTFALL.get(), remaining);
            }
        }
    }

    private static void notifyReady(ServerPlayer player) {
        player.displayClientMessage(Component.translatable("message.finalparadox.nightfall.ready"), true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 0.65F, 1.6F);
    }

    private static void capturePriorResistance(ServerPlayer player, CompoundTag state) {
        MobEffectInstance prior = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
        if (prior != null) {
            state.put(PRIOR_RESISTANCE_KEY, prior.save(new CompoundTag()));
        }
    }

    private static void maintainNightfallResistance(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                NIGHTFALL_RESISTANCE_REFRESH_TICKS,
                NIGHTFALL_RESISTANCE_AMPLIFIER,
                false,
                false,
                true));
    }

    private static void restorePriorResistance(ServerPlayer player, CompoundTag state) {
        MobEffectInstance current = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
        if (!player.isAlive()) {
            if (isNightfallResistance(current)) {
                player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            }
            return;
        }
        if (isNightfallResistance(current)) {
            CompoundTag currentTag = current.save(new CompoundTag());
            MobEffectInstance hidden = currentTag.contains("HiddenEffect", CompoundTag.TAG_COMPOUND)
                    ? MobEffectInstance.load(currentTag.getCompound("HiddenEffect"))
                    : null;
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            if (hidden != null && (hidden.isInfiniteDuration() || hidden.getDuration() > 0)) {
                player.addEffect(hidden);
                return;
            }
            restoreResistanceBackup(player, state);
            return;
        }

        // A stronger, longer or external-cleanse result is not owned here.
    }

    private static boolean isNightfallResistance(MobEffectInstance effect) {
        return effect != null
                && effect.getEffect() == MobEffects.DAMAGE_RESISTANCE
                && effect.getAmplifier() == NIGHTFALL_RESISTANCE_AMPLIFIER
                && !effect.isInfiniteDuration()
                && effect.getDuration() <= NIGHTFALL_RESISTANCE_REFRESH_TICKS
                && !effect.isAmbient()
                && !effect.isVisible()
                && effect.showIcon();
    }

    private static void restoreResistanceBackup(ServerPlayer player, CompoundTag state) {
        if (!state.contains(PRIOR_RESISTANCE_KEY, CompoundTag.TAG_COMPOUND)) return;
        CompoundTag backup = state.getCompound(PRIOR_RESISTANCE_KEY).copy();
        int duration = backup.getInt("Duration");
        if (duration != -1) {
            duration = Math.max(0, duration - state.getInt("Score"));
            if (duration == 0) return;
            backup.putInt("Duration", duration);
        }
        MobEffectInstance prior = MobEffectInstance.load(backup);
        if (prior != null) player.addEffect(prior);
    }

    private static Vec3 horizontalLook(ServerPlayer player) {
        Vec3 direction = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        return direction.lengthSqr() < 1.0E-4D
                ? Vec3.directionFromRotation(0.0F, player.getYRot())
                : direction.normalize();
    }

    private static float yawFromDirection(Vec3 direction) {
        return (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
    }

    private static InteractionHand abilityHand(CompoundTag state) {
        return state.getBoolean("Offhand") ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    private static void syncComboAnimation(ServerPlayer player, CompoundTag state, int score) {
        clearComboAnimation(player);
        ItemStack held = player.getItemInHand(abilityHand(state));
        if (!held.is(ModItems.NIGHTFALL.get())) return;
        CompoundTag tag = held.getOrCreateTag();
        tag.putInt(COMBO_ANIMATION_SCORE_TAG, Mth.clamp(score, 1, COMBO_END_SCORE));
        tag.putFloat(COMBO_ANIMATION_YAW_TAG, state.getFloat("Yaw"));
    }

    private static void clearComboAnimation(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            CompoundTag tag = stack.getTag();
            if (tag == null) continue;
            tag.remove(COMBO_ANIMATION_SCORE_TAG);
            tag.remove(COMBO_ANIMATION_YAW_TAG);
        }
    }

    private static void writeVec(CompoundTag tag, String key, Vec3 value) {
        CompoundTag vector = new CompoundTag();
        vector.putDouble("X", value.x);
        vector.putDouble("Y", value.y);
        vector.putDouble("Z", value.z);
        tag.put(key, vector);
    }

    private static Vec3 readVec(CompoundTag tag, String key) {
        CompoundTag vector = tag.getCompound(key);
        return new Vec3(vector.getDouble("X"), vector.getDouble("Y"), vector.getDouble("Z"));
    }

    private static boolean wasHit(ListTag hitTargets, UUID id) {
        for (int index = 0; index < hitTargets.size(); index++) {
            CompoundTag entry = hitTargets.getCompound(index);
            if (entry.hasUUID("Id") && entry.getUUID("Id").equals(id)) return true;
        }
        return false;
    }

    private static void rememberHit(ListTag hitTargets, UUID id) {
        CompoundTag entry = new CompoundTag();
        entry.putUUID("Id", id);
        hitTargets.add(entry);
    }

    private static boolean riftHitOnCooldown(ListTag hitTargets, UUID id, int score) {
        for (int index = 0; index < hitTargets.size(); index++) {
            CompoundTag entry = hitTargets.getCompound(index);
            if (entry.hasUUID("Id") && entry.getUUID("Id").equals(id)) {
                return score < entry.getInt("NextScore");
            }
        }
        return false;
    }

    private static void setRiftHitCooldown(ListTag hitTargets, UUID id, int nextScore) {
        for (int index = 0; index < hitTargets.size(); index++) {
            CompoundTag entry = hitTargets.getCompound(index);
            if (entry.hasUUID("Id") && entry.getUUID("Id").equals(id)) {
                entry.putInt("NextScore", nextScore);
                return;
            }
        }
        CompoundTag entry = new CompoundTag();
        entry.putUUID("Id", id);
        entry.putInt("NextScore", nextScore);
        hitTargets.add(entry);
    }

    private static DustParticleOptions dust(float red, float green, float blue, float scale) {
        return new DustParticleOptions(new Vector3f(red, green, blue), scale);
    }

    private static int[] chooseWarningPoints(RandomSource random) {
        int[] indices = new int[RIFT_WARNING_POINTS.size()];
        for (int index = 0; index < indices.length; index++) indices[index] = index;
        for (int index = indices.length - 1; index > 0; index--) {
            int swap = random.nextInt(index + 1);
            int value = indices[index];
            indices[index] = indices[swap];
            indices[swap] = value;
        }
        int[] selected = new int[22];
        System.arraycopy(indices, 0, selected, 0, selected.length);
        return selected;
    }

    private static List<Vec3> buildRiftWarningPoints() {
        List<Vec3> points = new ArrayList<>(62);
        addWarningLayer(points, 0.0D, 3.0D, 10);
        addWarningLayer(points, 0.927050983124842D, 2.85316954888546D, 8);
        addWarningLayer(points, -0.927050983124842D, 2.85316954888546D, 8);
        addWarningLayer(points, 1.76335575687742D, 2.42705098312484D, 7);
        addWarningLayer(points, -1.76335575687742D, 2.42705098312484D, 7);
        addWarningLayer(points, 2.42705098312484D, 1.76335575687742D, 6);
        addWarningLayer(points, -2.42705098312484D, 1.76335575687742D, 6);
        addWarningLayer(points, 2.85316954888546D, 0.927050983124842D, 5);
        addWarningLayer(points, -2.85316954888546D, 0.927050983124842D, 5);
        return List.copyOf(points);
    }

    private static void addWarningLayer(List<Vec3> points, double y, double radius, int count) {
        for (int index = 1; index <= count; index++) {
            double angle = Mth.TWO_PI * index / count;
            points.add(new Vec3(Math.sin(angle) * radius, y, Math.cos(angle) * radius));
        }
    }

    private static void discardSmallLaserProjectiles(ServerPlayer player, CompoundTag state) {
        ListTag projectiles = state.getList("SmallLaserProjectiles", CompoundTag.TAG_COMPOUND);
        for (int index = projectiles.size() - 1; index >= 0; index--) {
            discardSmallLaserProjectile(player, projectiles.getCompound(index));
        }
        state.remove("SmallLaserProjectiles");
        state.remove("SmallLaserHitCooldowns");
    }

    private static void discardSmallLaserProjectile(ServerPlayer player, CompoundTag projectile) {
        for (int index = 0; index < SMALL_LASER_PARTS; index++) {
            String key = "Part" + index;
            if (!projectile.hasUUID(key)) continue;
            UUID id = projectile.getUUID(key);
            for (ServerLevel level : player.server.getAllLevels()) {
                Entity raw = level.getEntity(id);
                if (raw == null) continue;
                PlayerTeam redTeam = level.getScoreboard().getPlayerTeam(SMALL_LASER_RED_TEAM);
                PlayerTeam blueTeam = level.getScoreboard().getPlayerTeam(SMALL_LASER_BLUE_TEAM);
                PlayerTeam currentTeam = level.getScoreboard().getPlayersTeam(raw.getScoreboardName());
                /*
                 * Scoreboard#removePlayerFromTeam throws if the supplied team
                 * is not the entity's current team. Remove exactly once from
                 * the actual Nightfall team; this also makes repeated cleanup
                 * after cancellation or logout harmless.
                 */
                if (currentTeam != null && (currentTeam == redTeam || currentTeam == blueTeam)) {
                    level.getScoreboard().removePlayerFromTeam(raw.getScoreboardName(), currentTeam);
                }
                raw.discard();
                break;
            }
        }
    }

    private static void discardLaserEntity(ServerPlayer player, CompoundTag state) {
        if (!state.hasUUID("LaserEntity")) return;
        UUID laserId = state.getUUID("LaserEntity");
        for (ServerLevel level : player.server.getAllLevels()) {
            Entity raw = level.getEntity(laserId);
            if (raw instanceof NightfallLaserEntity) {
                raw.discard();
                break;
            }
        }
        state.remove("LaserEntity");
    }

    private static void discardChainBladeEntity(ServerPlayer player, CompoundTag state) {
        if (!state.hasUUID("ChainBladeEntity")) return;
        UUID bladeId = state.getUUID("ChainBladeEntity");
        for (ServerLevel level : player.server.getAllLevels()) {
            Entity raw = level.getEntity(bladeId);
            if (raw instanceof NightfallChainBladeEntity) {
                raw.discard();
                break;
            }
        }
        state.remove("ChainBladeEntity");
    }
}
