package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Faithful B5 dual-fight controller, ported from the Final Paradox v1.1.15
 * datapack `bossfight/b5/**` command chains. Durable state lives in
 * {@link B5EncounterData}; transient visuals (armor stands, projectiles,
 * illusions, dialogue queue) are runtime-only.
 *
 * <p>Coordinates are relative to the arena floor anchor (player spawn
 * -1107 49 1426, anchor -1107 48 1426).
 */
public final class B5EncounterController {
    public static final int FASE_1 = 1;
    public static final int INTER_1 = 2;
    public static final int FASE_2 = 3;
    public static final int INTER_2 = 4;
    public static final int FASE_3 = 5;
    public static final int FASE_4 = 6;

    private static final int RUN_INTERVAL = 20;
    private static final int H5_TRIGGER = 35;
    private static final int H3_TRIGGER = 35;
    private static final int H7_TRIGGER = 14;

    private static final int SHIELD_KOYO = 0;
    private static final int SHIELD_GARI = 1;
    private static final int SHIELD_NONE = -1;
    private static final int[] FINALE_STAGE_TICKS = {
            40, 140, 240, 360, 460, 560, 620, 700, 800,
            880, 980, 1120, 1200, 1280, 1360, 1500, 1580
    };

    private B5EncounterData data;
    private ServerLevel serverLevel;
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<ArmorStand> h3Markers = new ArrayList<>();
    private final List<ArmorStand> h3GroundTridents = new ArrayList<>();
    private final List<ArmorStand> locoStands = new ArrayList<>();
    private ArmorStand h2Bomb;
    private ArmorStand h2Vision;
    private final List<Poza> pozas = new ArrayList<>();
    private final List<Trail> trails = new ArrayList<>();
    private final List<Illusion> illusions = new ArrayList<>();
    private ArmorStand venenoAs;
    private ArmorStand trustMarker;
    private UUID trustMarkerOwner;
    private final Map<String, Integer> playerColors = new HashMap<>();
    private final List<Dialogue> dialogueQueue = new ArrayList<>();
    private final List<Integer> pendingH6 = new ArrayList<>();
    private final List<Integer> pendingH7 = new ArrayList<>();
    private final int[] h5FireAt = new int[3];
    private int shieldFrame;
    private int h2ParticleFrame;
    private int h5ParticleFrame;
    private int warnCooldown;
    private int h4TimerTicks;
    private int trustColorFrame;
    private long trustHintReadyAt;
    private boolean trustConfident;
    private boolean trustStateInitialized;
    private boolean h4ProjectilesActive;
    private int h4ProjectileRunTicks;
    private UUID lastH2Target;
    private boolean leaveFleccyOpen;
    private boolean h5KoyoDialoguePlayed;
    private Vec3 realIllusionPos;

    /* ------------------------------ public entry ------------------------------ */

