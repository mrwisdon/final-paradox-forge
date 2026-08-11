package io.github.finalparadox.ability;

/** Pure timing, input, dimension and healing rules for the Paradoxical Tome. */
public final class ParadoxicalTomeRules {
    public static final int TIMELINE_TICKS = 20 * 20;
    public static final int COOLDOWN_TICKS = 25 * 20;
    public static final int DOUBLE_TAP_WINDOW_TICKS = 5;

    private ParadoxicalTomeRules() {
    }

    public static SneakStep tickSneak(int windowTicks, boolean wasSneaking, boolean sneaking) {
        int window = Math.max(0, windowTicks);
        boolean pressed = sneaking && !wasSneaking;
        if (pressed && window > 0) {
            return new SneakStep(0, true, true);
        }
        if (pressed) {
            return new SneakStep(DOUBLE_TAP_WINDOW_TICKS, true, false);
        }
        return new SneakStep(Math.max(0, window - 1), sneaking, false);
    }

    public static boolean sameDimension(String recordedDimension, String currentDimension) {
        return recordedDimension != null && recordedDimension.equals(currentDimension);
    }

    public static int tickTimeline(int remainingTicks) {
        return Math.max(0, remainingTicks - 1);
    }

    public static boolean timelineActive(int remainingTicks) {
        return remainingTicks > 0;
    }

    public static HealingPlan healingPlan(float initialHealth, float currentHealth) {
        int instantHealing = 0;
        if (initialHealth > currentHealth + 3.0F) {
            instantHealing += 4;
        }
        if (initialHealth > currentHealth + 7.0F) {
            instantHealing += 8;
        }
        return new HealingPlan(instantHealing, initialHealth > currentHealth + 1.0F);
    }

    public static float restoredHealth(float initialHealth, float currentHealth, float maxHealth) {
        float healed = currentHealth + healingPlan(initialHealth, currentHealth).instantHealing();
        return Math.max(currentHealth, Math.min(maxHealth, healed));
    }

    public record SneakStep(int windowTicks, boolean wasSneaking, boolean triggered) {
    }

    public record HealingPlan(int instantHealing, boolean regeneration) {
    }
}
