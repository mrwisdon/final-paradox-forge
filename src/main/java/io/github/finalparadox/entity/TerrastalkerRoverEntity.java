package io.github.finalparadox.entity;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.item.AdaptiveDefenseMatrixItem;
import io.github.finalparadox.network.TerrastalkerMissilePacket;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-authoritative reconstruction of Final Paradox's player-operated
 * Terrastalker (`el_montura`). The B8 and improved-item variants share the
 * source construction but keep their different energy and damage rules.
 */
public final class TerrastalkerRoverEntity extends Entity {
    public static final int B8_MAX_ENERGY = 100;
    public static final int IMPROVED_MAX_ENERGY = 25;
    public static final int IMPROVED_COOLDOWN_TICKS = 20 * 180;
    public static final int SOURCE_VISIBLE_PARTS = 18;

    private static final int VARIANT_IMPROVED = 0;
    private static final int VARIANT_B8 = 1;
    private static final int MELTDOWN_TICKS = 100;
    private static final int FIRE_INTERVAL_TICKS = 3;
    private static final int MISSILE_MAGAZINE_CAP = 1;
    private static final int MISSILE_RESERVE_CAP = 6;
    private static final int MISSILE_LOAD_TICKS = 60;
    private static final int MISSILE_REGEN_TICKS = 200;
    private static final int MISSILE_MAX_LIFE = 100;
    private static final int MISSILE_BLOCK_LIMIT = 3;
    private static final double MISSILE_SPEED = 1.0D;
    private static final double MISSILE_BLAST_RADIUS = 3.0D;
    private static final float MISSILE_DAMAGE = 40.0F;
    private static final double MISSILE_HIT_DISTANCE = 1.0D;
    private static final double MISSILE_AIM_RANGE = 64.0D;
    private static final double MISSILE_AIM_HIT_DISTANCE = 1.5D;
    private static final int MISSILE_LAUNCH_TICKS = 6;
    private static final float MISSILE_MAX_TURN_DEGREES = 12.0F;
    private static final int BULLET_LIFETIME_TICKS = 30;
    private static final int IMPROVED_DRAIN_INTERVAL_TICKS = 19;
    private static final int COLLISION_COOLDOWN_TICKS = 10;
    private static final int DISMOUNT_WINDOW_TICKS = 10;
    private static final int DISMOUNT_RELEASE_TICKS = 10;
    private static final String FIRE_SCORE_KEY = "finalparadox.terrastalker_fire_score";
    private static final double MOVE_SPEED = 0.14D;
    private static final double BULLET_SPEED = 1.8D;
    private static final double BULLET_SAMPLE_RADIUS = 1.6D;
    private static final double TERRAIN_STEP = 0.2D;
    private static final double TERRAIN_PROBE_FORWARD = 0.42D;
    private static final double MAX_SUPPORTED_DROP = 3.0D;
    /** Player#getMyRidingOffset contributes -0.35, producing the tuned local y=1.4. */
    private static final double VEHICLE_RIDER_OFFSET = 1.75D;
    /** Independent rover-local muzzle height; passenger update timing must not move it. */
    private static final double FIRE_ORIGIN_LOCAL_Y = 1.95D;

