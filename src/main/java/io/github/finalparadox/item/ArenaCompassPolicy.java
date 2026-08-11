package io.github.finalparadox.item;

/** Pure responsibility boundary: the compass never chooses an arena entry. */
public final class ArenaCompassPolicy {
    private ArenaCompassPolicy() {
    }

    public static Action decide(boolean inActiveArena, boolean inLegacyArena, boolean activeFight) {
        if (inActiveArena && activeFight) return Action.BLOCKED_BY_FIGHT;
        if (inActiveArena || inLegacyArena) return Action.RETURN_TO_OVERWORLD;
        return Action.DIRECT_TO_SANDBOX;
    }

    public enum Action {
        RETURN_TO_OVERWORLD,
        BLOCKED_BY_FIGHT,
        DIRECT_TO_SANDBOX
    }
}
