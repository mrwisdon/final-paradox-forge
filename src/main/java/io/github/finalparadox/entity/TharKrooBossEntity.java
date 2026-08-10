package io.github.finalparadox.entity;

import io.github.finalparadox.arena.ArenaDeploymentData;
import io.github.finalparadox.arena.ArenaDefinitions;
import io.github.finalparadox.arena.ArenaEntranceMemory;
import io.github.finalparadox.arena.ArenaFightParticipants;
import io.github.finalparadox.arena.B2ArenaStaging;
import io.github.finalparadox.registry.ModEntities;
import io.github.finalparadox.registry.ModItems;
import io.github.finalparadox.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Server-authoritative reconstruction of Thar Kroo's three trials and sacrifice finale. */
public final class TharKrooBossEntity extends MagmaCube {
    private static final EntityDataAccessor<Integer> DISPLAY_MODE = SynchedEntityData.defineId(
            TharKrooBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> VISUAL_YAW = SynchedEntityData.defineId(
            TharKrooBossEntity.class, EntityDataSerializers.FLOAT);

    private static final int WAITING = 0;
    private static final int INTRO = 1;
    private static final int COUNTDOWN = 2;
    private static final int PHASE_ONE = 3;
    private static final int INTERMISSION_ONE = 4;
    private static final int PHASE_TWO = 5;
    private static final int INTERMISSION_TWO = 6;
    private static final int PHASE_THREE = 7;
    private static final int FINALE = 8;
    private static final int COMPLETE = 9;
    private static final int PRE_BATTLE = 10;
    private static final int INTRO_LINES = 22;
    private static final int INTRO_LINE_GAP = 60;
    private static final int INTRO_END = INTRO_LINES * INTRO_LINE_GAP + 40;
    private static final int DEFEAT_DIALOGUE_1_TICK = 80;
    private static final int DEFEAT_DIALOGUE_2_TICK = 150;
    private static final int DEFEAT_RESPAWN_TICKS = 200;
    private static final String OWNER_KEY = "finalparadox.thar_kroo_owner";
    private static final String MINION_KEY = "finalparadox.thar_kroo_minion";
    private static final String CUSTODIAN_KEY = "finalparadox.thar_kroo_custodian";
    private static final DustParticleOptions RED_DUST = new DustParticleOptions(new Vector3f(0.74F, 0.15F, 0.15F), 2.0F);
    private static final DustParticleOptions PURPLE_DUST = new DustParticleOptions(new Vector3f(0.45F, 0.15F, 0.49F), 2.5F);
    private static final double[][] FIRE_FIELD_OFFSETS = {
            {12.7279, 12.7279}, {18, 0}, {12.7279, -12.7279}, {0, -18},
            {-12.7279, -12.7279}, {-18, 0}, {-12.7279, 12.7279}, {0, 18},
            {26.5317, 12.2705}, {28.5317, -9.2705}, {12.6336, -26.2705}, {0, -30},
            {-12.6336, -26.2705}, {-28.5317, -9.2705}, {-26.5317, 12.2705},
            {25, -23}, {-24, -23}, {12, 29}, {-12, 29}, {26, 29}, {-26, 29}
    };
    private static final double[] TORNADO_RADII = {
            13, 15, 17, 19, 21, 23, 14, 16, 18, 20, 22, 24
    };
    private static final double[] TORNADO_YAW_STEP = {
            1.05D, 1.0D, 0.95D, 0.9D, 0.85D, 0.8D,
            -1.05D, -1.0D, -0.95D, -0.9D, -0.885D, -0.8D
    };

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.finalparadox.thar_kroo.bossbar"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
    private final List<ScheduledLine> dialogue = new ArrayList<>();
    private final Set<UUID> entranceViewers = new HashSet<>();
    private final List<ShadowOrb> shadowOrbs = new ArrayList<>();
    private final List<FlameChargeState> flameCharges = new ArrayList<>();
    private final List<Vec3> fireFieldDangerPoints = new ArrayList<>();
    private final Map<BlockPos, BlockState> alteredBlocks = new HashMap<>();
    private final Set<UUID> laserHits = new HashSet<>();
    private final Map<UUID, Integer> laserDamageLevels = new HashMap<>();
    private final Set<UUID> flameHits = new HashSet<>();
    private final Set<UUID> encounterPlayers = new HashSet<>();
    private final Map<UUID, Integer> barrageHitCooldowns = new HashMap<>();
    private final Set<Integer> activeTornadoTracks = new HashSet<>();
    private final Map<Integer, Integer> tornadoAges = new HashMap<>();

    private int phase = WAITING;
    private int phaseTick;
    private int totalTick;
    private int abilityTick;
    private int summonTick;
    private int combatSecondTick;
    private int abilityLockTick;
    private int postIntermissionH6Tick;
    private int h1;
    private int h3;
    private int h5;
    private int h6;
    private int h80;
    private int h81;
    private int emptyPlayerTicks;
    private int laserTick = -1;
    private int pushTick = -1;
    private int flameTick = -1;
    private int fireFieldTick = -1;
    private int fireFieldDuration;
    private int fireFieldWarningTicks;
    private int fireFieldStageStart;
    private int fireFieldActivatedPoints;
    private int fireFieldExplosionWave;
    private int barrageTick = -1;
    private int blackfireTick = -1;
    private int delayedHomingTick = -1;
    private int delayedRapidFlameTick = -1;
    private int redVolleyCasts;
    private int visualAnimationTicks = -1;
    private int visualAnimationSequence;
    private int musicTick = -1;
    private boolean shielded;
    private boolean initialized;
    private boolean completionHandled;
    private boolean needsMusicRecovery;
    private boolean preBattleDialoguePlayed;
    private boolean defeatActive;
    private int defeatTicks;
    private UUID flameTarget;
    private UUID laserTarget;
    private UUID blackfireTarget;
    private Vec3 flameOrigin = Vec3.ZERO;
    private Vec3 flameDirection = Vec3.ZERO;
    private Vec3 laserDirection = Vec3.ZERO;
    private double laserRotationStep;
    private Vec3 blackfireDirection = Vec3.ZERO;
    private boolean blackfireRed;
    private double safeX;
    private double safeZ;
    private double anchorX;
    private double anchorY;
    private double anchorZ;

    public TharKrooBossEntity(EntityType<? extends MagmaCube> type, Level level) {
        super(type, level);
        xpReward = 150;
        bossEvent.setVisible(false);
        setNoGravity(true);
        setSilent(true);
    }

    @Nullable
    public static TharKrooBossEntity createPrepared(ServerLevel level, BlockPos position) {
        TharKrooBossEntity boss = ModEntities.THAR_KROO.get().create(level);
        if (boss != null) {
            boss.moveTo(position.getX(), position.getY(), position.getZ(), 180.0F, 0.0F);
            boss.initializeEncounter();
        }
        return boss;
    }

    public boolean isWaiting() {
        return phase == WAITING;
    }

    /** True when this waiting boss is still at its persisted encounter anchor. */
    public boolean isWaitingAt(BlockPos expected) {
        return isWaiting() && matchesAnchor(expected);
    }

    /** True when the persisted encounter anchor equals {@code expected}. */
    public boolean matchesAnchor(BlockPos expected) {
        return Mth.floor(anchorX) == expected.getX()
                && Mth.floor(anchorY) == expected.getY()
                && Mth.floor(anchorZ) == expected.getZ();
    }

    public boolean startPreBattleDialogue() {
        if (phase != WAITING || preBattleDialoguePlayed) return false;
        preBattleDialoguePlayed = true;
        if (!(level() instanceof ServerLevel server)) return true;
        entranceViewers.clear();
        for (ServerPlayer player : server.getServer().getPlayerList().getPlayers()) {
            if (player.level() == server && !player.isSpectator()
                    && ArenaEntranceMemory.markIfFirst(player, "b2")) {
                entranceViewers.add(player.getUUID());
            }
        }
        if (entranceViewers.isEmpty()) return true;
        phase = PRE_BATTLE;
        phaseTick = -1;
        totalTick = 0;
        queueIntroDialogue();
        playGlobal(SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 1.25F);
        return true;
    }

    public boolean preBattleDialoguePlayed() {
        return preBattleDialoguePlayed;
    }

    public void markPreBattleDialoguePlayed() {
        preBattleDialoguePlayed = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return MagmaCube.createAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DISPLAY_MODE, 0);
        entityData.define(VISUAL_YAW, 180.0F);
    }

    public int displayMode() {
        return entityData.get(DISPLAY_MODE);
    }

