package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public final class TeslaCoreEntity extends Entity {
    private UUID ownerId;
    public TeslaCoreEntity(EntityType<TeslaCoreEntity> type, Level level) { super(type, level); noPhysics = true; noCulling = true; }

    public static void spawn(ServerPlayer owner, double x, double y, double z) {
        TeslaCoreEntity core = new TeslaCoreEntity(ModEntities.TESLA_CORE.get(), owner.serverLevel());
        core.ownerId = owner.getUUID(); core.setPos(x, y + 0.4D, z); owner.serverLevel().addFreshEntity(core);
        owner.serverLevel().playSound(null, core.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0F, 1.3F);
    }

    @Override protected void defineSynchedData() { }
    @Override public void tick() {
        super.tick();
        setYRot(getYRot() + 5.0F);
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel level) || tickCount > 120) { discard(); return; }
        level.sendParticles(ParticleTypes.BUBBLE_POP, getX(), getY() + 0.4D, getZ(), 6, 0.2D, 0.2D, 0.2D, 0);
        if (tickCount % 20 != 0) return;
        Entity ownerEntity = ownerId == null ? null : level.getEntity(ownerId);
        if (!(ownerEntity instanceof ServerPlayer owner)) { discard(); return; }
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(6.0D),
                entity -> GlaivorusAbilityState.isHostileTarget(owner, entity))) {
            target.hurt(owner.damageSources().lightningBolt(), 5.0F);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 18, 0.25D, 0.5D, 0.25D, 0.15D);
        }
        level.playSound(null, blockPosition(), SoundEvents.CONDUIT_AMBIENT_SHORT, SoundSource.PLAYERS, 1.0F, 2.0F);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { if (tag.hasUUID("Owner")) ownerId = tag.getUUID("Owner"); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { if (ownerId != null) tag.putUUID("Owner", ownerId); }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
