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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BladeRingEntity extends Entity {
    public static final String COOLDOWN_KEY = "finalparadox.thorn_ring_cooldown";
    private UUID ownerId;
    private final Map<UUID, Integer> hitCooldowns = new HashMap<>();

    public BladeRingEntity(EntityType<BladeRingEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
    }

    public static void spawn(ServerPlayer owner) {
        ServerLevel level = owner.serverLevel();
        boolean alreadyActive = !level.getEntitiesOfClass(BladeRingEntity.class, owner.getBoundingBox().inflate(128),
                ring -> owner.getUUID().equals(ring.ownerId)).isEmpty();
        if (alreadyActive) return;
        BladeRingEntity ring = new BladeRingEntity(ModEntities.BLADE_RING.get(), level);
        ring.ownerId = owner.getUUID();
        ring.setPos(owner.getX(), owner.getY() + 0.75D, owner.getZ());
        level.addFreshEntity(ring);
        level.playSound(null, owner.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.65F);
    }

    @Override protected void defineSynchedData() { }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel level) || tickCount > 70) { discard(); return; }
        Entity ownerEntity = ownerId == null ? null : level.getEntity(ownerId);
        if (!(ownerEntity instanceof ServerPlayer owner) || !owner.isAlive()) { discard(); return; }
        setPos(owner.getX(), owner.getY() + 0.75D, owner.getZ());
        hitCooldowns.replaceAll((id, ticks) -> ticks - 1);
        hitCooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);

        double rotation = Math.toRadians(tickCount * 12.0D);
        for (int i = 0; i < 6; i++) {
            double angle = rotation + i * Math.PI / 3.0D;
            Vec3 blade = position().add(Math.cos(angle) * 2.15D, 0.15D, Math.sin(angle) * 2.15D);
            if ((tickCount & 1) == 0) level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.IRON_SWORD)),
                    blade.x, blade.y, blade.z, 1, 0.05D, 0.05D, 0.05D, 0.02D);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                    new net.minecraft.world.phys.AABB(blade, blade).inflate(0.9D),
                    entity -> GlaivorusAbilityState.isHostileTarget(owner, entity) && !hitCooldowns.containsKey(entity.getUUID()))) {
                target.hurt(owner.damageSources().playerAttack(owner), 8.0F);
                Vec3 away = target.position().subtract(owner.position()).normalize();
                target.push(away.x * 0.65D, 0.15D, away.z * 0.65D);
                hitCooldowns.put(target.getUUID(), 15);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
            }
        }
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) { if (tag.hasUUID("Owner")) ownerId = tag.getUUID("Owner"); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { if (ownerId != null) tag.putUUID("Owner", ownerId); }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
