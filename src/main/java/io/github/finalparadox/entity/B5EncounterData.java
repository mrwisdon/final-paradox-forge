package io.github.finalparadox.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.UUID;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Durable state of the B5 dual fight, persisted per dimension like the original
 * scoreboard values. Transient attack visuals (armor stands, projectiles,
 * illusions) live in the runtime controller and are re-created on demand.
 */
public final class B5EncounterData extends SavedData {
    private static final String DATA_NAME = "finalparadox_b5_encounter";

    private boolean active;
    private BlockPos anchor;
    private UUID koyoUuid;
    private UUID gariUuid;

    // Phase machine (mirrors the original score `fase`).
    private int fase;
    private int runTicks;
    private int countdownTicks;

    // Attack counters (mirror h1..h7 scores).
    private int h5Counter;
    private int h3Counter;
    private int h7Counter;
    private int shieldBearer = 1; // 0 koyo, 1 gari, -1 none
    private int shieldChanges;

    // Intermission state.
    private int interTicks;
    private int interEvent;

    // h3 state.
    private boolean h3Active;
    private int h3Remaining;
    private int h3Timer;
    private int locoRemaining;
    private int locoTicks;
    private int locoMoveTimer;

    // h2 state.
    private boolean h2Active;
    private long h2PendingDue;
    private boolean h2ScheduleTracked;
    private int h2VisionScore;
    private int h2Dano;
    private UUID h2Target;
    private boolean h2WaterBomb;
    private boolean h2Bouncing;

    // h5/h6/h7 state.
    private boolean h5VolleyActive;
    private int h5FlechaTicks;
    private int h5FlechaRound;
    private boolean h6Active;
    private int h6Ticks;
    private double h6TargetX;
    private double h6TargetZ;
    private boolean h7Active;
    private int h7Ticks;

    // h4 state.
    private boolean poisonActive;
    private int poisonTicks;
    private int poisonDano;
    private UUID poisonTarget;
    private boolean illusionActive;
    private int illusionTicks;
    private int illusionProjectileTicks;
    private int illusionVolleys;
    private UUID realIllusionUuid;
    private int h4Timer;
    private boolean h4DamagePhase;
    private int h4DamageTimer;

    // Finale / flow.
    private int finaleTicks;
    private boolean finalePrepared;
    private int dialogueCounter;
    private boolean respawnScheduled;
    private int respawnTicks;
    private int musicTicks;
    private int musicPhase; // 0 none, 1 main intro, 2 main loop, 3 inter intro, 4 inter loop, 5 inter final, 6 abatir
    private boolean victoryPlayed;
    private boolean preBattleDialoguePlayed;
    private int preBattleDialogueTicks = -1;
    private final Set<UUID> deadPlayers = new HashSet<>();
    private final Map<UUID, Integer> h5Hits = new HashMap<>();
    private final Map<UUID, Integer> h3IntermissionHits = new HashMap<>();

