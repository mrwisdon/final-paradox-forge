package io.github.finalparadox.arena;

/** Pure policy used by the active void dimension's fall handler. */
public final class ArenaVoidFallPolicy {
    private ArenaVoidFallPolicy() {
    }

    public static Action decide(boolean activeFightParticipant, boolean validCurrentArena) {
        if (activeFightParticipant) return Action.NORMAL_DEATH;
        return validCurrentArena ? Action.RESCUE_TO_ENTRY : Action.RETURN_TO_OVERWORLD;
    }

    public enum Action {
        NORMAL_DEATH,
        RESCUE_TO_ENTRY,
        RETURN_TO_OVERWORLD
    }
}
