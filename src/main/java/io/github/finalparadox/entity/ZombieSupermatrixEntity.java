package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * Visual-only first reconstruction stage for B8's Zombie Supermatrix.
 * Combat health, module waves and encounter ownership deliberately belong to
 * the later B8 controller rather than this model entity.
 */
public final class ZombieSupermatrixEntity extends Entity {
    public static final int SOURCE_GOLD_PARTS = 14;
    public static final int SOURCE_HELMET_PARTS = 15;

    private static final EntityDataAccessor<Boolean> VULNERABLE = SynchedEntityData.defineId(
            ZombieSupermatrixEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SPIN_BASE_TICKS = SynchedEntityData.defineId(
            ZombieSupermatrixEntity.class, EntityDataSerializers.INT);

    public ZombieSupermatrixEntity(EntityType<ZombieSupermatrixEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
        setInvulnerable(true);
    }

    public static ZombieSupermatrixEntity spawn(ServerLevel level, Vec3 corePosition, boolean vulnerable) {
        ZombieSupermatrixEntity matrix = new ZombieSupermatrixEntity(
                ModEntities.ZOMBIE_SUPERMATRIX.get(), level);
        matrix.setPos(corePosition);
        level.addFreshEntity(matrix);
        if (vulnerable) {
            matrix.setVulnerable(true);
        }
        return matrix;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(VULNERABLE, false);
        entityData.define(SPIN_BASE_TICKS, 0);
    }

    public boolean isVulnerableModel() {
        return entityData.get(VULNERABLE);
    }

    public void setVulnerable(boolean vulnerable) {
        if (level().isClientSide || isVulnerableModel() == vulnerable) {
            return;
        }
        entityData.set(VULNERABLE, vulnerable);
        if (level() instanceof ServerLevel serverLevel) {
            playTransition(serverLevel, vulnerable);
        }
    }

    public void toggleVulnerable() {
        setVulnerable(!isVulnerableModel());
    }

    /** Source child stands rotate three degrees per game tick. */
    public float spinTicks(float partialTick) {
        return entityData.get(SPIN_BASE_TICKS) + tickCount + partialTick;
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.5D, getZ(),
                    1, 0.3D, 0.3D, 0.3D, 0.0D);
        }
    }

    private void playTransition(ServerLevel level, boolean opening) {
        double y = getY() + 1.3D;
        if (opening) {
            level.playSound(null, blockPosition(), SoundEvents.ENDER_CHEST_OPEN,
                    SoundSource.MASTER, 4.0F, 0.5F);
            for (int index = 0; index < 32; index++) {
                double angle = Math.toRadians(index * 11.25D);
                double xMotion = Math.sin(angle) * 3.0D;
                double zMotion = Math.cos(angle) * 3.0D;
                level.sendParticles(ParticleTypes.END_ROD, getX(), y, getZ(),
                        0, xMotion, 0.0D, zMotion, 0.25D);
            }
        } else {
            level.playSound(null, blockPosition(), SoundEvents.ENDER_CHEST_CLOSE,
                    SoundSource.MASTER, 4.0F, 0.5F);
            for (int index = 0; index < 32; index++) {
                double angle = Math.toRadians(index * 11.25D);
                double x = Math.sin(angle);
                double z = Math.cos(angle);
                level.sendParticles(ParticleTypes.END_ROD,
                        getX() + x * 5.0D, y, getZ() + z * 5.0D,
                        0, -x * 3.0D, 0.0D, -z * 3.0D, 0.15D);
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION, getX(), y, getZ(),
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.FLASH, getX(), y, getZ(),
                1, 0.0D, 0.0D, 0.0D, 0.0D);
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
        return getBoundingBox().inflate(4.0D, 6.0D, 4.0D);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSqr) {
        return distanceSqr < 128.0D * 128.0D;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(VULNERABLE, tag.getBoolean("VulnerableModel"));
        entityData.set(SPIN_BASE_TICKS, tag.getInt("SpinTicks"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("VulnerableModel", isVulnerableModel());
        tag.putInt("SpinTicks", entityData.get(SPIN_BASE_TICKS) + tickCount);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