    private static final TagKey<EntityType<?>> TARGETS = TagKey.create(
            net.minecraft.core.registries.Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(
                    FinalParadox.MOD_ID, "terrastalker_targets"));

    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(TerrastalkerRoverEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ENERGY =
            SynchedEntityData.defineId(TerrastalkerRoverEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_MOVING =
            SynchedEntityData.defineId(TerrastalkerRoverEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_FIRING =
            SynchedEntityData.defineId(TerrastalkerRoverEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_GAIT_FRAME =
            SynchedEntityData.defineId(TerrastalkerRoverEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_PREVIOUS_MOVEMENT_YAW =
            SynchedEntityData.defineId(TerrastalkerRoverEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_MOVEMENT_YAW =
            SynchedEntityData.defineId(TerrastalkerRoverEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PREVIOUS_TURRET_YAW =
            SynchedEntityData.defineId(TerrastalkerRoverEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_TURRET_YAW =
            SynchedEntityData.defineId(TerrastalkerRoverEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PREVIOUS_CANNON_PITCH =
            SynchedEntityData.defineId(TerrastalkerRoverEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_CANNON_PITCH =
            SynchedEntityData.defineId(TerrastalkerRoverEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PREVIOUS_CABIN_YAW =
            SynchedEntityData.defineId(TerrastalkerRoverEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_CABIN_YAW =
            SynchedEntityData.defineId(TerrastalkerRoverEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_MELTDOWN =
            SynchedEntityData.defineId(TerrastalkerRoverEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID ownerId;
    private UUID dismountedPlayerId;
    private boolean dismountMountLocked;
    private int dismountReleaseCounter;
    private int drainTicks;
    private int gaitScore;
    private int collisionCooldown;
    private int meltdownAge;
    private long previousDismountAttempt = Long.MIN_VALUE;
    private boolean allowDismount;
    private UUID fireInputOwnerId;
    private boolean fireInputHeld;
    private Vec3 recoveryPosition;
    private final List<RoverBullet> bullets = new ArrayList<>();
    private final List<RoverMissile> missiles = new ArrayList<>();
    private int missileLoaded = 1;
    private int missileReserve = MISSILE_RESERVE_CAP;
    private int missileLoadTicks = MISSILE_LOAD_TICKS;
    private int missileRegenTicks = MISSILE_REGEN_TICKS;

    public TerrastalkerRoverEntity(EntityType<TerrastalkerRoverEntity> type, Level level) {
        super(type, level);
        blocksBuilding = false;
        setMaxUpStep(0.0F);
    }

    public static boolean spawnImproved(ServerPlayer player) {
        if (player.getPersistentData().getInt(AdaptiveDefenseMatrixItem.COOLDOWN_KEY) > 0) {
            player.displayClientMessage(
                    Component.translatable("message.finalparadox.defense_matrix.cooldown"), true);
            return false;
        }
        if (hasBoss(player)) {
            player.displayClientMessage(
                    Component.translatable("message.finalparadox.defense_matrix.boss"), true);
            return false;
        }
        if (hasOwnedRover(player)) {
            player.displayClientMessage(
                    Component.translatable("message.finalparadox.rover.already_active"), true);
            return false;
        }

        TerrastalkerRoverEntity rover = create(
                player.serverLevel(), player.position(), VARIANT_IMPROVED);
        rover.ownerId = player.getUUID();
        if (!player.serverLevel().addFreshEntity(rover)) return false;
        if (!player.startRiding(rover, true)) {
            rover.discard();
            return false;
        }
        rover.onMounted(player);
        rover.emitImprovedSpawnEffect(player.serverLevel());
        rover.pushImprovedSpawnTargets(player.serverLevel(), player);
        player.getPersistentData().putInt(
                AdaptiveDefenseMatrixItem.COOLDOWN_KEY, IMPROVED_COOLDOWN_TICKS);
        return true;
    }

    /** Spawns the unoccupied mount used by the B8 encounter. */
    public static Optional<TerrastalkerRoverEntity> spawnEncounter(
            ServerPlayer source, Vec3 position) {
        return spawnEncounter(source, position, position);
    }

    /**
     * B8 controller entry point with the arena-translated cabin-collapse recovery
     * location. The standalone test command uses the spawn point as its fallback.
     */
    public static Optional<TerrastalkerRoverEntity> spawnEncounter(
            ServerPlayer source, Vec3 position, Vec3 recoveryPosition) {
        TerrastalkerRoverEntity rover = create(
                source.serverLevel(), position, VARIANT_B8);
        rover.recoveryPosition = recoveryPosition;
        if (!source.serverLevel().addFreshEntity(rover)) return Optional.empty();
        rover.emitEncounterSpawnEffect(source.serverLevel());
        return Optional.of(rover);
    }

    private static TerrastalkerRoverEntity create(
            ServerLevel level, Vec3 position, int variant) {
        TerrastalkerRoverEntity rover = new TerrastalkerRoverEntity(
                ModEntities.TERRASTALKER_ROVER.get(), level);
        rover.setPos(position.x, position.y, position.z);
        rover.setYRot(0.0F);
        rover.yRotO = 0.0F;
        rover.entityData.set(DATA_VARIANT, variant);
        rover.entityData.set(DATA_ENERGY,
                variant == VARIANT_B8 ? B8_MAX_ENERGY : IMPROVED_MAX_ENERGY);
        rover.recoveryPosition = position;
        return rover;
    }

    private static boolean hasOwnedRover(ServerPlayer player) {
        if (player.getVehicle() instanceof TerrastalkerRoverEntity) return true;
        for (Entity entity : player.serverLevel().getAllEntities()) {
            if (entity instanceof TerrastalkerRoverEntity rover
                    && !rover.isRemoved()
                    && player.getUUID().equals(rover.ownerId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasBoss(ServerPlayer player) {
        for (Entity entity : player.serverLevel().getAllEntities()) {
            if (entity instanceof LivingEntity living
                    && living.isAlive() && living.getTags().contains("boss")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_VARIANT, VARIANT_B8);
        entityData.define(DATA_ENERGY, B8_MAX_ENERGY);
        entityData.define(DATA_MOVING, false);
        entityData.define(DATA_FIRING, false);
        entityData.define(DATA_GAIT_FRAME, -1);
        entityData.define(DATA_PREVIOUS_MOVEMENT_YAW, 0.0F);
        entityData.define(DATA_MOVEMENT_YAW, 0.0F);
        entityData.define(DATA_PREVIOUS_TURRET_YAW, 0.0F);
        entityData.define(DATA_TURRET_YAW, 0.0F);
        entityData.define(DATA_PREVIOUS_CANNON_PITCH, 1.0F);
        entityData.define(DATA_CANNON_PITCH, 1.0F);
        entityData.define(DATA_PREVIOUS_CABIN_YAW, 0.0F);
        entityData.define(DATA_CABIN_YAW, 0.0F);
        entityData.define(DATA_MELTDOWN, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel server)) {
            discard();
            return;
        }

        capturePreviousVisualState();
        if (collisionCooldown > 0) collisionCooldown--;
        if (dismountMountLocked) {
            ServerPlayer dismounted = server.getServer().getPlayerList()
                    .getPlayer(dismountedPlayerId);
            if (dismounted == null) {
                dismountMountLocked = false;
                dismountReleaseCounter = 0;
            } else if (!dismounted.isShiftKeyDown()) {
                // A double-sneak gesture itself contains a brief release between
                // taps; only clear the re-mount lock after a sustained release.
                if (++dismountReleaseCounter >= DISMOUNT_RELEASE_TICKS) {
                    dismountMountLocked = false;
                    dismountReleaseCounter = 0;
                }
            } else {
                dismountReleaseCounter = 0;
            }
        }
        tickBullets(server);
        tickMissileMagazine();
        tickMissiles(server);

        if (isMeltingDown()) {
            tickMeltdown(server);
            return;
        }

        if (getFirstPassenger() instanceof ServerPlayer rider) {
            if (dismountMountLocked && rider.getUUID().equals(dismountedPlayerId)) {
                // The dismount did not stick (or an auto-mount raced in); force
                // the rider out again so /tp cannot be reverted to the cabin.
                allowDismount = true;
                try {
                    rider.stopRiding();
                } finally {
                    allowDismount = false;
                }
                rider.connection.send(new ClientboundSetPassengersPacket(this));
                return;
            }
            if (!rider.getUUID().equals(fireInputOwnerId)) {
                fireInputOwnerId = rider.getUUID();
                fireInputHeld = false;
            }
            ownerId = rider.getUUID();
            rider.removeEffect(MobEffects.BLINDNESS);
            if (isCabinBlocked(rider)) {
                handleCabinBlocked(rider);
                return;
            }
            tickControls(server, rider);
            MutableComponent missileLine = Component.translatable(
                    "message.finalparadox.rover.missile.display",
                    missileLoaded, missileReserve);
            if (missileLoaded <= 0) {
                missileLine = missileLine.append(Component.translatable(
                        "message.finalparadox.rover.missile.reloading"));
            }
            rider.displayClientMessage(Component.translatable(
                            "message.finalparadox.defense_matrix.energy", getEnergy())
                    .append(Component.literal("  "))
                    .append(missileLine), true);
            if (collisionCooldown == 0 && pushHostiles(server, rider)) {
                collisionCooldown = COLLISION_COOLDOWN_TICKS;
                reduceEnergy(2, true);
            }
        } else {
            clearFireInput();
            setMoving(false);
            setFiring(false);
            tryMountNearbyPlayer(server);
        }

        tickSourceMaintenance(server);
    }

    private boolean isCabinBlocked(ServerPlayer rider) {
        BlockPos feet = rider.blockPosition();
        return !level().getBlockState(feet).isAir()
                || !level().getBlockState(feet.above()).isAir();
    }

    private void handleCabinBlocked(ServerPlayer rider) {
        rider.sendSystemMessage(Component.translatable(
                "message.finalparadox.rover.cabin_blocked"));
        level().playSound(null, rider.blockPosition(), SoundEvents.TRIDENT_RETURN,
                SoundSource.MASTER, 1.0F, 1.7F);
        if (isImproved()) {
            forceDismount(rider);
            discard();
            return;
        }
        if (recoveryPosition != null) {
            setPos(recoveryPosition.x, recoveryPosition.y, recoveryPosition.z);
            setDeltaMovement(Vec3.ZERO);
            setMoving(false);
            setFiring(false);
        }
    }

    private void tickControls(ServerLevel server, ServerPlayer rider) {
        boolean firing = fireInputHeld && rider.getUUID().equals(fireInputOwnerId);
        boolean moving = rider.getMainHandItem().isEmpty();
        setFiring(firing);
        entityData.set(DATA_CABIN_YAW, rider.getYRot());
        entityData.set(DATA_TURRET_YAW, rider.getYRot());
        entityData.set(DATA_CANNON_PITCH, rider.getXRot() - 3.0F);

        float desiredMovementYaw = !firing && moving
                ? rider.getYRot() : getMovementYaw();
        Vec3 horizontal = moving
                ? horizontalDirection(desiredMovementYaw).scale(MOVE_SPEED)
                : Vec3.ZERO;
        boolean wasMoving = isMoving();
        if (moveLikeSource(horizontal, desiredMovementYaw)) {
            setYRot(desiredMovementYaw);
            entityData.set(DATA_MOVEMENT_YAW, desiredMovementYaw);
        }
        setMoving(moving);
        if (moving) {
            tickGait(server);
        } else if (wasMoving || getGaitFrame() < 0) {
            snapStoppedGait();
        }

        if (firing) {
            int fireScore = rider.getPersistentData().getInt(FIRE_SCORE_KEY) + 1;
            if (fireScore >= FIRE_INTERVAL_TICKS) {
                rider.getPersistentData().putInt(FIRE_SCORE_KEY, 0);
                fire(server, rider);
            } else {
                rider.getPersistentData().putInt(FIRE_SCORE_KEY, fireScore);
            }
        }

        if (isImproved()) {
            rider.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, 25, 10, true, false));
        }
    }

    /**
     * Advances the interpolation baseline even when no rider controls are run.
     * This lets an idle, obstructed, or melting-down rover converge instead of
     * replaying its final rotation transition on every rendered tick.
     */
    private void capturePreviousVisualState() {
        entityData.set(DATA_PREVIOUS_MOVEMENT_YAW, getMovementYaw());
        entityData.set(DATA_PREVIOUS_TURRET_YAW, getTurretYaw());
        entityData.set(DATA_PREVIOUS_CABIN_YAW, getCabinYaw());
        entityData.set(DATA_PREVIOUS_CANNON_PITCH, getCannonPitch());
    }

    private void tickSourceMaintenance(ServerLevel server) {
        drainTicks++;
        int interval = isImproved() ? IMPROVED_DRAIN_INTERVAL_TICKS : 20;
        if (drainTicks < interval) return;
        drainTicks = 0;

        if (getFirstPassenger() == null && !hasNearbyPlayer(server, 30.0D)) {
            explodeAndDiscard(server);
            return;
        }
        if (isImproved()) reduceEnergy(1, false);
        if (getEnergy() <= 0) beginMeltdown(server);
    }

    private boolean moveLikeSource(Vec3 horizontal, float movementYaw) {
        if (horizontal.horizontalDistanceSqr() < 0.000001D) {
            setDeltaMovement(Vec3.ZERO);
            return false;
        }

        Vec3 forwardProbe = horizontalDirection(movementYaw).scale(TERRAIN_PROBE_FORWARD);
        Vec3 probe = position().add(forwardProbe);
        if (!hasHeadClearance(probe, movementYaw) || !hasSupportWithinSourceDepth(probe)) {
            setDeltaMovement(Vec3.ZERO);
            return false;
        }

        Vec3 resolved = horizontal;
        Vec3 destination = position().add(horizontal);
        if (isSolidAt(destination)) {
            resolved = resolved.add(0.0D, TERRAIN_STEP, 0.0D);
        } else if (!isSolidAt(destination.add(0.0D, -TERRAIN_STEP, 0.0D))) {
            resolved = resolved.add(0.0D, -TERRAIN_STEP, 0.0D);
        }

        setDeltaMovement(resolved);
        setPos(getX() + resolved.x, getY() + resolved.y, getZ() + resolved.z);
        return true;
    }

    private boolean hasHeadClearance(Vec3 probe, float movementYaw) {
        Vec3 right = new Vec3(
                horizontalDirection(movementYaw).z, 0.0D,
                -horizontalDirection(movementYaw).x);
        for (int height = 1; height <= 3; height++) {
            for (double side : new double[]{-0.5D, 0.0D, 0.5D}) {
                if (isSolidAt(probe.add(right.scale(side)).add(0.0D, height, 0.0D))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasSupportWithinSourceDepth(Vec3 probe) {
        for (double depth = 1.0D; depth <= MAX_SUPPORTED_DROP; depth += 1.0D) {
            if (isSolidAt(probe.add(0.0D, -depth, 0.0D))) return true;
        }
        return false;
    }

    private boolean isSolidAt(Vec3 point) {
        BlockPos pos = BlockPos.containing(point);
        return !level().getBlockState(pos).getCollisionShape(level(), pos).isEmpty();
    }

    private void tickGait(ServerLevel server) {
        if (!isMoving()) return;
        gaitScore++;
        if (gaitScore == 5) {
            setGaitFrame(0);
            playGaitSound(server, true);
        } else if (gaitScore == 10) {
            setGaitFrame(1);
            playGaitSound(server, false);
            emitFootParticles(server, 0.7D, 1.8D);
            emitFootParticles(server, -1.6D, -0.7D);
        } else if (gaitScore == 15) {
            setGaitFrame(2);
            playGaitSound(server, true);
        } else if (gaitScore >= 20) {
            setGaitFrame(3);
            playGaitSound(server, false);
            emitFootParticles(server, -0.7D, 1.8D);
            emitFootParticles(server, 1.6D, -0.7D);
            gaitScore = 1;
        }
    }

    private void snapStoppedGait() {
        if (gaitScore <= 5) {
            setGaitFrame(0);
        } else if (gaitScore <= 10) {
            setGaitFrame(1);
        } else if (gaitScore <= 15) {
            setGaitFrame(2);
        } else if (gaitScore >= 20) {
            setGaitFrame(3);
        }
    }

    private void playGaitSound(ServerLevel server, boolean iron) {
        server.playSound(null, blockPosition(),
                iron ? SoundEvents.IRON_GOLEM_STEP : SoundEvents.NETHERITE_BLOCK_STEP,
                SoundSource.MASTER, 1.2F, iron ? 0.0F : 0.8F);
    }

    private void emitFootParticles(ServerLevel server, double localX, double localZ) {
        Vec3 offset = rotateLocal(localX, 0.1D, localZ, getYRot());
        server.sendParticles(
                new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SMOOTH_STONE_SLAB)),
                getX() + offset.x, getY() + offset.y, getZ() + offset.z,
                10, 0.1D, 0.0D, 0.15D, 0.15D);
    }

    private void fire(ServerLevel server, ServerPlayer rider) {
        Vec3 direction = rider.getLookAngle().normalize();
        Vec3 origin = position().add(direction.scale(1.6D))
                .add(0.0D, FIRE_ORIGIN_LOCAL_Y, 0.0D);
        bullets.add(new RoverBullet(origin, direction.scale(BULLET_SPEED), rider.getUUID()));
        server.sendParticles(ParticleTypes.LAVA, origin.x, origin.y, origin.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.playSound(null, BlockPos.containing(origin), SoundEvents.IRON_GOLEM_REPAIR,
                SoundSource.MASTER, 0.2F, 0.8F);
    }

    private void tickBullets(ServerLevel server) {
        List<RoverBullet> consumed = new ArrayList<>();
        Vec3 matrixHitCenter = null;
        for (RoverBullet bullet : bullets) {
            Vec3 next = bullet.position.add(bullet.velocity);
            BlockHitResult blockHit = level().clip(new ClipContext(
                    bullet.position, next, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, this));
            if (blockHit.getType() != HitResult.Type.MISS) {
                emitBlockImpact(server, blockHit.getLocation());
                if (isImproved() && level().getBlockState(blockHit.getBlockPos()).is(Blocks.SPAWNER)) {
                    level().destroyBlock(blockHit.getBlockPos(), true);
                }
                consumed.add(bullet);
                continue;
            }

            bullet.position = next;
            bullet.life++;
            server.sendParticles(ParticleTypes.END_ROD,
                    next.x, next.y, next.z, 0, 0.0D, 1.0D, 0.0D, 999999.0D);
            server.sendParticles(new DustParticleOptions(
                            new Vector3f(0.231F, 0.231F, 0.231F), 0.7F),
                    next.x, next.y, next.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);

            if (isEncounterMode()) {
                int bulletResult = B8EncounterManager.testBullet(server, next);
                if (bulletResult == B8EncounterController.BULLET_HIT) {
                    matrixHitCenter = B8EncounterManager.bulletHitCenter(server);
                    consumed.add(bullet);
                    continue;
                }
                if (bulletResult == B8EncounterController.BULLET_BLOCKED) {
                    consumed.add(bullet);
                    continue;
                }
                if (B8EncounterManager.testModuleHit(server, next)) {
                    consumed.add(bullet);
                    continue;
                }
            }

            LivingEntity target = findBulletTarget(server, next.add(0.0D, -1.0D, 0.0D));
            if (target != null) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1));
                if (isEncounterMode()) {
                    B8EncounterManager.markRoverHit(target, bullet.shooter, server.getGameTime());
                }
                target.hurt(server.damageSources().magic(), isImproved() ? 9.0F : 7.0F);
                emitEnemyImpact(server, next);
                server.playSound(null, target.blockPosition(), SoundEvents.SHROOMLIGHT_HIT,
                        SoundSource.MASTER, 2.0F, 2.0F);
                consumed.add(bullet);
                continue;
            }

            if (bullet.life >= BULLET_LIFETIME_TICKS) consumed.add(bullet);
        }
        // Source hit.mcfunction removes every bullet inside the 1.5-block zone.
        if (matrixHitCenter != null) {
            double radiusSqr = 1.5D * 1.5D;
            for (RoverBullet bullet : bullets) {
                if (bullet.position.distanceToSqr(matrixHitCenter) <= radiusSqr) {
                    consumed.add(bullet);
                }
            }
        }
        if (!consumed.isEmpty()) {
            bullets.removeAll(consumed);
        }
    }

    private LivingEntity findBulletTarget(ServerLevel server, Vec3 sample) {
        AABB triggerBounds = new AABB(sample, sample).inflate(BULLET_SAMPLE_RADIUS);
        boolean triggered = !server.getEntitiesOfClass(LivingEntity.class, triggerBounds,
                        target -> target.isAlive()
                                && !target.isInvulnerable()
                                && target != getFirstPassenger()
                                && isSourceHostile(target)
                                && target.distanceToSqr(sample)
                                <= BULLET_SAMPLE_RADIUS * BULLET_SAMPLE_RADIUS).isEmpty();
        if (!triggered) return null;
        return server.getEntitiesOfClass(LivingEntity.class,
                        new AABB(sample, sample).inflate(2.0D),
                        target -> target.isAlive()
                                && !target.isInvulnerable()
                                && target != getFirstPassenger()
                                && isSourceHostile(target)
                                && target.distanceToSqr(sample) <= 4.0D)
                .stream().min(Comparator.comparingDouble(target -> target.distanceToSqr(sample)))
                .orElse(null);
    }

    /**
     * Missile hook called from {@link TerrastalkerMissilePacket}. TOW-style
     * wire guidance: the missile does not lock an entity, it flies toward the
     * rider's current aim point (recomputed every tick while the rider stays
     * on board). It breaks up to 3 blocks on the way and explodes for 40 true
     * damage against every hostile creature within 3 blocks.
     */
    public void launchMissile(ServerPlayer rider) {
        if (level().isClientSide || !(level() instanceof ServerLevel server)) return;
        if (getFirstPassenger() != rider || isMeltingDown()) return;
        if (missileLoaded <= 0) {
            rider.sendSystemMessage(Component.translatable(
                    "message.finalparadox.rover.missile.cooldown"));
            return;
        }
        Vec3 direction = rider.getLookAngle().normalize();
        Vec3 origin = position().add(direction.scale(1.6D))
                .add(0.0D, FIRE_ORIGIN_LOCAL_Y, 0.0D);
        missiles.add(new RoverMissile(
                origin, direction.scale(MISSILE_SPEED),
                aimPoint(rider), rider.getUUID()));
        missileLoaded = 0;
        server.sendParticles(ParticleTypes.LAVA, origin.x, origin.y, origin.z,
                4, 0.0D, 0.0D, 0.0D, 0.0D);
        server.playSound(null, BlockPos.containing(origin),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.MASTER, 1.0F, 0.7F);
    }

    /** The point the rider is currently aiming at, capped at 64 blocks. */
    private static Vec3 aimPoint(ServerPlayer rider) {
        Vec3 eye = rider.getEyePosition();
        Vec3 look = rider.getLookAngle().normalize();
        if (rider.level() instanceof ServerLevel server) {
            BlockHitResult hit = server.clip(new ClipContext(
                    eye, eye.add(look.scale(MISSILE_AIM_RANGE)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, rider));
            if (hit.getType() != HitResult.Type.MISS) return hit.getLocation();
        }
        return eye.add(look.scale(MISSILE_AIM_RANGE));
    }

    /**
     * Magazine: 1 chambered round + up to 6 reserve. Every 10s the reserve
     * regains one round (cap 6); with an empty chamber and reserve available,
     * a 3s small reload chambers one round (front +1, reserve -1).
     */
    private void tickMissileMagazine() {
        if (missileReserve < MISSILE_RESERVE_CAP) {
            missileRegenTicks--;
            if (missileRegenTicks <= 0) {
                missileReserve++;
                missileRegenTicks = MISSILE_REGEN_TICKS;
            }
        }
        if (missileLoaded <= 0 && missileReserve > 0) {
            missileLoadTicks--;
            if (missileLoadTicks <= 0) {
                missileLoaded = 1;
                missileReserve--;
                missileLoadTicks = MISSILE_LOAD_TICKS;
            }
        }
    }

    private void tickMissiles(ServerLevel server) {
        if (missiles.isEmpty()) return;
        List<RoverMissile> consumed = new ArrayList<>();
        for (RoverMissile missile : missiles) {
            // Wire guidance: keep following the rider's current aim point
            // while they stay on board; otherwise finish toward the last one.
            ServerPlayer rider = getFirstPassenger() instanceof ServerPlayer p
                    && p.getUUID().equals(missile.shooter) ? p : null;
            if (rider != null && rider.isAlive()) {
                missile.aimPoint = aimPoint(rider);
            }
            if (missile.life >= MISSILE_LAUNCH_TICKS) {
                Vec3 delta = missile.aimPoint.subtract(missile.position);
                if (delta.lengthSqr() > 1.0E-6D) {
                    missile.velocity = limitTurn(
                            missile.velocity, delta.normalize(), MISSILE_MAX_TURN_DEGREES)
                            .scale(MISSILE_SPEED);
                }
            }
            Vec3 next = missile.position.add(missile.velocity);
            missile.position = next;
            missile.life++;
            spawnMissileTrail(server, next);

            if (hasHostileNear(server, next, MISSILE_HIT_DISTANCE)
                    || next.distanceToSqr(missile.aimPoint)
                    <= MISSILE_AIM_HIT_DISTANCE * MISSILE_AIM_HIT_DISTANCE) {
                missileExplosion(server, next, missile.shooter);
                consumed.add(missile);
                continue;
            }

            BlockPos block = BlockPos.containing(next);
            if (isBreakableMissileBlock(server, block)) {
                server.destroyBlock(block, true);
                missile.blocksBroken++;
                if (missile.blocksBroken >= MISSILE_BLOCK_LIMIT) {
                    missileExplosion(server, next, missile.shooter);
                    consumed.add(missile);
                    continue;
                }
            }

            if (missile.life >= MISSILE_MAX_LIFE) {
                missileExplosion(server, next, missile.shooter);
                consumed.add(missile);
            }
        }
        if (!consumed.isEmpty()) missiles.removeAll(consumed);
    }

    private boolean hasHostileNear(ServerLevel server, Vec3 point, double radius) {
        return !server.getEntitiesOfClass(LivingEntity.class,
                        new AABB(point, point).inflate(radius),
                        entity -> entity.isAlive()
                                && !entity.isInvulnerable()
                                && isSourceHostile(entity))
                .isEmpty();
    }

    /** Rotates {@code current} toward {@code desired} by at most the given angle. */
    private static Vec3 limitTurn(Vec3 current, Vec3 desired, float maxDegrees) {
        Vec3 u = current.normalize();
        Vec3 v = desired.normalize();
        double cos = Math.max(-1.0D, Math.min(1.0D, u.dot(v)));
        double angle = Math.acos(cos);
        double maxRad = Math.toRadians(maxDegrees);
        if (angle <= maxRad) return v;
        Vec3 axis;
        if (angle > Math.PI - 1.0E-4D) {
            Vec3 fallback = u.cross(new Vec3(0.0D, 1.0D, 0.0D));
            axis = fallback.lengthSqr() > 1.0E-6D
                    ? fallback : u.cross(new Vec3(1.0D, 0.0D, 0.0D));
        } else {
            axis = u.cross(v);
        }
        axis = axis.normalize();
        double s = Math.sin(maxRad);
        double c = Math.cos(maxRad);
        return u.scale(c)
                .add(axis.cross(u).scale(s))
                .add(axis.scale(axis.dot(u) * (1.0D - c)))
                .normalize();
    }

    private void spawnMissileTrail(ServerLevel server, Vec3 point) {
        server.sendParticles(ParticleTypes.CRIT,
                point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.2D);
        server.sendParticles(ParticleTypes.END_ROD,
                point.x, point.y, point.z, 0, 0.0D, 0.0D, 0.0D, 0.05D);
        server.sendParticles(ParticleTypes.FLAME,
                point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.02D);
    }

    private void missileExplosion(ServerLevel server, Vec3 point, UUID shooter) {
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.CLOUD,
                point.x, point.y, point.z, 20, 0.3D, 0.3D, 0.3D, 0.4D);
        server.sendParticles(ParticleTypes.FLAME,
                point.x, point.y, point.z, 14, 0.6D, 0.6D, 0.6D, 0.1D);
        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                point.x, point.y, point.z, 30, 0.5D, 0.5D, 0.5D, 0.1D);
        server.playSound(null, BlockPos.containing(point),
                SoundEvents.GENERIC_EXPLODE, SoundSource.MASTER, 2.0F, 2.0F);
        server.playSound(null, BlockPos.containing(point),
                SoundEvents.TRIDENT_HIT, SoundSource.MASTER, 2.0F, 0.6F);

        double radiusSqr = MISSILE_BLAST_RADIUS * MISSILE_BLAST_RADIUS;
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class,
                new AABB(point, point).inflate(MISSILE_BLAST_RADIUS),
                entity -> entity.isAlive() && !entity.isInvulnerable()
                        && isSourceHostile(entity))) {
            if (victim.distanceToSqr(point) > radiusSqr) continue;
            if (isEncounterMode()) {
                B8EncounterManager.markRoverHit(victim, shooter, server.getGameTime());
            }
            victim.hurt(server.damageSources().magic(), MISSILE_DAMAGE);
        }
    }

    private static boolean isBreakableMissileBlock(ServerLevel server, BlockPos pos) {
        BlockState state = server.getBlockState(pos);
        return !state.isAir()
                && state.getFluidState().isEmpty()
                && state.getDestroySpeed(server, pos) >= 0.0F;
    }

    private static boolean isSourceHostile(LivingEntity target) {
        return target.getType().is(TARGETS)
                || target.getType().getCategory() == MobCategory.MONSTER
                || target.getTags().contains("hostile");
    }

    private static void emitBlockImpact(ServerLevel server, Vec3 point) {
        server.sendParticles(new BlockParticleOption(
                        ParticleTypes.BLOCK, Blocks.QUARTZ_BLOCK.defaultBlockState()),
                point.x, point.y, point.z, 5, 0.0D, 0.0D, 0.0D, 0.2D);
    }

    private static void emitEnemyImpact(ServerLevel server, Vec3 point) {
        emitBlockImpact(server, point);
        server.sendParticles(ParticleTypes.CRIT, point.x, point.y + 1.5D, point.z,
                8, 0.0D, 0.0D, 0.0D, 1.0D);
    }

    private boolean pushHostiles(ServerLevel server, ServerPlayer rider) {
        boolean hit = false;
        for (LivingEntity target : server.getEntitiesOfClass(
                LivingEntity.class, getBoundingBox().inflate(2.5D),
                entity -> entity != rider && entity.isAlive()
                        && !entity.isInvulnerable()
                        && !entity.getTags().contains("detonante")
                        && !entity.getTags().contains("14_acechador_hitbox")
                        && isSourceHostile(entity))) {
            Vec3 away = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
            if (away.lengthSqr() > 0.0001D) {
                away = away.normalize().scale(0.9D).add(0.0D, 0.25D, 0.0D);
                target.setDeltaMovement(target.getDeltaMovement().add(away));
                target.hurtMarked = true;
            }
            server.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1.0D, target.getZ(),
                    20, 0.0D, 0.0D, 0.0D, 0.5D);
            hit = true;
        }
        if (hit) {
            server.playSound(null, blockPosition(), SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,
                    SoundSource.MASTER, 1.0F, 2.0F);
        }
        return hit;
    }

    private void tryMountNearbyPlayer(ServerLevel server) {
        List<ServerPlayer> candidates = server.getEntitiesOfClass(
                ServerPlayer.class, getBoundingBox().inflate(2.0D),
                player -> !player.isSpectator() && player.getVehicle() == null
                        && player.isShiftKeyDown()
                        && !(dismountMountLocked
                        && player.getUUID().equals(dismountedPlayerId)));
        if (candidates.isEmpty()) return;
        ServerPlayer rider = candidates.stream()
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if (rider != null && rider.startRiding(this, true)) {
            ownerId = rider.getUUID();
            onMounted(rider);
        }
    }

    private void onMounted(ServerPlayer rider) {
        previousDismountAttempt = Long.MIN_VALUE;
        fireInputOwnerId = rider.getUUID();
        fireInputHeld = false;
        // `subirse/ini` invokes frame/index immediately, before the scheduled
        // control loop. That always attempts one 0.14-block step and advances
        // danom to 1, even when the rider is holding an item. While sneaking,
        // the source keeps the leg core's previous yaw; otherwise a successful
        // first step adopts the player's yaw.
        if (gaitScore == 0) {
            boolean firing = rider.isShiftKeyDown();
            float desiredMovementYaw = firing ? getMovementYaw() : rider.getYRot();
            entityData.set(DATA_PREVIOUS_TURRET_YAW, rider.getYRot());
            entityData.set(DATA_TURRET_YAW, rider.getYRot());
            entityData.set(DATA_PREVIOUS_CABIN_YAW, rider.getYRot());
            entityData.set(DATA_CABIN_YAW, rider.getYRot());
            if (moveLikeSource(
                    horizontalDirection(desiredMovementYaw).scale(MOVE_SPEED),
                    desiredMovementYaw)) {
                setYRot(desiredMovementYaw);
                entityData.set(DATA_PREVIOUS_MOVEMENT_YAW, desiredMovementYaw);
                entityData.set(DATA_MOVEMENT_YAW, desiredMovementYaw);
            }
            gaitScore = 1;
        }
        level().playSound(null, blockPosition(), SoundEvents.ARMOR_EQUIP_NETHERITE,
                SoundSource.MASTER, 2.0F, 0.0F);
        rider.sendSystemMessage(Component.translatable("message.finalparadox.rover.controls.title"));
        rider.sendSystemMessage(Component.empty());
        rider.sendSystemMessage(Component.translatable("message.finalparadox.rover.controls.stop"));
        rider.sendSystemMessage(Component.translatable("message.finalparadox.rover.controls.move"));
        rider.sendSystemMessage(Component.translatable("message.finalparadox.rover.controls.fire"));
        rider.sendSystemMessage(Component.translatable("message.finalparadox.rover.controls.missile"));
        rider.sendSystemMessage(Component.translatable("message.finalparadox.rover.controls.dismount"));
    }

    private boolean hasNearbyPlayer(ServerLevel server, double radius) {
        return !server.getEntitiesOfClass(
                ServerPlayer.class, getBoundingBox().inflate(radius),
                player -> !player.isSpectator()).isEmpty();
    }

    public void damageEnergy(int amount) {
        if (level() instanceof ServerLevel server) {
            server.playSound(null, blockPosition(), SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,
                    SoundSource.MASTER, 2.0F, 0.9F);
        }
        reduceEnergy(amount, true);
    }

    /** True when any of this rover's bullets is within the given radius. */
    public boolean hasBulletsNear(Vec3 center, double radius) {
        double radiusSqr = radius * radius;
        for (RoverBullet bullet : bullets) {
            if (bullet.position.distanceToSqr(center) <= radiusSqr) return true;
        }
        return false;
    }

    private void reduceEnergy(int amount, boolean announce) {
        if (amount <= 0 || isMeltingDown()) return;
        entityData.set(DATA_ENERGY, Math.max(0, getEnergy() - amount));
        if (announce && getFirstPassenger() instanceof ServerPlayer rider) {
            if (isEncounterMode()) {
                // Source b8/danar_montura tellraw: "<!> -<amount>% energy" in red.
                rider.displayClientMessage(
                        Component.translatable("luisb1202.functions.bossfight.b8.danar_montura.1")
                                .withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD)
                                .append(Component.translatable("luisb1202.functions.bossfight.b8.danar_montura.2")
                                        .withStyle(net.minecraft.ChatFormatting.RED))
                                .append(Component.literal(String.valueOf(amount))
                                        .withStyle(net.minecraft.ChatFormatting.RED))
                                .append(Component.translatable("luisb1202.functions.bossfight.b8.danar_montura.3")
                                        .withStyle(net.minecraft.ChatFormatting.RED)),
                        true);
            } else {
                rider.displayClientMessage(Component.translatable(
                        "message.finalparadox.rover.energy_damage", amount), true);
            }
        }
    }

    private void beginMeltdown(ServerLevel server) {
        if (isMeltingDown()) return;
        entityData.set(DATA_MELTDOWN, true);
        meltdownAge = 0;
        setMoving(false);
        setFiring(false);
        if (getFirstPassenger() instanceof ServerPlayer rider) {
            if (isEncounterMode()) rider.kill();
            forceDismount(rider);
        }
        for (ServerPlayer player : server.getEntitiesOfClass(
                ServerPlayer.class, getBoundingBox().inflate(10.0D),
                player -> !player.isSpectator())) {
            player.sendSystemMessage(Component.translatable(
                    "message.finalparadox.rover.no_energy"));
            server.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RETURN,
                    SoundSource.MASTER, 1.0F, 1.7F);
        }
        server.playSound(null, blockPosition(), SoundEvents.CREEPER_PRIMED,
                SoundSource.MASTER, 1.0F, 0.0F);
    }

    private void tickMeltdown(ServerLevel server) {
        meltdownAge++;
        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                getX(), getY(), getZ(), 2, 0.5D, 0.5D, 0.5D, 0.0D);
        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                getX(), getY() + 1.0D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.2D);
        server.sendParticles(ParticleTypes.FLAME,
                getX(), getY() + 1.0D, getZ(), 1, 0.5D, 0.5D, 0.5D, 0.0D);
        if (meltdownAge % 5 == 1) {
            server.playSound(null, blockPosition(), SoundEvents.NOTE_BLOCK_BIT.value(),
                    SoundSource.MASTER, 1.0F, 1.0F);
        }
        if (meltdownAge >= MELTDOWN_TICKS) explodeAndDiscard(server);
    }

    private void explodeAndDiscard(ServerLevel server) {
        server.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE,
                SoundSource.MASTER, 1.0F, 1.4F);
        server.sendParticles(ParticleTypes.EXPLOSION,
                getX(), getY() + 1.5D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                getX(), getY() + 1.0D, getZ(), 60, 0.0D, 0.0D, 0.0D, 0.5D);
        server.sendParticles(ParticleTypes.LAVA,
                getX(), getY() + 1.0D, getZ(), 10, 0.0D, 0.0D, 0.0D, 0.5D);
        server.sendParticles(ParticleTypes.FLAME,
                getX(), getY() + 1.0D, getZ(), 60, 0.0D, 0.0D, 0.0D, 0.5D);
        server.sendParticles(new ItemParticleOption(
                        ParticleTypes.ITEM, new ItemStack(Items.SMOOTH_STONE_SLAB)),
                getX(), getY() + 1.5D, getZ(), 200, 0.0D, 0.0D, 0.0D, 0.3D);
        for (int index = 0; index < 36; index++) {
            double angle = Math.toRadians(10.0D + index * 10.0D);
            server.sendParticles(ParticleTypes.LARGE_SMOKE,
                    getX(), getY() - 0.5D, getZ(), 0,
                    Math.sin(angle), 0.0D, Math.cos(angle), 0.5D);
        }

        for (LivingEntity target : server.getEntitiesOfClass(
                LivingEntity.class, getBoundingBox().inflate(5.0D),
                entity -> entity.isAlive() && entity != getFirstPassenger())) {
            if (target instanceof Player) {
                target.hurt(server.damageSources().magic(), 6.0F);
            } else if (isSourceHostile(target)) {
                Vec3 away = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
                if (away.lengthSqr() > 0.0001D) {
                    away = away.normalize().scale(1.1D).add(0.0D, 0.3D, 0.0D);
                    target.setDeltaMovement(target.getDeltaMovement().add(away));
                    target.hurtMarked = true;
                }
                target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 40, 1, true, false));
                target.hurt(server.damageSources().magic(), 20.0F);
                target.setSecondsOnFire(8);
            }
        }
        discard();
    }

    private void emitEncounterSpawnEffect(ServerLevel server) {
        for (int index = 0; index < 32; index++) {
            double angle = Math.toRadians(11.25D + index * 11.25D);
            server.sendParticles(ParticleTypes.END_ROD,
                    getX(), getY(), getZ(), 0,
                    Math.sin(angle) * 3.0D, 0.0D,
                    Math.cos(angle) * 3.0D, 0.12D);
        }
        server.sendParticles(ParticleTypes.EXPLOSION,
                getX(), getY(), getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.FLASH,
                getX(), getY(), getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private void emitImprovedSpawnEffect(ServerLevel server) {
        double y = getY() + 1.5D;
        for (int index = 0; index < 32; index++) {
            double angle = Math.toRadians(251.25D + index * 11.25D);
            server.sendParticles(ParticleTypes.END_ROD,
                    getX(), y, getZ(), 0,
                    Math.sin(angle), 0.0D, Math.cos(angle), 0.5D);
        }
        server.sendParticles(ParticleTypes.FLASH,
                getX(), y, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.EXPLOSION,
                getX(), y, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.playSound(null, blockPosition(), SoundEvents.EVOKER_PREPARE_ATTACK,
                SoundSource.MASTER, 6.0F, 1.4F);
    }

    private void pushImprovedSpawnTargets(ServerLevel server, ServerPlayer owner) {
        for (LivingEntity target : server.getEntitiesOfClass(
                LivingEntity.class, getBoundingBox().inflate(8.0D),
                entity -> entity != owner && entity.isAlive() && isSourceHostile(entity))) {
            Vec3 away = target.position().subtract(owner.position()).multiply(1.0D, 0.0D, 1.0D);
            if (away.lengthSqr() > 0.0001D) {
                away = away.normalize().scale(1.1D).add(0.0D, 0.3D, 0.0D);
                target.setDeltaMovement(target.getDeltaMovement().add(away));
                target.hurtMarked = true;
            }
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
        }
    }

    public boolean shouldCancelDismount(Player player) {
        return !allowDismount;
    }

    public void onDismountAttempt(ServerPlayer player) {
        // Lock the mount only during an actual B8 combat phase. Other mod
        // bosses carry the "boss" tag, and a finished/abandoned B8 encounter
        // must never keep riders locked either.
        if (B8EncounterManager.inCombat(player.serverLevel())) {
            previousDismountAttempt = Long.MIN_VALUE;
            level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND,
                    SoundSource.MASTER, 2.0F, 2.0F);
            player.displayClientMessage(Component.translatable(
                    "message.finalparadox.rover.no_dismount"), true);
            return;
        }
        long now = level().getGameTime();
        if (previousDismountAttempt != Long.MIN_VALUE) {
            long elapsed = now - previousDismountAttempt;
            if (elapsed >= 0L && elapsed <= DISMOUNT_WINDOW_TICKS) {
                forceDismount(player);
                return;
            }
        }
        previousDismountAttempt = now;
    }

    private void forceDismount(ServerPlayer player) {
        previousDismountAttempt = Long.MIN_VALUE;
        clearFireInput();
        setFiring(false);
        dismountedPlayerId = player.getUUID();
        dismountMountLocked = true;
        dismountReleaseCounter = 0;
        allowDismount = true;
        try {
            player.stopRiding();
        } finally {
            allowDismount = false;
        }
        if (player.getVehicle() == this) {
            // The mount event can silently swallow the dismount; remove the
            // passenger directly as a fallback.
            allowDismount = true;
            try {
                removePassenger(player);
            } finally {
                allowDismount = false;
            }
        }
        // Place the rider behind the rover on the ground instead of leaving
        // them standing in the cabin (vanilla keeps the riding position).
        if (level() instanceof ServerLevel server) {
            // Force the client to clear its riding state even if the vanilla
            // passenger sync was missed; otherwise the camera yaw and strafing
            // stay locked and the player appears stuck in the cabin.
            player.connection.send(new ClientboundSetPassengersPacket(this));
            double yaw = Math.toRadians(getMovementYaw());
            double x = getX() + Math.sin(yaw) * 1.5D;
            double z = getZ() - Math.cos(yaw) * 1.5D;
            BlockPos ground = BlockPos.containing(x, getY(), z);
            while (ground.getY() > server.getMinBuildHeight() + 1
                    && server.getBlockState(ground)
                    .getCollisionShape(server, ground).isEmpty()) {
                ground = ground.below();
            }
            player.teleportTo(server, x, ground.getY() + 1.0D, z,
                    player.getYRot(), player.getXRot());
        }
        level().playSound(null, blockPosition(), SoundEvents.ARMOR_EQUIP_NETHERITE,
                SoundSource.MASTER, 2.0F, 0.0F);
    }

    public void prepareOwnerDisconnect(ServerPlayer player) {
        if (player.getUUID().equals(fireInputOwnerId)) clearFireInput();
        if (isImproved() && player.getUUID().equals(ownerId)) discard();
    }

    public void onFireInput(ServerPlayer player, boolean firing) {
        if (getFirstPassenger() != player) return;
        fireInputOwnerId = player.getUUID();
        fireInputHeld = firing;
    }

    private void clearFireInput() {
        fireInputOwnerId = null;
        fireInputHeld = false;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public double getPassengersRidingOffset() {
        return VEHICLE_RIDER_OFFSET;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty() && passenger instanceof Player;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(2.5D, 2.4D, 2.5D);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) ownerId = tag.getUUID("Owner");
        entityData.set(DATA_VARIANT, tag.getInt("Variant"));
        entityData.set(DATA_ENERGY, tag.getInt("Energy"));
        entityData.set(DATA_GAIT_FRAME, tag.getInt("GaitFrame"));
        float movementYaw = tag.contains("MovementYaw")
                ? tag.getFloat("MovementYaw") : getYRot();
        entityData.set(DATA_PREVIOUS_MOVEMENT_YAW, movementYaw);
        entityData.set(DATA_MOVEMENT_YAW, movementYaw);
        float turretYaw = tag.contains("TurretYaw")
                ? tag.getFloat("TurretYaw") : movementYaw;
        entityData.set(DATA_PREVIOUS_TURRET_YAW, turretYaw);
        entityData.set(DATA_TURRET_YAW, turretYaw);
        float cannonPitch = tag.getFloat("CannonPitch");
        entityData.set(DATA_PREVIOUS_CANNON_PITCH, cannonPitch);
        entityData.set(DATA_CANNON_PITCH, cannonPitch);
        float cabinYaw = tag.getFloat("CabinYaw");
        entityData.set(DATA_PREVIOUS_CABIN_YAW, cabinYaw);
        entityData.set(DATA_CABIN_YAW, cabinYaw);
        entityData.set(DATA_MELTDOWN, tag.getBoolean("Meltdown"));
        gaitScore = tag.getInt("GaitScore");
        drainTicks = tag.getInt("DrainTicks");
        missileLoaded = Math.max(0, Math.min(MISSILE_MAGAZINE_CAP,
                tag.contains("MissileLoaded") ? tag.getInt("MissileLoaded") : 1));
        missileReserve = Math.max(0, Math.min(MISSILE_RESERVE_CAP,
                tag.contains("MissileReserve") ? tag.getInt("MissileReserve")
                        : MISSILE_RESERVE_CAP));
        missileLoadTicks = tag.contains("MissileLoadTicks")
                ? tag.getInt("MissileLoadTicks") : MISSILE_LOAD_TICKS;
        missileRegenTicks = tag.contains("MissileRegenTicks")
                ? tag.getInt("MissileRegenTicks") : MISSILE_REGEN_TICKS;
        meltdownAge = tag.getInt("MeltdownAge");
        if (tag.contains("RecoveryX")) {
            recoveryPosition = new Vec3(
                    tag.getDouble("RecoveryX"),
                    tag.getDouble("RecoveryY"),
                    tag.getDouble("RecoveryZ"));
        }
        entityData.set(DATA_MOVING, false);
        entityData.set(DATA_FIRING, false);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) tag.putUUID("Owner", ownerId);
        tag.putInt("Variant", entityData.get(DATA_VARIANT));
        tag.putInt("Energy", getEnergy());
        tag.putInt("GaitFrame", getGaitFrame());
        tag.putFloat("MovementYaw", getMovementYaw());
        tag.putFloat("TurretYaw", getTurretYaw());
        tag.putFloat("CannonPitch", getCannonPitch());
        tag.putFloat("CabinYaw", getCabinYaw());
        tag.putBoolean("Meltdown", isMeltingDown());
        tag.putInt("GaitScore", gaitScore);
        tag.putInt("DrainTicks", drainTicks);
        tag.putInt("MissileLoaded", missileLoaded);
        tag.putInt("MissileReserve", missileReserve);
        tag.putInt("MissileLoadTicks", missileLoadTicks);
        tag.putInt("MissileRegenTicks", missileRegenTicks);
        tag.putInt("MeltdownAge", meltdownAge);
        if (recoveryPosition != null) {
            tag.putDouble("RecoveryX", recoveryPosition.x);
            tag.putDouble("RecoveryY", recoveryPosition.y);
            tag.putDouble("RecoveryZ", recoveryPosition.z);
        }
    }

    public int getEnergy() {
        return entityData.get(DATA_ENERGY);
    }

    public boolean isMoving() {
        return entityData.get(DATA_MOVING);
    }

    private void setMoving(boolean moving) {
        entityData.set(DATA_MOVING, moving);
    }

    public boolean isFiring() {
        return entityData.get(DATA_FIRING);
    }

    private void setFiring(boolean firing) {
        entityData.set(DATA_FIRING, firing);
    }

    public int getGaitFrame() {
        return entityData.get(DATA_GAIT_FRAME);
    }

    private void setGaitFrame(int frame) {
        entityData.set(DATA_GAIT_FRAME, frame);
    }

    public float getMovementYaw() {
        return entityData.get(DATA_MOVEMENT_YAW);
    }

    public float getPreviousMovementYaw() {
        return entityData.get(DATA_PREVIOUS_MOVEMENT_YAW);
    }

    public float getTurretYaw() {
        return entityData.get(DATA_TURRET_YAW);
    }

    public float getPreviousTurretYaw() {
        return entityData.get(DATA_PREVIOUS_TURRET_YAW);
    }

    public float getCannonPitch() {
        return entityData.get(DATA_CANNON_PITCH);
    }

    public float getPreviousCannonPitch() {
        return entityData.get(DATA_PREVIOUS_CANNON_PITCH);
    }

    public float getCabinYaw() {
        return entityData.get(DATA_CABIN_YAW);
    }

    public float getPreviousCabinYaw() {
        return entityData.get(DATA_PREVIOUS_CABIN_YAW);
    }

    public boolean isEncounterMode() {
        return entityData.get(DATA_VARIANT) == VARIANT_B8;
    }

    public boolean isImproved() {
        return entityData.get(DATA_VARIANT) == VARIANT_IMPROVED;
    }

    public boolean isMeltingDown() {
        return entityData.get(DATA_MELTDOWN);
    }

    private static Vec3 horizontalDirection(float yaw) {
        return new Vec3(-Math.sin(Math.toRadians(yaw)), 0.0D,
                Math.cos(Math.toRadians(yaw)));
    }

    private static Vec3 rotateLocal(double x, double y, double z, float yaw) {
        double radians = Math.toRadians(yaw);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        return new Vec3(x * cos - z * sin, y, x * sin + z * cos);
    }

    private static final class RoverBullet {
        private Vec3 position;
        private final Vec3 velocity;
        private final UUID shooter;
        private int life;

        private RoverBullet(Vec3 position, Vec3 velocity, UUID shooter) {
            this.position = position;
            this.velocity = velocity;
            this.shooter = shooter;
        }
    }

    private static final class RoverMissile {
        private Vec3 position;
        private Vec3 velocity;
        private Vec3 aimPoint;
        private final UUID shooter;
        private int life;
        private int blocksBroken;

        private RoverMissile(Vec3 position, Vec3 velocity, Vec3 aimPoint, UUID shooter) {
            this.position = position;
            this.velocity = velocity;
            this.aimPoint = aimPoint;
            this.shooter = shooter;
        }
    }
}
