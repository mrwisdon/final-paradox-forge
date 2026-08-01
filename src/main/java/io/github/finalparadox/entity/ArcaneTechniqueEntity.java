package io.github.finalparadox.entity;

import io.github.finalparadox.ability.ArcaneMasteryState;
import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.registry.ModEntities;
import io.github.finalparadox.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class ArcaneTechniqueEntity extends Entity implements IEntityAdditionalSpawnData {
    public static final int ARCANE_SLASH = 1;
    public static final int ARS_AERUM = 2;
    public static final int FINAL_ARCANA = 3;
    public static final int ARCANE_SLASH_REVERSE = 4;

    private UUID ownerId;
    private int mode;
    private Vec3 origin = Vec3.ZERO;
    private Vec3 direction = Vec3.ZERO;
    private final Set<UUID> targets = new HashSet<>();
    private final Set<UUID> hitTargets = new HashSet<>();

    public ArcaneTechniqueEntity(EntityType<ArcaneTechniqueEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
    }

    public static void spawn(ServerPlayer owner, int mode) {
        ArcaneTechniqueEntity entity = create(owner, mode);
        if (mode == ARCANE_SLASH) {
            entity.setPos(sweepOrigin(owner, entity.direction).add(0, -1, 0));
        } else if (mode == ARS_AERUM) {
            entity.setPos(owner.position().add(entity.direction.scale(5.5)).add(0, 5, 0));
        } else {
            entity.setPos(owner.getX(), owner.getY() + 1, owner.getZ());
        }
        owner.serverLevel().addFreshEntity(entity);
    }

    private static ArcaneTechniqueEntity create(ServerPlayer owner, int mode) {
        ArcaneTechniqueEntity entity = new ArcaneTechniqueEntity(ModEntities.ARCANE_TECHNIQUE.get(), owner.serverLevel());
        entity.ownerId = owner.getUUID();
        entity.mode = mode;
        entity.origin = owner.position();
        entity.direction = owner.getLookAngle().multiply(1, 0, 1);
        if (entity.direction.lengthSqr() < .001) entity.direction = Vec3.directionFromRotation(0, owner.getYRot());
        entity.direction = entity.direction.normalize();
        entity.setYRot(owner.getYRot());
        return entity;
    }

    private static Vec3 sweepOrigin(ServerPlayer owner, Vec3 direction) {
        LivingEntity target = owner.serverLevel().getEntitiesOfClass(LivingEntity.class,
                        owner.getBoundingBox().inflate(15), candidate -> GlaivorusAbilityState.isHostileTarget(owner, candidate))
                .stream().min(Comparator.comparingDouble(owner::distanceToSqr)).orElse(null);
        return target == null ? owner.position() : target.position().subtract(direction.scale(2));
    }

    private static void spawnReverseSweep(ServerPlayer owner) {
        ArcaneTechniqueEntity reverse = create(owner, ARCANE_SLASH_REVERSE);
        reverse.setPos(sweepOrigin(owner, reverse.direction).add(0, .2, 0));
        owner.serverLevel().addFreshEntity(reverse);
    }

    public int mode() { return mode; }
    public ItemStack displayStack() { return ModItems.ARCANE_MASTER_BLADE.get().getDefaultInstance(); }

    @Override protected void defineSynchedData() {}

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel server)) { discard(); return; }
        ServerPlayer owner = ownerId == null ? null : server.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null) { discard(); return; }

        if (mode == ARCANE_SLASH || mode == ARCANE_SLASH_REVERSE) {
            tickSweep(server, owner, mode == ARCANE_SLASH_REVERSE);
            if (mode == ARCANE_SLASH && tickCount >= 14) {
                spawnReverseSweep(owner);
                discard();
            } else if (mode == ARCANE_SLASH_REVERSE && tickCount >= 15) discard();
        } else if (mode == ARS_AERUM) {
            tickArsAerum(server, owner);
            if (tickCount >= 60) discard();
        } else if (mode == FINAL_ARCANA) {
            finalPrelude(server, owner);
            if (tickCount >= 29) discard();
        }
    }

    private void tickSweep(ServerLevel server, ServerPlayer owner, boolean reverse) {
        Vec3 sword = sweepSwordPosition(position(), getYRot(), tickCount, reverse);
        server.sendParticles(ParticleTypes.END_ROD, sword.x, sword.y, sword.z, 2, .08, .08, .08, .01);
        AABB volume = new AABB(sword, sword).inflate(2);
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, volume,
                candidate -> GlaivorusAbilityState.isHostileTarget(owner, candidate)
                        && !hitTargets.contains(candidate.getUUID())
                        && candidate.position().add(0, candidate.getBbHeight() * .5, 0).distanceToSqr(sword) <= 4)) {
            if (abilityDamage(owner, target, 19)) {
                hitTargets.add(target.getUUID());
                Vec3 away = target.position().subtract(position()).multiply(1, 0, 1);
                if (away.lengthSqr() < .01) away = direction;
                away = away.normalize();
                target.push(away.x * .8, .2, away.z * .8);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1), owner);
                server.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1, target.getZ(), 1, 0, 0, 0, 0);
                server.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1, 1.2F);
            }
        }
    }

    public static Vec3 sweepSwordPosition(Vec3 core, float ownerYaw, float age, boolean reverse) {
        float rotation = sweepRotation(age) * (reverse ? -1 : 1);
        double angle = Math.toRadians(ownerYaw + (reverse ? 300 : 130) + rotation);
        double right = 1.2 * Math.cos(angle) + 1.5 * Math.sin(angle);
        double forward = -1.2 * Math.sin(angle) + 1.5 * Math.cos(angle);
        return core.add(right, sweepHeight(age, reverse), forward);
    }

    public static float sweepRotation(float age) {
        float first = Math.min(age, 9) * 42;
        float pauseAdjusted = Math.max(0, Math.min(age - 10, 5)) * 8;
        return first + pauseAdjusted;
    }

    public static double sweepHeight(float age, boolean reverse) {
        double change = Math.min(age, 9) * .098 + Math.max(0, Math.min(age - 10, 5)) * .01;
        return 1.4 + (reverse ? -change : change);
    }

    public static float sweepArmYaw(float age, boolean reverse) {
        float change = Math.min(age, 9) * 2 + Math.max(0, age - 9);
        return reverse ? 15 - change : -10 + change;
    }

    public static Vec3 upperSwordPosition(Vec3 core, float ownerYaw, float age) {
        double localRight = age < 7 ? 3 : -1.5;
        Vec3 forward = Vec3.directionFromRotation(0, ownerYaw + 245).multiply(1, 0, 1).normalize();
        Vec3 right = new Vec3(forward.z, 0, -forward.x);
        return core.add(right.scale(localRight)).add(forward.scale(.2)).add(0, -.45, 0);
    }

    private void tickArsAerum(ServerLevel server, ServerPlayer owner) {
        Vec3 upperCenter = origin.add(direction.scale(5.5)).add(0, 5, 0);
        if (tickCount <= 14) {
            Vec3 sword = upperSwordPosition(upperCenter, getYRot(), tickCount);
            for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, new AABB(sword, sword).inflate(5),
                    candidate -> GlaivorusAbilityState.isHostileTarget(owner, candidate)
                            && !targets.contains(candidate.getUUID())
                            && candidate.position().add(0, candidate.getBbHeight() * .5, 0).distanceToSqr(sword) <= 25)) {
                targets.add(target.getUUID());
                abilityDamage(owner, target, 2);
                target.setDeltaMovement(0, 1, 0);
                target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 80, 50), owner);
                server.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1, target.getZ(), 1, 0, 0, 0, 0);
            }
            server.sendParticles(ParticleTypes.END_ROD, sword.x, sword.y, sword.z, 2, .08, .08, .08, .01);
        }
        if (tickCount == 1) {
            server.playSound(null, blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1, 1.4F);
        }
        if (tickCount == 15) moveToUltimaStart();
        if (tickCount == 19 || tickCount == 25 || tickCount == 35) {
            if (tickCount == 19 || tickCount == 35) moveComboCoreToTarget(server);
            forTarget(server, target -> {
                abilityDamage(owner, target, 2);
                target.setDeltaMovement(0, .4, 0);
            });
            comboFlash(server, false);
        }
        if (tickCount == 49) {
            forTarget(server, target -> {
                target.removeEffect(MobEffects.SLOW_FALLING);
                target.setDeltaMovement(0, -1.5, 0);
                abilityDamage(owner, target, isBoss(target) ? 50 : 99);
            });
            comboFlash(server, true);
        }
    }

    private void moveToUltimaStart() {
        Vec3 forward = Vec3.directionFromRotation(0, getYRot() + 245).multiply(1, 0, 1).normalize();
        Vec3 right = new Vec3(forward.z, 0, -forward.x);
        setPos(position().add(right.scale(-1)).add(0, -1, 0));
    }

    private void moveComboCoreToTarget(ServerLevel server) {
        LivingEntity nearest = targets.stream().map(server::getEntity).filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast).filter(LivingEntity::isAlive)
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if (nearest != null) {
            Vec3 forward = Vec3.directionFromRotation(0, getYRot() + 285).multiply(1, 0, 1).normalize();
            Vec3 right = new Vec3(forward.z, 0, -forward.x);
            setPos(nearest.position().add(right).add(0, 1, 0));
        }
    }

    private void comboFlash(ServerLevel server, boolean finisher) {
        int arms = finisher ? 2 : 1;
        for (int arm = 0; arm < arms; arm++) {
            double offset = arms == 1 ? 0 : (arm == 0 ? -.4 : .4);
            server.sendParticles(ParticleTypes.FLASH, getX(), getY() + offset, getZ(), 1, 0, 0, 0, 0);
            for (int i = 0; i < 12; i++) {
                double angle = Math.toRadians(i * 15 - 75);
                server.sendParticles(ParticleTypes.END_ROD, getX() + Math.cos(angle) * 2.7,
                        getY() + offset + Math.sin(angle) * 2.7, getZ(), 1, 0, 0, 0, 0);
            }
        }
        server.playSound(null, blockPosition(), finisher ? SoundEvents.WITHER_SHOOT : SoundEvents.PLAYER_ATTACK_NODAMAGE,
                SoundSource.PLAYERS, 1, finisher ? 1.3F : .6F);
    }

    private void finalPrelude(ServerLevel server, ServerPlayer owner) {
        Vec3 center = owner.position().add(0, 1, 0);
        if (tickCount <= 18) {
            double radius = .25 + tickCount * .16;
            for (int i = 0; i < 16; i++) {
                double angle = i * Math.PI / 8 + tickCount * .12;
                server.sendParticles(ParticleTypes.ENCHANT, center.x + Math.cos(angle) * radius,
                        center.y + (i % 4) * .35 - .3, center.z + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
            }
        }
        if (tickCount == 10) finalArcane(owner);
        if (tickCount >= 11) {
            int travel = tickCount - 10;
            double radius = travel <= 8 ? travel * 1.1 : 8.8 + (travel - 8) * .15;
            for (int i = 0; i < 16; i++) {
                double angle = i * Math.PI / 8;
                server.sendParticles(ParticleTypes.END_ROD, center.x + Math.cos(angle) * radius,
                        center.y, center.z + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
            }
        }
        if (tickCount == 29) {
            for (int i = 0; i < 16; i++) {
                double angle = i * Math.PI / 8;
                server.sendParticles(ParticleTypes.EXPLOSION, center.x + Math.cos(angle) * 10.45,
                        center.y, center.z + Math.sin(angle) * 10.45, 1, 0, 0, 0, 0);
            }
        }
    }

    private void finalArcane(ServerPlayer owner) {
        ServerLevel server = owner.serverLevel();
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(12),
                candidate -> GlaivorusAbilityState.isHostileTarget(owner, candidate))) {
            abilityDamage(owner, target, 24);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 14), owner);
            Vec3 away = target.position().subtract(owner.position());
            if (away.lengthSqr() > .01) {
                away = away.normalize();
                target.push(away.x * 1.2, .35, away.z * 1.2);
            }
        }
        for (ServerPlayer ally : server.players()) if (ally.distanceToSqr(owner) <= 144) {
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 5));
            ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 2));
            ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
            for (MobEffectInstance effect : new ArrayList<>(ally.getActiveEffects()))
                if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) ally.removeEffect(effect.getEffect());
        }
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, owner.getX(), owner.getY() + 1, owner.getZ(), 1, 0, 0, 0, 0);
        server.playSound(null, owner.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1, 1.5F);
    }

    private void forTarget(ServerLevel server, Consumer<LivingEntity> action) {
        for (UUID id : new HashSet<>(targets)) {
            Entity entity = server.getEntity(id);
            if (entity instanceof LivingEntity living && living.isAlive()) action.accept(living);
        }
    }

    private static boolean isBoss(LivingEntity target) {
        return target instanceof EnderDragon || target instanceof WitherBoss || target.getTags().contains("boss");
    }

    public static boolean abilityDamage(ServerPlayer owner, LivingEntity target, float amount) {
        owner.getPersistentData().putBoolean(ArcaneMasteryState.ABILITY_DAMAGE_KEY, true);
        boolean result = target.hurt(owner.damageSources().playerAttack(owner), amount);
        owner.getPersistentData().remove(ArcaneMasteryState.ABILITY_DAMAGE_KEY);
        return result;
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) ownerId = tag.getUUID("Owner");
        mode = tag.getInt("Mode");
        origin = new Vec3(tag.getDouble("OX"), tag.getDouble("OY"), tag.getDouble("OZ"));
        direction = new Vec3(tag.getDouble("DX"), tag.getDouble("DY"), tag.getDouble("DZ"));
        targets.clear();
        for (int i = 0; i < tag.getList("Targets", 11).size(); i++)
            targets.add(net.minecraft.nbt.NbtUtils.loadUUID(tag.getList("Targets", 11).get(i)));
        hitTargets.clear();
        for (int i = 0; i < tag.getList("HitTargets", 11).size(); i++)
            hitTargets.add(net.minecraft.nbt.NbtUtils.loadUUID(tag.getList("HitTargets", 11).get(i)));
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) tag.putUUID("Owner", ownerId);
        tag.putInt("Mode", mode);
        tag.putDouble("OX", origin.x); tag.putDouble("OY", origin.y); tag.putDouble("OZ", origin.z);
        tag.putDouble("DX", direction.x); tag.putDouble("DY", direction.y); tag.putDouble("DZ", direction.z);
        net.minecraft.nbt.ListTag targetList = new net.minecraft.nbt.ListTag();
        for (UUID id : targets) targetList.add(net.minecraft.nbt.NbtUtils.createUUID(id));
        tag.put("Targets", targetList);
        net.minecraft.nbt.ListTag hitList = new net.minecraft.nbt.ListTag();
        for (UUID id : hitTargets) hitList.add(net.minecraft.nbt.NbtUtils.createUUID(id));
        tag.put("HitTargets", hitList);
    }

    @Override public void writeSpawnData(FriendlyByteBuf buffer) { buffer.writeInt(mode); }
    @Override public void readSpawnData(FriendlyByteBuf buffer) { mode = buffer.readInt(); }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
