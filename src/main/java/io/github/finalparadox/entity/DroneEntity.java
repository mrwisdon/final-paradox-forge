package io.github.finalparadox.entity;

import io.github.finalparadox.item.ReconDroneItem;
import io.github.finalparadox.registry.ModEntities;
import io.github.finalparadox.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

/**
 * Server-authoritative flying camera drone. The owner's body stays at the
 * deploy anchor while the client camera follows this entity; movement and bomb
 * drops are validated on the server.
 */
public final class DroneEntity extends Entity {
    public static final int FLAG_FORWARD = 1;
    public static final int FLAG_BACK = 2;
    public static final int FLAG_LEFT = 4;
    public static final int FLAG_RIGHT = 8;
    public static final int FLAG_UP = 16;
    public static final int FLAG_DOWN = 32;

    public static final int MAX_BOMBS = 8;
    public static final int DEPLOY_COOLDOWN_TICKS = 20 * 180;

    private static final String ACTIVE_KEY = "finalparadox.recon_drone_active";
    private static final String ANCHOR_X_KEY = "finalparadox.recon_drone_anchor_x";
    private static final String ANCHOR_Y_KEY = "finalparadox.recon_drone_anchor_y";
    private static final String ANCHOR_Z_KEY = "finalparadox.recon_drone_anchor_z";
    private static final String ANCHOR_DIMENSION_KEY = "finalparadox.recon_drone_anchor_dimension";

    private static final float MOVE_SPEED = 0.45F;
    private static final int HOVER_CLEANUP_TICKS = 20;

