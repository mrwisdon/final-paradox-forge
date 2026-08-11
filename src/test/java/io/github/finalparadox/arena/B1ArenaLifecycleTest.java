package io.github.finalparadox.arena;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class B1ArenaLifecycleTest {
    private static final UUID WAITING = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ACTIVE_A = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID ACTIVE_B = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID UNLOADED = UUID.fromString("00000000-0000-0000-0000-000000000004");

    @Test
    void activeFightNeverRunsArenaReconcile() {
        assertFalse(B1ArenaLifecycle.shouldReconcile(0L, true));
        assertFalse(B1ArenaLifecycle.shouldReconcile(40L, true));
        assertFalse(B1ArenaLifecycle.shouldReconcile(4_000L, true));
    }

    @Test
    void waitingBatchReconcilesTwicePerSecond() {
        assertTrue(B1ArenaLifecycle.shouldReconcile(0L, false));
        assertFalse(B1ArenaLifecycle.shouldReconcile(5L, false));
        assertFalse(B1ArenaLifecycle.shouldReconcile(35L, false));
        assertTrue(B1ArenaLifecycle.shouldReconcile(40L, false));
    }

    @Test
    void runningBossOutranksSavedWaitingCopy() {
        B1ArenaLifecycle.BossRef waiting = new B1ArenaLifecycle.BossRef(WAITING, true, 1.0D);
        B1ArenaLifecycle.BossRef active = new B1ArenaLifecycle.BossRef(ACTIVE_A, false, 20.0D);
        assertEquals(ACTIVE_A, B1ArenaLifecycle.selectAuthoritative(
                Optional.of(WAITING), List.of(waiting, active)).orElseThrow().uuid());
    }

    @Test
    void savedRunningBossWinsAndSurplusSelectionIsDeterministic() {
        B1ArenaLifecycle.BossRef first = new B1ArenaLifecycle.BossRef(ACTIVE_A, false, 10.0D);
        B1ArenaLifecycle.BossRef saved = new B1ArenaLifecycle.BossRef(ACTIVE_B, false, 30.0D);
        assertEquals(ACTIVE_B, B1ArenaLifecycle.selectAuthoritative(
                Optional.of(ACTIVE_B), List.of(first, saved)).orElseThrow().uuid());
        assertEquals(ACTIVE_A, B1ArenaLifecycle.selectAuthoritative(
                Optional.empty(), List.of(saved, first)).orElseThrow().uuid());
    }

    @Test
    void unloadedSavedOwnerIsPreservedAndLoadedCopyDiscardsItself() {
        assertEquals(B1ArenaLifecycle.AuthorityDecision.DISCARD_SELF,
                B1ArenaLifecycle.authorityDecision(
                        ACTIVE_A, false, Optional.of(UNLOADED), false, false, false));
        assertFalse(B1ArenaLifecycle.shouldClearRecordedBoss(false, false, false));
    }

    @Test
    void loadedRunningBossReplacesSavedWaitingOwner() {
        assertEquals(B1ArenaLifecycle.AuthorityDecision.REPLACE_WAITING,
                B1ArenaLifecycle.authorityDecision(
                        ACTIVE_A, false, Optional.of(WAITING), true, true, true));
    }

    @Test
    void resolvedInvalidRecordMayBeReclaimedAndCleared() {
        assertEquals(B1ArenaLifecycle.AuthorityDecision.CLAIM,
                B1ArenaLifecycle.authorityDecision(
                        ACTIVE_A, false, Optional.of(UNLOADED), true, false, false));
        assertTrue(B1ArenaLifecycle.shouldClearRecordedBoss(false, true, false));
        assertTrue(B1ArenaLifecycle.shouldClearRecordedBoss(true, false, false));
    }

    @Test
    void savedRunningOwnerRejectsOtherLoadedBosses() {
        assertEquals(B1ArenaLifecycle.AuthorityDecision.DISCARD_SELF,
                B1ArenaLifecycle.authorityDecision(
                        ACTIVE_B, false, Optional.of(ACTIVE_A), true, true, false));
        assertEquals(B1ArenaLifecycle.AuthorityDecision.KEEP,
                B1ArenaLifecycle.authorityDecision(
                        ACTIVE_A, false, Optional.of(ACTIVE_A), true, true, false));
    }

    @Test
    void onlyServerSideDeathMayFinishVictory() {
        assertFalse(B1ArenaLifecycle.shouldFinishVictory(false, true));
        assertFalse(B1ArenaLifecycle.shouldFinishVictory(true, false));
        assertTrue(B1ArenaLifecycle.shouldFinishVictory(true, true));
    }
}
