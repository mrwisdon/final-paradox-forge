package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.UUID;

public final class SummonedBeeEntity extends Bee {
    @Nullable private UUID ownerId;
    private int variant;

    public SummonedBeeEntity(EntityType<? extends Bee> type, Level level) {
        super(type, level);
        setNoAi(true);
        setInvulnerable(true);
        setBaby(true);
        noPhysics = true;
    }

    public static void spawn(ServerLevel level, ServerPlayer owner, int variant) {
        SummonedBeeEntity bee = new SummonedBeeEntity(ModEntities.SUMMONED_BEE.get(), level);
        bee.ownerId = owner.getUUID();
        bee.variant = variant;
        double side = variant == 1 ? 0.5D : variant == 2 ? -0.5D : 0.0D;
        Vec3 right = owner.getLookAngle().cross(new Vec3(0, 1, 0)).normalize();
        bee.setPos(owner.getX() + right.x * side, owner.getY() + (variant == 0 ? 2.5D : 2.0D), owner.getZ() + right.z * side);
        level.addFreshEntity(bee);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, bee.getX(), bee.getY() + 0.2D, bee.getZ(), 4, 0.1D, 0.1D, 0.1D, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (tickCount >= 160 || !(level() instanceof ServerLevel server)) { discard(); return; }
        ServerPlayer owner = owner(server);
        if (owner == null || distanceToSqr(owner) > 6400.0D) { discard(); return; }

        LivingEntity target = server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(40.0D),
                        entity -> GlaivorusAbilityState.isHostileTarget(owner, entity)).stream()
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if (target == null) { discard(); return; }

        Vec3 previousProbe = position().add(0.0D, -1.2D, 0.0D);
        Vec3 desired = new Vec3(target.getX(), target.getEyeY() + 1.2D, target.getZ()).subtract(position()).normalize();
        Vec3 side = desired.cross(new Vec3(0, 1, 0)).normalize();
        double sideStep = variant == 1 ? 0.2D : variant == 2 ? -0.2D : 0.0D;
        setDeltaMovement(desired.scale(0.3D).add(side.scale(sideStep)).add(0, variant == 0 ? 0.2D : 0, 0));
        move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
        setYRot((float) Math.toDegrees(Math.atan2(-desired.x, desired.z)));
        server.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.15D, getZ(), 1, 0, 0, 0, 0);

        // The original datapack checks one block around a point 1.2 blocks below
        // the visual bee. Test the full movement segment as well, so a fast bee
        // cannot pass through a target between two ticks without dealing damage.
        Vec3 currentProbe = position().add(0.0D, -1.2D, 0.0D);
        net.minecraft.world.phys.AABB hitBox = target.getBoundingBox().inflate(1.0D);
        boolean collided = hitBox.contains(currentProbe)
                || hitBox.intersects(getBoundingBox().move(0.0D, -1.2D, 0.0D))
                || hitBox.clip(previousProbe, currentProbe).isPresent();
        if (collided) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 0), owner);
            if (!(target instanceof WitherBoss) && !(target instanceof EnderDragon) && !target.getTags().contains("boss")) {
                target.hurt(owner.damageSources().playerAttack(owner), 3.0F);
            }
            discard();
        }
    }

    @Nullable private ServerPlayer owner(ServerLevel level) {
        return ownerId == null ? null : level.getServer().getPlayerList().getPlayer(ownerId);
    }

    @Override public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source) { return true; }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); if (ownerId != null) tag.putUUID("Owner", ownerId); tag.putInt("Variant", variant); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); if (tag.hasUUID("Owner")) ownerId = tag.getUUID("Owner"); variant = tag.getInt("Variant"); }
}
