package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.network.NetworkHooks;

/**
 * Level-three Detonating affix instance used by the B8 Detonator skeleton.
 * The source map uses an invisible ten-health slime plus two armor stands;
 * this entity keeps the same server body while one renderer draws both the
 * rotating TNT and its countdown.
 */
public final class B8DetonatorBombEntity extends Slime {
    public static final int FUSE_TICKS = 100;
    public static final double BLAST_RADIUS = 25.0D;
    /** Throw origin above the dying skeleton's feet: its TNT head height. */
    public static final double THROW_HEIGHT = 1.7D;
    private static final double THROW_GRAVITY = 0.08D;
    private static final double THROW_DRAG = 0.91D;
    private static final double THROW_UP_SPEED = 1.0D;

    private static final EntityDataAccessor<Integer> DATA_FUSE =
            SynchedEntityData.defineId(B8DetonatorBombEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_LANDED =
            SynchedEntityData.defineId(B8DetonatorBombEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_RED_FLASH =
            SynchedEntityData.defineId(B8DetonatorBombEntity.class, EntityDataSerializers.BOOLEAN);

    private int flashTicks;
    private int flashStep;
    private boolean destroyed;
    private double throwVx;
    private double throwVy;
    private double throwVz;

    public B8DetonatorBombEntity(EntityType<? extends Slime> type, Level level) {
        super(type, level);
        setNoAi(true);
        setNoGravity(true);
        setPersistenceRequired();
        xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D);
    }

    public static B8DetonatorBombEntity spawn(ServerLevel level, Vec3 origin, int directionIndex) {
        B8DetonatorBombEntity bomb = new B8DetonatorBombEntity(
                ModEntities.B8_DETONATOR_BOMB.get(), level);
        double radians = Math.toRadians(Math.floorMod(directionIndex, 24) * 15.0D);
        bomb.setPos(origin.x, origin.y + THROW_HEIGHT, origin.z);
        bomb.throwVx = Math.sin(radians);
        bomb.throwVy = THROW_UP_SPEED;
        bomb.throwVz = Math.cos(radians);
        bomb.setDeltaMovement(Vec3.ZERO);
        bomb.setHealth(10.0F);
        bomb.addTag("b8_h3_detonator_bomb");
        bomb.addTag("afijo_lvl_3");
        bomb.setGlowingTag(true);
        level.addFreshEntity(bomb);
        bomb.joinFlashTeam(level, true);
        level.sendParticles(ParticleTypes.EXPLOSION,
                origin.x, origin.y + THROW_HEIGHT, origin.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.CLOUD,
                origin.x, origin.y + THROW_HEIGHT, origin.z,
                8, 0.2D, 0.2D, 0.2D, 0.2D);
        level.sendParticles(ParticleTypes.LAVA,
                origin.x, origin.y + THROW_HEIGHT, origin.z,
                3, 0.0D, 0.0D, 0.0D, 0.0D);
        level.playSound(null, bomb.blockPosition(), SoundEvents.TNT_PRIMED,
                SoundSource.MASTER, 1.0F, 1.0F);
        return bomb;
    }

    @Override
    protected void registerGoals() {
        // The source slime is only a projectile/hit body and has no AI goals.
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_FUSE, 0);
        entityData.define(DATA_LANDED, false);
        entityData.define(DATA_RED_FLASH, true);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel server) || !B8EncounterManager.isActive(server)) {
            discard();
            return;
        }

        if (!hasNearbyPlayer(server, 80.0D)) {
            discard();
            return;
        }

        if (!hasLanded()) {
            // Manual throw physics: a visible parabola independent of vanilla
            // slime movement (NoGravity + zeroed delta keeps it stable).
            throwVy -= THROW_GRAVITY;
            throwVy *= 0.98D;
            throwVx *= THROW_DRAG;
            throwVz *= THROW_DRAG;
            setPos(getX() + throwVx, getY() + throwVy, getZ() + throwVz);
            setDeltaMovement(Vec3.ZERO);
            server.sendParticles(ParticleTypes.LARGE_SMOKE,
                    getX(), getY() + 0.5D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            if (random.nextInt(5) == 0) {
                server.sendParticles(ParticleTypes.LAVA,
                        getX(), getY() + 0.5D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
            if (shouldLand(server)) {
                entityData.set(DATA_LANDED, true);
                setDeltaMovement(Vec3.ZERO);
                addTag("hostile");
            }
        } else {
            setDeltaMovement(Vec3.ZERO);
        }

        int fuse = getFuseTicks() + 1;
        entityData.set(DATA_FUSE, fuse);
        tickFlash(server, fuse);
        if (fuse >= FUSE_TICKS) explode(server);
    }

    /**
     * Landing check for the manually integrated throw: stop against solid
     * bodies (walls) and rest on the floor once the vertical speed turns down.
     */
    private boolean shouldLand(ServerLevel server) {
        if (onGround()) return true;
        BlockPos body = BlockPos.containing(getX(), getY() + 0.5D, getZ());
        if (!server.getBlockState(body).getCollisionShape(server, body).isEmpty()) {
            setPos(getX() - throwVx, getY(), getZ() - throwVz);
            return true;
        }
        if (throwVy > 0.0D) return false;
        BlockPos feet = BlockPos.containing(getX(), getY() - 0.1D, getZ());
        BlockState state = server.getBlockState(feet);
        if (state.getCollisionShape(server, feet).isEmpty()) return false;
        setPos(getX(), feet.getY() + 1.0D, getZ());
        return true;
    }

    private void tickFlash(ServerLevel server, int fuse) {
        flashTicks++;
        int interval = fuse <= 20 ? 10 : fuse <= 40 ? 6 : fuse <= 60 ? 5 : fuse <= 80 ? 3 : 2;
        if (flashTicks < interval) return;
        flashTicks = 0;
        flashStep++;
        boolean red = (flashStep & 1) != 0;
        entityData.set(DATA_RED_FLASH, red);
        joinFlashTeam(server, red);
    }

    private void joinFlashTeam(ServerLevel server, boolean red) {
        String name = red ? "red" : "yellow";
        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(name);
        if (team == null) team = scoreboard.addPlayerTeam(name);
        team.setColor(red ? ChatFormatting.RED : ChatFormatting.YELLOW);
        scoreboard.addPlayerToTeam(getScoreboardName(), team);
    }

    private boolean hasNearbyPlayer(ServerLevel server, double radius) {
        return !server.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(radius),
                player -> !player.isSpectator()).isEmpty();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!hasLanded()) return false;
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        if (destroyed) return;
        destroyed = true;
        if (level() instanceof ServerLevel server) emitDestroyed(server);
        discard();
    }

    private void emitDestroyed(ServerLevel server) {
        Vec3 center = position().add(0.0D, 1.0D, 0.0D);
        server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.TNT)),
                center.x, center.y, center.z, 50, 0.0D, 0.0D, 0.0D, 0.2D);
        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                center.x, center.y, center.z, 4, 0.1D, 0.1D, 0.1D, 0.0D);
        server.sendParticles(ParticleTypes.EXPLOSION,
                center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.playSound(null, blockPosition(), SoundEvents.GRASS_BREAK,
                SoundSource.MASTER, 1.0F, 1.3F);
        server.playSound(null, blockPosition(), SoundEvents.CREEPER_HURT,
                SoundSource.MASTER, 1.0F, 1.2F);
    }

    private void explode(ServerLevel server) {
        if (destroyed) return;
        destroyed = true;
        Vec3 center = position();
        server.sendParticles(ParticleTypes.EXPLOSION,
                center.x, center.y + 3.0D, center.z, 50, 8.0D, 4.0D, 8.0D, 0.0D);
        server.sendParticles(ParticleTypes.EXPLOSION,
                center.x, center.y + 0.5D, center.z, 10, 1.0D, 1.0D, 1.0D, 0.0D);
        server.playSound(null, blockPosition(), SoundEvents.END_GATEWAY_SPAWN,
                SoundSource.MASTER, 2.0F, 0.1F);
        server.playSound(null, blockPosition(), SoundEvents.END_PORTAL_SPAWN,
                SoundSource.MASTER, 2.0F, 2.0F);

        for (int index = 0; index < 64; index++) {
            double angle = index * Math.PI * 2.0D / 64.0D;
            server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    center.x, center.y + 0.1D, center.z, 0,
                    Math.sin(angle), 0.0D, Math.cos(angle), 0.8D);
        }
        server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                center.x, center.y + 2.0D, center.z, 40, 0.1D, 3.0D, 0.1D, 0.0D);
        server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                center.x, center.y + 6.0D, center.z, 200, 1.5D, 0.4D, 1.2D, 0.01D);
        server.sendParticles(ParticleTypes.FLAME,
                center.x, center.y + 2.0D, center.z, 10, 0.1D, 3.0D, 0.1D, 0.0D);
        server.sendParticles(ParticleTypes.FLAME,
                center.x, center.y + 6.0D, center.z, 20, 1.5D, 0.4D, 1.2D, 0.0D);
        server.sendParticles(ParticleTypes.LAVA,
                center.x, center.y + 6.0D, center.z, 4, 1.5D, 0.4D, 1.2D, 0.0D);

        double radiusSqr = BLAST_RADIUS * BLAST_RADIUS;
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator() || player.distanceToSqr(this) > radiusSqr) continue;
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 0,
                    false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.HARM, 20, 2,
                    false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 1));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 160, 1));
            Component speaker = Component.translatable("luisb1202.functions.afijos.descubrir.hd.3")
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFBBDFF))
                            .withBold(true).withItalic(true));
            player.sendSystemMessage(speaker.copy().append(
                    Component.translatable("luisb1202.functions.afijos.detonante.boom.1")));
            player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(),
                    SoundSource.MASTER, 1.0F, 2.0F);
        }
        for (TerrastalkerRoverEntity rover : server.getEntitiesOfClass(
                TerrastalkerRoverEntity.class,
                new AABB(center, center).inflate(BLAST_RADIUS),
                rover -> rover.isAlive() && rover.isEncounterMode()
                        && rover.position().distanceToSqr(center) <= radiusSqr)) {
            rover.damageEnergy(3);
        }
        discard();
    }

    public int getFuseTicks() {
        return entityData.get(DATA_FUSE);
    }

    public boolean hasLanded() {
        return entityData.get(DATA_LANDED);
    }

    public boolean isRedFlash() {
        return entityData.get(DATA_RED_FLASH);
    }

    /** The source starts with a red "3" for ten ticks before its first update to four. */
    public int getCountdownNumber() {
        int fuse = getFuseTicks();
        if (fuse < 10) return 3;
        if (fuse <= 20) return 4;
        if (fuse <= 40) return 3;
        if (fuse <= 60) return 2;
        if (fuse <= 80) return 1;
        return 0;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_FUSE, tag.getInt("Fuse"));
        entityData.set(DATA_LANDED, tag.getBoolean("Landed"));
        entityData.set(DATA_RED_FLASH, tag.getBoolean("RedFlash"));
        flashTicks = tag.getInt("FlashTicks");
        flashStep = tag.getInt("FlashStep");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Fuse", getFuseTicks());
        tag.putBoolean("Landed", hasLanded());
        tag.putBoolean("RedFlash", isRedFlash());
        tag.putInt("FlashTicks", flashTicks);
        tag.putInt("FlashStep", flashStep);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
