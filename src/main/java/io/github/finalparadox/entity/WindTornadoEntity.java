package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.UUID;

public final class WindTornadoEntity extends Entity {
    private static final DustParticleOptions GREEN = new DustParticleOptions(new Vector3f(0.0F, 1.0F, 0.333F), 1.5F);
    @Nullable private UUID ownerId;

    public WindTornadoEntity(EntityType<WindTornadoEntity> type, Level level) { super(type, level); noPhysics = true; noCulling = true; }

    public static void spawn(ServerPlayer owner) {
        WindTornadoEntity tornado = new WindTornadoEntity(ModEntities.WIND_TORNADO.get(), owner.level());
        tornado.ownerId = owner.getUUID(); tornado.setPos(owner.position()); tornado.setYRot(owner.getYRot());
        owner.level().addFreshEntity(tornado);
        owner.level().playSound(null, owner.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_2, SoundSource.PLAYERS, 1.0F, 0.0F);
    }

    @Override protected void defineSynchedData() { }
    @Override public void tick() {
        super.tick(); if (level().isClientSide) return;
        if (tickCount >= 40 || !(level() instanceof ServerLevel server)) { discard(); return; }
        double radians = Math.toRadians(getYRot());
        setPos(getX() - Math.sin(radians) * 0.35D, getY(), getZ() + Math.cos(radians) * 0.35D);
        BlockPos here = blockPosition();
        if (!server.getBlockState(here).getCollisionShape(server, here).isEmpty()) setPos(getX(), getY() + 0.5D, getZ());
        else { BlockPos below = here.below(); if (server.getBlockState(below).getCollisionShape(server, below).isEmpty()) setPos(getX(), getY() - 0.5D, getZ()); }
        for (int i = 0; i < 4; i++) {
            double a = (tickCount * 0.55D) + i * Math.PI / 2.0D;
            server.sendParticles(ParticleTypes.POOF, getX() + Math.cos(a) * 0.5D, getY() + 0.8D, getZ() + Math.sin(a) * 0.5D, 1, 0, 0, 0, 0.02D);
        }
        server.sendParticles(GREEN, getX(), getY() + 0.1D, getZ(), 1, 0, 0.8D, 0.5D, 0.0D);
        ServerPlayer owner = owner(server);
        if (owner == null) { discard(); return; }
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(2.0D),
                entity -> GlaivorusAbilityState.isHostileTarget(owner, entity))) {
            target.setDeltaMovement(target.getDeltaMovement().x, 1.25D, target.getDeltaMovement().z);
            server.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_3, SoundSource.PLAYERS, 0.2F, 1.9F);
        }
    }
    @Nullable private ServerPlayer owner(ServerLevel level) { return ownerId == null ? null : level.getServer().getPlayerList().getPlayer(ownerId); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { if(tag.hasUUID("Owner")) ownerId=tag.getUUID("Owner"); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { if(ownerId!=null) tag.putUUID("Owner",ownerId); }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
