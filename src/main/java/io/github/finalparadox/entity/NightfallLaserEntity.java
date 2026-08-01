package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public final class NightfallLaserEntity extends Entity {
    private static final EntityDataAccessor<Float> BEAM_YAW =
            SynchedEntityData.defineId(NightfallLaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BEAM_PITCH =
            SynchedEntityData.defineId(NightfallLaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> PHASE =
            SynchedEntityData.defineId(NightfallLaserEntity.class, EntityDataSerializers.INT);
    @Nullable
    private UUID ownerId;

    public NightfallLaserEntity(EntityType<NightfallLaserEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
    }

    public static NightfallLaserEntity spawn(ServerLevel level, Vec3 base, Vec3 direction, int phase) {
        return spawn(level, base, direction, phase, null);
    }

    public static NightfallLaserEntity spawn(ServerLevel level, Vec3 base, Vec3 direction,
                                             int phase, @Nullable UUID ownerId) {
        NightfallLaserEntity laser = new NightfallLaserEntity(ModEntities.NIGHTFALL_LASER.get(), level);
        laser.ownerId = ownerId;
        laser.setGlowingTag(true);
        laser.setBeam(base, direction, phase);
        level.addFreshEntity(laser);
        return laser;
    }

    public void setBeam(Vec3 base, Vec3 direction, int phase) {
        Vec3 normalized = direction.normalize();
        setPos(base);
        if (!level().isClientSide) {
            tickCount = 0;
        }
        entityData.set(BEAM_YAW, (float) Math.toDegrees(Math.atan2(-normalized.x, normalized.z)));
        entityData.set(BEAM_PITCH, (float) Math.toDegrees(-Math.asin(Mth.clamp(normalized.y, -1.0D, 1.0D))));
        entityData.set(PHASE, phase);
    }

    public Vec3 beamDirection() {
        return Vec3.directionFromRotation(entityData.get(BEAM_PITCH), entityData.get(BEAM_YAW));
    }

    public int phase() {
        return entityData.get(PHASE);
    }

    public boolean isOwnedBy(UUID ownerId) {
        return ownerId.equals(this.ownerId);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSqr) {
        // The visual extends roughly 23 blocks from a 0.1-block entity origin.
        // Vanilla derives render distance from that tiny entity box (about 6.4
        // blocks), which made boss-owned beams disappear while their origin was
        // near the arena centre. Keep the whole beam visible throughout the arena.
        return distanceSqr < 128.0D * 128.0D;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(BEAM_YAW, 0.0F);
        entityData.define(BEAM_PITCH, 0.0F);
        entityData.define(PHASE, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > 90) discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(BEAM_YAW, tag.getFloat("BeamYaw"));
        entityData.set(BEAM_PITCH, tag.getFloat("BeamPitch"));
        entityData.set(PHASE, tag.getInt("Phase"));
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("BeamYaw", entityData.get(BEAM_YAW));
        tag.putFloat("BeamPitch", entityData.get(BEAM_PITCH));
        tag.putInt("Phase", entityData.get(PHASE));
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
