package io.github.finalparadox.entity;

final class TerrastalkerDismountRules {
    private TerrastalkerDismountRules() {
    }

    static boolean shouldCancel(
            boolean riderAlive,
            boolean riderRemoved,
            boolean allowDismount
    ) {
        return riderAlive && !riderRemoved && !allowDismount;
    }

    static boolean shouldExit(boolean b8CombatActive) {
        return !b8CombatActive;
    }
}
