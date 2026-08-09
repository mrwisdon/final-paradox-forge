package io.github.finalparadox.entity;

/** Pure server-authoritative Gatling timing state, kept free of Minecraft classes. */
public final class DroneGatlingCycle {
    public static final int IDLE = 0;
    public static final int WARMING = 1;
    public static final int FIRING = 2;
    public static final int OVERHEATED = 3;

    public static final int WARMUP_TICKS = 20;
    public static final int MAX_HEAT = 120;
    public static final int OVERHEAT_LOCK_TICKS = 80;
    public static final int FIRE_INTERVAL_TICKS = 2;

    private DroneGatlingCycle() {
    }

    public static Step advance(boolean held, Snapshot current) {
        if (current.state == OVERHEATED) {
            int nextLock = Math.max(0, current.overheatLockTicks - 1);
            int nextHeat = Math.max(0, current.heat - 2);
            if (nextLock > 0) {
                return new Step(new Snapshot(
                        OVERHEATED, 0, nextHeat, nextLock, 0), false, false);
            }
            return new Step(new Snapshot(
                    held ? WARMING : IDLE, 0, nextHeat, 0, 0), false, false);
        }

        int cooledHeat = current.state == FIRING && held
                ? current.heat : Math.max(0, current.heat - 2);
        if (!held) {
            return new Step(new Snapshot(IDLE, 0, cooledHeat, 0, 0), false, false);
        }

        if (current.state != FIRING) {
            int nextWarmup = current.warmupTicks + 1;
            if (nextWarmup < WARMUP_TICKS) {
                return new Step(new Snapshot(
                        WARMING, nextWarmup, cooledHeat, 0, 0), false, false);
            }
            int nextHeat = cooledHeat + 1;
            if (nextHeat >= MAX_HEAT) {
                return overheatedStep();
            }
            return new Step(new Snapshot(
                    FIRING, WARMUP_TICKS, nextHeat, 0,
                    FIRE_INTERVAL_TICKS - 1), true, false);
        }

        int nextHeat = current.heat + 1;
        if (nextHeat >= MAX_HEAT) {
            return overheatedStep();
        }
        if (current.fireCooldown > 0) {
            return new Step(new Snapshot(
                    FIRING, WARMUP_TICKS, nextHeat, 0,
                    current.fireCooldown - 1), false, false);
        }
        return new Step(new Snapshot(
                FIRING, WARMUP_TICKS, nextHeat, 0,
                FIRE_INTERVAL_TICKS - 1), true, false);
    }

    private static Step overheatedStep() {
        return new Step(new Snapshot(
                OVERHEATED, 0, MAX_HEAT, OVERHEAT_LOCK_TICKS, 0),
                false, true);
    }

    public record Snapshot(
            int state,
            int warmupTicks,
            int heat,
            int overheatLockTicks,
            int fireCooldown) {
        public static Snapshot initial() {
            return new Snapshot(IDLE, 0, 0, 0, 0);
        }
    }

    public record Step(Snapshot snapshot, boolean fire, boolean overheatedStarted) {
    }
}
