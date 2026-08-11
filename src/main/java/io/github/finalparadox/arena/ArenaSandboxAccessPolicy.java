package io.github.finalparadox.arena;

/** Prevents a carried/placed sandbox from becoming an escape during a fight. */
public final class ArenaSandboxAccessPolicy {
    private ArenaSandboxAccessPolicy() {
    }

    public static boolean mayRequest(boolean playerInActiveArena, boolean anyFightActive) {
        return !playerInActiveArena || !anyFightActive;
    }
}
