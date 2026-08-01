package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.registry.ModEntities;
import io.github.finalparadox.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class GlaivorusBladeEntity extends Entity {
    private static final int LAST_ANIMATION_SCORE = 120;
    private static final int SCORE_JUMP = 43;
    private static final double HIT_RADIUS_SQR = 9.0D;
    private static final double HOSTILE_SPEED_SCALE = 0.85D;
    private static final double HOSTILE_SWEEP_START = forwardOffset(71.0D);
    private static final double HOSTILE_SWEEP_END = forwardOffset(86.0D);
    private static final DustParticleOptions HOSTILE_PATH_DUST =
            new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.2F);

    @Nullable private UUID ownerId;
    private boolean hostile;
    private final Set<UUID> sweepStruck = new HashSet<>();
    private double baseX;
    private double baseY;
    private double baseZ;
    private double terrainOffset;

    public GlaivorusBladeEntity(EntityType<GlaivorusBladeEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
    }

    public static void spawnInitial(ServerPlayer owner) {
        ServerLevel level = owner.serverLevel();
        double x = owner.getX();
        double z = owner.getZ();
        double y = findGround(level, x, owner.getY() + 1.0D, z, 12);
        if (Double.isNaN(y)) y = owner.getY();
        spawn(level, owner, x, y, z, owner.getYRot());
    }

    public static void spawnForTarget(ServerPlayer owner, LivingEntity target) {
        ServerLevel level = owner.serverLevel();
        List<Vec3> valid = new ArrayList<>();
        double phase = owner.getRandom().nextBoolean() ? 11.25D : 0.0D;
        for (int index = 0; index < 16; index++) {
            double angle = Math.toRadians(index * 22.5D + phase);
            double x = target.getX() + Math.cos(angle) * 8.0D;
            double z = target.getZ() + Math.sin(angle) * 8.0D;
            double y = findGround(level, x, target.getY() + 1.0D, z, 4);
            if (!Double.isNaN(y)) valid.add(new Vec3(Mth.floor(x) + 0.5D, y, Mth.floor(z) + 0.5D));
        }

        Vec3 origin;
        if (valid.isEmpty()) {
            double angle = Math.toRadians(target.getYRot());
            double x = target.getX() - Math.sin(angle) * 8.0D;
            double z = target.getZ() + Math.cos(angle) * 8.0D;
            double y = findGround(level, x, target.getY() + 1.0D, z, 12);
            origin = new Vec3(x, Double.isNaN(y) ? target.getY() : y, z);
        } else {
            origin = valid.get(owner.getRandom().nextInt(valid.size()));
        }

        double dx = target.getX() - origin.x;
        double dz = target.getZ() - origin.z;
        // Local X is the blade's sweep axis; aim that axis through the selected target.
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx));
        spawn(level, owner, origin.x, origin.y, origin.z, yaw);
    }

    public static void spawnHostile(LivingEntity owner, double x, double y, double z, ServerPlayer target) {
        if (!(owner.level() instanceof ServerLevel level)) return;
        double ground = findGround(level, x, y + 4.0D, z, 12);
        if (!Double.isNaN(ground)) y = ground;
        double dx = target.getX() - x;
        double dz = target.getZ() - z;
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx));
        GlaivorusBladeEntity blade = new GlaivorusBladeEntity(ModEntities.GLAIVORUS_BLADE.get(), level);
        blade.ownerId = owner.getUUID();
        blade.hostile = true;
        blade.baseX = x;
        blade.baseY = y;
        blade.baseZ = z;
        blade.setPos(x, y - 11.0D, z);
        blade.setYRot(yaw);
        blade.setYHeadRot(yaw);
        level.addFreshEntity(blade);
    }

    private static void spawn(ServerLevel level, ServerPlayer owner, double x, double y, double z, float yaw) {
        GlaivorusBladeEntity blade = new GlaivorusBladeEntity(ModEntities.GLAIVORUS_BLADE.get(), level);
        blade.ownerId = owner.getUUID();
        blade.baseX = x;
        blade.baseY = y;
        blade.baseZ = z;
        blade.setPos(x, y - 11.0D, z);
        blade.setYRot(yaw);
        blade.setYHeadRot(yaw);
        level.addFreshEntity(blade);
    }

    private static double findGround(ServerLevel level, double x, double startY, double z, int maxDrop) {
        int blockX = Mth.floor(x);
        int blockZ = Mth.floor(z);
        int start = Math.min(Mth.floor(startY), level.getMaxBuildHeight() - 2);
        int end = Math.max(level.getMinBuildHeight(), start - maxDrop);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = start; y >= end; y--) {
            pos.set(blockX, y, blockZ);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                pos.setY(y + 1);
                boolean clearFeet = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
                pos.setY(y + 2);
                boolean clearHead = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
                if (clearFeet && clearHead) return y + 1.0D;
            }
        }
        return Double.NaN;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        double score = animationProgress(tickCount);
        double previousScore = animationProgress(tickCount - 1);
        if (score >= (hostile ? 100 : LAST_ANIMATION_SCORE)) {
            discard();
            return;
        }

        double forward = forwardOffset(score);
        double radians = Math.toRadians(getYRot());
        double x = baseX + Math.cos(radians) * forward;
        double z = baseZ + Math.sin(radians) * forward;
        double y = baseY + verticalOffset(score) + terrainOffset;
        setPos(x, y, z);

        if (hostile && score < 71.0D && tickCount % 10 == 1) markHostileSweepPath();
        if (score >= 71 && score <= 85) followTerrain();
        if (crossed(previousScore, score, 12.0D)) level().playSound(null, blockPosition(), SoundEvents.ARMOR_EQUIP_IRON,
                SoundSource.PLAYERS, 2.0F, 0.0F);
        if (!hostile && crossed(previousScore, score, 17.0D)) riseImpact();
        if (score >= 71 && score <= 85) sweepParticles();
        if (hostile && score >= 71 && score <= 86) hostileDamage();
        else if (score >= 72 && score <= 85) sweepDamage();
        if (crossed(previousScore, score, 73.0D)) level().playSound(null, blockPosition(), SoundEvents.WITHER_SHOOT,
                SoundSource.PLAYERS, 1.5F, 0.8F);
    }

    private static int animationScore(int age) {
        return age <= 17 ? age : age + SCORE_JUMP;
    }

    private double animationProgress(int age) {
        if (!hostile || age <= 27) return animationScore(age);
        return 71.0D + (age - 28) * HOSTILE_SPEED_SCALE;
    }

    private static boolean crossed(double previous, double current, double score) {
        return previous < score && current >= score;
    }

    private void markHostileSweepPath() {
        if (!(level() instanceof ServerLevel server)) return;
        double radians = Math.toRadians(getYRot());
        double forwardX = Math.cos(radians);
        double forwardZ = Math.sin(radians);
        double sideX = -forwardZ;
        double sideZ = forwardX;
        Set<BlockPos> markedBlocks = new HashSet<>();
        for (double forward = HOSTILE_SWEEP_START; forward <= HOSTILE_SWEEP_END; forward += 1.0D) {
            for (double side = -1.0D; side <= 1.0D; side += 1.0D) {
                double x = baseX + forwardX * forward + sideX * side;
                double z = baseZ + forwardZ * forward + sideZ * side;
                double ground = findGround(server, x, baseY + 8.0D, z, 20);
                if (Double.isNaN(ground)) continue;
                BlockPos groundBlock = BlockPos.containing(x, ground - 0.1D, z);
                if (!markedBlocks.add(groundBlock)) continue;
                server.sendParticles(HOSTILE_PATH_DUST, groundBlock.getX() + 0.5D, ground + 0.08D,
                        groundBlock.getZ() + 0.5D, 1, 0, 0, 0, 0);
            }
        }
    }

    private void followTerrain() {
        BlockPos bladeHeight = BlockPos.containing(getX(), getY() + 2.8D, getZ());
        if (!level().getBlockState(bladeHeight).getCollisionShape(level(), bladeHeight).isEmpty()) {
            terrainOffset += 1.0D;
            setPos(getX(), getY() + 1.0D, getZ());
            return;
        }
        BlockPos belowBlade = BlockPos.containing(getX(), getY() + 1.8D, getZ());
        if (level().getBlockState(belowBlade).getCollisionShape(level(), belowBlade).isEmpty()) {
            terrainOffset -= 1.0D;
            setPos(getX(), getY() - 1.0D, getZ());
        }
    }

    private void riseImpact() {
        if (!(level() instanceof ServerLevel server)) return;
        for (double side = -1.5D; side <= 1.5D; side += 0.5D) {
            Vec3 point = localPoint(side, 2.8D, 0.0D);
            server.sendParticles(ParticleTypes.LARGE_SMOKE, point.x, point.y, point.z, 2, 0, 0, 0, 0.1D);
        }
        Vec3 center = localPoint(0.0D, 2.8D, 0.0D);
        server.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 1, 0, 0, 0, 0.1D);
        server.sendParticles(ParticleTypes.LAVA, center.x, center.y, center.z, 3, 0, 0, 0, 0.1D);
        level().playSound(null, blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 2.0F, 0.6F);
        level().playSound(null, blockPosition(), SoundEvents.NETHER_BRICKS_BREAK, SoundSource.PLAYERS, 2.0F, 0.0F);
        level().playSound(null, blockPosition(), SoundEvents.BLAZE_HURT, SoundSource.PLAYERS, 2.0F, 0.0F);
        hitAtCenters(List.of(center, center.add(0.0D, 3.0D, 0.0D), center.add(0.0D, -3.0D, 0.0D)), false);
    }

    private void sweepParticles() {
        if (!(level() instanceof ServerLevel server)) return;
        Vec3 center = localPoint(0.0D, 2.8D, 0.0D);
        server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, center.x, center.y, center.z, 1, 0, 0, 0, 0);
        server.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z, 2, 0, 0, 0, 0.02D);
    }

    private void sweepDamage() {
        Vec3 left = localPoint(-1.5D, 2.8D, 0.0D);
        Vec3 center = localPoint(0.0D, 2.8D, 0.0D);
        hitAtCenters(List.of(left, left.add(0, 3, 0), left.add(0, -3, 0),
                center, center.add(0, 3, 0), center.add(0, -3, 0)), true);
    }

    private void hostileDamage() {
        LivingEntity source = livingOwner();
        AABB bounds = getBoundingBox().inflate(1.5D, 4.5D, 1.5D);
        for (ServerPlayer target : level().getEntitiesOfClass(ServerPlayer.class, bounds,
                player -> player.isAlive() && !player.isSpectator())) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0), source);
            target.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1), source);
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D,
                        target.getZ(), 1, 0, 0, 0, 0);
                server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.REDSTONE_BLOCK)),
                        target.getX(), target.getY() + 1.2D, target.getZ(), 20, 0, 0, 0, 0.12D);
            }
            level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH,
                    SoundSource.PLAYERS, 1.0F, 0.6F);
        }
    }

    private void hitAtCenters(List<Vec3> centers, boolean rememberForSweep) {
        ServerPlayer owner = owner();
        if (owner == null) return;
        AABB bounds = new AABB(getX() - 5.0D, getY() - 3.2D, getZ() - 5.0D,
                getX() + 5.0D, getY() + 9.0D, getZ() + 5.0D);
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, bounds, this::isDamageTarget)) {
            if (rememberForSweep && sweepStruck.contains(target.getUUID())) continue;
            boolean inside = centers.stream().anyMatch(center -> target.position().distanceToSqr(center) <= HIT_RADIUS_SQR
                    || target.getBoundingBox().distanceToSqr(center) <= HIT_RADIUS_SQR);
            if (!inside) continue;

            float damage = isBoss(target) ? 8.0F : 23.0F;
            if (target.hurt(owner.damageSources().playerAttack(owner), damage)) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3), owner);
                if (rememberForSweep) sweepStruck.add(target.getUUID());
                GlaivorusAbilityState.recordHit(owner);
                if (level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D,
                            target.getZ(), 1, 0, 0, 0, 0);
                }
                level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH,
                        SoundSource.PLAYERS, 1.0F, 0.6F);
            }
        }
    }

    private boolean isDamageTarget(LivingEntity target) {
        EntityType<?> type = target.getType();
        boolean hostile = type.getCategory() == MobCategory.MONSTER || type.is(ModTags.EntityTypes.GLAIVORUS_TARGETS);
        return target.isAlive() && hostile && type != EntityType.PHANTOM && type != EntityType.GHAST;
    }

    private static boolean isBoss(LivingEntity target) {
        return target.getTags().contains("boss") || target instanceof WitherBoss || target instanceof EnderDragon;
    }

    private Vec3 localPoint(double side, double up, double forward) {
        double radians = Math.toRadians(getYRot());
        double x = getX() + Math.cos(radians) * side - Math.sin(radians) * forward;
        double z = getZ() + Math.sin(radians) * side + Math.cos(radians) * forward;
        return new Vec3(x, getY() + up, z);
    }

    private static double forwardOffset(double score) {
        double offset = 0.0D;
        int wholeScore = Mth.floor(score);
        for (int tick = 61; tick <= wholeScore; tick++) {
            if (tick <= 65) offset -= 0.2D;
            else if (tick <= 70) offset -= 0.05D;
            else if (tick <= 73) offset += 1.5D;
            else if (tick <= 82) offset += 3.5D;
            else if (tick <= 85) offset += 1.5D;
            else if (tick <= 88) offset += 0.1D;
        }
        double partial = score - wholeScore;
        int nextTick = wholeScore + 1;
        if (nextTick >= 61 && nextTick <= 65) offset -= 0.2D * partial;
        else if (nextTick >= 66 && nextTick <= 70) offset -= 0.05D * partial;
        else if (nextTick >= 71 && nextTick <= 73) offset += 1.5D * partial;
        else if (nextTick >= 74 && nextTick <= 82) offset += 3.5D * partial;
        else if (nextTick >= 83 && nextTick <= 85) offset += 1.5D * partial;
        else if (nextTick >= 86 && nextTick <= 88) offset += 0.1D * partial;
        return offset;
    }

    private static double verticalOffset(double score) {
        if (score <= 10) return -11.0D;
        if (score <= 15) return -11.0D + (score - 10) * 2.0D;
        if (score <= 17) return -1.0D + (score - 15) * 0.2D;
        if (score >= 93) return -0.6D - (score - 92) * 2.0D;
        return -0.6D;
    }

    @Nullable
    private ServerPlayer owner() {
        return ownerId == null || !(level() instanceof ServerLevel server)
                ? null : server.getServer().getPlayerList().getPlayer(ownerId);
    }

    @Nullable
    private LivingEntity livingOwner() {
        if (ownerId == null || !(level() instanceof ServerLevel server)) return null;
        Entity entity = server.getEntity(ownerId);
        return entity instanceof LivingEntity living ? living : owner();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) ownerId = tag.getUUID("Owner");
        hostile = tag.getBoolean("Hostile");
        baseX = tag.getDouble("BaseX");
        baseY = tag.getDouble("BaseY");
        baseZ = tag.getDouble("BaseZ");
        terrainOffset = tag.getDouble("TerrainOffset");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) tag.putUUID("Owner", ownerId);
        tag.putBoolean("Hostile", hostile);
        tag.putDouble("BaseX", baseX);
        tag.putDouble("BaseY", baseY);
        tag.putDouble("BaseZ", baseZ);
        tag.putDouble("TerrainOffset", terrainOffset);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0D;
    }
}
