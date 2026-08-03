package io.github.finalparadox.entity;

import io.github.finalparadox.arena.ArenaDeploymentData;
import io.github.finalparadox.registry.ModItems;
import io.github.finalparadox.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Server-side B8 Zombie Supermatrix encounter controller. Mirrors the source
 * datapack chain (b8/ini, b8/cuenta_atras, b8/summon, b8/setvida, b8/run,
 * b8/reset) with every coordinate resolved from the player-deployed floor
 * anchor. M2-M8 features (mounts, bullet hits, hazards, phase timelines,
 * victory/defeat, dialogue and music) are hook stubs on purpose.
 */
public final class B8EncounterController {
    public static final String TIMER_COUNTDOWN_3 = "countdown_3";
    public static final String TIMER_COUNTDOWN_2 = "countdown_2";
    public static final String TIMER_COUNTDOWN_1 = "countdown_1";
    public static final String TIMER_SUMMON = "summon";
    public static final String TIMER_RIDE_3 = "ride_3";
    public static final String TIMER_RIDE_2 = "ride_2";
    public static final String TIMER_RIDE_1 = "ride_1";
    public static final String TIMER_COMENZAR = "comenzar";
    public static final String TIMER_H2_INI = "h2_ini";
    public static final String TIMER_H4_GEN_2 = "h4_gen_2";
    public static final String TIMER_H4_GEN_3 = "h4_gen_3";
    public static final String TIMER_H1_INI = "h1_ini";
    public static final String TIMER_H1_INI3 = "h1_ini3";
    public static final String TIMER_H4_INI = "h4_ini";
    public static final String TIMER_H4_INI2 = "h4_ini2";
    public static final String TIMER_H4_INI3 = "h4_ini3";
    public static final String TIMER_RESPAWN = "respawn";
    public static final String TIMER_VICTORY = "victory";
    public static final String TIMER_MUSIC_LOOP = "music_loop";
    public static final String TIMER_DIALOGUE_1 = "dialogue_1";
    public static final String TIMER_DIALOGUE_2 = "dialogue_2";
    public static final String TIMER_DIALOGUE_3 = "dialogue_3";
    public static final String TIMER_DIALOGUE_4 = "dialogue_4";
    public static final String TIMER_DIALOGUE_5 = "dialogue_5";
    public static final String TIMER_DIALOGUE_6 = "dialogue_6";
    public static final String TIMER_DIALOGUE_7 = "dialogue_7";
    public static final String TIMER_DIALOGUE_8 = "dialogue_8";
    public static final String TIMER_FRASES_H1 = "frases_h1";

    public static final int BULLET_MISS = 0;
    public static final int BULLET_BLOCKED = 1;
    public static final int BULLET_HIT = 2;

    private static final int RUN_INTERVAL = 20;
    private static final int CONTAINMENT_RADIUS = 26;
    private static final int HOSTILE_CLEANUP_RADIUS = 29;
    private static final int INITIAL_HEALTH = 250;
    private static final double HIT_RADIUS_SQ = 1.5D * 1.5D;
    private static final double BLOCK_RADIUS_SQ = 3.0D * 3.0D;
    // Source hitbox: summoned at core-0.8, teleported to core-1.3, then sampled
    // at +1.5 => the hit center is the core position plus 0.2 blocks.
    private static final double HIT_CENTER_LOCAL_Y = 0.2D;
    private static final double MOUNT_RADIUS = 14.0D;
    private static final double MOUNT_START_YAW = 300.0D;
    private static final double MOUNT_YAW_STEP = 20.0D;
    private static final int H2_MARKER_Y_OFFSET = 10;
    private static final int H2_BASE_RADIUS = 7;
    private static final int H2_RING_STEP = 2;
    private static final int H2_RINGS = 10;
    private static final int H2_DIRECTIONS = 32;
    private static final int H2_PER_RING = 5;
    private static final int H2_BASE_KEEP = 20;
    private static final int H2_PER_PLAYER = 12;
    private static final double H2_DIRECTION_STEP_DEG = 11.25D;
    private static final double H2_DIRECTION_JITTER_DEG = 5.625D;
    private static final int H2_END_DELAY_TICKS = 40;
    private static final int H1_ADVANCE_TICKS = 101;
    private static final int H1_KILL_TICKS = 114;
    private static final int H1_WARN_INTERVAL = 10;
    private static final double H1_WARN_STEP = 0.4D;
    private static final double H1_WARN_LIMIT = 23.0D;
    private static final double H1_DAMAGE_RADIUS = 4.0D;
    private static final double H1_CLOUD_RADIUS = 3.0D;
    private static final double H1_ROD_SPEED = 0.1D;
    private static final int H4_SOURCE_KILL_TICKS = 119;
    private static final int H4_SOURCE_ROTATE_TICKS = 19;
    private static final double H4_WARN_ROTATE = 7.0D;
    private static final int H4_WARN_KILL_TICKS = 100;
    private static final double H4_DAMAGE_RADIUS = 5.5D;
    private static final double H4_CLOUD_RADIUS = 3.6D;
    private static final double H4_ROD_SPEED = 0.12D;
    private static final double[] H4_RADII = {7.0D, 14.0D, 20.0D};
    private static final double H3_RADIUS = 25.0D;
    // Source markers sit at anchor-4, so the adds emerge from below the floor.
    // Per user feedback (2026-08-02) the whole appear-to-land arc is shifted up
    // to start on the walkable floor (anchor+1) while keeping the source peak
    // (floor+5) and landing (floor).
    private static final double H3_MARKER_Y_OFFSET = 1.0D;
    private static final int H3_MARKERS = 64;
    private static final double H3_PLAYER_CLEAR = 10.0D;
    private static final int H3_SNIPER_FIRE_INTERVAL = 9;
    private static final double H3_SETUP_TICKS = 20;
    /** Sniper hover height above the walkable floor, per user feedback. */
    private static final double H3_SNIPER_HOVER_OFFSET = 3.0D;
    private static final String ADD_AGE_KEY = "finalparadox.b8_add_age";
    private static final String ADD_TYPE_KEY = "finalparadox.b8_add_type";
    private static final String ADD_YAW_KEY = "finalparadox.b8_add_yaw";
    private static final String ADD_FIRE_KEY = "finalparadox.b8_sniper_fire";
    private static final String CRUSH_COOLDOWN_KEY = "finalparadox.b8_crush_cooldown";
    private static final String CRUSH_CAST_KEY = "finalparadox.b8_crush_cast";
    private static final String CRUSH_DIR_X_KEY = "finalparadox.b8_crush_dir_x";
    private static final String CRUSH_DIR_Z_KEY = "finalparadox.b8_crush_dir_z";
    private static final String CRUSH_STAND_KEY = "finalparadox.b8_crush_stand";
    private static final String CRUSH_OWNER_KEY = "finalparadox.b8_crush_owner";
    private static final String[] ROBOT_SKULLS = {
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTUyZGM0YmJiMjU5NDMwNWRjNTVjY2NjZWMxNGEyODI0YTg5NDc3YWFjYTQyYjRkYzVkZDU3OTJhODg2ZTg4MSJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzIyMDI2YTcwNGUyOWI2MGI5NDU1ZWMxYjllNTY3M2NjNjhjMjkxMGQ3Y2FmNzY0Yzg1N2IxMDhlYTkyNDRjOSJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzM1ODU4ZDY5ZGQ2M2YyMDM3ODA2NTgwMDg4NDQ3YjhmYWQ5NGUxMWIwOGJmYTVlMTNjOGRiNzJiYmEzZTEyNSJ9fX0="
    };
    private static final String SNIPER_SKULL =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjhiNjg1MzkwNmMzMzU1NThjODhjODBkYzEzZjA1YzMxNDE1MTU2MzBiZmFiMTM5ZWVjOGY0ZmRkOTQwZDdhZiJ9fX0=";
    private static final long MUSIC_LOOP_INTERVAL = 2448; // source 122.4s
    private static final String DIALOGUE_BASE = "luisb1202.functions.bossfight.b8.dialogos.";
    private static final String SPEAKER_KOROS = "luisb1202.functions.afijos.descubrir.hd.3";
    private static final String SPEAKER_MATRIX = DIALOGUE_BASE + "dia4.1";

