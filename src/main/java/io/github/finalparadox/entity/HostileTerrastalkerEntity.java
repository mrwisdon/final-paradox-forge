package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Server-authoritative reconstruction of the autonomous hostile Terrastalker. */
public final class HostileTerrastalkerEntity extends Monster implements TerrastalkerVisualState {
    public static final int SOURCE_VISIBLE_PARTS = 22;
    private static final double PLAYER_SEARCH_RANGE = 65.0D;
    private static final double GUN_RANGE = 20.0D;
    private static final int GUN_INTERVAL = 3;
    private static final int GUN_BULLET_LIFE = 35;
    private static final float TURRET_YAW_STEP = 4.0F;
    private static final float CANNON_PITCH_STEP = 3.0F;
    private static final double GUN_SPREAD = 0.025D;
    private static final double ENTRY_JUMP_VELOCITY = 0.85D;
    private static final float LEG_DEPLOYMENT_STEP = 0.1F;
    private static final String LAST_GUN_HIT_TICK_KEY =
            "finalparadox.hostile_terrastalker_last_gun_hit";

    private static final EntityDataAccessor<Integer> DATA_GAIT_FRAME =
            SynchedEntityData.defineId(HostileTerrastalkerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_PREVIOUS_MOVEMENT_YAW =
            SynchedEntityData.defineId(HostileTerrastalkerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_MOVEMENT_YAW =
            SynchedEntityData.defineId(HostileTerrastalkerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PREVIOUS_TURRET_YAW =
            SynchedEntityData.defineId(HostileTerrastalkerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_TURRET_YAW =
            SynchedEntityData.defineId(HostileTerrastalkerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PREVIOUS_CANNON_PITCH =
            SynchedEntityData.defineId(HostileTerrastalkerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_CANNON_PITCH =
            SynchedEntityData.defineId(HostileTerrastalkerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_COOLING =
            SynchedEntityData.defineId(HostileTerrastalkerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_LANDED =
            SynchedEntityData.defineId(HostileTerrastalkerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_PREVIOUS_LEG_DEPLOYMENT =
            SynchedEntityData.defineId(HostileTerrastalkerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_LEG_DEPLOYMENT =
            SynchedEntityData.defineId(HostileTerrastalkerEntity.class, EntityDataSerializers.FLOAT);

    private final List<GunBullet> bullets = new ArrayList<>();
    private BlockPos encounterAnchor;
    private int activePlayerCount = 1;
    private int heat = -20;
    private int gaitScore;
    private int noTargetTicks;
    private boolean selfDestructing;
    private boolean deathHandled;
    private int lastDisplayedHealth = Integer.MIN_VALUE;

    public HostileTerrastalkerEntity(EntityType<? extends HostileTerrastalkerEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setMaxUpStep(1.25F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.FOLLOW_RANGE, 100.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ARMOR, 8.0D);
    }

    public static HostileTerrastalkerEntity spawn(
            ServerLevel level, Vec3 position, BlockPos anchor, int activePlayers) {
        HostileTerrastalkerEntity entity = new HostileTerrastalkerEntity(
                ModEntities.HOSTILE_TERRASTALKER.get(), level);
        entity.activePlayerCount = Math.max(1, activePlayers);
        entity.encounterAnchor = anchor.immutable();
        double maxHealth = HostileTerrastalkerRules.maxHealth(entity.activePlayerCount);
        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
        entity.setHealth((float) maxHealth);
        entity.setPos(position.x, position.y, position.z);
        entity.setDeltaMovement(0.0D, ENTRY_JUMP_VELOCITY, 0.0D);
        entity.hasImpulse = true;
        entity.setYRot(level.random.nextFloat() * 360.0F);
        entity.addTag("hostile");
        entity.addTag("14_acechador_core");
        entity.addTag("14_acechador_hitbox");
        entity.addTag("b8_hostile_terrastalker");
        entity.setCustomNameVisible(true);
        entity.updateHealthName();
        level.addFreshEntity(entity);
        level.playSound(null, entity.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH,
                SoundSource.HOSTILE, 1.5F, 0.8F);
        level.sendParticles(ParticleTypes.CLOUD,
                entity.getX(), entity.getY(), entity.getZ(), 18, 0.8D, 0.1D, 0.8D, 0.06D);
        return entity;
    }

    @Override
    protected void registerGoals() {
        // Source movement is driven by a guide entity. This entity drives its
        // own navigation so there is still one authoritative encounter object.
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_GAIT_FRAME, -1);
        entityData.define(DATA_PREVIOUS_MOVEMENT_YAW, 0.0F);
        entityData.define(DATA_MOVEMENT_YAW, 0.0F);
        entityData.define(DATA_PREVIOUS_TURRET_YAW, 0.0F);
        entityData.define(DATA_TURRET_YAW, 0.0F);
        entityData.define(DATA_PREVIOUS_CANNON_PITCH, 1.0F);
        entityData.define(DATA_CANNON_PITCH, 1.0F);
        entityData.define(DATA_COOLING, false);
        entityData.define(DATA_LANDED, false);
        entityData.define(DATA_PREVIOUS_LEG_DEPLOYMENT, 0.0F);
        entityData.define(DATA_LEG_DEPLOYMENT, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || !(level() instanceof ServerLevel server)) return;

        capturePreviousVisualState();
        updateHealthName();
        tickProjectiles(server);

        if (!entityData.get(DATA_LANDED)) {
            if (onGround() && getDeltaMovement().y <= 0.0D) land(server);
            return;
        }

        float legDeployment = entityData.get(DATA_LEG_DEPLOYMENT);
        if (legDeployment < 1.0F) {
            entityData.set(DATA_LEG_DEPLOYMENT,
                    Math.min(1.0F, legDeployment + LEG_DEPLOYMENT_STEP));
            return;
        }

        ServerPlayer target = nearestPlayer(server, PLAYER_SEARCH_RANGE);
        if (target == null) {
            getNavigation().stop();
            if (++noTargetTicks >= 20) selfDestruct(server);
            return;
        }
        noTargetTicks = 0;
        aimAt(target);
        tickMovement(target);
        tickWeapons(server, target);
    }

    private void capturePreviousVisualState() {
        entityData.set(DATA_PREVIOUS_MOVEMENT_YAW, entityData.get(DATA_MOVEMENT_YAW));
        entityData.set(DATA_PREVIOUS_TURRET_YAW, entityData.get(DATA_TURRET_YAW));
        entityData.set(DATA_PREVIOUS_CANNON_PITCH, entityData.get(DATA_CANNON_PITCH));
        entityData.set(DATA_PREVIOUS_LEG_DEPLOYMENT, entityData.get(DATA_LEG_DEPLOYMENT));
    }

    private void land(ServerLevel server) {
        entityData.set(DATA_LANDED, true);
        setDeltaMovement(Vec3.ZERO);
        server.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE, 2.0F, 1.5F);
        server.sendParticles(ParticleTypes.CLOUD,
                getX(), getY(), getZ(), 30, 1.2D, 0.25D, 1.2D, 0.08D);
    }

    private void tickMovement(ServerPlayer target) {
        if (tickCount % 10 == 0) {
            Vec3 destination = target.position();
            Vec3 difference = destination.subtract(position()).multiply(1.0D, 0.0D, 1.0D);
            if (difference.lengthSqr() < 100.0D && difference.lengthSqr() > 0.01D) {
                Vec3 tangent = new Vec3(-difference.z, 0.0D, difference.x).normalize().scale(8.0D);
                destination = destination.add(tangent);
            }
            getNavigation().moveTo(destination.x, destination.y, destination.z, 1.0D);
        }

        Vec3 motion = getDeltaMovement();
        if (motion.horizontalDistanceSqr() > 0.0004D) {
            float yaw = (float) (Mth.atan2(-motion.x, motion.z) * Mth.RAD_TO_DEG);
            entityData.set(DATA_MOVEMENT_YAW, yaw);
            setYRot(yaw);
            yBodyRot = yaw;
            if (++gaitScore >= 20) gaitScore = 1;
            entityData.set(DATA_GAIT_FRAME, Math.max(0, (gaitScore - 1) / 5));
        } else {
            gaitScore = 0;
            entityData.set(DATA_GAIT_FRAME, -1);
        }
    }

    private void aimAt(ServerPlayer target) {
        Vec3 origin = position().add(0.0D, 1.0D, 0.0D);
        Vec3 difference = target.getEyePosition().subtract(origin);
        float yaw = (float) (Mth.atan2(-difference.x, difference.z) * Mth.RAD_TO_DEG);
        float pitch = (float) -Math.toDegrees(Math.atan2(
                difference.y, Math.sqrt(difference.x * difference.x + difference.z * difference.z)));
        entityData.set(DATA_TURRET_YAW, HostileTerrastalkerRules.turnAimToward(
                getTurretYaw(), yaw, TURRET_YAW_STEP));
        entityData.set(DATA_CANNON_PITCH, HostileTerrastalkerRules.turnAimToward(
                getCannonPitch(), Mth.clamp(pitch, -70.0F, 70.0F), CANNON_PITCH_STEP));
    }

    private void tickWeapons(ServerLevel server, ServerPlayer target) {
        if (entityData.get(DATA_COOLING)) {
            heat -= 2;
            if ((tickCount & 3) == 0) {
                server.sendParticles(heat > 0 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.BUBBLE,
                        getX(), getY() + 1.0D, getZ(), 2, 0.4D, 0.2D, 0.4D, 0.02D);
            }
            if (heat <= 0) {
                heat = -20;
                entityData.set(DATA_COOLING, false);
                server.playSound(null, blockPosition(), SoundEvents.FIRECHARGE_USE,
                        SoundSource.HOSTILE, 2.0F, 1.4F);
            }
            return;
        }

        heat += 5;
        if (distanceToSqr(target) <= GUN_RANGE * GUN_RANGE && tickCount % GUN_INTERVAL == 0) {
            fireGun(server);
        }
        if (heat >= HostileTerrastalkerRules.overheatThreshold(activePlayerCount)) {
            entityData.set(DATA_COOLING, true);
            broadcast(server, "luisb1202.functions.carga_lanas.14_verde.el_acechador.msg.1");
        }
    }

    private void fireGun(ServerLevel server) {
        Vec3 muzzle = muzzlePosition();
        Vec3 aimedDirection = Vec3.directionFromRotation(getCannonPitch(), getTurretYaw());
        Vec3 direction = aimedDirection.add(
                random.nextGaussian() * GUN_SPREAD,
                random.nextGaussian() * GUN_SPREAD,
                random.nextGaussian() * GUN_SPREAD).normalize();
        bullets.add(new GunBullet(muzzle, direction));
        server.playSound(null, blockPosition(), SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.HOSTILE, 0.7F, 1.8F);
    }

    private Vec3 muzzlePosition() {
        float yaw = getTurretYaw() * Mth.DEG_TO_RAD;
        return position().add(-Mth.sin(yaw) * 1.4D, 1.65D, Mth.cos(yaw) * 1.4D);
    }

    private void tickProjectiles(ServerLevel server) {
        for (int index = bullets.size() - 1; index >= 0; index--) {
            GunBullet bullet = bullets.get(index);
            bullet.age++;
            bullet.position = bullet.position.add(bullet.velocity);
            server.sendParticles(ParticleTypes.END_ROD,
                    bullet.position.x, bullet.position.y, bullet.position.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            server.sendParticles(new DustParticleOptions(new Vector3f(0.231F), 1.0F),
                    bullet.position.x, bullet.position.y, bullet.position.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            ServerPlayer hit = playerAt(server, bullet.position, 0.65D);
            if (hit != null) {
                applyGunHit(server, hit);
                bullets.remove(index);
            } else if (bullet.age >= GUN_BULLET_LIFE || isSolid(server, bullet.position)) {
                emitProjectileImpact(server, bullet.position);
                bullets.remove(index);
            }
        }

    }

    private void applyGunHit(ServerLevel server, ServerPlayer player) {
        CompoundTag playerData = player.getPersistentData();
        long now = server.getGameTime();
        if (playerData.contains(LAST_GUN_HIT_TICK_KEY)
                && !HostileTerrastalkerRules.canApplyGunHit(
                now, playerData.getLong(LAST_GUN_HIT_TICK_KEY))) return;
        playerData.putLong(LAST_GUN_HIT_TICK_KEY, now);

        boolean ridingTerrastalker = player.getVehicle() instanceof TerrastalkerRoverEntity;
        int roverDamage = HostileTerrastalkerRules.roverEnergyDamage(ridingTerrastalker);
        if (roverDamage > 0 && player.getVehicle() instanceof TerrastalkerRoverEntity rover) {
            rover.damageEnergy(roverDamage);
            return;
        }

        int playerDamage = HostileTerrastalkerRules.playerDamage(ridingTerrastalker);
        if (playerDamage <= 0) return;
        MobEffectInstance resistance = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
        MobEffectInstance savedResistance = resistance == null
                ? null : new MobEffectInstance(resistance);
        if (savedResistance != null) player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        player.hurt(server.damageSources().magic(), playerDamage);
        if (savedResistance != null && player.isAlive()) player.addEffect(savedResistance);
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, 1, 0, false, false, false));
    }

    private static void emitProjectileImpact(ServerLevel server, Vec3 position) {
        server.sendParticles(ParticleTypes.EXPLOSION,
                position.x, position.y, position.z, 1, 0.2D, 0.2D, 0.2D, 0.0D);
        server.playSound(null, BlockPos.containing(position), SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE, 1.0F, 1.6F);
    }

    private static boolean isSolid(ServerLevel server, Vec3 position) {
        BlockPos block = BlockPos.containing(position);
        return !server.getBlockState(block).getCollisionShape(server, block).isEmpty();
    }

    private ServerPlayer playerAt(ServerLevel server, Vec3 position, double radius) {
        return server.getEntitiesOfClass(ServerPlayer.class,
                        new AABB(position, position).inflate(radius),
                        player -> player.isAlive() && !player.isSpectator())
                .stream().min(Comparator.comparingDouble(player -> player.distanceToSqr(position)))
                .orElse(null);
    }

    private ServerPlayer nearestPlayer(ServerLevel server, double radius) {
        return server.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(radius),
                        player -> player.isAlive() && !player.isSpectator())
                .stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    private void selfDestruct(ServerLevel server) {
        if (selfDestructing) return;
        selfDestructing = true;
        emitDeathEffect(server);
        discard();
    }

    private void updateHealthName() {
        int displayedHealth = Mth.ceil(getHealth());
        if (displayedHealth == lastDisplayedHealth) return;
        lastDisplayedHealth = displayedHealth;
        int filled = Math.max(0, Math.min(10,
                Mth.ceil(getHealth() * 10.0F / Math.max(1.0F, getMaxHealth()))));
        String bar = "❤ " + "▌".repeat(filled) + "§8" + "▌".repeat(10 - filled);
        setCustomName(Component.translatable("entity.finalparadox.hostile_terrastalker")
                .append(Component.literal(" §c" + bar + " §f" + displayedHealth + "/"
                        + Mth.ceil(getMaxHealth()))));
        setCustomNameVisible(true);
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        if (!deathHandled && level() instanceof ServerLevel server) {
            deathHandled = true;
            emitDeathEffect(server);
            if (!selfDestructing) B8EncounterManager.onHostileTerrastalkerDeath(server, this);
        }
        super.die(source);
    }

    private void emitDeathEffect(ServerLevel server) {
        Vec3 center = position().add(0.0D, 0.8D, 0.0D);
        server.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE, 3.0F, 0.8F);
        server.sendParticles(ParticleTypes.EXPLOSION,
                center.x, center.y, center.z, 8, 1.0D, 0.7D, 1.0D, 0.1D);
        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                center.x, center.y, center.z, 80, 1.5D, 1.0D, 1.5D, 0.15D);
        server.sendParticles(ParticleTypes.LAVA,
                center.x, center.y, center.z, 40, 1.2D, 0.8D, 1.2D, 0.2D);
        server.sendParticles(new ItemParticleOption(
                        ParticleTypes.ITEM, new ItemStack(Items.SMOOTH_STONE_SLAB)),
                center.x, center.y, center.z, 200, 1.2D, 0.8D, 1.2D, 0.3D);
    }

    private static void broadcast(ServerLevel server, String key) {
        for (ServerPlayer player : server.players()) {
            if (!player.isSpectator()) player.sendSystemMessage(Component.translatable(key));
        }
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return BuiltInLootTables.EMPTY;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier,
                                   net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        activePlayerCount = Math.max(1, tag.getInt("ActivePlayers"));
        heat = tag.getInt("Heat");
        gaitScore = tag.getInt("GaitScore");
        noTargetTicks = tag.getInt("NoTargetTicks");
        deathHandled = tag.getBoolean("DeathHandled");
        entityData.set(DATA_COOLING, tag.getBoolean("Cooling"));
        entityData.set(DATA_LANDED, tag.getBoolean("Landed"));
        entityData.set(DATA_GAIT_FRAME, tag.getInt("GaitFrame"));
        entityData.set(DATA_MOVEMENT_YAW, tag.getFloat("MovementYaw"));
        entityData.set(DATA_PREVIOUS_MOVEMENT_YAW, tag.getFloat("MovementYaw"));
        entityData.set(DATA_TURRET_YAW, tag.getFloat("TurretYaw"));
        entityData.set(DATA_PREVIOUS_TURRET_YAW, tag.getFloat("TurretYaw"));
        entityData.set(DATA_CANNON_PITCH, tag.getFloat("CannonPitch"));
        entityData.set(DATA_PREVIOUS_CANNON_PITCH, tag.getFloat("CannonPitch"));
        float legDeployment = tag.contains("LegDeployment")
                ? tag.getFloat("LegDeployment") : (tag.getBoolean("Landed") ? 1.0F : 0.0F);
        entityData.set(DATA_LEG_DEPLOYMENT, legDeployment);
        entityData.set(DATA_PREVIOUS_LEG_DEPLOYMENT, legDeployment);
        if (tag.contains("Anchor")) encounterAnchor = BlockPos.of(tag.getLong("Anchor"));
        setCustomNameVisible(true);
        updateHealthName();
        bullets.clear();
        for (int i = 0; i < tag.getList("Bullets", CompoundTag.TAG_COMPOUND).size(); i++) {
            bullets.add(GunBullet.load(tag.getList("Bullets", CompoundTag.TAG_COMPOUND).getCompound(i)));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ActivePlayers", activePlayerCount);
        tag.putInt("Heat", heat);
        tag.putInt("GaitScore", gaitScore);
        tag.putInt("NoTargetTicks", noTargetTicks);
        tag.putBoolean("DeathHandled", deathHandled);
        tag.putBoolean("Cooling", entityData.get(DATA_COOLING));
        tag.putBoolean("Landed", entityData.get(DATA_LANDED));
        tag.putInt("GaitFrame", getGaitFrame());
        tag.putFloat("MovementYaw", getMovementYaw());
        tag.putFloat("TurretYaw", getTurretYaw());
        tag.putFloat("CannonPitch", getCannonPitch());
        tag.putFloat("LegDeployment", getLegDeployment());
        if (encounterAnchor != null) tag.putLong("Anchor", encounterAnchor.asLong());
        ListTag bulletTags = new ListTag();
        for (GunBullet bullet : bullets) bulletTags.add(bullet.save());
        tag.put("Bullets", bulletTags);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override public int getGaitFrame() { return entityData.get(DATA_GAIT_FRAME); }
    @Override public float getMovementYaw() { return entityData.get(DATA_MOVEMENT_YAW); }
    @Override public float getPreviousMovementYaw() { return entityData.get(DATA_PREVIOUS_MOVEMENT_YAW); }
    @Override public float getTurretYaw() { return entityData.get(DATA_TURRET_YAW); }
    @Override public float getPreviousTurretYaw() { return entityData.get(DATA_PREVIOUS_TURRET_YAW); }
    @Override public float getCannonPitch() { return entityData.get(DATA_CANNON_PITCH); }
    @Override public float getPreviousCannonPitch() { return entityData.get(DATA_PREVIOUS_CANNON_PITCH); }
    @Override public float getCabinYaw() { return getMovementYaw(); }
    @Override public float getPreviousCabinYaw() { return getPreviousMovementYaw(); }
    @Override public float getLegDeployment() { return entityData.get(DATA_LEG_DEPLOYMENT); }
    @Override public float getPreviousLegDeployment() {
        return entityData.get(DATA_PREVIOUS_LEG_DEPLOYMENT);
    }
    @Override public boolean isMeltingDown() { return entityData.get(DATA_COOLING); }
    @Override public boolean isHostileVisual() { return true; }
    @Override public int sourceVisibleParts() { return SOURCE_VISIBLE_PARTS; }
    @Override public boolean showMountHint() { return false; }

    private static final class GunBullet {
        private Vec3 position;
        private final Vec3 velocity;
        private int age;

        private GunBullet(Vec3 position, Vec3 velocity) {
            this.position = position;
            this.velocity = velocity;
        }

        private CompoundTag save() {
            CompoundTag tag = vectorTag(position);
            tag.put("Velocity", vectorTag(velocity));
            tag.putInt("Age", age);
            return tag;
        }

        private static GunBullet load(CompoundTag tag) {
            GunBullet bullet = new GunBullet(readVector(tag), readVector(tag.getCompound("Velocity")));
            bullet.age = tag.getInt("Age");
            return bullet;
        }
    }

    private static CompoundTag vectorTag(Vec3 vector) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("X", vector.x);
        tag.putDouble("Y", vector.y);
        tag.putDouble("Z", vector.z);
        return tag;
    }

    private static Vec3 readVector(CompoundTag tag) {
        return new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
    }
}
