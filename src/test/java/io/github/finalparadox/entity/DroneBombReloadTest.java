package io.github.finalparadox.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class DroneBombReloadTest {
    @Test
    void fullPayloadNeverBanksProgress() {
        DroneBombReload.Snapshot snapshot = DroneBombReload.Snapshot.initial();
        assertEquals(DroneBombReload.MAX_BOMBS, snapshot.bombs());
        assertEquals(0, snapshot.progressTicks());

        for (int tick = 0; tick < 1000; tick++) {
            snapshot = DroneBombReload.tick(snapshot);
            assertEquals(DroneBombReload.MAX_BOMBS, snapshot.bombs());
            assertEquals(0, snapshot.progressTicks());
        }
    }

    @Test
    void dropRestartsAFreshFullInterval() {
        DroneBombReload.Snapshot fromFull = DroneBombReload.afterDrop(
                new DroneBombReload.Snapshot(DroneBombReload.MAX_BOMBS, 0));
        assertEquals(7, fromFull.bombs());
        assertEquals(0, fromFull.progressTicks());

        DroneBombReload.Snapshot fromPartial = DroneBombReload.afterDrop(
                new DroneBombReload.Snapshot(7, 199));
        assertEquals(6, fromPartial.bombs());
        assertEquals(0, fromPartial.progressTicks());
    }

    @Test
    void restoresExactlyOneAfterFullInterval() {
        DroneBombReload.Snapshot snapshot =
                new DroneBombReload.Snapshot(7, 0);
        for (int tick = 1; tick < DroneBombReload.RELOAD_TICKS; tick++) {
            snapshot = DroneBombReload.tick(snapshot);
            assertEquals(7, snapshot.bombs());
            assertEquals(tick, snapshot.progressTicks());
        }

        snapshot = DroneBombReload.tick(snapshot);
        assertEquals(8, snapshot.bombs());
        assertEquals(0, snapshot.progressTicks());
    }

    @Test
    void eachSubsequentIntervalRestoresOnlyOne() {
        DroneBombReload.Snapshot snapshot =
                new DroneBombReload.Snapshot(5, 0);
        snapshot = advanceFullIntervals(snapshot, 1);
        assertEquals(6, snapshot.bombs());
        assertEquals(0, snapshot.progressTicks());

        snapshot = advanceFullIntervals(snapshot, 1);
        assertEquals(7, snapshot.bombs());
        assertEquals(0, snapshot.progressTicks());
    }

    @Test
    void partialProgressContinuesToTheSameFullInterval() {
        DroneBombReload.Snapshot snapshot =
                new DroneBombReload.Snapshot(6, 150);
        snapshot = advanceTicks(snapshot, 49);
        assertEquals(6, snapshot.bombs());
        assertEquals(199, snapshot.progressTicks());

        snapshot = DroneBombReload.tick(snapshot);
        assertEquals(7, snapshot.bombs());
        assertEquals(0, snapshot.progressTicks());
    }

    @Test
    void normalizeClampsInvalidNbtValues() {
        DroneBombReload.Snapshot negative = DroneBombReload.normalize(-3, -5);
        assertEquals(0, negative.bombs());
        assertEquals(0, negative.progressTicks());

        DroneBombReload.Snapshot oversized = DroneBombReload.normalize(99, 5000);
        assertEquals(DroneBombReload.MAX_BOMBS, oversized.bombs());
        assertEquals(0, oversized.progressTicks());

        DroneBombReload.Snapshot partial = DroneBombReload.normalize(7, 5000);
        assertEquals(7, partial.bombs());
        assertEquals(DroneBombReload.RELOAD_TICKS - 1, partial.progressTicks());
    }

    @Test
    void missingLegacyProgressReadsAsZero() {
        DroneBombReload.Snapshot legacy = DroneBombReload.normalize(7, 0);
        assertEquals(7, legacy.bombs());
        assertEquals(0, legacy.progressTicks());
    }

    private static DroneBombReload.Snapshot advanceFullIntervals(
            DroneBombReload.Snapshot snapshot, int intervals) {
        return advanceTicks(snapshot, intervals * DroneBombReload.RELOAD_TICKS);
    }

    private static DroneBombReload.Snapshot advanceTicks(
            DroneBombReload.Snapshot snapshot, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            snapshot = DroneBombReload.tick(snapshot);
        }
        return snapshot;
    }
}