    // Anchor-relative offsets from the player-deployed floor anchor.
    private static final Vec3iOffset PLAYER_SPAWN = new Vec3iOffset(11, 1, 1);
    private static final Vec3iOffset MATRIX_CORE = new Vec3iOffset(0, 7, 0);
    private static final Vec3iOffset SPECTATOR_TP = new Vec3iOffset(0, 13, 0);
    private static final Vec3iOffset TP_BACK = new Vec3iOffset(13, 1, 0);
    private static final int FORCELOAD_MIN_X = -23;
    private static final int FORCELOAD_MAX_X = 24;
    private static final int FORCELOAD_MIN_Z = -24;
    private static final int FORCELOAD_MAX_Z = 24;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("luisb1202.functions.bossfight.b8.setvida.1"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    private ServerLevel serverLevel;
    private B8EncounterData data;
    private int runTicks;
    private int lastBossValue = -1;
    private long lastHitTick = Long.MIN_VALUE;

    public void prepare(ServerLevel server, B8EncounterData data) {
        this.serverLevel = server;
        this.data = data;
        h2CleanupModules(server, data);
        clearH1(server, data);
        clearH4(server, data);
        h3Cleanup(server, data);
        data.setActive(true);
        data.setState(B8EncounterData.STATE_COUNTDOWN);
        data.setFase(1);
        data.setRonda(0);
        data.setHealth(INITIAL_HEALTH);
        data.setHealthTotal(INITIAL_HEALTH);
        data.setVulnerable(false);
        data.setAddCount(0);
        data.setMatrixUuid(null);
        data.clearTimers();
        data.clearCleanup();
        data.clearParticipants();
        data.clearSpectators();
        data.clearMounts();
        for (ServerPlayer player : server.players()) {
            data.addParticipant(player.getUUID());
        }

        snapshotGameRules(server, data);
        applyBossGameRules(server, data);

        BlockPos spawn = data.anchor().offset(PLAYER_SPAWN.x(), PLAYER_SPAWN.y(), PLAYER_SPAWN.z());
        for (ServerPlayer player : server.players()) {
            player.setRespawnPosition(server.dimension(), spawn, 0.0F, false, false);
        }
        forceChunks(server, data, true);

        long now = server.getGameTime();
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_COUNTDOWN_3, now + 60));
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_COUNTDOWN_2, now + 80));
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_COUNTDOWN_1, now + 100));
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_SUMMON, now + 120));
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_DIALOGUE_8, now + 40)); // source ini8 @2s
        notifyPlayers(Component.literal("B8 encounter started. Countdown: 3..."));
    }

    public void recover(ServerLevel server, B8EncounterData data) {
        this.serverLevel = server;
        this.data = data;
        forceChunks(server, data, true);
        if (data.state() >= B8EncounterData.STATE_PHASE_1
                && data.state() <= B8EncounterData.STATE_PHASE_5) {
            setupBossBar(server);
            recoverMounts(server, data);
            if (matrix() == null) {
                spawnMatrix(data.vulnerable());
            }
        }
    }

    public void tick(ServerLevel server, B8EncounterData data) {
        if (!data.active()) return;
        this.serverLevel = server;
        this.data = data;
        fireTimers(server, data);
        if (data.h2Active()
                && data.state() >= B8EncounterData.STATE_PHASE_1
                && data.state() <= B8EncounterData.STATE_PHASE_5) {
            tickH2(server, data);
        }
        if (data.h1Active()
                && data.state() >= B8EncounterData.STATE_PHASE_1
                && data.state() <= B8EncounterData.STATE_PHASE_5) {
            tickH1(server, data);
        }
        if (data.h4Active()
                && data.state() >= B8EncounterData.STATE_PHASE_1
                && data.state() <= B8EncounterData.STATE_PHASE_5) {
            tickH4(server, data);
        }
        if (data.state() >= B8EncounterData.STATE_PHASE_1
                && data.state() <= B8EncounterData.STATE_PHASE_5) {
            tickAdds(server, data);
        }
        if (data.state() >= B8EncounterData.STATE_PHASE_1
                && data.state() <= B8EncounterData.STATE_PHASE_5) {
            applyCombatEffects(server);
        }
        if (data.state() >= B8EncounterData.STATE_PHASE_1
                && data.state() <= B8EncounterData.STATE_PHASE_5) {
            runTicks++;
            if (runTicks >= RUN_INTERVAL) {
                runTicks = 0;
                runLoop(server, data);
            }
        }
    }

    /**
     * Source b8/run resistance: during combat every non-spectator player gets
     * Resistance V and Regeneration V so arena hazards cannot one-shot them.
     */
    private void applyCombatEffects(ServerLevel server) {
        MobEffectInstance resistance = new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, 40, 4, true, false, false);
        MobEffectInstance regeneration = new MobEffectInstance(
                MobEffects.REGENERATION, 40, 4, true, false, false);
        for (ServerPlayer player : server.players()) {
            if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
                player.addEffect(resistance);
                player.addEffect(regeneration);
            }
        }
    }

    private void fireTimers(ServerLevel server, B8EncounterData data) {
        if (data.timers().isEmpty()) return;
        long now = server.getGameTime();
        List<B8EncounterData.TimerEntry> due = new ArrayList<>();
        for (B8EncounterData.TimerEntry timer : data.timers()) {
            if (timer.dueGameTime() <= now) due.add(timer);
        }
        if (due.isEmpty()) return;
        for (B8EncounterData.TimerEntry timer : due) {
            data.removeTimer(timer);
            switch (timer.type()) {
                case TIMER_COUNTDOWN_3 -> showCountdown("luisb1202.functions.bossfight.b1.cuenta_atras.3.1");
                case TIMER_COUNTDOWN_2 -> showCountdown("luisb1202.functions.afijos.detonante.2.1");
                case TIMER_COUNTDOWN_1 -> showCountdown("luisb1202.functions.afijos.detonante.1.2");
                case TIMER_SUMMON -> summonEncounter(server, data);
                case TIMER_RIDE_3 -> showCountdown("luisb1202.functions.bossfight.b1.cuenta_atras.3.1");
                case TIMER_RIDE_2 -> showCountdown("luisb1202.functions.afijos.detonante.2.1");
                case TIMER_RIDE_1 -> showCountdown("luisb1202.functions.afijos.detonante.1.2");
                case TIMER_COMENZAR -> comenzar(server, data);
                case TIMER_H2_INI -> startH2(server, data);
                case TIMER_H4_GEN_2 -> addH4Source(server, data);
                case TIMER_H4_GEN_3 -> addH4Source(server, data);
                case TIMER_H1_INI -> startH1(server, data, 1);
                case TIMER_H1_INI3 -> startH1(server, data, 3);
                case TIMER_H4_INI -> startH4(server, data, 1);
                case TIMER_H4_INI2 -> startH4(server, data, 2);
                case TIMER_H4_INI3 -> startH4(server, data, 3);
                case TIMER_RESPAWN -> respawn(server, data);
                case TIMER_VICTORY -> victory(server, data);
                case TIMER_MUSIC_LOOP -> playB8Music(server);
                case TIMER_DIALOGUE_1 -> dialogue(server, 1);
                case TIMER_DIALOGUE_2 -> dialogue(server, 2);
                case TIMER_DIALOGUE_3 -> dialogue(server, 3);
                case TIMER_DIALOGUE_4 -> dialogue(server, 4);
                case TIMER_DIALOGUE_5 -> dialogue(server, 5);
                case TIMER_DIALOGUE_6 -> dialogue(server, 6);
                case TIMER_DIALOGUE_7 -> dialogue(server, 7);
                case TIMER_DIALOGUE_8 -> dialogue(server, 8);
                case TIMER_FRASES_H1 -> frasesH1(server);
                default -> {
                }
            }
        }
    }

    private void comenzar(ServerLevel server, B8EncounterData data) {
        // Source fase/1/comenzar: growl + title; music (M8) and h2/ini +1s (M4) land here.
        for (ServerPlayer player : server.players()) {
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("luisb1202.functions.bossfight.b8.fase.1.comenzar.1")));
            server.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                    SoundSource.MASTER, 1.0F, 1.5F);
        }
        // Source fase/1/comenzar schedules h2/ini at +1s.
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_H2_INI, server.getGameTime() + 20));
        playB8Music(server);
        notifyPlayers(Component.literal("B8 battle started."));
    }

    private void showCountdown(String key) {
        for (ServerPlayer player : serverLevel.players()) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(key)));
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(),
                    SoundSource.MASTER, 1.0F, 1.5F);
        }
    }

    private void summonEncounter(ServerLevel server, B8EncounterData data) {
        // Phase 1 init (source b8/summon -> fase/1/ini -> ini_monturas).
        data.setState(B8EncounterData.STATE_PHASE_1);
        data.setFase(1);
        data.setRonda(0);
        data.setVulnerable(false);
        spawnMatrix(false);
        setupBossBar(server);
        startPhase1(server, data);
    }

    private void spawnMatrix(boolean vulnerable) {
        BlockPos core = data.anchor().offset(MATRIX_CORE.x(), MATRIX_CORE.y(), MATRIX_CORE.z());
        // Remove any stray model matrix inside the arena before the official one.
        BlockPos min = data.anchor().offset(-25, -9, -25);
        BlockPos max = data.anchor().offset(25, 13, 25);
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                min.getX(), min.getY(), min.getZ(),
                max.getX() + 1.0D, max.getY() + 1.0D, max.getZ() + 1.0D);
        for (ZombieSupermatrixEntity stray : serverLevel.getEntitiesOfClass(
                ZombieSupermatrixEntity.class, box, Entity::isAlive)) {
            stray.discard();
        }
        ZombieSupermatrixEntity matrix = ZombieSupermatrixEntity.spawn(
                serverLevel, new Vec3(core.getX(), core.getY(), core.getZ()), vulnerable);
        // Source matriz/gen tags the core "boss"; rovers use it to reject dismount.
        matrix.addTag("boss");
        data.setMatrixUuid(matrix.getUUID());
        ArenaDeploymentData.get(serverLevel).setActiveBossUuid(matrix.getUUID());
    }

    private void startPhase1(ServerLevel server, B8EncounterData data) {
        // Source fase/1/ini: titles and pling, then ini_monturas.
        for (ServerPlayer player : server.players()) {
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("luisb1202.functions.bossfight.b1.ini_f1.1")));
            player.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.translatable("luisb1202.functions.bossfight.b4.fase.1.ini.1")));
            server.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                    SoundSource.MASTER, 1.0F, 1.5F);
        }
        spawnMounts(server, data);
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_DIALOGUE_3, server.getGameTime() + 40));
        notifyPlayers(Component.literal("Mounts spawned. All non-spectator players must board to start."));
    }

    private void spawnMounts(ServerLevel server, B8EncounterData data) {
        BlockPos anchor = data.anchor();
        BlockPos recovery = anchor.offset(TP_BACK.x(), TP_BACK.y(), TP_BACK.z());
        int index = 0;
        for (ServerPlayer player : server.players()) {
            if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) continue;
            double yaw = Math.toRadians(MOUNT_START_YAW + MOUNT_YAW_STEP * index);
            Vec3 position = new Vec3(
                    anchor.getX() + MOUNT_RADIUS * (-Math.sin(yaw)),
                    anchor.getY() + 1.0D,
                    anchor.getZ() + MOUNT_RADIUS * Math.cos(yaw));
            TerrastalkerRoverEntity.spawnEncounter(player, position,
                            new Vec3(recovery.getX(), recovery.getY(), recovery.getZ()))
                    .ifPresent(rover -> {
                        data.assignMount(player.getUUID(), rover.getUUID());
                        data.addCleanup(rover.getUUID());
                    });
            index++;
        }
    }

    /** Re-spawns only the assigned rovers that no longer exist after a reload. */
    private void recoverMounts(ServerLevel server, B8EncounterData data) {
        List<UUID> order = data.mountOrder();
        for (int index = 0; index < order.size(); index++) {
            UUID rider = order.get(index);
            UUID roverUuid = data.mountRover(rider);
            if (roverUuid != null && server.getEntity(roverUuid) != null) continue;
            ServerPlayer player = server.getServer().getPlayerList().getPlayer(rider);
            if (player == null || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) continue;
            double yaw = Math.toRadians(MOUNT_START_YAW + MOUNT_YAW_STEP * index);
            BlockPos anchor = data.anchor();
            BlockPos recovery = anchor.offset(TP_BACK.x(), TP_BACK.y(), TP_BACK.z());
            Vec3 position = new Vec3(
                    anchor.getX() + MOUNT_RADIUS * (-Math.sin(yaw)),
                    anchor.getY() + 1.0D,
                    anchor.getZ() + MOUNT_RADIUS * Math.cos(yaw));
            TerrastalkerRoverEntity.spawnEncounter(player, position,
                            new Vec3(recovery.getX(), recovery.getY(), recovery.getZ()))
                    .ifPresent(rover -> {
                        data.assignMount(rider, rover.getUUID());
                        data.addCleanup(rover.getUUID());
                    });
        }
    }

    private void runLoop(ServerLevel server, B8EncounterData data) {
        BlockPos center = data.anchor();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();

        for (ServerPlayer player : server.players()) {
            GameType gameType = player.gameMode.getGameModeForPlayer();
            if (gameType == GameType.SPECTATOR) {
                player.displayClientMessage(
                        Component.translatable("luisb1202.functions.bossfight.b8.run.1"), true);
                player.teleportTo(server,
                        centerX + SPECTATOR_TP.x(), centerY + SPECTATOR_TP.y(), centerZ + SPECTATOR_TP.z(),
                        player.getYRot(), player.getXRot());
            } else if (gameType != GameType.CREATIVE) {
                double dx = player.getX() - centerX;
                double dz = player.getZ() - centerZ;
                if (dx * dx + dz * dz > CONTAINMENT_RADIUS * (double) CONTAINMENT_RADIUS) {
                    player.teleportTo(server,
                            centerX + TP_BACK.x(), centerY + TP_BACK.y(), centerZ + TP_BACK.z(),
                            90.0F, 0.0F);
                }
            }
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, 2020, 1, false, false, false));
            if (!bossEvent.getPlayers().contains(player)) {
                bossEvent.addPlayer(player);
            }
        }

        // Source fase/1/run: when every non-spectator player is mounted, the
        // three-two-one ride countdown begins once (ronda 0 -> 1).
        if (data.state() == B8EncounterData.STATE_PHASE_1
                && data.ronda() == 0
                && allNonSpectatorsMounted(server)) {
            startRideCountdown(server, data);
        }

        // Source: execute positioned <center> run kill @e[tag=hostile,distance=29..]
        for (Entity entity : server.getAllEntities()) {
            if (entity.isAlive() && entity.getTags().contains("hostile")) {
                double dx = entity.getX() - centerX;
                double dz = entity.getZ() - centerZ;
                if (dx * dx + dz * dz >= HOSTILE_CLEANUP_RADIUS * (double) HOSTILE_CLEANUP_RADIUS) {
                    entity.kill();
                }
            }
        }

        refreshBossBar();
        recountAdds(server, data);
        runPhase(server, data);

        // Source: execute unless entity @p run function b8/respawn
        if (server.players().isEmpty()) {
            endEncounter(server, data);
        }
    }

    private boolean allNonSpectatorsMounted(ServerLevel server) {
        for (ServerPlayer player : server.players()) {
            if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) continue;
            if (!(player.getVehicle() instanceof TerrastalkerRoverEntity rover)
                    || !rover.isEncounterMode() || !rover.isAlive()) {
                return false;
            }
        }
        return true;
    }

    private void startRideCountdown(ServerLevel server, B8EncounterData data) {
        data.setRonda(1);
        long now = server.getGameTime();
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_RIDE_3, now + 20));
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_RIDE_2, now + 40));
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_RIDE_1, now + 60));
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_COMENZAR, now + 80));
    }

    /* ------------------------------ H2 modules ------------------------------ */

    public void startH2(ServerLevel server, B8EncounterData data) {
        h2CleanupModules(server, data);
        computeH2Candidates(server, data);
        data.setH2Active(true);
        data.setH2EndDelay(-1);

        BlockPos center = data.anchor();
        server.playSound(null, center, SoundEvents.ILLUSIONER_PREPARE_MIRROR,
                SoundSource.MASTER, 10.0F, 1.7F);
        server.playSound(null, center, SoundEvents.NETHERITE_BLOCK_BREAK,
                SoundSource.MASTER, 10.0F, 0.0F);
        emitH2Particulas(server, data);
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_DIALOGUE_1, server.getGameTime() + 40));
        dialogue(server, 6);
        notifyPlayers(Component.literal("B8 H2: gold modules incoming."));
    }

    private void emitH2Particulas(ServerLevel server, B8EncounterData data) {
        BlockPos center = data.anchor();
        double y = center.getY() + 1.0D;
        for (int index = 0; index < 32; index++) {
            double angle = Math.toRadians(index * 11.25D);
            server.sendParticles(ParticleTypes.END_ROD,
                    center.getX(), y, center.getZ(), 0,
                    Math.sin(angle) * 3.0D, 0.0D, Math.cos(angle) * 3.0D, 0.25D);
        }
        server.sendParticles(ParticleTypes.EXPLOSION,
                center.getX(), y, center.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.FLASH,
                center.getX(), y, center.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private void computeH2Candidates(ServerLevel server, B8EncounterData data) {
        BlockPos anchor = data.anchor();
        double centerX = anchor.getX();
        double centerZ = anchor.getZ();
        double markerY = anchor.getY() + H2_MARKER_Y_OFFSET;
        List<double[]> candidates = new ArrayList<>();
        for (int ring = 0; ring < H2_RINGS; ring++) {
            int radius = H2_BASE_RADIUS + H2_RING_STEP * ring;
            List<Integer> directions = new ArrayList<>();
            for (int i = 0; i < H2_DIRECTIONS; i++) directions.add(i);
            for (int pick = 0; pick < H2_PER_RING && !directions.isEmpty(); pick++) {
                // Source h2/pos markers start at 11.25 degrees (11.25 * (1..32)).
                double yaw = Math.toRadians(
                        (directions.remove(server.random.nextInt(directions.size())) + 1)
                                * H2_DIRECTION_STEP_DEG
                                + (server.random.nextBoolean() ? H2_DIRECTION_JITTER_DEG : 0.0D));
                candidates.add(new double[]{
                        centerX + radius * (-Math.sin(yaw)),
                        markerY,
                        centerZ + radius * Math.cos(yaw)});
            }
        }
        // Source: keep 20 random candidates, then 12 more per non-spectator player.
        List<double[]> pool = new ArrayList<>(candidates);
        List<double[]> kept = new ArrayList<>();
        for (int i = 0; i < H2_BASE_KEEP && !pool.isEmpty(); i++) {
            kept.add(pool.remove(server.random.nextInt(pool.size())));
        }
        int players = 0;
        for (ServerPlayer player : server.players()) {
            if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) players++;
        }
        for (int i = 0; i < players * H2_PER_PLAYER && !pool.isEmpty(); i++) {
            kept.add(pool.remove(server.random.nextInt(pool.size())));
        }
        data.h2Candidates().addAll(kept);
        data.setH2PendingSpawns(kept.size());
    }

    private void tickH2(ServerLevel server, B8EncounterData data) {
        if (data.h2PendingSpawns() > 0) {
            spawnOneH2Module(server, data);
            data.setH2PendingSpawns(data.h2PendingSpawns() - 1);
        }
        if (countH2Modules(server, data) == 0 && data.h2PendingSpawns() == 0) {
            int delay = Math.max(0, data.h2EndDelay() + 1);
            data.setH2EndDelay(delay);
            if (delay >= H2_END_DELAY_TICKS) {
                data.setH2Active(false);
                data.setH2EndDelay(-1);
                openMatrix();
                data.addTimer(new B8EncounterData.TimerEntry(
                        TIMER_DIALOGUE_2, server.getGameTime() + 50));
                dialogue(server, 5);
                notifyPlayers(Component.literal("B8 matrix is now vulnerable."));
            }
        } else {
            data.setH2EndDelay(-1);
        }
    }

    private void spawnOneH2Module(ServerLevel server, B8EncounterData data) {
        List<double[]> candidates = data.h2Candidates();
        if (candidates.isEmpty()) return;
        double[] candidate = candidates.get(server.random.nextInt(candidates.size()));
        data.removeH2Candidate(candidate);
        BlockPos anchor = data.anchor();
        B8H2ModuleEntity module = B8H2ModuleEntity.spawn(server,
                new Vec3(candidate[0], candidate[1] + pickH2YOffset(server), candidate[2]),
                anchor);
        data.addCleanup(module.getUUID());
        BlockPos block = BlockPos.containing(module.position());
        server.playSound(null, block, SoundEvents.ENDER_EYE_LAUNCH,
                SoundSource.MASTER, 4.0F, 0.5F);
        server.playSound(null, block, SoundEvents.ENDER_EYE_DEATH,
                SoundSource.MASTER, 4.0F, 0.5F);
    }

    private static double pickH2YOffset(ServerLevel server) {
        double roll = server.random.nextDouble();
        if (roll < 0.2D) return 0.0D;
        if (roll < 0.2D + 4.0D / 15.0D) return 2.0D;
        if (roll < 0.2D + 8.0D / 15.0D) return 4.0D;
        if (roll < 0.2D + 10.0D / 15.0D) return 6.0D;
        return 5.0D;
    }

    private int countH2Modules(ServerLevel server, B8EncounterData data) {
        BlockPos anchor = data.anchor();
        if (anchor == null) return 0;
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                anchor.offset(-40, -10, -40), anchor.offset(40, 30, 40));
        return server.getEntitiesOfClass(B8H2ModuleEntity.class, box, Entity::isAlive).size();
    }

    /** Rover bullets break a module within 1.5 of its sample point. */
    public boolean testModuleHit(ServerLevel server, Vec3 bulletPosition) {
        net.minecraft.world.phys.AABB box =
                new net.minecraft.world.phys.AABB(bulletPosition, bulletPosition).inflate(3.0D);
        B8H2ModuleEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (B8H2ModuleEntity module : server.getEntitiesOfClass(
                B8H2ModuleEntity.class, box, Entity::isAlive)) {
            Vec3 sample = module.position().add(0.0D, B8H2ModuleEntity.BREAK_SAMPLE_Y, 0.0D);
            double distanceSqr = bulletPosition.distanceToSqr(sample);
            if (distanceSqr <= 1.5D * 1.5D && distanceSqr < best) {
                best = distanceSqr;
                nearest = module;
            }
        }
        if (nearest == null) return false;
        Vec3 center = nearest.position().add(0.0D, B8H2ModuleEntity.BREAK_SAMPLE_Y, 0.0D);
        server.sendParticles(ParticleTypes.EXPLOSION,
                center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.CLOUD,
                center.x, center.y, center.z, 5, 0.0D, 0.0D, 0.0D, 0.2D);
        server.sendParticles(new net.minecraft.core.particles.ItemParticleOption(
                        ParticleTypes.ITEM, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_BLOCK)),
                center.x, center.y, center.z, 20, 0.0D, 0.0D, 0.0D, 0.2D);
        server.playSound(null, BlockPos.containing(center), SoundEvents.ENDER_EYE_DEATH,
                SoundSource.MASTER, 3.0F, 1.4F);
        nearest.discard();
        return true;
    }

    public void onModuleLanded(ServerLevel server, B8H2ModuleEntity module) {
        Vec3 center = module.position().add(0.0D, B8H2ModuleEntity.BOOM_SAMPLE_Y, 0.0D);
        for (int index = 0; index < 60; index++) {
            double angle = Math.toRadians(index * 6.0D);
            server.sendParticles(ParticleTypes.SQUID_INK,
                    center.x + Math.sin(angle) * 3.0D, center.y, center.z + Math.cos(angle) * 3.0D,
                    1, 0.0D, 0.0D, 0.0D, 0.5D);
        }
        server.sendParticles(ParticleTypes.EXPLOSION,
                center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.SQUID_INK,
                center.x, center.y, center.z, 60, 0.0D, 0.0D, 0.0D, 0.5D);
        server.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE,
                SoundSource.MASTER, 4.0F, 1.6F);
        for (TerrastalkerRoverEntity rover : encounterRovers(server, data)) {
            rover.damageEnergy(5);
        }
        module.discard();
    }

    private List<TerrastalkerRoverEntity> encounterRovers(
            ServerLevel server, B8EncounterData data) {
        List<TerrastalkerRoverEntity> rovers = new ArrayList<>();
        for (UUID uuid : data.cleanupEntities()) {
            Entity entity = server.getEntity(uuid);
            if (entity instanceof TerrastalkerRoverEntity rover && rover.isAlive()) {
                rovers.add(rover);
            }
        }
        return rovers;
    }

    private void h2CleanupModules(ServerLevel server, B8EncounterData data) {
        if (data.anchor() != null) {
            BlockPos anchor = data.anchor();
            net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                    anchor.offset(-40, -10, -40), anchor.offset(40, 30, 40));
            for (B8H2ModuleEntity module : server.getEntitiesOfClass(
                    B8H2ModuleEntity.class, box, Entity::isAlive)) {
                module.discard();
            }
        }
        data.setH2Active(false);
        data.setH2PendingSpawns(0);
        data.setH2EndDelay(-1);
        data.clearH2Candidates();
    }

    /* ------------------------------ H1 beam ------------------------------ */

    public void startH1(ServerLevel server, B8EncounterData data, int beamCount) {
        data.clearH1Damaged();
        data.setH1Active(true);
        BlockPos center = data.anchor();
        ServerPlayer locked = randomNonSpectator(server);
        float firstYaw = locked != null
                ? yawToward(server, center, locked)
                : server.random.nextFloat() * 360.0F;
        data.addH1Beam(new B8EncounterData.BeamState(
                center.getX(), center.getY(), center.getZ(), firstYaw, 0, 0));
        for (int i = 1; i < beamCount; i++) {
            data.addH1Beam(new B8EncounterData.BeamState(
                    center.getX(), center.getY(), center.getZ(),
                    server.random.nextFloat() * 360.0F, 0, 0));
        }
        showHazardTitle(server);
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_FRASES_H1, server.getGameTime() + 10));
    }

    private void tickH1(ServerLevel server, B8EncounterData data) {
        List<B8EncounterData.BeamState> beams = new ArrayList<>();
        for (B8EncounterData.BeamState beam : data.h1Beams()) {
            int danom = beam.danom() + 1;
            int danom2 = beam.danom2() + 1;
            double x = beam.x();
            double y = beam.y();
            double z = beam.z();
            float yaw = beam.yaw();
            if (danom >= H1_ADVANCE_TICKS) {
                double rad = Math.toRadians(yaw);
                x += 2.0D * (-Math.sin(rad));
                z += 2.0D * Math.cos(rad);
                if (danom2 >= 2) {
                    h1Explosion(server, data, x, y, z);
                    danom2 = 0;
                }
                if (danom >= H1_KILL_TICKS) continue;
            } else if (danom2 >= H1_WARN_INTERVAL) {
                data.h1Warns().add(new B8EncounterData.WarnState(x, y, z, yaw));
                data.setDirty();
                danom2 = 0;
            }
            beams.add(new B8EncounterData.BeamState(x, y, z, yaw, danom, danom2));
        }
        data.replaceH1Beams(beams);
        if (data.h1Beams().isEmpty()) {
            data.setH1Active(false);
            data.replaceH1Warns(List.of());
        } else {
            tickH1Warns(server, data);
        }
    }

    private void tickH1Warns(ServerLevel server, B8EncounterData data) {
        BlockPos center = data.anchor();
        double cx = center.getX();
        double cz = center.getZ();
        List<B8EncounterData.WarnState> warns = new ArrayList<>();
        for (B8EncounterData.WarnState warn : data.h1Warns()) {
            double x = warn.x();
            double y = warn.y();
            double z = warn.z();
            double rad = Math.toRadians(warn.yaw());
            double rightX = Math.cos(rad);
            double rightZ = Math.sin(rad);
            double forwardX = -Math.sin(rad);
            double forwardZ = Math.cos(rad);
            for (int step = 0; step < 4; step++) {
                x += forwardX * H1_WARN_STEP;
                z += forwardZ * H1_WARN_STEP;
                // local (x=+/-3, y=1.02, z=0.2): side particle just past the step
                server.sendParticles(ParticleTypes.END_ROD,
                        x + rightX * 3.0D + forwardX * 0.2D, y + 1.02D,
                        z + rightZ * 3.0D + forwardZ * 0.2D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                server.sendParticles(ParticleTypes.END_ROD,
                        x - rightX * 3.0D + forwardX * 0.2D, y + 1.02D,
                        z - rightZ * 3.0D + forwardZ * 0.2D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                // local (x=+/-3, y=1.1, z=0): downward streak at the current step
                server.sendParticles(ParticleTypes.END_ROD,
                        x + rightX * 3.0D, y + 1.1D, z + rightZ * 3.0D,
                        0, 0.0D, -1.0D, 0.0D, 999999.0D);
                server.sendParticles(ParticleTypes.END_ROD,
                        x - rightX * 3.0D, y + 1.1D, z - rightZ * 3.0D,
                        0, 0.0D, -1.0D, 0.0D, 999999.0D);
            }
            for (int i = 0; i < 5; i++) {
                double ox = 2.0D - 0.5D * i;
                double oz = -1.0D + 0.2D * i;
                double rightOffX = rightX * ox;
                double rightOffZ = rightZ * ox;
                double fwdOffX = forwardX * oz;
                double fwdOffZ = forwardZ * oz;
                server.sendParticles(ParticleTypes.END_ROD,
                        x + rightOffX + fwdOffX, y + 1.1D, z + rightOffZ + fwdOffZ,
                        0, 0.0D, -1.0D, 0.0D, 999999.0D);
                server.sendParticles(ParticleTypes.END_ROD,
                        x - rightOffX + fwdOffX, y + 1.1D, z - rightOffZ + fwdOffZ,
                        0, 0.0D, -1.0D, 0.0D, 999999.0D);
            }
            double dx = x - cx;
            double dz = z - cz;
            if (dx * dx + dz * dz <= H1_WARN_LIMIT * H1_WARN_LIMIT) {
                warns.add(new B8EncounterData.WarnState(x, y, z, warn.yaw()));
            }
        }
        data.replaceH1Warns(warns);
    }

    private void h1Explosion(ServerLevel server, B8EncounterData data, double x, double y, double z) {
        Vec3 center = new Vec3(x, y + 1.0D, z);
        emitHazardExplosion(server, data, center, H1_CLOUD_RADIUS, H1_ROD_SPEED);
        for (TerrastalkerRoverEntity rover : encounterRovers(server, data)) {
            if (rover.position().distanceToSqr(center) <= H1_DAMAGE_RADIUS * H1_DAMAGE_RADIUS
                    && data.markH1Damaged(rover.getUUID())) {
                rover.damageEnergy(5);
            }
        }
    }

    /* ------------------------------ H4 area ------------------------------ */

    public void startH4(ServerLevel server, B8EncounterData data, int variant) {
        showHazardTitle(server);
        emitH2Particulas(server, data);
        BlockPos center = data.anchor();
        server.playSound(null, center, SoundEvents.ILLUSIONER_PREPARE_MIRROR,
                SoundSource.MASTER, 10.0F, 1.7F);
        server.playSound(null, center, SoundEvents.NETHERITE_BLOCK_BREAK,
                SoundSource.MASTER, 10.0F, 0.0F);
        data.setH4Active(true);
        addH4Source(server, data);
        if (variant >= 2) {
            data.addTimer(new B8EncounterData.TimerEntry(TIMER_H4_GEN_2, server.getGameTime() + 10));
        }
        if (variant >= 3) {
            data.addTimer(new B8EncounterData.TimerEntry(TIMER_H4_GEN_3, server.getGameTime() + 90));
        }
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_FRASES_H1, server.getGameTime() + 10));
    }

    private void addH4Source(ServerLevel server, B8EncounterData data) {
        List<Double> available = new ArrayList<>();
        for (double radius : H4_RADII) {
            boolean used = false;
            for (B8EncounterData.H4SourceState source : data.h4Sources()) {
                if (Math.abs(source.radius() - radius) < 0.01D) {
                    used = true;
                    break;
                }
            }
            if (!used) available.add(radius);
        }
        if (available.isEmpty()) return;
        double radius = available.get(server.random.nextInt(available.size()));
        data.addH4Source(new B8EncounterData.H4SourceState(radius, 0.0F, 0));
    }

    private void tickH4(ServerLevel server, B8EncounterData data) {
        BlockPos center = data.anchor();
        double cx = center.getX();
        double cy = center.getY() + 1.0D;
        double cz = center.getZ();
        List<B8EncounterData.H4SourceState> sources = new ArrayList<>();
        for (B8EncounterData.H4SourceState source : data.h4Sources()) {
            int danom = source.danom() + 1;
            if (danom >= H4_SOURCE_KILL_TICKS) continue;
            float yaw = source.yaw();
            if (danom <= H4_SOURCE_ROTATE_TICKS) {
                for (int giro = 0; giro < 2; giro++) {
                    yaw += 10.0F;
                    double rad = Math.toRadians(yaw);
                    double wx = cx + source.radius() * (-Math.sin(rad));
                    double wz = cz + source.radius() * Math.cos(rad);
                    boolean near = false;
                    for (B8EncounterData.H4WarnState warn : data.h4Warns()) {
                        double dx = warn.x() - wx;
                        double dy = warn.y() - cy;
                        double dz = warn.z() - wz;
                        if (dx * dx + dy * dy + dz * dz <= 25.0D) {
                            near = true;
                            break;
                        }
                    }
                    if (!near) {
                        data.h4Warns().add(new B8EncounterData.H4WarnState(wx, cy, wz, 0.0F, 0));
                        data.setDirty();
                    }
                }
            }
            sources.add(new B8EncounterData.H4SourceState(source.radius(), yaw, danom));
        }
        data.replaceH4Sources(sources);

        List<B8EncounterData.H4WarnState> warns = new ArrayList<>();
        for (B8EncounterData.H4WarnState warn : data.h4Warns()) {
            float yaw = warn.yaw() + (float) H4_WARN_ROTATE;
            int danom = warn.danom() + 1;
            double rad = Math.toRadians(yaw);
            double fx = -Math.sin(rad);
            double fz = Math.cos(rad);
            server.sendParticles(ParticleTypes.END_ROD,
                    warn.x() + fx * 4.0D, warn.y() + 0.08D, warn.z() + fz * 4.0D,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            server.sendParticles(ParticleTypes.END_ROD,
                    warn.x() - fx * 4.0D, warn.y() + 0.08D, warn.z() - fz * 4.0D,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            if (danom >= H4_WARN_KILL_TICKS) {
                h4Explosion(server, data, warn);
                continue;
            }
            warns.add(new B8EncounterData.H4WarnState(warn.x(), warn.y(), warn.z(), yaw, danom));
        }
        data.replaceH4Warns(warns);
        if (data.h4Sources().isEmpty() && data.h4Warns().isEmpty()) {
            data.setH4Active(false);
        }
    }

    private void h4Explosion(ServerLevel server, B8EncounterData data, B8EncounterData.H4WarnState warn) {
        Vec3 center = new Vec3(warn.x(), warn.y(), warn.z());
        emitHazardExplosion(server, data, center, H4_CLOUD_RADIUS, H4_ROD_SPEED);
        for (TerrastalkerRoverEntity rover : encounterRovers(server, data)) {
            if (rover.position().distanceToSqr(center) <= H4_DAMAGE_RADIUS * H4_DAMAGE_RADIUS) {
                rover.damageEnergy(5);
            }
        }
    }

    /* --------------------------- shared hazard bits --------------------------- */

    private void showHazardTitle(ServerLevel server) {
        for (ServerPlayer player : server.players()) {
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("luisb1202.functions.bossfight.b1.h1.ini.1")));
            player.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.translatable("luisb1202.functions.bossfight.b8.h1.warn_title.1")));
            server.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                    SoundSource.MASTER, 1.0F, 1.5F);
        }
    }

    private void emitHazardExplosion(
            ServerLevel server, B8EncounterData data, Vec3 center,
            double cloudRadius, double rodSpeed) {
        for (int index = 0; index < 24; index++) {
            double angle = Math.toRadians(index * 15.0D);
            server.sendParticles(ParticleTypes.CLOUD,
                    center.x + Math.sin(angle) * cloudRadius, center.y,
                    center.z + Math.cos(angle) * cloudRadius,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        for (int index = 0; index < 24; index++) {
            double angle = Math.toRadians(index * 15.0D);
            server.sendParticles(ParticleTypes.END_ROD,
                    center.x, center.y + 0.1D, center.z, 0,
                    Math.sin(angle) * 3.0D, 0.0D, Math.cos(angle) * 3.0D, rodSpeed);
        }
        server.sendParticles(ParticleTypes.EXPLOSION,
                center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.FLASH,
                center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(new BlockParticleOption(
                        ParticleTypes.BLOCK, Blocks.GRAY_CONCRETE.defaultBlockState()),
                center.x, center.y, center.z, 100, 1.0D, 0.0D, 1.0D, 0.2D);
        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                center.x, center.y, center.z, 10, 0.0D, 0.0D, 0.0D, 0.1D);
        emitCenterLine(server, data, center);
        BlockPos block = BlockPos.containing(center);
        server.playSound(null, block, SoundEvents.GENERIC_EXPLODE,
                SoundSource.MASTER, 1.0F, 2.0F);
        server.playSound(null, block, SoundEvents.FIREWORK_ROCKET_LAUNCH,
                SoundSource.MASTER, 1.0F, 2.0F);
    }

    /** Source rayo_recu: end-rod line from anchor+(0,8.5,-0.5) to the target. */
    private void emitCenterLine(ServerLevel server, B8EncounterData data, Vec3 target) {
        BlockPos anchor = data.anchor();
        Vec3 from = new Vec3(anchor.getX(), anchor.getY() + 8.5D, anchor.getZ() - 0.5D);
        Vec3 delta = target.subtract(from);
        double length = delta.length();
        if (length <= 1.5D) return;
        Vec3 direction = delta.normalize();
        for (double distance = 0.8D; distance < length - 1.5D; distance += 0.8D) {
            Vec3 point = from.add(direction.scale(distance));
            server.sendParticles(ParticleTypes.END_ROD,
                    point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private ServerPlayer randomNonSpectator(ServerLevel server) {
        List<ServerPlayer> candidates = new ArrayList<>();
        for (ServerPlayer player : server.players()) {
            if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) candidates.add(player);
        }
        return candidates.isEmpty() ? null
                : candidates.get(server.random.nextInt(candidates.size()));
    }

    private static float yawToward(ServerLevel server, BlockPos from, ServerPlayer target) {
        double dx = target.getX() - from.getX();
        double dz = target.getZ() - from.getZ();
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    private void clearH1(ServerLevel server, B8EncounterData data) {
        data.setH1Active(false);
        data.replaceH1Beams(List.of());
        data.replaceH1Warns(List.of());
        data.clearH1Damaged();
    }

    private void clearH4(ServerLevel server, B8EncounterData data) {
        data.setH4Active(false);
        data.replaceH4Sources(List.of());
        data.replaceH4Warns(List.of());
    }

    /* ------------------------------ H3 adds ------------------------------ */

    public void spawnAddWave(ServerLevel server, B8EncounterData data, String type) {
        List<Vec3> positions = addSpawnPositions(server, data);
        int players = nonSpectatorCount(server);
        switch (type) {
            case "zombie" -> spawnAddsFrom(server, data, positions,
                    8 + 4 * players, pos -> spawnZombieRobot(server, data, pos));
            case "sniper" -> spawnAddsFrom(server, data, positions,
                    2 + players, pos -> spawnSniper(server, data, pos));
            case "golem" -> spawnAddsFrom(server, data, positions,
                    1 + 2 * players, pos -> spawnGolem(server, data, pos));
            case "tnt" -> spawnAddsFrom(server, data, positions,
                    6 + 2 * Math.min(3, players), pos -> spawnTnt(server, data, pos));
            default -> {
                return;
            }
        }
        recountAdds(server, data);
    }

    private List<Vec3> addSpawnPositions(ServerLevel server, B8EncounterData data) {
        BlockPos anchor = data.anchor();
        double cx = anchor.getX();
        double cy = anchor.getY() + H3_MARKER_Y_OFFSET;
        double cz = anchor.getZ();
        List<Vec3> positions = new ArrayList<>();
        for (int index = 0; index < H3_MARKERS; index++) {
            // Source h3/pos lists 64 markers at 5.625 * (1..64) degrees.
            double rad = Math.toRadians(5.625D * (index + 1));
            positions.add(new Vec3(
                    cx + H3_RADIUS * Math.sin(rad), cy, cz + H3_RADIUS * Math.cos(rad)));
        }
        positions.removeIf(position -> {
            for (ServerPlayer player : server.players()) {
                if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR
                        && player.position().distanceToSqr(position)
                        <= H3_PLAYER_CLEAR * H3_PLAYER_CLEAR) {
                    return true;
                }
            }
            return false;
        });
        return positions;
    }

    private void spawnAddsFrom(
            ServerLevel server, B8EncounterData data, List<Vec3> positions,
            int count, java.util.function.Consumer<Vec3> spawner) {
        List<Vec3> pool = new ArrayList<>(positions);
        for (int index = 0; index < count && !pool.isEmpty(); index++) {
            spawner.accept(pool.remove(server.random.nextInt(pool.size())));
        }
    }

    private void spawnZombieRobot(ServerLevel server, B8EncounterData data, Vec3 pos) {
        Zombie zombie = new Zombie(EntityType.ZOMBIE, server);
        zombie.setPos(pos);
        setAddBasics(zombie, "luisb1202.functions.bossfight.b8.h3.zombie_robot.gen.1",
                15.0D, 0.21D, "b8_h3_enemigo1");
        zombie.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 999999, 0, false, false));
        equipAddArmor(zombie, 7435570, 12560699, randomRobotSkull());
        faceBoss(zombie, data);
        server.addFreshEntity(zombie);
        data.addCleanup(zombie.getUUID());
    }

    private void spawnSniper(ServerLevel server, B8EncounterData data, Vec3 pos) {
        Skeleton sniper = new Skeleton(EntityType.SKELETON, server);
        sniper.setPos(pos.x, pos.y + H3_SNIPER_HOVER_OFFSET, pos.z);
        sniper.setNoAi(true);
        sniper.setNoGravity(true);
        setAddBasics(sniper, "luisb1202.functions.bossfight.b8.h3.sniper.gen.1",
                30.0D, 0.0D, "b8_h3_enemigo2");
        equipAddArmor(sniper, 7693106, 12551739, skullStack(SNIPER_SKULL));
        faceBoss(sniper, data);
        server.addFreshEntity(sniper);
        data.addCleanup(sniper.getUUID());
    }

    private void spawnGolem(ServerLevel server, B8EncounterData data, Vec3 pos) {
        IronGolem golem = new IronGolem(EntityType.IRON_GOLEM, server);
        golem.setPos(pos);
        setAddBasics(golem, "luisb1202.functions.bossfight.b8.h3.golem.gen.1",
                150.0D, 0.25D, "b8_h3_golem");
        golem.addTag("afijo_lvl_1");
        golem.addTag("afijo_aplastante");
        initializeCrushingCooldown(golem, server);
        faceBoss(golem, data);
        server.addFreshEntity(golem);
        data.addCleanup(golem.getUUID());
    }

    private void spawnTnt(ServerLevel server, B8EncounterData data, Vec3 pos) {
        Skeleton tnt = new Skeleton(EntityType.SKELETON, server);
        tnt.setPos(pos);
        setAddBasics(tnt, "luisb1202.functions.bossfight.b8.h3.tnt.gen.1",
                15.0D, 0.18D, "b8_h3_enemigo3");
        tnt.addTag("afijo_lvl_3");
        tnt.addTag("afijo_detonante");
        equipAddArmor(tnt, 7435570, 12560699, new ItemStack(Items.TNT));
        faceBoss(tnt, data);
        server.addFreshEntity(tnt);
        data.addCleanup(tnt.getUUID());
    }

    private void setAddBasics(Mob mob, String nameKey, double maxHealth, double speed, String typeTag) {
        mob.setCustomName(Component.translatable(nameKey));
        mob.setCustomNameVisible(true);
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
            mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
        }
        if (mob.getAttribute(Attributes.FOLLOW_RANGE) != null) {
            mob.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(60.0D);
        }
        if (mob.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        }
        mob.setHealth((float) maxHealth);
        mob.getPersistentData().putString(ADD_TYPE_KEY, typeTag);
        mob.addTag("hostile");
        mob.addTag("b8_add");
        mob.addTag(typeTag);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            mob.setDropChance(slot, 0.0F);
        }
    }

    private void equipAddArmor(Mob mob, int bootColor, int legColor, ItemStack skull) {
        mob.setItemSlot(EquipmentSlot.FEET, dyedArmor(Items.LEATHER_BOOTS, bootColor));
        mob.setItemSlot(EquipmentSlot.LEGS, dyedArmor(Items.LEATHER_LEGGINGS, legColor));
        mob.setItemSlot(EquipmentSlot.CHEST, dyedArmor(Items.LEATHER_CHESTPLATE, bootColor));
        mob.setItemSlot(EquipmentSlot.HEAD, skull);
    }

    private static ItemStack dyedArmor(net.minecraft.world.item.Item item, int color) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTagElement("display").putInt("color", color);
        return stack;
    }

    private ItemStack randomRobotSkull() {
        if (serverLevel.random.nextInt(4) == 0) return ItemStack.EMPTY;
        return skullStack(ROBOT_SKULLS[serverLevel.random.nextInt(ROBOT_SKULLS.length)]);
    }

    private static ItemStack skullStack(String textureValue) {
        ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
        CompoundTag tag = new CompoundTag();
        CompoundTag owner = new CompoundTag();
        CompoundTag properties = new CompoundTag();
        net.minecraft.nbt.ListTag textures = new net.minecraft.nbt.ListTag();
        CompoundTag texture = new CompoundTag();
        texture.putString("Value", textureValue);
        textures.add(texture);
        properties.put("textures", textures);
        owner.put("Properties", properties);
        tag.put("SkullOwner", owner);
        skull.setTag(tag);
        return skull;
    }

    private void faceBoss(Mob mob, B8EncounterData data) {
        BlockPos core = data.anchor().offset(0, 7, 0);
        double dx = core.getX() - mob.getX();
        double dz = core.getZ() - mob.getZ();
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        mob.setYRot(yaw);
        mob.yHeadRot = yaw;
        mob.getPersistentData().putFloat(ADD_YAW_KEY, yaw);
    }

    private void tickAdds(ServerLevel server, B8EncounterData data) {
        BlockPos center = data.anchor();
        if (center == null) return;
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                center.offset(-40, -10, -40), center.offset(40, 30, 40));
        for (Mob add : server.getEntitiesOfClass(
                Mob.class, box, entity -> entity.isAlive() && entity.getTags().contains("b8_add"))) {
            CompoundTag persistent = add.getPersistentData();
            int age = persistent.getInt(ADD_AGE_KEY);
            String type = persistent.getString(ADD_TYPE_KEY);
            if (age < H3_SETUP_TICKS) {
                persistent.putInt(ADD_AGE_KEY, age + 1);
                double fx = 0.0D;
                double fy = 0.0D;
                double fz = 0.0D;
                if (!"b8_h3_enemigo2".equals(type)) {
                    double yaw = Math.toRadians(persistent.getFloat(ADD_YAW_KEY));
                    fx = -Math.sin(yaw) * 0.4D;
                    fz = Math.cos(yaw) * 0.4D;
                    // Taller jump arc per user feedback (2026-08-02): rise 10
                    // blocks over the first 10 ticks, fall 10 over the last 10.
                    if (age < 10) fy = 1.0D;
                    else fy = -1.0D;
                } else if (age >= 9 && age < 18) {
                    fy = 0.1D;
                }
                add.moveTo(add.getX() + fx, add.getY() + fy, add.getZ() + fz,
                        add.getYRot(), add.getXRot());
                // The teleport animation must not accumulate fall velocity, or
                // the adds would tunnel through the floor when it ends.
                add.setDeltaMovement(0.0D, 0.0D, 0.0D);
                if (age == 19 && ("b8_h3_golem".equals(type) || "b8_h3_enemigo3".equals(type))) {
                    spawnFlecha(server, add);
                }
            }
            if ("b8_h3_enemigo2".equals(type)) {
                int fire = persistent.getInt(ADD_FIRE_KEY) + 1;
                if (fire >= H3_SNIPER_FIRE_INTERVAL) {
                    fire = 0;
                    spawnSniperBullet(server, data, add);
                }
                persistent.putInt(ADD_FIRE_KEY, fire);
                if (server.getGameTime() % 2 == 0) {
                    server.sendParticles(ParticleTypes.LARGE_SMOKE,
                            add.getX(), add.getY(), add.getZ(), 0, 0.0D, -1.0D, 0.0D, 0.22D);
                    server.sendParticles(ParticleTypes.FLAME,
                            add.getX(), add.getY(), add.getZ(), 0, 0.0D, -1.0D, 0.0D, 0.22D);
                }
            }
            if ("b8_h3_golem".equals(type) && add instanceof IronGolem golem) {
                ServerPlayer nearest = nearestNonSpectator(server, golem.position());
                if (nearest != null) {
                    golem.setPersistentAngerTarget(nearest.getUUID());
                    golem.setTarget(nearest);
                }
                tickCrushingGolem(server, data, golem, age);
            }
        }
        cleanupOrphanedCrushingBars(server, box);
    }

    private void initializeCrushingCooldown(IronGolem golem, ServerLevel server) {
        CompoundTag persistent = golem.getPersistentData();
        if (persistent.contains(CRUSH_COOLDOWN_KEY)) return;
        int cooldown = 3;
        if (server.random.nextBoolean()) cooldown += 3;
        if (server.random.nextBoolean()) cooldown += 3;
        persistent.putInt(CRUSH_COOLDOWN_KEY, cooldown);
    }

    private void tickCrushingGolem(
            ServerLevel server, B8EncounterData data, IronGolem golem, int setupAge) {
        CompoundTag persistent = golem.getPersistentData();
        initializeCrushingCooldown(golem, server);
        int cast = persistent.getInt(CRUSH_CAST_KEY);
        if (cast > 0) {
            tickCrushingCast(server, data, golem, cast);
            return;
        }

        ServerPlayer nearby = nearestNonSpectator(server, golem.position());
        if (nearby != null && nearby.distanceToSqr(golem) <= 30.0D * 30.0D
                && server.getGameTime() % 20L == 0L) {
            persistent.putInt(CRUSH_COOLDOWN_KEY,
                    persistent.getInt(CRUSH_COOLDOWN_KEY) - 1);
        }
        if (setupAge < H3_SETUP_TICKS || persistent.getInt(CRUSH_COOLDOWN_KEY) > 0
                || !golem.onGround()) return;

        List<ServerPlayer> visible = server.getEntitiesOfClass(
                ServerPlayer.class, golem.getBoundingBox().inflate(9.0D),
                player -> !player.isSpectator() && player.distanceToSqr(golem) <= 81.0D
                        && golem.hasLineOfSight(player));
        if (visible.isEmpty()) {
            persistent.putInt(CRUSH_COOLDOWN_KEY, 5);
            return;
        }
        beginCrushingCast(server, data, golem,
                visible.get(server.random.nextInt(visible.size())));
    }

    private void beginCrushingCast(
            ServerLevel server, B8EncounterData data, IronGolem golem, ServerPlayer target) {
        Vec3 flat = target.getEyePosition().subtract(golem.getEyePosition()).multiply(1.0D, 0.0D, 1.0D);
        if (flat.lengthSqr() < 1.0E-6D) flat = new Vec3(0.0D, 0.0D, 1.0D);
        flat = flat.normalize();
        CompoundTag persistent = golem.getPersistentData();
        persistent.putInt(CRUSH_CAST_KEY, 50);
        persistent.putInt(CRUSH_COOLDOWN_KEY, 12);
        persistent.putDouble(CRUSH_DIR_X_KEY, flat.x);
        persistent.putDouble(CRUSH_DIR_Z_KEY, flat.z);
        golem.setNoAi(true);
        faceDirection(golem, flat);

        ArmorStand bar = new ArmorStand(EntityType.ARMOR_STAND, server);
        bar.setPos(golem.getX(), golem.getY() + 3.0D, golem.getZ());
        byte flags = bar.getEntityData().get(net.minecraft.world.entity.decoration.ArmorStand.DATA_CLIENT_FLAGS);
        bar.getEntityData().set(net.minecraft.world.entity.decoration.ArmorStand.DATA_CLIENT_FLAGS,
                (byte) (flags | 0x01 | 0x10)); // small + marker
        bar.setNoGravity(true);
        bar.setInvisible(true);
        bar.setInvulnerable(true);
        bar.setCustomNameVisible(true);
        bar.setCustomName(Component.translatable(
                "luisb1202.functions.afijos.aplastante.ini2.1"));
        bar.addTag("b8_h3_crushing_cast");
        bar.getPersistentData().putUUID(CRUSH_OWNER_KEY, golem.getUUID());
        server.addFreshEntity(bar);
        persistent.putUUID(CRUSH_STAND_KEY, bar.getUUID());
        data.addCleanup(bar.getUUID());
        server.playSound(null, golem.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(),
                SoundSource.MASTER, 0.1F, 2.0F);

        BlockPos anchor = data.anchor();
        if (anchor != null) {
            net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                    anchor.offset(-40, -10, -40), anchor.offset(40, 30, 40));
            for (IronGolem other : server.getEntitiesOfClass(
                    IronGolem.class, box, candidate -> candidate.isAlive()
                            && candidate.getTags().contains("afijo_aplastante"))) {
                if (other != golem) {
                    CompoundTag otherData = other.getPersistentData();
                    initializeCrushingCooldown(other, server);
                    otherData.putInt(CRUSH_COOLDOWN_KEY,
                            otherData.getInt(CRUSH_COOLDOWN_KEY) + 1);
                }
            }
        }
    }

    private void tickCrushingCast(
            ServerLevel server, B8EncounterData data, IronGolem golem, int cast) {
        CompoundTag persistent = golem.getPersistentData();
        Vec3 direction = new Vec3(
                persistent.getDouble(CRUSH_DIR_X_KEY), 0.0D,
                persistent.getDouble(CRUSH_DIR_Z_KEY));
        if (direction.lengthSqr() < 1.0E-6D) direction = new Vec3(0.0D, 0.0D, 1.0D);
        direction = direction.normalize();
        faceDirection(golem, direction);

        int remaining = cast - 1;
        persistent.putInt(CRUSH_CAST_KEY, remaining);
        ArmorStand bar = crushingBar(server, persistent);
        if (bar != null) {
            bar.moveTo(golem.getX(), golem.getY() + 3.0D, golem.getZ(),
                    golem.getYRot(), 0.0F);
        }
        String stageKey = switch (remaining) {
            case 40 -> "luisb1202.functions.afijos.aplastante.cast_1.1";
            case 30 -> "luisb1202.functions.afijos.aplastante.cast_2.1";
            case 20 -> "luisb1202.functions.afijos.aplastante.cast_3.1";
            case 10 -> "luisb1202.functions.afijos.aplastante.cast_4.1";
            default -> null;
        };
        if (stageKey != null) {
            if (bar != null) bar.setCustomName(Component.translatable(stageKey));
            server.playSound(null, golem.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.MASTER, 0.1F, 2.0F);
        }

        if (remaining >= 6 && remaining <= 9) {
            golem.moveTo(golem.getX(), golem.getY() + 0.6D, golem.getZ(),
                    golem.getYRot(), golem.getXRot());
        } else if (remaining >= 2 && remaining <= 5) {
            golem.moveTo(golem.getX(), golem.getY() - 0.6D, golem.getZ(),
                    golem.getYRot(), golem.getXRot());
        }
        golem.setDeltaMovement(Vec3.ZERO);

        if (remaining > 0) return;
        golem.setNoAi(false);
        persistent.remove(CRUSH_CAST_KEY);
        if (bar != null) bar.discard();
        persistent.remove(CRUSH_STAND_KEY);
        B8CrushingWaveEntity wave = B8CrushingWaveEntity.spawn(
                server, golem.position(), direction);
        data.addCleanup(wave.getUUID());
        server.playSound(null, golem.blockPosition(), SoundEvents.END_GATEWAY_SPAWN,
                SoundSource.MASTER, 1.0F, 2.0F);
    }

    private static void faceDirection(IronGolem golem, Vec3 direction) {
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        golem.setYRot(yaw);
        golem.yBodyRot = yaw;
        golem.yHeadRot = yaw;
    }

    private ArmorStand crushingBar(ServerLevel server, CompoundTag persistent) {
        if (!persistent.hasUUID(CRUSH_STAND_KEY)) return null;
        Entity entity = server.getEntity(persistent.getUUID(CRUSH_STAND_KEY));
        return entity instanceof ArmorStand stand ? stand : null;
    }

    private void cleanupOrphanedCrushingBars(
            ServerLevel server, net.minecraft.world.phys.AABB box) {
        for (ArmorStand stand : server.getEntitiesOfClass(
                ArmorStand.class, box, entity -> entity.getTags().contains("b8_h3_crushing_cast"))) {
            CompoundTag persistent = stand.getPersistentData();
            if (!persistent.hasUUID(CRUSH_OWNER_KEY)) {
                stand.discard();
                continue;
            }
            Entity owner = server.getEntity(persistent.getUUID(CRUSH_OWNER_KEY));
            if (!(owner instanceof IronGolem golem) || !golem.isAlive()
                    || golem.getPersistentData().getInt(CRUSH_CAST_KEY) <= 0) {
                stand.discard();
            }
        }
    }

    public void onAddDeath(ServerLevel server, LivingEntity victim, boolean playerKilled) {
        if (victim.getTags().contains("b8_h3_golem")) {
            CompoundTag persistent = victim.getPersistentData();
            ArmorStand bar = crushingBar(server, persistent);
            if (bar != null) bar.discard();
            persistent.remove(CRUSH_STAND_KEY);
            persistent.remove(CRUSH_CAST_KEY);
        }
        if (!playerKilled || !victim.getTags().contains("b8_h3_enemigo3")) return;
        for (int index = 0; index < 2; index++) {
            B8DetonatorBombEntity bomb = B8DetonatorBombEntity.spawn(
                    server, victim.position(), server.random.nextInt(24));
            data.addCleanup(bomb.getUUID());
        }
    }

    private void spawnFlecha(ServerLevel server, Mob add) {
        Snowball snowball = new Snowball(EntityType.SNOWBALL, server);
        snowball.setPos(add.getX(), add.getY() + 4.0D, add.getZ());
        snowball.setDeltaMovement(0.0D, -10.0D, 0.0D);
        snowball.addTag("b8_h3_arrow");
        ServerPlayer owner = nearestNonSpectator(server, add.position());
        if (owner != null) snowball.setOwner(owner);
        server.addFreshEntity(snowball);
    }

    private void spawnSniperBullet(ServerLevel server, B8EncounterData data, Mob sniper) {
        TerrastalkerRoverEntity target = nearestRover(server, data, sniper.position());
        Vec3 direction = target != null
                ? target.position().subtract(sniper.position()).normalize()
                : new Vec3(0.0D, 0.0D, 1.0D);
        server.sendParticles(ParticleTypes.EXPLOSION,
                sniper.getX() + direction.x, sniper.getY() + 1.4D, sniper.getZ() + direction.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.LAVA,
                sniper.getX() + direction.x, sniper.getY() + 1.4D, sniper.getZ() + direction.z,
                1, 0.0D, 0.0D, 0.0D, 2.0D);
        B8SniperBulletEntity bullet = B8SniperBulletEntity.spawn(
                server, sniper.position(), direction, data.anchor().getY());
        bullet.addTag("b8_h3_sniper_bala");
        data.addCleanup(bullet.getUUID());
        server.playSound(null, sniper.blockPosition(), SoundEvents.IRON_GOLEM_HURT,
                SoundSource.MASTER, 3.0F, 2.0F);
        server.playSound(null, sniper.blockPosition(), SoundEvents.PUFFER_FISH_BLOW_OUT,
                SoundSource.MASTER, 3.0F, 2.0F);
    }

    public boolean onSniperBulletTick(ServerLevel server, B8SniperBulletEntity bullet) {
        Vec3 sample = bullet.position().add(0.0D, B8SniperBulletEntity.SAMPLE_Y, 0.0D);
        BlockPos sampleBlock = BlockPos.containing(sample);
        boolean blockHit = !server.getBlockState(sampleBlock)
                .getCollisionShape(server, sampleBlock).isEmpty();
        boolean bulletNear = false;
        TerrastalkerRoverEntity target = null;
        for (TerrastalkerRoverEntity rover : encounterRovers(server, data)) {
            if (rover.position().distanceToSqr(sample) <= 4.0D) {
                target = rover;
            } else if (rover.hasBulletsNear(sample, 1.0D)) {
                bulletNear = true;
            }
        }
        if (!blockHit && !bulletNear && target == null) return false;
        if (target != null) target.damageEnergy(2);
        Vec3 pos = bullet.position();
        server.sendParticles(ParticleTypes.EXPLOSION,
                pos.x, pos.y + 0.1D, pos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.LAVA,
                pos.x, pos.y + 0.1D, pos.z, 1, 0.0D, 0.0D, 0.0D, 2.0D);
        for (int index = 0; index < 16; index++) {
            double angle = Math.toRadians(index * 22.5D);
            server.sendParticles(ParticleTypes.FLAME,
                    pos.x + Math.sin(angle), pos.y + 1.8D, pos.z + Math.cos(angle),
                    0, Math.sin(angle), 0.0D, Math.cos(angle), 0.1D);
        }
        server.playSound(null, BlockPos.containing(pos), SoundEvents.NETHERITE_BLOCK_STEP,
                SoundSource.MASTER, 1.0F, 1.8F);
        if (bulletNear) {
            server.playSound(null, BlockPos.containing(pos), SoundEvents.BLAZE_HURT,
                    SoundSource.MASTER, 2.0F, 0.8F);
        }
        bullet.discard();
        return true;
    }

    private TerrastalkerRoverEntity nearestRover(
            ServerLevel server, B8EncounterData data, Vec3 from) {
        TerrastalkerRoverEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (TerrastalkerRoverEntity rover : encounterRovers(server, data)) {
            double distance = from.distanceToSqr(rover.position());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = rover;
            }
        }
        return best;
    }

    private ServerPlayer nearestNonSpectator(ServerLevel server, Vec3 from) {
        ServerPlayer best = null;
        double bestDistance = Double.MAX_VALUE;
        for (ServerPlayer player : server.players()) {
            if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) continue;
            double distance = from.distanceToSqr(player.position());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = player;
            }
        }
        return best;
    }

    private int nonSpectatorCount(ServerLevel server) {
        int count = 0;
        for (ServerPlayer player : server.players()) {
            if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) count++;
        }
        return count;
    }

    private int countAdds(ServerLevel server, B8EncounterData data) {
        BlockPos center = data.anchor();
        if (center == null) return 0;
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                center.offset(-40, -10, -40), center.offset(40, 30, 40));
        return server.getEntitiesOfClass(Mob.class, box,
                entity -> entity.isAlive() && entity.getTags().contains("b8_add")).size();
    }

    private void recountAdds(ServerLevel server, B8EncounterData data) {
        data.setAddCount(countAdds(server, data));
    }

    private void h3Cleanup(ServerLevel server, B8EncounterData data) {
        BlockPos center = data.anchor();
        if (center == null) return;
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                center.offset(-40, -10, -40), center.offset(40, 30, 40));
        for (Entity entity : server.getEntitiesOfClass(Entity.class, box,
                candidate -> candidate.isAlive()
                        && (candidate.getTags().contains("b8_add")
                        || candidate.getTags().contains("b8_h3_sniper_bala")
                        || candidate.getTags().contains("b8_h3_detonator_bomb")
                        || candidate.getTags().contains("b8_h3_crushing_wave")
                        || candidate.getTags().contains("b8_h3_crushing_cast")))) {
            entity.discard();
        }
        data.setAddCount(0);
    }

    /* ------------------------------ M5 phases ------------------------------ */

    private void runPhase(ServerLevel server, B8EncounterData data) {
        int fase = data.fase();
        int ronda = data.ronda();
        int adds = data.addCount();
        switch (fase) {
            case 2, 3 -> {
                if (ronda < 3 && adds <= 3) advanceRound(server, data);
                else if (ronda == 3 && adds == 0) advanceRound(server, data);
            }
            case 4 -> {
                if (ronda < 3 && adds == 0) advanceRound(server, data);
                else if (ronda == 3 && adds <= 3) advanceRound(server, data);
                else if (ronda == 4 && adds == 0) advanceRound(server, data);
            }
            case 5 -> {
                if (ronda < 3 && adds == 0) advanceRound(server, data);
                else if (ronda >= 3 && ronda <= 6 && !acechadorExists(server, data)) {
                    advanceRound(server, data);
                }
            }
            default -> {
            }
        }
    }

    private void advanceRound(ServerLevel server, B8EncounterData data) {
        int ronda = data.ronda() + 1;
        data.setRonda(ronda);
        int fase = data.fase();
        long now = server.getGameTime();
        switch (fase) {
            case 2 -> {
                if (ronda <= 3) {
                    spawnAddWave(server, data, "zombie");
                } else if (ronda == 4) {
                    schedule(server, data, now, TIMER_H1_INI, 1);
                    schedule(server, data, now, TIMER_H1_INI, 120);
                    schedule(server, data, now, TIMER_H1_INI3, 240);
                    schedule(server, data, now, TIMER_H1_INI3, 360);
                    schedule(server, data, now, TIMER_H2_INI, 480);
                    schedule(server, data, now, TIMER_H1_INI, 620);
                    schedule(server, data, now, TIMER_H1_INI, 760);
                }
            }
            case 3 -> {
                switch (ronda) {
                    case 1 -> {
                        spawnAddWave(server, data, "sniper");
                        spawnAddWave(server, data, "sniper");
                        spawnAddWave(server, data, "sniper");
                    }
                    case 2 -> {
                        spawnAddWave(server, data, "zombie");
                        spawnAddWave(server, data, "sniper");
                        schedule(server, data, now, TIMER_H1_INI, 60);
                    }
                    case 3 -> {
                        spawnAddWave(server, data, "sniper");
                        spawnAddWave(server, data, "zombie");
                        schedule(server, data, now, TIMER_H1_INI3, 60);
                    }
                    case 4 -> {
                        schedule(server, data, now, TIMER_H4_INI, 1);
                        schedule(server, data, now, TIMER_H4_INI, 120);
                        schedule(server, data, now, TIMER_H4_INI, 240);
                        schedule(server, data, now, TIMER_H1_INI, 240);
                        schedule(server, data, now, TIMER_H4_INI, 360);
                        schedule(server, data, now, TIMER_H1_INI3, 360);
                        schedule(server, data, now, TIMER_H4_INI, 480);
                        schedule(server, data, now, TIMER_H1_INI3, 480);
                        schedule(server, data, now, TIMER_H2_INI, 600);
                        schedule(server, data, now, TIMER_H4_INI, 760);
                        schedule(server, data, now, TIMER_H4_INI, 920);
                    }
                    default -> {
                    }
                }
            }
            case 4 -> {
                switch (ronda) {
                    case 1 -> spawnAddWave(server, data, "golem");
                    case 2 -> {
                        spawnAddWave(server, data, "golem");
                        schedule(server, data, now, TIMER_H4_INI, 60);
                    }
                    case 3 -> {
                        spawnAddWave(server, data, "tnt");
                        spawnAddWave(server, data, "tnt");
                    }
                    case 4 -> {
                        spawnAddWave(server, data, "tnt");
                        spawnAddWave(server, data, "zombie");
                        schedule(server, data, now, TIMER_H1_INI3, 60);
                    }
                    case 5 -> {
                        schedule(server, data, now, TIMER_H4_INI, 1);
                        schedule(server, data, now, TIMER_H4_INI2, 120);
                        schedule(server, data, now, TIMER_H4_INI2, 260);
                        schedule(server, data, now, TIMER_H1_INI3, 260);
                        schedule(server, data, now, TIMER_H4_INI2, 420);
                        schedule(server, data, now, TIMER_H1_INI3, 420);
                        schedule(server, data, now, TIMER_H4_INI2, 560);
                        schedule(server, data, now, TIMER_H1_INI3, 560);
                        schedule(server, data, now, TIMER_H2_INI, 700);
                        schedule(server, data, now, TIMER_H4_INI, 840);
                        schedule(server, data, now, TIMER_H4_INI2, 980);
                    }
                    default -> {
                    }
                }
            }
            case 5 -> {
                switch (ronda) {
                    case 1 -> {
                        spawnAddWave(server, data, "golem");
                        spawnAddWave(server, data, "zombie");
                    }
                    case 2 -> {
                        spawnAddWave(server, data, "golem");
                        spawnAddWave(server, data, "sniper");
                        schedule(server, data, now, TIMER_H4_INI2, 60);
                    }
                    case 3, 4 -> acechadorRound(server, data, "ronda3");
                    case 5, 6 -> acechadorRound(server, data, "ronda5");
                    case 7 -> {
                        schedule(server, data, now, TIMER_H4_INI, 1);
                        schedule(server, data, now, TIMER_H4_INI2, 120);
                        schedule(server, data, now, TIMER_H4_INI3, 260);
                        schedule(server, data, now, TIMER_H4_INI3, 480);
                        schedule(server, data, now, TIMER_H4_INI3, 680);
                        schedule(server, data, now, TIMER_H1_INI3, 680);
                        schedule(server, data, now, TIMER_H2_INI, 800);
                    }
                    default -> {
                    }
                }
            }
            default -> {
            }
        }
    }

    private static void schedule(
            ServerLevel server, B8EncounterData data, long now, String type, long offsetTicks) {
        data.addTimer(new B8EncounterData.TimerEntry(type, now + offsetTicks));
    }

    private void acechadorRound(ServerLevel server, B8EncounterData data, String keySuffix) {
        // The hostile acechador is deferred; the source round plays a sound and
        // a tellraw message. Spawn the acechador here once implemented.
        String key = "luisb1202.functions.bossfight.b8.fase.5.ronda" + keySuffix + ".1";
        for (ServerPlayer player : server.players()) {
            server.playSound(null, player.blockPosition(), SoundEvents.EVOKER_PREPARE_ATTACK,
                    SoundSource.MASTER, 6.0F, 1.4F);
            player.sendSystemMessage(Component.translatable(key));
        }
    }

    private boolean acechadorExists(ServerLevel server, B8EncounterData data) {
        BlockPos center = data.anchor();
        if (center == null) return false;
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                center.offset(-40, -10, -40), center.offset(40, 30, 40));
        return !server.getEntitiesOfClass(Entity.class, box,
                entity -> entity.isAlive() && entity.getTags().contains("14_acechador_core")).isEmpty();
    }

    /* ------------------------------ M6 defeat flow ------------------------------ */

    public void onPlayerDeath(ServerLevel server, ServerPlayer player) {
        if (!data.active()) return;
        data.addSpectator(player.getUUID());
        if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
            player.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
        }
        BlockPos center = data.anchor();
        if (center != null) {
            player.teleportTo(server,
                    center.getX(), center.getY() + SPECTATOR_TP.y(), center.getZ(),
                    player.getYRot(), player.getXRot());
        }
        if (allPlayersSpectator(server)) {
            defeat(server, data);
        }
    }

    private boolean allPlayersSpectator(ServerLevel server) {
        for (ServerPlayer player : server.players()) {
            if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) return false;
        }
        return true;
    }

    private void defeat(ServerLevel server, B8EncounterData data) {
        if (data.state() == B8EncounterData.STATE_DEFEAT) return;
        data.setState(B8EncounterData.STATE_DEFEAT);
        for (ServerPlayer player : server.players()) {
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("luisb1202.functions.bossfight.b1.derrota.1")));
            player.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.translatable("luisb1202.functions.bossfight.b1.derrota.2")));
            server.playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH,
                    SoundSource.MASTER, 1.0F, 1.8F);
        }
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_RESPAWN, server.getGameTime() + 100));
    }

    private void respawn(ServerLevel server, B8EncounterData data) {
        BlockPos anchor = data.anchor();
        endEncounter(server, data);
        if (anchor == null) return;
        for (ServerPlayer player : server.players()) {
            if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
                player.teleportTo(server,
                        anchor.getX() + 14, anchor.getY() + 1, anchor.getZ(),
                        90.0F, 0.0F);
            }
            player.removeEffect(MobEffects.WITHER);
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, 2020, 1, false, false, false));
        }
        notifyPlayers(Component.literal(
                "B8 defeat: arena reset; use /finalparadox arena start b8 to retry."));
    }

    public void setHealth(int health) {
        if (!data.active()) return;
        int clamped = Math.max(0, Math.min(health, data.healthTotal()));
        data.setHealth(clamped);
        refreshBossBar();
        checkHealth();
    }

    private void checkHealth() {
        int health = data.health();
        if (health <= 0) {
            onDefeat();
            return;
        }
        if (health <= 200 && data.fase() < 2) enterPhase(2);
        else if (health <= 150 && data.fase() < 3) enterPhase(3);
        else if (health <= 100 && data.fase() < 4) enterPhase(4);
        else if (health <= 50 && data.fase() < 5) enterPhase(5);
    }

    private void enterPhase(int phase) {
        data.setState(B8EncounterData.STATE_PHASE_1 + phase - 1);
        data.setFase(phase);
        data.setRonda(0);
        // A phase switch must never let an active H2 cycle reopen the matrix.
        h2CleanupModules(serverLevel, data);
        closeMatrix();
        String subtitle = phase == 5
                ? "luisb1202.functions.bossfight.b3.fase.5.ini.1"
                : "luisb1202.functions.bossfight.b8.fase." + phase + ".ini.1";
        for (ServerPlayer player : serverLevel.players()) {
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("luisb1202.functions.bossfight.b1.ini_f1.1")));
            player.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.translatable(subtitle)));
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                    SoundSource.MASTER, 1.0F, 1.5F);
        }
        dialogue(serverLevel, 7);
        // M5: phase timelines (add waves, hazard scheduling) land here.
        notifyPlayers(Component.literal("B8 entered phase " + phase + " (matrix closed and invulnerable)."));
    }

    public void forcePhase(int phase) {
        if (!data.active() || data.state() < B8EncounterData.STATE_PHASE_1) return;
        enterPhase(phase);
    }

    public void setVulnerable(boolean vulnerable) {
        if (!data.active()) return;
        data.setVulnerable(vulnerable);
        ZombieSupermatrixEntity matrix = matrix();
        if (matrix != null) matrix.setVulnerable(vulnerable);
    }

    private void openMatrix() {
        setVulnerable(true);
    }

    private void closeMatrix() {
        setVulnerable(false);
    }

    private void onDefeat() {
        if (data.state() == B8EncounterData.STATE_VICTORY) return;
        // Source b8/explosion -> musica/abatir_boss: switch to the victory
        // track at the moment of death and cancel the pending B8 loop.
        cancelPendingMusicLoop(data);
        stopRecords(serverLevel);
        playRecord(serverLevel, ModSounds.ABATIR_JEFE.get());
        // Source b8/explosion: 0 health -> gold/cloud/lightning burst at the
        // core, then victory two seconds later.
        ZombieSupermatrixEntity matrix = matrix();
        if (matrix != null) {
            emitVictoryExplosion(serverLevel, matrix.position());
            matrix.discard();
        }
        BlockPos anchor = data.anchor();
        if (anchor != null) {
            net.minecraft.world.entity.LightningBolt bolt =
                    EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (bolt != null) {
                bolt.moveTo(anchor.getX() + 0.5D, anchor.getY() + 8.0D, anchor.getZ() + 0.5D);
                serverLevel.addFreshEntity(bolt);
            }
        }
        data.setState(B8EncounterData.STATE_VICTORY);
        data.addTimer(new B8EncounterData.TimerEntry(TIMER_VICTORY, serverLevel.getGameTime() + 40));
    }

    private void emitVictoryExplosion(ServerLevel server, Vec3 center) {
        for (int index = 0; index < 64; index++) {
            double angle = Math.toRadians(5.625D * (index + 1));
            server.sendParticles(ParticleTypes.CLOUD,
                    center.x, center.y, center.z, 0,
                    Math.sin(angle), 0.0D, Math.cos(angle), 1.0D);
        }
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.CLOUD,
                center.x, center.y, center.z, 20, 0.0D, 0.0D, 0.0D, 1.0D);
        server.sendParticles(new ItemParticleOption(
                        ParticleTypes.ITEM, new ItemStack(Items.GOLD_BLOCK)),
                center.x, center.y, center.z, 200, 0.0D, 0.0D, 0.0D, 1.0D);
        server.playSound(null, BlockPos.containing(center), SoundEvents.END_PORTAL_SPAWN,
                SoundSource.MASTER, 4.0F, 1.5F);
    }

    private void victory(ServerLevel server, B8EncounterData data) {
        for (ServerPlayer player : server.players()) {
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("luisb1202.functions.bossfight.b1.victoria.1")));
            player.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.translatable("luisb1202.functions.bossfight.b8.victoria.1")));
            server.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                    SoundSource.MASTER, 1.0F, 0.8F);
            server.playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH,
                    SoundSource.MASTER, 0.7F, 1.4F);
        }
        dialogue(server, 4);
        BlockPos anchor = data.anchor();
        if (anchor != null) {
            for (ServerPlayer player : server.players()) {
                if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                    player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
                    player.teleportTo(server,
                            anchor.getX() + 14, anchor.getY() + 1, anchor.getZ(),
                            90.0F, 0.0F);
                }
                player.setRespawnPosition(server.dimension(), server.getSharedSpawnPos(),
                        0.0F, false, false);
            }
            spawnReward(server, anchor);
            spawnCelebration(server, anchor);
        }
        endEncounter(server, data);
        notifyPlayers(Component.literal("B8 victory! Reward dropped at the arena."));
    }

    private void spawnReward(ServerLevel server, BlockPos anchor) {
        BlockPos pos = anchor.offset(-33, 2, 0);
        ItemStack stack = new ItemStack(ModItems.ADAPTIVE_DEFENSE_MATRIX.get());
        ItemEntity item = new ItemEntity(server,
                pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, stack);
        item.setDeltaMovement(0.0D, 0.5D, 0.0D);
        item.setPickUpDelay(10);
        server.addFreshEntity(item);
        server.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.MASTER, 1.0F, 2.0F);
        server.sendParticles(ParticleTypes.EXPLOSION,
                pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private void spawnCelebration(ServerLevel server, BlockPos anchor) {
        int[] palette = {0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0x5555FF, 0xFF55FF};
        for (int index = 0; index < 6; index++) {
            ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
            CompoundTag tag = new CompoundTag();
            CompoundTag fireworks = new CompoundTag();
            net.minecraft.nbt.ListTag explosions = new net.minecraft.nbt.ListTag();
            CompoundTag explosion = new CompoundTag();
            explosion.putInt("Colors", palette[server.random.nextInt(palette.length)]);
            explosions.add(explosion);
            fireworks.put("Explosions", explosions);
            fireworks.putByte("Flight", (byte) 2);
            tag.put("Fireworks", fireworks);
            firework.setTag(tag);
            FireworkRocketEntity rocket = new FireworkRocketEntity(server,
                    anchor.getX() + 0.5D + (server.random.nextInt(21) - 10),
                    anchor.getY() + 8.0D,
                    anchor.getZ() + 0.5D + (server.random.nextInt(21) - 10),
                    firework);
            server.addFreshEntity(rocket);
        }
    }

    /** Source b8/omitir: admin skip with the same reward and cleanup. */
    public void skip(ServerLevel server, B8EncounterData data) {
        for (ServerPlayer player : server.players()) {
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("luisb1202.functions.bossfight.b8.omitir.2")));
            player.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.translatable("luisb1202.functions.bossfight.b8.omitir.3")));
            server.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                    SoundSource.MASTER, 1.0F, 0.8F);
            server.playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH,
                    SoundSource.MASTER, 0.7F, 1.4F);
        }
        BlockPos anchor = data.anchor();
        if (anchor != null) {
            for (ServerPlayer player : server.players()) {
                if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                    player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
                    player.teleportTo(server,
                            anchor.getX() + 14, anchor.getY() + 1, anchor.getZ(),
                            90.0F, 0.0F);
                }
                player.setRespawnPosition(server.dimension(), server.getSharedSpawnPos(),
                        0.0F, false, false);
            }
            spawnReward(server, anchor);
        }
        endEncounter(server, data);
        notifyPlayers(Component.literal("B8 boss skipped; reward dropped."));
    }

    /* ------------------------------ M8 music and dialogue ------------------------------ */

    private void playB8Music(ServerLevel server) {
        playRecord(server, ModSounds.B8_ABORDO_LOOP.get());
        data.addTimer(new B8EncounterData.TimerEntry(
                TIMER_MUSIC_LOOP, server.getGameTime() + MUSIC_LOOP_INTERVAL));
    }

    private void cancelPendingMusicLoop(B8EncounterData data) {
        List<B8EncounterData.TimerEntry> pending = new ArrayList<>();
        for (B8EncounterData.TimerEntry timer : data.timers()) {
            if (TIMER_MUSIC_LOOP.equals(timer.type())) pending.add(timer);
        }
        for (B8EncounterData.TimerEntry timer : pending) data.removeTimer(timer);
    }

    private void playRecord(ServerLevel server, SoundEvent event) {
        for (ServerPlayer player : server.players()) {
            player.playNotifySound(event, SoundSource.RECORDS, 999999.0F, 1.0F);
        }
    }

    private void stopRecords(ServerLevel server) {
        for (ServerPlayer player : server.players()) {
            player.connection.send(new ClientboundStopSoundPacket(null, SoundSource.RECORDS));
        }
    }

    private void dialogue(ServerLevel server, int index) {
        switch (index) {
            case 1 -> showDialogue(server, SPEAKER_KOROS, DIALOGUE_BASE + "dia1.1",
                    true, SoundEvents.TRIDENT_RETURN, 1.7F);
            case 2 -> showDialogue(server, SPEAKER_KOROS, DIALOGUE_BASE + "dia2.1",
                    true, SoundEvents.TRIDENT_RETURN, 1.7F);
            case 3 -> showDialogue(server, SPEAKER_KOROS, DIALOGUE_BASE + "dia3.1",
                    true, SoundEvents.TRIDENT_RETURN, 1.7F);
            case 4 -> showDialogue(server, SPEAKER_MATRIX, DIALOGUE_BASE + "dia4.2",
                    false, SoundEvents.ENDERMAN_AMBIENT, 0.0F);
            case 5 -> showDialogue(server, SPEAKER_MATRIX, DIALOGUE_BASE + "dia5.1",
                    false, SoundEvents.ENDERMAN_AMBIENT, 0.0F);
            case 6 -> showDialogue(server, SPEAKER_MATRIX, DIALOGUE_BASE + "dia6.1",
                    false, SoundEvents.ENDERMAN_AMBIENT, 0.0F);
            case 7 -> showDialogue(server, SPEAKER_MATRIX, DIALOGUE_BASE + "dia7.1",
                    false, SoundEvents.ENDERMAN_AMBIENT, 0.0F);
            case 8 -> showDialogue(server, SPEAKER_MATRIX, DIALOGUE_BASE + "dia8.1",
                    false, SoundEvents.ENDERMAN_AMBIENT, 0.0F);
            default -> {
            }
        }
    }

    private void showDialogue(
            ServerLevel server, String speakerKey, String messageKey,
            boolean korosStyle, SoundEvent sound, float pitch) {
        Component speaker = korosStyle
                ? Component.translatable(speakerKey)
                .withStyle(style -> style.withBold(true).withItalic(true)
                        .withColor(TextColor.fromRgb(0xFBBDFF)))
                : Component.translatable(speakerKey);
        for (ServerPlayer player : server.players()) {
            player.sendSystemMessage(speaker.copy().append(Component.translatable(messageKey)));
            server.playSound(null, player.blockPosition(), sound, SoundSource.MASTER, 1.0F, pitch);
        }
    }

    private void frasesH1(ServerLevel server) {
        String key = DIALOGUE_BASE + "frases_h1." + (1 + server.random.nextInt(6));
        showDialogue(server, SPEAKER_MATRIX, key, false, SoundEvents.ENDERMAN_AMBIENT, 0.0F);
    }

    /**
     * Evaluates one rover bullet against the matrix hit zone from the bullet's
     * current server position. Vulnerable: bullets within 1.5 blocks hit and
     * deal 1 damage (once per tick). Invulnerable: bullets within 3.0 blocks
     * are intercepted and removed with the block feedback.
     */
    public int testBullet(Vec3 bulletPosition) {
        if (!data.active()
                || data.state() < B8EncounterData.STATE_PHASE_1
                || data.state() > B8EncounterData.STATE_PHASE_5) {
            return BULLET_MISS;
        }
        ZombieSupermatrixEntity matrix = matrix();
        if (matrix == null) return BULLET_MISS;
        Vec3 center = hitCenter(matrix);
        double distanceSqr = bulletPosition.distanceToSqr(center);
        if (data.vulnerable()) {
            if (distanceSqr <= HIT_RADIUS_SQ) {
                performHit(serverLevel, center);
                return BULLET_HIT;
            }
        } else if (distanceSqr <= BLOCK_RADIUS_SQ) {
            performBlock(serverLevel, bulletPosition);
            return BULLET_BLOCKED;
        }
        return BULLET_MISS;
    }

    public Vec3 bulletHitCenter() {
        ZombieSupermatrixEntity matrix = matrix();
        return matrix != null ? hitCenter(matrix) : Vec3.ZERO;
    }

    private static Vec3 hitCenter(ZombieSupermatrixEntity matrix) {
        return matrix.position().add(0.0D, HIT_CENTER_LOCAL_Y, 0.0D);
    }

    private void performHit(ServerLevel server, Vec3 center) {
        // The source hit.mcfunction runs once per tick: one damage for every
        // bullet inside the 1.5-block zone in that tick.
        long now = server.getGameTime();
        if (now == lastHitTick) return;
        lastHitTick = now;
        BlockPos block = BlockPos.containing(center);
        server.sendParticles(new BlockParticleOption(
                        ParticleTypes.BLOCK, Blocks.GOLD_BLOCK.defaultBlockState()),
                center.x, center.y, center.z, 20, 0.5D, 0.5D, 0.5D, 0.3D);
        server.sendParticles(ParticleTypes.CLOUD,
                center.x, center.y, center.z, 1, 0.5D, 0.5D, 0.5D, 0.3D);
        server.sendParticles(ParticleTypes.EXPLOSION,
                center.x, center.y, center.z, 1, 0.6D, 0.6D, 0.6D, 0.0D);
        server.playSound(null, block, SoundEvents.GILDED_BLACKSTONE_BREAK,
                SoundSource.MASTER, 4.0F, 0.0F);
        server.playSound(null, block, SoundEvents.GILDED_BLACKSTONE_BREAK,
                SoundSource.MASTER, 4.0F, 1.0F);
        // In the encounter the rover autocannon fires at the original 3-tick
        // cadence, so each matrix hit returns to 1 (original fight pace).
        setHealth(data.health() - 1);
    }

    private void performBlock(ServerLevel server, Vec3 bulletPosition) {
        BlockPos block = BlockPos.containing(bulletPosition);
        server.playSound(null, block, SoundEvents.ANVIL_LAND,
                SoundSource.MASTER, 4.0F, 2.0F);
        server.sendParticles(ParticleTypes.CLOUD,
                bulletPosition.x, bulletPosition.y, bulletPosition.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.CRIT,
                bulletPosition.x, bulletPosition.y, bulletPosition.z, 10, 0.0D, 0.0D, 0.0D, 0.5D);
    }

    private void setupBossBar(ServerLevel server) {
        refreshBossBar();
        for (ServerPlayer player : server.players()) {
            bossEvent.addPlayer(player);
        }
    }

    private void refreshBossBar() {
        int percent = Math.max(0, Math.min(100, data.health() * 100 / data.healthTotal()));
        if (percent == lastBossValue) return;
        lastBossValue = percent;
        bossEvent.setProgress(percent / 100.0F);
    }

    private ZombieSupermatrixEntity matrix() {
        UUID uuid = data.matrixUuid();
        if (uuid == null) return null;
        Entity entity = serverLevel.getEntity(uuid);
        return entity instanceof ZombieSupermatrixEntity matrix ? matrix : null;
    }

    public void endEncounter(ServerLevel server, B8EncounterData data) {
        // The victory track (started at boss death) keeps playing after a
        // win, matching the source reset that does not stop records.
        boolean endedInVictory = data.state() == B8EncounterData.STATE_VICTORY;
        ZombieSupermatrixEntity matrix = matrix();
        if (matrix != null) matrix.discard();
        for (UUID uuid : data.cleanupEntities()) {
            Entity entity = server.getEntity(uuid);
            if (entity != null) entity.discard();
        }
        bossEvent.removeAllPlayers();
        data.clearTimers();
        h2CleanupModules(server, data);
        clearH1(server, data);
        clearH4(server, data);
        h3Cleanup(server, data);
        forceChunks(server, data, false);
        restoreGameRules(server, data);
        ArenaDeploymentData.get(server).clearActiveBoss();
        data.setActive(false);
        data.setState(B8EncounterData.STATE_IDLE);
        data.setMatrixUuid(null);
        data.clearParticipants();
        data.clearSpectators();
        data.clearMounts();
        data.clearCleanup();
        if (!endedInVictory) stopRecords(server);
        lastBossValue = -1;
    }

    public String status() {
        return "state=" + stateName(data.state())
                + ", fase=" + data.fase()
                + ", ronda=" + data.ronda()
                + ", health=" + data.health() + "/" + data.healthTotal()
                + ", vulnerable=" + data.vulnerable()
                + ", addCount=" + data.addCount()
                + ", matrix=" + (data.matrixUuid() != null ? data.matrixUuid() : "none")
                + ", participants=" + data.participantCount()
                + ", mounts=" + data.mountOrder().size()
                + ", h2=" + (data.h2Active()
                        ? "active(pending=" + data.h2PendingSpawns() + ")"
                        : "off")
                + ", h1=" + (data.h1Active() ? "active(" + data.h1Beams().size() + ")" : "off")
                + ", h4=" + (data.h4Active() ? "active(" + data.h4Sources().size() + ")" : "off")
                + ", timers=" + data.timers().size()
                + ", anchor=" + (data.anchor() != null ? data.anchor() : "none");
    }

    private static String stateName(int state) {
        return switch (state) {
            case B8EncounterData.STATE_IDLE -> "IDLE";
            case B8EncounterData.STATE_COUNTDOWN -> "COUNTDOWN";
            case B8EncounterData.STATE_PHASE_1 -> "PHASE_1";
            case B8EncounterData.STATE_PHASE_2 -> "PHASE_2";
            case B8EncounterData.STATE_PHASE_3 -> "PHASE_3";
            case B8EncounterData.STATE_PHASE_4 -> "PHASE_4";
            case B8EncounterData.STATE_PHASE_5 -> "PHASE_5";
            case B8EncounterData.STATE_VICTORY -> "VICTORY";
            case B8EncounterData.STATE_DEFEAT -> "DEFEAT";
            case B8EncounterData.STATE_CLEANUP -> "CLEANUP";
            default -> "UNKNOWN(" + state + ")";
        };
    }

    private void notifyPlayers(Component message) {
        for (ServerPlayer player : serverLevel.players()) {
            player.sendSystemMessage(message);
        }
    }

    private void forceChunks(ServerLevel server, B8EncounterData data, boolean force) {
        if (data.anchor() == null) return;
        net.minecraft.world.level.ChunkPos min = new net.minecraft.world.level.ChunkPos(
                data.anchor().offset(FORCELOAD_MIN_X, 0, FORCELOAD_MIN_Z));
        net.minecraft.world.level.ChunkPos max = new net.minecraft.world.level.ChunkPos(
                data.anchor().offset(FORCELOAD_MAX_X, 0, FORCELOAD_MAX_Z));
        for (int x = min.x; x <= max.x; x++) {
            for (int z = min.z; z <= max.z; z++) {
                server.setChunkForced(x, z, force);
            }
        }
    }

    private void snapshotGameRules(ServerLevel server, B8EncounterData data) {
        GameRules rules = server.getGameRules();
        data.setRuleImmediateRespawn(rules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN));
        data.setRuleMobGriefing(rules.getBoolean(GameRules.RULE_MOBGRIEFING));
        data.setRuleMobSpawning(rules.getBoolean(GameRules.RULE_DOMOBSPAWNING));
        data.setRuleKeepInventory(rules.getBoolean(GameRules.RULE_KEEPINVENTORY));
        data.setRuleFireTick(rules.getBoolean(GameRules.RULE_DOFIRETICK));
        data.setRuleNaturalRegeneration(rules.getBoolean(GameRules.RULE_NATURAL_REGENERATION));
        data.setRuleRandomTickSpeed(rules.getInt(GameRules.RULE_RANDOMTICKING));
    }

    private void applyBossGameRules(ServerLevel server, B8EncounterData data) {
        // Mirrors bossfight/boss_gamerules.mcfunction.
        server.getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(true, server.getServer());
        server.getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(false, server.getServer());
        server.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server.getServer());
        server.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, server.getServer());
        server.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(false, server.getServer());
        server.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, server.getServer());
        server.getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(true, server.getServer());
    }

    private void restoreGameRules(ServerLevel server, B8EncounterData data) {
        server.getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(data.ruleImmediateRespawn(), server.getServer());
        server.getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(data.ruleMobGriefing(), server.getServer());
        server.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(data.ruleMobSpawning(), server.getServer());
        server.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(data.ruleKeepInventory(), server.getServer());
        server.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(data.ruleFireTick(), server.getServer());
        server.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(data.ruleRandomTickSpeed(), server.getServer());
        server.getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(data.ruleNaturalRegeneration(), server.getServer());
    }

    private record Vec3iOffset(int x, int y, int z) {
    }
}
