package io.github.finalparadox.arena;

/**
 * Pure policy for the global READY-state waiting maintenance cadence.
 *
 * <p>Every arena in READY state shares one low-cost maintenance schedule:
 * once every two seconds, and only while no boss fight is active in the
 * level. During a fight every READY arena is skipped; deployment
 * (DEPLOYING) is never blocked by this policy.
 */
public final class ArenaReadyMaintenancePolicy {
    /** READY waiting batches reconcile once every two seconds. */
    public static final int READY_MAINTENANCE_TICKS = 40;

    private ArenaReadyMaintenancePolicy() {
    }

    /**
     * Returns true when READY maintenance may run this tick: no fight is
     * active in the level and the game time is on the maintenance interval.
     */
    public static boolean shouldMaintainReady(long gameTime, boolean anyFightActive) {
        return !anyFightActive && gameTime % READY_MAINTENANCE_TICKS == 0L;
    }
}