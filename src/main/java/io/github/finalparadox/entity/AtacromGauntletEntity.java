package io.github.finalparadox.entity;

import io.github.finalparadox.item.AtacromGauntletItem;
import io.github.finalparadox.registry.ModEntities;
import io.github.finalparadox.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public final class AtacromGauntletEntity extends Entity {
    private UUID ownerId;

    public AtacromGauntletEntity(EntityType<AtacromGauntletEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
    }

    public static void ensure(ServerPlayer owner) {
        AtacromGauntletEntity existing = find(owner);
        if (existing != null) {
            return;
        }
        AtacromGauntletEntity gauntlet =
                new AtacromGauntletEntity(ModEntities.ATACROM_GAUNTLET_VISUAL.get(), owner.serverLevel());
        gauntlet.ownerId = owner.getUUID();
        gauntlet.follow(owner);
        owner.serverLevel().addFreshEntity(gauntlet);
    }

    private static AtacromGauntletEntity find(ServerPlayer owner) {
        return owner.serverLevel().getEntitiesOfClass(
                        AtacromGauntletEntity.class, owner.getBoundingBox().inflate(8.0D),
                        entity -> owner.getUUID().equals(entity.ownerId))
                .stream().findFirst().orElse(null);
    }

    private void follow(ServerPlayer owner) {
        Vec3 forward = Vec3.directionFromRotation(0.0F, owner.getYRot());
        Vec3 left = new Vec3(forward.z, 0.0D, -forward.x);
        Vec3 position = owner.position().add(left.scale(0.78D)).add(forward.scale(0.72D))
                .add(0.0D, 1.05D, 0.0D);
        setPos(position);
        setYRot(owner.getYRot());
    }

    public ItemStack displayStack() {
        return new ItemStack(ModItems.ATACROM_GAUNTLET_DISPLAY.get());
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (!(level() instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }
        ServerPlayer owner = ownerId == null
                ? null : serverLevel.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null || !AtacromGauntletItem.isGuarding(owner)) {
            discard();
            return;
        }
        follow(owner);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            ownerId = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
