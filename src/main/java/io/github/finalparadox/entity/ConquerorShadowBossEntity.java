package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Direct, arena-relative port of Final Paradox v1.1.15 bossfight/b6.
 *
 * <p>Command counters, ordering, selectors, particles, armor-stand laser construction and
 * translated text are preserved. Only the original fixed arena coordinates are replaced by
 * offsets from the entity's spawn position.</p>
 */
public final class ConquerorShadowBossEntity extends Skeleton {
    private static final int COUNTDOWN = 0;
    private static final int FIGHT = 1;
    private static final int TELEPORT = 2;
    private static final int LASER = 3;
    private static final int JUDGMENT = 4;
    private static final int COMPLETE = 5;
    private static final int DEFEAT = 6;
    private static final int ENCOUNTER_RADIUS = 64;
    private static final String MEMORY_OWNER = "finalparadox.conqueror_shadow_owner";
    private static final String TRANSIENT_OWNER = "finalparadox.conqueror_shadow_transient_owner";
    private static final String HEAD_TEXTURE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjdjMDJiYzVjOTgzYmUwN2QyODVkMDk1ZTg3ZTRhNDExYjk3ZmE0ZmQ1M2FhNjc5NTA2YzhmMzIwMjhmN2FkOCJ9fX0=";
    private static final DustParticleOptions WHITE =
            new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 1.0F);
    private static final DustParticleOptions RED =
            new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 2.0F);
    private static final DustParticleOptions TELEPORT_RED =
            new DustParticleOptions(new Vector3f(0.714F, 0.141F, 0.141F), 2.0F);
    private static final int[] BLACK_RAIN_RADII = {9, 12, 15, 18, 21, 24, 27};
    private static final int[] BLACK_RAIN_COUNTS = {8, 8, 12, 12, 12, 12, 12};

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.finalparadox.conqueror_shadow.bossbar"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
    private final List<Beam> beams = new ArrayList<>();
    private final List<MemoryPortal> memoryPortals = new ArrayList<>();
    private final List<JudgmentWave> judgmentWaves = new ArrayList<>();
    private final List<VerticalProjectile> verticalProjectiles = new ArrayList<>();

    private boolean initialized;
    private boolean victoryHandled;
    private boolean needsTransientRecovery;
    private int state = COUNTDOWN;
    private int stateTick;
    private int phase = 1;
    private int h1;
    private int h2;
    private int h3;
    private int h4;
    private int h5;
    private int postAbilityLock;
    private int laserCasts;
    private int emptyPlayerTicks;
    private int musicTick = -1;
    private double anchorX;
    private double anchorY;
    private double anchorZ;
    private final List<ShieldZone> shields = new ArrayList<>();
    private Vec3 teleportMarker = Vec3.ZERO;
    private Vec3 teleportTarget = Vec3.ZERO;

    public ConquerorShadowBossEntity(EntityType<? extends Skeleton> type, Level level) {
        super(type, level);
        xpReward = 120;
        bossEvent.setVisible(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Skeleton.createAttributes()
                .add(Attributes.MAX_HEALTH, 330.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.21D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 60.0D);
    }

    public static boolean isStyxMemory(LivingEntity entity) {
        return entity.getPersistentData().hasUUID(MEMORY_OWNER);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                         @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        initializeEncounter();
        return data;
    }

    private void initializeEncounter() {
        if (initialized) return;
        initialized = true;
        anchorX = getX();
        anchorY = getY();
        anchorZ = getZ();
        state = COUNTDOWN;
        stateTick = 0;
        musicTick = 0;
        setPersistenceRequired();
        addTag("boss");
        setCanPickUpLoot(false);
        setNoAi(true);
        setInvulnerable(true);
        setInvisible(true);
        setCustomNameVisible(false);
        clearSourceEquipment();
        setHealth(getMaxHealth());
    }

    private void clearSourceEquipment() {
        for (EquipmentSlot slot : EquipmentSlot.values()) setItemSlot(slot, ItemStack.EMPTY);
    }

    private void summonSourceBoss(ServerLevel server) {
        equipSourceAppearance();
        setCustomName(Component.translatable("entity.finalparadox.conqueror_shadow"));
        setCustomNameVisible(true);
        setInvisible(false);
        setInvulnerable(false);
        setNoAi(false);
        setYRot(190.0F);
        setYHeadRot(190.0F);
        addEffect(new MobEffectInstance(MobEffects.WEAKNESS, -1, 0, false, false));
        sourceTeleportParticles(server, position());
        play(SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 1.5F);
        bossEvent.setVisible(true);
        beginPhase(1);
    }

    private void equipSourceAppearance() {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
        setItemSlot(EquipmentSlot.FEET, dyed(Items.LEATHER_BOOTS.getDefaultInstance(), 13697024));
        setItemSlot(EquipmentSlot.LEGS, dyed(Items.LEATHER_LEGGINGS.getDefaultInstance(), 2236962));
        setItemSlot(EquipmentSlot.CHEST, dyed(Items.LEATHER_CHESTPLATE.getDefaultInstance(), 2236962));
        setItemSlot(EquipmentSlot.HEAD, skull(HEAD_TEXTURE));
        for (EquipmentSlot slot : EquipmentSlot.values()) setDropChance(slot, 0.0F);
    }

    private static ItemStack dyed(ItemStack stack, int color) {
        if (stack.getItem() instanceof DyeableLeatherItem leather) leather.setColor(stack, color);
        return stack;
    }

    private static ItemStack skull(String texture) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        CompoundTag owner = new CompoundTag();
        owner.putUUID("Id", UUID.fromString("94bb193e-5e99-49a4-9bf7-ef50fafc9120"));
        CompoundTag properties = new CompoundTag();
        ListTag textures = new ListTag();
        CompoundTag value = new CompoundTag();
        value.putString("Value", texture);
        textures.add(value);
        properties.put("textures", textures);
        owner.put("Properties", properties);
        stack.getOrCreateTag().put("SkullOwner", owner);
        return stack;
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return false;
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return BuiltInLootTables.EMPTY;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || isRemoved() || isDeadOrDying()) return;
        if (!initialized) initializeEncounter();
        if (needsTransientRecovery) recoverTransientState(server);

        bossEvent.setName(Component.translatable("entity.finalparadox.conqueror_shadow.bossbar"));
        bossEvent.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0.0F, 1.0F));
        stateTick++;
        tickMusic(server);
        tickMemoryPortals(server);
        tickVerticalProjectiles(server);
        if (postAbilityLock > 0) postAbilityLock--;

        if (state == COUNTDOWN) tickCountdown(server);
        else if (state == FIGHT) tickFight(server);
        else if (state == TELEPORT) tickTeleport(server);
        else if (state == LASER) tickLaser(server);
        else if (state == JUDGMENT) tickJudgment(server);
        else if (state == DEFEAT) tickDefeat(server);

        tickShield(server);
        if (state != COUNTDOWN && state != COMPLETE && state != DEFEAT) tickAbandonment(server);
    }

    private void tickCountdown(ServerLevel server) {
        if (stateTick == 1) playMusic(server, ModSounds.CONQUEROR_SHADOW_INTRO.get());
        if (stateTick == 20) broadcastSourceDialogue("dialogue.finalparadox.conqueror_shadow.encouragement");
        if (stateTick == 60) sourceTitle(Component.translatable("title.finalparadox.conqueror_shadow.countdown_3"), Component.empty());
        else if (stateTick == 80) sourceTitle(Component.translatable("title.finalparadox.conqueror_shadow.countdown_2"), Component.empty());
        else if (stateTick == 100) sourceTitle(Component.translatable("title.finalparadox.conqueror_shadow.countdown_1"), Component.empty());
        if (stateTick == 60 || stateTick == 80 || stateTick == 100) {
            play(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0F, 1.5F);
        }
        if (stateTick >= 120) summonSourceBoss(server);
    }

    private void beginPhase(int newPhase) {
        phase = newPhase;
        h1 = 0;
        h2 = 0;
        h4 = 0;
        if (phase == 1) {
            h3 = 3;
            h5 = 35;
        } else if (phase == 2) {
            h3 = 17;
            h5 = 25;
        } else {
            h3 = 19;
            h5 = 25;
        }
        state = FIGHT;
        stateTick = 0;
        sourceTitle(Component.translatable("title.finalparadox.conqueror_shadow.phase_mark"),
                Component.translatable(phase == 1 ? "title.finalparadox.conqueror_shadow.phase_one"
                        : phase == 2 ? "title.finalparadox.conqueror_shadow.phase_two"
                        : "title.finalparadox.conqueror_shadow.final_phase"));
        play(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 1.5F);
    }

    private void tickFight(ServerLevel server) {
        if (stateTick % 20 != 0) return;
        if (postAbilityLock <= 0 && h1 > 0) h1--;
        h3++;
        h5++;
        int healthPercent = Mth.floor(getHealth() * 100.0F / getMaxHealth());

        if (postAbilityLock <= 0 && h4 == 0
                && ((phase == 1 && healthPercent <= 55) || (phase == 2 && healthPercent <= 15))) {
            beginJudgment(server);
            return;
        }
        if (postAbilityLock > 0) return;
        if (h1 <= 0) {
            beginTeleport(server);
            return;
        }
        if (h5 >= 40) {
            beginMemorySummon(server);
            h5 = 0;
        }
        int laserThreshold = phase == 2 ? 22 : 21;
        if (h3 >= laserThreshold) {
            beginLaser(server);
            return;
        }
        if ((phase == 1 && ((h2 == 0 && healthPercent <= 75) || (h2 == 1 && healthPercent <= 60)))
                || (phase == 2 && ((h2 == 0 && healthPercent <= 35) || (h2 == 1 && healthPercent <= 20)))
                || (phase == 3 && h2 == 0 && healthPercent <= 5)) {
            createShield(server);
            h2++;
        }
    }

    private void beginTeleport(ServerLevel server) {
        ServerPlayer chosenPlayer = randomParticipant(server);
        if (chosenPlayer == null) return;
        h1 = nextTeleportDelay();
        state = TELEPORT;
        stateTick = 0;
        teleportMarker = position();
        teleportTarget = selectSourceTeleportTarget(chosenPlayer);
        setInvulnerable(true);
        setNoAi(true);
        sourceTeleportParticles(server, position());
        setInvisible(true);
    }

    private Vec3 selectSourceTeleportTarget(ServerPlayer player) {
        double radius = switch (random.nextInt(3)) {
            case 0 -> 3.5D;
            case 1 -> 7.0D;
            default -> 12.0D;
        };
        int points = radius == 3.5D ? 16 : 32;
        List<Vec3> candidates = new ArrayList<>();
        Vec3 arenaCenter = arenaCenter();
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2.0D * i / points;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            Vec3 candidate = new Vec3(x, sourceGroundY(x, z), z);
            if (candidate.distanceToSqr(arenaCenter) < 900.0D) candidates.add(candidate);
        }
        if (candidates.isEmpty()) return arenaCenter;
        Vec3 first = candidates.stream().min((a, b) ->
                Double.compare(a.distanceToSqr(teleportMarker), b.distanceToSqr(teleportMarker))).orElse(candidates.get(0));
        List<Vec3> preferred = candidates.stream()
                .filter(point -> point.distanceTo(first) >= 3.0D && point.distanceTo(first) <= 5.0D).toList();
        if (!preferred.isEmpty()) return preferred.get(random.nextInt(preferred.size()));
        return candidates.stream().filter(point -> point.distanceTo(first) >= 3.0D)
                .min((a, b) -> Double.compare(a.distanceToSqr(first), b.distanceToSqr(first))).orElse(first);
    }

    private void tickTeleport(ServerLevel server) {
        Vec3 delta = teleportTarget.subtract(teleportMarker);
        if (delta.length() > 1.0D) teleportMarker = teleportMarker.add(delta.normalize().scale(0.7D));
        server.sendParticles(ParticleTypes.SQUID_INK, teleportMarker.x, teleportMarker.y + 1.0D, teleportMarker.z,
                0, 0.0D, 1.0D, 0.0D, 0.05D);
        server.sendParticles(TELEPORT_RED, teleportMarker.x, teleportMarker.y + 1.0D, teleportMarker.z,
                0, 0.0D, 1.0D, 0.0D, 0.05D);
        if (teleportMarker.distanceTo(teleportTarget) > 1.0D) return;

        sourceTeleportParticles(server, teleportMarker);
        moveTo(teleportMarker.x, teleportMarker.y, teleportMarker.z, getYRot(), 0.0F);
        ServerPlayer nearest = nearestParticipant(server, teleportMarker);
        if (nearest != null) lookAt(nearest, 180.0F, 180.0F);
        setInvisible(false);
        setInvulnerable(false);
        setNoAi(false);
        state = FIGHT;
        stateTick = 0;
        postAbilityLock = 20;
    }

    private int nextTeleportDelay() {
        int delay = 1;
        int[] additions = {1, 1, 2, 2, 2};
        for (int addition : additions) if (random.nextBoolean()) delay += addition;
        return delay;
    }

    private void sourceTeleportParticles(ServerLevel server, Vec3 center) {
        server.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y + 1.2D, center.z,
                15, 0.3D, 0.8D, 0.3D, 0.0D);
        for (int plane = 0; plane < 2; plane++) {
            for (int i = 0; i < 12; i++) {
                double angle = Math.PI * 2.0D * i / 12.0D;
                double a = Math.cos(angle) * 2.5D;
                double y = center.y + 1.4D + Math.sin(angle) * 2.5D;
                double x = center.x + (plane == 0 ? a : 0);
                double z = center.z + (plane == 1 ? a : 0);
                server.sendParticles(ParticleTypes.SQUID_INK, x, y, z, 0,
                        plane == 0 ? -Math.cos(angle) : 0, -Math.sin(angle),
                        plane == 1 ? -Math.cos(angle) : 0, 0.45D);
            }
        }
        playAt(center, SoundEvents.SHULKER_TELEPORT, 2.0F, 1.2F);
    }

    private void createShield(ServerLevel server) {
        ShieldZone shield = new ShieldZone(getX(), anchorY - 0.4D, getZ());
        shields.add(shield);
        ArmorStand name = newTransientArmorStand(server,
                new Vec3(shield.x, anchorY + 1.5D, shield.z), "shield_name");
        name.setCustomName(Component.translatable("entity.finalparadox.conqueror_shadow.immunity_name"));
        name.setCustomNameVisible(true);
        server.sendParticles(ParticleTypes.FLASH, getX(), getY() + 2.0D, getZ(), 1, 0, 0, 0, 0);
        server.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.0D, getZ(),
                40, 0, 0, 0, 0.3D);
        play(SoundEvents.ILLUSIONER_PREPARE_MIRROR, 3.0F, 1.4F);
    }

    private void tickShield(ServerLevel server) {
        for (ShieldZone shield : shields) {
            shield.rotation += 6.0D;
            double rotation = Math.toRadians(shield.rotation);
            for (int plane = 0; plane < 2; plane++) {
                double planeAngle = rotation + plane * Math.PI / 2.0D;
                Vec3 axis = new Vec3(Math.sin(planeAngle), 0.0D, Math.cos(planeAngle));
                for (int i = 1; i <= 11; i++) {
                    double arc = Math.toRadians(i * 7.5D);
                    double y = shield.y + 0.55D + Math.cos(arc) * 7.0D;
                    double horizontal = Math.sin(arc) * 7.0D;
                    for (int sign : new int[]{-1, 1}) {
                        Vec3 point = new Vec3(shield.x, y, shield.z).add(axis.scale(horizontal * sign));
                        server.sendParticles(WHITE, point.x, point.y, point.z, 1, 0, 0, 0, 0);
                    }
                }
                for (int sign : new int[]{-1, 1}) {
                    Vec3 endpoint = new Vec3(shield.x, shield.y + 0.4D, shield.z)
                            .add(axis.scale(7.0D * sign));
                    server.sendParticles(ParticleTypes.FIREWORK, endpoint.x, endpoint.y, endpoint.z,
                            1, 0, 0, 0, 0);
                }
            }
            Vec3 center = shield.center();
            AABB bounds = new AABB(center, center).inflate(7.0D);
            for (LivingEntity living : server.getEntitiesOfClass(LivingEntity.class, bounds,
                    entity -> entity.position().distanceToSqr(center) <= 49.0D)) {
                living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 77, false, false));
            }
        }
    }

    private void discardShield(ServerLevel server, boolean effects) {
        if (effects) {
            for (ShieldZone shield : shields) {
                server.sendParticles(ParticleTypes.LARGE_SMOKE, shield.x, shield.y + 3.5D, shield.z,
                        40, 3, 3, 3, 0);
                server.sendParticles(ParticleTypes.FIREWORK, shield.x, shield.y + 3.5D, shield.z,
                        20, 3, 3, 3, 0);
                server.sendParticles(ParticleTypes.FIREWORK, shield.x, shield.y + 1.5D, shield.z,
                        40, 0, 0, 0, 0.7D);
                playAt(shield.center(), SoundEvents.FIRE_EXTINGUISH, 3.0F, 1.3F);
                playAt(shield.center(), SoundEvents.GLASS_BREAK, 3.0F, 0.8F);
            }
        }
        discardOwnedKind(server, "shield_name");
        shields.clear();
    }

    private void beginLaser(ServerLevel server) {
        state = LASER;
        stateTick = 0;
        h3 = 0;
        laserCasts = phase;
        setNoAi(true);
        startLaserCast(server);
    }

    private void startLaserCast(ServerLevel server) {
        ServerPlayer target = randomParticipant(server);
        if (target == null) return;
        List<Vec3> ring = new ArrayList<>();
        Vec3 center = arenaCenter();
        for (int i = 0; i < 48; i++) {
            double angle = Math.PI * 2.0D * i / 48.0D;
            Vec3 point = new Vec3(center.x + Math.cos(angle) * 21.0D,
                    sourceGroundY(center.x + Math.cos(angle) * 21.0D, center.z + Math.sin(angle) * 21.0D),
                    center.z + Math.sin(angle) * 21.0D);
            boolean tooClose = participants(server).stream()
                    .anyMatch(player -> player.position().distanceTo(point) <= 20.0D);
            boolean intersectsLaser = beams.stream().anyMatch(beam -> beam.isWithin(point, 3.0D));
            if (!tooClose && !intersectsLaser) ring.add(point);
        }
        Vec3 point = ring.isEmpty() ? center : ring.get(random.nextInt(ring.size()));
        sourceTeleportParticles(server, position());
        moveTo(point.x, point.y, point.z, getYRot(), 0.0F);
        lookAt(target, 180.0F, 180.0F);
        sourceTeleportParticles(server, position());
    }

    private void tickLaser(ServerLevel server) {
        for (int cast = 0; cast < laserCasts; cast++) {
            int base = cast * 16;
            if (cast > 0 && stateTick == base) startLaserCast(server);
            if (stateTick == base + 3) spawnBeamSet(server, 2.0D, 0.0D);
            else if (stateTick == base + 6) spawnBeamSet(server, 1.0D, -4.0D, 4.0D);
            else if (stateTick == base + 9) spawnBeamSet(server, 0.0D, -8.0D, 8.0D);
            else if (stateTick == base + 12) spawnBeamSet(server, -1.0D, -12.0D, 12.0D);
        }
        Iterator<Beam> iterator = beams.iterator();
        while (iterator.hasNext()) if (iterator.next().tick(server)) iterator.remove();
        if (stateTick <= (laserCasts - 1) * 16 + 103 || !beams.isEmpty()) return;
        setNoAi(false);
        beginTeleport(server);
    }

    private void spawnBeamSet(ServerLevel server, double forwardOffset, double... sideOffsets) {
        Vec3 direction = horizontalLook();
        Vec3 side = new Vec3(-direction.z, 0, direction.x);
        for (double sideOffset : sideOffsets) {
            Vec3 origin = position().add(0, -0.5D, 0)
                    .add(direction.scale(forwardOffset)).add(side.scale(sideOffset));
            beams.add(new Beam(server, origin, direction));
        }
    }

    private Vec3 horizontalLook() {
        double yaw = Math.toRadians(getYRot());
        return new Vec3(-Math.sin(yaw), 0, Math.cos(yaw)).normalize();
    }

    private ArmorStand newTransientArmorStand(ServerLevel server, Vec3 position, String kind) {
        ArmorStand stand = EntityType.ARMOR_STAND.create(server);
        if (stand == null) throw new IllegalStateException("Could not create B6 armor stand");
        stand.moveTo(position.x, position.y, position.z, getYRot(), 0.0F);
        stand.setNoGravity(true);
        stand.setSilent(true);
        stand.setInvulnerable(true);
        stand.setInvisible(true);
        CompoundTag markerData = new CompoundTag();
        stand.saveWithoutId(markerData);
        markerData.putBoolean("Marker", true);
        markerData.putBoolean("Small", !"laser".equals(kind));
        stand.load(markerData);
        stand.noPhysics = true;
        stand.getPersistentData().putUUID(TRANSIENT_OWNER, getUUID());
        stand.getPersistentData().putString("finalparadox.b6_kind", kind);
        server.addFreshEntity(stand);
        return stand;
    }

    private void beginMemorySummon(ServerLevel server) {
        for (ServerPlayer player : participants(server)) {
            server.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.MASTER,
                    1.0F, 0.7F);
        }
        Vec3 direction = horizontalLook();
        Vec3 side = new Vec3(-direction.z, 0, direction.x);
        List<Vec3> offsets = new ArrayList<>(List.of(direction.scale(-4.0D),
                direction.scale(2.0D).add(side.scale(4.0D)),
                direction.scale(2.0D).add(side.scale(-4.0D))));
        if (participants(server).size() <= 1) offsets.remove(random.nextInt(offsets.size()));
        for (Vec3 offset : offsets) {
            memoryPortals.add(new MemoryPortal(position().add(offset).add(0, 3.7D, 0), getYRot()));
        }
    }

    private void tickMemoryPortals(ServerLevel server) {
        Iterator<MemoryPortal> iterator = memoryPortals.iterator();
        while (iterator.hasNext()) if (iterator.next().tick(server)) iterator.remove();
    }

    private void spawnStygianMemory(ServerLevel server, Vec3 position) {
        Skeleton memory = EntityType.SKELETON.create(server);
        if (memory == null) return;
        memory.moveTo(position.x, position.y, position.z, getYRot(), 0.0F);
        memory.setDeltaMovement(0.0D, 0.4D, 0.0D);
        memory.setCustomName(Component.translatable("entity.finalparadox.styx_memory"));
        memory.setCustomNameVisible(true);
        memory.setPersistenceRequired();
        memory.setCanPickUpLoot(false);
        memory.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, -1, 0, false, false));
        memory.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.SKELETON_SKULL));
        memory.setItemSlot(EquipmentSlot.CHEST, dyed(Items.LEATHER_CHESTPLATE.getDefaultInstance(), 2236962));
        for (EquipmentSlot slot : EquipmentSlot.values()) memory.setDropChance(slot, 0.0F);
        memory.getPersistentData().putUUID(MEMORY_OWNER, getUUID());
        memory.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0D);
        memory.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.2D);
        memory.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(60.0D);
        memory.setHealth(12.0F);
        ServerPlayer target = nearestParticipant(server, position);
        if (target != null) memory.setTarget(target);
        server.addFreshEntity(memory);
        for (int i = 0; i < 12; i++) {
            double angle = Math.PI * 2.0D * i / 12.0D;
            server.sendParticles(new DustParticleOptions(new Vector3f(1, 0, 0), 1),
                    position.x + Math.cos(angle) * 1.5D, position.y + 0.2D,
                    position.z + Math.sin(angle) * 1.5D, 1, 0, 0, 0, 0);
        }
        server.sendParticles(ParticleTypes.LARGE_SMOKE, position.x, position.y, position.z,
                10, 0, 0, 0, 0.1D);
        server.sendParticles(ParticleTypes.EXPLOSION, position.x, position.y, position.z,
                1, 0, 0, 0, 0.1D);
        playAt(position, SoundEvents.GRAVEL_BREAK, 2.0F, 0.01F);
        playAt(position, SoundEvents.ENDER_EYE_LAUNCH, 2.0F, 1.2F);
    }

    private void beginJudgment(ServerLevel server) {
        state = JUDGMENT;
        stateTick = 0;
        h4++;
        setNoAi(true);
        setInvulnerable(true);
        sourceTitle(Component.translatable("title.finalparadox.conqueror_shadow.ability_mark"),
                Component.translatable("title.finalparadox.conqueror_shadow.judgment"));
        play(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 1.5F);
        sourceTeleportParticles(server, position());
        setInvisible(true);
        judgmentWaves.clear();
        for (int i = 0; i < 10; i++) judgmentWaves.add(new JudgmentWave(80 + i * 20));
    }

    private void tickJudgment(ServerLevel server) {
        if (stateTick == 10) broadcastSourceDialogue("dialogue.finalparadox.conqueror_shadow.shield_hint");
        for (JudgmentWave wave : judgmentWaves) wave.tick(server, stateTick);
        if (stateTick < 340) return;
        judgmentWaves.clear();
        discardShield(server, true);
        setInvisible(false);
        setInvulnerable(false);
        setNoAi(false);
        Vec3 center = arenaCenter();
        moveTo(center.x, sourceGroundY(center.x, center.z), center.z, getYRot(), 0.0F);
        beginPhase(phase + 1);
        beginTeleport(server);
        // h4/end starts h1 and then the new phase function resets h1 to zero.
        h1 = 0;
    }

    private void tickVerticalProjectiles(ServerLevel server) {
        Iterator<VerticalProjectile> iterator = verticalProjectiles.iterator();
        while (iterator.hasNext()) if (iterator.next().tick(server)) iterator.remove();
    }

    private List<Vec3> chooseFireColumnPoints() {
        List<Vec3> candidates = new ArrayList<>();
        List<Vec3> centers = shields.isEmpty() ? List.of(arenaCenter())
                : shields.stream().map(ShieldZone::center).toList();
        for (Vec3 center : centers) {
            for (int radius : new int[]{3, 5}) {
                int count = radius == 3 ? 8 : 12;
                for (int i = 0; i < count; i++) {
                    double angle = Math.PI * 2.0D * i / count;
                    double x = center.x + Math.cos(angle) * radius;
                    double z = center.z + Math.sin(angle) * radius;
                    candidates.add(new Vec3(x, sourceGroundY(x, z), z));
                }
            }
        }
        Collections.shuffle(candidates, new java.util.Random(random.nextLong()));
        return new ArrayList<>(candidates.subList(0, Math.min(12, candidates.size())));
    }

    private List<Vec3> chooseBlackRainPoints(ServerLevel server) {
        Vec3 center = arenaCenter();
        List<Vec3> candidates = new ArrayList<>();
        for (int ring = 0; ring < BLACK_RAIN_RADII.length; ring++) {
            int radius = BLACK_RAIN_RADII[ring];
            int count = BLACK_RAIN_COUNTS[ring];
            for (int i = 0; i < count; i++) {
                double angle = Math.PI * 2.0D * i / count;
                Vec3 ground = new Vec3(center.x + Math.cos(angle) * radius,
                        sourceGroundY(center.x + Math.cos(angle) * radius, center.z + Math.sin(angle) * radius),
                        center.z + Math.sin(angle) * radius);
                if (shields.stream().anyMatch(shield ->
                        ground.distanceToSqr(new Vec3(shield.x, ground.y, shield.z)) <= 64.0D)) continue;
                if (participants(server).stream().noneMatch(player -> player.position().distanceTo(ground) <= 40.0D)) continue;
                candidates.add(ground.add(0, 40, 0));
            }
        }
        Collections.shuffle(candidates, new java.util.Random(random.nextLong()));
        return new ArrayList<>(candidates.subList(0, Math.min(12, candidates.size())));
    }

    private void judgmentDamage(ServerLevel server) {
        for (ServerPlayer player : participants(server)) {
            if (shields.stream().anyMatch(shield -> player.position().distanceTo(shield.center()) <= 7.5D)) {
                continue;
            }
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 2, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 0, false, false));
        }
    }

    private void blackProjectileExplosion(ServerLevel server, Vec3 position) {
        for (int i = 0; i < 24; i++) {
            double angle = Math.PI * 2.0D * i / 24.0D;
            server.sendParticles(ParticleTypes.END_ROD, position.x, position.y + 6.0D, position.z,
                    0, Math.cos(angle), 0, Math.sin(angle), 0.6D);
        }
        server.sendParticles(ParticleTypes.EXPLOSION, position.x, position.y + 6.0D, position.z,
                1, 0, 0, 0, 0);
        server.sendParticles(ParticleTypes.LAVA, position.x, position.y + 6.0D, position.z,
                2, 0, 0, 0, 0);
        playAt(position.add(0, 6, 0), SoundEvents.GENERIC_EXPLODE, 2.0F, 1.4F);
    }

    private Vec3 arenaCenter() {
        return new Vec3(anchorX, anchorY, anchorZ - 4.0D);
    }

    private double sourceGroundY(double x, double z) {
        BlockPos.MutableBlockPos cursor =
                new BlockPos.MutableBlockPos(Mth.floor(x), Mth.floor(anchorY + 5.0D), Mth.floor(z));
        for (int i = 0; i < 12; i++) {
            if (!level().getBlockState(cursor).getCollisionShape(level(), cursor).isEmpty()
                    && level().getBlockState(cursor.above()).getCollisionShape(level(), cursor.above()).isEmpty()) {
                return cursor.getY() + 1.0D;
            }
            cursor.move(0, -1, 0);
        }
        return anchorY;
    }

    private void tickAbandonment(ServerLevel server) {
        List<ServerPlayer> audience = audience(server);
        if (!audience.isEmpty() && participants(server).isEmpty()) {
            beginDefeat(server);
            return;
        }
        if (audience.isEmpty()) emptyPlayerTicks++;
        else emptyPlayerTicks = 0;
        if (emptyPlayerTicks < 600) return;
        resetEncounter(server);
    }

    private void beginDefeat(ServerLevel server) {
        state = DEFEAT;
        stateTick = 0;
        setNoAi(true);
        setInvulnerable(true);
        sourceTitle(Component.translatable("title.finalparadox.conqueror_shadow.defeat_mark"),
                Component.translatable("title.finalparadox.conqueror_shadow.defeat"));
        for (ServerPlayer player : audience(server)) {
            server.playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH,
                    SoundSource.MASTER, 1.0F, 1.8F);
        }
        stopMusic();
    }

    private void tickDefeat(ServerLevel server) {
        if (stateTick == 50) broadcastSourceDialogue("dialogue.finalparadox.conqueror_shadow.defeat_hint");
        if (stateTick >= 100) resetEncounter(server);
    }

    private void resetEncounter(ServerLevel server) {
        cleanupTransient(server);
        stopMusic();
        moveTo(anchorX, sourceGroundY(anchorX, anchorZ), anchorZ, 90.0F, 0.0F);
        setHealth(getMaxHealth());
        state = COUNTDOWN;
        stateTick = 0;
        phase = 1;
        emptyPlayerTicks = 0;
        musicTick = 0;
        setInvisible(true);
        setCustomNameVisible(false);
        setNoAi(true);
        setInvulnerable(true);
        bossEvent.setVisible(false);
        clearSourceEquipment();
    }

    private List<ServerPlayer> participants(ServerLevel server) {
        AABB area = new AABB(anchorX - ENCOUNTER_RADIUS, anchorY - 24, anchorZ - ENCOUNTER_RADIUS,
                anchorX + ENCOUNTER_RADIUS, anchorY + 32, anchorZ + ENCOUNTER_RADIUS);
        return server.getEntitiesOfClass(ServerPlayer.class, area,
                player -> player.isAlive() && !player.isSpectator());
    }

    private List<ServerPlayer> audience(ServerLevel server) {
        AABB area = new AABB(anchorX - ENCOUNTER_RADIUS, anchorY - 24, anchorZ - ENCOUNTER_RADIUS,
                anchorX + ENCOUNTER_RADIUS, anchorY + 32, anchorZ + ENCOUNTER_RADIUS);
        return server.getEntitiesOfClass(ServerPlayer.class, area);
    }

    @Nullable
    private ServerPlayer randomParticipant(ServerLevel server) {
        List<ServerPlayer> players = participants(server);
        return players.isEmpty() ? null : players.get(random.nextInt(players.size()));
    }

    @Nullable
    private ServerPlayer nearestParticipant(ServerLevel server, Vec3 point) {
        return participants(server).stream()
                .min((a, b) -> Double.compare(a.position().distanceToSqr(point), b.position().distanceToSqr(point)))
                .orElse(null);
    }

    private void sourceTitle(Component title, Component subtitle) {
        if (!(level() instanceof ServerLevel server)) return;
        for (ServerPlayer player : audience(server)) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 20));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
        }
    }

    private void broadcastSourceDialogue(String key) {
        if (!(level() instanceof ServerLevel server)) return;
        Component line = Component.empty()
                .append(Component.translatable("dialogue.finalparadox.conqueror_shadow.speaker")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD, ChatFormatting.ITALIC))
                .append(Component.translatable(key));
        for (ServerPlayer player : audience(server)) {
            player.sendSystemMessage(line);
            server.playSound(null, player.blockPosition(), SoundEvents.ELDER_GUARDIAN_AMBIENT,
                    SoundSource.MASTER, 1.0F, 1.8F);
        }
    }

    private void play(SoundEvent sound, float volume, float pitch) {
        if (level() instanceof ServerLevel server) {
            server.playSound(null, blockPosition(), sound, SoundSource.MASTER, volume, pitch);
        }
    }

    private void playAt(Vec3 position, SoundEvent sound, float volume, float pitch) {
        if (level() instanceof ServerLevel server) {
            server.playSound(null, BlockPos.containing(position), sound, SoundSource.MASTER, volume, pitch);
        }
    }

    private void tickMusic(ServerLevel server) {
        if (musicTick < 0) return;
        musicTick++;
        if (musicTick == 245 || (musicTick > 245 && (musicTick - 245) % 984 == 0)) {
            playMusic(server, ModSounds.CONQUEROR_SHADOW_LOOP.get());
        }
    }

    private void playMusic(ServerLevel server, SoundEvent sound) {
        for (ServerPlayer player : audience(server)) {
            server.playSound(null, player.blockPosition(), sound, SoundSource.RECORDS, 999999.0F, 1.0F);
        }
    }

    private void stopMusic() {
        if (level().getServer() == null) return;
        ResourceLocation intro = ModSounds.CONQUEROR_SHADOW_INTRO.get().getLocation();
        ResourceLocation loop = ModSounds.CONQUEROR_SHADOW_LOOP.get().getLocation();
        for (ServerPlayer player : level().getServer().getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundStopSoundPacket(intro, SoundSource.RECORDS));
            player.connection.send(new ClientboundStopSoundPacket(loop, SoundSource.RECORDS));
        }
        musicTick = -1;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void die(DamageSource source) {
        if (level() instanceof ServerLevel server && !victoryHandled) {
            victoryHandled = true;
            state = COMPLETE;
            sourceTitle(Component.translatable("title.finalparadox.conqueror_shadow.congratulations"),
                    Component.translatable("title.finalparadox.conqueror_shadow.victory"));
            for (ServerPlayer player : audience(server)) {
                server.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.MASTER,
                        1.0F, 0.8F);
                server.playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.MASTER,
                        0.7F, 1.4F);
            }
            cleanupTransient(server);
            stopMusic();
        }
        super.die(source);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide && level() instanceof ServerLevel server) cleanupTransient(server);
        super.remove(reason);
    }

    private void cleanupTransient(ServerLevel server) {
        cleanupAbilityTransients(server);
        discardShield(server, false);
        AABB area = new AABB(anchorX - 128, anchorY - 96, anchorZ - 128,
                anchorX + 128, anchorY + 128, anchorZ + 128);
        for (Entity entity : server.getEntities(this, area, candidate ->
                candidate.getPersistentData().hasUUID(MEMORY_OWNER)
                        && getUUID().equals(candidate.getPersistentData().getUUID(MEMORY_OWNER)))) {
            entity.discard();
        }
    }

    private void cleanupAbilityTransients(ServerLevel server) {
        beams.forEach(Beam::discard);
        beams.clear();
        memoryPortals.clear();
        judgmentWaves.clear();
        verticalProjectiles.forEach(VerticalProjectile::discard);
        verticalProjectiles.clear();
        AABB area = new AABB(anchorX - 128, anchorY - 96, anchorZ - 128,
                anchorX + 128, anchorY + 128, anchorZ + 128);
        for (Entity entity : server.getEntities(this, area, candidate ->
                candidate.getPersistentData().hasUUID(TRANSIENT_OWNER)
                        && getUUID().equals(candidate.getPersistentData().getUUID(TRANSIENT_OWNER))
                        && !"shield_name".equals(candidate.getPersistentData().getString("finalparadox.b6_kind")))) {
            if (entity instanceof ArmorStand stand) {
                PlayerTeam team = server.getScoreboard().getPlayerTeam("fp_b6_red");
                if (team != null) server.getScoreboard().removePlayerFromTeam(stand.getScoreboardName(), team);
            }
            entity.discard();
        }
    }

    private void discardOwnedKind(ServerLevel server, String kind) {
        AABB area = getBoundingBox().inflate(128);
        for (Entity entity : server.getEntities(this, area, candidate ->
                candidate.getPersistentData().hasUUID(TRANSIENT_OWNER)
                        && getUUID().equals(candidate.getPersistentData().getUUID(TRANSIENT_OWNER))
                        && kind.equals(candidate.getPersistentData().getString("finalparadox.b6_kind")))) {
            entity.discard();
        }
    }

    private void recoverTransientState(ServerLevel server) {
        needsTransientRecovery = false;
        int interruptedState = state;
        cleanupAbilityTransients(server);
        if (interruptedState == JUDGMENT) h4 = 0;
        setInvisible(false);
        setNoAi(false);
        setInvulnerable(false);
        state = FIGHT;
        stateTick = 0;
        postAbilityLock = 20;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("B6Initialized", initialized);
        tag.putBoolean("B6Victory", victoryHandled);
        tag.putInt("B6State", state);
        tag.putInt("B6StateTick", stateTick);
        tag.putInt("B6Phase", phase);
        tag.putInt("B6H1", h1);
        tag.putInt("B6H2", h2);
        tag.putInt("B6H3", h3);
        tag.putInt("B6H4", h4);
        tag.putInt("B6H5", h5);
        tag.putInt("B6MusicTick", musicTick);
        tag.putDouble("B6AnchorX", anchorX);
        tag.putDouble("B6AnchorY", anchorY);
        tag.putDouble("B6AnchorZ", anchorZ);
        ListTag shieldTags = new ListTag();
        for (ShieldZone shield : shields) {
            CompoundTag shieldTag = new CompoundTag();
            shieldTag.putDouble("X", shield.x);
            shieldTag.putDouble("Y", shield.y);
            shieldTag.putDouble("Z", shield.z);
            shieldTag.putDouble("Rotation", shield.rotation);
            shieldTags.add(shieldTag);
        }
        tag.put("B6Shields", shieldTags);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        initialized = tag.getBoolean("B6Initialized");
        victoryHandled = tag.getBoolean("B6Victory");
        state = tag.getInt("B6State");
        stateTick = tag.getInt("B6StateTick");
        phase = Math.max(1, tag.getInt("B6Phase"));
        h1 = tag.getInt("B6H1");
        h2 = tag.getInt("B6H2");
        h3 = tag.getInt("B6H3");
        h4 = tag.getInt("B6H4");
        h5 = tag.getInt("B6H5");
        musicTick = tag.getInt("B6MusicTick");
        anchorX = tag.getDouble("B6AnchorX");
        anchorY = tag.getDouble("B6AnchorY");
        anchorZ = tag.getDouble("B6AnchorZ");
        shields.clear();
        ListTag shieldTags = tag.getList("B6Shields", Tag.TAG_COMPOUND);
        for (int i = 0; i < shieldTags.size(); i++) {
            CompoundTag shieldTag = shieldTags.getCompound(i);
            ShieldZone shield = new ShieldZone(shieldTag.getDouble("X"),
                    shieldTag.getDouble("Y"), shieldTag.getDouble("Z"));
            shield.rotation = shieldTag.getDouble("Rotation");
            shields.add(shield);
        }
        needsTransientRecovery = state == TELEPORT || state == LASER || state == JUDGMENT;
        bossEvent.setVisible(state != COUNTDOWN && state != COMPLETE);
    }

    private final class Beam {
        private Vec3 origin;
        private final Vec3 direction;
        private final List<ArmorStand> parts = new ArrayList<>();
        private int score = -40;

        private Beam(ServerLevel server, Vec3 origin, Vec3 direction) {
            this.origin = origin;
            this.direction = direction;
            for (int i = 0; i < 4; i++) {
                ArmorStand part = newTransientArmorStand(server, origin.add(direction.scale(i * 0.35D)), "laser");
                part.setGlowingTag(true);
                part.setRightArmPose(new Rotations(345.0F, 225.0F, 0.0F));
                part.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BLACK_CONCRETE));
                parts.add(part);
            }
            playAt(origin, SoundEvents.ENDER_EYE_DEATH, 4.0F, 1.4F);
            playAt(origin, SoundEvents.TRIDENT_RETURN, 4.0F, 1.7F);
            Vec3 warningOrigin = origin.add(0, 0.9D, 0);
            for (double distance = 0.5D; distance <= 45.0D; distance += 0.5D) {
                Vec3 point = warningOrigin.add(direction.scale(distance));
                server.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z,
                        1, 0, 0, 0, 0);
            }
        }

        private boolean tick(ServerLevel server) {
            score++;
            if (score == 0) turnRed(server);
            if (score >= 18 && score <= 24) move(-0.4D);
            if (score == 29) playAt(origin, SoundEvents.PUFFER_FISH_BLOW_OUT, 1.0F, 0.9F);
            if (score >= 30 && score <= 50) {
                move(2.0D);
                hitPlayers(server);
                move(2.0D);
                hitPlayers(server);
            }
            if (score < 50) return false;
            discard();
            return true;
        }

        private void move(double amount) {
            origin = origin.add(direction.scale(amount));
            for (int i = 0; i < parts.size(); i++) {
                ArmorStand part = parts.get(i);
                Vec3 point = origin.add(direction.scale(i * 0.35D));
                part.moveTo(point.x, point.y, point.z, getYRot(), 0.0F);
            }
        }

        private void hitPlayers(ServerLevel server) {
            Vec3 safe = arenaCenter();
            for (ArmorStand part : parts) {
                for (double yOffset : new double[]{0.0D, 0.5D}) {
                    AABB hit = new AABB(part.position().add(0, yOffset, 0),
                            part.position().add(0, yOffset, 0)).inflate(0.4D);
                    for (ServerPlayer player : server.getEntitiesOfClass(ServerPlayer.class, hit,
                            p -> p.isAlive() && !p.isSpectator() && p.position().distanceTo(safe) > 4.7D)) {
                        player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 0));
                    }
                }
            }
        }

        private void turnRed(ServerLevel server) {
            PlayerTeam team = server.getScoreboard().getPlayerTeam("fp_b6_red");
            if (team == null) team = server.getScoreboard().addPlayerTeam("fp_b6_red");
            team.setColor(ChatFormatting.RED);
            for (ArmorStand part : parts) server.getScoreboard().addPlayerToTeam(part.getScoreboardName(), team);
        }

        private boolean isWithin(Vec3 point, double distance) {
            return parts.stream().anyMatch(part -> part.position().distanceTo(point) <= distance);
        }

        private void discard() {
            if (level() instanceof ServerLevel server) {
                PlayerTeam team = server.getScoreboard().getPlayerTeam("fp_b6_red");
                if (team != null) {
                    for (ArmorStand part : parts) {
                        server.getScoreboard().removePlayerFromTeam(part.getScoreboardName(), team);
                    }
                }
            }
            parts.forEach(Entity::discard);
            parts.clear();
        }
    }

    private final class MemoryPortal {
        private Vec3 position;
        private float yaw;
        private int age;

        private MemoryPortal(Vec3 position, float yaw) {
            this.position = position;
            this.yaw = yaw;
        }

        private boolean tick(ServerLevel server) {
            age++;
            double radians = Math.toRadians(yaw);
            Vec3 direction = new Vec3(-Math.sin(radians), 0, Math.cos(radians));
            for (int sign : new int[]{-1, 1}) {
                Vec3 point = position.add(direction.scale(sign));
                server.sendParticles(ParticleTypes.LARGE_SMOKE, point.x, point.y, point.z,
                        1, 0, 0, 0, 0);
                server.sendParticles(new DustParticleOptions(new Vector3f(1, 0, 0), 1),
                        point.x, point.y, point.z, 1, 0, 0, 0, 0);
            }
            position = position.add(0, -0.2D, 0);
            yaw += 16.0F;
            if (age < 20) return false;
            spawnStygianMemory(server, position);
            return true;
        }
    }

    private final class JudgmentWave {
        private final int startTick;
        private List<Vec3> firePoints = List.of();
        private List<Vec3> blackPoints = List.of();
        private int fireIndex;
        private int blackIndex;

        private JudgmentWave(int startTick) {
            this.startTick = startTick;
        }

        private void tick(ServerLevel server, int judgmentTick) {
            int age = judgmentTick - startTick;
            if (age < 0) return;
            if (age == 0) firePoints = chooseFireColumnPoints();
            if (age == 0 || age == 3 || age == 6) {
                if (shields.isEmpty()) {
                    playAt(arenaCenter(), SoundEvents.BLAZE_SHOOT, 4.0F, 0.8F);
                } else {
                    for (ShieldZone shield : shields) {
                        playAt(shield.center(), SoundEvents.BLAZE_SHOOT, 4.0F, 0.8F);
                    }
                }
            }
            if (age >= 1 && age <= 6) {
                for (int i = 0; i < 2 && fireIndex < firePoints.size(); i++, fireIndex++) {
                    verticalProjectiles.add(new VerticalProjectile(server, firePoints.get(fireIndex), true));
                }
            }
            if (age == 27) blackPoints = chooseBlackRainPoints(server);
            if (age >= 28 && age <= 33) {
                for (int i = 0; i < 2 && blackIndex < blackPoints.size(); i++, blackIndex++) {
                    verticalProjectiles.add(new VerticalProjectile(server, blackPoints.get(blackIndex), false));
                }
            }
            if (age == 53) judgmentDamage(server);
        }
    }

    private final class VerticalProjectile {
        private final Slime entity;
        private final boolean upward;
        private int age;

        private VerticalProjectile(ServerLevel server, Vec3 position, boolean upward) {
            this.upward = upward;
            Slime slime = EntityType.SLIME.create(server);
            if (slime == null) throw new IllegalStateException("Could not create B6 judgment projectile");
            entity = slime;
            slime.setSize(2, true);
            slime.moveTo(position.x, position.y, position.z, 0, 0);
            slime.setNoAi(true);
            slime.setNoGravity(true);
            slime.setSilent(true);
            slime.setInvulnerable(true);
            slime.setInvisible(true);
            if (upward) slime.setRemainingFireTicks(999999);
            slime.getPersistentData().putUUID(TRANSIENT_OWNER, getUUID());
            slime.getPersistentData().putString("finalparadox.b6_kind", upward ? "fire_column" : "black_rain");
            server.addFreshEntity(slime);
        }

        private boolean tick(ServerLevel server) {
            age++;
            entity.moveTo(entity.getX(), entity.getY() + (upward ? 3.0D : -2.0D), entity.getZ(), 0, 0);
            if (!upward) {
                server.sendParticles(ParticleTypes.SQUID_INK, entity.getX(), entity.getY() + 4.0D, entity.getZ(),
                        1, 0.1D, 0.1D, 0.1D, 0);
                server.sendParticles(ParticleTypes.SQUID_INK, entity.getX(), entity.getY() + 5.0D, entity.getZ(),
                        1, 0.1D, 0.1D, 0.1D, 0);
                server.sendParticles(RED, entity.getX(), entity.getY() + 6.0D, entity.getZ(),
                        1, 0.1D, 0.1D, 0.1D, 0);
                server.sendParticles(RED, entity.getX(), entity.getY() + 7.0D, entity.getZ(),
                        1, 0.1D, 0.1D, 0.1D, 0);
            }
            if (age < 23) return false;
            if (!upward) blackProjectileExplosion(server, entity.position());
            discard();
            return true;
        }

        private void discard() {
            entity.discard();
        }
    }

    private static final class ShieldZone {
        private final double x;
        private final double y;
        private final double z;
        private double rotation;

        private ShieldZone(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private Vec3 center() {
            return new Vec3(x, y, z);
        }
    }
}
