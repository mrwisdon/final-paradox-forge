package io.github.finalparadox.entity;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.item.ReconDroneItem;
import io.github.finalparadox.network.DroneTracerPacket;
import io.github.finalparadox.network.ModNetwork;
import io.github.finalparadox.registry.ModEntities;
import io.github.finalparadox.registry.ModItems;
import io.github.finalparadox.registry.ModTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.network.NetworkHooks;

import java.util.Optional;
import java.util.UUID;

/**
 * Client-controlled flying vehicle used by the recon-drone item. The real
 * player rides this entity, so vanilla vehicle movement also moves the server
 * player and naturally drives chunk streaming around the drone.
 */
public final class DroneEntity extends Entity {
    public static final int FLAG_FORWARD = 1;
    public static final int FLAG_BACK = 2;
    public static final int FLAG_LEFT = 4;
    public static final int FLAG_RIGHT = 8;
    public static final int FLAG_UP = 16;
    public static final int FLAG_DOWN = 32;
    public static final int INPUT_MASK = FLAG_FORWARD | FLAG_BACK | FLAG_LEFT
            | FLAG_RIGHT | FLAG_UP | FLAG_DOWN;

    public static final int MAX_BOMBS = DroneBombReload.MAX_BOMBS;
    public static final int DEPLOY_COOLDOWN_TICKS = 20 * 180;
    public static final int BOMB_RELOAD_TICKS = DroneBombReload.RELOAD_TICKS;

    public static final int GATLING_IDLE = DroneGatlingCycle.IDLE;
    public static final int GATLING_WARMING = DroneGatlingCycle.WARMING;
    public static final int GATLING_FIRING = DroneGatlingCycle.FIRING;
    public static final int GATLING_OVERHEATED = DroneGatlingCycle.OVERHEATED;
    public static final int GATLING_WARMUP_TICKS = DroneGatlingCycle.WARMUP_TICKS;
    private static final float GATLING_DAMAGE_PER_RAY = 4.0F;

    private static final String ACTIVE_KEY = "finalparadox.recon_drone_active";
    private static final String ACTIVE_DRONE_UUID_KEY = "finalparadox.recon_drone_uuid";
    private static final String BODY_PROXY_UUID_KEY = "finalparadox.recon_drone_body_proxy_uuid";
    private static final String ANCHOR_X_KEY = "finalparadox.recon_drone_anchor_x";
    private static final String ANCHOR_Y_KEY = "finalparadox.recon_drone_anchor_y";
    private static final String ANCHOR_Z_KEY = "finalparadox.recon_drone_anchor_z";
    private static final String ANCHOR_DIMENSION_KEY = "finalparadox.recon_drone_anchor_dimension";
    private static final String WAS_INVISIBLE_KEY = "finalparadox.recon_drone_was_invisible";
    private static final String WAS_NO_GRAVITY_KEY = "finalparadox.recon_drone_was_no_gravity";
    private static final String BODY_DAMAGE_BYPASS_KEY =
            "finalparadox.recon_drone_body_damage_bypass";
    private static final String BOMB_RELOAD_PROGRESS_KEY =
            "finalparadox.recon_drone_bomb_reload_progress";

