package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import java.util.UUID;

public final class FrostStormEntity extends Entity {
    private UUID ownerId;
    public FrostStormEntity(EntityType<FrostStormEntity> type, Level level) { super(type, level); noPhysics = true; noCulling = true; }
    public static void spawn(ServerPlayer owner) {
        ServerLevel level = owner.serverLevel();
        FrostStormEntity storm = new FrostStormEntity(ModEntities.FROST_STORM.get(), level);
        storm.ownerId = owner.getUUID(); storm.setPos(owner.getX(), owner.getY(), owner.getZ()); level.addFreshEntity(storm);
        level.playSound(null, owner.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.7F);
    }
    @Override protected void defineSynchedData() { }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel level) || tickCount > 50) { discard(); return; }
        Entity ownerEntity = ownerId == null ? null : level.getEntity(ownerId);
        if (!(ownerEntity instanceof ServerPlayer owner)) { discard(); return; }
        if (tickCount <= 25) {
            for (int i = 0; i < 12; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0D, radius = random.nextDouble() * 4.5D;
                level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.PACKED_ICE.defaultBlockState()),
                        getX() + Math.cos(angle) * radius, getY() + 7.0D + random.nextDouble() * 2.0D,
                        getZ() + Math.sin(angle) * radius, 1, 0, -0.8D, 0, 0.25D);
            }
            level.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 4.0D, getZ(), 10, 4.0D, 3.0D, 4.0D, 0.06D);
        }
        if (tickCount == 8 || tickCount == 16 || tickCount == 24 || tickCount == 32) {
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.5D, 3.0D, 4.5D),
                    entity -> GlaivorusAbilityState.isHostileTarget(owner, entity))) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3));
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 20, 3));
            }
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()), getX(), getY() + 0.2D, getZ(), 60, 4.0D, 0.4D, 4.0D, 0.15D);
            level.playSound(null, blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.7F, 1.2F);
        }
        if (tickCount % 4 == 0) for (int i = 0; i < 48; i++) {
            double angle = i * Math.PI / 24.0D;
            level.sendParticles(new DustParticleOptions(new Vector3f(0.82F, 0.94F, 1.0F), 0.9F),
                    getX() + Math.cos(angle) * 4.5D, getY() + 0.15D, getZ() + Math.sin(angle) * 4.5D, 1, 0, 0, 0, 0);
        }
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { if (tag.hasUUID("Owner")) ownerId = tag.getUUID("Owner"); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { if (ownerId != null) tag.putUUID("Owner", ownerId); }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
