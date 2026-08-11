package io.github.finalparadox.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MarawTharProjectileGeometryTest {

    @Test
    void axialPointNineIsWithinRadius() {
        assertTrue(MarawTharProjectileGeometry.withinSphereRadius(
                0.0D, 0.0D, 0.0D, 0.9D, 0.0D, 0.0D, 1.0D));
        assertTrue(MarawTharProjectileGeometry.withinSphereRadius(
                0.0D, 0.0D, 0.0D, 0.0D, 0.9D, 0.0D, 1.0D));
        assertTrue(MarawTharProjectileGeometry.withinSphereRadius(
                0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.9D, 1.0D));
    }

    @Test
    void diagonalPointEightIsRejectedDespiteCubeOverlap() {
        // Inside the inflated AABB (|x|,|z| <= 1) but outside the sphere:
        // sqrt(0.8^2 + 0.8^2) ~ 1.131 > 1.
        assertFalse(MarawTharProjectileGeometry.withinSphereRadius(
                0.0D, 0.0D, 0.0D, 0.8D, 0.0D, 0.8D, 1.0D));
        assertFalse(MarawTharProjectileGeometry.withinSphereRadius(
                0.0D, 0.0D, 0.0D, 0.8D, 0.8D, 0.0D, 1.0D));
    }

    @Test
    void exactRadiusBoundaryIsWithinRadius() {
        assertTrue(MarawTharProjectileGeometry.withinSphereRadius(
                0.0D, 0.0D, 0.0D, 1.0D, 0.0D, 0.0D, 1.0D));
        assertTrue(MarawTharProjectileGeometry.withinSphereRadius(
                0.0D, 0.0D, 0.0D, 0.0D, -1.0D, 0.0D, 1.0D));
        assertTrue(MarawTharProjectileGeometry.withinSphereRadius(
                0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D));
    }

    @Test
    void beyondRadiusIsRejected() {
        assertFalse(MarawTharProjectileGeometry.withinSphereRadius(
                0.0D, 0.0D, 0.0D, 1.01D, 0.0D, 0.0D, 1.0D));
        assertFalse(MarawTharProjectileGeometry.withinSphereRadius(
                0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.1D, 1.0D));
    }

    @Test
    void verticalDistanceCountsTowardRadius() {
        assertTrue(MarawTharProjectileGeometry.withinSphereRadius(
                0.0D, 0.0D, 0.0D, 0.5D, 0.8D, 0.0D, 1.0D));
        assertFalse(MarawTharProjectileGeometry.withinSphereRadius(
                0.0D, 0.0D, 0.0D, 0.5D, 0.9D, 0.0D, 1.0D));
    }

    @Test
    void centerAndOffsetSamplesWork() {
        assertTrue(MarawTharProjectileGeometry.withinSphereRadius(
                10.0D, 51.5D, -3.0D, 10.0D, 51.5D, -3.0D, 1.0D));
        assertTrue(MarawTharProjectileGeometry.withinSphereRadius(
                10.0D, 51.5D, -3.0D, 10.6D, 51.5D, -3.0D, 1.0D));
        assertFalse(MarawTharProjectileGeometry.withinSphereRadius(
                10.0D, 51.5D, -3.0D, 10.0D, 51.5D, -4.1D, 1.0D));
    }
}