package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

/**
 * Gariheuz (加里赫兹): the crossbow-wielding pillager half of the B5 dual fight.
 * The original summon sets Health 1000 with MaxHealth 800. LivingEntity clamps
 * the loaded health to the max attribute, so Gari's effective durability is 800.
 */
public final class GariBossEntity extends Pillager {
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("luisb1202.functions.bossfight.b5.vida.ini.1"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
    private boolean bossBarEnabled = true;

    public GariBossEntity(EntityType<GariBossEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setSilent(true);
    }

    @Nullable
    public static GariBossEntity createPrepared(ServerLevel level) {
        GariBossEntity entity = ModEntities.GARI.get().create(level);
        if (entity != null) {
            entity.applyOriginalSpawnData();
            entity.setHealth(entity.getMaxHealth());
        }
        return entity;
    }

    @Override
    protected void dropFromLootTable(DamageSource source, boolean causedByPlayer) {
        // Source summon uses DeathLootTable:"empty".
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Pillager.createAttributes()
                .add(Attributes.MAX_HEALTH, 800.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D)
                .add(Attributes.ARMOR, 7.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
        applyOriginalSpawnData();
        return data;
    }

    private void applyOriginalSpawnData() {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setDropChance(slot, 0.0F);
        }
        setCustomName(Component.translatable("entity.pillager.6.name.1"));
        setCustomNameVisible(true);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (bossBarEnabled) bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel) {
            bossEvent.setProgress(getHealth() / Math.max(1.0F, getMaxHealth()));
        }
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !isDeadOrDying() && level() instanceof ServerLevel server) {
            B5EncounterManager.onBossHit(server, false, position());
        }
        return hurt;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (level() instanceof ServerLevel) bossEvent.removeAllPlayers();
        super.remove(reason);
    }

    public void setBossBarEnabled(boolean enabled) {
        this.bossBarEnabled = enabled;
        if (!enabled && level() instanceof ServerLevel) {
            bossEvent.removeAllPlayers();
        } else if (enabled && level() instanceof ServerLevel server) {
            for (ServerPlayer player : server.players()) bossEvent.addPlayer(player);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        bossBarEnabled = !tag.contains("B5BossBarEnabled") || tag.getBoolean("B5BossBarEnabled");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("B5BossBarEnabled", bossBarEnabled);
    }
}
