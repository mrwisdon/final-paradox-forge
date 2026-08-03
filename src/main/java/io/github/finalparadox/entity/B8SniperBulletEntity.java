package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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

/**
 * Sniper conduit bullet from h3/sniper. Advances 0.4 blocks/tick along its
 * stored direction, emits smoke/flame, and explodes on terrain, rover bullets
 * or rovers (handled by the B8 controller).
 */
public final class B8SniperBulletEntity extends Entity {
    public static final double SPEED = 0.4D;
    public static final double SAMPLE_Y = 1.7D;
    public static final double KILL_RADIUS = 30.0D;
    public static final double KILL_CENTER_LOCAL_Y = 6.0D;

    private static final EntityDataAccessor<Float> DATA_PITCH =
            SynchedEntityData.defineId(B8SniperBulletEntity.class, EntityDataSerializers.FLOAT);

    private Vec3 direction = new Vec3(0.0D, 0.0D, 1.0D);
    private double anchorY;
    private double anchorX;
    private double anchorZ;

    public B8SniperBulletEntity(EntityType<B8SniperBulletEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
        setInvulnerable(true);
    }

    public static B8SniperBulletEntity spawn(
            ServerLevel level, Vec3 position, Vec3 direction, BlockPos anchor) {
        B8SniperBulletEntity bullet = new B8SniperBulletEntity(ModEntities.B8_SNIPER_BULLET.get(), level);
        bullet.setPos(position);
        bullet.direction = direction.normalize();
        bullet.anchorX = anchor.getX();
        bullet.anchorY = anchor.getY();
        bullet.anchorZ = anchor.getZ();
        bullet.entityData.set(DATA_PITCH, 0.0F);
        level.addFreshEntity(bullet);
        return bullet;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_PITCH, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel server)) {
            discard();
            return;
        }
        setPos(getX() + direction.x * SPEED, getY() + direction.y * SPEED,
                getZ() + direction.z * SPEED);
        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                getX(), getY() + 1.6D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.FLAME,
                getX(), getY() + 1.6D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);

        double dx = getX() - anchorX;
        double dz = getZ() - anchorZ;
        double dy = getY() - (anchorY + KILL_CENTER_LOCAL_Y);
        if (dx * dx + dy * dy + dz * dz > KILL_RADIUS * KILL_RADIUS) {
            discard();
            return;
        }
        if (B8EncounterManager.onSniperBulletTick(server, this)) {
            // The controller performed the explosion and discarded the bullet.
            return;
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
        direction = new Vec3(tag.getDouble("DirX"), tag.getDouble("DirY"), tag.getDouble("DirZ"));
        anchorY = tag.getDouble("AnchorY");
        anchorX = tag.getDouble("AnchorX");
        anchorZ = tag.getDouble("AnchorZ");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("DirX", direction.x);
        tag.putDouble("DirY", direction.y);
        tag.putDouble("DirZ", direction.z);
        tag.putDouble("AnchorY", anchorY);
        tag.putDouble("AnchorX", anchorX);
        tag.putDouble("AnchorZ", anchorZ);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
