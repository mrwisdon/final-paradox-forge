package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import java.util.Optional;

/**
 * Falling gold-block module from the B8 H2 shield-breaking window. Mirrors
 * the source armor stand: falls 0.036 blocks/tick, rotates 3 degrees/tick,
 * emits the golden dust glow, and detonates when it reaches the floor.
 */
public final class B8H2ModuleEntity extends Entity {
    public static final double FALL_SPEED = 0.036D;
    public static final double ROTATION_STEP = 3.0D;
    /** Vanilla teleports armor stands over three client interpolation steps. */
    public static final float LAUNCH_DURATION_TICKS = 3.0F;
    /** Source summon position: arena anchor + (0, 6.5, 0). */
    public static final double LAUNCH_ORIGIN_LOCAL_Y = 6.5D;
    /** Source triggers the floor boom at stand y <= 76.5 (anchor y - 1.5). */
    public static final double BOOM_ANCHOR_OFFSET = 1.5D;
    /** Source samples bullets 1.4 above the stand origin. */
    public static final double BREAK_SAMPLE_Y = 1.4D;
    /** Source emits the landing boom 1.7 above the stand origin. */
    public static final double BOOM_SAMPLE_Y = 1.7D;
    /** Source emits the golden glow dust 2 above the stand origin. */
    public static final double GLOW_SAMPLE_Y = 2.0D;

    private static final EntityDataAccessor<Long> DATA_SPAWN_GAME_TIME =
            SynchedEntityData.defineId(B8H2ModuleEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_ANCHOR =
            SynchedEntityData.defineId(B8H2ModuleEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    private double anchorY;

    public B8H2ModuleEntity(EntityType<B8H2ModuleEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
        setInvulnerable(true);
    }

    public static B8H2ModuleEntity spawn(ServerLevel level, Vec3 position, BlockPos anchor) {
        B8H2ModuleEntity module = new B8H2ModuleEntity(ModEntities.B8_H2_MODULE.get(), level);
        module.setPos(position);
        module.anchorY = anchor.getY();
        module.entityData.set(DATA_ANCHOR, Optional.of(anchor.immutable()));
        module.entityData.set(DATA_SPAWN_GAME_TIME, level.getGameTime());
        module.setGlowingTag(true);
        level.addFreshEntity(module);
        return module;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_SPAWN_GAME_TIME, Long.MIN_VALUE);
        entityData.define(DATA_ANCHOR, Optional.empty());
    }

    public float visualAge(float partialTick) {
        long spawnGameTime = entityData.get(DATA_SPAWN_GAME_TIME);
        if (spawnGameTime == Long.MIN_VALUE) return LAUNCH_DURATION_TICKS;
        return Math.max(0.0F, level().getGameTime() - spawnGameTime + partialTick);
    }

    public float launchProgress(float partialTick) {
        return Math.min(1.0F, Math.max(0.0F, visualAge(partialTick) / LAUNCH_DURATION_TICKS));
    }

    public float moduleYaw(float partialTick) {
        return visualAge(partialTick) * (float) ROTATION_STEP;
    }

    public Optional<Vec3> launchOrigin() {
        return entityData.get(DATA_ANCHOR).map(anchor -> new Vec3(
                anchor.getX(), anchor.getY() + LAUNCH_ORIGIN_LOCAL_Y, anchor.getZ()));
    }

    @Override
    public int getTeamColor() {
        // Source armor stands are Glowing:1b members of the yellow team.
        return 0xFFFF55;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(new DustParticleOptions(
                            new Vector3f(1.0F, 0.933F, 0.0F), 1.5F),
                    getX() + (level().random.nextDouble() - 0.5D) * 0.4D,
                    getY() + GLOW_SAMPLE_Y + (level().random.nextDouble() - 0.5D) * 0.4D,
                    getZ() + (level().random.nextDouble() - 0.5D) * 0.4D,
                    0.0D, 0.0D, 0.0D);
            return;
        }
        if (!(level() instanceof ServerLevel server)) {
            discard();
            return;
        }
        setPos(getX(), getY() - FALL_SPEED, getZ());
        if (getY() <= anchorY - BOOM_ANCHOR_OFFSET) {
            B8EncounterManager.onModuleLanded(server, this);
        }
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
        return getBoundingBox().inflate(2.0D, 3.0D, 2.0D);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        anchorY = tag.getDouble("AnchorY");
        if (tag.contains("AnchorX") && tag.contains("AnchorZ")) {
            entityData.set(DATA_ANCHOR, Optional.of(new BlockPos(
                    tag.getInt("AnchorX"), (int) Math.round(anchorY), tag.getInt("AnchorZ"))));
        }
        int legacyAnimationAge = tag.contains("AnimationAge")
                ? tag.getInt("AnimationAge")
                : Math.max(0, Math.round(tag.getFloat("Yaw") / (float) ROTATION_STEP));
        long spawnGameTime = tag.contains("SpawnGameTime")
                ? tag.getLong("SpawnGameTime")
                : level().getGameTime() - legacyAnimationAge;
        entityData.set(DATA_SPAWN_GAME_TIME, spawnGameTime);
        setGlowingTag(true);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("AnchorY", anchorY);
        entityData.get(DATA_ANCHOR).ifPresent(anchor -> {
            tag.putInt("AnchorX", anchor.getX());
            tag.putInt("AnchorZ", anchor.getZ());
        });
        tag.putLong("SpawnGameTime", entityData.get(DATA_SPAWN_GAME_TIME));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