    /** ini.mcfunction: arena setup, music and 6-second countdown. */
    public void prepare(ServerLevel server, B5EncounterData data, KoyomiBossEntity koyo, GariBossEntity gari) {
        this.data = data;
        this.serverLevel = server;
        data.setActive(true);
        data.setFase(0);
        data.setCountdownTicks(0);
        data.setH2PendingDue(0L);
        data.setKoyoUuid(koyo.getUUID());
        data.setGariUuid(gari.getUUID());
        for (ServerPlayer player : server.players()) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 101, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 100));
            double rx = player.getX() - data.anchor().getX();
            double rz = player.getZ() - data.anchor().getZ();
            if (rx < -58 || rx > 14 || rz < -21 || rz > 21) {
                Vec3 inside = rel(data, 3, 1, 0);
                player.teleportTo(inside.x, inside.y, inside.z);
            }
        }
        leaveFleccyOpen = false;
        data.clearH5Hits();
        data.clearH3IntermissionHits();
        h5KoyoDialoguePlayed = false;
        closeFleccy(server, data);
        placeBarriers(server, data);
        startMusicMain(server, data);
        data.setDirty();
    }

    /** Restores a safe deterministic checkpoint after a server/controller reload. */
    public void recover(ServerLevel server, B5EncounterData data) {
        this.data = data;
        this.serverLevel = server;
        boolean restartActiveH2 = data.h2Active();
        boolean migrateMissingH2Schedule = !data.h2ScheduleTracked();
        clearTrustMarker();
        cleanupPersistedTransientEntities(server, data);
        projectiles.clear();
        dialogueQueue.clear();
        pendingH6.clear();
        pendingH7.clear();
        data.setH2Active(false);
        data.setH3Active(false);
        data.setH3Remaining(0);
        data.setLocoRemaining(0);
        data.setH5VolleyActive(false);
        data.setH6Active(false);
        data.setH7Active(false);
        data.setPoisonActive(false);
        data.setIllusionActive(false);
        if ((restartActiveH2 || (migrateMissingH2Schedule && data.h2PendingDue() <= 0L))
                && (data.fase() == FASE_1 || data.fase() == FASE_2 || data.fase() == FASE_3)
                && data.shieldBearer() == SHIELD_GARI) {
            data.setH2PendingDue(server.getGameTime() + (restartActiveH2 ? 1L : 100L));
        } else if (migrateMissingH2Schedule) {
            data.setH2PendingDue(data.h2PendingDue());
        }
        h4ProjectilesActive = false;
        h5FireAt[0] = h5FireAt[1] = h5FireAt[2] = 0;
        if (data.fase() == 0) {
            ensureCountdownBosses(server, data);
        } else if (data.fase() == INTER_1 || data.fase() == INTER_2) {
            ensureGariForIntermissionRecovery(server, data);
            startIntermission(server, data, data.fase());
        }
    }

    /** summon.mcfunction: phase 1 setup, shield to Gari, dialogue. */
    public void start(ServerLevel server, B5EncounterData data) {
        data.setFase(FASE_1);
        data.setRunTicks(0);
        data.setH5Counter(25);   // fase/1/ini: h5=25
        data.setH3Counter(5);    // fase/1/ini: h3=5
        data.setH7Counter(0);
        data.setShieldChanges(0);
        data.setShieldBearer(SHIELD_KOYO); // summon gives Koyomi the shield before h1/switch.
        data.setInterTicks(0);
        data.setInterEvent(0);
        data.setH2Active(false);
        data.setH3Active(false);
        data.setH5VolleyActive(false);
        data.setH6Active(false);
        data.setH7Active(false);
        data.setPoisonActive(false);
        data.setIllusionActive(false);
        data.setH4Timer(-1);
        data.setH4DamagePhase(false);
        data.setFinaleTicks(0);
        data.setDialogueCounter(0);
        data.setRespawnScheduled(false);
        data.setVictoryPlayed(false);
        KoyomiBossEntity koyo = koyo(server, data);
        GariBossEntity gari = gari(server, data);
        if (koyo != null) {
            koyo.setNoAi(false);
            koyo.setInvulnerable(false);
            koyo.setBossBarEnabled(true);
        }
        if (gari != null) {
            gari.setNoAi(false);
            gari.setInvulnerable(false);
            gari.setBossBarEnabled(true);
        }
        switchShield(server, data); // h1/switch: Koyomi -> Gari and schedule h2/ini after 5 seconds.
        broadcastTitle("luisb1202.functions.bossfight.b1.ini_f1.1");
        broadcastSubtitle("luisb1202.functions.bossfight.b1.ini_f1.2");
        playForAll(server, SoundEvents.NOTE_BLOCK_PLING.value(), 1.5F);
        playForAll(server, SoundEvents.ENDER_DRAGON_GROWL, 1.5F);
        data.setDirty();
    }

    public void tick(ServerLevel server, B5EncounterData data) {
        this.data = data;
        this.serverLevel = server;
        tickMusic(server, data);
        tickDialogueQueue(server, data);
        if (data.fase() == 0) {
            clearTrustMarker();
            tickCountdown(server, data);
            return;
        }
        KoyomiBossEntity koyo = koyo(server, data);
        GariBossEntity gari = gari(server, data);
        if (data.fase() == INTER_1 || data.fase() == INTER_2) {
            clearTrustMarker();
            // h1/run remains scheduled throughout both intermissions in the datapack.
            // Gari therefore keeps the visible shield ring while the intermission
            // mechanics are running.
            tickShieldRing(server, data, koyo);
            tickIntermission(server, data, koyo, gari);
            return;
        }
        if (data.fase() == FASE_4) {
            clearTrustMarker();
            tickFinale(server, data);
            return;
        }
        tickTrustMarker(server, data, koyo, gari);
        tickShieldRing(server, data, koyo);
        tickProjectiles(server, data);
        data.setRunTicks(data.runTicks() + 1);
        if (data.runTicks() % RUN_INTERVAL == 0) {
            runPhase(server, data, koyo, gari);
        }
        tickAttacks(server, data, koyo, gari);
        playerLoopTail(server, data);
    }

    private void tickCountdown(ServerLevel server, B5EncounterData data) {
        int ticks = data.countdownTicks() + 1;
        data.setCountdownTicks(ticks);
        if (ticks == 1) {
            sendDialogueNow(server, dialogueGari("luisb1202.functions.bossfight.b5.dialogos.dia16.1"),
                    SoundEvents.PILLAGER_AMBIENT, 1.2F);
        }
        if (ticks == 50) {
            sendDialogueNow(server, dialogueKoyo("luisb1202.functions.bossfight.b5.dialogos.dia16.2"),
                    SoundEvents.PILLAGER_AMBIENT, 1.7F);
        }
        if (ticks == 60) countdownTitle(server, "luisb1202.functions.bossfight.b1.cuenta_atras.3.1");
        if (ticks == 80) countdownTitle(server, "luisb1202.functions.afijos.detonante.2.1");
        if (ticks == 100) countdownTitle(server, "luisb1202.functions.afijos.detonante.1.2");
        if (ticks >= 120) start(server, data);
    }

    private void playerLoopTail(ServerLevel server, B5EncounterData data) {
        boolean anyNonSpectator = false;
        for (ServerPlayer player : server.players()) {
            if (data.isDeadPlayer(player.getUUID())) {
                if (player.isAlive() && !player.isSpectator()) player.setGameMode(GameType.SPECTATOR);
                if (player.isAlive()) {
                    Vec3 tp = rel(data, -1, 7, 0);
                    player.teleportTo(tp.x, tp.y, tp.z);
                }
            } else if (player.isSpectator()) {
                Vec3 tp = rel(data, -1, 7, 0);
                player.teleportTo(tp.x, tp.y, tp.z);
            } else if (player.isAlive()) {
                anyNonSpectator = true;
            }
        }
        if (!anyNonSpectator) {
            defeat(server, data);
        }
        if (data.respawnScheduled()) {
            data.setRespawnTicks(data.respawnTicks() + 1);
            if (data.respawnTicks() >= 100) {
                respawn(server, data);
            }
        }
    }

    /* ------------------------------ phase machine ------------------------------ */

    private void runPhase(ServerLevel server, B5EncounterData data,
                          KoyomiBossEntity koyo, GariBossEntity gari) {
        int fase = data.fase();
        if (fase == FASE_1 || fase == FASE_2 || fase == FASE_3) {
            if (koyo == null || koyo.isRemoved()) reviveKoyo(server, data);
            if (gari == null || gari.isRemoved()) reviveGari(server, data);
            koyo = koyo(server, data);
            gari = gari(server, data);
        }
        if (koyo == null || gari == null) return;
        double koyoPct = percent(koyo, 920.0D);
        double gariPct = percent(gari, 800.0D);
        boolean koyoShielded = data.shieldBearer() == SHIELD_KOYO;
        boolean gariShielded = data.shieldBearer() == SHIELD_GARI;

        if (fase == FASE_1) {
            if (koyoShielded) data.setH5Counter(data.h5Counter() + 1);
            if (data.h5Counter() >= H5_TRIGGER) startH5(server, data);
            if (koyoPct <= 68.0D && data.shieldChanges() == 1) switchShield(server, data);
            if (gariPct <= 68.0D) {
                startIntermission(server, data, INTER_1);
                return;
            }
        } else if (fase == FASE_2 || fase == FASE_3) {
            if (koyoShielded) {
                data.setH5Counter(data.h5Counter() + 1);
                data.setH3Counter(data.h3Counter() + 1);
            }
            if (gariShielded) data.setH7Counter(data.h7Counter() + 1);
            if (data.h5Counter() >= H5_TRIGGER) startH5(server, data);
            if (data.h3Counter() >= H3_TRIGGER) startH3(server, data);
            if (data.h7Counter() >= H7_TRIGGER) startH7(server, data);
            if (fase == FASE_2) {
                if (gariPct <= 37.0D && data.shieldChanges() == 0) switchShield(server, data);
                if (koyoPct <= 37.0D) {
                    startIntermission(server, data, INTER_2);
                    return;
                }
            } else {
                if (gariPct <= 6.0D && data.shieldChanges() == 0) switchShield(server, data);
                if (koyoPct <= 6.0D) {
                    startFinale(server, data);
                    return;
                }
            }
        }
        applyShieldEffects(data, koyo, gari);
        applyTrust(server, data, koyo, gari);
        runLoopTail(server, data, koyo, gari);
    }

    private void runLoopTail(ServerLevel server, B5EncounterData data,
                             KoyomiBossEntity koyo, GariBossEntity gari) {
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator() || player.isCreative()) continue;
            double rx = player.getX() - data.anchor().getX();
            double rz = player.getZ() - data.anchor().getZ();
            if (rx < -58 || rx > 14 || rz < -21 || rz > 21) {
                Vec3 tp = rel(data, 3, 1, 0);
                player.teleportTo(tp.x, tp.y, tp.z);
            }
        }
        for (ServerPlayer player : server.players()) {
            if (Math.abs(player.getY() - relY(data, -5.0D)) <= 1.0D) {
                waterFall(server, data, player);
            }
        }
        for (LivingEntity boss : List.of(koyo, gari)) {
            if (boss != null && boss.isAlive() && Math.abs(boss.getY() - relY(data, -5.0D)) <= 1.0D) {
                waterFall(server, data, boss);
            }
        }
        // caen en fleccy: entities inside the fleecy pit (-1156 50 1428, dx -4 dy 4 dz -4).
        for (ServerPlayer player : server.players()) {
            if (inFleccyBox(data, player)) {
                waterFall(server, data, player);
            }
        }
        for (LivingEntity boss : List.of(koyo, gari)) {
            if (boss != null && boss.isAlive() && inFleccyBox(data, boss)) {
                waterFall(server, data, boss);
            }
        }
        if (koyo.isAlive() && gari.isAlive() && koyo.distanceToSqr(gari) <= 4.0D) {
            B5Particles.squidBurst(server, koyo.position());
            // separar_2: Koyomi is placed 4 blocks toward -1131 from Gari.
            Vec3 dir = rel(data, -24, 1, 0).subtract(gari.position()).normalize();
            koyo.teleportTo(gari.getX() + dir.x * 4.0D, gari.getY(), gari.getZ() + dir.z * 4.0D);
            B5Particles.squidBurst(server, koyo.position());
        }
        if (koyo.isAlive()) {
            for (Boat boat : server.getEntitiesOfClass(Boat.class, koyo.getBoundingBox().inflate(20.0D),
                    e -> e.isAlive())) {
                boat.discard();
            }
        }
        for (ServerPlayer player : server.players()) {
            player.removeEffect(MobEffects.LEVITATION);
            player.removeEffect(MobEffects.JUMP);
        }
    }

    private static boolean inFleccyBox(B5EncounterData data, Entity entity) {
        double rx = entity.getX() - data.anchor().getX();
        double ry = entity.getY() - data.anchor().getY();
        double rz = entity.getZ() - data.anchor().getZ();
        return rx >= -53 && rx <= -49 && ry >= 2 && ry <= 6 && rz >= -2 && rz <= 2;
    }

    private void waterFall(ServerLevel server, B5EncounterData data, LivingEntity entity) {
        Vec3 tp = rel(data, -22, 1, 0);
        entity.teleportTo(tp.x, tp.y, tp.z);
        server.playSound(null, entity.blockPosition(), SoundEvents.FOX_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        server.sendParticles(ParticleTypes.SPLASH, tp.x, tp.y + 0.4D, tp.z, 200, 0.3D, 0.3D, 0.3D, 0.3D);
        if (entity instanceof ServerPlayer player && (koyo(server, data) != null || gari(server, data) != null)) {
            player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
        }
    }

    private void applyShieldEffects(B5EncounterData data, KoyomiBossEntity koyo, GariBossEntity gari) {
        LivingEntity shielded = data.shieldBearer() == SHIELD_KOYO ? koyo
                : data.shieldBearer() == SHIELD_GARI ? gari : null;
        if (shielded != null) {
            shielded.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 10, true, false));
            shielded.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4, true, false));
        }
    }

    private void switchShield(ServerLevel server, B5EncounterData data) {
        int bearer = data.shieldBearer();
        data.setShieldBearer(bearer == SHIELD_KOYO ? SHIELD_GARI
                : bearer == SHIELD_GARI ? SHIELD_KOYO : SHIELD_KOYO);
        data.setShieldChanges(data.shieldChanges() + 1);
        clearTrustMarker();
        resetH2(server, data);
        if (data.shieldBearer() == SHIELD_GARI) {
            // h1/switch: shield to Gari -> h2/ini at 5s (skipped in intermissions).
            if (data.fase() != INTER_1 && data.fase() != INTER_2) {
                resetH5(server, data);
                data.setH2PendingDue(server.getGameTime() + 100L);
            }
            for (ServerPlayer player : server.players()) {
                player.removeEffect(MobEffects.DAMAGE_BOOST);
            }
        }
    }

    private void applyTrust(ServerLevel server, B5EncounterData data,
                            KoyomiBossEntity koyo, GariBossEntity gari) {
        LivingEntity unshielded;
        boolean confiado;
        if (data.shieldBearer() == SHIELD_KOYO) {
            unshielded = gari;
            confiado = koyo.distanceToSqr(gari) > 15.0D * 15.0D;
        } else {
            unshielded = koyo;
            confiado = koyo.distanceToSqr(gari) <= 15.0D * 15.0D;
        }
        if (confiado) {
            unshielded.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 3, true, false));
            unshielded.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 0, true, false));
        }
        updateTrustState(confiado);
    }

    private void tickTrustMarker(ServerLevel server, B5EncounterData data,
                                 KoyomiBossEntity koyo, GariBossEntity gari) {
        LivingEntity owner = data.shieldBearer() == SHIELD_KOYO ? gari
                : data.shieldBearer() == SHIELD_GARI ? koyo : null;
        if (owner == null || !owner.isAlive()) {
            clearTrustMarker();
            return;
        }

        boolean created = trustMarker == null || !trustMarker.isAlive()
                || !owner.getUUID().equals(trustMarkerOwner);
        if (created) {
            clearTrustMarker();
            trustMarker = markerStand(server, owner.getX(), owner.getY() + 2.4D, owner.getZ(), true);
            trustMarkerOwner = owner.getUUID();
            trustMarker.setCustomNameVisible(true);
            trustStateInitialized = false;
        }
        trustMarker.teleportTo(owner.getX(), owner.getY() + 2.4D, owner.getZ());

        boolean stateChanged = false;
        if (!trustStateInitialized) {
            boolean confident = owner == koyo
                    ? koyo.distanceToSqr(gari) <= 15.0D * 15.0D
                    : koyo.distanceToSqr(gari) > 15.0D * 15.0D;
            stateChanged = updateTrustState(confident);
        }
        if (!trustConfident) return;

        server.sendParticles(ParticleTypes.CRIT,
                owner.getX(), owner.getY() + 1.0D, owner.getZ(), 2, 0, 0.5D, 0, 0.5D);
        if (!created && !stateChanged) advanceTrustColor();
    }

    private boolean updateTrustState(boolean confident) {
        if (trustStateInitialized && trustConfident == confident) return false;
        trustStateInitialized = true;
        trustConfident = confident;
        if (trustMarker == null || !trustMarker.isAlive()) return true;
        trustMarker.setCustomName(confident
                ? Component.translatable("luisb1202.functions.bossfight.b5.h1.confianza.confiar.1")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
                : Component.translatable("luisb1202.functions.bossfight.b5.h1.confianza.descofiar.1")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        return true;
    }

    private void advanceTrustColor() {
        trustColorFrame++;
        if (trustColorFrame >= 20) trustColorFrame = 0;
        if (trustColorFrame >= 1 && trustColorFrame <= 9 && trustMarker != null && trustMarker.isAlive()) {
            trustMarker.setCustomName(Component.translatable(
                    "luisb1202.functions.bossfight.b5.h1.confianza.colorines." + trustColorFrame)
                    .withStyle(ChatFormatting.BOLD));
        }
    }

    private void clearTrustMarker() {
        if (trustMarker != null && trustMarker.isAlive()) trustMarker.discard();
        trustMarker = null;
        trustMarkerOwner = null;
        trustStateInitialized = false;
        trustConfident = false;
    }

    public void onBossHit(ServerLevel server, boolean koyoHit, Vec3 pos) {
        if (data == null || !data.active()) return;
        int fase = data.fase();
        if (fase == INTER_1 || fase == INTER_2 || fase == FASE_4 || fase == 0) return;
        boolean shieldedHit = koyoHit ? data.shieldBearer() == SHIELD_KOYO : data.shieldBearer() == SHIELD_GARI;
        if (shieldedHit) {
            rebound(server, pos);
            return;
        }
        showTrustHint(server, koyoHit);
    }

    private void showTrustHint(ServerLevel server, boolean koyoHit) {
        if (server.getGameTime() < trustHintReadyAt) return;
        KoyomiBossEntity koyo = koyo(server, data);
        GariBossEntity gari = gari(server, data);
        if (koyo == null || gari == null || !koyo.isAlive() || !gari.isAlive()) return;

        boolean confident = koyoHit
                ? koyo.distanceToSqr(gari) <= 15.0D * 15.0D
                : koyo.distanceToSqr(gari) > 15.0D * 15.0D;
        if (!confident) return;

        String key = "luisb1202.functions.bossfight.b5.h1.confianza.msg_koros." + (koyoHit ? "2" : "1");
        sendDialogueNow(server, dialogueHd(key), SoundEvents.TRIDENT_RETURN, 1.7F);
        trustHintReadyAt = server.getGameTime() + 60L;
    }

    private void rebound(ServerLevel server, Vec3 pos) {
        for (ServerPlayer player : server.players()) {
            if (!player.isSpectator() && player.distanceToSqr(pos) <= 5.0D * 5.0D) {
                player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 0));
            }
        }
        B5Particles.critFan(server, pos.add(0, 1.0D, 0));
        server.playSound(null, BlockPos.containing(pos), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 2.0F);
        server.playSound(null, BlockPos.containing(pos), SoundEvents.PLAYER_HURT_ON_FIRE, SoundSource.AMBIENT, 1.0F, 1.2F);
        server.playSound(null, BlockPos.containing(pos), SoundEvents.GRASS_BREAK, SoundSource.AMBIENT, 1.0F, 2.0F);
    }

    /* ------------------------------ intermissions ------------------------------ */

    private void startIntermission(ServerLevel server, B5EncounterData data, int which) {
        clearTrustMarker();
        data.setFase(which);
        data.setInterTicks(0);
        data.setInterEvent(0);
        data.clearH3IntermissionHits();
        data.setShieldBearer(SHIELD_KOYO);
        data.setShieldChanges(0);
        data.setH5Counter(0);
        data.setH3Counter(0);
        data.setH7Counter(0);
        data.setPoisonActive(false);
        data.setIllusionActive(false);
        resetH2(server, data);
        resetH3(server, data);
        resetH5(server, data);
        resetH7(server, data);
        clearIntermissionVisuals(server, data);
        KoyomiBossEntity koyo = koyo(server, data);
        if (koyo != null) {
            koyo.setInvulnerable(true);
            koyo.setNoAi(true);
            Vec3 tp = rel(data, 19, 16, 0);
            koyo.teleportTo(tp.x, tp.y, tp.z);
            B5Particles.squidBurst(server, koyo.position());
        }
        // inter ini first tags Koyomi and then always invokes h1/switch, leaving
        // Gari as the shield bearer. Keep this independent of Koyomi lookup so
        // recovery cannot silently leave the shield on the wrong boss.
        switchShield(server, data);
        GariBossEntity gari = gari(server, data);
        applyShieldEffects(data, koyo, gari);
        tickShieldRing(server, data, koyo);
        broadcastTitle("luisb1202.functions.bossfight.b1.ini_f1.1");
        broadcastSubtitle(which == INTER_1
                ? "luisb1202.functions.bossfight.b5.fase.inter1.ini.1"
                : "luisb1202.functions.bossfight.b5.fase.inter2.ini.1");
        playForAll(server, SoundEvents.NOTE_BLOCK_PLING.value(), 1.5F);
        if (which == INTER_1) {
            scheduleDialogues(server, new int[]{0, 80, 200, 280},
                    new String[]{
                            "luisb1202.functions.bossfight.b5.dialogos.dia7.1",
                            "luisb1202.functions.bossfight.b5.dialogos.dia7.2",
                            "luisb1202.functions.bossfight.b5.dialogos.dia7.3",
                            "luisb1202.functions.bossfight.b5.dialogos.dia7.4"},
                    new int[]{0, 0, 1, 1});
        } else {
            scheduleDialogues(server, new int[]{0, 120, 220},
                    new String[]{
                            "luisb1202.functions.bossfight.b5.dialogos.dia8.1",
                            "luisb1202.functions.bossfight.b5.dialogos.dia8.2",
                            "luisb1202.functions.bossfight.b5.dialogos.dia8.3"},
                    new int[]{0, 1, 0});
        }
        startMusicInterIntro(server, data);
    }

    private void tickIntermission(ServerLevel server, B5EncounterData data,
                                  KoyomiBossEntity koyo, GariBossEntity gari) {
        data.setInterTicks(data.interTicks() + 1);
        int t = data.interTicks();
        boolean inter1 = data.fase() == INTER_1;
        if (t == 100) startPoison(server, data);
        if (inter1) {
            if (t == 350 || t == 490 || t == 630 || t == 770) startLoco(server, data);
            if (t == 930) startIllusionTrails(server, data);
        } else {
            if (t == 300) startIllusionTrails(server, data);
            if (t == 400 || t == 700 || t == 1000 || t == 1300 || t == 1600) startLoco(server, data);
        }
        // h3/run is an independently scheduled loop in the datapack. Tick existing
        // warning markers before paso_loco creates the next row so new markers begin
        // aging on the following tick, matching `schedule ... h3/run 1t`.
        tickH3(server, data, koyo);
        tickLoco(server, data);
        tickPozas(server, data);
        tickTrails(server, data);
        tickPoison(server, data);
        tickIllusions(server, data);
        tickH4Timer(server, data);
        tickH4Projectiles(server, data);
        tickProjectiles(server, data);
        data.setRunTicks(data.runTicks() + 1);
        if (data.runTicks() % RUN_INTERVAL == 0) {
            // b5/run continues during intermissions and refreshes resistance XI
            // plus slowness V on the current shield bearer every second.
            applyShieldEffects(data, koyo, gari);
            if (data.illusionActive() && !illusions.isEmpty()) {
                data.setInterEvent(data.interEvent() + 1);
                if (data.interEvent() >= 6 && !h4ProjectilesActive) {
                    h4ProjectilesActive = true;
                    h4ProjectileRunTicks = 0;
                }
            }
        }
        playerLoopTail(server, data);
    }

    private void enterFase2(ServerLevel server, B5EncounterData data, int nextFase) {
        clearTrustMarker();
        data.setFase(nextFase);
        data.setH5Counter(14);
        data.setH3Counter(28);
        data.setH7Counter(0);
        data.setShieldChanges(0);
        data.setShieldBearer(SHIELD_KOYO);
        data.setInterEvent(0);
        data.setH4Timer(-1);
        data.setH4DamagePhase(false);
        ScoreboardHelper.removeTimer(server);
        GariBossEntity gari = gari(server, data);
        if (gari != null) {
            gari.setHealth(nextFase == FASE_2 ? 544.0F : 296.0F);
            gari.setNoAi(false);
            gari.setInvulnerable(false);
        }
        KoyomiBossEntity koyo = koyo(server, data);
        if (koyo != null) {
            Vec3 tp = rel(data, -36, 1, 0);
            koyo.teleportTo(tp.x, tp.y, tp.z);
            B5Particles.squidBurst(server, koyo.position());
            koyo.setNoAi(false);
            koyo.setInvulnerable(false);
        }
        clearIntermissionVisuals(server, data);
        broadcastTitle("luisb1202.functions.bossfight.b1.ini_f1.1");
        broadcastSubtitle(nextFase == FASE_2
                ? "luisb1202.functions.bossfight.b1.ini_f2.1"
                : "luisb1202.functions.bossfight.b3.fase.5.ini.1");
        playForAll(server, SoundEvents.NOTE_BLOCK_PLING.value(), 1.5F);
    }

    /* ------------------------------ h5 / h6 / h7 ------------------------------ */

    private void startH5(ServerLevel server, B5EncounterData data) {
        data.setH5Counter(0);
        data.setH5VolleyActive(true);
        data.setH5FlechaRound(0);
        int now = server.getServer().getTickCount();
        h5FireAt[0] = now + 16;
        h5FireAt[1] = now + 40;
        h5FireAt[2] = now + 64;
        startH6(server, data);
        pendingH6.add(now + 24);
        pendingH6.add(now + 48);
        scheduleDialogues(server, new int[]{4, 90},
                new String[]{
                        "luisb1202.functions.bossfight.b5.dialogos.dia10.2",
                        "luisb1202.functions.bossfight.b5.dialogos.dia13.1"},
                new int[]{0, 2});
    }

    private void startH6(ServerLevel server, B5EncounterData data) {
        data.setH6Active(true);
        data.setH6Ticks(0);
        GariBossEntity gari = gari(server, data);
        if (gari == null || !gari.isAlive()) {
            data.setH6Active(false);
            return;
        }
        List<Vec3> candidates = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            double a = i * Math.PI * 2.0D / 24.0D;
            candidates.add(new Vec3(gari.getX() + Math.cos(a) * 14.0D,
                    relY(data, 1.0D), gari.getZ() + Math.sin(a) * 14.0D));
        }
        candidates.removeIf(c -> {
            BlockPos pos = BlockPos.containing(c);
            return isNoground(server, pos.below()) || !isNoground(server, pos)
                    || c.x < data.anchor().getX() - 56 || c.x > data.anchor().getX() + 12
                    || c.z < data.anchor().getZ() - 19 || c.z > data.anchor().getZ() + 19;
        });
        if (candidates.isEmpty()) {
            data.setH6Active(false);
            return;
        }
        int min = Integer.MAX_VALUE;
        int[] counts = new int[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            int n = 0;
            for (ServerPlayer player : server.players()) {
                if (!player.isSpectator() && player.distanceToSqr(candidates.get(i)) <= 12.0D * 12.0D) n++;
            }
            counts[i] = n;
            min = Math.min(min, n);
        }
        List<Vec3> best = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            if (counts[i] == min) best.add(candidates.get(i));
        }
        Vec3 target = best.get(server.random.nextInt(best.size()));
        data.setH6Target(target.x, target.z);
        server.playSound(null, gari.blockPosition(), SoundEvents.BAT_TAKEOFF, SoundSource.PLAYERS, 2.0F, 1.5F);
    }

    private void tickH6(ServerLevel server, B5EncounterData data, GariBossEntity gari) {
        if (!data.h6Active() || gari == null || !gari.isAlive()) return;
        Vec3 target = new Vec3(data.h6TargetX(), relY(data, 1.0D), data.h6TargetZ());
        Vec3 step = target.subtract(gari.position());
        if (step.lengthSqr() <= 1.5D * 1.5D || data.h6Ticks() >= 60) {
            h6End(server, data, gari);
            return;
        }
        Vec3 dir = step.normalize();
        for (int i = 0; i < 2; i++) {
            Vec3 hop = gari.position().add(dir.scale(0.9D));
            gari.teleportTo(hop.x, relY(data, 1.0D), hop.z);
            ServerPlayer nearest = nearestPlayer(server, gari.position(), Double.MAX_VALUE);
            if (nearest != null) gari.lookAt(EntityAnchorArgument.Anchor.EYES, nearest.getEyePosition());
            server.sendParticles(ParticleTypes.CLOUD, gari.getX(), gari.getY(), gari.getZ(), 1, 0, 0, 0, 0);
        }
        data.setH6Ticks(data.h6Ticks() + 1);
    }

    private void h6End(ServerLevel server, B5EncounterData data, GariBossEntity gari) {
        data.setH6Active(false);
        KoyomiBossEntity koyo = koyo(server, data);
        if (koyo != null && gari.isAlive()) {
            Vec3 dir = rel(data, -24, 1, 0).subtract(gari.position()).normalize();
            for (int i = 0; i < 12; i++) {
                if (!isNoground(server, gari.blockPosition())) {
                    koyo.teleportTo(gari.getX() + dir.x, gari.getY(), gari.getZ() + dir.z);
                }
            }
        }
    }

    private void fireH5Flecha(ServerLevel server, B5EncounterData data, GariBossEntity gari) {
        if (gari == null || !gari.isAlive()) return;
        ServerPlayer target = randomPlayer(server);
        if (target == null) return;
        gari.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
        server.playSound(null, gari.blockPosition(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 2.0F, 0.7F);
        server.playSound(null, gari.blockPosition(), SoundEvents.PLAYER_HURT_DROWN, SoundSource.PLAYERS, 2.0F, 1.4F);
        server.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                gari.getX(), gari.getY() + 1.0D, gari.getZ(), 10, 0, 0, 0, 1);
        server.sendParticles(ParticleTypes.EXPLOSION,
                gari.getX(), gari.getY() + 1.0D, gari.getZ(), 1, 0, 0, 0, 0);
        projectiles.add(new Projectile(gari.position().add(0, 1.6D, 0), target.getUUID(), Projectile.H5));
    }

    private void startH7(ServerLevel server, B5EncounterData data) {
        data.setH7Counter(0);
        int now = server.getServer().getTickCount();
        startH6(server, data);
        pendingH7.add(now + 10);
        pendingH6.add(now + 26);
        pendingH7.add(now + 36);
        pendingH6.add(now + 52);
        pendingH7.add(now + 62);
        if (server.random.nextBoolean()) {
            scheduleDialogues(server, new int[]{0},
                    new String[]{"luisb1202.functions.bossfight.b5.dialogos.dia12."
                            + (server.random.nextInt(2) + 1)}, new int[]{0});
        }
    }

    private void startH7Burst(ServerLevel server, B5EncounterData data, GariBossEntity gari) {
        if (gari == null || !gari.isAlive()) return;
        ServerPlayer target = randomPlayer(server);
        if (target != null) {
            gari.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
        }
        gari.setYRot(gari.getYRot() - 22.5F);
        gari.setNoAi(true);
        data.setH7Active(true);
        data.setH7Ticks(0);
    }

    private void tickH7(ServerLevel server, B5EncounterData data, GariBossEntity gari) {
        if (!data.h7Active() || gari == null || !gari.isAlive()) return;
        gari.setYRot(gari.getYRot() + 22.5F);
        Vec3 origin = gari.position();
        Vec3 direction = Vec3.directionFromRotation(0.0F, gari.getYRot());
        projectiles.add(new Projectile(origin, direction.scale(0.23D), Projectile.DISPARO));
        server.sendParticles(ParticleTypes.SWEEP_ATTACK,
                gari.getX(), gari.getY() + 1.0D, gari.getZ(), 1, 0, 0, 0, 0);
        server.playSound(null, gari.blockPosition(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 3.0F, 0.8F);
        data.setH7Ticks(data.h7Ticks() + 1);
        if (data.h7Ticks() >= 16) {
            data.setH7Active(false);
            gari.setNoAi(false);
        }
    }

    /* ------------------------------ h3 ------------------------------ */

    private void startH3(ServerLevel server, B5EncounterData data) {
        data.setH3Counter(0);
        data.setH3Active(true);
        data.setH3Remaining(12);
        data.setH3Timer(15);
        for (ServerPlayer player : server.players()) {
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("item.written_book.10.page.1.10")
                            .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.parseColor("#FBBDFF")))));
            player.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.translatable("luisb1202.functions.bossfight.b5.h3.warn.1")
                            .withStyle(ChatFormatting.BOLD)));
        }
        playForAll(server, SoundEvents.EXPERIENCE_ORB_PICKUP, 2.0F);
        int variant = server.random.nextInt(5) + 2;
        scheduleDialogues(server, new int[]{0},
                new String[]{"luisb1202.functions.bossfight.b5.dialogos.dia1." + variant}, new int[]{1});
    }

    private void h3Round(ServerLevel server, B5EncounterData data, KoyomiBossEntity koyo) {
        if (koyo == null || !koyo.isAlive()) return;
        server.playSound(null, koyo.blockPosition(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 2.0F, 0.8F);
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator()) continue;
            h3Markers.add(markerStand(server, player.getX(), relY(data, 1), player.getZ(), true));
        }
        Vec3 base = koyo.position().add(localOffset(koyo, 0.2D, 0.0D, 0.6D)).add(0.2D, 0.0D, 0.0D);
        ArmorStand koyoTrident = markerStand(server, base.x, base.y, base.z, false);
        koyoTrident.setRightArmPose(new Rotations(270.0F, 0.0F, 0.0F));
        koyoTrident.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
        koyoTrident.teleportTo(base.x, base.y + 7.0D, base.z);
        koyoTrident.getPersistentData().putBoolean("KoyoTrident", true);
        h3Markers.add(koyoTrident);
    }

    private void tickH3(ServerLevel server, B5EncounterData data, KoyomiBossEntity koyo) {
        if (!data.h3Active() && h3Markers.isEmpty() && h3GroundTridents.isEmpty()) return;
        Iterator<ArmorStand> it = h3Markers.iterator();
        while (it.hasNext()) {
            ArmorStand stand = it.next();
            if (!stand.isAlive()) {
                it.remove();
                continue;
            }
            int age = stand.getPersistentData().getInt("Age") + 1;
            stand.getPersistentData().putInt("Age", age);
            if (stand.getPersistentData().getBoolean("KoyoTrident")) {
                if (age >= 4) {
                    stand.discard();
                    it.remove();
                }
                continue;
            }
            stand.setYRot(stand.getYRot() + 7.0F);
            Vec3 fwd = Vec3.directionFromRotation(0.0F, stand.getYRot());
            for (double sign : new double[]{1.0D, -1.0D}) {
                Vec3 warning = stand.position().add(fwd.scale(3.5D * sign)).add(0.0D, 0.1D, 0.0D);
                BlockPos floor = BlockPos.containing(warning.x, stand.getY() - 1.0D, warning.z);
                if (!server.getBlockState(floor).isAir()) {
                    sendH3WarningParticle(server, data, warning);
                }
            }
            if (age >= 35) {
                h3Boom(server, data, stand.position());
                stand.discard();
                it.remove();
            }
        }
        if (data.h3Active()) {
            data.setH3Timer(data.h3Timer() - 1);
            if (data.h3Timer() <= 0) {
                h3Round(server, data, koyo);
                data.setH3Remaining(data.h3Remaining() - 1);
                data.setH3Timer(data.h3Remaining() > 0 ? 15 : -1);
                if (data.h3Remaining() <= 0) data.setH3Active(false);
            }
        }
        Iterator<ArmorStand> gt = h3GroundTridents.iterator();
        while (gt.hasNext()) {
            ArmorStand stand = gt.next();
            if (!stand.isAlive()) {
                gt.remove();
                continue;
            }
            int age = stand.getPersistentData().getInt("Age") + 1;
            stand.getPersistentData().putInt("Age", age);
            if (age == 60) stand.teleportTo(stand.getX(), stand.getY() - 2.0D, stand.getZ());
            if (age >= 62) {
                stand.discard();
                gt.remove();
            }
        }
    }

    /** h3/run: normal phases show the circle to everyone; interphases hide it from green poison. */
    private void sendH3WarningParticle(ServerLevel server, B5EncounterData data, Vec3 point) {
        boolean intermission = data.fase() == INTER_1 || data.fase() == INTER_2;
        for (ServerPlayer player : server.players()) {
            if (intermission && playerColors.getOrDefault(player.getScoreboardName(), 2) != 2) continue;
            server.sendParticles(player, ParticleTypes.FIREWORK, true,
                    point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
    }

    private void h3Boom(ServerLevel server, B5EncounterData data, Vec3 pos) {
        B5Particles.h3Boom(server, pos);
        ArmorStand ground = markerStand(server, pos.x, relY(data, 16), pos.z, false);
        ground.setYRot(server.random.nextFloat() * 360.0F);
        ground.setRightArmPose(new Rotations(90.0F, server.random.nextFloat() * 360.0F, 0.0F));
        ground.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
        ground.teleportTo(pos.x, relY(data, 0.5D), pos.z);
        if (server.getBlockState(ground.blockPosition().below()).isAir()) {
            ground.discard();
        } else {
            h3GroundTridents.add(ground);
        }
        server.playSound(null, BlockPos.containing(pos), SoundEvents.TRIDENT_HIT_GROUND, SoundSource.PLAYERS, 1.0F, 0.8F);
        server.playSound(null, BlockPos.containing(pos), SoundEvents.GILDED_BLACKSTONE_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F);
        server.playSound(null, BlockPos.containing(pos), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.2F);
        boolean intermission = data.fase() == INTER_1 || data.fase() == INTER_2;
        for (ServerPlayer player : server.players()) {
            if (!player.isSpectator() && player.distanceToSqr(pos) <= 3.8D * 3.8D) {
                if (intermission) {
                    int hits = data.incrementH3IntermissionHits(player.getUUID());
                    player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, Math.min(4, hits)));
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                }
            }
        }
    }

    private void startLoco(ServerLevel server, B5EncounterData data) {
        data.setLocoRemaining(14);
        data.setLocoTicks(0);
        data.setLocoMoveTimer(0);
        Vec3 start = rel(data, 12, 1, 19.75D);
        for (int i = 0; i < 8; i++) {
            locoStands.add(markerStand(server, start.x, start.y, start.z - 5.5D * i, true));
        }
        if (!locoStands.isEmpty()) {
            ArmorStand first = locoStands.remove(server.random.nextInt(locoStands.size()));
            ArmorStand second = null;
            double nearest = Double.MAX_VALUE;
            for (ArmorStand candidate : locoStands) {
                double distance = candidate.distanceToSqr(first);
                if (distance < nearest) {
                    nearest = distance;
                    second = candidate;
                }
            }
            first.discard();
            if (second != null) {
                locoStands.remove(second);
                second.discard();
            }
        }
        broadcastTitle("luisb1202.functions.bossfight.koros_msg_lotear.1");
        broadcastSubtitle("luisb1202.functions.bossfight.b5.h3.ini_loco.1");
        playForAll(server, SoundEvents.VEX_CHARGE, 1.0F);
        playForAll(server, SoundEvents.BAT_TAKEOFF, 1.0F);
        boolean first = data.interEvent() <= 1;
        scheduleDialogues(server, new int[]{0},
                new String[]{first
                        ? "luisb1202.functions.bossfight.b5.dialogos.dia3.1"
                        : "luisb1202.functions.bossfight.b5.dialogos.dia1."
                        + (server.random.nextInt(5) + 2)}, new int[]{first ? 2 : 1});
        data.setInterEvent(data.interEvent() + 1);
    }

    private void tickLoco(ServerLevel server, B5EncounterData data) {
        if (data.locoRemaining() <= 0) return;
        data.setLocoTicks(data.locoTicks() + 1);
        data.setLocoMoveTimer(data.locoMoveTimer() + 1);
        for (ArmorStand stand : locoStands) {
            if (!stand.isAlive()) continue;
            // paso_loco -> h3/gen creates an empty invisible marker. The visible
            // trident is only created by boom_vsfx after the warning reaches age 35.
            h3Markers.add(markerStand(server, stand.getX(), stand.getY(), stand.getZ(), false));
            stand.teleportTo(stand.getX() - 5.0D, stand.getY(), stand.getZ());
        }
        // run_loco performs paso_loco first and end_loco second on score 14.
        if (data.locoTicks() >= 14) {
            for (ArmorStand stand : locoStands) {
                if (stand.isAlive()) stand.discard();
            }
            locoStands.clear();
            data.setLocoRemaining(0);
            for (ArmorStand stand : h3Markers) {
                stand.getPersistentData().putInt("Age",
                        stand.getPersistentData().getInt("Age") - 90);
            }
        }
    }

    /* ------------------------------ h2 ------------------------------ */

    private void startH2(ServerLevel server, B5EncounterData data) {
        resetH2(server, data);
        KoyomiBossEntity koyo = koyo(server, data);
        if (koyo == null || !koyo.isAlive()) return;
        Vec3 walkTarget = rel(data, -22, 1, 0);
        koyo.lookAt(EntityAnchorArgument.Anchor.EYES, walkTarget);
        for (int i = 0; i < 12; i++) {
            BlockPos check = new BlockPos(koyo.blockPosition().getX(), relYInt(data, 9), koyo.blockPosition().getZ());
            if (!isNoground(server, check)) {
                Vec3 fwd = koyo.getLookAngle();
                koyo.teleportTo(koyo.getX() + fwd.x, koyo.getY(), koyo.getZ() + fwd.z);
            }
        }
        h2Bomb = markerStand(server, koyo.getX(), koyo.getY(), koyo.getZ(), true);
        koyo.lookAt(EntityAnchorArgument.Anchor.EYES, rel(data, -16, 1, 0));
        golpear(server, data, koyo.getYRot());
        selectH2Target(server, data);
        dialogueQueue.add(new Dialogue(server.getServer().getTickCount() + 14,
                dialogueKoyo("luisb1202.functions.bossfight.b5.dialogos.dia9.1"),
                SoundEvents.PILLAGER_AMBIENT, 1.7F));
        data.setH2Active(true);
        data.setH2VisionScore(-100);
    }

    private void golpear(ServerLevel server, B5EncounterData data, float sourceYaw) {
        if (h2Bomb == null || !h2Bomb.isAlive()) return;
        data.setH2VisionScore(-100);
        if (h2Vision != null && h2Vision.isAlive()) h2Vision.discard();
        B5Particles.h2Golpe(server, new Vec3(h2Bomb.getX(), relY(data, 1.2D), h2Bomb.getZ()));
        h2Vision = markerStand(server, h2Bomb.getX(), h2Bomb.getY(), h2Bomb.getZ(), true);
        h2Vision.setYRot(sourceYaw);
        h2Vision.setXRot(0.0F);
        Vec3 fwd = Vec3.directionFromRotation(0.0F, sourceYaw);
        h2Vision.teleportTo(h2Vision.getX() + fwd.x * 6.0D, h2Vision.getY(), h2Vision.getZ() + fwd.z * 6.0D);
        h2Vision.setXRot(-100.0F);
        ServerPlayer target = data.h2Target() != null
                ? server.getServer().getPlayerList().getPlayer(data.h2Target()) : null;
        if (target != null && !target.isSpectator() && target.distanceToSqr(h2Bomb.position()) <= 1.0D) {
            target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 3, true, false));
            for (ServerPlayer player : server.players()) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1, true, false));
            }
            selectH2Target(server, data);
        }
    }

    private void selectH2Target(ServerLevel server, B5EncounterData data) {
        List<ServerPlayer> candidates = new ArrayList<>();
        for (ServerPlayer player : server.players()) {
            if (!player.isSpectator() && !player.getUUID().equals(lastH2Target)) candidates.add(player);
        }
        if (candidates.isEmpty()) {
            for (ServerPlayer player : server.players()) {
                if (!player.isSpectator()) candidates.add(player);
            }
        }
        if (candidates.isEmpty()) return;
        ServerPlayer target = candidates.get(server.random.nextInt(candidates.size()));
        data.setH2Target(target.getUUID());
        lastH2Target = target.getUUID();
        Component msg = Component.translatable("luisb1202.functions.bossfight.b5.h2.seleccionar.1")
                .append(Component.translatable("luisb1202.functions.bossfight.b5.h2.seleccionar.2")
                        .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.parseColor("#FBBDFF"))))
                .append(Component.translatable("item.written_book.5.page.2.3").withStyle(ChatFormatting.BOLD))
                .append(target.getDisplayName().copy().withStyle(ChatFormatting.BOLD))
                .append(Component.translatable("luisb1202.functions.bossfight.b5.h2.seleccionar.3"));
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator()) continue;
            player.displayClientMessage(msg, true);
            player.displayClientMessage(msg, false);
        }
        h2Warn(server, data);
    }

    private void h2Warn(ServerLevel server, B5EncounterData data) {
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator() || !player.getUUID().equals(data.h2Target())) continue;
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("item.written_book.10.page.1.10")
                            .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.parseColor("#FBBDFF")))));
            player.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.translatable("luisb1202.functions.bossfight.b5.h2.warn.1").withStyle(ChatFormatting.BOLD)));
            server.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS, 1.0F, 2.0F);
        }
    }

    private void tickH2(ServerLevel server, B5EncounterData data) {
        if (!data.h2Active() || h2Bomb == null || !h2Bomb.isAlive() || h2Vision == null || !h2Vision.isAlive()) {
            data.setH2Active(false);
            return;
        }
        h2ParticleFrame++;
        int particleScore = Math.max(1, h2ParticleFrame);
        double radius = particleScore <= 2 ? 1.0D
                : particleScore <= 4 ? 0.95D
                : particleScore <= 6 ? 0.85D
                : particleScore <= 8 ? 0.8D
                : particleScore <= 10 ? 0.85D : 0.95D;
        if (h2ParticleFrame >= 12) h2ParticleFrame = 1;
        B5Particles.h2BombRing(server, h2Bomb.position().add(0.0D, 1.4D, 0.0D), radius);
        if (server.random.nextInt(2) == 0) {
            server.sendParticles(ParticleTypes.END_ROD,
                    h2Bomb.getX(), h2Bomb.getY() + 1.4D, h2Bomb.getZ(), 1, 0.3D, 0.3D, 0.3D, 0);
        }
        if (data.h2Bouncing()) {
            h2Bounce(server, data);
            return;
        }
        Vec3 bombPos = h2Vision.position().add(localOffset(h2Vision, 0.0D, 6.0D, 0.0D));
        h2Bomb.teleportTo(bombPos.x, bombPos.y, bombPos.z);
        BlockPos collisionPos = BlockPos.containing(h2Bomb.position().add(0.0D, 1.2D, 0.0D));
        boolean grounded = !isNoground(server, collisionPos);
        boolean inWater = server.getBlockState(collisionPos).getBlock() == Blocks.WATER;
        if (grounded || inWater) {
            h2Boom(server, data);
            return;
        }
        int vision = data.h2VisionScore() + 1;
        data.setH2VisionScore(vision);
        if (vision >= -100 && vision <= -96) {
            h2Vision.teleportTo(h2Vision.getX(), h2Vision.getY() + 0.6D, h2Vision.getZ());
        } else if (vision >= -95 && vision <= -90) {
            h2Vision.teleportTo(h2Vision.getX(), h2Vision.getY() + 0.15D, h2Vision.getZ());
        } else if (vision >= -90 && vision <= 90) {
            // mover: Rotation[1] = score -> pitch steers the bomb arc.
            h2Vision.setXRot(vision);
            Vec3 vf = Vec3.directionFromRotation(0.0F, h2Vision.getYRot());
            h2Vision.teleportTo(h2Vision.getX() + vf.x * 0.03D,
                    h2Vision.getY(), h2Vision.getZ() + vf.z * 0.03D);
        } else if (vision >= 90) {
            h2Vision.teleportTo(h2Vision.getX(), h2Vision.getY() - 0.07D, h2Vision.getZ());
        }
        if (vision >= -90) {
            Vec3 vf = Vec3.directionFromRotation(0.0F, h2Vision.getYRot());
            BlockPos ahead = BlockPos.containing(h2Bomb.position().add(vf).add(0.0D, 1.0D, 0.0D));
            if (!isNoground(server, ahead)) {
                data.setH2VisionScore(91);
                vision = 91;
            }
        }
        if (vision >= 90) {
            ServerPlayer target = data.h2Target() != null
                    ? server.getServer().getPlayerList().getPlayer(data.h2Target()) : null;
            if (target != null && !target.isSpectator() && target.distanceToSqr(h2Bomb.position()) <= 1.0D) {
                // source mover: golpear runs as the player -> new vision faces the player.
                golpear(server, data, target.getYRot());
                return;
            }
        }
        if (vision >= 60 && warnCooldown <= 0) {
            h2Warn(server, data);
            warnCooldown = 20;
        }
        if (data.h2VisionScore() >= 90) h2Bomb.setYRot(h2Bomb.getYRot() + 11.0F);
        if (warnCooldown > 0) warnCooldown--;
    }

    private void h2Boom(ServerLevel server, B5EncounterData data) {
        if (h2Bomb == null || !h2Bomb.isAlive()) return;
        BlockPos collisionPos = BlockPos.containing(h2Bomb.position().add(0.0D, 1.2D, 0.0D));
        boolean inWater = server.getBlockState(collisionPos).getBlock() == Blocks.WATER;
        if (inWater) data.setH2WaterBomb(true);
        h2Bomb.teleportTo(h2Bomb.getX(), h2Bomb.getY() + (data.h2WaterBomb() ? 6.0D : 2.0D), h2Bomb.getZ());
        B5Particles.h2Boom(server, new Vec3(h2Bomb.getX(), relY(data, 1), h2Bomb.getZ()));
        data.setH2Dano(data.h2Dano() + 1);
        int amp = switch (Math.min(6, data.h2Dano())) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 2;
            case 4 -> 3;
            case 5 -> 4;
            default -> 5;
        };
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator()) continue;
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, amp));
        }
        golpear(server, data, h2Bomb.getYRot());
        data.setH2Bouncing(true);
        h2Bounce(server, data);
    }

    private void h2Bounce(ServerLevel server, B5EncounterData data) {
        if (h2Bomb == null || !h2Bomb.isAlive() || h2Vision == null || !h2Vision.isAlive()) {
            data.setH2Active(false);
            return;
        }
        int vision = data.h2VisionScore() + 1;
        data.setH2VisionScore(vision);
        if (vision >= -100 && vision <= -96) {
            h2Vision.teleportTo(h2Vision.getX(), h2Vision.getY() + 0.4D, h2Vision.getZ());
        } else if (vision >= -95 && vision <= -90) {
            h2Vision.teleportTo(h2Vision.getX(), h2Vision.getY() + 0.1D, h2Vision.getZ());
        } else if (vision >= -89 && vision <= -80) {
            h2Vision.teleportTo(h2Vision.getX(), h2Vision.getY() + 0.02D, h2Vision.getZ());
        } else if (vision >= -75 && vision <= -70) {
            h2Vision.teleportTo(h2Vision.getX(), h2Vision.getY() - 0.02D, h2Vision.getZ());
        } else if (vision >= -69 && vision <= -65) {
            h2Vision.teleportTo(h2Vision.getX(), h2Vision.getY() - 0.05D, h2Vision.getZ());
        }
        Vec3 bombPos = h2Vision.position().add(localOffset(h2Vision, 0.0D, 6.0D, 0.0D));
        h2Bomb.teleportTo(bombPos.x, bombPos.y, bombPos.z);
        if (vision >= -65) {
            data.setH2VisionScore(91);
            data.setH2Bouncing(false);
            if (data.h2WaterBomb()) {
                data.setH2WaterBomb(false);
                h2Vision.discard();
                h2Vision = markerStand(server, h2Bomb.getX(), h2Bomb.getY(), h2Bomb.getZ(), true);
                Vec3 waterTarget = rel(data, -14, 1, 0).subtract(h2Vision.position());
                float waterYaw = (float) Math.toDegrees(Math.atan2(-waterTarget.x, waterTarget.z));
                h2Vision.setYRot(waterYaw);
                h2Vision.setXRot(0.0F);
                Vec3 vf = Vec3.directionFromRotation(0.0F, waterYaw);
                h2Vision.teleportTo(h2Vision.getX() + vf.x * 6.0D, h2Vision.getY(), h2Vision.getZ() + vf.z * 6.0D);
                data.setH2VisionScore(-90);
                B5Particles.h2Golpe(server, new Vec3(h2Bomb.getX(), relY(data, 1.2D), h2Bomb.getZ()));
            }
        }
    }

    private void resetH2(ServerLevel server, B5EncounterData data) {
        data.setH2Active(false);
        data.setH2PendingDue(0L);
        data.setH2Dano(0);
        data.setH2VisionScore(0);
        data.setH2WaterBomb(false);
        data.setH2Bouncing(false);
        data.setH2Target(null);
        h2ParticleFrame = 0;
        if (h2Bomb != null && h2Bomb.isAlive()) {
            server.sendParticles(ParticleTypes.CLOUD, h2Bomb.getX(), h2Bomb.getY() + 1.2D, h2Bomb.getZ(),
                    20, 0, 0, 0, 0.2D);
            server.sendParticles(ParticleTypes.FLASH, h2Bomb.getX(), h2Bomb.getY() + 1.2D, h2Bomb.getZ(),
                    1, 0, 0, 0, 0);
            h2Bomb.discard();
        }
        if (h2Vision != null && h2Vision.isAlive()) h2Vision.discard();
        h2Bomb = null;
        h2Vision = null;
    }

    /* ------------------------------ h4 ------------------------------ */

    private void startPoison(ServerLevel server, B5EncounterData data) {
        data.setPoisonActive(true);
        data.setPoisonTicks(0);
        data.setPoisonDano(0);
        List<ServerPlayer> activePlayers = new ArrayList<>();
        for (ServerPlayer player : server.players()) {
            if (!player.isSpectator() && player.isAlive()) activePlayers.add(player);
        }
        ServerPlayer target = activePlayers.isEmpty() ? null
                : activePlayers.get(server.random.nextInt(activePlayers.size()));
        data.setPoisonTarget(target != null ? target.getUUID() : null);
        playerColors.clear();
        List<ServerPlayer> unassigned = new ArrayList<>(activePlayers);
        int assignment = 0;
        while (!unassigned.isEmpty()) {
            ServerPlayer player = unassigned.remove(server.random.nextInt(unassigned.size()));
            int pattern = assignment++ % 4;
            playerColors.put(player.getScoreboardName(), pattern == 0 || pattern == 3 ? 2 : 1);
        }
        pozas.clear();
        if (activePlayers.size() == 1) {
            playerColors.put(activePlayers.get(0).getScoreboardName(), 2);
            double[][] poolRel = {{-28, 1.5, 0}, {-19, 1.5, 5}, {-19, 1.5, -5}};
            int furthest = 0;
            double bestDist = -1;
            for (int i = 0; i < poolRel.length; i++) {
                Vec3 pos = rel(data, poolRel[i][0], poolRel[i][1], poolRel[i][2]);
                ArmorStand stand = markerStand(server, pos.x, pos.y, pos.z, true);
                stand.setCustomNameVisible(false);
                pozas.add(new Poza(stand, 0));
                if (target != null) {
                    double d = target.distanceToSqr(pos);
                    if (d > bestDist) {
                        bestDist = d;
                        furthest = i;
                    }
                }
            }
            setPozaName(server, pozas.get(furthest), true);
        }
        Vec3 vp = rel(data, -20, 6, 0);
        venenoAs = markerStand(server, vp.x, vp.y, vp.z, true);
        for (ServerPlayer player : server.players()) {
            int color = playerColors.getOrDefault(player.getScoreboardName(), 2);
            poisonTitle(server, player, color);
        }
    }

    private void poisonTitle(ServerLevel server, ServerPlayer player, int color) {
        String hex = color == 1 ? "#63FF00" : "#B74CDF";
        player.connection.send(new ClientboundSetTitleTextPacket(
                Component.translatable("luisb1202.functions.bossfight.b5.h4.fase_ilusion.ini_veneno.1")
                        .withStyle(Style.EMPTY.withColor(TextColor.parseColor(hex)))));
        player.connection.send(new ClientboundSetSubtitleTextPacket(
                Component.translatable(color == 1
                                ? "luisb1202.functions.bossfight.b5.h4.fase_ilusion.ini_veneno.2"
                                : "luisb1202.functions.bossfight.b5.h4.fase_ilusion.ini_veneno.3")
                        .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.parseColor(hex)))));
        server.playSound(null, player.blockPosition(), SoundEvents.BREWING_STAND_BREW,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        server.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT_DROWN,
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private void setPozaName(ServerLevel server, Poza poza, boolean green) {
        poza.color = green ? 1 : 2;
        if (poza.stand.isAlive()) {
            poza.stand.setCustomNameVisible(true);
            poza.stand.setCustomName(Component.translatable(green
                    ? "luisb1202.functions.bossfight.b5.h4.fase_ilusion.pozas.volver_verde.1"
                    : "luisb1202.functions.bossfight.b5.h4.fase_ilusion.pozas.volver_morado.1")
                    .withStyle(Style.EMPTY.withColor(TextColor.parseColor(green ? "#7AFF1E" : "#E76CFF"))));
        }
    }

    private void tickPozas(ServerLevel server, B5EncounterData data) {
        if (!data.poisonActive()) return;
        for (Poza poza : pozas) {
            if (!poza.stand.isAlive()) continue;
            if (poza.color == 1) {
                server.sendParticles(B5Particles.dust(0.251F, 1.0F, 0.063F, 1.8F),
                        poza.stand.getX(), poza.stand.getY() - 0.5D, poza.stand.getZ(), 2, 1, 0, 1, 0);
            } else if (poza.color == 2) {
                server.sendParticles(B5Particles.dust(0.78F, 0.063F, 1.0F, 1.8F),
                        poza.stand.getX(), poza.stand.getY() - 0.5D, poza.stand.getZ(), 2, 1, 0, 1, 0);
            }
        }
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator()) continue;
            int pc = playerColors.getOrDefault(player.getScoreboardName(), 2);
            for (Poza poza : pozas) {
                if (!poza.stand.isAlive() || poza.color == 0 || poza.color == pc) continue;
                if (player.distanceToSqr(poza.stand.position()) <= 2.0D * 2.0D) {
                    regenPoza(server, data, poza, player);
                    break;
                }
            }
        }
    }

    private void regenPoza(ServerLevel server, B5EncounterData data, Poza stepped, ServerPlayer player) {
        List<Poza> others = new ArrayList<>(pozas);
        others.remove(stepped);
        others.removeIf(p -> !p.stand.isAlive());
        if (!others.isEmpty()) {
            Poza other = others.get(server.random.nextInt(others.size()));
            setPozaName(server, other, stepped.color == 2);
        }
        int newColor = stepped.color;
        playerColors.put(player.getScoreboardName(), newColor);
        poisonTitle(server, player, newColor);
        server.sendParticles(ParticleTypes.EXPLOSION,
                stepped.stand.getX(), stepped.stand.getY(), stepped.stand.getZ(), 1, 0, 0, 0, 0);
        stepped.color = 0;
        stepped.stand.setCustomNameVisible(false);
    }

    private void tickPoison(ServerLevel server, B5EncounterData data) {
        if (!data.poisonActive()) return;
        data.setPoisonTicks(data.poisonTicks() + 1);
        if (venenoAs != null && venenoAs.isAlive()) {
            venenoAs.setYRot(venenoAs.getYRot() + 8.0F);
            for (ServerPlayer player : server.players()) {
                int color = playerColors.getOrDefault(player.getScoreboardName(), 2);
                server.sendParticles(player, color == 1
                                ? B5Particles.dust(0.533F, 1.0F, 0.0F, 2.0F)
                                : B5Particles.dust(0.757F, 0.243F, 0.859F, 2.0F),
                        false, player.getX(), relY(data, 1.5D), player.getZ(), 1, 12, 0.3D, 12, 0);
            }
        }
        for (Illusion illusion : illusions) {
            if (!illusion.entity.isAlive()) continue;
            for (ServerPlayer player : server.players()) {
                int pc = playerColors.getOrDefault(player.getScoreboardName(), 2);
                if ((pc == 1 && illusion.role == 1) || (pc == 2 && illusion.role == 2)) {
                    server.sendParticles(player, pc == 1
                                    ? B5Particles.dust(0.533F, 1.0F, 0.0F, 1.5F)
                                    : B5Particles.dust(0.757F, 0.243F, 0.859F, 1.5F),
                            true, illusion.entity.getX(), illusion.entity.getY() + 1.0D, illusion.entity.getZ(),
                            2, 0.3D, 0.6D, 0.3D, 0);
                }
            }
        }
    }

    private void startIllusionTrails(ServerLevel server, B5EncounterData data) {
        clearTrails();
        data.setIllusionActive(false);
        data.setInterEvent(0);
        List<Vec3> candidates = new ArrayList<>();
        double[] xs = {2, -4, -10, -16, -22, -28, -34, -40, -46};
        for (double x : xs) {
            candidates.add(rel(data, x, 1, 13));
            candidates.add(rel(data, x, 1, -13));
        }
        while (candidates.size() > 11) {
            candidates.remove(server.random.nextInt(candidates.size()));
        }
        GariBossEntity gari = gari(server, data);
        Vec3 origin = gari != null ? gari.position() : rel(data, -41, 1, -2);
        for (Vec3 target : candidates) {
            trails.add(new Trail(markerStand(server, origin.x, origin.y, origin.z, true), target));
        }
        B5Particles.squidBurst(server, origin);
        server.playSound(null, BlockPos.containing(origin), SoundEvents.FIREWORK_ROCKET_LAUNCH,
                SoundSource.PLAYERS, 2.0F, 0.8F);
        if (gari != null) {
            data.setGariUuid(null);
            gari.discard();
        }
        playForAll(server, SoundEvents.EVOKER_CAST_SPELL, 0.8F);
        startMusicInterIntro(server, data);
        data.setDirty();
    }

    private void tickTrails(ServerLevel server, B5EncounterData data) {
        if (trails.isEmpty()) return;
        Iterator<Trail> it = trails.iterator();
        while (it.hasNext()) {
            Trail trail = it.next();
            if (!trail.stand.isAlive()) {
                it.remove();
                continue;
            }
            for (int i = 0; i < 2; i++) {
                Vec3 move = localOffset(trail.stand, 0.2D, 0.2D, 0.4D);
                trail.stand.teleportTo(trail.stand.getX() + move.x,
                        trail.stand.getY() + move.y, trail.stand.getZ() + move.z);
                trail.stand.lookAt(EntityAnchorArgument.Anchor.EYES, trail.target);
            }
            for (ServerPlayer player : server.players()) {
                int color = playerColors.getOrDefault(player.getScoreboardName(), 2);
                server.sendParticles(player, color == 1
                                ? B5Particles.dust(0.4F, 1.0F, 0.0F, 1.5F)
                                : B5Particles.dust(0.718F, 0.0F, 1.0F, 1.5F),
                        true, trail.stand.getX(), trail.stand.getY() + 1.0D, trail.stand.getZ(),
                        1, 0, 0, 0, 0);
            }
            server.sendParticles(ParticleTypes.SQUID_INK,
                    trail.stand.getX(), trail.stand.getY() + 1.0D, trail.stand.getZ(), 1, 0, 0, 0, 0);
            if (trail.stand.distanceToSqr(trail.target) <= 1.0D) {
                endTrail(server, data, trail);
                it.remove();
            }
        }
        if (trails.isEmpty()) {
            endParte1(server, data);
        }
    }

    private void endTrail(ServerLevel server, B5EncounterData data, Trail trail) {
        B5Particles.squidBurst(server, trail.target);
        trail.stand.discard();
        server.playSound(null, BlockPos.containing(trail.target), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 2.0F, 1.0F);
        GariBossEntity illusion = GariBossEntity.createPrepared(server);
        if (illusion != null) {
            illusion.moveTo(trail.target.x, trail.target.y, trail.target.z, 0, 0);
            illusion.setNoAi(true);
            illusion.setInvulnerable(false);
            illusion.setBossBarEnabled(false);
            illusion.getPersistentData().putBoolean("B5Illusion", true);
            server.addFreshEntity(illusion);
            illusions.add(new Illusion(illusion, 0));
        }
    }

    private void endParte1(ServerLevel server, B5EncounterData data) {
        List<Illusion> pool = new ArrayList<>(illusions);
        Illusion real = pool.remove(server.random.nextInt(pool.size()));
        real.role = 0;
        for (int i = 0; i < 5 && !pool.isEmpty(); i++) {
            pool.remove(server.random.nextInt(pool.size())).role = 2;
        }
        for (Illusion illusion : pool) {
            illusion.role = 1;
        }
        for (Illusion illusion : illusions) {
            illusion.entity.setHealth(100.0F);
        }
        realIllusionPos = real.entity.position();
        data.setRealIllusionUuid(real.entity.getUUID());
        data.setIllusionActive(true);
        data.setIllusionTicks(0);
        data.setH4Timer(75);
        data.setH4DamagePhase(false);
        h4TimerTicks = 0;
        ScoreboardHelper.setupTimer(server, 75);
        scheduleTellraw(server, 20, dialogueGari("luisb1202.functions.bossfight.b5.dialogos.dia4.1"));
        scheduleKorosTip(server, 100, "luisb1202.functions.bossfight.b5.h4.msg_ayuda.1", true);
        startMusicInterFinal(server, data);
        data.setDirty();
    }

    private void tickH4Timer(ServerLevel server, B5EncounterData data) {
        if (data.h4Timer() < 0) return;
        if (!data.h4DamagePhase()) {
            h4TimerTicks++;
            if (h4TimerTicks % 20 == 0) {
                data.setH4Timer(data.h4Timer() - 1);
                ScoreboardHelper.updateTimer(server, Math.max(0, data.h4Timer()));
                if (data.h4Timer() == 22) {
                    scheduleTellraw(server, 0, dialogueGari("luisb1202.functions.bossfight.b5.dialogos.dia5.1"));
                }
                if (data.h4Timer() == 10) {
                    scheduleKorosTip(server, 0, "luisb1202.functions.bossfight.b5.h4.msg_10sec.1", true);
                }
            }
            if (data.h4Timer() <= 0) {
                data.setH4DamagePhase(true);
                data.setH4DamageTimer(0);
                scheduleKorosTip(server, 0, "luisb1202.functions.bossfight.b5.h4.msg_damage.1", true);
            }
        } else {
            data.setH4DamageTimer(data.h4DamageTimer() + 1);
            if (data.h4DamageTimer() % 40 == 0) {
                for (ServerPlayer player : server.players()) {
                    if (player.isSpectator()) continue;
                    server.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                            player.getX(), player.getY() + 1.0D, player.getZ(), 15, 0, 0, 0, 1);
                    server.sendParticles(ParticleTypes.EXPLOSION,
                            player.getX(), player.getY() + 0.6D, player.getZ(), 1, 0, 0, 0, 1);
                    // damage.mcfunction executes `effect give @a` once as each living participant.
                    for (ServerPlayer target : server.players()) {
                        target.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 2));
                    }
                    server.playSound(null, player.blockPosition(), SoundEvents.MAGMA_CUBE_SQUISH,
                            SoundSource.PLAYERS, 1.0F, 1.5F);
                    server.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT_ON_FIRE,
                            SoundSource.PLAYERS, 1.0F, 1.8F);
                }
            }
        }
    }

    private void tickH4Projectiles(ServerLevel server, B5EncounterData data) {
        if (!h4ProjectilesActive) return;
        h4ProjectileRunTicks++;
        if (h4ProjectileRunTicks == 1 || h4ProjectileRunTicks == 11 || h4ProjectileRunTicks == 21) {
            fireH4Volley(server, data);
        }
        if (h4ProjectileRunTicks >= 21) {
            h4ProjectilesActive = false;
            data.setInterEvent(0);
        }
    }

    private void fireH4Volley(ServerLevel server, B5EncounterData data) {
        List<Illusion> eligible = new ArrayList<>();
        for (Illusion illusion : illusions) {
            if (!illusion.entity.isAlive() || illusion.role == 0) continue;
            boolean far = true;
            for (ServerPlayer player : server.players()) {
                if (!player.isSpectator() && player.distanceToSqr(illusion.entity) <= 16.0D * 16.0D) {
                    far = false;
                    break;
                }
            }
            if (far) eligible.add(illusion);
        }
        for (int i = 0; i < 2 && !eligible.isEmpty(); i++) {
            Illusion illusion = eligible.remove(server.random.nextInt(eligible.size()));
            ServerPlayer target = randomPlayer(server);
            if (target == null) continue;
            Vec3 origin = illusion.entity.position();
            Vec3 toward = target.position().subtract(origin);
            Vec3 dir = new Vec3(toward.x, 0.0D, toward.z).normalize();
            projectiles.add(new Projectile(origin, dir.scale(0.23D), Projectile.DISPARO));
            server.sendParticles(ParticleTypes.EXPLOSION,
                    illusion.entity.getX(), illusion.entity.getY() + 1.0D, illusion.entity.getZ(), 1, 0, 0, 0, 0);
            server.playSound(null, illusion.entity.blockPosition(), SoundEvents.CROSSBOW_SHOOT,
                    SoundSource.PLAYERS, 3.0F, 0.8F);
        }
    }

    private void tickIllusions(ServerLevel server, B5EncounterData data) {
        if (!data.illusionActive()) return;
        data.setIllusionTicks(data.illusionTicks() + 1);
        for (Illusion illusion : illusions) {
            if (!illusion.entity.isAlive()) continue;
            ServerPlayer nearest = null;
            double best = Double.MAX_VALUE;
            for (ServerPlayer player : server.players()) {
                double d = player.distanceToSqr(illusion.entity);
                if (d < best) {
                    best = d;
                    nearest = player;
                }
            }
            if (nearest != null) {
                illusion.entity.lookAt(EntityAnchorArgument.Anchor.EYES, nearest.position());
            }
        }
        Iterator<Illusion> it = illusions.iterator();
        while (it.hasNext()) {
            Illusion illusion = it.next();
            if (illusion.entity.isAlive()) continue;
            if (illusion.role == 0) {
                illusionExito(server, data);
                return;
            }
            illusionBoom(server, data, illusion.entity.position());
            it.remove();
        }
        if (illusions.isEmpty()) return;
    }

    private void illusionBoom(ServerLevel server, B5EncounterData data, Vec3 pos) {
        B5Particles.h2Boom(server, new Vec3(pos.x, relY(data, 1), pos.z));
        data.setPoisonDano(data.poisonDano() + 1);
        int amp = data.poisonDano() <= 1 ? 1 : 5;
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator()) continue;
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
            player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, amp));
        }
        int variant = server.random.nextInt(3) + 1;
        scheduleDialogues(server, new int[]{0},
                new String[]{"luisb1202.functions.bossfight.b5.dialogos.dia2." + variant}, new int[]{0});
    }

    private void illusionExito(ServerLevel server, B5EncounterData data) {
        data.setIllusionActive(false);
        for (Illusion illusion : illusions) {
            if (illusion.entity.isAlive() && illusion.role != 0) illusion.entity.discard();
        }
        illusions.clear();
        data.setH4Timer(-1);
        data.setH4DamagePhase(false);
        ScoreboardHelper.removeTimer(server);
        GariBossEntity real = GariBossEntity.createPrepared(server);
        if (real != null) {
            Vec3 pos = realIllusionPos != null ? realIllusionPos : rel(data, -41, 1, -2);
            real.moveTo(pos.x, pos.y, pos.z, 90, 0);
            server.addFreshEntity(real);
            data.setGariUuid(real.getUUID());
        }
        playForAll(server, SoundEvents.PLAYER_LEVELUP, 2.0F);
        scheduleTellraw(server, 20, dialogueGari("luisb1202.functions.bossfight.b5.dialogos.dia6.1"));
        startMusicInterFinal(server, data);
        int next = data.fase() == INTER_1 ? FASE_2 : FASE_3;
        enterFase2(server, data, next);
    }

    /* ------------------------------ projectiles ------------------------------ */

    private void tickProjectiles(ServerLevel server, B5EncounterData data) {
        if (projectiles.isEmpty()) return;
        if (projectiles.stream().anyMatch(projectile -> projectile.kind == Projectile.H5)) {
            h5ParticleFrame = h5ParticleFrame % 12 + 1;
        }
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.age++;
            if (p.age > 600) {
                it.remove();
                continue;
            }
            if (p.kind == Projectile.H5) {
                ServerPlayer target = server.getServer().getPlayerList().getPlayer(p.target);
                if (target == null || target.isSpectator()) {
                    it.remove();
                    continue;
                }
                Vec3 aim = target.getEyePosition().subtract(p.pos);
                boolean playerNearby = server.players().stream()
                        .anyMatch(player -> player.distanceToSqr(p.pos) <= 12.0D * 12.0D);
                double speed = playerNearby ? 0.16D : 0.26D;
                if (p.age >= 260) speed += 0.04D;
                if (p.age >= 360) speed += 0.04D;
                if (p.age >= 460) speed += 0.15D;
                p.pos = p.pos.add(aim.normalize().scale(speed));
                server.sendParticles(B5Particles.dust(0.686F, 0.227F, 0.745F, 2.3F),
                        p.pos.x, p.pos.y - 0.3D, p.pos.z, 1, 0, 0, 0, 0);
                server.sendParticles(B5Particles.dust(0.541F, 0.933F, 0.22F, 0.8F),
                        p.pos.x, p.pos.y - 0.3D, p.pos.z, 2, 0.1D, 0.1D, 0.1D, 0);
                Vec3 trailAim = target.getEyePosition().subtract(p.pos);
                B5Particles.h5Wing(server, p.pos, trailAim, h5ParticleFrame);
                Vec3 hitCheck = p.pos.add(0, -0.8D, 0);
                ServerPlayer hitPlayer = nearestPlayer(server, hitCheck, 1.4D);
                if (hitPlayer != null) {
                    h5Boom(server, hitPlayer, hitCheck);
                    it.remove();
                    continue;
                }
                KoyomiBossEntity koyo = koyo(server, data);
                if (koyo != null && koyo.isAlive() && koyo.distanceToSqr(hitCheck) <= 2.5D * 2.5D) {
                    h5BoomKoyo(server, hitCheck);
                    it.remove();
                    continue;
                }
            } else {
                p.pos = p.pos.add(p.vel);
                // h4/proyectiles/run uses count=0: delta becomes one exact
                // directional velocity instead of a random spawn spread.
                server.sendParticles(ParticleTypes.END_ROD,
                        p.pos.x, p.pos.y + 1.2D, p.pos.z, 0, 0, 1, 0, 100000);
                server.sendParticles(B5Particles.dust(0.831F, 0.106F, 0.925F, 1.0F),
                        p.pos.x, p.pos.y + 1.2D, p.pos.z, 1, 0, 0, 0, 0);
                boolean hit = false;
                for (ServerPlayer player : server.players()) {
                    if (!player.isSpectator() && player.distanceToSqr(p.pos) <= 1.0D) {
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 0, true, false));
                        player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
                        server.playSound(null, player.blockPosition(), SoundEvents.ARROW_HIT,
                                SoundSource.PLAYERS, 1.0F, 1.2F);
                        server.sendParticles(ParticleTypes.CRIT,
                                player.getX(), player.getY() + 1.0D, player.getZ(), 10, 0, 0, 0, 0.5D);
                        server.sendParticles(ParticleTypes.SWEEP_ATTACK,
                                player.getX(), player.getY() + 1.0D, player.getZ(), 1, 0, 0, 0, 0);
                        hit = true;
                        break;
                    }
                }
                if (!hit && (p.pos.x < data.anchor().getX() - 58 || p.pos.x > data.anchor().getX() + 14
                        || p.pos.z < data.anchor().getZ() - 21 || p.pos.z > data.anchor().getZ() + 21)) {
                    it.remove();
                    continue;
                }
                if (hit) it.remove();
            }
        }
    }

    private void h5Boom(ServerLevel server, ServerPlayer target, Vec3 pos) {
        B5Particles.critFan(server, pos);
        server.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.x, pos.y, pos.z, 14, 0, 0, 0, 1);
        server.sendParticles(B5Particles.dust(0.686F, 0.227F, 0.745F, 2.3F),
                pos.x, pos.y, pos.z, 15, 1.5D, 1.5D, 1.5D, 0);
        server.sendParticles(B5Particles.dust(0.541F, 0.933F, 0.22F, 0.8F),
                pos.x, pos.y, pos.z, 25, 1.5D, 1.5D, 1.5D, 0);
        server.playSound(null, BlockPos.containing(pos), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 2.0F, 0.6F);
        server.playSound(null, BlockPos.containing(pos), SoundEvents.DROWNED_HURT_WATER, SoundSource.PLAYERS, 2.0F, 0.6F);
        server.playSound(null, BlockPos.containing(pos), SoundEvents.PLAYER_HURT_ON_FIRE, SoundSource.PLAYERS, 2.0F, 0.6F);
        int count = data.incrementH5Hits(target.getUUID());
        int amp = switch (Math.min(4, count)) {
            case 1, 2 -> 2;
            case 3 -> 3;
            default -> 4;
        };
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 0, true, false));
        target.addEffect(new MobEffectInstance(MobEffects.HARM, 1, amp));
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 240, 0));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 0));
    }

    private void h5BoomKoyo(ServerLevel server, Vec3 pos) {
        B5Particles.critFan(server, pos);
        server.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y + 1.0D, pos.z, 1, 0, 0, 0, 0);
        server.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 1.0D, pos.z, 14, 0, 0, 0, 0.3D);
        server.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.x, pos.y + 1.0D, pos.z, 14, 0, 0, 0, 1.0D);
        server.sendParticles(B5Particles.dust(0.686F, 0.227F, 0.745F, 2.3F),
                pos.x, pos.y, pos.z, 15, 1.5D, 1.5D, 1.5D, 0);
        server.sendParticles(B5Particles.dust(0.541F, 0.933F, 0.22F, 0.8F),
                pos.x, pos.y, pos.z, 25, 1.5D, 1.5D, 1.5D, 0);
        server.playSound(null, BlockPos.containing(pos), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 2.0F, 0.6F);
        server.playSound(null, BlockPos.containing(pos), SoundEvents.DROWNED_HURT_WATER, SoundSource.PLAYERS, 2.0F, 0.6F);
        server.playSound(null, BlockPos.containing(pos), SoundEvents.PLAYER_HURT_ON_FIRE, SoundSource.PLAYERS, 2.0F, 0.6F);
        server.playSound(null, BlockPos.containing(pos), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 2.0F);
        if (!h5KoyoDialoguePlayed) {
            h5KoyoDialoguePlayed = true;
            int variant = server.random.nextInt(4) + 1;
            Component line = variant <= 2
                    ? dialogueGari("luisb1202.functions.bossfight.b5.dialogos.dia11." + variant)
                    : dialogueKoyo("luisb1202.functions.bossfight.b5.dialogos.dia11." + variant);
            dialogueQueue.add(new Dialogue(server.getServer().getTickCount(), line,
                    SoundEvents.PILLAGER_AMBIENT, 1.2F));
        }
    }

    /* ------------------------------ finale / victory / defeat ------------------------------ */

    private void startFinale(ServerLevel server, B5EncounterData data) {
        clearTrustMarker();
        data.setFase(FASE_4);
        data.setFinaleTicks(0);
        data.setFinalePrepared(false);
        data.setDialogueCounter(0);
        data.clearDeadPlayers();
        data.clearH5Hits();
        data.clearH3IntermissionHits();
        data.setShieldBearer(SHIELD_NONE);
        resetH2(server, data);
        resetH3(server, data);
        resetH5(server, data);
        resetH7(server, data);
        clearIntermissionVisuals(server, data);
        data.setH4Timer(-1);
        data.setH4DamagePhase(false);
        ScoreboardHelper.removeTimer(server);
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator()) {
                Vec3 tp = rel(data, -17, 1, 0);
                player.teleportTo(tp.x, tp.y, tp.z);
                player.setGameMode(GameType.ADVENTURE);
            }
        }
        KoyomiBossEntity koyo = koyo(server, data);
        GariBossEntity gari = gari(server, data);
        if (koyo != null) {
            koyo.setNoAi(false);
            koyo.setInvulnerable(true);
            koyo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 999999, 100, true, false));
            koyo.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 999999, 100, true, false));
        }
        if (gari != null) {
            gari.setNoAi(false);
            gari.setInvulnerable(true);
            gari.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 999999, 100, true, false));
            gari.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 999999, 100, true, false));
            gari.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    private void tickFinale(ServerLevel server, B5EncounterData data) {
        data.setFinaleTicks(data.finaleTicks() + 1);
        if (!data.finalePrepared() && data.finaleTicks() >= 20) {
            data.setFinalePrepared(true);
            tpEnd(server, data);
        }
        int nextStage = data.dialogueCounter() + 1;
        if (data.finalePrepared() && nextStage <= FINALE_STAGE_TICKS.length
                && data.finaleTicks() >= FINALE_STAGE_TICKS[nextStage - 1]) {
            emitFinaleStage(server, data, nextStage);
            data.setDialogueCounter(nextStage);
        }
        if (data.dialogueCounter() >= 17 && data.finaleTicks() >= 1620) victory(server, data);
    }

    private void tpEnd(ServerLevel server, B5EncounterData data) {
        resetH2(server, data);
        resetH3(server, data);
        resetH5(server, data);
        resetH7(server, data);
        clearIntermissionVisuals(server, data);
        removeBarriers(server, data);
        KoyomiBossEntity oldKoyo = koyo(server, data);
        GariBossEntity oldGari = gari(server, data);
        if (oldKoyo != null) oldKoyo.discard();
        if (oldGari != null) oldGari.discard();
        KoyomiBossEntity koyo = KoyomiBossEntity.createPrepared(server);
        GariBossEntity gari = GariBossEntity.createPrepared(server);
        if (koyo != null) {
            Vec3 pos = rel(data, -41, 1, 2);
            koyo.moveTo(pos.x, pos.y, pos.z, -90, 0);
            koyo.setInvulnerable(true);
            koyo.setNoAi(true);
            koyo.setBossBarEnabled(false);
            server.addFreshEntity(koyo);
            data.setKoyoUuid(koyo.getUUID());
            B5Particles.squidBurst(server, koyo.position());
        }
        if (gari != null) {
            Vec3 pos = rel(data, -41, 1, -2);
            gari.moveTo(pos.x, pos.y, pos.z, -90, 0);
            gari.setInvulnerable(true);
            gari.setNoAi(true);
            gari.setBossBarEnabled(false);
            server.addFreshEntity(gari);
            data.setGariUuid(gari.getUUID());
            B5Particles.squidBurst(server, gari.position());
        }
        data.setShieldBearer(SHIELD_GARI);
        data.setDialogueCounter(0);
        dialogueQueue.clear();
        startMusicAbatir(server, data);
    }

    private void emitFinaleStage(ServerLevel server, B5EncounterData data, int stage) {
        int suffix = switch (stage) {
            case 3 -> 4;
            case 4 -> 5;
            case 5 -> 6;
            case 6 -> 7;
            case 7 -> 8;
            case 8 -> 9;
            case 9 -> 10;
            case 10 -> 11;
            case 11 -> 12;
            case 12 -> 13;
            case 13 -> 14;
            case 14 -> 15;
            case 15 -> 16;
            case 16 -> 17;
            case 17 -> 18;
            default -> stage;
        };
        boolean koyoLine = stage == 3 || stage == 5 || stage == 8 || stage == 9
                || stage == 13 || stage == 14 || stage == 17;
        Component message = koyoLine
                ? Component.translatable("luisb1202.functions.bossfight.b5.dialogos.dia_end.3")
                .withStyle(Style.EMPTY.withColor(TextColor.parseColor("#ea3434")))
                .copy().append(Component.translatable("luisb1202.functions.bossfight.b5.dialogos.dia_end." + suffix)
                        .withStyle(Style.EMPTY.withColor(TextColor.parseColor("#ffa4be"))))
                : Component.translatable("luisb1202.functions.bossfight.b5.dialogos.dia_end." + suffix)
                .withStyle(stage == 7 ? Style.EMPTY.withColor(TextColor.parseColor("#FBBDFF")) : Style.EMPTY);
        for (ServerPlayer player : server.players()) {
            player.sendSystemMessage(message);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 10, true, false));
            if (player.isSpectator()) {
                Vec3 tp = rel(data, -17, 1, 0);
                player.teleportTo(tp.x, tp.y, tp.z);
                player.setGameMode(GameType.ADVENTURE);
            }
        }
        if (stage == 7) playForAll(server, SoundEvents.TRIDENT_RETURN, 1.7F);
        else playForAll(server, SoundEvents.PILLAGER_AMBIENT, koyoLine ? 1.2F : 1.7F);
        KoyomiBossEntity koyo = koyo(server, data);
        GariBossEntity gari = gari(server, data);
        if (stage == 3 && koyo != null && gari != null) {
            koyo.lookAt(EntityAnchorArgument.Anchor.EYES, gari.getEyePosition());
            koyo.setXRot(0.0F);
        } else if (stage == 4 && koyo != null && gari != null) {
            gari.lookAt(EntityAnchorArgument.Anchor.EYES, koyo.getEyePosition());
            gari.setXRot(0.0F);
        } else if (stage == 6 && gari != null) {
            gari.lookAt(EntityAnchorArgument.Anchor.EYES, rel(data, -18, 2, 0));
        } else if (stage == 7 && koyo != null) {
            koyo.lookAt(EntityAnchorArgument.Anchor.EYES, rel(data, -18, 2, 0));
        }
        if (stage == 16) gariIrse(server, data);
        if (stage == 17) koyoIrseAndOpenFleccy(server, data);
    }

    private void gariIrse(ServerLevel server, B5EncounterData data) {
        GariBossEntity gari = gari(server, data);
        if (gari != null) {
            dropReward(server, gari.position());
            B5Particles.squidBurst(server, gari.position());
            gari.discard();
        }
    }

    private void koyoIrseAndOpenFleccy(ServerLevel server, B5EncounterData data) {
        KoyomiBossEntity koyo = koyo(server, data);
        if (koyo != null) {
            B5Particles.squidBurst(server, koyo.position());
            koyo.discard();
        }
        openFleccy(server, data);
    }

    private void openFleccy(ServerLevel server, B5EncounterData data) {
        int ax = data.anchor().getX();
        int az = data.anchor().getZ();
        for (int y = 0; y <= 4; y++) {
            for (int dz = 4; dz >= 0; dz--) {
                BlockPos pos = new BlockPos(ax - 48, data.anchor().getY() + 2 + y, az + 2 - dz);
                if (server.getBlockState(pos).getBlock() == Blocks.CYAN_STAINED_GLASS
                        || server.getBlockState(pos).getBlock() == Blocks.CYAN_STAINED_GLASS_PANE) {
                    server.destroyBlock(pos, false);
                }
            }
        }
        Vec3 c = rel(data, -47, 5, 0);
        server.sendParticles(ParticleTypes.CLOUD, c.x, c.y, c.z, 15, 0, 1, 0, 0);
    }

    private void victory(ServerLevel server, B5EncounterData data) {
        if (data.victoryPlayed()) return;
        data.setVictoryPlayed(true);
        leaveFleccyOpen = true;
        broadcastTitle("luisb1202.functions.bossfight.b1.victoria.1");
        broadcastSubtitle("luisb1202.functions.bossfight.b5.victoria.1");
        playForAll(server, SoundEvents.PLAYER_LEVELUP, 0.8F);
        for (ServerPlayer player : server.players()) {
            player.setGameMode(GameType.SURVIVAL);
        }
        endEncounter(server, data);
    }

    private void defeat(ServerLevel server, B5EncounterData data) {
        if (data.respawnScheduled()) return;
        data.setRespawnScheduled(true);
        data.setRespawnTicks(0);
        broadcastTitle("luisb1202.functions.bossfight.b1.derrota.1");
        broadcastSubtitle("luisb1202.functions.bossfight.b1.derrota.2");
        playForAll(server, SoundEvents.WITHER_DEATH, 1.8F);
        scheduleDialogues(server, new int[]{0, 50, 98},
                new String[]{
                        "luisb1202.functions.bossfight.b5.dialogos.dia15.1",
                        "luisb1202.functions.bossfight.b5.dialogos.dia15.2",
                        "luisb1202.functions.bossfight.b5.dialogos.dia15.3"},
                new int[]{0, 1, 0});
    }

    private void endEncounter(ServerLevel server, B5EncounterData data) {
        KoyomiBossEntity koyo = koyo(server, data);
        GariBossEntity gari = gari(server, data);
        if (koyo != null) {
            koyo.setBossBarEnabled(false);
            koyo.discard();
        }
        if (gari != null) {
            gari.setBossBarEnabled(false);
            gari.discard();
        }
        resetH2(server, data);
        resetH3(server, data);
        resetH5(server, data);
        resetH7(server, data);
        clearIntermissionVisuals(server, data);
        clearTransient();
        ScoreboardHelper.removeTimer(server);
        removeBarriers(server, data);
        if (!leaveFleccyOpen) {
            closeFleccy(server, data);
        }
        stopMusic(server, data);
        for (ServerPlayer player : server.players()) {
            if (data.isDeadPlayer(player.getUUID()) && player.isSpectator()) {
                player.setGameMode(GameType.ADVENTURE);
            }
        }
        data.clearDeadPlayers();
        data.clearH5Hits();
        data.clearH3IntermissionHits();
        data.setActive(false);
        data.setFase(0);
        data.setCountdownTicks(0);
        data.setFinalePrepared(false);
        data.setDirty();
    }

    /** Public reset entry used by the manager. */
    public void endEncounterPublic(ServerLevel server, B5EncounterData data) {
        endEncounter(server, data);
    }

    private void respawn(ServerLevel server, B5EncounterData data) {
        endEncounter(server, data);
        KoyomiBossEntity koyo = KoyomiBossEntity.createPrepared(server);
        GariBossEntity gari = GariBossEntity.createPrepared(server);
        if (koyo != null) {
            Vec3 pos = rel(data, -41, 1, 2);
            koyo.moveTo(pos.x, pos.y, pos.z, -90, 0);
            koyo.setInvulnerable(true);
            koyo.setNoAi(true);
            koyo.setBossBarEnabled(false);
            server.addFreshEntity(koyo);
        }
        if (gari != null) {
            Vec3 pos = rel(data, -41, 1, -2);
            gari.moveTo(pos.x, pos.y, pos.z, -90, 0);
            gari.setInvulnerable(true);
            gari.setNoAi(true);
            gari.setBossBarEnabled(false);
            server.addFreshEntity(gari);
        }
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator()) {
                Vec3 tp = rel(data, -17, 1, 0);
                player.teleportTo(tp.x, tp.y, tp.z);
                player.setGameMode(GameType.ADVENTURE);
            }
            player.removeEffect(MobEffects.WITHER);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 101, true, false));
        }
        data.setActive(false);
        data.setRespawnScheduled(false);
        data.setDirty();
    }

    /* ------------------------------ reward ------------------------------ */

    private void dropReward(ServerLevel server, Vec3 pos) {
        ItemStack stack = new ItemStack(Items.ORANGE_DYE);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("chapa_gariheuz", 1);
        ListTag canPlace = new ListTag();
        canPlace.add(StringTag.valueOf("minecraft:structure_void"));
        tag.put("CanPlaceOn", canPlace);
        CompoundTag display = new CompoundTag();
        display.putString("Name", jsonText("luisb1202.functions.bossfight.b5.recompensa.1", "#987764"));
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(jsonText("empty", null)));
        lore.add(StringTag.valueOf(jsonText("luisb1202.functions.bossfight.b5.recompensa.2", null)));
        lore.add(StringTag.valueOf(jsonText("luisb1202.functions.bossfight.b5.recompensa.3", null)));
        lore.add(StringTag.valueOf(jsonText("luisb1202.functions.bossfight.b5.recompensa.4", null)));
        lore.add(StringTag.valueOf(jsonText("empty", null)));
        lore.add(StringTag.valueOf(jsonText("item.quartz.1.lore.7.1", null)));
        lore.add(StringTag.valueOf(jsonText("empty", null)));
        display.put("Lore", lore);
        tag.put("display", display);
        ListTag enchantments = new ListTag();
        enchantments.add(new CompoundTag());
        tag.put("Enchantments", enchantments);
        tag.putInt("HideFlags", 16);
        ItemEntity item = new ItemEntity(server, pos.x, pos.y + 1.0D, pos.z, stack);
        item.setPickUpDelay(25);
        // Source Age:32768 is read through ItemEntity's short age field as -32768.
        item.setUnlimitedLifetime();
        item.setDeltaMovement(0, 0.5D, 0);
        server.addFreshEntity(item);
        server.sendParticles(ParticleTypes.FLASH, pos.x, pos.y + 1.0D, pos.z, 1, 0, 0, 0, 0);
        server.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y + 1.0D, pos.z, 1, 0, 0, 0, 0);
        server.playSound(null, pos.x, pos.y, pos.z, SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 1.0F, 2.0F);
    }

    private static String jsonText(String key, String color) {
        String c = color != null ? ",\"color\":\"" + color + "\"" : "";
        return "{\"translate\":\"" + key + "\"" + c + "}";
    }

    /* ------------------------------ music ------------------------------ */

    private void startMusicMain(ServerLevel server, B5EncounterData data) {
        playRecord(server, ModSounds.KOYOMI_MAIN_INTRO.get());
        data.setMusicPhase(1);
        data.setMusicTicks(0);
    }

    private void startMusicInterIntro(ServerLevel server, B5EncounterData data) {
        playRecord(server, ModSounds.KOYOMI_INTER_INTRO.get());
        data.setMusicPhase(3);
        data.setMusicTicks(0);
    }

    private void startMusicInterFinal(ServerLevel server, B5EncounterData data) {
        stopMusic(server, data);
        playRecord(server, ModSounds.KOYOMI_INTER_FINAL.get());
        data.setMusicPhase(5);
        data.setMusicTicks(0);
    }

    private void startMusicAbatir(ServerLevel server, B5EncounterData data) {
        stopMusic(server, data);
        playRecord(server, ModSounds.ABATIR_JEFE.get());
        data.setMusicPhase(6);
        data.setMusicTicks(0);
    }

    private void stopMusic(ServerLevel server, B5EncounterData data) {
        for (ServerPlayer player : server.players()) {
            stopRecord(player, ModSounds.KOYOMI_MAIN_INTRO.get());
            stopRecord(player, ModSounds.KOYOMI_MAIN_LOOP.get());
            stopRecord(player, ModSounds.KOYOMI_INTER_INTRO.get());
            stopRecord(player, ModSounds.KOYOMI_INTER_LOOP.get());
            stopRecord(player, ModSounds.KOYOMI_INTER_FINAL.get());
            stopRecord(player, ModSounds.ABATIR_JEFE.get());
        }
        data.setMusicPhase(0);
        data.setMusicTicks(0);
    }

    private void tickMusic(ServerLevel server, B5EncounterData data) {
        int phase = data.musicPhase();
        if (phase == 0) return;
        data.setMusicTicks(data.musicTicks() + 1);
        int t = data.musicTicks();
        switch (phase) {
            case 1 -> {
                if (t >= (int) (122.55D * 20.0D)) {
                    playRecord(server, ModSounds.KOYOMI_MAIN_LOOP.get());
                    data.setMusicPhase(2);
                    data.setMusicTicks(0);
                }
            }
            case 2 -> {
                if (t >= (int) (112.34D * 20.0D)) {
                    playRecord(server, ModSounds.KOYOMI_MAIN_LOOP.get());
                    data.setMusicTicks(0);
                }
            }
            case 3 -> {
                if (t >= (int) (2.2D * 20.0D)) {
                    stopMainMusic(server);
                }
                if (t >= (int) (53.23D * 20.0D)) {
                    playRecord(server, ModSounds.KOYOMI_INTER_LOOP.get());
                    data.setMusicPhase(4);
                    data.setMusicTicks(0);
                }
            }
            case 4 -> {
                if (t >= (int) (51.06D * 20.0D)) {
                    playRecord(server, ModSounds.KOYOMI_INTER_LOOP.get());
                    data.setMusicTicks(0);
                }
            }
            case 5 -> {
                if (t >= (int) (71.49D * 20.0D)) {
                    playRecord(server, ModSounds.KOYOMI_MAIN_LOOP.get());
                    data.setMusicPhase(2);
                    data.setMusicTicks(0);
                }
            }
            default -> {
            }
        }
    }

    private void stopMainMusic(ServerLevel server) {
        for (ServerPlayer player : server.players()) {
            stopRecord(player, ModSounds.KOYOMI_MAIN_INTRO.get());
            stopRecord(player, ModSounds.KOYOMI_MAIN_LOOP.get());
            stopRecord(player, ModSounds.KOYOMI_INTER_FINAL.get());
        }
    }

    private static void stopRecord(ServerPlayer player, SoundEvent event) {
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                event.getLocation(), null));
    }

    private void playRecord(ServerLevel server, SoundEvent event) {
        for (ServerPlayer player : server.players()) {
            player.playNotifySound(event, SoundSource.RECORDS, 999999.0F, 1.0F);
        }
    }

    /* ------------------------------ arena blocks ------------------------------ */

    private void placeBarriers(ServerLevel server, B5EncounterData data) {
        int ax = data.anchor().getX();
        int ay = data.anchor().getY();
        int az = data.anchor().getZ();
        fillBarrier(server, ax + 19, ay + 11, az - 7, ax + 12, ay + 2, az + 7);
        fillBarrier(server, ax - 60, ay - 5, az + 20, ax - 60, ay + 21, az - 20);
        fillBarrier(server, ax - 57, ay - 4, az - 24, ax + 13, ay + 16, az - 24);
        fillBarrier(server, ax - 57, ay - 4, az + 24, ax + 14, ay + 17, az + 24);
        fillBarrier(server, ax + 16, ay - 4, az + 20, ax + 16, ay + 15, az - 20);
        fillBarrier(server, ax - 49, ay + 1, az + 11, ax - 51, ay + 11, az + 9);
        fillBarrier(server, ax - 51, ay + 1, az - 9, ax - 49, ay + 11, az - 11);
        fillBarrier(server, ax + 7, ay + 11, az - 11, ax + 5, ay + 1, az - 9);
        fillBarrier(server, ax + 7, ay + 11, az + 11, ax + 5, ay + 1, az + 9);
        fillBarrier(server, ax - 47, ay + 11, az + 4, ax - 55, ay + 8, az - 4);
        fillBarrier(server, ax - 56, ay + 9, az - 4, ax - 56, ay + 9, az + 4);
    }

    private void removeBarriers(ServerLevel server, B5EncounterData data) {
        int ax = data.anchor().getX();
        int ay = data.anchor().getY();
        int az = data.anchor().getZ();
        clearBarrier(server, ax + 19, ay + 11, az - 7, ax + 12, ay + 2, az + 7);
        clearBarrier(server, ax - 60, ay - 5, az + 20, ax - 60, ay + 21, az - 20);
        clearBarrier(server, ax - 57, ay - 4, az - 24, ax + 13, ay + 16, az - 24);
        clearBarrier(server, ax - 57, ay - 4, az + 24, ax + 14, ay + 17, az + 24);
        clearBarrier(server, ax + 16, ay - 4, az + 20, ax + 16, ay + 15, az - 20);
        clearBarrier(server, ax - 49, ay + 1, az + 11, ax - 51, ay + 11, az + 9);
        clearBarrier(server, ax - 51, ay + 1, az - 9, ax - 49, ay + 11, az - 11);
        clearBarrier(server, ax + 7, ay + 11, az - 11, ax + 5, ay + 1, az - 9);
        clearBarrier(server, ax + 7, ay + 11, az + 11, ax + 5, ay + 1, az + 9);
        clearBarrier(server, ax - 47, ay + 11, az + 4, ax - 55, ay + 8, az - 4);
        clearBarrier(server, ax - 56, ay + 9, az - 4, ax - 56, ay + 9, az + 4);
    }

    private static void fillBarrier(ServerLevel server, int x1, int y1, int z1, int x2, int y2, int z2) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (server.getBlockState(pos).isAir()) {
                        server.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static void clearBarrier(ServerLevel server, int x1, int y1, int z1, int x2, int y2, int z2) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (server.getBlockState(pos).getBlock() == Blocks.BARRIER) {
                        server.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private void closeFleccy(ServerLevel server, B5EncounterData data) {
        int ax = data.anchor().getX();
        int az = data.anchor().getZ();
        for (int y = 0; y <= 4; y++) {
            for (int dz = 4; dz >= 0; dz--) {
                server.setBlock(new BlockPos(ax - 48, data.anchor().getY() + 2 + y, az + 2 - dz),
                        Blocks.CYAN_STAINED_GLASS.defaultBlockState(), 3);
            }
        }
        server.setBlock(new BlockPos(ax - 48, data.anchor().getY() + 4, az),
                Blocks.CYAN_STAINED_GLASS_PANE.defaultBlockState(), 3);
    }

    /* ------------------------------ tick glue ------------------------------ */

    private void tickAttacks(ServerLevel server, B5EncounterData data,
                             KoyomiBossEntity koyo, GariBossEntity gari) {
        int now = server.getServer().getTickCount();
        if (data.h5VolleyActive()) {
            int round = data.h5FlechaRound();
            if (round < 3 && now >= h5FireAt[round]) {
                fireH5Flecha(server, data, gari);
                data.setH5FlechaRound(round + 1);
                if (round + 1 >= 3) data.setH5VolleyActive(false);
            }
        }
        Iterator<Integer> h6it = pendingH6.iterator();
        while (h6it.hasNext()) {
            if (now >= h6it.next()) {
                startH6(server, data);
                h6it.remove();
            }
        }
        Iterator<Integer> h7it = pendingH7.iterator();
        while (h7it.hasNext()) {
            if (now >= h7it.next()) {
                startH7Burst(server, data, gari);
                h7it.remove();
            }
        }
        tickH6(server, data, gari);
        tickH7(server, data, gari);
        tickH3(server, data, koyo);
        tickH2(server, data);
        long h2Due = data.h2PendingDue();
        if (h2Due > 0L && server.getGameTime() >= h2Due) {
            data.setH2PendingDue(0L);
            if (data.shieldBearer() == SHIELD_GARI) startH2(server, data);
        }
    }

    private void tickShieldRing(ServerLevel server, B5EncounterData data, KoyomiBossEntity koyo) {
        if (data.shieldBearer() < 0) return;
        LivingEntity shielded = data.shieldBearer() == SHIELD_KOYO ? koyo : gari(server, data);
        if (shielded == null || !shielded.isAlive()) return;
        shieldFrame++;
        int frame = Math.floorMod(shieldFrame - 1, 16) + 1;
        double radius = frame <= 2 || frame >= 15 ? 1.5D
                : frame <= 4 ? 1.45D
                : frame <= 6 || frame >= 13 ? 1.4D
                : frame <= 8 || frame >= 11 ? 1.3D : 1.25D;
        B5Particles.shieldRing(server, shielded.position().add(0, 1.4D, 0), radius);
    }

    /* ------------------------------ helpers ------------------------------ */

    private KoyomiBossEntity koyo(ServerLevel server, B5EncounterData data) {
        if (data.koyoUuid() == null) return null;
        Entity e = server.getEntity(data.koyoUuid());
        return e instanceof KoyomiBossEntity k ? k : null;
    }

    private GariBossEntity gari(ServerLevel server, B5EncounterData data) {
        if (data.gariUuid() == null) return null;
        Entity e = server.getEntity(data.gariUuid());
        return e instanceof GariBossEntity g ? g : null;
    }

    private void reviveKoyo(ServerLevel server, B5EncounterData data) {
        KoyomiBossEntity old = koyo(server, data);
        if (old != null && !old.isRemoved()) old.discard();
        KoyomiBossEntity koyo = KoyomiBossEntity.createPrepared(server);
        if (koyo == null) return;
        Vec3 pos = rel(data, -41, 1, 2);
        koyo.moveTo(pos.x, pos.y, pos.z, -90, 0);
        server.addFreshEntity(koyo);
        data.setKoyoUuid(koyo.getUUID());
        koyo.setHealth(0.02F);
        koyo.setInvulnerable(true);
        koyo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 999999, 100, true, false));
        koyo.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 999999, 100, true, false));
        if (data.shieldBearer() == SHIELD_KOYO) data.setShieldBearer(SHIELD_NONE);
        resetH2(server, data);
        data.setDirty();
    }

    private void reviveGari(ServerLevel server, B5EncounterData data) {
        GariBossEntity old = gari(server, data);
        if (old != null && !old.isRemoved()) old.discard();
        GariBossEntity gari = GariBossEntity.createPrepared(server);
        if (gari == null) return;
        Vec3 pos = rel(data, -41, 1, -2);
        gari.moveTo(pos.x, pos.y, pos.z, -90, 0);
        server.addFreshEntity(gari);
        data.setGariUuid(gari.getUUID());
        gari.setHealth(0.02F);
        gari.setInvulnerable(true);
        gari.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 999999, 100, true, false));
        gari.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 999999, 100, true, false));
        gari.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        data.setShieldBearer(SHIELD_GARI);
        data.setShieldChanges(data.shieldChanges() + 1);
        resetH2(server, data);
        data.setDirty();
    }

    private void resetH3(ServerLevel server, B5EncounterData data) {
        data.setH3Active(false);
        data.setH3Remaining(0);
        data.setLocoRemaining(0);
        discardAll(h3Markers);
        discardAll(h3GroundTridents);
        discardAll(locoStands);
    }

    private void resetH5(ServerLevel server, B5EncounterData data) {
        data.setH5VolleyActive(false);
        data.setH5FlechaRound(0);
        h5FireAt[0] = h5FireAt[1] = h5FireAt[2] = 0;
        pendingH6.clear();
        projectiles.removeIf(projectile -> projectile.kind == Projectile.H5);
    }

    private void resetH7(ServerLevel server, B5EncounterData data) {
        data.setH7Active(false);
        data.setH7Ticks(0);
        GariBossEntity gari = gari(server, data);
        if (gari != null) gari.setNoAi(false);
    }

    private void clearIntermissionVisuals(ServerLevel server, B5EncounterData data) {
        discardAll(locoStands);
        data.setLocoRemaining(0);
        for (Poza poza : pozas) {
            if (poza.stand.isAlive()) poza.stand.discard();
        }
        pozas.clear();
        if (venenoAs != null && venenoAs.isAlive()) venenoAs.discard();
        venenoAs = null;
        clearTrails();
    }

    private void clearTrails() {
        for (Trail trail : trails) {
            if (trail.stand.isAlive()) trail.stand.discard();
        }
        trails.clear();
    }

    private void clearTransient() {
        clearTrustMarker();
        projectiles.clear();
        discardAll(h3Markers);
        discardAll(h3GroundTridents);
        discardAll(locoStands);
        if (h2Bomb != null && h2Bomb.isAlive()) h2Bomb.discard();
        if (h2Vision != null && h2Vision.isAlive()) h2Vision.discard();
        h2Bomb = null;
        h2Vision = null;
        for (Poza poza : pozas) {
            if (poza.stand.isAlive()) poza.stand.discard();
        }
        pozas.clear();
        clearTrails();
        for (Illusion illusion : illusions) {
            if (illusion.entity.isAlive()) illusion.entity.discard();
        }
        illusions.clear();
        if (venenoAs != null && venenoAs.isAlive()) venenoAs.discard();
        venenoAs = null;
        dialogueQueue.clear();
        pendingH6.clear();
        pendingH7.clear();
        h4ProjectilesActive = false;
        h5FireAt[0] = h5FireAt[1] = h5FireAt[2] = 0;
    }

    private static void discardAll(List<ArmorStand> stands) {
        for (ArmorStand stand : stands) {
            if (stand.isAlive()) stand.discard();
        }
        stands.clear();
    }

    private ArmorStand markerStand(ServerLevel server, double x, double y, double z, boolean small) {
        ArmorStand stand = new ArmorStand(server, x, y, z);
        stand.setInvisible(true);
        stand.setNoGravity(true);
        stand.setInvulnerable(true);
        stand.setSilent(true);
        CompoundTag markerData = new CompoundTag();
        stand.saveWithoutId(markerData);
        markerData.putBoolean("Marker", true);
        markerData.putBoolean("Small", small);
        stand.load(markerData);
        stand.noPhysics = true;
        stand.getPersistentData().putBoolean("B5Marker", true);
        server.addFreshEntity(stand);
        return stand;
    }

    private void cleanupPersistedTransientEntities(ServerLevel server, B5EncounterData data) {
        AABB bounds = encounterBounds(data);
        for (ArmorStand stand : server.getEntitiesOfClass(ArmorStand.class, bounds,
                entity -> entity.getPersistentData().getBoolean("B5Marker"))) {
            stand.discard();
        }
        for (GariBossEntity illusion : server.getEntitiesOfClass(GariBossEntity.class, bounds,
                entity -> entity.getPersistentData().getBoolean("B5Illusion"))) {
            illusion.discard();
        }
    }

    private void ensureGariForIntermissionRecovery(ServerLevel server, B5EncounterData data) {
        if (gari(server, data) != null) return;
        GariBossEntity gari = GariBossEntity.createPrepared(server);
        if (gari == null) return;
        Vec3 pos = rel(data, -41, 1, -2);
        gari.moveTo(pos.x, pos.y, pos.z, -90.0F, 0.0F);
        server.addFreshEntity(gari);
        data.setGariUuid(gari.getUUID());
    }

    private void ensureCountdownBosses(ServerLevel server, B5EncounterData data) {
        if (koyo(server, data) == null) {
            KoyomiBossEntity koyo = KoyomiBossEntity.createPrepared(server);
            if (koyo != null) {
                Vec3 pos = rel(data, -41, 1, 2);
                koyo.moveTo(pos.x, pos.y, pos.z, -90.0F, 0.0F);
                koyo.setArenaAnchor(data.anchor());
                koyo.setInvulnerable(true);
                koyo.setNoAi(true);
                koyo.setBossBarEnabled(false);
                server.addFreshEntity(koyo);
                data.setKoyoUuid(koyo.getUUID());
            }
        }
        if (gari(server, data) == null) {
            GariBossEntity gari = GariBossEntity.createPrepared(server);
            if (gari != null) {
                Vec3 pos = rel(data, -41, 1, -2);
                gari.moveTo(pos.x, pos.y, pos.z, -90.0F, 0.0F);
                gari.setInvulnerable(true);
                gari.setNoAi(true);
                gari.setBossBarEnabled(false);
                server.addFreshEntity(gari);
                data.setGariUuid(gari.getUUID());
            }
        }
    }

    private static AABB encounterBounds(B5EncounterData data) {
        Vec3 min = rel(data, -65, -10, -30);
        Vec3 max = rel(data, 25, 80, 30);
        return new AABB(min, max);
    }

    private void countdownTitle(ServerLevel server, String key) {
        server.getServer().getPlayerList().broadcastAll(
                new ClientboundSetTitleTextPacket(Component.translatable(key)));
        playForAll(server, SoundEvents.NOTE_BLOCK_BELL.value(), 1.5F);
    }

    private static boolean isNoground(ServerLevel server, BlockPos pos) {
        BlockState state = server.getBlockState(pos);
        return state.getCollisionShape(server, pos).isEmpty();
    }

    private static double relY(B5EncounterData data, double y) {
        return data.anchor().getY() + y;
    }

    private static int relYInt(B5EncounterData data, int y) {
        return data.anchor().getY() + y;
    }

    private static Vec3 rel(B5EncounterData data, double x, double y, double z) {
        return new Vec3(data.anchor().getX() + x, data.anchor().getY() + y, data.anchor().getZ() + z);
    }

    private static Vec3 localOffset(Entity entity, double leftAmount, double upAmount, double forwardAmount) {
        Vec3 forward = Vec3.directionFromRotation(entity.getXRot(), entity.getYRot());
        Vec3 up = Vec3.directionFromRotation(entity.getXRot() - 90.0F, entity.getYRot());
        Vec3 left = up.cross(forward).normalize();
        return left.scale(leftAmount).add(up.scale(upAmount)).add(forward.scale(forwardAmount));
    }

    private static ServerPlayer nearestPlayer(ServerLevel server, Vec3 pos, double radius) {
        ServerPlayer nearest = null;
        double best = radius * radius;
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator() || !player.isAlive()) continue;
            double distance = player.distanceToSqr(pos);
            if (distance <= best) {
                best = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    private static double percent(LivingEntity entity, double max) {
        return entity.getHealth() / max * 100.0D;
    }

    private ServerPlayer randomPlayer(ServerLevel server) {
        List<ServerPlayer> candidates = server.players().stream()
                .filter(p -> !p.isSpectator() && p.isAlive())
                .toList();
        return candidates.isEmpty() ? null : candidates.get(server.random.nextInt(candidates.size()));
    }

    private void broadcastTitle(String key) {
        if (serverLevel == null) return;
        for (ServerPlayer player : serverLevel.players()) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(key)));
        }
    }

    private void broadcastSubtitle(String key) {
        if (serverLevel == null) return;
        for (ServerPlayer player : serverLevel.players()) {
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(key)));
        }
    }

    private void playForAll(ServerLevel server, SoundEvent event, float pitch) {
        for (ServerPlayer player : server.players()) {
            server.playSound(null, player.blockPosition(), event, SoundSource.PLAYERS, 1.0F, pitch);
        }
    }

    /* ------------------------------ dialogues ------------------------------ */

    private Component dialogueGari(String lineKey) {
        return Component.literal("").append(
                        Component.translatable("luisb1202.functions.bossfight.b5.dialogos.dia10.1")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED).withBold(true).withItalic(true)))
                .append(Component.translatable(lineKey));
    }

    private Component dialogueKoyo(String lineKey) {
        return Component.literal("").append(
                        Component.translatable("luisb1202.functions.bossfight.b5.dialogos.dia1.1")
                                .withStyle(Style.EMPTY.withColor(TextColor.parseColor("#ea3434")).withBold(true).withItalic(true)))
                .append(Component.translatable(lineKey)
                        .withStyle(Style.EMPTY.withColor(TextColor.parseColor("#ffa4be"))));
    }

    private Component dialogueHd(String lineKey) {
        return Component.literal("").append(
                        Component.translatable("luisb1202.functions.afijos.descubrir.hd.3")
                                .withStyle(Style.EMPTY.withColor(TextColor.parseColor("#FBBDFF")).withBold(true).withItalic(true)))
                .append(Component.translatable(lineKey));
    }

    private void scheduleDialogues(ServerLevel server, int[] delays, String[] keys, int[] style) {
        for (int i = 0; i < keys.length; i++) {
            Component msg = switch (style[i]) {
                case 1 -> dialogueKoyo(keys[i]);
                case 2 -> dialogueHd(keys[i]);
                default -> dialogueGari(keys[i]);
            };
            SoundEvent sound = style[i] == 2 ? SoundEvents.TRIDENT_RETURN : SoundEvents.PILLAGER_AMBIENT;
            float pitch = style[i] == 2 ? 1.7F : (style[i] == 1 ? 1.7F : 1.2F);
            dialogueQueue.add(new Dialogue(server.getServer().getTickCount() + delays[i], msg, sound, pitch));
        }
    }

    private void scheduleTellraw(ServerLevel server, int ticks, Component msg) {
        dialogueQueue.add(new Dialogue(server.getServer().getTickCount() + ticks, msg,
                SoundEvents.PILLAGER_AMBIENT, 1.2F));
    }

    private void scheduleKorosTip(ServerLevel server, int ticks, String lineKey, boolean alert) {
        dialogueQueue.add(new Dialogue(server.getServer().getTickCount() + ticks, dialogueHd(lineKey),
                alert ? SoundEvents.TRIDENT_RETURN : null, 1.7F) {
            @Override
            void run(ServerLevel level, B5EncounterController controller, B5EncounterData encounterData) {
                if (alert) controller.playForAll(level, SoundEvents.NOTE_BLOCK_PLING.value(), 2.0F);
            }
        });
    }

    private void sendDialogueNow(ServerLevel server, Component msg, SoundEvent sound, float pitch) {
        for (ServerPlayer player : server.players()) player.sendSystemMessage(msg);
        playForAll(server, sound, pitch);
    }

    private void tickDialogueQueue(ServerLevel server, B5EncounterData data) {
        int now = server.getServer().getTickCount();
        List<Dialogue> due = new ArrayList<>();
        Iterator<Dialogue> it = dialogueQueue.iterator();
        while (it.hasNext()) {
            Dialogue d = it.next();
            if (now < d.tick) continue;
            due.add(d);
            it.remove();
        }
        for (Dialogue d : due) {
            if (d.msg != null) {
                for (ServerPlayer player : server.players()) {
                    player.displayClientMessage(d.msg, false);
                }
                if (d.sound != null) {
                    playForAll(server, d.sound, d.pitch);
                }
            }
            d.run(server, this, data);
        }
    }

    private static Component title(String key) {
        return Component.translatable(key);
    }

    /* ------------------------------ runtime types ------------------------------ */

    private static final class Projectile {
        static final int H5 = 0;
        static final int DISPARO = 1;
        Vec3 pos;
        final Vec3 vel;
        final UUID target;
        final int kind;
        int age;

        Projectile(Vec3 pos, UUID target, int kind) {
            this.pos = pos;
            this.vel = null;
            this.target = target;
            this.kind = kind;
            this.age = 0;
        }

        Projectile(Vec3 pos, Vec3 vel, int kind) {
            this.pos = pos;
            this.vel = vel;
            this.target = null;
            this.kind = kind;
            this.age = 0;
        }
    }

    private static final class Poza {
        final ArmorStand stand;
        int color;

        Poza(ArmorStand stand, int color) {
            this.stand = stand;
            this.color = color;
        }
    }

    private static final class Trail {
        final ArmorStand stand;
        final Vec3 target;

        Trail(ArmorStand stand, Vec3 target) {
            this.stand = stand;
            this.target = target;
        }
    }

    private static final class Illusion {
        final GariBossEntity entity;
        int role;

        Illusion(GariBossEntity entity, int role) {
            this.entity = entity;
            this.role = role;
        }
    }

    private static class Dialogue {
        final int tick;
        final Component msg;
        final SoundEvent sound;
        final float pitch;

        Dialogue(int tick, Component msg, SoundEvent sound, float pitch) {
            this.tick = tick;
            this.msg = msg;
            this.sound = sound;
            this.pitch = pitch;
        }

        void run(ServerLevel server, B5EncounterController controller, B5EncounterData data) {
        }
    }

    private static final class ScoreboardHelper {
        private ScoreboardHelper() {
        }

        static void setupTimer(ServerLevel server, int value) {
            Scoreboard board = server.getScoreboard();
            Objective objective = board.getObjective("fp_b5_h4_timer");
            if (objective == null) {
                objective = board.addObjective("fp_b5_h4_timer", ObjectiveCriteria.DUMMY,
                        Component.translatable("luisb1202.functions.bossfight.b5.h4.ini_timer.1"),
                        ObjectiveCriteria.RenderType.INTEGER);
            }
            board.setDisplayObjective(Scoreboard.DISPLAY_SLOT_SIDEBAR, objective);
            for (ServerPlayer player : server.players()) {
                board.getOrCreatePlayerScore(player.getScoreboardName(), objective).setScore(value);
            }
        }

        static void updateTimer(ServerLevel server, int value) {
            Objective objective = server.getScoreboard().getObjective("fp_b5_h4_timer");
            if (objective == null) return;
            for (ServerPlayer player : server.players()) {
                server.getScoreboard().getOrCreatePlayerScore(player.getScoreboardName(), objective).setScore(value);
            }
        }

        static void removeTimer(ServerLevel server) {
            Scoreboard board = server.getScoreboard();
            Objective objective = board.getObjective("fp_b5_h4_timer");
            if (objective != null) {
                board.removeObjective(objective);
            }
        }
    }
}
