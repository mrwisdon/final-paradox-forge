package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.particles.ItemParticleOption;
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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

/**
 * Gravity bomb dropped by the recon drone. It falls for up to two seconds,
 * explodes on impact, damages hostile entities, and does not break terrain.
 */
public final class DroneBombEntity extends Entity {
    private static final int MAX_FUSE_TICKS = 40;
    private static final double EXPLOSION_RADIUS = 3.5D;
    private static final float EXPLOSION_DAMAGE = 20.0F;

    private UUID ownerId;

    public DroneBombEntity(EntityType<DroneBombEntity> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public void setOwner(UUID ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        Vec3 velocity = getDeltaMovement().add(0.0D, -0.045D, 0.0D);
        move(MoverType.SELF, velocity);
        if (onGround()) {
            velocity = new Vec3(velocity.x * 0.4D, 0.0D, velocity.z * 0.4D);
        }
        setDeltaMovement(velocity);
        if (tickCount >= MAX_FUSE_TICKS || (onGround() && tickCount > 6)) {
            explode();
        }
    }

    private void explode() {
        if (!(level() instanceof ServerLevel server)) {
            discard();
            return;
        }
        server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(),
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(),
                24, 0.8D, 0.5D, 0.8D, 0.1D);
        server.sendParticles(ParticleTypes.FLASH, getX(), getY(), getZ(),
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,
                        new ItemStack(Items.TNT)),
                getX(), getY(), getZ(), 30, 0.6D, 0.6D, 0.6D, 0.2D);
        server.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS, 1.2F, 1.0F);

        ServerPlayer owner = ownerId == null
                ? null : server.getServer().getPlayerList().getPlayer(ownerId);
        if (owner != null) {
            for (LivingEntity target : server.getEntitiesOfClass(
                    LivingEntity.class, getBoundingBox().inflate(EXPLOSION_RADIUS),
                    entity -> GlaivorusAbilityState.isHostileTarget(owner, entity))) {
                if (target.hurt(owner.damageSources().playerAttack(owner),
                        EXPLOSION_DAMAGE)) {
                    Vec3 away = target.position().subtract(position());
                    if (away.lengthSqr() < 1.0E-6D) {
                        away = new Vec3(0.0D, 1.0D, 0.0D);
                    }
                    target.setDeltaMovement(target.getDeltaMovement()
                            .add(away.normalize().scale(1.2D)).add(0.0D, 0.6D, 0.0D));
                    target.hurtMarked = true;
                }
            }
        }
        discard();
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            ownerId = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
