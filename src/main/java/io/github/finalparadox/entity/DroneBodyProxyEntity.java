package io.github.finalparadox.entity;

import io.github.finalparadox.FinalParadox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Vulnerable body left at the recon-drone deploy point. Damage is forwarded to
 * the real player, whose ordinary damage is suppressed while riding the drone.
 */
public final class DroneBodyProxyEntity extends LivingEntity {
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER =
            SynchedEntityData.defineId(
                    DroneBodyProxyEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final int ORPHAN_CLEANUP_TICKS = 20;

    private UUID ownerId;
    private int orphanTicks;
    private boolean anchorTicketActive;
    private int ticketChunkX;
    private int ticketChunkZ;
    private double anchorX;
    private double anchorY;
    private double anchorZ;

    public DroneBodyProxyEntity(EntityType<? extends DroneBodyProxyEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public void initializeFrom(ServerPlayer owner, Vec3 anchor) {
        ownerId = owner.getUUID();
        entityData.set(DATA_OWNER, Optional.of(ownerId));
        anchorX = anchor.x;
        anchorY = anchor.y;
        anchorZ = anchor.z;
        setPos(anchor);
        setYRot(owner.getYRot());
        setXRot(owner.getXRot());
        setCustomName(owner.getDisplayName().copy());
        setCustomNameVisible(false);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setItemSlot(slot, owner.getItemBySlot(slot).copy());
        }
        getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                .setBaseValue(Math.max(1.0D, owner.getMaxHealth()));
        setHealth(owner.getHealth());
    }

    public UUID getOwnerId() {
        return ownerId != null ? ownerId : entityData.get(DATA_OWNER).orElse(null);
    }

    public boolean acquireAnchorTicket() {
        if (!(level() instanceof ServerLevel server)) {
            return false;
        }
        ChunkPos chunk = chunkPosition();
        ticketChunkX = chunk.x;
        ticketChunkZ = chunk.z;
        anchorTicketActive = ForgeChunkManager.forceChunk(
                server, FinalParadox.MOD_ID, this,
                ticketChunkX, ticketChunkZ, true, true);
        return anchorTicketActive;
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        ServerPlayer owner = getOwnerId() == null
                ? null : server.getServer().getPlayerList().getPlayer(getOwnerId());
        if (owner == null || !DroneEntity.isActiveFor(owner)
                || DroneEntity.findBodyProxyFor(owner) != this) {
            if (++orphanTicks > ORPHAN_CLEANUP_TICKS) {
                discard();
            }
            return;
        }
        orphanTicks = 0;
        if (position().distanceToSqr(anchorX, anchorY, anchorZ) > 1.0E-6D) {
            setPos(anchorX, anchorY, anchorZ);
        }
        if (!anchorTicketActive) {
            acquireAnchorTicket();
        }
        getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                .setBaseValue(Math.max(1.0D, owner.getMaxHealth()));
        setHealth(owner.getHealth());
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack ownerStack = owner.getItemBySlot(slot);
            if (!ItemStack.matches(ownerStack, getItemBySlot(slot))) {
                setItemSlot(slot, ownerStack.copy());
            }
        }
        setYRot(owner.getYRot());
        setXRot(owner.getXRot());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!(level() instanceof ServerLevel server) || isRemoved()) {
            return false;
        }
        UUID id = getOwnerId();
        ServerPlayer owner = id == null
                ? null : server.getServer().getPlayerList().getPlayer(id);
        if (owner == null) {
            return false;
        }
        boolean damaged = DroneEntity.forwardBodyDamage(owner, this, source, amount);
        if (damaged && !isRemoved()) {
            setHealth(Math.max(0.01F, owner.getHealth()));
            hurtTime = hurtDuration = 10;
            level().broadcastEntityEvent(this, (byte) 2);
        }
        return damaged;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        releaseAnchorTicket();
        super.remove(reason);
    }

    private void releaseAnchorTicket() {
        if (anchorTicketActive && level() instanceof ServerLevel server) {
            ForgeChunkManager.forceChunk(
                    server, FinalParadox.MOD_ID, this,
                    ticketChunkX, ticketChunkZ, false, true);
            anchorTicketActive = false;
        }
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return List.of(
                getItemBySlot(EquipmentSlot.FEET),
                getItemBySlot(EquipmentSlot.LEGS),
                getItemBySlot(EquipmentSlot.CHEST),
                getItemBySlot(EquipmentSlot.HEAD));
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> getPersistentData().contains("MainHand")
                    ? ItemStack.of(getPersistentData().getCompound("MainHand")) : ItemStack.EMPTY;
            case OFFHAND -> getPersistentData().contains("OffHand")
                    ? ItemStack.of(getPersistentData().getCompound("OffHand")) : ItemStack.EMPTY;
            case FEET -> readEquipment("Feet");
            case LEGS -> readEquipment("Legs");
            case CHEST -> readEquipment("Chest");
            case HEAD -> readEquipment("Head");
        };
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        String key = switch (slot) {
            case MAINHAND -> "MainHand";
            case OFFHAND -> "OffHand";
            case FEET -> "Feet";
            case LEGS -> "Legs";
            case CHEST -> "Chest";
            case HEAD -> "Head";
        };
        if (stack.isEmpty()) {
            getPersistentData().remove(key);
        } else {
            getPersistentData().put(key, stack.save(new CompoundTag()));
        }
    }

    private ItemStack readEquipment(String key) {
        return getPersistentData().contains(key)
                ? ItemStack.of(getPersistentData().getCompound(key)) : ItemStack.EMPTY;
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_OWNER, Optional.empty());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            ownerId = tag.getUUID("Owner");
            entityData.set(DATA_OWNER, Optional.of(ownerId));
        }
        anchorX = tag.getDouble("AnchorX");
        anchorY = tag.getDouble("AnchorY");
        anchorZ = tag.getDouble("AnchorZ");
        setPos(anchorX, anchorY, anchorZ);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (getOwnerId() != null) tag.putUUID("Owner", getOwnerId());
        tag.putDouble("AnchorX", anchorX);
        tag.putDouble("AnchorY", anchorY);
        tag.putDouble("AnchorZ", anchorZ);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
