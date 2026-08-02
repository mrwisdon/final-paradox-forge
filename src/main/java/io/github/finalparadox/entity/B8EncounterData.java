package io.github.finalparadox.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Durable state of the B8 Zombie Supermatrix encounter, persisted per
 * dimension like the original scoreboard values. The floor anchor is the
 * player-deployed arena anchor; every runtime coordinate is derived from it
 * (see docs/reconstruction/b8-arena-evidence.md).
 */
public final class B8EncounterData extends SavedData {
    private static final String DATA_NAME = "finalparadox_b8_encounter";

    public static final int STATE_IDLE = 0;
    public static final int STATE_COUNTDOWN = 1;
    public static final int STATE_PHASE_1 = 2;
    public static final int STATE_PHASE_2 = 3;
    public static final int STATE_PHASE_3 = 4;
    public static final int STATE_PHASE_4 = 5;
    public static final int STATE_PHASE_5 = 6;
    public static final int STATE_VICTORY = 7;
    public static final int STATE_DEFEAT = 8;
    public static final int STATE_CLEANUP = 9;

    private boolean active;
    private BlockPos anchor;
    private int state = STATE_IDLE;
    private int fase = 1;
    private int ronda;
    private int health = 250;
    private int healthTotal = 250;
    private boolean vulnerable;
    private int addCount;
    private UUID matrixUuid;
    private final Set<UUID> participants = new HashSet<>();
    private final Set<UUID> spectators = new HashSet<>();
    private final Map<UUID, UUID> mountOwnership = new HashMap<>();
    private final List<UUID> mountOrder = new ArrayList<>();
    private final List<TimerEntry> timers = new ArrayList<>();
    private final Set<UUID> cleanupEntities = new HashSet<>();

    // H2 shield-breaking state.
    private boolean h2Active;
    private int h2PendingSpawns;
    private int h2EndDelay = -1;
    private final List<double[]> h2Candidates = new ArrayList<>();

    // H1 straight-beam and H4 rotating-area hazard state.
    private boolean h1Active;
    private final List<BeamState> h1Beams = new ArrayList<>();
    private final List<WarnState> h1Warns = new ArrayList<>();
    private final Set<UUID> h1Damaged = new HashSet<>();
    private boolean h4Active;
    private final List<H4SourceState> h4Sources = new ArrayList<>();
    private final List<H4WarnState> h4Warns = new ArrayList<>();

    // Game-rule snapshot taken at encounter start and restored on reset.
    private boolean ruleImmediateRespawn = true;
    private boolean ruleMobGriefing = true;
    private boolean ruleMobSpawning = true;
    private boolean ruleKeepInventory = true;
    private boolean ruleFireTick = true;
    private boolean ruleNaturalRegeneration = true;
    private int ruleRandomTickSpeed = 3;

    public record TimerEntry(String type, long dueGameTime) {
    }

    public record BeamState(double x, double y, double z, float yaw, int danom, int danom2) {
    }

    public record WarnState(double x, double y, double z, float yaw) {
    }

    public record H4SourceState(double radius, float yaw, int danom) {
    }

    public record H4WarnState(double x, double y, double z, float yaw, int danom) {
    }

