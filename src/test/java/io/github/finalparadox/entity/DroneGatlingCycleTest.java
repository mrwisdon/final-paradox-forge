package io.github.finalparadox.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DroneGatlingCycleTest {
    @Test
    void warmsForExactlyTwentyTicksBeforeFirstSalvo() {
        DroneGatlingCycle.Snapshot snapshot = DroneGatlingCycle.Snapshot.initial();
        for (int tick = 1; tick < DroneGatlingCycle.WARMUP_TICKS; tick++) {
            DroneGatlingCycle.Step step = DroneGatlingCycle.advance(true, snapshot);
            snapshot = step.snapshot();
            assertEquals(DroneGatlingCycle.WARMING, snapshot.state());
            assertFalse(step.fire());
        }

        DroneGatlingCycle.Step twentieth = DroneGatlingCycle.advance(true, snapshot);
        assertEquals(DroneGatlingCycle.FIRING, twentieth.snapshot().state());
        assertEquals(1, twentieth.snapshot().heat());
        assertTrue(twentieth.fire());
    }

    @Test
    void overheatsOnTheFullOneHundredTwentiethFiringTick() {
        DroneGatlingCycle.Snapshot snapshot = enterFiring();
        assertEquals(1, snapshot.heat());

        for (int firingTick = 2; firingTick <= 119; firingTick++) {
            DroneGatlingCycle.Step step = DroneGatlingCycle.advance(true, snapshot);
            snapshot = step.snapshot();
            assertEquals(DroneGatlingCycle.FIRING, snapshot.state());
            assertEquals(firingTick, snapshot.heat());
            assertFalse(step.overheatedStarted());
        }

        DroneGatlingCycle.Step firingTick120 = DroneGatlingCycle.advance(true, snapshot);
        assertEquals(DroneGatlingCycle.OVERHEATED, firingTick120.snapshot().state());
        assertEquals(120, firingTick120.snapshot().heat());
        assertEquals(80, firingTick120.snapshot().overheatLockTicks());
        assertTrue(firingTick120.overheatedStarted());
        assertFalse(firingTick120.fire());

        DroneGatlingCycle.Step followingTick =
                DroneGatlingCycle.advance(true, firingTick120.snapshot());
        assertEquals(DroneGatlingCycle.OVERHEATED, followingTick.snapshot().state());
        assertEquals(79, followingTick.snapshot().overheatLockTicks());
        assertFalse(followingTick.overheatedStarted());
    }

    @Test
    void lockedOverheatCannotBeCanceledByRelease() {
        DroneGatlingCycle.Snapshot snapshot = overheat();
        for (int cooldownTick = 1; cooldownTick < 80; cooldownTick++) {
            snapshot = DroneGatlingCycle.advance(false, snapshot).snapshot();
            assertEquals(DroneGatlingCycle.OVERHEATED, snapshot.state());
            assertEquals(80 - cooldownTick, snapshot.overheatLockTicks());
        }

        snapshot = DroneGatlingCycle.advance(false, snapshot).snapshot();
        assertEquals(DroneGatlingCycle.IDLE, snapshot.state());
        assertEquals(0, snapshot.overheatLockTicks());
    }

    @Test
    void heldInputRestartsWarmupOnlyAfterOverheatLockEnds() {
        DroneGatlingCycle.Snapshot snapshot = overheat();
        for (int cooldownTick = 0; cooldownTick < 79; cooldownTick++) {
            snapshot = DroneGatlingCycle.advance(true, snapshot).snapshot();
        }
        assertEquals(DroneGatlingCycle.OVERHEATED, snapshot.state());
        assertEquals(1, snapshot.overheatLockTicks());

        snapshot = DroneGatlingCycle.advance(true, snapshot).snapshot();
        assertEquals(DroneGatlingCycle.WARMING, snapshot.state());
        assertEquals(0, snapshot.warmupTicks());
        snapshot = DroneGatlingCycle.advance(true, snapshot).snapshot();
        assertEquals(1, snapshot.warmupTicks());
    }

    @Test
    void unlockedHeatDropsByTwoWheneverNotFiring() {
        DroneGatlingCycle.Snapshot released = new DroneGatlingCycle.Snapshot(
                DroneGatlingCycle.FIRING, 20, 51, 0, 0);
        released = DroneGatlingCycle.advance(false, released).snapshot();
        assertEquals(DroneGatlingCycle.IDLE, released.state());
        assertEquals(49, released.heat());

        DroneGatlingCycle.Snapshot warming = new DroneGatlingCycle.Snapshot(
                DroneGatlingCycle.WARMING, 5, 49, 0, 0);
        warming = DroneGatlingCycle.advance(true, warming).snapshot();
        assertEquals(47, warming.heat());
    }

    private static DroneGatlingCycle.Snapshot enterFiring() {
        DroneGatlingCycle.Snapshot snapshot = DroneGatlingCycle.Snapshot.initial();
        for (int tick = 0; tick < 20; tick++) {
            snapshot = DroneGatlingCycle.advance(true, snapshot).snapshot();
        }
        return snapshot;
    }

    private static DroneGatlingCycle.Snapshot overheat() {
        DroneGatlingCycle.Snapshot snapshot = enterFiring();
        for (int tick = 1; tick < 120; tick++) {
            snapshot = DroneGatlingCycle.advance(true, snapshot).snapshot();
        }
        return snapshot;
    }
}