    private static final EntityDataAccessor<Integer> DATA_BOMBS =
            SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.INT);

    private UUID ownerId;
    private int inputState;
    private int hoverTicks;
    private double anchorX;
    private double anchorY;
    private double anchorZ;

    public DroneEntity(EntityType<DroneEntity> type, Level level) {
        super(type, level);
        noCulling = true;
        setNoGravity(true);
    }

    public static boolean deploy(ServerPlayer player) {
        if (isActiveFor(player)) {
            return false;
        }
        DroneEntity drone = new DroneEntity(ModEntities.RECON_DRONE.get(), player.serverLevel());
        Vec3 look = player.getLookAngle();
        Vec3 spawn = player.getEyePosition().add(look.scale(2.5D)).subtract(0.0D, 0.5D, 0.0D);
        drone.setPos(spawn);
        drone.setYRot(player.getYRot());
        drone.setXRot(player.getXRot());
        drone.ownerId = player.getUUID();
        drone.anchorX = player.getX();
        drone.anchorY = player.getY();
        drone.anchorZ = player.getZ();
        drone.entityData.set(DATA_BOMBS, MAX_BOMBS);
        if (!player.serverLevel().addFreshEntity(drone)) {
            return false;
        }

        player.setCamera(drone);
        CompoundTag data = player.getPersistentData();
        data.putBoolean(ACTIVE_KEY, true);
        data.putDouble(ANCHOR_X_KEY, player.getX());
        data.putDouble(ANCHOR_Y_KEY, player.getY());
        data.putDouble(ANCHOR_Z_KEY, player.getZ());
        data.putString(ANCHOR_DIMENSION_KEY, player.level().dimension().location().toString());
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);
        player.getCooldowns().addCooldown(ModItems.RECON_DRONE.get(), DEPLOY_COOLDOWN_TICKS);
        player.getPersistentData().putInt(ReconDroneItem.COOLDOWN_KEY, DEPLOY_COOLDOWN_TICKS);

        ServerLevel level = player.serverLevel();
        level.playSound(null, drone.blockPosition(), SoundEvents.ARMOR_EQUIP_NETHERITE,
                SoundSource.PLAYERS, 1.0F, 0.8F);
        level.sendParticles(ParticleTypes.CLOUD, spawn.x, spawn.y, spawn.z,
                20, 0.3D, 0.2D, 0.3D, 0.05D);
        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                        "message.finalparadox.recon_drone.controls.title"), true);
        return true;
    }

    public static boolean isActiveFor(ServerPlayer player) {
        return player.getPersistentData().getBoolean(ACTIVE_KEY)
                && player.getCamera() instanceof DroneEntity drone
                && player.getUUID().equals(drone.ownerId);
    }

    public static DroneEntity findFor(ServerPlayer player) {
        if (player.getCamera() instanceof DroneEntity drone
                && player.getUUID().equals(drone.ownerId)
                && !drone.isRemoved()) {
            return drone;
        }
        if (!player.getPersistentData().getBoolean(ACTIVE_KEY)) {
            return null;
        }
        for (Entity entity : player.serverLevel().getAllEntities()) {
            if (entity instanceof DroneEntity drone
                    && player.getUUID().equals(drone.ownerId)
                    && !drone.isRemoved()) {
                return drone;
            }
        }
        return null;
    }

    public static void endFor(ServerPlayer player) {
        DroneEntity drone = findFor(player);
        if (drone != null) {
            drone.endMode(player);
        } else if (player.getPersistentData().getBoolean(ACTIVE_KEY)) {
            player.getPersistentData().remove(ACTIVE_KEY);
            player.getPersistentData().remove(ANCHOR_X_KEY);
            player.getPersistentData().remove(ANCHOR_Y_KEY);
            player.getPersistentData().remove(ANCHOR_Z_KEY);
            player.getPersistentData().remove(ANCHOR_DIMENSION_KEY);
        }
    }

    /** Clears drone mode without restoring the camera or position (death/logout). */
    public static void cancelFor(ServerPlayer player) {
        DroneEntity drone = findFor(player);
        if (drone != null) {
            drone.discard();
        }
        player.getPersistentData().remove(ACTIVE_KEY);
        player.getPersistentData().remove(ANCHOR_X_KEY);
        player.getPersistentData().remove(ANCHOR_Y_KEY);
        player.getPersistentData().remove(ANCHOR_Z_KEY);
        player.getPersistentData().remove(ANCHOR_DIMENSION_KEY);
    }

    public void onInput(ServerPlayer player, int state) {
        if (isControlledBy(player)) {
            inputState = state;
        }
    }

    public boolean dropBomb(ServerPlayer player) {
        if (!isControlledBy(player)) {
            return false;
        }
        int bombs = getBombs();
        if (bombs <= 0) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.finalparadox.recon_drone.no_bombs"), true);
            return false;
        }

        DroneBombEntity bomb = new DroneBombEntity(
                ModEntities.RECON_DRONE_BOMB.get(), level());
        bomb.setPos(getX(), getY() - 0.2D, getZ());
        bomb.setOwner(player.getUUID());
        level().addFreshEntity(bomb);
        entityData.set(DATA_BOMBS, bombs - 1);
        level().playSound(null, blockPosition(), SoundEvents.TNT_PRIMED,
                SoundSource.PLAYERS, 1.0F, 0.9F);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SMOKE, getX(), getY() - 0.2D, getZ(),
                    8, 0.1D, 0.1D, 0.1D, 0.02D);
        }
        return true;
    }

    public int getBombs() {
        return entityData.get(DATA_BOMBS);
    }

    public void freezeBody(ServerPlayer player) {
        if (!isControlledBy(player)) {
            return;
        }
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);
        if (player.position().distanceToSqr(anchorX, anchorY, anchorZ) > 1.0E-4D) {
            player.setPos(anchorX, anchorY, anchorZ);
            player.hurtMarked = true;
        }
    }

    public static void freezeFor(ServerPlayer player) {
        DroneEntity drone = findFor(player);
        if (drone != null) {
            drone.freezeBody(player);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (!(level() instanceof ServerLevel server)) {
            discard();
            return;
        }
        ServerPlayer owner = ownerId == null
                ? null : server.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null || !isActiveFor(owner)) {
            if (++hoverTicks > HOVER_CLEANUP_TICKS) {
                discard();
            }
            return;
        }
        hoverTicks = 0;
        setYRot(owner.getYRot());
        setXRot(owner.getXRot());
        applyInput();
        freezeBody(owner);
    }

    private void applyInput() {
        Vec3 move = Vec3.ZERO;
        Vec3 forward = Vec3.directionFromRotation(0.0F, getYRot());
        Vec3 right = Vec3.directionFromRotation(0.0F, getYRot() + 90.0F);
        if ((inputState & FLAG_FORWARD) != 0) {
            move = move.add(forward);
        }
        if ((inputState & FLAG_BACK) != 0) {
            move = move.subtract(forward);
        }
        if ((inputState & FLAG_LEFT) != 0) {
            move = move.subtract(right);
        }
        if ((inputState & FLAG_RIGHT) != 0) {
            move = move.add(right);
        }
        if ((inputState & FLAG_UP) != 0) {
            move = move.add(0.0D, 1.0D, 0.0D);
        }
        if ((inputState & FLAG_DOWN) != 0) {
            move = move.subtract(0.0D, 1.0D, 0.0D);
        }
        if (move.lengthSqr() <= 0.0D) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        Vec3 horizontal = new Vec3(move.x, 0.0D, move.z);
        Vec3 resolved;
        if (horizontal.lengthSqr() > 0.0D) {
            resolved = horizontal.normalize().scale(MOVE_SPEED)
                    .add(0.0D, move.y * MOVE_SPEED, 0.0D);
        } else {
            resolved = new Vec3(0.0D, move.y * MOVE_SPEED, 0.0D);
        }
        setDeltaMovement(resolved);
        move(MoverType.SELF, resolved);
    }

    private boolean isControlledBy(ServerPlayer player) {
        return player.getUUID().equals(ownerId) && isActiveFor(player);
    }

    private void endMode(ServerPlayer player) {
        player.setCamera(null);
        CompoundTag data = player.getPersistentData();
        data.remove(ACTIVE_KEY);
        data.remove(ANCHOR_X_KEY);
        data.remove(ANCHOR_Y_KEY);
        data.remove(ANCHOR_Z_KEY);
        data.remove(ANCHOR_DIMENSION_KEY);
        player.setNoGravity(false);
        player.setDeltaMovement(Vec3.ZERO);
        if (player.position().distanceToSqr(anchorX, anchorY, anchorZ) > 0.01D) {
            player.connection.teleport(anchorX, anchorY, anchorZ,
                    player.getYRot(), player.getXRot());
        }
        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                        "message.finalparadox.recon_drone.exited"), true);
        if (level() instanceof ServerLevel server) {
            server.playSound(null, blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER,
                    SoundSource.PLAYERS, 1.0F, 1.4F);
        }
        discard();
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_BOMBS, MAX_BOMBS);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            ownerId = tag.getUUID("Owner");
        }
        inputState = tag.getInt("InputState");
        entityData.set(DATA_BOMBS, tag.getInt("Bombs"));
        anchorX = tag.getDouble("AnchorX");
        anchorY = tag.getDouble("AnchorY");
        anchorZ = tag.getDouble("AnchorZ");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
        tag.putInt("InputState", inputState);
        tag.putInt("Bombs", getBombs());
        tag.putDouble("AnchorX", anchorX);
        tag.putDouble("AnchorY", anchorY);
        tag.putDouble("AnchorZ", anchorZ);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
