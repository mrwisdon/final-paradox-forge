package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Koyomi (柯约米): trident-wielding zombie half of the B5 dual fight.
 * Matches `bossfight/b5/summon_koyo.mcfunction` (equipment, attributes,
 * permanent damage resistance). The encounter itself is driven by
 * {@link B5EncounterManager}.
 */
public final class KoyomiBossEntity extends Zombie {
    private static final String SKIN_VALUE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv"
                    + "OWNmN2M3Y2IyYWZhYTE3ZTk0YTA0ODZmYmRiNjk0ODU0MGI1Zjk0NTFlZDVhMzY4OTQ5M2U3OTQxZGE2MjgxOCJ9fX0=";

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("luisb1202.functions.bossfight.b5.setvida.1"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
    private boolean bossBarEnabled = true;
    private BlockPos arenaAnchor;

    public KoyomiBossEntity(EntityType<KoyomiBossEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    @Nullable
    public static KoyomiBossEntity createPrepared(ServerLevel level) {
        KoyomiBossEntity entity = ModEntities.KOYOMI.get().create(level);
        if (entity != null) {
            entity.applyOriginalSpawnData();
            entity.setHealth(entity.getMaxHealth());
        }
        return entity;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 920.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.ARMOR, 20.0D);
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
        setItemSlot(EquipmentSlot.MAINHAND, trident());
        setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        setItemSlot(EquipmentSlot.LEGS, coloredLeather(Items.LEATHER_LEGGINGS, 5283427, 2));
        setItemSlot(EquipmentSlot.CHEST, coloredLeather(Items.LEATHER_CHESTPLATE, 3038778, 3));
        setItemSlot(EquipmentSlot.HEAD, koyomiHead());
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setDropChance(slot, 0.0F);
        }
        // summon_koyo ActiveEffects Id 11 in the source version: resistance I.
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 999999, 0, false, false));
        setCustomName(Component.translatable("luisb1202.functions.bossfight.b5.summon_koyo.1"));
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
            B5EncounterManager.onBossHit(server, true, position());
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

    public void setArenaAnchor(BlockPos anchor) {
        this.arenaAnchor = anchor.immutable();
    }

    /** Read-only arena anchor used to attribute this waiting boss to its arena. */
    public BlockPos arenaAnchor() {
        return arenaAnchor;
    }

    private static ItemStack trident() {
        ItemStack stack = new ItemStack(Items.TRIDENT);
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        stack.getOrCreateTag().putInt("RepairCost", 999999);
        return stack;
    }

    private static ItemStack coloredLeather(net.minecraft.world.item.Item item, int color, int protLevel) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTagElement("display").putInt("color", color);
        stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, protLevel);
        return stack;
    }

    private static ItemStack koyomiHead() {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        CompoundTag owner = new CompoundTag();
        owner.putUUID("Id", skullId());
        CompoundTag properties = new CompoundTag();
        ListTag textures = new ListTag();
        CompoundTag texture = new CompoundTag();
        texture.putString("Value", SKIN_VALUE);
        textures.add(texture);
        properties.put("textures", textures);
        owner.put("Properties", properties);
        head.getOrCreateTag().put("SkullOwner", owner);
        return head;
    }

    private static UUID skullId() {
        int a = -1799677634, b = 1587104164, c = -1678250160, d = -84111072;
        return new UUID(((long) a << 32) | (b & 0xFFFFFFFFL),
                ((long) c << 32) | (d & 0xFFFFFFFFL));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        bossBarEnabled = !tag.contains("B5BossBarEnabled") || tag.getBoolean("B5BossBarEnabled");
        if (tag.contains("B5AnchorX")) {
            arenaAnchor = new BlockPos(tag.getInt("B5AnchorX"), tag.getInt("B5AnchorY"), tag.getInt("B5AnchorZ"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("B5BossBarEnabled", bossBarEnabled);
        if (arenaAnchor != null) {
            tag.putInt("B5AnchorX", arenaAnchor.getX());
            tag.putInt("B5AnchorY", arenaAnchor.getY());
            tag.putInt("B5AnchorZ", arenaAnchor.getZ());
        }
    }
}
