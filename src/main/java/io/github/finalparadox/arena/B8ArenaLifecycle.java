package io.github.finalparadox.arena;

/**
 * Pure policy for the B8 encounter's low-cost waiting lifecycle. The staged
 * Echo of Koros only needs reconciliation before the encounter is triggered
 * and while no fight is running; during combat the scan is skipped entirely.
 */
public final class B8ArenaLifecycle {
    /** Waiting batches reconcile once every two seconds; active fights never run the arena scan. */
    public static final int WAITING_RECONCILE_TICKS = 40;

    private B8ArenaLifecycle() {
    }

    public static boolean shouldReconcile(
            long gameTime,
            boolean encounterActive,
            boolean b8Triggered
    ) {
        return !encounterActive && !b8Triggered && gameTime % WAITING_RECONCILE_TICKS == 0L;
    }
}
