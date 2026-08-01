package io.github.finalparadox.ability;

import io.github.finalparadox.registry.ModTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class EotharTimeStopState {
    public static final String COOLDOWN_KEY = "finalparadox.eothar_cooldown";
    private static final String ACTIVE_KEY = "finalparadox.eothar_active";
    private static final String ELAPSED_KEY = "finalparadox.eothar_elapsed";
    private static final String FROZEN_IDS_KEY = "finalparadox.eothar_frozen_ids";
    private static final String FROZEN_BY_KEY = "finalparadox.eothar_frozen_by";
    private static final String WAS_NO_AI_KEY = "finalparadox.eothar_was_no_ai";
    private static final String WAS_NO_GRAVITY_KEY = "finalparadox.eothar_was_no_gravity";
    private static final String MOTION_X_KEY = "finalparadox.eothar_motion_x";
    private static final String MOTION_Y_KEY = "finalparadox.eothar_motion_y";
    private static final String MOTION_Z_KEY = "finalparadox.eothar_motion_z";
    private static final int DURATION_TICKS = 145;
    private static final int COOLDOWN_TICKS = 20 * 60 * 3 + 20 * 30;

    private EotharTimeStopState() {
    }

    public static boolean activate(ServerPlayer owner) {
        CompoundTag data = owner.getPersistentData();
        if (data.getInt(COOLDOWN_KEY) > 0) {
            fail(owner, "message.finalparadox.eothar.cooldown");
            return false;
        }
        if (hasActiveStop(owner.getServer())) {
            fail(owner, "message.finalparadox.eothar.active");
            return false;
        }
        if (hasBoss(owner.getServer())) {
            fail(owner, "message.finalparadox.eothar.boss");
            return false;
        }

        data.putBoolean(ACTIVE_KEY, true);
        data.putInt(ELAPSED_KEY, 0);
        data.putInt(COOLDOWN_KEY, COOLDOWN_TICKS);
        data.put(FROZEN_IDS_KEY, new ListTag());
        for (ServerPlayer player : owner.getServer().getPlayerList().getPlayers()) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 160, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, true));
            player.displayClientMessage(Component.translatable("message.finalparadox.eothar.started"), true);
            player.level().playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                    SoundSource.PLAYERS, 1.0F, 1.3F);
            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS, 1.0F, 0.9F);
        }
        burst(owner.serverLevel(), owner.position(), 5.0D);
        freezeLoadedEntities(owner);
        return true;
    }

    public static void tick(ServerPlayer player) {
        tickCooldown(player);
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(ACTIVE_KEY)) {
            return;
        }
        int elapsed = data.getInt(ELAPSED_KEY) + 1;
        data.putInt(ELAPSED_KEY, elapsed);
        freezeLoadedEntities(player);
        renderClock(player, elapsed);
        if (elapsed == 70) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE,
                    SoundSource.PLAYERS, 10.0F, 0.4F);
        }
        if (elapsed == 84 || elapsed == 114 || elapsed == 144) {
            for (ServerPlayer ally : player.getServer().getPlayerList().getPlayers()) {
                ally.level().playSound(null, ally.blockPosition(), SoundEvents.BELL_BLOCK,
                        SoundSource.PLAYERS, 1.0F, 0.4F);
            }
        }
        if (elapsed % 12 == 10) {
            for (ServerPlayer ally : player.getServer().getPlayerList().getPlayers()) {
                ally.level().playSound(null, ally.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(),
                        SoundSource.PLAYERS, 0.1F, 0.8F);
            }
        }
        if (elapsed >= DURATION_TICKS) {
            finish(player, true);
        }
    }

    public static void cancel(ServerPlayer player) {
        if (player.getPersistentData().getBoolean(ACTIVE_KEY)) {
            finish(player, false);
        }
    }

    public static void restoreIfOrphaned(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        CompoundTag entityData = entity.getPersistentData();
        if (!entityData.hasUUID(FROZEN_BY_KEY)) {
            return;
        }
        UUID ownerId = entityData.getUUID(FROZEN_BY_KEY);
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
        if (owner != null && owner.getPersistentData().getBoolean(ACTIVE_KEY)) {
            return;
        }
        restoreEntity(entity, ownerId);
    }

    private static void tickCooldown(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int remaining = data.getInt(COOLDOWN_KEY);
        if (remaining <= 0) {
            return;
        }
        int decrement = allPlayersInSanctuary(player.getServer()) ? 10 : 1;
        remaining = Math.max(0, remaining - decrement);
        if (remaining > 0) {
            data.putInt(COOLDOWN_KEY, remaining);
            return;
        }
        data.remove(COOLDOWN_KEY);
        player.displayClientMessage(Component.translatable("message.finalparadox.eothar.ready"), true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(),
                SoundSource.PLAYERS, 1.0F, 1.5F);
    }

    private static void finish(ServerPlayer owner, boolean detonate) {
        restoreFrozenEntities(owner);
        CompoundTag data = owner.getPersistentData();
        data.remove(ACTIVE_KEY);
        data.remove(ELAPSED_KEY);
        data.remove(FROZEN_IDS_KEY);
        if (!detonate) {
            return;
        }
        ServerLevel level = owner.serverLevel();
        burst(level, owner.position(), 5.0D);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                owner.getBoundingBox().inflate(12.0D), entity -> GlaivorusAbilityState.isHostileTarget(owner, entity))) {
            target.hurt(owner.damageSources().playerAttack(owner), 3.0F);
            Vec3 away = target.position().subtract(owner.position()).multiply(1.0D, 0.0D, 1.0D);
            if (away.lengthSqr() > 0.001D) {
                away = away.normalize();
                target.push(away.x, 0.25D, away.z);
            }
        }
        level.playSound(null, owner.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 1.0F, 1.5F);
    }

    private static void freezeLoadedEntities(ServerPlayer owner) {
        MinecraftServer server = owner.getServer();
        for (ServerPlayer anchor : server.getPlayerList().getPlayers()) {
            ServerLevel level = anchor.serverLevel();
            AABB area = anchor.getBoundingBox().inflate(60.0D);
            for (Mob mob : level.getEntitiesOfClass(Mob.class, area, entity ->
                    !(entity instanceof Sheep)
                            && !entity.isNoAi()
                            && !entity.isPersistenceRequired()
                            && !isBoss(entity)
                            && !entity.isInvulnerable()
                            && GlaivorusAbilityState.isHostileTarget(owner, entity))) {
                freezeMob(owner, mob);
            }
            for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, area,
                    entity -> !entity.getTags().contains("16_infernal_flame"))) {
                if (projectile instanceof SmallFireball || projectile instanceof WitherSkull) {
                    projectile.discard();
                } else {
                    freezeProjectile(owner, projectile);
                }
            }
        }
    }

    private static void freezeMob(ServerPlayer owner, Mob mob) {
        CompoundTag entityData = mob.getPersistentData();
        if (entityData.hasUUID(FROZEN_BY_KEY)) {
            return;
        }
        entityData.putUUID(FROZEN_BY_KEY, owner.getUUID());
        entityData.putBoolean(WAS_NO_AI_KEY, mob.isNoAi());
        mob.setNoAi(true);
        remember(owner, mob);
    }

    private static void freezeProjectile(ServerPlayer owner, Projectile projectile) {
        CompoundTag entityData = projectile.getPersistentData();
        if (!entityData.hasUUID(FROZEN_BY_KEY)) {
            Vec3 motion = projectile.getDeltaMovement();
            entityData.putUUID(FROZEN_BY_KEY, owner.getUUID());
            entityData.putBoolean(WAS_NO_GRAVITY_KEY, projectile.isNoGravity());
            entityData.putDouble(MOTION_X_KEY, motion.x);
            entityData.putDouble(MOTION_Y_KEY, motion.y);
            entityData.putDouble(MOTION_Z_KEY, motion.z);
            remember(owner, projectile);
        }
        projectile.setNoGravity(true);
        projectile.setDeltaMovement(Vec3.ZERO);
    }

    private static void remember(ServerPlayer owner, Entity entity) {
        CompoundTag ownerData = owner.getPersistentData();
        ListTag ids = ownerData.getList(FROZEN_IDS_KEY, Tag.TAG_STRING);
        ids.add(StringTag.valueOf(entity.getUUID().toString()));
        ownerData.put(FROZEN_IDS_KEY, ids);
    }

    private static void restoreFrozenEntities(ServerPlayer owner) {
        ListTag ids = owner.getPersistentData().getList(FROZEN_IDS_KEY, Tag.TAG_STRING);
        for (int index = 0; index < ids.size(); index++) {
            UUID id;
            try {
                id = UUID.fromString(ids.getString(index));
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            Entity entity = findEntity(owner.getServer(), id);
            if (entity == null) {
                continue;
            }
            restoreEntity(entity, owner.getUUID());
        }
    }

    private static void restoreEntity(Entity entity, UUID ownerId) {
        CompoundTag entityData = entity.getPersistentData();
        if (!entityData.hasUUID(FROZEN_BY_KEY) || !ownerId.equals(entityData.getUUID(FROZEN_BY_KEY))) {
            return;
        }
        if (entity instanceof Mob mob) {
            mob.setNoAi(entityData.getBoolean(WAS_NO_AI_KEY));
        } else if (entity instanceof Projectile projectile) {
            projectile.setNoGravity(entityData.getBoolean(WAS_NO_GRAVITY_KEY));
            projectile.setDeltaMovement(entityData.getDouble(MOTION_X_KEY),
                    entityData.getDouble(MOTION_Y_KEY), entityData.getDouble(MOTION_Z_KEY));
        }
        entityData.remove(FROZEN_BY_KEY);
        entityData.remove(WAS_NO_AI_KEY);
        entityData.remove(WAS_NO_GRAVITY_KEY);
        entityData.remove(MOTION_X_KEY);
        entityData.remove(MOTION_Y_KEY);
        entityData.remove(MOTION_Z_KEY);
        if (entity.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.EXPLOSION, entity.getX(), entity.getY() + 1.0D,
                    entity.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static Entity findEntity(MinecraftServer server, UUID id) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(id);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    private static void renderClock(ServerPlayer owner, int elapsed) {
        ServerLevel level = owner.serverLevel();
        double[] angles = {Math.toRadians(elapsed * 30.0D), Math.toRadians(elapsed * 2.5D), 0.0D};
        double[] lengths = {2.3D, 3.4D, 4.4D};
        for (int hand = 0; hand < angles.length; hand++) {
            for (double distance = 0.3D; distance <= lengths[hand]; distance += 0.35D) {
                double x = owner.getX() + Math.cos(angles[hand]) * distance;
                double z = owner.getZ() + Math.sin(angles[hand]) * distance;
                level.sendParticles(ParticleTypes.END_ROD, x, owner.getY() + 0.12D, z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        level.sendParticles(ParticleTypes.ENCHANT, owner.getX(), owner.getY() + 1.0D, owner.getZ(),
                1, 0.0D, 0.0D, 0.0D, 4.0D);
    }

    private static void burst(ServerLevel level, Vec3 center, double radius) {
        for (int index = 0; index < 48; index++) {
            double angle = index * Math.PI * 2.0D / 48.0D;
            level.sendParticles(ParticleTypes.END_ROD,
                    center.x + Math.cos(angle) * radius, center.y + 0.1D,
                    center.z + Math.sin(angle) * radius,
                    1, 0.0D, 0.0D, 0.0D, 0.4D);
        }
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static boolean hasActiveStop(MinecraftServer server) {
        return server != null && server.getPlayerList().getPlayers().stream()
                .anyMatch(player -> player.getPersistentData().getBoolean(ACTIVE_KEY));
    }

    private static boolean hasBoss(MinecraftServer server) {
        if (server == null) {
            return false;
        }
        for (ServerPlayer anchor : server.getPlayerList().getPlayers()) {
            if (!anchor.serverLevel().getEntitiesOfClass(LivingEntity.class,
                    anchor.getBoundingBox().inflate(128.0D), EotharTimeStopState::isBoss).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBoss(LivingEntity entity) {
        return entity instanceof EnderDragon || entity instanceof WitherBoss || entity.getTags().contains("boss");
    }

    private static boolean allPlayersInSanctuary(MinecraftServer server) {
        return server != null && !server.getPlayerList().getPlayers().isEmpty()
                && server.getPlayerList().getPlayers().stream().allMatch(player ->
                player.getTags().contains("finalparadox_sanctuary")
                        || player.level().getBiome(player.blockPosition()).is(ModTags.Biomes.SANCTUARY));
    }

    private static void fail(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND,
                SoundSource.PLAYERS, 0.3F, 1.5F);
    }
}
