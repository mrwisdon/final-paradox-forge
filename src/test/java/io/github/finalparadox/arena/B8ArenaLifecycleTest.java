package io.github.finalparadox.arena;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class B8ArenaLifecycleTest {
    @Test
    void activeFightNeverRunsArenaReconcile() {
        assertFalse(B8ArenaLifecycle.shouldReconcile(0L, true, false));
        assertFalse(B8ArenaLifecycle.shouldReconcile(40L, true, false));
        assertFalse(B8ArenaLifecycle.shouldReconcile(4_000L, true, false));
    }

    @Test
    void triggeredEncounterNeverRunsArenaReconcile() {
        assertFalse(B8ArenaLifecycle.shouldReconcile(0L, false, true));
        assertFalse(B8ArenaLifecycle.shouldReconcile(40L, false, true));
        assertFalse(B8ArenaLifecycle.shouldReconcile(4_000L, false, true));
    }

    @Test
    void waitingBatchReconcilesOnceEveryTwoSecondsBeforeTrigger() {
        assertTrue(B8ArenaLifecycle.shouldReconcile(0L, false, false));
        assertFalse(B8ArenaLifecycle.shouldReconcile(5L, false, false));
        assertFalse(B8ArenaLifecycle.shouldReconcile(35L, false, false));
        assertTrue(B8ArenaLifecycle.shouldReconcile(40L, false, false));
        assertTrue(B8ArenaLifecycle.shouldReconcile(4_000L, false, false));
        assertFalse(B8ArenaLifecycle.shouldReconcile(4_001L, false, false));
    }
}
