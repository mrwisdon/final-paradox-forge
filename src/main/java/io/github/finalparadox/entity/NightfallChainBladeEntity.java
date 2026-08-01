package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModEntities;
import io.github.finalparadox.registry.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public final class NightfallChainBladeEntity extends Entity {
    private static final int MAX_LIFETIME = 100;
    private static final double VICTORY_DROP_HEIGHT = 7.0D;
    private static final double VICTORY_DROP_SPEED = 0.75D;
    private static final EntityDataAccessor<Boolean> DATA_VICTORY_REWARD =
            SynchedEntityData.defineId(
                    NightfallChainBladeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_REWARD_LANDED =
            SynchedEntityData.defineId(
                    NightfallChainBladeEntity.class, EntityDataSerializers.BOOLEAN);
    private boolean bossOwned;
    private boolean victoryReward;
    private boolean rewardLanded;
    private double rewardGroundY;
    @Nullable
    private UUID bossOwner;

    public NightfallChainBladeEntity(EntityType<NightfallChainBladeEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
    }

    public static NightfallChainBladeEntity spawn(ServerLevel level, Vec3 ground, float yaw) {
        NightfallChainBladeEntity blade =
                new NightfallChainBladeEntity(ModEntities.NIGHTFALL_CHAIN_BLADE.get(), level);
        blade.moveTo(ground.x, ground.y, ground.z, yaw, 0.0F);
        level.addFreshEntity(blade);
        return blade;
    }

    public static NightfallChainBladeEntity spawnBossBlade(
            ServerLevel level, Vec3 position, float yaw, UUID owner) {
        NightfallChainBladeEntity blade =
                new NightfallChainBladeEntity(ModEntities.NIGHTFALL_CHAIN_BLADE.get(), level);
        blade.bossOwned = true;
        blade.bossOwner = owner;
        blade.moveTo(position.x, position.y, position.z, yaw, 0.0F);
        level.addFreshEntity(blade);
        return blade;
    }

    public static NightfallChainBladeEntity spawnVictoryReward(
            ServerLevel level, Vec3 ground, float yaw) {
        NightfallChainBladeEntity blade =
                new NightfallChainBladeEntity(ModEntities.NIGHTFALL_CHAIN_BLADE.get(), level);
        blade.victoryReward = true;
        blade.rewardGroundY = ground.y;
        blade.setVictoryRewardData(true);
        blade.setRewardLanded(false);
        blade.moveTo(
                ground.x, ground.y + VICTORY_DROP_HEIGHT, ground.z, yaw, 0.0F);
        level.addFreshEntity(blade);
        return blade;
    }

    public boolean isBossOwnedBy(UUID owner) {
        return bossOwned && owner.equals(bossOwner);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_VICTORY_REWARD, false);
        entityData.define(DATA_REWARD_LANDED, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (victoryReward) {
            tickVictoryReward();
        } else if (!bossOwned && tickCount > MAX_LIFETIME) {
            discard();
        }
    }

    private void tickVictoryReward() {
        if (rewardLanded) {
            return;
        }
        double nextY = getY() - VICTORY_DROP_SPEED;
        if (nextY > rewardGroundY) {
            setPos(getX(), nextY, getZ());
            return;
        }
        setPos(getX(), rewardGroundY, getZ());
        rewardLanded = true;
        setRewardLanded(true);
        if (level() instanceof ServerLevel server) {
            server.playSound(null, blockPosition(), SoundEvents.ANVIL_LAND,
                    SoundSource.MASTER, 1.1F, 0.8F);
            server.playSound(null, blockPosition(), SoundEvents.TRIDENT_HIT_GROUND,
                    SoundSource.MASTER, 0.9F, 0.7F);
            server.sendParticles(
                    new ItemParticleOption(
                            ParticleTypes.ITEM, new ItemStack(net.minecraft.world.item.Items.STONE)),
                    getX(), getY() + 0.15D, getZ(),
                    28, 0.45D, 0.12D, 0.45D, 0.08D);
            server.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    getX(), getY() + 0.35D, getZ(),
                    18, 0.32D, 0.28D, 0.32D, 0.025D);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!isVictoryReward() || !isRewardLanded()) {
            return InteractionResult.PASS;
        }
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack reward = ModItems.NIGHTFALL.get().getDefaultInstance();
        if (!player.getInventory().add(reward)) {
            return InteractionResult.CONSUME;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.broadcastChanges();
        }
        level().playSound(null, blockPosition(), SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS, 1.0F, 0.8F);
        discard();
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isPickable() {
        return isVictoryReward() && isRewardLanded();
    }

    public boolean isVictoryReward() {
        return level().isClientSide ? entityData.get(DATA_VICTORY_REWARD) : victoryReward;
    }

    public boolean isRewardLanded() {
        return level().isClientSide ? entityData.get(DATA_REWARD_LANDED) : rewardLanded;
    }

    private void setVictoryRewardData(boolean reward) {
        entityData.set(DATA_VICTORY_REWARD, reward);
    }

    private void setRewardLanded(boolean landed) {
        entityData.set(DATA_REWARD_LANDED, landed);
    }

    @Override
    public boolean shouldBeSaved() {
        return bossOwned || victoryReward;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        bossOwned = tag.getBoolean("MarawTharBossOwned");
        bossOwner = tag.hasUUID("MarawTharBossOwner") ? tag.getUUID("MarawTharBossOwner") : null;
        victoryReward = tag.getBoolean("MarawTharVictoryReward");
        rewardLanded = tag.getBoolean("MarawTharRewardLanded");
        rewardGroundY = tag.getDouble("MarawTharRewardGroundY");
        setVictoryRewardData(victoryReward);
        setRewardLanded(rewardLanded);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("MarawTharBossOwned", bossOwned);
        if (bossOwner != null) {
            tag.putUUID("MarawTharBossOwner", bossOwner);
        }
        tag.putBoolean("MarawTharVictoryReward", victoryReward);
        tag.putBoolean("MarawTharRewardLanded", rewardLanded);
        tag.putDouble("MarawTharRewardGroundY", rewardGroundY);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
