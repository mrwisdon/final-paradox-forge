package io.github.finalparadox.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Optional;
import java.util.UUID;

public final class ArenaDeploymentData extends SavedData {
    private static final String DATA_NAME = "finalparadox_arena_deployment";

    private DeploymentState state = DeploymentState.IDLE;
    private String arenaId = "";
    private BlockPos floorAnchor;
    private int nextTile;
    private String error = "";
    private UUID activeBossUuid;
    private UUID stagedKoyoUuid;
    private UUID stagedGariUuid;
    private UUID korosUuid;

    public static ArenaDeploymentData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ArenaDeploymentData::load, ArenaDeploymentData::new, DATA_NAME);
    }

    private static ArenaDeploymentData load(CompoundTag tag) {
        ArenaDeploymentData data = new ArenaDeploymentData();
        try {
            data.state = DeploymentState.valueOf(tag.getString("State"));
        } catch (IllegalArgumentException exception) {
            data.state = DeploymentState.ERROR;
            data.error = "Saved deployment state was invalid";
        }
        data.arenaId = tag.getString("ArenaId");
        if (tag.contains("FloorAnchor", Tag.TAG_LONG)) data.floorAnchor = BlockPos.of(tag.getLong("FloorAnchor"));
        data.nextTile = Math.max(0, tag.getInt("NextTile"));
        if (data.error.isEmpty()) data.error = tag.getString("Error");
        if (tag.hasUUID("ActiveBoss")) data.activeBossUuid = tag.getUUID("ActiveBoss");
        if (tag.hasUUID("StagedKoyo")) data.stagedKoyoUuid = tag.getUUID("StagedKoyo");
        if (tag.hasUUID("StagedGari")) data.stagedGariUuid = tag.getUUID("StagedGari");
        if (tag.hasUUID("Koros")) data.korosUuid = tag.getUUID("Koros");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString("State", state.name());
        tag.putString("ArenaId", arenaId);
        if (floorAnchor != null) tag.putLong("FloorAnchor", floorAnchor.asLong());
        tag.putInt("NextTile", nextTile);
        tag.putString("Error", error);
        if (activeBossUuid != null) tag.putUUID("ActiveBoss", activeBossUuid);
        if (stagedKoyoUuid != null) tag.putUUID("StagedKoyo", stagedKoyoUuid);
        if (stagedGariUuid != null) tag.putUUID("StagedGari", stagedGariUuid);
        if (korosUuid != null) tag.putUUID("Koros", korosUuid);
        return tag;
    }

    public void begin(ArenaDefinition definition, BlockPos anchor) {
        state = DeploymentState.DEPLOYING;
        arenaId = definition.id();
        floorAnchor = anchor.immutable();
        nextTile = 0;
        error = "";
        activeBossUuid = null;
        stagedKoyoUuid = null;
        stagedGariUuid = null;
        korosUuid = null;
        setDirty();
    }

    public void advance(ArenaDefinition definition) {
        nextTile++;
        if (nextTile >= definition.tileCount()) state = DeploymentState.READY;
        setDirty();
    }

    public void fail(String message) {
        state = DeploymentState.ERROR;
        error = message;
        setDirty();
    }

    public void setActiveBossUuid(UUID uuid) {
        activeBossUuid = uuid;
        setDirty();
    }

    public void clearActiveBoss() {
        if (activeBossUuid == null) return;
        activeBossUuid = null;
        setDirty();
    }

    public void setB5Stage(UUID koyoUuid, UUID gariUuid, UUID echoUuid) {
        stagedKoyoUuid = koyoUuid;
        stagedGariUuid = gariUuid;
        korosUuid = echoUuid;
        activeBossUuid = koyoUuid;
        setDirty();
    }

    public void clearB5Stage() {
        stagedKoyoUuid = null;
        stagedGariUuid = null;
        korosUuid = null;
        activeBossUuid = null;
        setDirty();
    }

    public void clearKoros() {
        if (korosUuid == null) return;
        korosUuid = null;
        setDirty();
    }

    public DeploymentState state() {
        return state;
    }

    public String arenaId() {
        return arenaId;
    }

    public Optional<BlockPos> floorAnchor() {
        return Optional.ofNullable(floorAnchor);
    }

    public int nextTile() {
        return nextTile;
    }

    public String error() {
        return error;
    }

    public Optional<UUID> activeBossUuid() {
        return Optional.ofNullable(activeBossUuid);
    }

    public Optional<UUID> stagedKoyoUuid() {
        return Optional.ofNullable(stagedKoyoUuid);
    }

    public Optional<UUID> stagedGariUuid() {
        return Optional.ofNullable(stagedGariUuid);
    }

    public Optional<UUID> korosUuid() {
        return Optional.ofNullable(korosUuid);
    }

    public enum DeploymentState {
        IDLE,
        DEPLOYING,
        READY,
        ERROR
    }
}
