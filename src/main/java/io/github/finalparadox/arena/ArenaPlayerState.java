package io.github.finalparadox.arena;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.registry.ModDimensions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

/** Per-player persistent ownership of pending travel and the current arena slot. */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID)
public final class ArenaPlayerState {
    static final String PENDING_ARENA = "finalparadox.pending_arena";
    static final String PENDING_ORIGIN = "finalparadox.pending_arena_origin";
    static final String CURRENT_ARENA = "finalparadox.current_arena";
    static final String VOID_GUARD_UNTIL = "finalparadox.void_guard_until";
    static final String ORIGINAL_GAME_MODE = "finalparadox.original_game_mode";
    static final String PENDING_MODE_RESTORE = "finalparadox.pending_mode_restore";

    private ArenaPlayerState() {
    }

    public static void waitFor(ServerPlayer player, ArenaSlots.ArenaSlot slot) {
        CompoundTag data = player.getPersistentData();
        data.putString(PENDING_ARENA, slot.definition().id());
        data.putString(PENDING_ORIGIN, player.serverLevel().dimension().location().toString());
    }

    public static Optional<ArenaSlots.ArenaSlot> pending(ServerPlayer player) {
        return ArenaSlots.forArenaId(player.getPersistentData().getString(PENDING_ARENA));
    }

    public static boolean pendingOriginStillMatches(ServerPlayer player) {
        return player.serverLevel().dimension().location().toString()
                .equals(player.getPersistentData().getString(PENDING_ORIGIN));
    }

    public static void clearPending(ServerPlayer player) {
        player.getPersistentData().remove(PENDING_ARENA);
        player.getPersistentData().remove(PENDING_ORIGIN);
    }

    public static void enter(ServerPlayer player, ArenaSlots.ArenaSlot slot) {
        CompoundTag data = player.getPersistentData();
        data.putString(CURRENT_ARENA, slot.definition().id());
        firstOrExistingGameMode(data, player.gameMode.getGameModeForPlayer().getId());
    }

    public static Optional<ArenaSlots.ArenaSlot> current(ServerPlayer player) {
        return ArenaSlots.forArenaId(player.getPersistentData().getString(CURRENT_ARENA));
    }

    public static void clearCurrent(ServerPlayer player) {
        player.getPersistentData().remove(CURRENT_ARENA);
        player.getPersistentData().remove(VOID_GUARD_UNTIL);
    }

    public static boolean beginVoidGuard(ServerPlayer player, int ticks) {
        long now = player.serverLevel().getGameTime();
        long until = player.getPersistentData().getLong(VOID_GUARD_UNTIL);
        if (until > now) return false;
        player.getPersistentData().putLong(VOID_GUARD_UNTIL, now + ticks);
        return true;
    }

    /**
     * Restores the game mode the player had when arena activity began and
     * atomically clears the snapshot plus any pending restore marker. When a
     * restore is pending but the snapshot is absent or corrupt, SURVIVAL is the
     * safe default; without either marker this is a no-op.
     */
    public static void restoreOriginalGameMode(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        restoreTargetGameMode(data).ifPresent(player::setGameMode);
        data.remove(ORIGINAL_GAME_MODE);
        data.remove(PENDING_MODE_RESTORE);
    }

    /** First snapshot wins so consecutive arena activity never overwrites it. */
    static int firstOrExistingGameMode(CompoundTag data, int currentId) {
        if (!hasOriginalGameMode(data)) {
            data.putInt(ORIGINAL_GAME_MODE, currentId);
        }
        return data.getInt(ORIGINAL_GAME_MODE);
    }

    static boolean hasOriginalGameMode(CompoundTag data) {
        return data.contains(ORIGINAL_GAME_MODE, Tag.TAG_INT);
    }

    static int originalGameModeId(CompoundTag data) {
        return data.getInt(ORIGINAL_GAME_MODE);
    }

    static boolean hasPendingModeRestore(CompoundTag data) {
        return data.getBoolean(PENDING_MODE_RESTORE);
    }

    static void markPendingModeRestore(CompoundTag data) {
        data.putBoolean(PENDING_MODE_RESTORE, true);
    }

    /** Decides the mode to restore; empty means nothing is owed. */
    static Optional<GameType> restoreTargetGameMode(CompoundTag data) {
        if (hasOriginalGameMode(data)) {
            Optional<GameType> decoded = validGameModeById(data.getInt(ORIGINAL_GAME_MODE));
            if (decoded.isPresent()) return decoded;
        }
        return hasPendingModeRestore(data)
                ? Optional.of(GameType.SURVIVAL)
                : Optional.empty();
    }

    /** Safe numeric decode; empty when the stored id is out of range (corrupt). */
    static Optional<GameType> validGameModeById(int id) {
        for (GameType type : GameType.values()) {
            if (type.getId() == id) return Optional.of(type);
        }
        return Optional.empty();
    }

    /** Copies durable restore state across a respawn clone. */
    static void copyModeRestoreState(CompoundTag source, CompoundTag target) {
        if (hasOriginalGameMode(source)) {
            target.putInt(ORIGINAL_GAME_MODE, source.getInt(ORIGINAL_GAME_MODE));
        }
        if (hasPendingModeRestore(source)) {
            target.putBoolean(PENDING_MODE_RESTORE, true);
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clearPending(player);
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        clearPending(player);
        // Consume a queued offline defeat restore before any stale outside-arena
        // restore/clear, so the outcome is independent of subscriber order and a
        // SURVIVAL fallback can never overwrite the restored original mode.
        ArenaFightParticipants.consumePendingModeRestore(player);
        if (!ModDimensions.isActiveArena(player.serverLevel().dimension())) {
            restoreOriginalGameMode(player);
            clearCurrent(player);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        clearPending(player);
        if (ModDimensions.isActiveArena(event.getFrom())
                && !ModDimensions.isActiveArena(event.getTo())) {
            restoreOriginalGameMode(player);
        }
        if (!ModDimensions.isActiveArena(event.getTo())) clearCurrent(player);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer replacement)) return;
        CompoundTag source = event.getOriginal().getPersistentData();
        CompoundTag target = replacement.getPersistentData();
        String current = source.getString(CURRENT_ARENA);
        if (!current.isEmpty()) target.putString(CURRENT_ARENA, current);
        copyModeRestoreState(source, target);
    }
}
