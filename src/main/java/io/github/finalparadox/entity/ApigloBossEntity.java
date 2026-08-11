package io.github.finalparadox.entity;

import io.github.finalparadox.arena.ArenaDefinitions;
import io.github.finalparadox.arena.ArenaDeploymentData;
import io.github.finalparadox.arena.ArenaEntranceMemory;
import io.github.finalparadox.arena.ArenaFightParticipants;
import io.github.finalparadox.arena.B1ArenaLifecycle;
import io.github.finalparadox.arena.B1ArenaStaging;
import io.github.finalparadox.registry.ModEntities;
import io.github.finalparadox.registry.ModItems;
import io.github.finalparadox.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
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
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ApigloBossEntity extends Zombie {
    private static final int INTRO = 0;
    private static final int PHASE_ONE = 1;
    private static final int INTERMISSION_ONE = 2;
    private static final int PHASE_TWO = 3;
    private static final int INTERMISSION_TWO = 4;
    private static final int PHASE_THREE = 5;
    private static final int FINALE = 6;
    private static final int COUNTDOWN = 7;
    private static final int WAITING = 8;
    private static final int PRE_BATTLE = 9;
    private static final int[] INTRO_TICKS = {0, 60, 124, 220, 306, 388, 486, 556, 642, 702, 784, 918, 978, 1038};
    private static final boolean[] INTRO_GLAIVORUS = {false, false, false, false, false, false, false, true, true, true, true, false, false, false};
    private static final int INTRO_END_TICK = 1124;
    private static final int MUSIC_INTRO_TICKS = 62;
    private static final int MUSIC_LOOP_TICKS = 2230;
    private static final int MUSIC_ENTRANCE_LOOP_TICKS = 2080;
    private static final int MUSIC_NONE = 0;
    private static final int MUSIC_ENTRANCE = 1;
    private static final int MUSIC_BATTLE = 2;
    private static final int DEFEAT_DIALOGUE_TICK = 3 * 20;
    private static final int DEFEAT_RESPAWN_TICK = 6 * 20;
    private static final int[] BUTCHERING_TICKS = {80,120,160,200,240,270,300,330,360,390,410,430,450,470,490,505,520,535,550,565,573,581,589,597,605,613,621,629,637};
    private static final double STAMPEDE_RING_RADIUS = 19.0D;
    private static final double STAMPEDE_CHARGE_STEP = STAMPEDE_RING_RADIUS * 2.0D / 30.0D;
    private static final String OWNER_KEY = "finalparadox.apiglo_owner";
    private static final String SERVANT_KEY = "finalparadox.apiglo_servant";
    private static final String TRANSIENT_KEY = "finalparadox.apiglo_transient";
    private static final DustParticleOptions PURPLE_DUST = new DustParticleOptions(new Vector3f(0.557F, 0.345F, 0.655F), 2.0F);
    private static final DustParticleOptions PINK_DUST = new DustParticleOptions(new Vector3f(0.745F, 0.494F, 0.710F), 1.5F);
    private static final DustParticleOptions RED_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F);
    private static final String APIGLO_HEAD_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjE3MmVhOWYwZjM1ZTVjZTk5NGJlODM2MDZmOGQ0NDIwNDUyODM2YTc2NDY3YzFhODFjNmQ5YmY0ZGE3OGRiYSJ9fX0=";
    private static final String SERVANT_HEAD_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmMwMjUzODhlOWIwYmZmYTQyM2FjNzk3ZDFlNzg4OWUzM2QzYjMwODUyY2VjMmUwZWQ4YzZlYzVjY2IwN2RiNCJ9fX0=";

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.finalparadox.apiglo.bossbar"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
    private final ServerBossEvent tormentEvent = new ServerBossEvent(
            Component.translatable("entity.finalparadox.apiglo.torment"), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
    private final List<ScheduledLine> dialogue = new ArrayList<>();
    private final List<PortalSpawn> portals = new ArrayList<>();
    private final Set<UUID> entranceViewers = new HashSet<>();
    private int phase = INTRO;
    private int phaseTick;
    private int totalTick;
    private int secondTick;
    private int h2 = -12;
    private int h4;
    private int h5;
    private int h6 = 16;
    private int butcheringTick = -1;
    private int tormentFailures;
    private int stampedeCasts;
    private int emptyPlayerTicks;
    private int musicTick = -1;
    private int musicMode = MUSIC_NONE;
    private int pleadTick = -1;
    private boolean initialized;
    private boolean phaseOneButchering;
    private boolean phaseTwoButchering;
    private boolean needsReloadRecovery;
    private boolean victoryHandled;
    private boolean preBattleDialoguePlayed;
    private boolean defeatActive;
    private int defeatTicks;
    private double anchorX;
    private double anchorY;
    private double anchorZ;
    @Nullable private StampedeState stampede;
    @Nullable private FallingSwordState fallingSwords;
    @Nullable private TormentState torment;

    public ApigloBossEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        xpReward = 100;
        bossEvent.setVisible(false);
        tormentEvent.setVisible(false);
    }

    @Nullable
    public static ApigloBossEntity createPrepared(ServerLevel level, BlockPos position) {
        ApigloBossEntity boss = ModEntities.APIGLO.get().create(level);
        if (boss != null) {
            boss.moveTo(position.getX(), position.getY(), position.getZ(), 90.0F, 0.0F);
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

    /**
     * Keeps loaded legacy duplicates from ticking AI or abilities. The saved
     * UUID remains authoritative while unresolved; a running boss may replace
     * a loaded waiting owner, but never an unloaded owner.
     */
    private boolean enforceArenaAuthority(ServerLevel level) {
        ArenaDeploymentData data = ArenaDeploymentData.get(level, ArenaDefinitions.B1);
        if (data.state() != ArenaDeploymentData.DeploymentState.READY
                || !ArenaDefinitions.B1.id().equals(data.arenaId())
                || data.floorAnchor().isEmpty()) {
            return true;
        }
        BlockPos expected = ArenaDefinitions.B1.bossSpawnBlock(data.floorAnchor().orElseThrow());
        if (!matchesAnchor(expected)) return true;

        Optional<UUID> saved = data.activeBossUuid();
        Entity resolved = saved.map(level::getEntity).orElse(null);
        boolean savedResolved = saved.isPresent() && resolved != null;
        boolean savedValid = resolved instanceof ApigloBossEntity boss
                && boss.isAlive()
                && boss.matchesAnchor(expected);
        boolean savedWaiting = savedValid && ((ApigloBossEntity) resolved).isWaiting();
        B1ArenaLifecycle.AuthorityDecision decision = B1ArenaLifecycle.authorityDecision(
                getUUID(), isWaiting(), saved, savedResolved, savedValid, savedWaiting);
        return switch (decision) {
            case KEEP -> true;
            case CLAIM -> {
                data.setActiveBossUuid(getUUID());
                yield true;
            }
            case REPLACE_WAITING -> {
                ((ApigloBossEntity) resolved).discard();
                data.setActiveBossUuid(getUUID());
                yield true;
            }
            case DISCARD_SELF -> {
                discard();
                yield false;
            }
        };
    }

    public boolean startPreBattleDialogue() {
        if (phase != WAITING || preBattleDialoguePlayed) return false;
        preBattleDialoguePlayed = true;
        if (!(level() instanceof ServerLevel server)) return true;
        entranceViewers.clear();
        for (ServerPlayer player : server.getServer().getPlayerList().getPlayers()) {
            if (player.level() == server && !player.isSpectator()
                    && player.distanceToSqr(anchorX, anchorY, anchorZ) <= 96.0D * 96.0D
                    && ArenaEntranceMemory.markIfFirst(player, "b1")) {
                entranceViewers.add(player.getUUID());
            }
        }
        if (entranceViewers.isEmpty()) return true;
        phase = PRE_BATTLE;
        phaseTick = -1;
        if (musicTick < 0) startEntranceMusic(server);
        return true;
    }

    public boolean preBattleDialoguePlayed() {
        return preBattleDialoguePlayed;
    }

    public void markPreBattleDialoguePlayed() {
        preBattleDialoguePlayed = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 900.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 60.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
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
        phaseTick = 0;
        anchorX = getX();
        anchorY = getY();
        anchorZ = getZ();
        setPersistenceRequired();
        addTag("boss");
        setCanPickUpLoot(false);
        setCustomName(Component.translatable("entity.finalparadox.apiglo"));
        setCustomNameVisible(true);
        equipBoss();
        setHealth(900.0F);
        setNoAi(true);
        setInvulnerable(true);
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 0, false, false));
        moveTo(anchorX + 16.5D, groundY(anchorX + 16.5D, anchorZ), anchorZ, 90.0F, 0.0F);
    }

    private void equipBoss() {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.APIGLO_BLADE.get()));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
        setItemSlot(EquipmentSlot.HEAD, skull(APIGLO_HEAD_TEXTURE, new int[]{-1799677634,1587104164,-1678250160,-84111072}));
        for (EquipmentSlot slot : EquipmentSlot.values()) setDropChance(slot, 0.0F);
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        // B1 uses the proximity-triggered pre-battle dialogue and the Echo of
        // Koros to start the encounter, so the boss no longer opens a menu on use.
        return InteractionResult.CONSUME;
    }

    public int handleGuideAction(ServerPlayer player, String action) {
        if (phase != WAITING) {
            player.sendSystemMessage(Component.translatable("message.finalparadox.apiglo.interaction.already_started"));
            return 0;
        }
        if (player.level() != level() || !player.isAlive()) {
            player.sendSystemMessage(Component.translatable("message.finalparadox.apiglo.interaction.too_far"));
            return 0;
        }
        switch (action) {
            case "main" -> showGuideMain(player);
            case "briefing" -> showGuideBriefing(player);
            case "mechanics" -> showGuideMechanics(player);
            case "confirm" -> showGuideConfirmation(player);
            case "start" -> {
                if (!allPlayersInsideArena()) {
                    showGuidePlayersMissing(player);
                    return 0;
                }
                return B1ArenaStaging.beginEncounter(player) ? 1 : 0;
            }
            default -> {
                return 0;
            }
        }
        return 1;
    }

    public boolean beginEncounter() {
        if (phase != WAITING) return false;
        if (!(level() instanceof ServerLevel server)) return false;
        totalTick = 0;
        secondTick = 0;
        dialogue.clear();
        playGlobal(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.2F);
        startBattleMusic(server);
        // The original encounter starts with the Apiglo/Glaivorus exchange, then the countdown.
        // When the pre-battle exchange already played on arena entry, skip straight to the countdown.
        phase = preBattleDialoguePlayed ? COUNTDOWN : INTRO;
        phaseTick = -1;
        return true;
    }

    private boolean allPlayersInsideArena() {
        if (!(level() instanceof ServerLevel server)) return false;
        for (ServerPlayer player : server.getServer().getPlayerList().getPlayers()) {
            if (player.level() != level()
                    || player.getX() < anchorX - 22.0D || player.getX() > anchorX + 22.0D
                    || player.getY() < anchorY - 1.0D || player.getY() > anchorY + 10.0D
                    || player.getZ() < anchorZ - 21.0D || player.getZ() > anchorZ + 21.0D) return false;
        }
        return true;
    }

    private void showGuideMain(ServerPlayer player) {
        guideHeader(player, "message.finalparadox.apiglo.guide.main");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(guideButton("message.finalparadox.apiglo.guide.option.help", "briefing"));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(guideButton("message.finalparadox.apiglo.guide.option.start", "confirm"));
        guideFooter(player);
    }

    private void showGuideBriefing(ServerPlayer player) {
        guideHeader(player, "message.finalparadox.apiglo.guide.briefing");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(guideButton("message.finalparadox.apiglo.guide.continue", "mechanics"));
        player.sendSystemMessage(guideButton("message.finalparadox.apiglo.guide.back", "main"));
        playGuideSound(player);
    }

    private void showGuideMechanics(ServerPlayer player) {
        guideHeader(player, "message.finalparadox.apiglo.guide.mechanics.intro");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(guideHover("fall"));
        player.sendSystemMessage(guideHover("butcher"));
        player.sendSystemMessage(guideHover("torment"));
        player.sendSystemMessage(guideHover("servants"));
        player.sendSystemMessage(guideHover("despair"));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.translatable("message.finalparadox.apiglo.guide.interphase"));
        player.sendSystemMessage(guideHover("stampede"));
        player.sendSystemMessage(guideHover("elite"));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(guideButton("message.finalparadox.apiglo.guide.back", "main"));
        playGuideSound(player);
    }

    private void showGuideConfirmation(ServerPlayer player) {
        guideHeader(player, "message.finalparadox.apiglo.guide.confirm");
        player.sendSystemMessage(Component.empty());
        Component yes = guideButton("message.finalparadox.apiglo.guide.yes", "start");
        Component no = guideButton("message.finalparadox.apiglo.guide.no", "main");
        player.sendSystemMessage(Component.empty()
                .append(Component.translatable("message.finalparadox.apiglo.guide.choice.prefix"))
                .append(yes)
                .append(Component.translatable("message.finalparadox.apiglo.guide.choice.gap"))
                .append(no));
        player.sendSystemMessage(Component.empty());
        playGuideSound(player);
    }

    private void showGuidePlayersMissing(ServerPlayer player) {
        guideHeader(player, "message.finalparadox.apiglo.guide.players_missing");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(guideButton("message.finalparadox.apiglo.guide.back", "main"));
        player.sendSystemMessage(Component.empty());
        player.level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.MASTER, 1.0F, 2.0F);
    }

    private void guideHeader(ServerPlayer player, String bodyKey) {
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.empty()
                .append(Component.translatable("message.finalparadox.apiglo.guide.speaker").withStyle(
                        Style.EMPTY.withColor(TextColor.fromRgb(0xFBBDFF)).withBold(true).withItalic(true)))
                .append(Component.translatable(bodyKey)));
    }

    private void guideFooter(ServerPlayer player) {
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.translatable("message.finalparadox.apiglo.guide.instruction"));
        playGuideSound(player);
    }

    private Component guideButton(String key, String action) {
        return Component.translatable(key).setStyle(Style.EMPTY.withClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND, "/finalparadox apiglo_menu " + getUUID() + " " + action)));
    }

    private static Component guideHover(String ability) {
        Style style = Style.EMPTY;
        if (ability.equals("butcher")) style = style.withColor(TextColor.fromRgb(0xFF8500));
        return Component.translatable("message.finalparadox.apiglo.guide.ability." + ability).setStyle(
                style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("message.finalparadox.apiglo.guide.ability." + ability + ".detail"))));
    }

    private void playGuideSound(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.GRASS_BREAK, SoundSource.MASTER, 0.5F, 2.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) return;
        if (isRemoved() || isDeadOrDying()) return;
        // Waiting and intermission phases deliberately disable vanilla AI. Encounter scripting must keep ticking.
        if (!initialized) initializeEncounter();
        if (!enforceArenaAuthority(level)) return;
        if (needsReloadRecovery) recoverAfterReload(level);
        bossEvent.setName(Component.translatable("entity.finalparadox.apiglo.bossbar"));
        bossEvent.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0.0F, 1.0F));
        if (phase == WAITING) {
            bossEvent.setVisible(false);
            return;
        }
        if (defeatActive) {
            tickDefeat(level);
            return;
        }
        totalTick++;
        processDialogue(level);
        tickMusic(level);
        tickHazards(level);

        if (phase == INTRO) tickIntro(level);
        else if (phase == PRE_BATTLE) tickPreBattleDialogue(level);
        else if (phase == COUNTDOWN) tickCountdown(level);
        else tickFight(level);
        phaseTick++;
    }

    private void tickIntro(ServerLevel level) {
        tickIntroLines(level);
        if (phaseTick >= INTRO_END_TICK) {
            phase = COUNTDOWN;
            phaseTick = -1;
        }
    }

    private void tickPreBattleDialogue(ServerLevel level) {
        tickIntroLines(level);
        if (phaseTick >= INTRO_END_TICK) {
            phase = WAITING;
            phaseTick = 0;
            entranceViewers.clear();
        }
    }

    private void tickIntroLines(ServerLevel level) {
        for (int index = 0; index < INTRO_TICKS.length; index++) {
            if (phaseTick != INTRO_TICKS[index]) continue;
            Component line = Component.translatable("dialogue.finalparadox.apiglo.intro." + (index + 1));
            if (phase == PRE_BATTLE) broadcastEntrance(line);
            else broadcast(line);
            playGlobal(INTRO_GLAIVORUS[index] ? SoundEvents.TRIDENT_THROW : SoundEvents.PIGLIN_ANGRY, 1.0F,
                    INTRO_GLAIVORUS[index] ? 1.5F : 1.4F);
        }
    }

    private void tickCountdown(ServerLevel level) {
        if (phaseTick == 60) {
            showTitle(Component.translatable("title.finalparadox.apiglo.countdown.3"), Component.empty());
            playGlobal(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0F, 1.5F);
        } else if (phaseTick == 80) {
            showTitle(Component.translatable("title.finalparadox.apiglo.countdown.2"), Component.empty());
            playGlobal(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0F, 1.5F);
        } else if (phaseTick == 100) {
            showTitle(Component.translatable("title.finalparadox.apiglo.countdown.1"), Component.empty());
            playGlobal(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0F, 1.5F);
        } else if (phaseTick >= 120) {
            startPhaseOne();
        }
    }

    private void startPhaseOne() {
        phase = PHASE_ONE;
        phaseTick = -1;
        secondTick = 0;
        h2 = -12;
        h6 = 16;
        setInvulnerable(false);
        setNoAi(false);
        bossEvent.setVisible(true);
        showTitle(Component.translatable("title.finalparadox.apiglo.phase.marker"),
                Component.translatable("title.finalparadox.apiglo.phase.1"));
        playGlobal(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 1.5F);
        playGlobal(SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 1.5F);
    }

    private void tickFight(ServerLevel level) {
        if (ArenaFightParticipants.allDefeated(level, ArenaDefinitions.B1)) {
            startDefeat(level);
            return;
        }
        List<ServerPlayer> players = combatPlayers();
        if (phase != INTRO && phase != COUNTDOWN && players.isEmpty()) {
            if (++emptyPlayerTicks >= 200) {
                startDefeat(level);
                return;
            }
        } else emptyPlayerTicks = 0;

        if (phase == PHASE_ONE) {
            if (!phaseOneButchering && getHealth() <= 702.0F) {
                phaseOneButchering = true;
                startLongButchering();
                scheduleSet(2, 0, 60, 100);
            }
            if (getHealth() <= 450.0F) startIntermissionOne();
            else tickAbilityCounters();
        } else if (phase == INTERMISSION_ONE) {
            tickIntermissionOne();
        } else if (phase == PHASE_TWO) {
            if (!phaseTwoButchering && getHealth() <= 468.0F) {
                phaseTwoButchering = true;
                startLongButchering();
                scheduleSet(4, 0, 60, 100);
            }
            if (getHealth() <= 225.0F) startIntermissionTwo();
            else tickAbilityCounters();
        } else if (phase == INTERMISSION_TWO) {
            tickIntermissionTwo();
        } else if (phase == PHASE_THREE) {
            if (getHealth() <= 270.0F) startFinale();
            else tickAbilityCounters();
        } else if (phase == FINALE) {
            tickFinale();
        }
    }

    private void tickAbilityCounters() {
        if (++secondTick < 20) return;
        secondTick = 0;
        h2++;
        h6++;
        if (phase != PHASE_ONE) h4++;
        if (phase == PHASE_THREE) h5++;

        int h2Limit = phase == PHASE_THREE ? 11 : 9;
        int h4Limit = phase == PHASE_THREE ? 26 : 22;
        int h6Limit = phase == PHASE_ONE ? 30 : 33;
        if (h2 >= h2Limit) {
            h2 = 0;
            startFallingSwords();
        }
        if (phase != PHASE_ONE && h4 >= h4Limit) {
            h4 = 0;
            startTorment();
        }
        if (h6 >= h6Limit) {
            h6 = 0;
            startServantPortals();
        }
        if (phase == PHASE_THREE && h5 >= 5) {
            h5 = 0;
            spawnHostileGlaivorus();
        }
    }

    private void startIntermissionOne() {
        phase = INTERMISSION_ONE;
        phaseTick = -1;
        setNoAi(true);
        setInvulnerable(true);
        teleportToBalcony();
        scheduleSet(1, 40, 80, 140, 200);
        showTitle(Component.translatable("title.finalparadox.apiglo.phase.marker"),
                Component.translatable("title.finalparadox.apiglo.intermission.1"));
    }

    private void tickIntermissionOne() {
        if (phaseTick == 240 || phaseTick == 440 || phaseTick == 640 || phaseTick == 1060 || phaseTick == 1260) startStampede();
        if (phaseTick == 800) startReinforcementPortals();
        if (phaseTick == 1440) scheduleSet(3, 0, 60);
        if (phaseTick >= 1500) startPhaseTwo();
    }

    private void startPhaseTwo() {
        phase = PHASE_TWO;
        phaseTick = -1;
        secondTick = 0;
        h2 = -12;
        h4 = 12;
        h6 = 0;
        setHealth(675.0F);
        setNoAi(false);
        setInvulnerable(false);
        teleportToArenaEdge();
        showTitle(Component.translatable("title.finalparadox.apiglo.phase.marker"),
                Component.translatable("title.finalparadox.apiglo.phase.2"));
    }

    private void startIntermissionTwo() {
        phase = INTERMISSION_TWO;
        phaseTick = -1;
        setNoAi(true);
        setInvulnerable(true);
        teleportToBalcony();
        scheduleSet(5, 20, 80, 120, 160, 200);
        showTitle(Component.translatable("title.finalparadox.apiglo.phase.marker"),
                Component.translatable("title.finalparadox.apiglo.intermission.2"));
    }

    private void tickIntermissionTwo() {
        if (phaseTick == 240 || phaseTick == 440 || phaseTick == 980 || phaseTick == 1180) startStampede();
        if (phaseTick == 360 || phaseTick == 560 || phaseTick == 1100 || phaseTick == 1300) startFallingSwords();
        if (phaseTick == 640) startTorment();
        if (phaseTick == 720) startReinforcementPortals();
        if (phaseTick == 1420) scheduleSet(6, 0, 60, 120, 180);
        if (phaseTick >= 1500) startPhaseThree();
    }

    private void startPhaseThree() {
        phase = PHASE_THREE;
        phaseTick = -1;
        secondTick = 0;
        h2 = -12;
        h4 = -12;
        h5 = 0;
        h6 = -12;
        setHealth(540.0F);
        setNoAi(false);
        setInvulnerable(false);
        teleportToArenaEdge();
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, -1, 0, false, false));
        addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 0, false, false));
        showTitle(Component.translatable("title.finalparadox.apiglo.phase.marker"),
                Component.translatable("title.finalparadox.apiglo.phase.3"));
        schedulePortalAction(80, true);
        schedulePortalAction(80, false);
    }

    private void startFinale() {
        phase = FINALE;
        phaseTick = -1;
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 3, false, false));
        scheduleSet(7, 0, 60, 140, 220, 300);
    }

    private void tickFinale() {
        if (phaseTick == 220) {
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, -1, 10, false, false));
            addEffect(new MobEffectInstance(MobEffects.WEAKNESS, -1, 10, false, false));
        }
        if (pleadTick < 0 && getHealth() <= 144.0F) {
            pleadTick = 0;
            addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2, false, false));
            scheduleSet(8, 0, 60, 140, 220);
        }
        if (pleadTick >= 0) pleadTick++;
    }

    private void startLongButchering() {
        butcheringTick = 0;
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2, false, false));
        showTitle(Component.translatable("title.finalparadox.apiglo.ability.marker"),
                Component.translatable("title.finalparadox.apiglo.ability.butchering"));
    }

    private void tickHazards(ServerLevel level) {
        if (butcheringTick >= 0) {
            butcheringTick++;
            for (int tick : BUTCHERING_TICKS) if (butcheringTick == tick) spawnHostileGlaivorus();
            if (butcheringTick > BUTCHERING_TICKS[BUTCHERING_TICKS.length - 1]) butcheringTick = -1;
        }
        if (stampede != null && stampede.tick(level)) stampede = null;
        if (fallingSwords != null && fallingSwords.tick(level)) fallingSwords = null;
        if (torment != null && torment.tick(level)) torment = null;
        List<PortalSpawn> completedPortals = new ArrayList<>();
        for (PortalSpawn portal : List.copyOf(portals)) {
            if (portal.tick(level)) completedPortals.add(portal);
        }
        portals.removeAll(completedPortals);
    }

    private void startStampede() {
        if (stampede != null) stampede.removeAll();
        stampedeCasts++;
        stampede = new StampedeState();
        showTitle(Component.translatable("title.finalparadox.apiglo.ability.marker"),
                Component.translatable("title.finalparadox.apiglo.ability.stampede"));
        if (stampedeCasts == 3) broadcast(Component.translatable("message.finalparadox.apiglo.stampede.unmarked"));
    }

    private void startFallingSwords() {
        List<ServerPlayer> players = combatPlayers();
        if (players.isEmpty()) return;
        fallingSwords = new FallingSwordState(players.get(random.nextInt(players.size())));
    }

    private void startReinforcementPortals() {
        List<Vec3> positions = randomGridPositions(3 + Math.max(1, combatPlayers().size()), 4.0D);
        int melee = 1 + (combatPlayers().size() >= 2 ? 1 : 0) + (combatPlayers().size() >= 4 ? 1 : 0);
        for (int index = 0; index < positions.size(); index++) portals.add(new PortalSpawn(positions.get(index), index < melee ? 1 : 2, 0));
        playGlobal(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 0.7F);
    }

    private void startServantPortals() {
        int existing = (int)ownedEntities(Mob.class).stream()
                .filter(mob -> mob.getPersistentData().getBoolean(SERVANT_KEY)).count();
        int count = Math.min(12 - existing, 2 + Math.max(1, combatPlayers().size()));
        if (count <= 0) return;
        for (Vec3 position : randomGridPositions(count, 4.0D)) portals.add(new PortalSpawn(position, random.nextInt(3) == 0 ? 3 : 4, 0));
        playGlobal(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 0.7F);
    }

    private void schedulePortalAction(int delay, boolean reinforcement) {
        portals.add(new PortalSpawn(new Vec3(anchorX, anchorY, anchorZ), reinforcement ? 5 : 6, -delay));
    }

    private void startTorment() {
        if (torment != null) torment.removeAll();
        int count = Math.min(3, Math.max(1, combatPlayers().size()));
        torment = new TormentState(randomGridPositions(count, 6.0D));
        showTitle(Component.translatable("title.finalparadox.apiglo.ability.marker"),
                Component.translatable("title.finalparadox.apiglo.ability.torment"));
        broadcast(Component.translatable("message.finalparadox.apiglo.torment.warn"));
    }

    private void spawnHostileGlaivorus() {
        List<ServerPlayer> players = combatPlayers();
        if (players.isEmpty()) return;
        ServerPlayer target = players.get(random.nextInt(players.size()));
        List<Vec3> points = randomGridPositions(1, 0.0D);
        Vec3 point = points.isEmpty() ? target.position() : points.get(0);
        GlaivorusBladeEntity.spawnHostile(this, point.x, point.y, point.z, target);
    }

    private void scheduleSet(int set, int... delays) {
        for (int index = 0; index < delays.length; index++) {
            int sound = dialogueSound(set, index + 1);
            dialogue.add(new ScheduledLine(totalTick + delays[index], "dialogue.finalparadox.apiglo.set." + set + "." + (index + 1), sound));
        }
    }

    private static int dialogueSound(int set, int line) {
        if (set == 1) return line == 3 ? 1 : 0;
        if (set == 2 || set == 4) return line == 1 ? 0 : 1;
        if (set == 3) return 0;
        if (set == 5) return line == 2 || line == 4 ? 1 : 0;
        if (set == 6) return line == 4 ? 1 : 2;
        if (set == 7) return line == 2 || line == 4 ? 1 : 2;
        return line == 4 ? 3 : 2;
    }

    private void processDialogue(ServerLevel level) {
        for (Iterator<ScheduledLine> iterator = dialogue.iterator(); iterator.hasNext();) {
            ScheduledLine line = iterator.next();
            if (line.dueTick > totalTick) continue;
            broadcast(Component.translatable(line.key));
            if (line.sound == 0) playGlobal(SoundEvents.PIGLIN_ANGRY, 1.0F, 1.4F);
            else if (line.sound == 1) playGlobal(SoundEvents.TRIDENT_THROW, 1.0F, 1.5F);
            else if (line.sound == 2) playGlobal(SoundEvents.ZOMBIFIED_PIGLIN_HURT, 1.0F, 0.8F);
            else playGlobal(SoundEvents.TRIDENT_RETURN, 1.0F, 1.7F);
            iterator.remove();
        }
    }

    private void startEntranceMusic(ServerLevel level) {
        musicTick = 0;
        musicMode = MUSIC_ENTRANCE;
        playMusicTrack(level, ModSounds.APIGLO_ENTRANCE_INTRO.get());
    }

    private void startBattleMusic(ServerLevel level) {
        musicTick = 0;
        musicMode = MUSIC_BATTLE;
        playMusicTrack(level, ModSounds.APIGLO_INTRO.get());
    }

    private void tickMusic(ServerLevel level) {
        if (musicTick < 0) return;
        musicTick++;
        if (musicMode == MUSIC_ENTRANCE) {
            if (musicTick >= MUSIC_ENTRANCE_LOOP_TICKS
                    && (musicTick - MUSIC_ENTRANCE_LOOP_TICKS) % MUSIC_ENTRANCE_LOOP_TICKS == 0) {
                playMusicTrack(level, ModSounds.APIGLO_ENTRANCE_LOOP.get());
            }
        } else if (musicTick >= MUSIC_INTRO_TICKS
                && (musicTick - MUSIC_INTRO_TICKS) % MUSIC_LOOP_TICKS == 0) {
            playMusicTrack(level, ModSounds.APIGLO_LOOP.get());
        }
    }

    private void stopMusic() {
        stopMusicAudio();
        musicTick = -1;
        musicMode = MUSIC_NONE;
        needsReloadRecovery = false;
    }

    private void playMusicTrack(ServerLevel level, SoundEvent sound) {
        // A streamed sound is not tied to this entity's chunk lifetime. Clear any stale
        // instance before a deliberate start so reload recovery and loop boundaries are idempotent.
        stopMusicAudio();
        level.playSound(null, blockPosition(), sound, SoundSource.RECORDS, 4.0F, 1.0F);
    }

    private void stopMusicAudio() {
        if (level().getServer() == null) return;
        ResourceLocation intro = ModSounds.APIGLO_INTRO.get().getLocation();
        ResourceLocation loop = ModSounds.APIGLO_LOOP.get().getLocation();
        ResourceLocation entranceIntro = ModSounds.APIGLO_ENTRANCE_INTRO.get().getLocation();
        ResourceLocation entranceLoop = ModSounds.APIGLO_ENTRANCE_LOOP.get().getLocation();
        for (ServerPlayer player : level().getServer().getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundStopSoundPacket(intro, SoundSource.RECORDS));
            player.connection.send(new ClientboundStopSoundPacket(loop, SoundSource.RECORDS));
            player.connection.send(new ClientboundStopSoundPacket(entranceIntro, SoundSource.RECORDS));
            player.connection.send(new ClientboundStopSoundPacket(entranceLoop, SoundSource.RECORDS));
        }
    }

    private void teleportToBalcony() {
        teleportParticles();
        moveTo(anchorX + 22.0D, groundY(anchorX + 22.0D, anchorZ) + 16.0D, anchorZ, 90.0F, 30.0F);
        teleportParticles();
    }

    private void teleportToArenaEdge() {
        teleportParticles();
        moveTo(anchorX + 16.5D, groundY(anchorX + 16.5D, anchorZ), anchorZ, 90.0F, 0.0F);
        teleportParticles();
    }

    private void teleportParticles() {
        if (level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0D, getZ(), 40, 0, 0, 0, 0.2D);
            level.playSound(null, blockPosition(), SoundEvents.SHULKER_TELEPORT, SoundSource.MASTER, 3.0F, 0.8F);
        }
    }

    private List<ServerPlayer> combatPlayers() {
        if (!(level() instanceof ServerLevel server)) return List.of();
        return server.getPlayers(player -> !player.isSpectator() && player.isAlive()
                && player.distanceToSqr(anchorX, anchorY, anchorZ) <= 64.0D * 64.0D);
    }

    public static void onPlayerDeath(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel server)) return;
        ArenaDeploymentData data = ArenaDeploymentData.get(server, ArenaDefinitions.B1);
        if (!ArenaDefinitions.B1.id().equals(data.arenaId())) return;
        data.activeBossUuid().map(server::getEntity)
                .filter(ApigloBossEntity.class::isInstance)
                .map(ApigloBossEntity.class::cast)
                .filter(Entity::isAlive)
                .ifPresent(boss -> boss.handlePlayerDeath(player));
    }

    private void handlePlayerDeath(ServerPlayer player) {
        if (!(level() instanceof ServerLevel server) || defeatActive || victoryHandled) return;
        if (phase == WAITING || phase == PRE_BATTLE) return;
        if (!ArenaFightParticipants.markDefeated(
                server, ArenaDefinitions.B1, player.getUUID())) return;
        if (!player.isSpectator()) player.setGameMode(GameType.SPECTATOR);
        player.teleportTo(server, anchorX - 17.0D, anchorY + 10.0D, anchorZ,
                player.getYRot(), player.getXRot());
        if (ArenaFightParticipants.allDefeated(server, ArenaDefinitions.B1)) {
            startDefeat(server);
        }
    }

    private void startDefeat(ServerLevel server) {
        if (defeatActive || victoryHandled || phase == WAITING || phase == PRE_BATTLE) return;
        defeatActive = true;
        defeatTicks = 0;
        setInvulnerable(true);
        setNoAi(true);
        Component title = Component.translatable("luisb1202.functions.bossfight.b1.derrota.1");
        Component subtitle = Component.translatable("luisb1202.functions.bossfight.b1.derrota.2");
        for (ServerPlayer player : ArenaFightParticipants.onlinePlayers(server, ArenaDefinitions.B1)) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 10));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            server.playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH,
                    SoundSource.MASTER, 1.0F, 1.8F);
        }
    }

    private void tickDefeat(ServerLevel server) {
        defeatTicks++;
        if (defeatTicks == DEFEAT_DIALOGUE_TICK) {
            Component dialogue = Component.translatable(
                    "luisb1202.functions.bossfight.b1.dialogos.dia10.1");
            for (ServerPlayer player : ArenaFightParticipants.onlinePlayers(server, ArenaDefinitions.B1)) {
                player.sendSystemMessage(dialogue);
                server.playSound(null, player.blockPosition(), SoundEvents.PIGLIN_ANGRY,
                        SoundSource.MASTER, 1.0F, 1.4F);
            }
        }
        if (defeatTicks >= DEFEAT_RESPAWN_TICK) restartAfterDefeat(server);
    }

    private void restartAfterDefeat(ServerLevel server) {
        BlockPos bossAnchor = new BlockPos(
                Mth.floor(anchorX), Mth.floor(anchorY), Mth.floor(anchorZ));
        ArenaDeploymentData data = ArenaDeploymentData.get(server, ArenaDefinitions.B1);
        BlockPos floorAnchor = data.floorAnchor().orElse(bossAnchor.offset(0, -1, 0));
        BlockPos destination = floorAnchor.offset(ArenaDefinitions.B1_RESPAWN.offset());
        Set<UUID> participants = ArenaFightParticipants.participants(server, ArenaDefinitions.B1);
        for (UUID playerId : participants) {
            ServerPlayer player = server.getServer().getPlayerList().getPlayer(playerId);
            if (player == null) continue;
            if (player.isSpectator()) {
                player.teleportTo(server, destination.getX() + 0.5D,
                        destination.getY(), destination.getZ() + 0.5D,
                        ArenaDefinitions.B1_RESPAWN.yaw(), 0.0F);
                player.setGameMode(GameType.ADVENTURE);
            }
            player.removeEffect(MobEffects.WITHER);
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, 2020, 1, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.HEAL, 20, 10, true, false));
        }
        cleanupEncounter(server);
        stopMusic();
        data.clearActiveBoss();
        ArenaFightParticipants.clear(server, ArenaDefinitions.B1);
        discard();
        B1ArenaStaging.spawn(server, data, floorAnchor)
                .ifPresent(stage -> stage.boss().markPreBattleDialoguePlayed());
    }

    private List<Vec3> randomGridPositions(int count, double avoidPlayerRadius) {
        List<Vec3> candidates = new ArrayList<>();
        for (int x = -18; x <= 18; x += 6) {
            for (int z = -18; z <= 18; z += 6) {
                double px = anchorX + x;
                double pz = anchorZ + z;
                double py = groundY(px, pz);
                boolean blocked = combatPlayers().stream().anyMatch(player -> player.distanceToSqr(px, py, pz) <= avoidPlayerRadius * avoidPlayerRadius);
                if (!blocked) candidates.add(new Vec3(px, py, pz));
            }
        }
        java.util.Collections.shuffle(candidates, new java.util.Random(random.nextLong()));
        return new ArrayList<>(candidates.subList(0, Math.min(count, candidates.size())));
    }

    private double groundY(double x, double z) {
        if (!(level() instanceof ServerLevel server)) return anchorY;
        int start = Mth.clamp(Mth.floor(anchorY + 8.0D), server.getMinBuildHeight(), server.getMaxBuildHeight() - 2);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(x), start, Mth.floor(z));
        for (int y = start; y >= Math.max(server.getMinBuildHeight(), start - 24); y--) {
            pos.setY(y);
            if (!server.getBlockState(pos).getCollisionShape(server, pos).isEmpty()) return y + 1.0D;
        }
        return anchorY;
    }

    private void broadcast(Component message) {
        for (ServerPlayer player : level().getServer().getPlayerList().getPlayers()) {
            if (player.distanceToSqr(anchorX, anchorY, anchorZ) <= 96.0D * 96.0D) player.sendSystemMessage(message);
        }
    }

    private void broadcastEntrance(Component message) {
        for (ServerPlayer player : level().getServer().getPlayerList().getPlayers()) {
            if (entranceViewers.contains(player.getUUID())
                    && player.distanceToSqr(anchorX, anchorY, anchorZ) <= 96.0D * 96.0D) {
                player.sendSystemMessage(message);
            }
        }
    }

    private void showTitle(Component title, Component subtitle) {
        for (ServerPlayer player : level().getServer().getPlayerList().getPlayers()) {
            if (player.distanceToSqr(anchorX, anchorY, anchorZ) > 96.0D * 96.0D) continue;
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 10));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
        }
    }

    private void playGlobal(SoundEvent sound, float volume, float pitch) {
        if (level() instanceof ServerLevel server) server.playSound(null, blockPosition(), sound, SoundSource.MASTER, volume, pitch);
    }

    private void markOwned(Entity entity) {
        entity.getPersistentData().putUUID(OWNER_KEY, getUUID());
    }

    private boolean isOwned(Entity entity) {
        return entity.getPersistentData().hasUUID(OWNER_KEY) && entity.getPersistentData().getUUID(OWNER_KEY).equals(getUUID());
    }

    private <T extends Entity> List<T> ownedEntities(Class<T> type) {
        return level().getEntitiesOfClass(type, new AABB(anchorX - 80, anchorY - 32, anchorZ - 80,
                anchorX + 80, anchorY + 48, anchorZ + 80), this::isOwned);
    }

    private void cleanupEncounter(ServerLevel level) {
        for (Entity entity : level.getEntities(this, new AABB(anchorX - 96, anchorY - 48, anchorZ - 96,
                anchorX + 96, anchorY + 64, anchorZ + 96), this::isOwned)) entity.discard();
        dialogue.clear();
        portals.clear();
        stampede = null;
        fallingSwords = null;
        torment = null;
        tormentEvent.removeAllPlayers();
        tormentEvent.setVisible(false);
    }

    private void recoverAfterReload(ServerLevel level) {
        needsReloadRecovery = false;
        for (Entity entity : level.getEntities(this, new AABB(anchorX - 96, anchorY - 48, anchorZ - 96,
                anchorX + 96, anchorY + 64, anchorZ + 96), entity -> isOwned(entity)
                && entity.getPersistentData().getBoolean(TRANSIENT_KEY))) entity.discard();
        portals.clear();
        stampede = null;
        fallingSwords = null;
        torment = null;
        tormentEvent.setVisible(false);
        if (musicMode == MUSIC_ENTRANCE) {
            if (musicTick >= MUSIC_ENTRANCE_LOOP_TICKS) {
                musicTick = MUSIC_ENTRANCE_LOOP_TICKS;
                playMusicTrack(level, ModSounds.APIGLO_ENTRANCE_LOOP.get());
            } else if (musicTick >= 0) {
                musicTick = 0;
                playMusicTrack(level, ModSounds.APIGLO_ENTRANCE_INTRO.get());
            }
        } else if (musicTick >= MUSIC_INTRO_TICKS) {
            // Recovery starts the loop from its beginning, so its clock must restart too.
            // Keeping the old offset would schedule another copy partway through this one.
            musicTick = MUSIC_INTRO_TICKS;
            playMusicTrack(level, ModSounds.APIGLO_LOOP.get());
        } else if (musicTick >= 0) {
            musicTick = 0;
            playMusicTrack(level, ModSounds.APIGLO_INTRO.get());
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
        tormentEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
        tormentEvent.removePlayer(player);
    }

    @Override
    public void die(DamageSource source) {
        boolean serverSide = level() instanceof ServerLevel;
        if (B1ArenaLifecycle.shouldFinishVictory(serverSide, isVictoryConditionMet())
                && level() instanceof ServerLevel server) {
            finishVictory(server);
        }
        super.die(source);
    }

    /**
     * The source encounter declares victory as soon as its active boss disappears.
     * Waiting, intro, and countdown are invulnerable, so a combat kill is the only
     * normal way to satisfy this condition.
     */
    private boolean isVictoryConditionMet() {
        return phase != WAITING && !victoryHandled;
    }

    private void finishVictory(ServerLevel server) {
        victoryHandled = true;
        showTitle(Component.translatable("title.finalparadox.apiglo.victory"),
                Component.translatable("subtitle.finalparadox.apiglo.victory"));
        playGlobal(SoundEvents.PLAYER_LEVELUP, 1.0F, 0.8F);
        cleanupEncounter(server);
        ArenaDeploymentData.get(server, ArenaDefinitions.B1).clearActiveBoss();
        ArenaFightParticipants.clear(server, ArenaDefinitions.B1);
        stopMusic();
        bossEvent.setVisible(false);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return BuiltInLootTables.EMPTY;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        spawnAtLocation(createTrophyHead());
        spawnAtLocation(createSplinteredBlade());
    }

    private static ItemStack createTrophyHead() {
        ItemStack stack = skull(APIGLO_HEAD_TEXTURE, new int[]{1883308891,-1848883088,-1551411942,1691079525});
        stack.setHoverName(Component.translatable("item.finalparadox.apiglo_head").withStyle(
                Style.EMPTY.withColor(TextColor.fromRgb(0x987764)).withBold(true).withItalic(true)));
        setLore(stack, "", "item.finalparadox.apiglo_head.lore.1", "", "item.finalparadox.apiglo_head.lore.2", "");
        stack.getOrCreateTag().putInt("RepairCost", 999999);
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        stack.getOrCreateTag().putInt("HideFlags", 4);
        stack.getOrCreateTag().putBoolean("cabeza_apiglo", true);
        stack.getOrCreateTag().putBoolean("finalparadox_apiglo_trophy", true);
        return stack;
    }

    private static ItemStack createSplinteredBlade() {
        ItemStack stack = new ItemStack(Items.IRON_SWORD);
        stack.setHoverName(Component.translatable("item.finalparadox.splintered_glaivoran_blade").withStyle(
                Style.EMPTY.withColor(TextColor.fromRgb(0x75FFCD)).withBold(true).withItalic(true)));
        setLore(stack, "", "item.finalparadox.splintered_glaivoran_blade.lore.1",
                "item.finalparadox.splintered_glaivoran_blade.lore.2",
                "", "item.finalparadox.splintered_glaivoran_blade.lore.3", "");
        stack.getOrCreateTag().putInt("RepairCost", 999999);
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        ListTag enchantments = new ListTag();
        enchantments.add(new CompoundTag());
        stack.getOrCreateTag().put("Enchantments", enchantments);
        stack.getOrCreateTag().putInt("HideFlags", 5);
        stack.getOrCreateTag().putBoolean("forja", true);
        stack.getOrCreateTag().putBoolean("glaivorus_1", true);
        stack.getOrCreateTag().putBoolean("finalparadox_forge_material", true);
        return stack;
    }

    private static void setLore(ItemStack stack, String... keys) {
        CompoundTag display = stack.getOrCreateTagElement("display");
        ListTag lore = new ListTag();
        for (String key : keys) lore.add(StringTag.valueOf(Component.Serializer.toJson(
                key.isEmpty() ? Component.empty() : Component.translatable(key))));
        display.put("Lore", lore);
    }

    private static ItemStack skull(String texture, int[] id) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        CompoundTag owner = new CompoundTag();
        owner.putIntArray("Id", id);
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("ApigloInitialized", initialized);
        tag.putInt("ApigloPhase", phase);
        tag.putInt("ApigloPhaseTick", phaseTick);
        tag.putInt("ApigloTotalTick", totalTick);
        tag.putInt("ApigloSecondTick", secondTick);
        tag.putInt("ApigloH2", h2);
        tag.putInt("ApigloH4", h4);
        tag.putInt("ApigloH5", h5);
        tag.putInt("ApigloH6", h6);
        tag.putInt("ApigloButchering", butcheringTick);
        tag.putInt("ApigloTormentFailures", tormentFailures);
        tag.putInt("ApigloStampedeCasts", stampedeCasts);
        tag.putInt("ApigloEmptyPlayerTicks", emptyPlayerTicks);
        tag.putInt("ApigloMusic", musicTick);
        tag.putInt("ApigloMusicMode", musicMode);
        tag.putInt("ApigloPlead", pleadTick);
        tag.putBoolean("ApigloP1Butchering", phaseOneButchering);
        tag.putBoolean("ApigloP2Butchering", phaseTwoButchering);
        tag.putBoolean("ApigloVictoryHandled", victoryHandled);
        tag.putBoolean("ApigloPreBattleDialoguePlayed", preBattleDialoguePlayed);
        tag.putBoolean("ApigloDefeatActive", defeatActive);
        tag.putInt("ApigloDefeatTicks", defeatTicks);
        tag.putDouble("ApigloAnchorX", anchorX);
        tag.putDouble("ApigloAnchorY", anchorY);
        tag.putDouble("ApigloAnchorZ", anchorZ);
        ListTag lines = new ListTag();
        for (ScheduledLine line : dialogue) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Due", line.dueTick);
            entry.putString("Key", line.key);
            entry.putInt("Sound", line.sound);
            lines.add(entry);
        }
        tag.put("ApigloDialogue", lines);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        initialized = tag.getBoolean("ApigloInitialized");
        phase = tag.getInt("ApigloPhase");
        phaseTick = tag.getInt("ApigloPhaseTick");
        totalTick = tag.getInt("ApigloTotalTick");
        secondTick = tag.getInt("ApigloSecondTick");
        h2 = tag.getInt("ApigloH2");
        h4 = tag.getInt("ApigloH4");
        h5 = tag.getInt("ApigloH5");
        h6 = tag.getInt("ApigloH6");
        butcheringTick = tag.getInt("ApigloButchering");
        tormentFailures = tag.getInt("ApigloTormentFailures");
        stampedeCasts = tag.getInt("ApigloStampedeCasts");
        emptyPlayerTicks = tag.getInt("ApigloEmptyPlayerTicks");
        musicTick = tag.contains("ApigloMusic") ? tag.getInt("ApigloMusic") : -1;
        musicMode = tag.contains("ApigloMusicMode") ? tag.getInt("ApigloMusicMode") : MUSIC_NONE;
        pleadTick = tag.getInt("ApigloPlead");
        phaseOneButchering = tag.getBoolean("ApigloP1Butchering");
        phaseTwoButchering = tag.getBoolean("ApigloP2Butchering");
        victoryHandled = tag.getBoolean("ApigloVictoryHandled");
        preBattleDialoguePlayed = tag.getBoolean("ApigloPreBattleDialoguePlayed");
        defeatActive = tag.getBoolean("ApigloDefeatActive");
        defeatTicks = tag.getInt("ApigloDefeatTicks");
        anchorX = tag.getDouble("ApigloAnchorX");
        anchorY = tag.getDouble("ApigloAnchorY");
        anchorZ = tag.getDouble("ApigloAnchorZ");
        dialogue.clear();
        ListTag lines = tag.getList("ApigloDialogue", 10);
        for (int index = 0; index < lines.size(); index++) {
            CompoundTag entry = lines.getCompound(index);
            dialogue.add(new ScheduledLine(entry.getInt("Due"), entry.getString("Key"), entry.getInt("Sound")));
        }
        bossEvent.setVisible(phase != INTRO && phase != COUNTDOWN && phase != WAITING && phase != PRE_BATTLE);
        // Data commands used by legacy map abilities also call Entity#load while this
        // entity is already in the world. Only a real chunk/world load should restart
        // the streamed encounter music.
        needsReloadRecovery = !isAddedToWorld() && initialized && phase != WAITING && musicTick >= 0;
    }

    private record ScheduledLine(int dueTick, String key, int sound) {
    }

    private final class StampedeState {
        private final double safeX;
        private final double safeZ;
        private final List<StampedeUnit> units = new ArrayList<>();
        private final Set<UUID> struck = new HashSet<>();
        private int tick;

        private StampedeState() {
            safeX = anchorX + (-12 + random.nextInt(13) * 2);
            safeZ = anchorZ + (-12 + random.nextInt(13) * 2);
            if (!(level() instanceof ServerLevel server)) return;
            for (int offset = -18; offset <= 18; offset += 2) {
                if (Math.abs(anchorX + offset - safeX) > 3.25D) {
                    addHoglin(server, anchorX + offset, anchorZ - STAMPEDE_RING_RADIUS, 0, 1);
                    addHoglin(server, anchorX + offset, anchorZ + STAMPEDE_RING_RADIUS, 0, -1);
                }
                if (Math.abs(anchorZ + offset - safeZ) > 3.25D) {
                    addHoglin(server, anchorX - STAMPEDE_RING_RADIUS, anchorZ + offset, 1, 0);
                    addHoglin(server, anchorX + STAMPEDE_RING_RADIUS, anchorZ + offset, -1, 0);
                }
            }
        }

        private void addHoglin(ServerLevel server, double x, double z, double dx, double dz) {
            Hoglin hoglin = EntityType.HOGLIN.create(server);
            if (hoglin == null) return;
            hoglin.moveTo(x, groundY(x, z) + 4.0D, z, (float)Math.toDegrees(Math.atan2(-dx, dz)), 0.0F);
            hoglin.setNoAi(true);
            hoglin.setSilent(true);
            hoglin.setInvulnerable(true);
            hoglin.setPersistenceRequired();
            markOwned(hoglin);
            hoglin.getPersistentData().putBoolean(TRANSIENT_KEY, true);
            hoglin.getPersistentData().putDouble("ApigloStampedeDX", dx);
            hoglin.getPersistentData().putDouble("ApigloStampedeDZ", dz);
            server.addFreshEntity(hoglin);
            units.add(new StampedeUnit(hoglin.getUUID(), dx, dz));
        }

        private boolean tick(ServerLevel server) {
            tick++;
            if (units.isEmpty()) {
                for (Hoglin hoglin : ownedEntities(Hoglin.class)) units.add(new StampedeUnit(hoglin.getUUID(),
                        hoglin.getPersistentData().getDouble("ApigloStampedeDX"), hoglin.getPersistentData().getDouble("ApigloStampedeDZ")));
            }
            if (tick <= 20) {
                for (StampedeUnit unit : units) {
                    Entity entity = server.getEntity(unit.id);
                    if (entity != null) entity.setPos(entity.getX(), entity.getY() - 0.2D, entity.getZ());
                }
            }
            if (stampedeCasts <= 2 && tick <= 120 && tick % 5 == 0) markSafeArea(server);
            if (tick == 60 || tick == 85 || tick == 110) {
                int value = tick == 60 ? 3 : tick == 85 ? 2 : 1;
                showTitle(Component.translatable("title.finalparadox.apiglo.stampede.countdown", value), Component.empty());
                playGlobal(SoundEvents.UI_BUTTON_CLICK.value(), 0.3F, 2.0F);
            }
            if (tick >= 130 && tick < 160) {
                for (StampedeUnit unit : units) {
                    Entity entity = server.getEntity(unit.id);
                    if (entity == null) continue;
                    entity.setPos(entity.getX() + unit.dx * STAMPEDE_CHARGE_STEP, entity.getY(), entity.getZ() + unit.dz * STAMPEDE_CHARGE_STEP);
                    server.sendParticles(ParticleTypes.SPIT, entity.getX(), entity.getY() + 1.0D, entity.getZ(), 1, 0, 1, 0, 0.2D);
                    for (ServerPlayer player : combatPlayers()) {
                        if (struck.contains(player.getUUID()) || Math.abs(player.getX() - safeX) <= 3.25D && Math.abs(player.getZ() - safeZ) <= 3.25D) continue;
                        if (player.distanceToSqr(entity) > 4.0D) continue;
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 0));
                        player.hurt(damageSources().mobAttack(ApigloBossEntity.this), 15.0F);
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2));
                        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 1, 0, 0, 0, 0);
                        struck.add(player.getUUID());
                    }
                }
            }
            if (tick >= 160) {
                server.sendParticles(ParticleTypes.EXPLOSION, safeX, anchorY + 0.7D, safeZ, 1, 0, 0, 0, 0);
                playGlobal(SoundEvents.GENERIC_EXPLODE, 1.0F, 0.6F);
                removeAll();
                return true;
            }
            return false;
        }

        private void markSafeArea(ServerLevel server) {
            for (double offset = -3.0D; offset <= 3.0D; offset += 1.0D) {
                double y1 = groundY(safeX + offset, safeZ - 3.0D) + 0.15D;
                double y2 = groundY(safeX + offset, safeZ + 3.0D) + 0.15D;
                server.sendParticles(ParticleTypes.END_ROD, safeX + offset, y1, safeZ - 3.0D, 1, 0, 0, 0, 0);
                server.sendParticles(ParticleTypes.END_ROD, safeX + offset, y2, safeZ + 3.0D, 1, 0, 0, 0, 0);
                double y3 = groundY(safeX - 3.0D, safeZ + offset) + 0.15D;
                double y4 = groundY(safeX + 3.0D, safeZ + offset) + 0.15D;
                server.sendParticles(ParticleTypes.END_ROD, safeX - 3.0D, y3, safeZ + offset, 1, 0, 0, 0, 0);
                server.sendParticles(ParticleTypes.END_ROD, safeX + 3.0D, y4, safeZ + offset, 1, 0, 0, 0, 0);
            }
        }

        private void removeAll() {
            if (!(level() instanceof ServerLevel server)) return;
            for (StampedeUnit unit : units) {
                Entity entity = server.getEntity(unit.id);
                if (entity != null) entity.discard();
            }
        }
    }

    private record StampedeUnit(UUID id, double dx, double dz) {
    }

    private final class FallingSwordState {
        private final List<Vec3> markers = new ArrayList<>();
        private final List<UUID> stands = new ArrayList<>();
        private final List<Vec3> impacts;
        private final List<PendingSwordImpact> pendingImpacts = new ArrayList<>();
        private final Set<UUID> struck = new HashSet<>();
        private int nextImpactIndex;
        private int tick;

        private FallingSwordState(ServerPlayer target) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            Vec3 origin = new Vec3(target.getX() + Math.cos(angle) * 12.0D, target.getY(), target.getZ() + Math.sin(angle) * 12.0D);
            Vec3 forward = target.position().subtract(origin).multiply(1, 0, 1).normalize();
            Vec3 side = new Vec3(-forward.z, 0, forward.x);
            for (int step = 2; step <= 6; step++) {
                for (int lateral = -4; lateral <= 4; lateral += 2) {
                    Vec3 p = origin.add(forward.scale(step * 4.0D)).add(side.scale(lateral));
                    markers.add(new Vec3(p.x, groundY(p.x, p.z), p.z));
                }
            }
            Vec3 finalCore = origin.add(forward.scale(24.0D));
            impacts = new ArrayList<>(markers);
            impacts.sort(Comparator.comparingDouble((Vec3 point) -> point.distanceToSqr(finalCore)).reversed());
            java.util.Collections.shuffle(markers, new java.util.Random(random.nextLong()));
            spawnStands();
            playGlobal(SoundEvents.VEX_CHARGE, 2.0F, 1.0F);
            playGlobal(SoundEvents.BAT_TAKEOFF, 2.0F, 1.0F);
        }

        private void spawnStands() {
            if (!(level() instanceof ServerLevel server)) return;
            for (Vec3 marker : markers) {
                ArmorStand stand = swordStand(server, marker.x, marker.y + 12.0D, marker.z, false);
                stand.setDeltaMovement(0, -0.9D, 0);
                server.addFreshEntity(stand);
                stands.add(stand.getUUID());
            }
        }

        private boolean tick(ServerLevel server) {
            tick++;
            for (Vec3 marker : markers) server.sendParticles(ParticleTypes.LANDING_LAVA, marker.x, marker.y + 0.1D, marker.z, 1, 0, 0, 0, 0);

            if (tick >= 28 && nextImpactIndex < impacts.size()) {
                for (int count = 0; count < 2 && nextImpactIndex < impacts.size(); count++) {
                    pendingImpacts.add(new PendingSwordImpact(impacts.get(nextImpactIndex++), tick + 2));
                }
            }

            Iterator<PendingSwordImpact> iterator = pendingImpacts.iterator();
            while (iterator.hasNext()) {
                PendingSwordImpact pending = iterator.next();
                redColumn(server, pending.point());
                if (tick >= pending.impactTick()) {
                    impact(server, pending.point());
                    iterator.remove();
                }
            }

            if (tick >= 55 && pendingImpacts.isEmpty()) {
                for (UUID id : stands) {
                    Entity entity = server.getEntity(id);
                    if (entity != null) entity.discard();
                }
                return true;
            }
            return false;
        }

        private void redColumn(ServerLevel server, Vec3 point) {
            for (double y = 0.5D; y <= 4.0D; y += 0.5D) server.sendParticles(RED_DUST, point.x, point.y + y, point.z, 1, 0, 0, 0, 0);
        }

        private void impact(ServerLevel server, Vec3 point) {
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.RED_CONCRETE)), point.x, point.y + 0.1D, point.z, 20, 0.8, 0, 0.8, 0.1D);
            server.sendParticles(ParticleTypes.EXPLOSION, point.x, point.y + 0.1D, point.z, 1, 0, 0, 0, 0);
            playGlobal(SoundEvents.TRIDENT_HIT, 2.0F, 0.9F);
            for (ServerPlayer player : combatPlayers()) {
                if (player.distanceToSqr(point) > 1.44D || !struck.add(player.getUUID())) continue;
                player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 0, false, false),
                        ApigloBossEntity.this);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
            }
        }
    }

    private record PendingSwordImpact(Vec3 point, int impactTick) {
    }

    private final class TormentState {
        private final List<Vec3> points;
        private final List<UUID> stands = new ArrayList<>();
        private int tick;

        private TormentState(List<Vec3> points) {
            this.points = points;
            if (level() instanceof ServerLevel server) {
                for (Vec3 point : points) {
                    ArmorStand stand = swordStand(server, point.x - 0.3D, point.y + 4.5D, point.z - 0.5D, true);
                    server.addFreshEntity(stand);
                    stands.add(stand.getUUID());
                }
            }
            tormentEvent.setProgress(1.0F);
            tormentEvent.setVisible(true);
            playGlobal(SoundEvents.TRIDENT_THROW, 1.0F, 0.7F);
            playGlobal(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 0.7F);
        }

        private boolean tick(ServerLevel server) {
            tick++;
            tormentEvent.setProgress(Mth.clamp((200.0F - tick) / 200.0F, 0.0F, 1.0F));
            for (UUID id : stands) {
                Entity entity = server.getEntity(id);
                if (entity == null) continue;
                entity.setPos(entity.getX(), entity.getY() - 0.028D, entity.getZ());
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, entity.getX(), entity.getY(), entity.getZ(), 2, 0.2, 0.4, 0.2, 0.02D);
            }
            if (tick < 200) return false;
            boolean failed = false;
            for (Vec3 point : points) {
                ServerPlayer player = combatPlayers().stream().filter(p -> p.distanceToSqr(point) <= 6.25D)
                        .min(Comparator.comparingDouble(p -> p.distanceToSqr(point))).orElse(null);
                if (player == null) {
                    failed = true;
                    server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, point.x, point.y, point.z, 1, 0, 0, 0, 0);
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 180, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 180, 1));
                    server.sendParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 1.0D, player.getZ(), 12, 0.3, 0.5, 0.3, 0);
                }
            }
            if (failed) failTorment();
            removeAll();
            return true;
        }

        private void removeAll() {
            if (level() instanceof ServerLevel server) {
                for (UUID id : stands) {
                    Entity entity = server.getEntity(id);
                    if (entity != null) entity.discard();
                }
            }
            tormentEvent.setVisible(false);
        }
    }

    private void failTorment() {
        tormentFailures++;
        float damage = switch (tormentFailures) {
            case 1 -> 6.0F;
            case 2 -> 12.0F;
            case 3 -> 24.0F;
            case 4, 5 -> 30.0F;
            default -> 1000.0F;
        };
        for (ServerPlayer player : combatPlayers()) {
            player.hurt(damageSources().mobAttack(this), damage);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
        }
        broadcast(Component.translatable("message.finalparadox.apiglo.torment.fail"));
    }

    private ArmorStand swordStand(ServerLevel server, double x, double y, double z, boolean noGravity) {
        ArmorStand stand = new ArmorStand(server, x, y, z);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setNoGravity(noGravity);
        stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_SWORD));
        stand.setHeadPose(new Rotations(0.0F, 0.0F, 105.0F));
        markOwned(stand);
        stand.getPersistentData().putBoolean(TRANSIENT_KEY, true);
        return stand;
    }

    private final class PortalSpawn {
        private final Vec3 point;
        private final int type;
        private int tick;

        private PortalSpawn(Vec3 point, int type, int tick) {
            this.point = point;
            this.type = type;
            this.tick = tick;
        }

        private boolean tick(ServerLevel server) {
            if (++tick < 0) return false;
            if (type == 5) {
                startReinforcementPortals();
                return true;
            }
            if (type == 6) {
                spawnHostileGlaivorus();
                return true;
            }
            double y = point.y + 4.0D - tick * 0.2D;
            server.sendParticles(ParticleTypes.LARGE_SMOKE, point.x, y, point.z, 2, 1, 0, 1, 0.02D);
            server.sendParticles(PINK_DUST, point.x, y, point.z, 2, 1, 0, 1, 0);
            if (tick < 20) return false;
            if (type == 1) spawnBrute(server, point);
            else if (type == 2) spawnArcher(server, point, true);
            else if (type == 3) spawnArcher(server, point, false);
            else spawnServantZombie(server, point);
            return true;
        }
    }

    private void spawnBrute(ServerLevel server, Vec3 point) {
        PiglinBrute brute = EntityType.PIGLIN_BRUTE.create(server);
        if (brute == null) return;
        configureMinion(brute, point, "entity.finalparadox.apiglo.imperial_despot", 44.0D, 40.0F, 0.27D);
        brute.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(40.0D);
        brute.setImmuneToZombification(true);
        brute.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
        server.addFreshEntity(brute);
    }

    private void spawnArcher(ServerLevel server, Vec3 point, boolean imperial) {
        Skeleton skeleton = EntityType.SKELETON.create(server);
        if (skeleton == null) return;
        configureMinion(skeleton, point, imperial ? "entity.finalparadox.apiglo.imperial_archer" : "entity.finalparadox.apiglo.imperial_servant",
                imperial ? 25.0D : 20.0D, imperial ? 20.0F : 18.0F, imperial ? 0.27D : 0.25D);
        skeleton.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(imperial ? 50.0D : 60.0D);
        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(Enchantments.POWER_ARROWS, 1);
        if (imperial) bow.enchant(Enchantments.PUNCH_ARROWS, 1);
        skeleton.setItemSlot(EquipmentSlot.MAINHAND, bow);
        if (imperial) {
            skeleton.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
            skeleton.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
            ItemStack chest = new ItemStack(Items.GOLDEN_CHESTPLATE);
            chest.enchant(Enchantments.PROJECTILE_PROTECTION, 1);
            skeleton.setItemSlot(EquipmentSlot.CHEST, chest);
            skeleton.setItemSlot(EquipmentSlot.HEAD, skull(SERVANT_HEAD_TEXTURE, new int[]{1313110795,1103447220,-1608888045,-1865975293}));
        } else {
            skeleton.getPersistentData().putBoolean(SERVANT_KEY, true);
            equipServantArmor(skeleton, true);
        }
        server.addFreshEntity(skeleton);
    }

    private void spawnServantZombie(ServerLevel server, Vec3 point) {
        Zombie zombie = EntityType.ZOMBIE.create(server);
        if (zombie == null) return;
        configureMinion(zombie, point, "entity.finalparadox.apiglo.imperial_servant", 25.0D, 20.0F, 0.25D);
        zombie.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(60.0D);
        zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
        zombie.getPersistentData().putBoolean(SERVANT_KEY, true);
        equipServantArmor(zombie, false);
        server.addFreshEntity(zombie);
    }

    private void equipServantArmor(Mob mob, boolean archer) {
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        ItemStack chest = new ItemStack(Items.LEATHER_CHESTPLATE);
        if (archer) {
            dyeLeather(boots, 10416730);
            dyeLeather(chest, 10416730);
            mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        } else chest.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 1);
        mob.setItemSlot(EquipmentSlot.FEET, boots);
        mob.setItemSlot(EquipmentSlot.CHEST, chest);
        mob.setItemSlot(EquipmentSlot.HEAD, skull(SERVANT_HEAD_TEXTURE, new int[]{1313110795,1103447220,-1608888045,-1865975293}));
    }

    private static void dyeLeather(ItemStack stack, int color) {
        stack.getOrCreateTagElement("display").putInt("color", color);
    }

    private void configureMinion(Mob mob, Vec3 point, String nameKey, double maxHealth, float health, double speed) {
        mob.moveTo(point.x, point.y, point.z, random.nextFloat() * 360.0F, 0.0F);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
        mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        mob.setHealth(health);
        mob.setCustomName(Component.translatable(nameKey));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();
        markOwned(mob);
        for (EquipmentSlot slot : EquipmentSlot.values()) mob.setDropChance(slot, 0.0F);
        combatPlayers().stream().min(Comparator.comparingDouble(mob::distanceToSqr)).ifPresent(mob::setTarget);
        if (level() instanceof ServerLevel level) {
            level.sendParticles(PURPLE_DUST, point.x, point.y + 0.2D, point.z, 12, 1.5, 0, 1.5, 0);
            level.sendParticles(ParticleTypes.EXPLOSION, point.x, point.y, point.z, 1, 0, 0, 0, 0);
            level.playSound(null, BlockPos.containing(point), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.MASTER, 2.0F, 1.2F);
        }
    }
}