    public float visualYaw() {
        return entityData.get(VISUAL_YAW);
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
        phase = WAITING;
        anchorX = getX();
        anchorY = getY();
        anchorZ = getZ();
        defeatActive = false;
        defeatTicks = 0;
        setPersistenceRequired();
        setNoAi(true);
        setNoGravity(true);
        setSilent(true);
        setInvulnerable(true);
        setCustomName(Component.translatable("entity.finalparadox.thar_kroo"));
        setCustomNameVisible(false);
        setHealth(getMaxHealth());
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 0, false, false));
        addTag("boss");
        moveTo(anchorX, anchorY, anchorZ, 180.0F, 0.0F);
        entityData.set(VISUAL_YAW, 180.0F);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        // B2 uses the proximity-triggered pre-battle dialogue and the Echo of
        // Koros to start the encounter, so the boss no longer opens on use.
        return InteractionResult.CONSUME;
    }

    public boolean beginEncounter() {
        if (phase != WAITING) return false;
        cleanupTransient();
        encounterPlayers.clear();
        laserDamageLevels.clear();
        totalTick = 0;
        for (ServerPlayer player : combatPlayers()) encounterPlayers.add(player.getUUID());
        if (preBattleDialoguePlayed) {
            phase = COUNTDOWN;
            phaseTick = -1;
        } else {
            phase = INTRO;
            phaseTick = -1;
            queueIntroDialogue();
        }
        playGlobal(SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 1.25F);
        return true;
    }

    private void queueIntroDialogue() {
        dialogue.clear();
        for (int line = 1; line <= INTRO_LINES; line++) {
            dialogue.add(new ScheduledLine((line - 1) * INTRO_LINE_GAP,
                    "dialogue.finalparadox.thar_kroo.intro." + line));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || isRemoved() || isDeadOrDying()) return;
        if (!initialized) initializeEncounter();
        if (defeatActive) {
            tickDefeat(server);
            return;
        }
        if (ArenaFightParticipants.allDefeated(server, ArenaDefinitions.B2)) {
            startDefeat(server);
            return;
        }
        setDeltaMovement(Vec3.ZERO);
        bossEvent.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0.0F, 1.0F));
        if (phase == WAITING || phase == COMPLETE) {
            bossEvent.setVisible(false);
            return;
        }

        List<ServerPlayer> nearbyPlayers = combatPlayers();
        for (ServerPlayer player : nearbyPlayers) encounterPlayers.add(player.getUUID());
        if (nearbyPlayers.isEmpty()) {
            if (++emptyPlayerTicks >= 200) {
                showTitle(Component.translatable("title.finalparadox.thar_kroo.defeat"),
                        Component.translatable("subtitle.finalparadox.thar_kroo.defeat"));
                cleanupEncounter(server);
                discard();
            }
            return;
        }
        emptyPlayerTicks = 0;

        totalTick++;
        phaseTick++;
        if (needsMusicRecovery) {
            needsMusicRecovery = false;
            playTharMusic(server);
        } else {
            tickTharMusic(server);
        }
        processDialogue();
        tickTransientAttacks(server);

        if (phase == INTRO) tickIntro();
        else if (phase == PRE_BATTLE) tickPreBattle();
        else if (phase == COUNTDOWN) tickCountdown();
        else if (phase == INTERMISSION_ONE || phase == INTERMISSION_TWO) tickIntermission(server);
        else if (phase == FINALE) tickFinale(server);
        else tickCombat(server);
    }

    private void tickPreBattle() {
        if (phaseTick >= INTRO_END) {
            phase = WAITING;
            phaseTick = 0;
            entranceViewers.clear();
        }
    }

    public static void onPlayerDeath(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel server)) return;
        ArenaDeploymentData data = ArenaDeploymentData.get(server, ArenaDefinitions.B2);
        if (data.state() != ArenaDeploymentData.DeploymentState.READY
                || !ArenaDefinitions.B2.id().equals(data.arenaId())) {
            return;
        }
        data.activeBossUuid().map(server::getEntity)
                .filter(TharKrooBossEntity.class::isInstance)
                .map(TharKrooBossEntity.class::cast)
                .filter(Entity::isAlive)
                .ifPresent(boss -> boss.handlePlayerDeath(player));
    }

    public void handlePlayerDeath(ServerPlayer player) {
        if (!(level() instanceof ServerLevel server) || defeatActive) return;
        if (phase < COUNTDOWN || phase == PRE_BATTLE || phase == COMPLETE) return;
        if (!ArenaFightParticipants.markDefeated(
                server, ArenaDefinitions.B2, player.getUUID())) return;
        if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
            player.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
        }
        Vec3 above = position().add(0.0D, 10.0D, 0.0D);
        player.teleportTo(server, above.x, above.y, above.z, player.getYRot(), player.getXRot());
        if (ArenaFightParticipants.allDefeated(server, ArenaDefinitions.B2)) {
            startDefeat(server);
        }
    }

    private void startDefeat(ServerLevel server) {
        defeatActive = true;
        defeatTicks = 0;
        for (ServerPlayer player : ArenaFightParticipants.onlinePlayers(server, ArenaDefinitions.B2)) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 10));
            player.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.translatable("subtitle.finalparadox.thar_kroo.defeat")));
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("title.finalparadox.thar_kroo.defeat")));
            server.playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH,
                    SoundSource.MASTER, 1.0F, 1.8F);
        }
    }

    private void tickDefeat(ServerLevel server) {
        defeatTicks++;
        if (defeatTicks == DEFEAT_DIALOGUE_1_TICK) {
            for (ServerPlayer player : ArenaFightParticipants.onlinePlayers(server, ArenaDefinitions.B2)) {
                player.sendSystemMessage(
                        Component.translatable("dialogue.finalparadox.thar_kroo.defeat.1"));
            }
            server.playSound(null, blockPosition(), SoundEvents.ELDER_GUARDIAN_AMBIENT,
                    SoundSource.MASTER, 1.0F, 1.8F);
        } else if (defeatTicks == DEFEAT_DIALOGUE_2_TICK) {
            Component line = Component.empty()
                    .append(Component.translatable("message.finalparadox.koros.guide.speaker")
                            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFBBDFF))
                                    .withBold(true).withItalic(true)))
                    .append(Component.translatable("dialogue.finalparadox.thar_kroo.defeat.2"));
            for (ServerPlayer player : ArenaFightParticipants.onlinePlayers(server, ArenaDefinitions.B2)) {
                player.sendSystemMessage(line);
            }
            server.playSound(null, blockPosition(), SoundEvents.TRIDENT_RETURN,
                    SoundSource.MASTER, 1.0F, 1.7F);
        }
        if (defeatTicks >= DEFEAT_RESPAWN_TICKS) {
            restartAfterDefeat(server);
        }
    }

    private void restartAfterDefeat(ServerLevel server) {
        defeatActive = false;
        defeatTicks = 0;
        BlockPos destination = new BlockPos(
                Mth.floor(anchorX), Mth.floor(anchorY), Mth.floor(anchorZ) - 21);
        for (ServerPlayer player : ArenaFightParticipants.onlinePlayers(server, ArenaDefinitions.B2)) {
            if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                player.teleportTo(server, destination.getX() + 0.5D, destination.getY(),
                        destination.getZ() + 0.5D, 0.0F, 0.0F);
                player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);
            }
            player.removeEffect(MobEffects.WITHER);
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, 2020, 1, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.HEAL, 20, 10, true, false));
        }
        ArenaDeploymentData data = ArenaDeploymentData.get(server, ArenaDefinitions.B2);
        BlockPos anchor = data.floorAnchor().orElse(
                new BlockPos(Mth.floor(anchorX), Mth.floor(anchorY), Mth.floor(anchorZ)));
        cleanupEncounter(server);
        data.clearActiveBoss();
        discard();
        B2ArenaStaging.spawn(server, data, anchor)
                .ifPresent(stage -> stage.boss().markPreBattleDialoguePlayed());
    }

    private void tickIntro() {
        if (phaseTick >= INTRO_END) {
            phase = COUNTDOWN;
            phaseTick = -1;
        }
    }

    private void tickCountdown() {
        if (phaseTick == 60 || phaseTick == 80 || phaseTick == 100) {
            int value = phaseTick == 60 ? 3 : phaseTick == 80 ? 2 : 1;
            showTitle(Component.translatable("title.finalparadox.thar_kroo.countdown", value), Component.empty());
            playGlobal(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0F, 1.5F);
        } else if (phaseTick >= 120) startCombatPhase(PHASE_ONE);
    }

    private void startCombatPhase(int nextPhase) {
        phase = nextPhase;
        phaseTick = -1;
        abilityTick = 0;
        summonTick = 0;
        combatSecondTick = 0;
        abilityLockTick = 0;
        postIntermissionH6Tick = 0;
        shielded = false;
        setInvulnerable(false);
        entityData.set(DISPLAY_MODE, 0);
        bossEvent.setVisible(true);
        scaleHealthForPlayers();
        if (nextPhase == PHASE_ONE) {
            h1 = 45;
            h3 = -20;
            h5 = Integer.MIN_VALUE;
            h6 = 1;
            h80 = 0;
            h81 = 30;
        } else {
            h1 = 0;
            h3 = 17;
            h5 = 0;
            h6 = 1;
            h80 = Integer.MIN_VALUE;
            h81 = 0;
        }
        redVolleyCasts = 0;
        if (nextPhase == PHASE_ONE && musicTick < 0 && level() instanceof ServerLevel server) {
            musicTick = 0;
            playTharMusic(server);
        }
        int phaseNumber = nextPhase == PHASE_ONE ? 1 : nextPhase == PHASE_TWO ? 2 : 3;
        showTitle(Component.translatable("title.finalparadox.thar_kroo.phase.marker"),
                Component.translatable("title.finalparadox.thar_kroo.phase", phaseNumber));
        playGlobal(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 1.5F);
        if (nextPhase == PHASE_ONE && level() instanceof ServerLevel server) {
            spawnTrash(server);
        } else {
            startBarrage();
        }
    }

    private void scaleHealthForPlayers() {
        int players = Math.max(1, combatPlayers().size());
        double max = players == 1 ? 450.0D : players == 2 ? 770.0D : players == 3 ? 800.0D
                : players == 4 ? 900.0D : 1000.0D;
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(max);
        setHealth((float) max);
    }

    private void tickCombat(ServerLevel server) {
        if (shielded && ownedCustodians().isEmpty()) {
            shielded = false;
            setInvulnerable(false);
            playGlobal(SoundEvents.GLASS_BREAK, 1.0F, 0.8F);
        }

        if (!shielded && abilityLockTick <= 0 && getHealth() <= getMaxHealth() * 0.15F) {
            if (phase == PHASE_ONE) startIntermission(INTERMISSION_ONE);
            else if (phase == PHASE_TWO) startIntermission(INTERMISSION_TWO);
            else startFinale();
            return;
        }

        abilityTick++;
        summonTick++;
        if (++combatSecondTick < 20) return;
        combatSecondTick = 0;

        h1++;
        h3++;
        h6++;
        if (phase != PHASE_ONE) h5++;
        if (phase == PHASE_ONE) h80++;
        if (ownedCustodians().isEmpty()) h81++;

        if (abilityLockTick <= 0 && h1 >= 50 && laserTick < 0) {
            h1 = Integer.MIN_VALUE;
            startLaser();
        }
        int h3Limit = phase == PHASE_THREE ? 25 : 23;
        if (abilityLockTick <= 0 && h3 >= h3Limit) {
            h3 = 0;
            startFlameCharge(false);
            if (phase == PHASE_THREE) delayedRapidFlameTick = 160;
        }
        if (phase != PHASE_ONE && h5 >= 15) {
            h5 = 0;
            startBarrage();
        }
        if (abilityLockTick <= 0 && h6 >= 9 && blackfireTick < 0) {
            h6 = 0;
            startBlackfireVolley(phase != PHASE_ONE);
        }
        if (phase == PHASE_ONE && h80 >= 48) {
            h80 = 0;
            spawnTrash(server);
        }
        int custodianLimit = phase == PHASE_ONE ? 55 : phase == PHASE_THREE ? 60 : 55;
        if (h81 >= custodianLimit && ownedCustodians().isEmpty()) {
            h81 = 0;
            spawnCustodians(server);
        }
    }

    private void startIntermission(int nextPhase) {
        phase = nextPhase;
        phaseTick = -1;
        setInvulnerable(true);
        shielded = false;
        entityData.set(DISPLAY_MODE, 1);
        cleanupTransient();
        discardOwnedMinions((ServerLevel) level());
        postIntermissionH6Tick = 0;
        int intermission = nextPhase == INTERMISSION_ONE ? 1 : 2;
        int followingPhase = intermission + 1;
        showTitle(Component.translatable("title.finalparadox.thar_kroo.phase.marker"),
                Component.translatable("title.finalparadox.thar_kroo.intermission", intermission));
        dialogue.add(new ScheduledLine(totalTick + 20,
                "dialogue.finalparadox.thar_kroo.phase." + followingPhase + ".1"));
        dialogue.add(new ScheduledLine(totalTick + 80,
                "dialogue.finalparadox.thar_kroo.phase." + followingPhase + ".2"));
    }

    private void tickIntermission(ServerLevel server) {
        boolean second = phase == INTERMISSION_TWO;
        if (second && phaseTick == 40) spawnTrash(server);
        if (phaseTick == 110) startPushWave();
        if (phaseTick == 330) startFireField(false);
        if (phaseTick == 940) startFireField(true);
        if (phaseTick == 1080) {
            entityData.set(DISPLAY_MODE, 0);
            spawnCustodians(server);
        }
        if (phaseTick == 1170) {
            if (second) spawnTrash(server);
            startBlackfireVolley(false);
        }
        if (phaseTick == 1260) startBlackfireVolley(false);
        if (phaseTick == 1320) startHomingVolley();
        if (second && phaseTick == 1360) startFlameCharge(false);
        if (second && (phaseTick == 1520 || phaseTick == 1640)) startFlameCharge(true);
        int endTick = second ? 1640 : 1340;
        if (phaseTick >= endTick && !ownedCustodians().isEmpty()) {
            if (++postIntermissionH6Tick >= 180) {
                postIntermissionH6Tick = 0;
                startBlackfireVolley(false);
            }
        }
        if (phaseTick >= endTick && ownedCustodians().isEmpty()) {
            restoreArena(server);
            startCombatPhase(phase == INTERMISSION_ONE ? PHASE_TWO : PHASE_THREE);
        }
    }

    private void startFinale() {
        phase = FINALE;
        phaseTick = -1;
        setInvulnerable(true);
        bossEvent.setVisible(false);
        cleanupTransient();
        if (level() instanceof ServerLevel server) discardOwnedMinions(server);
        int[] times = {0,60,130,240,410,540,700,840,970,1070,1160};
        for (int line = 1; line <= times.length; line++) {
            dialogue.add(new ScheduledLine(totalTick + times[line - 1],
                    "dialogue.finalparadox.thar_kroo.finale." + line));
        }
        entityData.set(DISPLAY_MODE, 2);
    }

    private void tickFinale(ServerLevel server) {
        if (phaseTick % 4 == 0) server.sendParticles(ParticleTypes.LARGE_SMOKE,
                getX(), getY() + 1.5D, getZ(), 4, 0.7D, 1.0D, 0.7D, 0.02D);
        if (phaseTick >= 1320) {
            if ((displayMode() & 0xFF) != 6) entityData.set(DISPLAY_MODE, 6);
            double lift = Math.min(8.15D, (phaseTick - 1320) * 0.05D);
            if (phaseTick % 2 == 0) server.sendParticles(ParticleTypes.PORTAL,
                    getX(), getY() + lift, getZ(), 16, 1.8D, 1.2D, 1.8D, 0.08D);
        }
        if (phaseTick >= 1483 && !completionHandled) finishTrial(server);
    }

    private void finishTrial(ServerLevel server) {
        completionHandled = true;
        phase = COMPLETE;
        showTitle(Component.translatable("title.finalparadox.thar_kroo.victory"),
                Component.translatable("subtitle.finalparadox.thar_kroo.victory"));
        playGlobal(SoundEvents.PLAYER_LEVELUP, 1.0F, 0.8F);
        spawnAtLocation(createTharFragment());
        cleanupEncounter(server);
        discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (phase == PHASE_ONE || phase == PHASE_TWO || phase == PHASE_THREE) {
            float floor = getMaxHealth() * 0.15F;
            if (getHealth() - amount < floor) amount = Math.max(0.0F, getHealth() - floor);
        }
        return amount > 0.0F && super.hurt(source, amount);
    }

    private void startLaser() {
        if (laserTick >= 0) return;
        List<ServerPlayer> players = combatPlayers();
        if (players.isEmpty()) return;
        laserTick = 0;
        ServerPlayer target = players.get(random.nextInt(players.size()));
        laserTarget = target.getUUID();
        laserDirection = horizontalDirection(position(), target.position());
        faceVisual(laserDirection);
        laserRotationStep = random.nextBoolean() ? -0.9D : 0.9D;
        abilityLockTick = 270;
        laserHits.clear();
        showTitle(Component.translatable("title.finalparadox.thar_kroo.ability.marker"),
                Component.translatable("title.finalparadox.thar_kroo.ability.laser", target.getDisplayName()));
        playGlobal(SoundEvents.BEACON_POWER_SELECT, 1.0F, 1.2F);
    }

    private void tickLaser(ServerLevel server) {
        laserTick++;
        ServerPlayer target = laserTarget == null ? null
                : server.getServer().getPlayerList().getPlayer(laserTarget);
        Vec3 origin = new Vec3(getX(), anchorY - 0.2D, getZ());

        if (laserTick <= 100) {
            if (target != null && target.level() == server) {
                laserDirection = horizontalDirection(origin, target.position());
                faceVisual(laserDirection);
            }
            Vec3 side = new Vec3(-laserDirection.z, 0, laserDirection.x).scale(0.5D);
            for (double distance = 1.0D; distance <= 34.0D; distance += 1.5D) {
                Vec3 center = origin.add(laserDirection.scale(distance));
                Vec3 left = center.add(side);
                Vec3 right = center.subtract(side);
                server.sendParticles(ParticleTypes.FLAME, left.x, anchorY + 0.2D, left.z, 1, 0, 0, 0, 0);
                server.sendParticles(ParticleTypes.FLAME, right.x, anchorY + 0.2D, right.z, 1, 0, 0, 0, 0);
            }
        } else if (laserTick == 101 || laserTick == 113 || laserTick == 125) {
            int countdown = laserTick == 101 ? 3 : laserTick == 113 ? 2 : 1;
            showTitle(Component.translatable("title.finalparadox.thar_kroo.countdown", countdown),
                    Component.translatable("title.finalparadox.thar_kroo.ability.laser"));
            playGlobal(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 2.0F);
            for (int segment = 1; segment <= 30; segment++) {
                Vec3 point = origin.add(laserDirection.scale(segment));
                server.sendParticles(RED_DUST, point.x, point.y, point.z,
                        1, 0, 0, 0, 0);
            }
        } else if (laserTick >= 137 && laserTick < 247) {
            int beamAge = laserTick - 137;
            if (beamAge == 0) startVisualAnimation(laserRotationStep < 0.0D ? 3 : 4, 108);
            int rotationTicks = Mth.clamp(beamAge - 4, 0, 100);
            Vec3 sweepingDirection = rotateHorizontal(laserDirection,
                    Math.toRadians(laserRotationStep * rotationTicks));
            addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2, 1, false, false));
            for (int segment = 1; segment <= 86; segment++) {
                double distance = 0.4D + (segment <= 70
                        ? segment * 0.6D
                        : 42.4D + (segment - 71) * 0.4D);
                Vec3 point = origin.add(sweepingDirection.scale(distance));
                server.sendParticles((laserTick + segment) % 8 == 0 ? ParticleTypes.END_ROD : RED_DUST,
                        point.x, point.y, point.z, 1, 0, 0, 0, 0);
                for (ServerPlayer player : combatPlayers()) {
                    if (!laserIntersects(player, point) || !laserHits.add(player.getUUID())) continue;
                    int damageLevel = laserDamageLevels.merge(player.getUUID(), 1, Integer::sum);
                    int amplifier = switch (damageLevel) {
                        case 1 -> 2;
                        case 2 -> 3;
                        case 3 -> 4;
                        case 4 -> 80;
                        default -> -1;
                    };
                    if (amplifier >= 0) {
                        player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, amplifier, false, false), this);
                    }
                    server.sendParticles(ParticleTypes.LAVA, player.getX(), player.getY() + 1.0D,
                            player.getZ(), 6, 0, 0, 0, 0);
                }
            }
        }
        if (laserTick >= 247) {
            laserTick = -1;
            laserTarget = null;
            laserRotationStep = 0.0D;
            laserHits.clear();
            playGlobal(SoundEvents.FIRE_EXTINGUISH, 1.0F, 0.6F);
        }
    }

    private static Vec3 horizontalDirection(Vec3 origin, Vec3 target) {
        Vec3 direction = target.subtract(origin).multiply(1, 0, 1);
        return direction.lengthSqr() < 1.0E-6D ? new Vec3(0, 0, 1) : direction.normalize();
    }

    private static Vec3 rotateHorizontal(Vec3 direction, double radians) {
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        return new Vec3(direction.x * cosine - direction.z * sine, 0,
                direction.x * sine + direction.z * cosine);
    }

    private void faceVisual(Vec3 direction) {
        if (direction.lengthSqr() < 1.0E-6D) return;
        entityData.set(VISUAL_YAW, (float) Math.toDegrees(Math.atan2(-direction.x, direction.z)));
    }

    private static boolean laserIntersects(ServerPlayer player, Vec3 point) {
        for (int verticalSample = 0; verticalSample <= 8; verticalSample += 2) {
            double dx = player.getX() - point.x;
            double dy = player.getY() - verticalSample - point.y;
            double dz = player.getZ() - point.z;
            if (dx * dx + dy * dy + dz * dz <= 1.21D) return true;
        }
        return false;
    }

    private void startPushWave() {
        pushTick = 0;
        playGlobal(SoundEvents.WITHER_SHOOT, 1.0F, 0.8F);
    }

    private void tickPushWave(ServerLevel server) {
        pushTick++;
        double pull = pushTick <= 80 ? 0.11D : pushTick <= 140 ? 0.22D : 0.33D;
        double particleRadius = Math.max(2.0D, 30.0D - pushTick * 0.12D);
        for (int i = 0; i < 32; i++) {
            double a = Math.PI * 2 * i / 32.0D + pushTick * 0.025D;
            server.sendParticles(ParticleTypes.FLAME, getX() + Math.cos(a) * particleRadius, anchorY + 0.3D,
                    getZ() + Math.sin(a) * particleRadius, 1, 0, 0, 0, 0);
        }
        for (ServerPlayer player : combatPlayers()) {
            Vec3 toward = position().subtract(player.position()).multiply(1, 0, 1);
            if (toward.lengthSqr() <= 0.01D) continue;
            Vec3 movement = toward.normalize().scale(Math.min(pull, Math.sqrt(toward.lengthSqr())));
            player.teleportTo(player.getX() + movement.x, player.getY(), player.getZ() + movement.z);
        }
        if (pushTick >= 220) pushTick = -1;
    }

    private void startFlameCharge(boolean rapid) {
        List<ServerPlayer> players = combatPlayers();
        if (players.isEmpty()) return;
        ServerPlayer target = players.get(random.nextInt(players.size()));
        FlameChargeState charge = new FlameChargeState(target.getUUID(), rapid ? 120 : 160);
        charge.position = new Vec3(getX(), anchorY, getZ());
        charge.direction = horizontalDirection(charge.position, target.position());
        flameCharges.add(charge);
        flameTick = 0;
        faceVisual(charge.direction);
        if (isCombatPhase()) abilityLockTick = rapid ? 140 : 180;
        showTitle(Component.translatable("title.finalparadox.thar_kroo.ability.marker"),
                Component.translatable("title.finalparadox.thar_kroo.ability.flame_charge", target.getDisplayName()));
    }

    private void tickFlameCharge(ServerLevel server) {
        for (Iterator<FlameChargeState> iterator = flameCharges.iterator(); iterator.hasNext();) {
            FlameChargeState charge = iterator.next();
            charge.tick++;
            ServerPlayer target = server.getServer().getPlayerList().getPlayer(charge.target);

            if (charge.tick <= charge.launchTick) {
                if (target != null && target.level() == server) {
                    charge.direction = horizontalDirection(charge.position, target.position());
                    faceVisual(charge.direction);
                }
                double[] layerY = {-1.9021D, -1.6180D, -1.1756D, -0.6180D, 0.0D,
                        0.6180D, 1.1756D, 1.6180D, 1.9021D};
                double[] layerRadius = {0.6180D, 1.1756D, 1.6180D, 1.9021D, 2.0D,
                        1.9021D, 1.6180D, 1.1756D, 0.6180D};
                int[] jumpTicks = {1, 8, 16, 24, 32, 40, 48, 56, 64};
                double commonRise = Math.min(charge.tick, 64) * 0.023D;
                for (int layer = 0; layer < 9; layer++) {
                    double y = anchorY - 9.0D + commonRise + layerY[layer]
                            + (charge.tick >= jumpTicks[layer] ? 8.0D : 0.0D);
                    double ringRadius = layerRadius[layer];
                    for (int point = 0; point < 12; point++) {
                        double angle = point * Math.PI / 6.0D + charge.tick * 0.035D;
                        server.sendParticles(ParticleTypes.FLAME,
                                charge.position.x + Math.cos(angle) * ringRadius, y,
                                charge.position.z + Math.sin(angle) * ringRadius, 1, 0, 0, 0, 0);
                    }
                }
            }

            if (charge.tick == charge.launchTick) {
                startVisualAnimation(5, 15);
                playGlobal(SoundEvents.BLAZE_SHOOT, 1.0F, 0.7F);
                addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 10, false, false));
            } else if (charge.tick > charge.launchTick) {
                charge.position = charge.position.add(charge.direction.scale(0.55D));
                server.sendParticles(ParticleTypes.FLAME, charge.position.x, charge.position.y + 0.3D,
                        charge.position.z, 12, 1.4D, 0.6D, 1.4D, 0.02D);
                alterFlameGround(server, charge.position);
                for (ServerPlayer player : combatPlayers()) {
                    if (player.distanceToSqr(charge.position) <= 6.25D && charge.hits.add(player.getUUID())) {
                        player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1, false, false), this);
                    }
                }
            }

            double dx = charge.position.x - getX();
            double dz = charge.position.z - getZ();
            if (charge.tick > charge.launchTick && dx * dx + dz * dz >= 2500.0D) {
                iterator.remove();
            }
        }

        if (flameCharges.isEmpty()) {
            flameTick = -1;
        } else {
            flameTick++;
        }
    }

    private void alterFlameGround(ServerLevel server, Vec3 center) {
        int floorY = Mth.floor(anchorY - 1.0D);
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                BlockPos pos = BlockPos.containing(center.x + dx, floorY, center.z + dz);
                if (!server.getBlockState(pos).isAir()) {
                    alterGround(server, pos, Blocks.MAGMA_BLOCK.defaultBlockState());
                }
            }
        }
    }

    private boolean isCombatPhase() {
        return phase == PHASE_ONE || phase == PHASE_TWO || phase == PHASE_THREE;
    }

    private void startFireField(boolean rapid) {
        if (!(level() instanceof ServerLevel server) || fireFieldTick >= 0) return;
        fireFieldTick = 0;
        fireFieldWarningTicks = rapid ? 60 : 160;
        fireFieldActivatedPoints = 0;
        fireFieldExplosionWave = 0;
        fireFieldDangerPoints.clear();
        List<Vec3> candidates = new ArrayList<>();
        for (double[] offset : FIRE_FIELD_OFFSETS) {
            Vec3 candidate = new Vec3(anchorX + offset[0], anchorY, anchorZ + offset[1]);
            if (isValidFireFieldSafePoint(server, candidate)) candidates.add(candidate);
        }
        if (candidates.isEmpty()) {
            for (double[] offset : FIRE_FIELD_OFFSETS) {
                candidates.add(new Vec3(anchorX + offset[0], anchorY, anchorZ + offset[1]));
            }
        }
        Vec3 safe = candidates.remove(random.nextInt(candidates.size()));
        safeX = safe.x;
        safeZ = safe.z;
        candidates.sort((first, second) -> Double.compare(
                second.distanceToSqr(safe), first.distanceToSqr(safe)));
        for (double[] offset : FIRE_FIELD_OFFSETS) {
            Vec3 point = new Vec3(anchorX + offset[0], anchorY, anchorZ + offset[1]);
            if (point.distanceToSqr(safe) > 0.01D && candidates.stream()
                    .noneMatch(existing -> existing.distanceToSqr(point) < 0.01D)) {
                candidates.add(point);
            }
        }
        candidates.sort((first, second) -> Double.compare(
                second.distanceToSqr(safe), first.distanceToSqr(safe)));
        fireFieldDangerPoints.addAll(candidates);
        fireFieldStageStart = 4 + Math.max(0, fireFieldDangerPoints.size() - 1) * 2 + fireFieldWarningTicks;
        fireFieldDuration = fireFieldStageStart + 400;
        showTitle(Component.translatable("title.finalparadox.thar_kroo.ability.marker"),
                Component.translatable("title.finalparadox.thar_kroo.ability.fire_field"));
        playGlobal(SoundEvents.RESPAWN_ANCHOR_CHARGE, 1.0F, 0.65F);
    }

    private boolean isValidFireFieldSafePoint(ServerLevel server, Vec3 point) {
        int floorY = Mth.floor(anchorY - 1.0D);
        int valid = 0;
        int[][] samples = {{0, 0}, {4, 0}, {-4, 0}, {0, 4}, {0, -4},
                {3, 3}, {-3, 3}, {3, -3}, {-3, -3}};
        for (int[] sample : samples) {
            BlockPos floor = BlockPos.containing(point.x + sample[0], floorY, point.z + sample[1]);
            BlockState state = server.getBlockState(floor);
            if (!state.isAir() && !state.is(Blocks.MAGMA_BLOCK)
                    && server.getBlockState(floor.above()).isAir()) {
                valid++;
            }
        }
        return valid >= 6;
    }

    private void growFireFieldCore(ServerLevel server, Vec3 point, int radius) {
        int floorY = Mth.floor(anchorY - 1.0D);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos pos = BlockPos.containing(point.x + dx, floorY, point.z + dz);
                if (!server.getBlockState(pos).isAir()) {
                    alterGround(server, pos, Blocks.MAGMA_BLOCK.defaultBlockState());
                }
            }
        }
    }

    private void tickFireField(ServerLevel server) {
        fireFieldTick++;
        if (fireFieldTick % 4 == 0) {
            server.sendParticles(ParticleTypes.END_ROD, safeX, anchorY + 0.2D, safeZ,
                    12, 3.5D, 0.05D, 3.5D, 0.01D);
            for (Vec3 point : fireFieldDangerPoints.subList(0, fireFieldActivatedPoints)) {
                server.sendParticles(ParticleTypes.FLAME, point.x, anchorY + 0.15D, point.z,
                        1, 0.25D, 0.02D, 0.25D, 0);
            }
        }

        if (fireFieldTick >= 4 && (fireFieldTick - 4) % 2 == 0
                && fireFieldActivatedPoints < fireFieldDangerPoints.size()) {
            growFireFieldCore(server, fireFieldDangerPoints.get(fireFieldActivatedPoints++), 0);
        }

        int elapsed = fireFieldTick - fireFieldStageStart;
        int[] growthTicks = {0, 40, 65, 90, 115, 140, 165, 190, 215, 240, 265};
        for (int radius = 0; radius < growthTicks.length; radius++) {
            if (elapsed == growthTicks[radius]) {
                for (Vec3 point : fireFieldDangerPoints) growFireFieldCore(server, point, radius);
            }
        }

        if (elapsed >= 310 && elapsed <= 328 && elapsed % 2 == 0 && fireFieldExplosionWave < 10) {
            for (int n = 0; n < 2; n++) {
                int index = fireFieldExplosionWave * 2 + n;
                if (index >= fireFieldDangerPoints.size()) continue;
                Vec3 point = fireFieldDangerPoints.get(index);
                for (ServerPlayer player : combatPlayers()) {
                    if (player.distanceToSqr(point) <= 110.25D) {
                        player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 6, false, false), this);
                    }
                }
                server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, point.x, anchorY, point.z,
                        1, 0, 0, 0, 0);
            }
            fireFieldExplosionWave++;
        }

        if (elapsed == 340) restoreArenaFraction(server, 0.30D);
        if (elapsed == 360) restoreArenaFraction(server, 0.50D);
        if (elapsed == 380) restoreArenaFraction(server, 0.70D);
        if (fireFieldTick >= fireFieldDuration) {
            fireFieldTick = -1;
            fireFieldDangerPoints.clear();
            restoreArena(server);
        }
    }

    private void restoreArenaFraction(ServerLevel server, double fraction) {
        int amount = Mth.ceil(alteredBlocks.size() * fraction);
        Iterator<Map.Entry<BlockPos, BlockState>> iterator = alteredBlocks.entrySet().iterator();
        while (iterator.hasNext() && amount-- > 0) {
            Map.Entry<BlockPos, BlockState> entry = iterator.next();
            server.setBlock(entry.getKey(), entry.getValue(), 2);
            iterator.remove();
        }
    }

    private void startBarrage() {
        barrageTick = 0;
        playGlobal(SoundEvents.BLAZE_SHOOT, 0.8F, 0.55F);
    }

    private void tickBarrage(ServerLevel server) {
        if (barrageTick >= 0) barrageTick++;
        barrageHitCooldowns.replaceAll((uuid, ticks) -> ticks - 1);
        barrageHitCooldowns.values().removeIf(ticks -> ticks <= 0);

        if (barrageTick == 1 || barrageTick == 11 || barrageTick == 21) {
            if (activeTornadoTracks.size() < 12) {
                int track;
                do track = random.nextInt(12); while (activeTornadoTracks.contains(track));
                activeTornadoTracks.add(track);
                tornadoAges.put(track, 0);
            }
        }
        if (barrageTick > 21) barrageTick = -1;

        tornadoAges.replaceAll((track, age) -> age + 1);
        for (Iterator<Map.Entry<Integer, Integer>> iterator = tornadoAges.entrySet().iterator();
             iterator.hasNext();) {
            Map.Entry<Integer, Integer> tornado = iterator.next();
            int track = tornado.getKey();
            int age = tornado.getValue();
            if (age >= 600) {
                iterator.remove();
                activeTornadoTracks.remove(track);
                continue;
            }
            double initialYaw = (track & 1) == 0 ? 180.0D : 0.0D;
            double yaw = Math.toRadians(initialYaw + TORNADO_YAW_STEP[track] * age);
            double radius = TORNADO_RADII[track];
            Vec3 point = new Vec3(anchorX - Math.sin(yaw) * radius, anchorY - 0.2D,
                    anchorZ + Math.cos(yaw) * radius);
            server.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z,
                    5, 0.35D, 0.8D, 0.35D, 0.02D);
            server.sendParticles(PURPLE_DUST, point.x, point.y + 0.5D, point.z,
                    1, 0, 0, 0, 0);

            if (age < 60) continue;
            for (ServerPlayer player : combatPlayers()) {
                if (!barrageHitCooldowns.containsKey(player.getUUID())
                        && player.distanceToSqr(point) <= 1.21D) {
                    barrageHitCooldowns.put(player.getUUID(), 30);
                    server.explode(null, player.getX(), player.getY(), player.getZ(), 2.0F,
                            Level.ExplosionInteraction.NONE);
                }
            }
        }
        if (barrageTick < 0 && tornadoAges.isEmpty()) {
            barrageHitCooldowns.clear();
        }
    }

    private void startBlackfireVolley(boolean red) {
        if (blackfireTick >= 0) return;
        List<ServerPlayer> players = combatPlayers();
        if (players.isEmpty()) return;
        ServerPlayer target = players.get(random.nextInt(players.size()));
        blackfireTarget = target.getUUID();
        blackfireDirection = horizontalDirection(position(), target.position());
        faceVisual(blackfireDirection);
        blackfireRed = red;
        blackfireTick = 0;
        if (red && ++redVolleyCasts >= 2) {
            redVolleyCasts = 0;
            delayedHomingTick = 100;
            if (isCombatPhase()) abilityLockTick = 160;
        }
    }

    private void tickBlackfireVolley() {
        blackfireTick++;
        if (blackfireTick == 25) spawnBlackfireWave(7, 30.0D);
        if (blackfireTick == 50) spawnBlackfireWave(6, 25.0D);
        if (blackfireTick == 75) spawnBlackfireWave(7, 30.0D);
        if (blackfireTick >= 76) {
            blackfireTick = -1;
            blackfireTarget = null;
        }
    }

    private void spawnBlackfireWave(int count, double spread) {
        startVisualAnimation(5, 15);
        ServerPlayer target = blackfireTarget == null ? null
                : level().getServer().getPlayerList().getPlayer(blackfireTarget);
        if (target != null && target.level() == level()) {
            blackfireDirection = horizontalDirection(position(), target.position());
            faceVisual(blackfireDirection);
        }
        Vec3 origin = new Vec3(getX(), anchorY - 0.4D, getZ());
        double step = count <= 1 ? 0.0D : spread * 2.0D / (count - 1);
        for (int index = 0; index < count; index++) {
            double degrees = -spread + index * step;
            double radians = Math.toRadians(degrees);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            Vec3 direction = new Vec3(
                    blackfireDirection.x * cos - blackfireDirection.z * sin,
                    0,
                    blackfireDirection.x * sin + blackfireDirection.z * cos);
            shadowOrbs.add(new ShadowOrb(origin, direction.scale(0.3D), null, false, false));
        }
        playGlobal(SoundEvents.WITHER_SHOOT, 1.0F, 1.1F);
    }

    private void startHomingVolley() {
        startVisualAnimation(5, 15);
        Vec3 origin = new Vec3(getX(), anchorY + 0.4D, getZ());
        for (ServerPlayer target : combatPlayers()) {
            Vec3 velocity = new Vec3(0, 0, 0.18D);
            shadowOrbs.add(new ShadowOrb(origin, velocity, target.getUUID(), true, true));
            faceVisual(horizontalDirection(origin, target.position()));
        }
        playGlobal(SoundEvents.WITHER_SHOOT, 1.0F, 0.8F);
    }

    private void tickShadowOrbs(ServerLevel server) {
        for (Iterator<ShadowOrb> it = shadowOrbs.iterator(); it.hasNext();) {
            ShadowOrb orb = it.next();
            orb.age++;
            orb.position = orb.position.add(orb.velocity);
            if (orb.homing && orb.target != null) {
                ServerPlayer target = server.getServer().getPlayerList().getPlayer(orb.target);
                if (target != null && target.level() == server) {
                    Vec3 desired = target.getEyePosition().subtract(orb.position).normalize().scale(0.18D);
                    if (desired.lengthSqr() > 1.0E-6D) orb.velocity = desired;
                }
            }
            server.sendParticles(orb.red ? RED_DUST : PURPLE_DUST,
                    orb.position.x, orb.position.y, orb.position.z, 1, 0, 0, 0, 0);
            double dx = orb.position.x - getX();
            double dz = orb.position.z - getZ();
            boolean remove = orb.age >= 200 || !orb.homing && dx * dx + dz * dz >= 2500.0D;
            for (ServerPlayer player : combatPlayers()) {
                if (!remove && player.distanceToSqr(orb.position) <= 1.25D) {
                    if (!orb.homing) {
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 0));
                    }
                    player.addEffect(new MobEffectInstance(MobEffects.HARM, 1,
                            orb.homing ? 1 : 2, false, false), this);
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
                    remove = true;
                }
            }
            if (remove) it.remove();
        }
    }

    private void spawnTrash(ServerLevel server) {
        if (ownedMinions().size() >= 12) return;
        int count = 2 + combatPlayers().size();
        List<Vec3> candidates = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            double angle = Math.toRadians(i * 22.5D);
            candidates.add(new Vec3(anchorX + Math.cos(angle) * 16.0D, anchorY,
                    anchorZ + Math.sin(angle) * 16.0D));
        }
        for (int i = 0; i < 24; i++) {
            double angle = Math.toRadians(i * 15.0D);
            candidates.add(new Vec3(anchorX + Math.cos(angle) * 20.0D, anchorY,
                    anchorZ + Math.sin(angle) * 20.0D));
        }
        for (int i = 0; i < count && !candidates.isEmpty(); i++) {
            Vec3 point = candidates.remove(random.nextInt(candidates.size()));
            BlockPos floor = BlockPos.containing(point.x, anchorY - 1.0D, point.z);
            if (server.getBlockState(floor).is(Blocks.MAGMA_BLOCK)) {
                i--;
                continue;
            }
            spawnSkeleton(server, point, random.nextBoolean());
        }
    }

    private void spawnCustodians(ServerLevel server) {
        if (!ownedCustodians().isEmpty()) return;
        shielded = true;
        setInvulnerable(true);
        int count = 1 + Math.max(1, combatPlayers().size());
        List<Vec3> candidates = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            double angle = Math.toRadians(18.0D + i * 36.0D);
            candidates.add(new Vec3(anchorX + Math.cos(angle) * 13.0D, anchorY,
                    anchorZ + Math.sin(angle) * 13.0D));
        }
        List<Vec3> chosen = new ArrayList<>();
        while (chosen.size() < count && !candidates.isEmpty()) {
            Vec3 point = candidates.remove(random.nextInt(candidates.size()));
            BlockPos floor = BlockPos.containing(point.x, anchorY - 1.0D, point.z);
            if (server.getBlockState(floor).is(Blocks.MAGMA_BLOCK)
                    || chosen.stream().anyMatch(other -> other.distanceToSqr(point) < 25.0D)) continue;
            chosen.add(point);
            spawnZombie(server, point, true);
        }
        playGlobal(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 0.7F);
        broadcast(Component.translatable("message.finalparadox.thar_kroo.custodian_shield"));
    }

    private void spawnZombie(ServerLevel server, Vec3 point, boolean custodian) {
        Zombie zombie = EntityType.ZOMBIE.create(server);
        if (zombie == null) return;
        configureMinion(zombie, point, custodian);
        ItemStack weapon = new ItemStack(custodian ? Items.ENCHANTED_BOOK : Items.NETHERITE_SWORD);
        if (custodian) weapon.enchant(Enchantments.SHARPNESS, 2);
        zombie.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        if (custodian) equipAshArmor(zombie, true);
        server.addFreshEntity(zombie);
    }

    private void spawnSkeleton(ServerLevel server, Vec3 point, boolean melee) {
        Skeleton skeleton = EntityType.SKELETON.create(server);
        if (skeleton == null) return;
        configureMinion(skeleton, point, false);
        ItemStack weapon = new ItemStack(melee ? Items.NETHERITE_SWORD : Items.BOW);
            weapon.enchant(melee ? Enchantments.SHARPNESS : Enchantments.POWER_ARROWS, 5);
        skeleton.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        skeleton.getAttribute(Attributes.MAX_HEALTH).setBaseValue(melee ? 40.0D : 30.0D);
        skeleton.setHealth(melee ? 40.0F : 30.0F);
        equipAshArmor(skeleton, false);
        skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(melee
                ? Items.POLISHED_BLACKSTONE : Items.CHISELED_POLISHED_BLACKSTONE));
        server.addFreshEntity(skeleton);
    }

    private void configureMinion(Mob mob, Vec3 point, boolean custodian) {
        mob.moveTo(point.x, point.y, point.z, random.nextFloat() * 360.0F, 0);
        mob.setPersistenceRequired();
        mob.setCustomName(Component.translatable(custodian ? "entity.finalparadox.thar_kroo.custodian"
                : "entity.finalparadox.thar_kroo.ash_minion"));
        mob.setCustomNameVisible(true);
        mob.getPersistentData().putUUID(OWNER_KEY, getUUID());
        mob.getPersistentData().putBoolean(MINION_KEY, true);
        mob.getPersistentData().putBoolean(CUSTODIAN_KEY, custodian);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(custodian ? 100.0D : 40.0D);
        mob.setHealth(custodian ? 90.0F : 35.0F);
        mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(custodian ? 0.155D : 0.25D);
        mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(custodian ? 1.0D : 0.2D);
        mob.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(60.0D);
        mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0, false, false));
        if (custodian) mob.setNoAi(true);
    }

    private void equipAshArmor(Mob mob, boolean custodian) {
        ItemStack boots = dyedLeather(0x222222);
        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.getOrCreateTagElement("display").putInt("color", 0xC73326);
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.getOrCreateTagElement("display").putInt("color", 0x222222);
        mob.setItemSlot(EquipmentSlot.FEET, boots);
        if (custodian) mob.setItemSlot(EquipmentSlot.LEGS, leggings);
        mob.setItemSlot(EquipmentSlot.CHEST, chestplate);
        mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(custodian
                ? Items.BLACK_BANNER : Items.CHISELED_POLISHED_BLACKSTONE));
        for (EquipmentSlot slot : EquipmentSlot.values()) mob.setDropChance(slot, 0.0F);
    }

    private static ItemStack dyedLeather(int color) {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        stack.getOrCreateTagElement("display").putInt("color", color);
        return stack;
    }

    private List<LivingEntity> ownedMinions() {
        if (!(level() instanceof ServerLevel server)) return List.of();
        return server.getEntitiesOfClass(LivingEntity.class, arenaBounds(), entity ->
                entity.getPersistentData().hasUUID(OWNER_KEY)
                        && entity.getPersistentData().getUUID(OWNER_KEY).equals(getUUID())
                        && entity.getPersistentData().getBoolean(MINION_KEY));
    }

    private List<LivingEntity> ownedCustodians() {
        if (!(level() instanceof ServerLevel server)) return List.of();
        return server.getEntitiesOfClass(LivingEntity.class, arenaBounds(), entity ->
                entity.getPersistentData().hasUUID(OWNER_KEY)
                        && entity.getPersistentData().getUUID(OWNER_KEY).equals(getUUID())
                        && entity.getPersistentData().getBoolean(CUSTODIAN_KEY));
    }

    private void discardOwnedMinions(ServerLevel server) {
        for (LivingEntity minion : ownedMinions()) minion.discard();
    }

    private void tickTransientAttacks(ServerLevel server) {
        if (abilityLockTick > 0 && --abilityLockTick == 0 && h1 < -1000000) {
            h1 = 0;
        }
        if (visualAnimationTicks >= 0 && --visualAnimationTicks <= 0) {
            visualAnimationTicks = -1;
            int mode = displayMode() & 0xFF;
            if (mode != 1 && mode != 2) entityData.set(DISPLAY_MODE, 0);
        }
        if (delayedHomingTick >= 0 && --delayedHomingTick <= 0) {
            delayedHomingTick = -1;
            startHomingVolley();
        }
        if (delayedRapidFlameTick >= 0 && --delayedRapidFlameTick <= 0) {
            delayedRapidFlameTick = -1;
            startFlameCharge(true);
        }
        if (laserTick >= 0) tickLaser(server);
        if (pushTick >= 0) tickPushWave(server);
        if (flameTick >= 0) tickFlameCharge(server);
        if (fireFieldTick >= 0) tickFireField(server);
        if (barrageTick >= 0 || !tornadoAges.isEmpty()) tickBarrage(server);
        if (blackfireTick >= 0) tickBlackfireVolley();
        tickShadowOrbs(server);
    }

    private void startVisualAnimation(int mode, int duration) {
        int current = displayMode() & 0xFF;
        if (current == 1 || current == 2) return;
        visualAnimationSequence = (visualAnimationSequence + 1) & 0x7FFFFF;
        entityData.set(DISPLAY_MODE, (visualAnimationSequence << 8) | mode);
        visualAnimationTicks = duration;
    }

    private void alterGround(ServerLevel server, BlockPos pos, BlockState replacement) {
        BlockState original = server.getBlockState(pos);
        if (server.getBlockEntity(pos) != null || original.getDestroySpeed(server, pos) < 0.0F) return;
        alteredBlocks.putIfAbsent(pos.immutable(), original);
        server.setBlock(pos, replacement, 2);
    }

    private void restoreArena(ServerLevel server) {
        for (Map.Entry<BlockPos, BlockState> entry : alteredBlocks.entrySet()) {
            server.setBlock(entry.getKey(), entry.getValue(), 2);
        }
        alteredBlocks.clear();
    }

    private void restoreArenaIfGroundIdle(ServerLevel server) {
        if (flameTick < 0 && fireFieldTick < 0) restoreArena(server);
    }

    private void cleanupTransient() {
        laserTick = pushTick = flameTick = fireFieldTick = barrageTick = blackfireTick = -1;
        abilityLockTick = 0;
        delayedHomingTick = delayedRapidFlameTick = -1;
        visualAnimationTicks = -1;
        flameTarget = null;
        laserTarget = null;
        laserRotationStep = 0.0D;
        blackfireTarget = null;
        laserHits.clear();
        flameHits.clear();
        barrageHitCooldowns.clear();
        activeTornadoTracks.clear();
        tornadoAges.clear();
        flameCharges.clear();
        fireFieldDangerPoints.clear();
        shadowOrbs.clear();
        if (level() instanceof ServerLevel server) restoreArena(server);
    }

    private void cleanupEncounter(ServerLevel server) {
        cleanupTransient();
        for (Entity entity : server.getEntities(this, arenaBounds(), candidate ->
                candidate.getPersistentData().hasUUID(OWNER_KEY)
                        && candidate.getPersistentData().getUUID(OWNER_KEY).equals(getUUID()))) entity.discard();
        dialogue.clear();
        bossEvent.removeAllPlayers();
        bossEvent.setVisible(false);
        stopTharMusic();
        ArenaFightParticipants.clear(server, ArenaDefinitions.B2);
    }

    private List<ServerPlayer> combatPlayers() {
        if (!(level() instanceof ServerLevel server)) return List.of();
        return server.getPlayers(player -> !player.isSpectator() && player.isAlive()
                && player.distanceToSqr(anchorX, anchorY, anchorZ) <= 48.0D * 48.0D);
    }

    private AABB arenaBounds() {
        return new AABB(anchorX - 64, anchorY - 24, anchorZ - 64,
                anchorX + 64, anchorY + 40, anchorZ + 64);
    }

    private void processDialogue() {
        for (Iterator<ScheduledLine> iterator = dialogue.iterator(); iterator.hasNext();) {
            ScheduledLine line = iterator.next();
            if (line.dueTick > totalTick) continue;
            broadcast(Component.translatable(line.key));
            playGlobal(SoundEvents.GILDED_BLACKSTONE_BREAK, 0.55F, 1.3F);
            iterator.remove();
        }
    }

    private void broadcast(Component message) {
        if (level().getServer() == null) return;
        for (ServerPlayer player : level().getServer().getPlayerList().getPlayers()) {
            if (player.level() == level()
                    && (phase != PRE_BATTLE || entranceViewers.contains(player.getUUID()))) {
                player.sendSystemMessage(message);
            }
        }
    }

    private void showTitle(Component title, Component subtitle) {
        for (ServerPlayer player : combatPlayers()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 10));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
        }
    }

    private void playGlobal(SoundEvent sound, float volume, float pitch) {
        if (!(level() instanceof ServerLevel server)) return;
        server.playSound(null, blockPosition(), sound, SoundSource.MASTER, volume, pitch);
    }

    private void playTharMusic(ServerLevel server) {
        server.playSound(null, blockPosition(), ModSounds.THAR_KROO.get(), SoundSource.RECORDS, 4.0F, 1.0F);
    }

    private void tickTharMusic(ServerLevel server) {
        if (musicTick < 0) return;
        musicTick++;
        if (musicTick >= 2684) {
            playTharMusic(server);
            musicTick = 12;
        }
    }

    private void stopTharMusic() {
        if (level().getServer() == null) return;
        ResourceLocation sound = ModSounds.THAR_KROO.get().getLocation();
        for (ServerPlayer player : level().getServer().getPlayerList().getPlayers()) {
            if (encounterPlayers.contains(player.getUUID())) {
                player.connection.send(new ClientboundStopSoundPacket(sound, SoundSource.RECORDS));
            }
        }
        musicTick = -1;
        needsMusicRecovery = false;
        encounterPlayers.clear();
    }

    private static ItemStack createTharFragment() {
        return ModItems.THAR_FRAGMENT.get().getDefaultInstance();
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
        if (level() instanceof ServerLevel server) cleanupEncounter(server);
        super.die(source);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return BuiltInLootTables.EMPTY;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("TharInitialized", initialized);
        tag.putBoolean("TharComplete", completionHandled);
        tag.putBoolean("TharPreBattleDialoguePlayed", preBattleDialoguePlayed);
        tag.putBoolean("TharDefeatActive", defeatActive);
        tag.putInt("TharDefeatTicks", defeatTicks);
        tag.putBoolean("TharShielded", shielded);
        tag.putInt("TharPhase", phase);
        tag.putInt("TharPhaseTick", phaseTick);
        tag.putInt("TharTotalTick", totalTick);
        tag.putInt("TharAbilityTick", abilityTick);
        tag.putInt("TharSummonTick", summonTick);
        tag.putInt("TharCombatSecondTick", combatSecondTick);
        tag.putInt("TharAbilityLockTick", abilityLockTick);
        tag.putInt("TharH1", h1);
        tag.putInt("TharH3", h3);
        tag.putInt("TharH5", h5);
        tag.putInt("TharH6", h6);
        tag.putInt("TharH80", h80);
        tag.putInt("TharH81", h81);
        tag.putInt("TharRedVolleyCasts", redVolleyCasts);
        tag.putInt("TharEmptyPlayerTick", emptyPlayerTicks);
        tag.putInt("TharLaserTick", laserTick);
        tag.putInt("TharPushTick", pushTick);
        tag.putInt("TharFlameTick", flameTick);
        tag.putInt("TharFireFieldTick", fireFieldTick);
        tag.putInt("TharFireFieldDuration", fireFieldDuration);
        tag.putInt("TharBarrageTick", barrageTick);
        tag.putInt("TharMusicTick", musicTick);
        tag.putInt("TharDisplay", displayMode());
        tag.putFloat("TharVisualYaw", visualYaw());
        if (flameTarget != null) tag.putUUID("TharFlameTarget", flameTarget);
        tag.putDouble("TharFlameOriginX", flameOrigin.x);
        tag.putDouble("TharFlameOriginY", flameOrigin.y);
        tag.putDouble("TharFlameOriginZ", flameOrigin.z);
        tag.putDouble("TharFlameDirectionX", flameDirection.x);
        tag.putDouble("TharFlameDirectionY", flameDirection.y);
        tag.putDouble("TharFlameDirectionZ", flameDirection.z);
        tag.putDouble("TharSafeX", safeX);
        tag.putDouble("TharSafeZ", safeZ);
        tag.putDouble("TharAnchorX", anchorX);
        tag.putDouble("TharAnchorY", anchorY);
        tag.putDouble("TharAnchorZ", anchorZ);
        ListTag blocks = new ListTag();
        for (Map.Entry<BlockPos, BlockState> entry : alteredBlocks.entrySet()) {
            CompoundTag block = new CompoundTag();
            block.putLong("Pos", entry.getKey().asLong());
            block.put("State", NbtUtils.writeBlockState(entry.getValue()));
            blocks.add(block);
        }
        tag.put("TharAlteredBlocks", blocks);
        ListTag lines = new ListTag();
        for (ScheduledLine line : dialogue) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Due", line.dueTick);
            entry.putString("Key", line.key);
            lines.add(entry);
        }
        tag.put("TharDialogue", lines);
        ListTag laserLevels = new ListTag();
        for (Map.Entry<UUID, Integer> entry : laserDamageLevels.entrySet()) {
            CompoundTag level = new CompoundTag();
            level.putUUID("Player", entry.getKey());
            level.putInt("Level", entry.getValue());
            laserLevels.add(level);
        }
        tag.put("TharLaserDamageLevels", laserLevels);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        initialized = tag.getBoolean("TharInitialized");
        completionHandled = tag.getBoolean("TharComplete");
        preBattleDialoguePlayed = tag.getBoolean("TharPreBattleDialoguePlayed");
        defeatActive = tag.getBoolean("TharDefeatActive");
        defeatTicks = tag.getInt("TharDefeatTicks");
        shielded = tag.getBoolean("TharShielded");
        phase = tag.getInt("TharPhase");
        phaseTick = tag.getInt("TharPhaseTick");
        totalTick = tag.getInt("TharTotalTick");
        abilityTick = tag.getInt("TharAbilityTick");
        summonTick = tag.getInt("TharSummonTick");
        combatSecondTick = tag.getInt("TharCombatSecondTick");
        abilityLockTick = tag.getInt("TharAbilityLockTick");
        h1 = tag.getInt("TharH1");
        h3 = tag.getInt("TharH3");
        h5 = tag.getInt("TharH5");
        h6 = tag.getInt("TharH6");
        h80 = tag.getInt("TharH80");
        h81 = tag.getInt("TharH81");
        redVolleyCasts = tag.getInt("TharRedVolleyCasts");
        emptyPlayerTicks = tag.getInt("TharEmptyPlayerTick");
        // Active attacks are intentionally cancelled on reload: their moving samples are
        // transient, while the phase timers and cumulative laser escalation are persisted.
        laserTick = -1;
        pushTick = -1;
        flameTick = -1;
        fireFieldTick = -1;
        blackfireTick = -1;
        delayedHomingTick = -1;
        delayedRapidFlameTick = -1;
        fireFieldDuration = tag.getInt("TharFireFieldDuration");
        barrageTick = -1;
        musicTick = tag.contains("TharMusicTick") ? tag.getInt("TharMusicTick") : -1;
        needsMusicRecovery = musicTick >= 0;
        int savedDisplay = tag.getInt("TharDisplay");
        if ((savedDisplay & 0xFF) >= 3 && (savedDisplay & 0xFF) <= 5) savedDisplay = 0;
        entityData.set(DISPLAY_MODE, savedDisplay);
        entityData.set(VISUAL_YAW, tag.contains("TharVisualYaw") ? tag.getFloat("TharVisualYaw") : 180.0F);
        flameTarget = tag.hasUUID("TharFlameTarget") ? tag.getUUID("TharFlameTarget") : null;
        flameOrigin = new Vec3(tag.getDouble("TharFlameOriginX"), tag.getDouble("TharFlameOriginY"),
                tag.getDouble("TharFlameOriginZ"));
        flameDirection = new Vec3(tag.getDouble("TharFlameDirectionX"), tag.getDouble("TharFlameDirectionY"),
                tag.getDouble("TharFlameDirectionZ"));
        safeX = tag.getDouble("TharSafeX");
        safeZ = tag.getDouble("TharSafeZ");
        anchorX = tag.getDouble("TharAnchorX");
        anchorY = tag.getDouble("TharAnchorY");
        anchorZ = tag.getDouble("TharAnchorZ");
        alteredBlocks.clear();
        if (level() instanceof ServerLevel server) {
            ListTag blocks = tag.getList("TharAlteredBlocks", 10);
            for (int i = 0; i < blocks.size(); i++) {
                CompoundTag block = blocks.getCompound(i);
                alteredBlocks.put(BlockPos.of(block.getLong("Pos")),
                        NbtUtils.readBlockState(server.holderLookup(net.minecraft.core.registries.Registries.BLOCK),
                                block.getCompound("State")));
            }
        }
        dialogue.clear();
        ListTag lines = tag.getList("TharDialogue", 10);
        for (int i = 0; i < lines.size(); i++) {
            CompoundTag entry = lines.getCompound(i);
            dialogue.add(new ScheduledLine(entry.getInt("Due"), entry.getString("Key")));
        }
        laserHits.clear();
        laserDamageLevels.clear();
        ListTag laserLevels = tag.getList("TharLaserDamageLevels", 10);
        for (int i = 0; i < laserLevels.size(); i++) {
            CompoundTag level = laserLevels.getCompound(i);
            if (level.hasUUID("Player")) {
                laserDamageLevels.put(level.getUUID("Player"), level.getInt("Level"));
            }
        }
        flameHits.clear();
        barrageHitCooldowns.clear();
        shadowOrbs.clear();
        setInvulnerable(shielded || phase == WAITING || phase == INTRO || phase == COUNTDOWN
                || phase == PRE_BATTLE || phase == INTERMISSION_ONE || phase == INTERMISSION_TWO
                || phase == FINALE || phase == COMPLETE);
        if (level() instanceof ServerLevel server && !alteredBlocks.isEmpty()
                && phase != PHASE_ONE && phase != PHASE_TWO && phase != PHASE_THREE
                && flameTick < 0 && fireFieldTick < 0) restoreArena(server);
        bossEvent.setVisible(phase == PHASE_ONE || phase == PHASE_TWO || phase == PHASE_THREE);
    }

    private record ScheduledLine(int dueTick, String key) {
    }

    private static final class ShadowOrb {
        private Vec3 position;
        private Vec3 velocity;
        private final UUID target;
        private final boolean homing;
        private final boolean red;
        private int age;

        private ShadowOrb(Vec3 position, Vec3 velocity, @Nullable UUID target, boolean homing, boolean red) {
            this.position = position;
            this.velocity = velocity;
            this.target = target;
            this.homing = homing;
            this.red = red;
        }
    }

    private static final class FlameChargeState {
        private final UUID target;
        private final int launchTick;
        private final Set<UUID> hits = new HashSet<>();
        private Vec3 position = Vec3.ZERO;
        private Vec3 direction = Vec3.ZERO;
        private int tick;

        private FlameChargeState(UUID target, int launchTick) {
            this.target = target;
            this.launchTick = launchTick;
        }
    }
}
