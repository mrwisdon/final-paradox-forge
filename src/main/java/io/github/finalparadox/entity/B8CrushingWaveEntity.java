package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Server-authoritative terrain-following wave from the Crushing affix. */
public final class B8CrushingWaveEntity extends Entity {
    private static final double STEP = 0.7D;
    private static final int LIFETIME = 18;
    private static final double HIT_RADIUS = 2.5D;

    private Vec3 direction = new Vec3(0.0D, 0.0D, 1.0D);
    private final Set<UUID> damagedRovers = new HashSet<>();

    public B8CrushingWaveEntity(EntityType<B8CrushingWaveEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
    }

    public static B8CrushingWaveEntity spawn(ServerLevel level, Vec3 origin, Vec3 direction) {
        B8CrushingWaveEntity wave = new B8CrushingWaveEntity(
                ModEntities.B8_CRUSHING_WAVE.get(), level);
        Vec3 flat = new Vec3(direction.x, 0.0D, direction.z);
        if (flat.lengthSqr() < 1.0E-6D) flat = new Vec3(0.0D, 0.0D, 1.0D);
        wave.direction = flat.normalize();
        wave.setPos(origin.x, origin.y, origin.z);
        wave.addTag("b8_h3_crushing_wave");
        level.addFreshEntity(wave);
        return wave;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel server) || !B8EncounterManager.isActive(server)
                || tickCount >= LIFETIME) {
            discard();
            return;
        }

        double nextX = getX() + direction.x * STEP;
        double nextZ = getZ() + direction.z * STEP;
        Double nextY = followTerrain(server, nextX, getY(), nextZ);
        if (nextY == null) {
            discard();
            return;
        }
        setPos(nextX, nextY, nextZ);
        emitWave(server);
        hitPlayers(server);
        hitRovers(server);
    }

    private Double followTerrain(ServerLevel server, double x, double currentY, double z) {
        int blockX = net.minecraft.util.Mth.floor(x);
        int blockZ = net.minecraft.util.Mth.floor(z);
        int y = net.minecraft.util.Mth.floor(currentY);
        int steps = 0;
        while (!isPassable(server, new BlockPos(blockX, y, blockZ)) && steps++ < 8) y++;
        steps = 0;
        while (isPassable(server, new BlockPos(blockX, y - 1, blockZ)) && steps++ < 8) y--;
        BlockPos feet = new BlockPos(blockX, y, blockZ);
        if (!isPassable(server, feet) || isPassable(server, feet.below())
                || !isPassable(server, feet.above()) || !isPassable(server, feet.above(2))) {
            return null;
        }
        return (double) y;
    }

    private static boolean isPassable(ServerLevel server, BlockPos pos) {
        BlockState state = server.getBlockState(pos);
        return state.getCollisionShape(server, pos).isEmpty();
    }

    private void emitWave(ServerLevel server) {
        Vec3 side = new Vec3(direction.z, 0.0D, -direction.x);
        server.sendParticles(ParticleTypes.EXPLOSION,
                getX(), getY(), getZ(), 1, 0.0D, 0.0D, 0.0D, 1.0D);
        for (double offset : new double[]{-1.0D, -0.5D, 0.5D, 1.0D}) {
            Vec3 point = position().add(side.scale(offset));
            server.sendParticles(ParticleTypes.SPIT,
                    point.x, point.y, point.z, 1, 0.0D, 0.3D, 0.0D, 1.0D);
        }
        DustParticleOptions red = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.5F);
        for (double offset : new double[]{-1.5D, 1.5D}) {
            Vec3 point = position().add(side.scale(offset)).add(0.0D, 0.2D, 0.0D);
            server.sendParticles(red, point.x, point.y, point.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        server.playSound(null, blockPosition(), SoundEvents.NETHER_BRICKS_BREAK,
                SoundSource.MASTER, 0.3F, 0.4F);
    }

    private void hitPlayers(ServerLevel server) {
        double radiusSqr = HIT_RADIUS * HIT_RADIUS;
        for (ServerPlayer player : server.getEntitiesOfClass(
                ServerPlayer.class, getBoundingBox().inflate(HIT_RADIUS),
                player -> !player.isSpectator() && player.distanceToSqr(this) <= radiusSqr)) {
            player.addEffect(new MobEffectInstance(MobEffects.HARM, 20, 0,
                    false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
        }
    }

    private void hitRovers(ServerLevel server) {
        Vec3 sample = position().add(0.0D, 2.0D, 0.0D);
        double radiusSqr = HIT_RADIUS * HIT_RADIUS;
        for (TerrastalkerRoverEntity rover : server.getEntitiesOfClass(
                TerrastalkerRoverEntity.class, new AABB(sample, sample).inflate(HIT_RADIUS),
                rover -> rover.isAlive() && rover.isEncounterMode()
                        && rover.position().distanceToSqr(sample) <= radiusSqr
                        && damagedRovers.add(rover.getUUID()))) {
            rover.damageEnergy(4);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        direction = new Vec3(tag.getDouble("DirX"), 0.0D, tag.getDouble("DirZ"));
        ListTag list = tag.getList("DamagedRovers", Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) {
            damagedRovers.add(list.getCompound(index).getUUID("Uuid"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("DirX", direction.x);
        tag.putDouble("DirZ", direction.z);
        ListTag list = new ListTag();
        for (UUID uuid : damagedRovers) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Uuid", uuid);
            list.add(entry);
        }
        tag.put("DamagedRovers", list);
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
