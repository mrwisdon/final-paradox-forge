package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

public final class StygianArrowEntity extends Arrow {
    private static final DustParticleOptions PURPLE_DUST = new DustParticleOptions(new Vector3f(0.663F, 0.416F, 0.686F), 2.0F);
    private static final DustParticleOptions DARK_DUST = new DustParticleOptions(new Vector3f(0.192F, 0.169F, 0.192F), 2.0F);

    public StygianArrowEntity(EntityType<? extends StygianArrowEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        pickup = Pickup.DISALLOWED;
    }

    public StygianArrowEntity(Level level, LivingEntity owner) {
        this(ModEntities.STYGIAN_ARROW.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (inGround || tickCount >= 65) {
            discard();
            return;
        }
        if (level() instanceof ServerLevel server) {
            server.sendParticles(PURPLE_DUST, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
            server.sendParticles(DARK_DUST, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        }
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        Entity owner = getOwner();
        // The datapack applies its 7-point hit through a direct health handler after the
        // vanilla arrow hit. Temporarily clear hurt immunity so the separate damage packet
        // is not discarded merely because both impacts occur in the same game tick.
        int previousInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        target.hurt(damageSources().arrow(this, owner == null ? this : owner), 7.0F);
        target.invulnerableTime = Math.max(target.invulnerableTime, previousInvulnerableTime);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3), owner);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(PURPLE_DUST, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
            server.sendParticles(DARK_DUST, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
            server.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
