package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class EctronTowerEntity extends Entity {
    private UUID ownerId;
    private int pulse;

    public EctronTowerEntity(EntityType<EctronTowerEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
    }

    public static boolean hasActive(ServerPlayer owner) {
        for (ServerLevel dimension : owner.getServer().getAllLevels())
            for (Entity entity : dimension.getAllEntities()) if (entity instanceof EctronTowerEntity) return true;
        return false;
    }

    public static void spawn(ServerPlayer owner) {
        if (hasActive(owner)) return;
        List<BlockPos> candidates = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30.0);
            BlockPos.MutableBlockPos pos = BlockPos.containing(owner.getX() + Math.cos(angle) * 6,
                    owner.getY() + 5, owner.getZ() + Math.sin(angle) * 6).mutable();
            while (pos.getY() > owner.serverLevel().getMinBuildHeight() + 2
                    && owner.serverLevel().getBlockState(pos.below()).isAir()) pos.move(0, -1, 0);
            while (pos.getY() < owner.serverLevel().getMaxBuildHeight() - 5
                    && !owner.serverLevel().getBlockState(pos).isAir()) pos.move(0, 1, 0);
            if (owner.serverLevel().getBlockState(pos).isAir()
                    && !owner.serverLevel().getBlockState(pos.below()).isAir()
                    && !owner.serverLevel().getBlockState(pos.below(2)).isAir()) candidates.add(pos.immutable());
        }
        if (candidates.isEmpty()) return;
        BlockPos pos = candidates.get(owner.getRandom().nextInt(candidates.size()));
        EctronTowerEntity tower = new EctronTowerEntity(ModEntities.ECTRON_TOWER.get(), owner.serverLevel());
        tower.ownerId = owner.getUUID();
        tower.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
        tower.setCustomName(Component.translatable("entity.finalparadox.ectron_tower"));
        tower.setCustomNameVisible(true);
        owner.serverLevel().addFreshEntity(tower);
        owner.serverLevel().playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1, 1.2F);
    }

    public static double appearanceOffset(int age) {
        if (age <= 0) return -7;
        if (age < 5) return -1.9;
        if (age < 6) return -1.77;
        if (age < 7) return -1.64;
        if (age < 10) return -1.51;
        if (age < 11) return -2.11;
        if (age < 12) return -2.13;
        if (age < 13) return -2.15;
        if (age < 14) return -2.17;
        if (age < 16) return -2.19;
        if (age < 17) return -1.79;
        if (age < 18) return -1.77;
        if (age < 19) return -1.75;
        if (age < 20) return -1.73;
        if (age < 22) return -2.03;
        if (age < 24) return -2.05;
        if (age < 26) return -1.95;
        return -1.94;
    }

    @Override protected void defineSynchedData() {}

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel server)) { discard(); return; }
        ServerPlayer owner = ownerId == null ? null : server.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null || tickCount > 240) {
            server.playSound(null, blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, .8F, 1.2F);
            discard();
            return;
        }
        if (tickCount < 24) return;
        ServerPlayer target = server.players().stream().filter(p -> p.isAlive() && p.distanceToSqr(this) <= 225)
                .min(Comparator.comparingDouble(p -> p.distanceToSqr(this))).orElse(null);
        if (target == null) return;
        Vec3 start = position().add(0, 3.2, -.5), end = target.getEyePosition();
        LivingEntity blocker = server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().expandTowards(end.subtract(start)).inflate(1),
                e -> GlaivorusAbilityState.isHostileTarget(owner, e)).stream()
                .filter(e -> segmentDistance(start, end, e.getEyePosition()) < .85)
                .min(Comparator.comparingDouble(e -> start.distanceToSqr(e.getEyePosition()))).orElse(null);
        Vec3 beamEnd = blocker == null ? end : blocker.getEyePosition();
        Vec3 beam = beamEnd.subtract(start);
        int count = Math.max(1, (int)(beam.length() * 2));
        for (int i = 0; i <= count; i++) {
            Vec3 point = start.add(beam.scale(i / (double) count));
            server.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
        if (--pulse <= 0) {
            pulse = 19;
            if (blocker != null) {
                ArcaneTechniqueEntity.abilityDamage(owner, blocker, 6);
                blocker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1), owner);
            } else {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 0));
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 0));
                target.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20, 1));
            }
        }
    }

    private static double segmentDistance(Vec3 a, Vec3 b, Vec3 p) {
        Vec3 ab = b.subtract(a);
        double t = Math.max(0, Math.min(1, p.subtract(a).dot(ab) / ab.lengthSqr()));
        return p.distanceTo(a.add(ab.scale(t)));
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) { if (tag.hasUUID("Owner")) ownerId = tag.getUUID("Owner"); pulse = tag.getInt("Pulse"); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { if (ownerId != null) tag.putUUID("Owner", ownerId); tag.putInt("Pulse", pulse); }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
