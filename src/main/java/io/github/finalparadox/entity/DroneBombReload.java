package io.github.finalparadox.entity;

/**
 * Pure server-authoritative underslung bomb reload state, kept free of
 * Minecraft classes so its semantics can be unit-tested directly.
 */
public final class DroneBombReload {
    public static final int MAX_BOMBS = 8;
    public static final int RELOAD_TICKS = 20 * 10;

    private DroneBombReload() {
    }

    /**
     * Advances one server tick while the drone has a valid active controlling
     * rider. A full payload never banks progress. Below the cap, the interval
     * is always a fresh full {@link #RELOAD_TICKS} run: on the tick where
     * progress reaches the interval exactly one bomb is restored and progress
     * resets to zero.
     */
    public static Snapshot tick(Snapshot current) {
        if (current.bombs >= MAX_BOMBS) {
            return new Snapshot(MAX_BOMBS, 0);
        }
        int nextProgress = current.progressTicks + 1;
        if (nextProgress >= RELOAD_TICKS) {
            return new Snapshot(current.bombs + 1, 0);
        }
        return new Snapshot(current.bombs, nextProgress);
    }

    /**
     * A successful bomb drop restarts a fresh full interval. The count is
     * clamped so it can never go below zero.
     */
    public static Snapshot afterDrop(Snapshot current) {
        return new Snapshot(Math.max(0, current.bombs - 1), 0);
    }

    /**
     * Normalizes arbitrary (for example legacy NBT) values into a valid
     * snapshot: bomb count in [0, MAX_BOMBS], progress in [0, RELOAD_TICKS-1].
     * A missing legacy progress tag reads as zero through the entity NBT
     * loader, which this function accepts safely.
     */
    public static Snapshot normalize(int bombs, int progressTicks) {
        int normalizedBombs = clamp(bombs, 0, MAX_BOMBS);
        int normalizedProgress = normalizedBombs == MAX_BOMBS
                ? 0 : clamp(progressTicks, 0, RELOAD_TICKS - 1);
        return new Snapshot(normalizedBombs, normalizedProgress);
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public record Snapshot(int bombs, int progressTicks) {
        public static Snapshot initial() {
            return new Snapshot(MAX_BOMBS, 0);
        }
    }
}