    public static B8EncounterData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(B8EncounterData::load, B8EncounterData::new, DATA_NAME);
    }

    private static B8EncounterData load(CompoundTag tag) {
        B8EncounterData data = new B8EncounterData();
        data.active = tag.getBoolean("Active");
        if (tag.contains("Anchor", Tag.TAG_LONG)) data.anchor = BlockPos.of(tag.getLong("Anchor"));
        data.state = tag.getInt("State");
        data.fase = tag.getInt("Fase");
        data.ronda = tag.getInt("Ronda");
        data.health = tag.getInt("Health");
        data.healthTotal = Math.max(1, tag.getInt("HealthTotal"));
        data.vulnerable = tag.getBoolean("Vulnerable");
        data.addCount = tag.getInt("AddCount");
        if (tag.hasUUID("Matrix")) data.matrixUuid = tag.getUUID("Matrix");
        data.ruleImmediateRespawn = tag.getBoolean("RuleImmediateRespawn");
        data.ruleMobGriefing = tag.getBoolean("RuleMobGriefing");
        data.ruleMobSpawning = tag.getBoolean("RuleMobSpawning");
        data.ruleKeepInventory = tag.getBoolean("RuleKeepInventory");
        data.ruleFireTick = tag.getBoolean("RuleFireTick");
        data.ruleNaturalRegeneration = tag.getBoolean("RuleNaturalRegeneration");
        data.ruleRandomTickSpeed = tag.getInt("RuleRandomTickSpeed");
        data.participants.addAll(readUuidList(tag.getList("Participants", Tag.TAG_COMPOUND)));
        data.spectators.addAll(readUuidList(tag.getList("Spectators", Tag.TAG_COMPOUND)));
        data.cleanupEntities.addAll(readUuidList(tag.getList("Cleanup", Tag.TAG_COMPOUND)));
        ListTag mounts = tag.getList("Mounts", Tag.TAG_COMPOUND);
        for (int i = 0; i < mounts.size(); i++) {
            CompoundTag mount = mounts.getCompound(i);
            data.mountOwnership.put(mount.getUUID("Rider"), mount.getUUID("Rover"));
        }
        data.mountOrder.addAll(readUuidList(tag.getList("MountOrder", Tag.TAG_COMPOUND)));
        ListTag timerTags = tag.getList("Timers", Tag.TAG_COMPOUND);
        for (int i = 0; i < timerTags.size(); i++) {
            CompoundTag timer = timerTags.getCompound(i);
            data.timers.add(new TimerEntry(timer.getString("Type"), timer.getLong("Due")));
        }
        data.h2Active = tag.getBoolean("H2Active");
        data.h2PendingSpawns = tag.getInt("H2PendingSpawns");
        data.h2EndDelay = tag.getInt("H2EndDelay");
        ListTag h2 = tag.getList("H2Candidates", Tag.TAG_COMPOUND);
        for (int i = 0; i < h2.size(); i++) {
            CompoundTag entry = h2.getCompound(i);
            data.h2Candidates.add(new double[]{
                    entry.getDouble("X"), entry.getDouble("Y"), entry.getDouble("Z")});
        }
        data.h1Active = tag.getBoolean("H1Active");
        ListTag h1Beams = tag.getList("H1Beams", Tag.TAG_COMPOUND);
        for (int i = 0; i < h1Beams.size(); i++) {
            CompoundTag entry = h1Beams.getCompound(i);
            data.h1Beams.add(new BeamState(
                    entry.getDouble("X"), entry.getDouble("Y"), entry.getDouble("Z"),
                    entry.getFloat("Yaw"), entry.getInt("Danom"), entry.getInt("Danom2")));
        }
        ListTag h1Warns = tag.getList("H1Warns", Tag.TAG_COMPOUND);
        for (int i = 0; i < h1Warns.size(); i++) {
            CompoundTag entry = h1Warns.getCompound(i);
            data.h1Warns.add(new WarnState(
                    entry.getDouble("X"), entry.getDouble("Y"), entry.getDouble("Z"),
                    entry.getFloat("Yaw")));
        }
        data.h1Damaged.addAll(readUuidList(tag.getList("H1Damaged", Tag.TAG_COMPOUND)));
        data.h4Active = tag.getBoolean("H4Active");
        ListTag h4Sources = tag.getList("H4Sources", Tag.TAG_COMPOUND);
        for (int i = 0; i < h4Sources.size(); i++) {
            CompoundTag entry = h4Sources.getCompound(i);
            data.h4Sources.add(new H4SourceState(
                    entry.getDouble("Radius"), entry.getFloat("Yaw"), entry.getInt("Danom")));
        }
        ListTag h4Warns = tag.getList("H4Warns", Tag.TAG_COMPOUND);
        for (int i = 0; i < h4Warns.size(); i++) {
            CompoundTag entry = h4Warns.getCompound(i);
            data.h4Warns.add(new H4WarnState(
                    entry.getDouble("X"), entry.getDouble("Y"), entry.getDouble("Z"),
                    entry.getFloat("Yaw"), entry.getInt("Danom")));
        }
        return data;
    }

    private static List<UUID> readUuidList(ListTag list) {
        List<UUID> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            result.add(list.getCompound(i).getUUID("Uuid"));
        }
        return result;
    }

    private static ListTag writeUuidList(Iterable<UUID> uuids) {
        ListTag list = new ListTag();
        for (UUID uuid : uuids) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Uuid", uuid);
            list.add(entry);
        }
        return list;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("Active", active);
        if (anchor != null) tag.putLong("Anchor", anchor.asLong());
        tag.putInt("State", state);
        tag.putInt("Fase", fase);
        tag.putInt("Ronda", ronda);
        tag.putInt("Health", health);
        tag.putInt("HealthTotal", healthTotal);
        tag.putBoolean("Vulnerable", vulnerable);
        tag.putInt("AddCount", addCount);
        if (matrixUuid != null) tag.putUUID("Matrix", matrixUuid);
        tag.putBoolean("RuleImmediateRespawn", ruleImmediateRespawn);
        tag.putBoolean("RuleMobGriefing", ruleMobGriefing);
        tag.putBoolean("RuleMobSpawning", ruleMobSpawning);
        tag.putBoolean("RuleKeepInventory", ruleKeepInventory);
        tag.putBoolean("RuleFireTick", ruleFireTick);
        tag.putBoolean("RuleNaturalRegeneration", ruleNaturalRegeneration);
        tag.putInt("RuleRandomTickSpeed", ruleRandomTickSpeed);
        tag.put("Participants", writeUuidList(participants));
        tag.put("Spectators", writeUuidList(spectators));
        tag.put("Cleanup", writeUuidList(cleanupEntities));
        ListTag mounts = new ListTag();
        mountOwnership.forEach((rider, rover) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Rider", rider);
            entry.putUUID("Rover", rover);
            mounts.add(entry);
        });
        tag.put("Mounts", mounts);
        tag.put("MountOrder", writeUuidList(mountOrder));
        ListTag timerTags = new ListTag();
        for (TimerEntry timer : timers) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Type", timer.type());
            entry.putLong("Due", timer.dueGameTime());
            timerTags.add(entry);
        }
        tag.put("Timers", timerTags);
        tag.putBoolean("H2Active", h2Active);
        tag.putInt("H2PendingSpawns", h2PendingSpawns);
        tag.putInt("H2EndDelay", h2EndDelay);
        ListTag h2 = new ListTag();
        for (double[] candidate : h2Candidates) {
            CompoundTag entry = new CompoundTag();
            entry.putDouble("X", candidate[0]);
            entry.putDouble("Y", candidate[1]);
            entry.putDouble("Z", candidate[2]);
            h2.add(entry);
        }
        tag.put("H2Candidates", h2);
        tag.putBoolean("H1Active", h1Active);
        ListTag h1Beams = new ListTag();
        for (BeamState beam : this.h1Beams) {
            CompoundTag entry = new CompoundTag();
            entry.putDouble("X", beam.x());
            entry.putDouble("Y", beam.y());
            entry.putDouble("Z", beam.z());
            entry.putFloat("Yaw", beam.yaw());
            entry.putInt("Danom", beam.danom());
            entry.putInt("Danom2", beam.danom2());
            h1Beams.add(entry);
        }
        tag.put("H1Beams", h1Beams);
        ListTag h1Warns = new ListTag();
        for (WarnState warn : this.h1Warns) {
            CompoundTag entry = new CompoundTag();
            entry.putDouble("X", warn.x());
            entry.putDouble("Y", warn.y());
            entry.putDouble("Z", warn.z());
            entry.putFloat("Yaw", warn.yaw());
            h1Warns.add(entry);
        }
        tag.put("H1Warns", h1Warns);
        tag.put("H1Damaged", writeUuidList(h1Damaged));
        tag.putBoolean("H4Active", h4Active);
        ListTag h4Sources = new ListTag();
        for (H4SourceState source : this.h4Sources) {
            CompoundTag entry = new CompoundTag();
            entry.putDouble("Radius", source.radius());
            entry.putFloat("Yaw", source.yaw());
            entry.putInt("Danom", source.danom());
            h4Sources.add(entry);
        }
        tag.put("H4Sources", h4Sources);
        ListTag h4Warns = new ListTag();
        for (H4WarnState warn : this.h4Warns) {
            CompoundTag entry = new CompoundTag();
            entry.putDouble("X", warn.x());
            entry.putDouble("Y", warn.y());
            entry.putDouble("Z", warn.z());
            entry.putFloat("Yaw", warn.yaw());
            entry.putInt("Danom", warn.danom());
            h4Warns.add(entry);
        }
        tag.put("H4Warns", h4Warns);
        return tag;
    }

    public boolean h2Active() {
        return h2Active;
    }

    public void setH2Active(boolean h2Active) {
        this.h2Active = h2Active;
        setDirty();
    }

    public int h2PendingSpawns() {
        return h2PendingSpawns;
    }

    public void setH2PendingSpawns(int h2PendingSpawns) {
        this.h2PendingSpawns = h2PendingSpawns;
        setDirty();
    }

    public int h2EndDelay() {
        return h2EndDelay;
    }

    public void setH2EndDelay(int h2EndDelay) {
        this.h2EndDelay = h2EndDelay;
        setDirty();
    }

    public List<double[]> h2Candidates() {
        return h2Candidates;
    }

    public void clearH2Candidates() {
        if (!h2Candidates.isEmpty()) {
            h2Candidates.clear();
            setDirty();
        }
    }

    public void removeH2Candidate(double[] candidate) {
        if (h2Candidates.remove(candidate)) setDirty();
    }

    public boolean h1Active() {
        return h1Active;
    }

    public void setH1Active(boolean h1Active) {
        this.h1Active = h1Active;
        setDirty();
    }

    public List<BeamState> h1Beams() {
        return h1Beams;
    }

    public List<WarnState> h1Warns() {
        return h1Warns;
    }

    public void addH1Beam(BeamState beam) {
        h1Beams.add(beam);
        setDirty();
    }

    public void removeH1Beam(BeamState beam) {
        if (h1Beams.remove(beam)) setDirty();
    }

    public void replaceH1Beams(List<BeamState> beams) {
        h1Beams.clear();
        h1Beams.addAll(beams);
        setDirty();
    }

    public void replaceH1Warns(List<WarnState> warns) {
        h1Warns.clear();
        h1Warns.addAll(warns);
        setDirty();
    }

    public Set<UUID> h1Damaged() {
        return h1Damaged;
    }

    /** Returns true when this rover was not yet damaged in the current H1 round. */
    public boolean markH1Damaged(UUID uuid) {
        if (h1Damaged.add(uuid)) {
            setDirty();
            return true;
        }
        return false;
    }

    public void clearH1Damaged() {
        if (!h1Damaged.isEmpty()) {
            h1Damaged.clear();
            setDirty();
        }
    }

    public boolean h4Active() {
        return h4Active;
    }

    public void setH4Active(boolean h4Active) {
        this.h4Active = h4Active;
        setDirty();
    }

    public List<H4SourceState> h4Sources() {
        return h4Sources;
    }

    public List<H4WarnState> h4Warns() {
        return h4Warns;
    }

    public void addH4Source(H4SourceState source) {
        h4Sources.add(source);
        setDirty();
    }

    public void replaceH4Sources(List<H4SourceState> sources) {
        h4Sources.clear();
        h4Sources.addAll(sources);
        setDirty();
    }

    public void replaceH4Warns(List<H4WarnState> warns) {
        h4Warns.clear();
        h4Warns.addAll(warns);
        setDirty();
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

    public int state() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        setDirty();
    }

    public int fase() {
        return fase;
    }

    public void setFase(int fase) {
        this.fase = fase;
        setDirty();
    }

    public int ronda() {
        return ronda;
    }

    public void setRonda(int ronda) {
        this.ronda = ronda;
        setDirty();
    }

    public int health() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
        setDirty();
    }

    public int healthTotal() {
        return healthTotal;
    }

    public void setHealthTotal(int healthTotal) {
        this.healthTotal = healthTotal;
        setDirty();
    }

    public boolean vulnerable() {
        return vulnerable;
    }

    public void setVulnerable(boolean vulnerable) {
        this.vulnerable = vulnerable;
        setDirty();
    }

    public int addCount() {
        return addCount;
    }

    public void setAddCount(int addCount) {
        this.addCount = addCount;
        setDirty();
    }

    public UUID matrixUuid() {
        return matrixUuid;
    }

    public void setMatrixUuid(UUID matrixUuid) {
        this.matrixUuid = matrixUuid;
        setDirty();
    }

    public void addParticipant(UUID uuid) {
        if (participants.add(uuid)) setDirty();
    }

    public void clearParticipants() {
        if (!participants.isEmpty()) {
            participants.clear();
            setDirty();
        }
    }

    public int participantCount() {
        return participants.size();
    }

    public void addSpectator(UUID uuid) {
        if (spectators.add(uuid)) setDirty();
    }

    public void clearSpectators() {
        if (!spectators.isEmpty()) {
            spectators.clear();
            setDirty();
        }
    }

    public void assignMount(UUID rider, UUID rover) {
        if (mountOwnership.put(rider, rover) == null) {
            mountOrder.add(rider);
        }
        setDirty();
    }

    public void clearMounts() {
        if (!mountOwnership.isEmpty() || !mountOrder.isEmpty()) {
            mountOwnership.clear();
            mountOrder.clear();
            setDirty();
        }
    }

    public UUID mountRover(UUID rider) {
        return mountOwnership.get(rider);
    }

    public List<UUID> mountOrder() {
        return mountOrder;
    }

    public void addCleanup(UUID uuid) {
        if (cleanupEntities.add(uuid)) setDirty();
    }

    public Set<UUID> cleanupEntities() {
        return cleanupEntities;
    }

    public void clearCleanup() {
        if (!cleanupEntities.isEmpty()) {
            cleanupEntities.clear();
            setDirty();
        }
    }

    public List<TimerEntry> timers() {
        return timers;
    }

    public void addTimer(TimerEntry timer) {
        timers.add(timer);
        setDirty();
    }

    public void removeTimer(TimerEntry timer) {
        if (timers.remove(timer)) setDirty();
    }

    public void clearTimers() {
        if (!timers.isEmpty()) {
            timers.clear();
            setDirty();
        }
    }

    public boolean ruleImmediateRespawn() {
        return ruleImmediateRespawn;
    }

    public void setRuleImmediateRespawn(boolean value) {
        this.ruleImmediateRespawn = value;
        setDirty();
    }

    public boolean ruleMobGriefing() {
        return ruleMobGriefing;
    }

    public void setRuleMobGriefing(boolean value) {
        this.ruleMobGriefing = value;
        setDirty();
    }

    public boolean ruleMobSpawning() {
        return ruleMobSpawning;
    }

    public void setRuleMobSpawning(boolean value) {
        this.ruleMobSpawning = value;
        setDirty();
    }

    public boolean ruleKeepInventory() {
        return ruleKeepInventory;
    }

    public void setRuleKeepInventory(boolean value) {
        this.ruleKeepInventory = value;
        setDirty();
    }

    public boolean ruleFireTick() {
        return ruleFireTick;
    }

    public void setRuleFireTick(boolean value) {
        this.ruleFireTick = value;
        setDirty();
    }

    public boolean ruleNaturalRegeneration() {
        return ruleNaturalRegeneration;
    }

    public void setRuleNaturalRegeneration(boolean value) {
        this.ruleNaturalRegeneration = value;
        setDirty();
    }

    public int ruleRandomTickSpeed() {
        return ruleRandomTickSpeed;
    }

    public void setRuleRandomTickSpeed(int value) {
        this.ruleRandomTickSpeed = value;
        setDirty();
    }
}