    private static final float MOVE_SPEED = 0.45F;
    private static final int ORPHAN_CLEANUP_TICKS = 20;
    private static final double RIDER_OFFSET = -2.67D;

    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_BOMBS =
            net.minecraft.network.syncher.SynchedEntityData.defineId(
                    DroneEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_GATLING_STATE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(
                    DroneEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);

    private UUID ownerId;
    private int clientInputState;
    private int orphanTicks;
    private double anchorX;
    private double anchorY;
    private double anchorZ;
    private boolean allowDismount;
    private boolean gatlingHeld;
    private int bombReloadProgressTicks;
    private DroneGatlingCycle.Snapshot gatlingCycle = DroneGatlingCycle.Snapshot.initial();

    private int remoteLerpSteps;
    private double remoteLerpX;
    private double remoteLerpY;
    private double remoteLerpZ;
    private float remoteLerpYRot;
    private float remoteLerpXRot;

    public DroneEntity(EntityType<DroneEntity> type, Level level) {
        super(type, level);
        noCulling = true;
        setNoGravity(true);
    }

    public static boolean deploy(ServerPlayer player) {
        if (isActiveFor(player)) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        Vec3 anchor = player.position();
        DroneBodyProxyEntity proxy = new DroneBodyProxyEntity(
                ModEntities.RECON_DRONE_BODY.get(), level);
        proxy.initializeFrom(player, anchor);
        if (!level.addFreshEntity(proxy) || !proxy.acquireAnchorTicket()) {
            proxy.discard();
            return false;
        }

        DroneEntity drone = new DroneEntity(ModEntities.RECON_DRONE.get(), level);
        Vec3 look = player.getLookAngle();
        Vec3 spawn = player.getEyePosition().add(look.scale(2.5D))
                .subtract(0.0D, 0.5D, 0.0D);
        drone.setPos(spawn);
        drone.setYRot(player.getYRot());
        drone.setXRot(player.getXRot());
        drone.ownerId = player.getUUID();
        drone.anchorX = anchor.x;
        drone.anchorY = anchor.y;
        drone.anchorZ = anchor.z;
        drone.entityData.set(DATA_BOMBS, MAX_BOMBS);
        if (!level.addFreshEntity(drone)) {
            proxy.discard();
            return false;
        }

        CompoundTag data = player.getPersistentData();
        data.putBoolean(ACTIVE_KEY, true);
        data.putUUID(ACTIVE_DRONE_UUID_KEY, drone.getUUID());
        data.putUUID(BODY_PROXY_UUID_KEY, proxy.getUUID());
        data.putDouble(ANCHOR_X_KEY, anchor.x);
        data.putDouble(ANCHOR_Y_KEY, anchor.y);
        data.putDouble(ANCHOR_Z_KEY, anchor.z);
        data.putString(ANCHOR_DIMENSION_KEY, level.dimension().location().toString());
        data.putBoolean(WAS_INVISIBLE_KEY, player.isInvisible());
        data.putBoolean(WAS_NO_GRAVITY_KEY, player.isNoGravity());

        if (!player.startRiding(drone, true)) {
            clearModeData(data);
            drone.discard();
            proxy.discard();
            return false;
        }

        player.setInvisible(true);
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);
        player.getCooldowns().addCooldown(ModItems.RECON_DRONE.get(), DEPLOY_COOLDOWN_TICKS);
        data.putInt(ReconDroneItem.COOLDOWN_KEY, DEPLOY_COOLDOWN_TICKS);

        level.playSound(null, drone.blockPosition(), SoundEvents.ARMOR_EQUIP_NETHERITE,
                SoundSource.PLAYERS, 1.0F, 0.8F);
        level.sendParticles(ParticleTypes.CLOUD, spawn.x, spawn.y, spawn.z,
                20, 0.3D, 0.2D, 0.3D, 0.05D);
        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                        "message.finalparadox.recon_drone.controls.title"), true);
        return true;
    }

    public static boolean isActiveFor(ServerPlayer player) {
        DroneEntity drone = findFor(player);
        return drone != null && player.getVehicle() == drone;
    }

    public static DroneEntity findFor(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(ACTIVE_KEY) || !data.hasUUID(ACTIVE_DRONE_UUID_KEY)) {
            return null;
        }
        Entity entity = player.serverLevel().getEntity(data.getUUID(ACTIVE_DRONE_UUID_KEY));
        if (entity instanceof DroneEntity drone
                && player.getUUID().equals(drone.ownerId)
                && !drone.isRemoved()) {
            return drone;
        }
        return null;
    }

    public static DroneBodyProxyEntity findBodyProxyFor(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.hasUUID(BODY_PROXY_UUID_KEY)) {
            return null;
        }
        ServerLevel anchorLevel = anchorLevel(player, data);
        Entity entity = anchorLevel == null
                ? null : anchorLevel.getEntity(data.getUUID(BODY_PROXY_UUID_KEY));
        return entity instanceof DroneBodyProxyEntity proxy
                && player.getUUID().equals(proxy.getOwnerId())
                && !proxy.isRemoved() ? proxy : null;
    }

    public static void endFor(ServerPlayer player) {
        finishFor(player, true);
    }

    public static void cancelFor(ServerPlayer player) {
        finishFor(player, false);
    }

    /** Ends stale or structurally invalid sessions from the server player tick. */
    public static void validateFor(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(ACTIVE_KEY)) {
            return;
        }
        DroneEntity drone = findFor(player);
        DroneBodyProxyEntity proxy = findBodyProxyFor(player);
        if (drone == null || proxy == null || player.getVehicle() != drone
                || player.serverLevel() != drone.level()) {
            finishFor(player, false);
        }
    }

    public static boolean shouldBlockOwnerDamage(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        return data.getBoolean(ACTIVE_KEY)
                && !data.getBoolean(BODY_DAMAGE_BYPASS_KEY);
    }

    public static boolean forwardBodyDamage(
            ServerPlayer player, DroneBodyProxyEntity proxy,
            DamageSource source, float amount) {
        if (!isActiveFor(player) || findBodyProxyFor(player) != proxy || amount <= 0.0F) {
            return false;
        }
        CompoundTag data = player.getPersistentData();
        data.putBoolean(BODY_DAMAGE_BYPASS_KEY, true);
        try {
            return player.hurt(source, amount);
        } finally {
            data.remove(BODY_DAMAGE_BYPASS_KEY);
        }
    }

    private static void finishFor(ServerPlayer player, boolean notify) {
        CompoundTag data = player.getPersistentData();
        boolean ownsMountedDrone = player.getVehicle() instanceof DroneEntity mountedDrone
                && player.getUUID().equals(mountedDrone.ownerId);
        if (!hasSessionData(data) && !ownsMountedDrone) {
            return;
        }
        DroneEntity drone = findFor(player);
        if (drone == null && player.getVehicle() instanceof DroneEntity mounted
                && player.getUUID().equals(mounted.ownerId)) {
            drone = mounted;
        }
        DroneBodyProxyEntity proxy = findBodyProxyFor(player);

        double returnX = data.contains(ANCHOR_X_KEY) ? data.getDouble(ANCHOR_X_KEY) : player.getX();
        double returnY = data.contains(ANCHOR_Y_KEY) ? data.getDouble(ANCHOR_Y_KEY) : player.getY();
        double returnZ = data.contains(ANCHOR_Z_KEY) ? data.getDouble(ANCHOR_Z_KEY) : player.getZ();
        ServerLevel returnLevel = anchorLevel(player, data);
        boolean wasInvisible = data.getBoolean(WAS_INVISIBLE_KEY);
        boolean wasNoGravity = data.getBoolean(WAS_NO_GRAVITY_KEY);
        UUID proxyId = data.hasUUID(BODY_PROXY_UUID_KEY)
                ? data.getUUID(BODY_PROXY_UUID_KEY) : null;

        if (drone != null) {
            drone.allowDismount = true;
            if (player.connection.connection.isConnected()) {
                ModNetwork.sendDroneDismount(player, drone.getId());
            }
        }
        try {
            if (player.getVehicle() instanceof DroneEntity) {
                player.stopRiding();
            }
        } finally {
            if (drone != null) {
                drone.allowDismount = false;
                if (player.connection.connection.isConnected()) {
                    player.connection.send(new ClientboundSetPassengersPacket(drone));
                }
            }
        }

        clearModeData(data);
        player.setInvisible(wasInvisible);
        player.setNoGravity(wasNoGravity);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;

        if (returnLevel != null) {
            if (player.connection.connection.isConnected()
                    || returnLevel != player.serverLevel()) {
                player.teleportTo(returnLevel, returnX, returnY, returnZ,
                        player.getYRot(), player.getXRot());
            } else {
                player.setPos(returnX, returnY, returnZ);
            }
        }

        if (proxy == null && proxyId != null && returnLevel != null
                && returnLevel.getEntity(proxyId) instanceof DroneBodyProxyEntity loadedProxy) {
            proxy = loadedProxy;
        }

        if (proxyId != null && returnLevel != null) {
            ChunkPos anchorChunk = new ChunkPos(Mth.floor(returnX) >> 4, Mth.floor(returnZ) >> 4);
            ForgeChunkManager.forceChunk(returnLevel, FinalParadox.MOD_ID, proxyId,
                    anchorChunk.x, anchorChunk.z, false, true);
        }
        if (proxy != null) {
            proxy.discard();
        }
        if (drone != null) {
            if (notify) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "message.finalparadox.recon_drone.exited"), true);
                if (drone.level() instanceof ServerLevel server) {
                    server.playSound(null, drone.blockPosition(),
                            SoundEvents.ARMOR_EQUIP_LEATHER,
                            SoundSource.PLAYERS, 1.0F, 1.4F);
                }
            }
            drone.discard();
        }
    }

    private static ServerLevel anchorLevel(ServerPlayer player, CompoundTag data) {
        ResourceLocation location = ResourceLocation.tryParse(
                data.getString(ANCHOR_DIMENSION_KEY));
        if (location == null) {
            return player.serverLevel();
        }
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, location);
        ServerLevel level = player.getServer().getLevel(dimension);
        return level == null ? player.serverLevel() : level;
    }

    private static void clearModeData(CompoundTag data) {
        data.remove(ACTIVE_KEY);
        data.remove(ACTIVE_DRONE_UUID_KEY);
        data.remove(BODY_PROXY_UUID_KEY);
        data.remove(ANCHOR_X_KEY);
        data.remove(ANCHOR_Y_KEY);
        data.remove(ANCHOR_Z_KEY);
        data.remove(ANCHOR_DIMENSION_KEY);
        data.remove(WAS_INVISIBLE_KEY);
        data.remove(WAS_NO_GRAVITY_KEY);
        data.remove(BODY_DAMAGE_BYPASS_KEY);
    }

    private static boolean hasSessionData(CompoundTag data) {
        return data.contains(ACTIVE_KEY)
                || data.contains(ACTIVE_DRONE_UUID_KEY)
                || data.contains(BODY_PROXY_UUID_KEY)
                || data.contains(ANCHOR_X_KEY)
                || data.contains(ANCHOR_Y_KEY)
                || data.contains(ANCHOR_Z_KEY)
                || data.contains(ANCHOR_DIMENSION_KEY)
                || data.contains(WAS_INVISIBLE_KEY)
                || data.contains(WAS_NO_GRAVITY_KEY);
    }

    public boolean dropBomb(ServerPlayer player) {
        if (!isControlledBy(player)) {
            return false;
        }
        int bombs = getBombs();
        if (bombs <= 0) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.finalparadox.recon_drone.no_bombs"), true);
            return false;
        }
        DroneBombEntity bomb = new DroneBombEntity(
                ModEntities.RECON_DRONE_BOMB.get(), level());
        bomb.setPos(getX(), getY() - 0.2D, getZ());
        bomb.setOwner(player.getUUID());
        if (!level().addFreshEntity(bomb)) {
            return false;
        }
        DroneBombReload.Snapshot reload = DroneBombReload.afterDrop(
                new DroneBombReload.Snapshot(bombs, bombReloadProgressTicks));
        entityData.set(DATA_BOMBS, reload.bombs());
        bombReloadProgressTicks = reload.progressTicks();
        level().playSound(null, blockPosition(), SoundEvents.TNT_PRIMED,
                SoundSource.PLAYERS, 1.0F, 0.9F);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SMOKE, getX(), getY() - 0.2D, getZ(),
                    8, 0.1D, 0.1D, 0.1D, 0.02D);
        }
        return true;
    }

    public int getBombs() {
        return entityData.get(DATA_BOMBS);
    }

    public int getGatlingState() {
        return entityData.get(DATA_GATLING_STATE);
    }

    /** Accepts only the owner of the current, active mounted session. */
    public void setGatlingHeld(ServerPlayer player, boolean held) {
        if (!isControlledBy(player) || !player.isAlive()) {
            return;
        }
        if (gatlingHeld == held) {
            return;
        }
        gatlingHeld = held;
        if (!held && gatlingCycle.state() != GATLING_OVERHEATED) {
            gatlingCycle = new DroneGatlingCycle.Snapshot(
                    GATLING_IDLE, 0, gatlingCycle.heat(), 0, 0);
            entityData.set(DATA_GATLING_STATE, GATLING_IDLE);
        }
    }

    /** Client-only input state; movement is sent through vanilla vehicle packets. */
    public void setClientInput(int state) {
        clientInputState = state & INPUT_MASK;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (isControlledByLocalInstance()) {
                LivingEntity controller = getControllingPassenger();
                if (controller != null) {
                    setYRot(controller.getYRot());
                    setXRot(controller.getXRot());
                    applyClientMovement(clientInputState, getYRot());
                }
            } else {
                tickRemoteInterpolation();
            }
            return;
        }

        if (!(level() instanceof ServerLevel server)) {
            discard();
            return;
        }
        ServerPlayer owner = ownerId == null
                ? null : server.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null) {
            if (++orphanTicks > ORPHAN_CLEANUP_TICKS) {
                discard();
            }
            return;
        }
        orphanTicks = 0;
        if (!isControlledBy(owner)) {
            resetGatling();
            finishFor(owner, false);
            if (!isRemoved()) {
                discard();
            }
            return;
        }
        tickGatling(server, owner);
        tickBombReload(owner);
    }

    private void tickGatling(ServerLevel server, ServerPlayer owner) {
        if (!owner.isAlive()) {
            resetGatling();
            return;
        }
        DroneGatlingCycle.Step step = DroneGatlingCycle.advance(gatlingHeld, gatlingCycle);
        gatlingCycle = step.snapshot();
        entityData.set(DATA_GATLING_STATE, gatlingCycle.state());
        if (step.overheatedStarted()) {
            beginGatlingOverheat(server, owner);
        } else if (gatlingCycle.state() == GATLING_OVERHEATED
                && tickCount % 5 == 0) {
            emitGatlingOverheatSmoke(server, 2);
        }
        if (step.fire()) {
            fireGatlingSalvo(server, owner);
        }
    }

    private void resetGatling() {
        gatlingHeld = false;
        gatlingCycle = DroneGatlingCycle.Snapshot.initial();
        if (entityData.get(DATA_GATLING_STATE) != GATLING_IDLE) {
            entityData.set(DATA_GATLING_STATE, GATLING_IDLE);
        }
    }

    private void tickBombReload(ServerPlayer owner) {
        if (!owner.isAlive()) {
            return;
        }
        DroneBombReload.Snapshot reload = DroneBombReload.tick(
                new DroneBombReload.Snapshot(getBombs(), bombReloadProgressTicks));
        if (reload.bombs() != getBombs()) {
            entityData.set(DATA_BOMBS, reload.bombs());
        }
        bombReloadProgressTicks = reload.progressTicks();
    }

    private void fireGatlingSalvo(ServerLevel server, ServerPlayer owner) {
        Vec3 aim = Vec3.directionFromRotation(getXRot(), getYRot()).normalize();
        Vec3 right = Vec3.directionFromRotation(0.0F, getYRot() + 90.0F).normalize();
        DroneGatlingBallistics.Vector3 center = DroneGatlingBallistics.muzzleCenter(
                vector(position()), vector(aim));
        DroneGatlingBallistics.Vector3 convergence =
                DroneGatlingBallistics.convergencePoint(center, vector(aim));
        double spreadDegrees = DroneGatlingBallistics.spreadDegrees(gatlingCycle.heat());

        DroneGatlingBallistics.Vector3 leftStart = DroneGatlingBallistics.muzzle(
                center, vector(right), false);
        DroneGatlingBallistics.Vector3 rightStart = DroneGatlingBallistics.muzzle(
                center, vector(right), true);
        DroneGatlingBallistics.Vector3 leftDirection = DroneGatlingBallistics.spreadDirection(
                DroneGatlingBallistics.convergingDirection(leftStart, convergence),
                spreadDegrees, random.nextDouble(), random.nextDouble());
        DroneGatlingBallistics.Vector3 rightDirection = DroneGatlingBallistics.spreadDirection(
                DroneGatlingBallistics.convergingDirection(rightStart, convergence),
                spreadDegrees, random.nextDouble(), random.nextDouble());

        GatlingRay left = traceGatlingRay(server, owner, vec(leftStart), vec(leftDirection));
        GatlingRay rightRay = traceGatlingRay(
                server, owner, vec(rightStart), vec(rightDirection));
        ModNetwork.sendDroneTracer(this, new DroneTracerPacket(
                tracer(left), tracer(rightRay)));
    }

    private GatlingRay traceGatlingRay(
            ServerLevel server, ServerPlayer owner, Vec3 muzzle, Vec3 direction) {
        Vec3 fullEnd = muzzle.add(direction.scale(DroneGatlingBallistics.RANGE));
        BlockHitResult blockHit = server.clip(new ClipContext(
                muzzle, fullEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        boolean struckBlock = blockHit.getType() != HitResult.Type.MISS;
        Vec3 clippedEnd = struckBlock ? blockHit.getLocation() : fullEnd;

        LivingEntity target = null;
        Vec3 targetPoint = null;
        double nearestDistanceSqr = muzzle.distanceToSqr(clippedEnd);
        AABB searchBox = new AABB(muzzle, clippedEnd).inflate(1.0D);
        for (LivingEntity candidate : server.getEntitiesOfClass(
                LivingEntity.class, searchBox,
                entity -> isGatlingTarget(owner, entity))) {
            AABB candidateBox = candidate.getBoundingBox();
            Optional<Vec3> intersection = candidateBox.contains(muzzle)
                    ? Optional.of(muzzle) : candidateBox.clip(muzzle, clippedEnd);
            if (intersection.isEmpty()) {
                continue;
            }
            double distanceSqr = muzzle.distanceToSqr(intersection.get());
            if (distanceSqr < nearestDistanceSqr) {
                target = candidate;
                targetPoint = intersection.get();
                nearestDistanceSqr = distanceSqr;
            }
        }

        Vec3 impact = targetPoint == null ? clippedEnd : targetPoint;
        int impactType = struckBlock
                ? DroneTracerPacket.IMPACT_BLOCK : DroneTracerPacket.IMPACT_MISS;
        if (target != null) {
            impactType = DroneTracerPacket.IMPACT_ENTITY;
            int priorInvulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            try {
                target.hurt(owner.damageSources().playerAttack(owner), GATLING_DAMAGE_PER_RAY);
            } finally {
                target.invulnerableTime = Math.max(
                        priorInvulnerableTime, target.invulnerableTime);
            }
        }
        return new GatlingRay(muzzle, impact, impactType);
    }

    private boolean isGatlingTarget(ServerPlayer owner, LivingEntity target) {
        if (target == owner || getPassengers().contains(target) || !target.isAlive()
                || target.isInvulnerable()) {
            return false;
        }
        return target.getType().getCategory() == MobCategory.MONSTER
                || target.getType().is(ModTags.EntityTypes.GLAIVORUS_TARGETS);
    }

    private void beginGatlingOverheat(ServerLevel server, ServerPlayer owner) {
        emitGatlingOverheatSmoke(server, 10);
        server.playSound(null, blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 0.9F, 0.75F);
        owner.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                        "message.finalparadox.recon_drone.overheated"), true);
    }

    private void emitGatlingOverheatSmoke(ServerLevel server, int count) {
        Vec3 aim = Vec3.directionFromRotation(getXRot(), getYRot()).normalize();
        Vec3 right = Vec3.directionFromRotation(0.0F, getYRot() + 90.0F).normalize();
        DroneGatlingBallistics.Vector3 center = DroneGatlingBallistics.muzzleCenter(
                vector(position()), vector(aim));
        Vec3 left = vec(DroneGatlingBallistics.muzzle(center, vector(right), false));
        Vec3 rightMuzzle = vec(DroneGatlingBallistics.muzzle(center, vector(right), true));
        for (Vec3 muzzle : new Vec3[]{left, rightMuzzle}) {
            server.sendParticles(ParticleTypes.SMOKE,
                    muzzle.x, muzzle.y, muzzle.z, count,
                    0.06D, 0.06D, 0.06D, 0.015D);
        }
    }

    private static DroneGatlingBallistics.Vector3 vector(Vec3 value) {
        return new DroneGatlingBallistics.Vector3(value.x, value.y, value.z);
    }

    private static Vec3 vec(DroneGatlingBallistics.Vector3 value) {
        return new Vec3(value.x(), value.y(), value.z());
    }

    private static DroneTracerPacket.Trace tracer(GatlingRay ray) {
        return new DroneTracerPacket.Trace(
                ray.start.x, ray.start.y, ray.start.z,
                ray.impact.x, ray.impact.y, ray.impact.z,
                ray.impactType);
    }

    private record GatlingRay(Vec3 start, Vec3 impact, int impactType) {
    }

    private void applyClientMovement(int state, float yRot) {
        Vec3 move = Vec3.ZERO;
        Vec3 forward = Vec3.directionFromRotation(0.0F, yRot);
        Vec3 right = Vec3.directionFromRotation(0.0F, yRot + 90.0F);
        if ((state & FLAG_FORWARD) != 0) move = move.add(forward);
        if ((state & FLAG_BACK) != 0) move = move.subtract(forward);
        if ((state & FLAG_LEFT) != 0) move = move.subtract(right);
        if ((state & FLAG_RIGHT) != 0) move = move.add(right);
        if ((state & FLAG_UP) != 0) move = move.add(0.0D, 1.0D, 0.0D);
        if ((state & FLAG_DOWN) != 0) move = move.subtract(0.0D, 1.0D, 0.0D);

        if (move.lengthSqr() <= 0.0D) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        Vec3 horizontal = new Vec3(move.x, 0.0D, move.z);
        Vec3 resolved = horizontal.lengthSqr() > 0.0D
                ? horizontal.normalize().scale(MOVE_SPEED)
                    .add(0.0D, move.y * MOVE_SPEED, 0.0D)
                : new Vec3(0.0D, move.y * MOVE_SPEED, 0.0D);
        setDeltaMovement(resolved);
        move(MoverType.SELF, resolved);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot,
                       int steps, boolean teleport) {
        if (!level().isClientSide) {
            super.lerpTo(x, y, z, yRot, xRot, steps, teleport);
            return;
        }
        if (isControlledByLocalInstance()) {
            return;
        }
        remoteLerpX = x;
        remoteLerpY = y;
        remoteLerpZ = z;
        remoteLerpYRot = yRot;
        remoteLerpXRot = xRot;
        remoteLerpSteps = Math.max(1, steps);
    }

    private void tickRemoteInterpolation() {
        if (remoteLerpSteps <= 0) return;
        double fraction = 1.0D / remoteLerpSteps;
        setPos(Mth.lerp(fraction, getX(), remoteLerpX),
                Mth.lerp(fraction, getY(), remoteLerpY),
                Mth.lerp(fraction, getZ(), remoteLerpZ));
        setYRot(Mth.rotLerp((float) fraction, getYRot(), remoteLerpYRot));
        setXRot((float) Mth.lerp(fraction, getXRot(), remoteLerpXRot));
        --remoteLerpSteps;
    }

    private boolean isControlledBy(ServerPlayer player) {
        return player.getUUID().equals(ownerId)
                && player.getPersistentData().getBoolean(ACTIVE_KEY)
                && player.getVehicle() == this;
    }

    public boolean shouldCancelDismount(Player player) {
        return !allowDismount && getPassengers().contains(player);
    }

    /** Applies the server's explicit exit authorization on the owning client. */
    public void confirmClientDismount(Player player) {
        allowDismount = true;
        try {
            if (player.getVehicle() == this) {
                player.stopRiding();
            }
        } finally {
            allowDismount = false;
        }
    }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity passenger = getFirstPassenger();
        return passenger instanceof LivingEntity living ? living : null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty() && passenger instanceof Player;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public double getPassengersRidingOffset() {
        return RIDER_OFFSET;
    }

    @Override
    public boolean dismountsUnderwater() {
        return false;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
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
    protected void defineSynchedData() {
        entityData.define(DATA_BOMBS, MAX_BOMBS);
        entityData.define(DATA_GATLING_STATE, GATLING_IDLE);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) ownerId = tag.getUUID("Owner");
        DroneBombReload.Snapshot reload = DroneBombReload.normalize(
                tag.getInt("Bombs"), tag.getInt(BOMB_RELOAD_PROGRESS_KEY));
        entityData.set(DATA_BOMBS, reload.bombs());
        bombReloadProgressTicks = reload.progressTicks();
        anchorX = tag.getDouble("AnchorX");
        anchorY = tag.getDouble("AnchorY");
        anchorZ = tag.getDouble("AnchorZ");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) tag.putUUID("Owner", ownerId);
        tag.putInt("Bombs", getBombs());
        tag.putInt(BOMB_RELOAD_PROGRESS_KEY, bombReloadProgressTicks);
        tag.putDouble("AnchorX", anchorX);
        tag.putDouble("AnchorY", anchorY);
        tag.putDouble("AnchorZ", anchorZ);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
