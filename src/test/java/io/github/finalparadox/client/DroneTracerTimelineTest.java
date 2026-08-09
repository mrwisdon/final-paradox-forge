package io.github.finalparadox.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DroneTracerTimelineTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void packetJustBeforeTickBoundaryDoesNotJumpAWholeTick() {
        double spawn = 100.95D;
        double nextFrame = 101.05D;

        double elapsed = DroneTracerTimeline.elapsed(nextFrame, spawn);
        assertEquals(0.10D, elapsed, EPSILON);
        assertEquals(3.20D,
                DroneTracerTimeline.frontDistance(elapsed, 64.0D), EPSILON);
    }

    @Test
    void shortTracerIsHeldInsideFadeUntilItHasRenderedOnce() {
        double length = 1.0D;
        double stalledElapsed = 1.0D;
        double visualElapsed = DroneTracerTimeline.visualElapsed(
                stalledElapsed, length, false);

        assertTrue(DroneTracerTimeline.arrivalFade(visualElapsed, length) > 0.0D);
        assertEquals(length,
                DroneTracerTimeline.frontDistance(visualElapsed, length), EPSILON);
        assertTrue(DroneTracerTimeline.isExpired(stalledElapsed, length));
    }

    @Test
    void sixtyFourBlockTracerTravelsForExactlyTwoTicks() {
        assertEquals(2.0D, DroneTracerTimeline.travelTicks(64.0D), EPSILON);
        assertFalse(DroneTracerTimeline.hasArrived(1.999D, 64.0D));
        assertTrue(DroneTracerTimeline.hasArrived(2.0D, 64.0D));
    }

    @Test
    void frontTailAndArrivalFadeHaveStableBoundaries() {
        assertEquals(16.0D,
                DroneTracerTimeline.frontDistance(0.5D, 64.0D), EPSILON);
        assertEquals(15.35D,
                DroneTracerTimeline.tailDistance(16.0D), EPSILON);
        assertEquals(1.0D,
                DroneTracerTimeline.arrivalFade(2.0D, 64.0D), EPSILON);
        assertEquals(0.5D,
                DroneTracerTimeline.arrivalFade(2.125D, 64.0D), EPSILON);
        assertEquals(0.0D,
                DroneTracerTimeline.arrivalFade(2.25D, 64.0D), EPSILON);
    }
}
