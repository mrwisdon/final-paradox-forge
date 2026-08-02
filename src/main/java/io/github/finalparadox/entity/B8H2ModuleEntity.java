package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
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

/**
 * Falling gold-block module from the B8 H2 shield-breaking window. Mirrors
 * the source armor stand: falls 0.036 blocks/tick, rotates 3 degrees/tick,
 * emits the golden dust glow, and detonates when it reaches the floor.
 */
public final class B8H2ModuleEntity extends Entity {
    public static final double FALL_SPEED = 0.036D;
    public static final double ROTATION_STEP = 3.0D;
    /** Source triggers the floor boom at stand y <= 76.5 (anchor y - 1.5). */
    public static final double BOOM_ANCHOR_OFFSET = 1.5D;
    /** Source samples bullets 1.4 above the stand origin. */
    public static final double BREAK_SAMPLE_Y = 1.4D;
    /** Source emits the landing boom 1.7 above the stand origin. */
    public static final double BOOM_SAMPLE_Y = 1.7D;
    /** Source emits the golden glow dust 2 above the stand origin. */
    public static final double GLOW_SAMPLE_Y = 2.0D;

    private static final EntityDataAccessor<Float> DATA_YAW =
            SynchedEntityData.defineId(B8H2ModuleEntity.class, EntityDataSerializers.FLOAT);

    private double anchorY;

    public B8H2ModuleEntity(EntityType<B8H2ModuleEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
        setInvulnerable(true);
    }

    public static B8H2ModuleEntity spawn(ServerLevel level, Vec3 position, double anchorY) {
        B8H2ModuleEntity module = new B8H2ModuleEntity(ModEntities.B8_H2_MODULE.get(), level);
        module.setPos(position);
        module.anchorY = anchorY;
        module.entityData.set(DATA_YAW, 0.0F);
        level.addFreshEntity(module);
        return module;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_YAW, 0.0F);
    }

    public float moduleYaw() {
        return entityData.get(DATA_YAW);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel server)) {
            discard();
            return;
        }
        setPos(getX(), getY() - FALL_SPEED, getZ());
        entityData.set(DATA_YAW, moduleYaw() + (float) ROTATION_STEP);
        server.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.933F, 0.0F), 1.5F),
                getX(), getY() + GLOW_SAMPLE_Y, getZ(),
                1, 0.2D, 0.2D, 0.2D, 0.0D);
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
        entityData.set(DATA_YAW, tag.getFloat("Yaw"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("AnchorY", anchorY);
        tag.putFloat("Yaw", moduleYaw());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