    public static B5EncounterData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(B5EncounterData::load, B5EncounterData::new, DATA_NAME);
    }

    private static B5EncounterData load(CompoundTag tag) {
        B5EncounterData data = new B5EncounterData();
        data.active = tag.getBoolean("Active");
        if (tag.contains("Anchor", Tag.TAG_LONG)) data.anchor = BlockPos.of(tag.getLong("Anchor"));
        if (tag.hasUUID("Koyo")) data.koyoUuid = tag.getUUID("Koyo");
        if (tag.hasUUID("Gari")) data.gariUuid = tag.getUUID("Gari");
        data.fase = tag.getInt("Fase");
        data.runTicks = tag.getInt("RunTicks");
        data.countdownTicks = tag.getInt("CountdownTicks");
        data.h5Counter = tag.getInt("H5");
        data.h3Counter = tag.getInt("H3");
        data.h7Counter = tag.getInt("H7");
        data.shieldBearer = tag.getInt("ShieldBearer");
        data.shieldChanges = tag.getInt("ShieldChanges");
        data.interTicks = tag.getInt("InterTicks");
        data.interEvent = tag.getInt("InterEvent");
        data.h3Active = tag.getBoolean("H3Active");
        data.h3Remaining = tag.getInt("H3Remaining");
        data.h3Timer = tag.getInt("H3Timer");
        data.locoRemaining = tag.getInt("LocoRemaining");
        data.locoTicks = tag.getInt("LocoTicks");
        data.locoMoveTimer = tag.getInt("LocoMoveTimer");
        data.h2Active = tag.getBoolean("H2Active");
        data.h2PendingDue = tag.getLong("H2PendingDue");
        data.h2ScheduleTracked = tag.contains("H2PendingDue");
        data.h2VisionScore = tag.getInt("H2Vision");
        data.h2Dano = tag.getInt("H2Dano");
        if (tag.hasUUID("H2Target")) data.h2Target = tag.getUUID("H2Target");
        data.h2WaterBomb = tag.getBoolean("H2Water");
        data.h2Bouncing = tag.getBoolean("H2Bouncing");
        data.h5VolleyActive = tag.getBoolean("H5Active");
        data.h5FlechaTicks = tag.getInt("H5Ticks");
        data.h5FlechaRound = tag.getInt("H5Round");
        data.h6Active = tag.getBoolean("H6Active");
        data.h6Ticks = tag.getInt("H6Ticks");
        data.h6TargetX = tag.getDouble("H6X");
        data.h6TargetZ = tag.getDouble("H6Z");
        data.h7Active = tag.getBoolean("H7Active");
        data.h7Ticks = tag.getInt("H7Ticks");
        data.poisonActive = tag.getBoolean("PoisonActive");
        data.poisonTicks = tag.getInt("PoisonTicks");
        data.poisonDano = tag.getInt("PoisonDano");
        if (tag.hasUUID("PoisonTarget")) data.poisonTarget = tag.getUUID("PoisonTarget");
        data.illusionActive = tag.getBoolean("IllusionActive");
        data.illusionTicks = tag.getInt("IllusionTicks");
        data.illusionProjectileTicks = tag.getInt("IllusionProjectileTicks");
        data.illusionVolleys = tag.getInt("IllusionVolleys");
        if (tag.hasUUID("RealIllusion")) data.realIllusionUuid = tag.getUUID("RealIllusion");
        data.h4Timer = tag.getInt("H4Timer");
        data.h4DamagePhase = tag.getBoolean("H4DamagePhase");
        data.h4DamageTimer = tag.getInt("H4DamageTimer");
        data.finaleTicks = tag.getInt("FinaleTicks");
        data.finalePrepared = tag.getBoolean("FinalePrepared");
        data.dialogueCounter = tag.getInt("Dialogo");
        data.respawnScheduled = tag.getBoolean("RespawnScheduled");
        data.respawnTicks = tag.getInt("RespawnTicks");
        data.musicTicks = tag.getInt("MusicTicks");
        data.musicPhase = tag.getInt("MusicPhase");
        data.victoryPlayed = tag.getBoolean("VictoryPlayed");
        data.preBattleDialoguePlayed = tag.getBoolean("PreBattleDialoguePlayed");
        data.preBattleDialogueTicks = tag.contains("PreBattleDialogueTicks")
                ? tag.getInt("PreBattleDialogueTicks") : -1;
        long[] dead = tag.getLongArray("DeadPlayers");
        for (int i = 0; i + 1 < dead.length; i += 2) {
            data.deadPlayers.add(new UUID(dead[i], dead[i + 1]));
        }
        loadUuidCounters(tag.getCompound("H5Hits"), data.h5Hits);
        loadUuidCounters(tag.getCompound("H3IntermissionHits"), data.h3IntermissionHits);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("Active", active);
        if (anchor != null) tag.putLong("Anchor", anchor.asLong());
        if (koyoUuid != null) tag.putUUID("Koyo", koyoUuid);
        if (gariUuid != null) tag.putUUID("Gari", gariUuid);
        tag.putInt("Fase", fase);
        tag.putInt("RunTicks", runTicks);
        tag.putInt("CountdownTicks", countdownTicks);
        tag.putInt("H5", h5Counter);
        tag.putInt("H3", h3Counter);
        tag.putInt("H7", h7Counter);
        tag.putInt("ShieldBearer", shieldBearer);
        tag.putInt("ShieldChanges", shieldChanges);
        tag.putInt("InterTicks", interTicks);
        tag.putInt("InterEvent", interEvent);
        tag.putBoolean("H3Active", h3Active);
        tag.putInt("H3Remaining", h3Remaining);
        tag.putInt("H3Timer", h3Timer);
        tag.putInt("LocoRemaining", locoRemaining);
        tag.putInt("LocoTicks", locoTicks);
        tag.putInt("LocoMoveTimer", locoMoveTimer);
        tag.putBoolean("H2Active", h2Active);
        tag.putLong("H2PendingDue", h2PendingDue);
        tag.putInt("H2Vision", h2VisionScore);
        tag.putInt("H2Dano", h2Dano);
        if (h2Target != null) tag.putUUID("H2Target", h2Target);
        tag.putBoolean("H2Water", h2WaterBomb);
        tag.putBoolean("H2Bouncing", h2Bouncing);
        tag.putBoolean("H5Active", h5VolleyActive);
        tag.putInt("H5Ticks", h5FlechaTicks);
        tag.putInt("H5Round", h5FlechaRound);
        tag.putBoolean("H6Active", h6Active);
        tag.putInt("H6Ticks", h6Ticks);
        tag.putDouble("H6X", h6TargetX);
        tag.putDouble("H6Z", h6TargetZ);
        tag.putBoolean("H7Active", h7Active);
        tag.putInt("H7Ticks", h7Ticks);
        tag.putBoolean("PoisonActive", poisonActive);
        tag.putInt("PoisonTicks", poisonTicks);
        tag.putInt("PoisonDano", poisonDano);
        if (poisonTarget != null) tag.putUUID("PoisonTarget", poisonTarget);
        tag.putBoolean("IllusionActive", illusionActive);
        tag.putInt("IllusionTicks", illusionTicks);
        tag.putInt("IllusionProjectileTicks", illusionProjectileTicks);
        tag.putInt("IllusionVolleys", illusionVolleys);
        if (realIllusionUuid != null) tag.putUUID("RealIllusion", realIllusionUuid);
        tag.putInt("H4Timer", h4Timer);
        tag.putBoolean("H4DamagePhase", h4DamagePhase);
        tag.putInt("H4DamageTimer", h4DamageTimer);
        tag.putInt("FinaleTicks", finaleTicks);
        tag.putBoolean("FinalePrepared", finalePrepared);
        tag.putInt("Dialogo", dialogueCounter);
        tag.putBoolean("RespawnScheduled", respawnScheduled);
        tag.putInt("RespawnTicks", respawnTicks);
        tag.putInt("MusicTicks", musicTicks);
        tag.putInt("MusicPhase", musicPhase);
        tag.putBoolean("VictoryPlayed", victoryPlayed);
        tag.putBoolean("PreBattleDialoguePlayed", preBattleDialoguePlayed);
        tag.putInt("PreBattleDialogueTicks", preBattleDialogueTicks);
        long[] dead = new long[deadPlayers.size() * 2];
        int deadIndex = 0;
        for (UUID uuid : deadPlayers) {
            dead[deadIndex++] = uuid.getMostSignificantBits();
            dead[deadIndex++] = uuid.getLeastSignificantBits();
        }
        tag.putLongArray("DeadPlayers", dead);
        tag.put("H5Hits", saveUuidCounters(h5Hits));
        tag.put("H3IntermissionHits", saveUuidCounters(h3IntermissionHits));
        return tag;
    }

    private static void loadUuidCounters(CompoundTag tag, Map<UUID, Integer> counters) {
        for (String key : tag.getAllKeys()) {
            try {
                counters.put(UUID.fromString(key), tag.getInt(key));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed legacy entries instead of invalidating the encounter save.
            }
        }
    }

    private static CompoundTag saveUuidCounters(Map<UUID, Integer> counters) {
        CompoundTag tag = new CompoundTag();
        counters.forEach((uuid, count) -> tag.putInt(uuid.toString(), count));
        return tag;
    }

    public boolean active() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        setDirty();
    }

    public BlockPos anchor() {
        return anchor;
    }

    public void setAnchor(BlockPos anchor) {
        this.anchor = anchor.immutable();
        setDirty();
    }

    public UUID koyoUuid() {
        return koyoUuid;
    }

    public void setKoyoUuid(UUID uuid) {
        this.koyoUuid = uuid;
        setDirty();
    }

    public UUID gariUuid() {
        return gariUuid;
    }

    public void setGariUuid(UUID uuid) {
        this.gariUuid = uuid;
        setDirty();
    }

    public int fase() {
        return fase;
    }

    public void setFase(int fase) {
        this.fase = fase;
        setDirty();
    }

    public int runTicks() {
        return runTicks;
    }

    public void setRunTicks(int runTicks) {
        this.runTicks = runTicks;
        setDirty();
    }

    public int countdownTicks() {
        return countdownTicks;
    }

    public void setCountdownTicks(int countdownTicks) {
        this.countdownTicks = countdownTicks;
        setDirty();
    }

    public int h5Counter() {
        return h5Counter;
    }

    public void setH5Counter(int v) {
        this.h5Counter = v;
        setDirty();
    }

    public int h3Counter() {
        return h3Counter;
    }

    public void setH3Counter(int v) {
        this.h3Counter = v;
        setDirty();
    }

    public int h7Counter() {
        return h7Counter;
    }

    public void setH7Counter(int v) {
        this.h7Counter = v;
        setDirty();
    }

    public int shieldBearer() {
        return shieldBearer;
    }

    public void setShieldBearer(int v) {
        this.shieldBearer = v;
        setDirty();
    }

    public int shieldChanges() {
        return shieldChanges;
    }

    public void setShieldChanges(int v) {
        this.shieldChanges = v;
        setDirty();
    }

    public int interTicks() {
        return interTicks;
    }

    public void setInterTicks(int v) {
        this.interTicks = v;
        setDirty();
    }

    public int interEvent() {
        return interEvent;
    }

    public void setInterEvent(int v) {
        this.interEvent = v;
        setDirty();
    }

    public boolean h3Active() {
        return h3Active;
    }

    public void setH3Active(boolean v) {
        this.h3Active = v;
        setDirty();
    }

    public int h3Remaining() {
        return h3Remaining;
    }

    public void setH3Remaining(int v) {
        this.h3Remaining = v;
        setDirty();
    }

    public int h3Timer() {
        return h3Timer;
    }

    public void setH3Timer(int v) {
        this.h3Timer = v;
        setDirty();
    }

    public int locoRemaining() {
        return locoRemaining;
    }

    public void setLocoRemaining(int v) {
        this.locoRemaining = v;
        setDirty();
    }

    public int locoTicks() {
        return locoTicks;
    }

    public void setLocoTicks(int v) {
        this.locoTicks = v;
        setDirty();
    }

    public int locoMoveTimer() {
        return locoMoveTimer;
    }

    public void setLocoMoveTimer(int v) {
        this.locoMoveTimer = v;
        setDirty();
    }

    public boolean h2Active() {
        return h2Active;
    }

    public void setH2Active(boolean v) {
        this.h2Active = v;
        setDirty();
    }

    public boolean h2Bouncing() {
        return h2Bouncing;
    }

    public void setH2Bouncing(boolean v) {
        this.h2Bouncing = v;
        setDirty();
    }

    public long h2PendingDue() {
        return h2PendingDue;
    }

    public void setH2PendingDue(long v) {
        this.h2PendingDue = v;
        this.h2ScheduleTracked = true;
        setDirty();
    }

    public boolean h2ScheduleTracked() {
        return h2ScheduleTracked;
    }

    public int h2VisionScore() {
        return h2VisionScore;
    }

    public void setH2VisionScore(int v) {
        this.h2VisionScore = v;
        setDirty();
    }

    public int h2Dano() {
        return h2Dano;
    }

    public void setH2Dano(int v) {
        this.h2Dano = v;
        setDirty();
    }

    public UUID h2Target() {
        return h2Target;
    }

    public void setH2Target(UUID v) {
        this.h2Target = v;
        setDirty();
    }

    public boolean h2WaterBomb() {
        return h2WaterBomb;
    }

    public void setH2WaterBomb(boolean v) {
        this.h2WaterBomb = v;
        setDirty();
    }

    public boolean h5VolleyActive() {
        return h5VolleyActive;
    }

    public void setH5VolleyActive(boolean v) {
        this.h5VolleyActive = v;
        setDirty();
    }

    public int h5FlechaTicks() {
        return h5FlechaTicks;
    }

    public void setH5FlechaTicks(int v) {
        this.h5FlechaTicks = v;
        setDirty();
    }

    public int h5FlechaRound() {
        return h5FlechaRound;
    }

    public void setH5FlechaRound(int v) {
        this.h5FlechaRound = v;
        setDirty();
    }

    public boolean h6Active() {
        return h6Active;
    }

    public void setH6Active(boolean v) {
        this.h6Active = v;
        setDirty();
    }

    public int h6Ticks() {
        return h6Ticks;
    }

    public void setH6Ticks(int v) {
        this.h6Ticks = v;
        setDirty();
    }

    public double h6TargetX() {
        return h6TargetX;
    }

    public double h6TargetZ() {
        return h6TargetZ;
    }

    public void setH6Target(double x, double z) {
        this.h6TargetX = x;
        this.h6TargetZ = z;
        setDirty();
    }

    public boolean h7Active() {
        return h7Active;
    }

    public void setH7Active(boolean v) {
        this.h7Active = v;
        setDirty();
    }

    public int h7Ticks() {
        return h7Ticks;
    }

    public void setH7Ticks(int v) {
        this.h7Ticks = v;
        setDirty();
    }

    public boolean poisonActive() {
        return poisonActive;
    }

    public void setPoisonActive(boolean v) {
        this.poisonActive = v;
        setDirty();
    }

    public int poisonTicks() {
        return poisonTicks;
    }

    public void setPoisonTicks(int v) {
        this.poisonTicks = v;
        setDirty();
    }

    public int poisonDano() {
        return poisonDano;
    }

    public void setPoisonDano(int v) {
        this.poisonDano = v;
        setDirty();
    }

    public UUID poisonTarget() {
        return poisonTarget;
    }

    public void setPoisonTarget(UUID v) {
        this.poisonTarget = v;
        setDirty();
    }

    public boolean illusionActive() {
        return illusionActive;
    }

    public void setIllusionActive(boolean v) {
        this.illusionActive = v;
        setDirty();
    }

    public int illusionTicks() {
        return illusionTicks;
    }

    public void setIllusionTicks(int v) {
        this.illusionTicks = v;
        setDirty();
    }

    public int illusionProjectileTicks() {
        return illusionProjectileTicks;
    }

    public void setIllusionProjectileTicks(int v) {
        this.illusionProjectileTicks = v;
        setDirty();
    }

    public int illusionVolleys() {
        return illusionVolleys;
    }

    public void setIllusionVolleys(int v) {
        this.illusionVolleys = v;
        setDirty();
    }

    public UUID realIllusionUuid() {
        return realIllusionUuid;
    }

    public void setRealIllusionUuid(UUID v) {
        this.realIllusionUuid = v;
        setDirty();
    }

    public int h4Timer() {
        return h4Timer;
    }

    public void setH4Timer(int v) {
        this.h4Timer = v;
        setDirty();
    }

    public boolean h4DamagePhase() {
        return h4DamagePhase;
    }

    public void setH4DamagePhase(boolean v) {
        this.h4DamagePhase = v;
        setDirty();
    }

    public int h4DamageTimer() {
        return h4DamageTimer;
    }

    public void setH4DamageTimer(int v) {
        this.h4DamageTimer = v;
        setDirty();
    }

    public int finaleTicks() {
        return finaleTicks;
    }

    public void setFinaleTicks(int v) {
        this.finaleTicks = v;
        setDirty();
    }

    public boolean finalePrepared() {
        return finalePrepared;
    }

    public void setFinalePrepared(boolean finalePrepared) {
        this.finalePrepared = finalePrepared;
        setDirty();
    }

    public int dialogueCounter() {
        return dialogueCounter;
    }

    public void setDialogueCounter(int v) {
        this.dialogueCounter = v;
        setDirty();
    }

    public boolean respawnScheduled() {
        return respawnScheduled;
    }

    public void setRespawnScheduled(boolean v) {
        this.respawnScheduled = v;
        setDirty();
    }

    public int respawnTicks() {
        return respawnTicks;
    }

    public void setRespawnTicks(int v) {
        this.respawnTicks = v;
        setDirty();
    }

    public int musicTicks() {
        return musicTicks;
    }

    public void setMusicTicks(int v) {
        this.musicTicks = v;
        setDirty();
    }

    public int musicPhase() {
        return musicPhase;
    }

    public void setMusicPhase(int v) {
        this.musicPhase = v;
        setDirty();
    }

    public boolean victoryPlayed() {
        return victoryPlayed;
    }

    public void setVictoryPlayed(boolean v) {
        this.victoryPlayed = v;
        setDirty();
    }

    public boolean preBattleDialoguePlayed() {
        return preBattleDialoguePlayed;
    }

    public void startPreBattleDialogue() {
        if (preBattleDialoguePlayed) return;
        preBattleDialoguePlayed = true;
        preBattleDialogueTicks = 0;
        setDirty();
    }

    public int preBattleDialogueTicks() {
        return preBattleDialogueTicks;
    }

    public void setPreBattleDialogueTicks(int ticks) {
        if (this.preBattleDialogueTicks == ticks) return;
        this.preBattleDialogueTicks = ticks;
        setDirty();
    }

    public void resetPreBattleDialogue() {
        preBattleDialoguePlayed = false;
        preBattleDialogueTicks = -1;
        setDirty();
    }

    public void markPreBattleDialogueComplete() {
        preBattleDialoguePlayed = true;
        preBattleDialogueTicks = -1;
        setDirty();
    }

    public boolean isDeadPlayer(UUID uuid) {
        return deadPlayers.contains(uuid);
    }

    public void markDeadPlayer(UUID uuid) {
        if (deadPlayers.add(uuid)) setDirty();
    }

    public void clearDeadPlayers() {
        if (!deadPlayers.isEmpty()) {
            deadPlayers.clear();
            setDirty();
        }
    }

    public int incrementH5Hits(UUID uuid) {
        int count = h5Hits.merge(uuid, 1, Integer::sum);
        setDirty();
        return count;
    }

    public int incrementH3IntermissionHits(UUID uuid) {
        int count = h3IntermissionHits.merge(uuid, 1, Integer::sum);
        setDirty();
        return count;
    }

    public void clearH5Hits() {
        if (!h5Hits.isEmpty()) {
            h5Hits.clear();
            setDirty();
        }
    }

    public void clearH3IntermissionHits() {
        if (!h3IntermissionHits.isEmpty()) {
            h3IntermissionHits.clear();
            setDirty();
        }
    }
}
