package io.github.finalparadox.arena;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArenaPlayerStateTest {
    @Test
    void firstSnapshotWinsAcrossConsecutiveArenaActivity() {
        CompoundTag data = new CompoundTag();

        assertEquals(GameType.SURVIVAL.getId(),
                ArenaPlayerState.firstOrExistingGameMode(data, GameType.SURVIVAL.getId()));
        assertEquals(GameType.SURVIVAL.getId(),
                ArenaPlayerState.firstOrExistingGameMode(data, GameType.CREATIVE.getId()));

        assertTrue(ArenaPlayerState.hasOriginalGameMode(data));
        assertEquals(GameType.SURVIVAL.getId(), ArenaPlayerState.originalGameModeId(data));
    }

    @Test
    void restoreTargetPrefersSnapshotOverPendingFallback() {
        CompoundTag data = new CompoundTag();
        ArenaPlayerState.firstOrExistingGameMode(data, GameType.ADVENTURE.getId());
        ArenaPlayerState.markPendingModeRestore(data);

        assertEquals(Optional.of(GameType.ADVENTURE), ArenaPlayerState.restoreTargetGameMode(data));
    }

    @Test
    void restoreTargetFallsBackToSurvivalOnlyWhenRestorePending() {
        CompoundTag pending = new CompoundTag();
        ArenaPlayerState.markPendingModeRestore(pending);
        assertEquals(Optional.of(GameType.SURVIVAL), ArenaPlayerState.restoreTargetGameMode(pending));

        CompoundTag noPending = new CompoundTag();
        assertEquals(Optional.empty(), ArenaPlayerState.restoreTargetGameMode(noPending));
    }

    @Test
    void restoreTargetTreatsOutOfRangeSnapshotAsSurvivalFallbackOnlyWhenPending() {
        CompoundTag pending = new CompoundTag();
        pending.putInt(ArenaPlayerState.ORIGINAL_GAME_MODE, 99);
        ArenaPlayerState.markPendingModeRestore(pending);
        assertEquals(Optional.of(GameType.SURVIVAL), ArenaPlayerState.restoreTargetGameMode(pending));

        CompoundTag corruptWithoutPending = new CompoundTag();
        corruptWithoutPending.putInt(ArenaPlayerState.ORIGINAL_GAME_MODE, 99);
        assertEquals(Optional.empty(), ArenaPlayerState.restoreTargetGameMode(corruptWithoutPending));
    }

    @Test
    void validGameModeByIdDecodesKnownIdsOnly() {
        assertEquals(Optional.of(GameType.SURVIVAL), ArenaPlayerState.validGameModeById(0));
        assertEquals(Optional.of(GameType.CREATIVE), ArenaPlayerState.validGameModeById(1));
        assertEquals(Optional.of(GameType.ADVENTURE), ArenaPlayerState.validGameModeById(2));
        assertEquals(Optional.of(GameType.SPECTATOR), ArenaPlayerState.validGameModeById(3));
        assertEquals(Optional.empty(), ArenaPlayerState.validGameModeById(-1));
        assertEquals(Optional.empty(), ArenaPlayerState.validGameModeById(99));
    }

    @Test
    void cloneCopiesOriginalModeAndPendingRestoreState() {
        CompoundTag source = new CompoundTag();
        ArenaPlayerState.firstOrExistingGameMode(source, GameType.CREATIVE.getId());
        ArenaPlayerState.markPendingModeRestore(source);

        CompoundTag target = new CompoundTag();
        ArenaPlayerState.copyModeRestoreState(source, target);

        assertTrue(ArenaPlayerState.hasOriginalGameMode(target));
        assertEquals(GameType.CREATIVE.getId(), ArenaPlayerState.originalGameModeId(target));
        assertTrue(ArenaPlayerState.hasPendingModeRestore(target));
    }

    @Test
    void cloneCopiesNothingWhenSourceHasNoRestoreState() {
        CompoundTag target = new CompoundTag();
        ArenaPlayerState.copyModeRestoreState(new CompoundTag(), target);

        assertFalse(ArenaPlayerState.hasOriginalGameMode(target));
        assertFalse(ArenaPlayerState.hasPendingModeRestore(target));
    }

    @Test
    void loginPendingConsumptionOrderKeepsOriginalAdventureMode() {
        // Fixed order: pending consumption runs before the stale outside-arena
        // restore. The snapshot wins over the SURVIVAL fallback and the later
        // duplicate/stale restore is a no-op.
        CompoundTag fixedOrder = new CompoundTag();
        ArenaPlayerState.firstOrExistingGameMode(fixedOrder, GameType.ADVENTURE.getId());
        ArenaPlayerState.markPendingModeRestore(fixedOrder); // consume step
        assertEquals(Optional.of(GameType.ADVENTURE), restoreTargetAndClear(fixedOrder));
        assertFalse(ArenaPlayerState.hasOriginalGameMode(fixedOrder));
        assertFalse(ArenaPlayerState.hasPendingModeRestore(fixedOrder));
        assertEquals(Optional.empty(), ArenaPlayerState.restoreTargetGameMode(fixedOrder));

        // Buggy old order: the stale restore cleared the snapshot first, then the
        // later consumption step fell back to SURVIVAL and overwrote Adventure.
        CompoundTag buggyOrder = new CompoundTag();
        ArenaPlayerState.firstOrExistingGameMode(buggyOrder, GameType.ADVENTURE.getId());
        assertEquals(Optional.of(GameType.ADVENTURE), restoreTargetAndClear(buggyOrder));
        ArenaPlayerState.markPendingModeRestore(buggyOrder);
        assertEquals(Optional.of(GameType.SURVIVAL),
                ArenaPlayerState.restoreTargetGameMode(buggyOrder));
    }

    @Test
    void owesDefeatModeRestoreSkipsEscapedNonSpectator() {
        // Online participant already left the arena: no snapshot, not spectator.
        assertFalse(ArenaFightParticipants.owesDefeatModeRestore(false, false));
        // Defeat flow: still a spectator, or snapshot still present.
        assertTrue(ArenaFightParticipants.owesDefeatModeRestore(false, true));
        assertTrue(ArenaFightParticipants.owesDefeatModeRestore(true, false));
        assertTrue(ArenaFightParticipants.owesDefeatModeRestore(true, true));
    }

    private static Optional<GameType> restoreTargetAndClear(CompoundTag data) {
        Optional<GameType> target = ArenaPlayerState.restoreTargetGameMode(data);
        data.remove(ArenaPlayerState.ORIGINAL_GAME_MODE);
        data.remove(ArenaPlayerState.PENDING_MODE_RESTORE);
        return target;
    }
}
