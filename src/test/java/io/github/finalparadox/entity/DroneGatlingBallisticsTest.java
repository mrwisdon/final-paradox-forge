package io.github.finalparadox.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DroneGatlingBallisticsTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void muzzlesAreExactlyPointThreeBlocksEitherSide() {
        var center = new DroneGatlingBallistics.Vector3(10.0D, 20.0D, 30.0D);
        var right = new DroneGatlingBallistics.Vector3(1.0D, 0.0D, 0.0D);
        var leftMuzzle = DroneGatlingBallistics.muzzle(center, right, false);
        var rightMuzzle = DroneGatlingBallistics.muzzle(center, right, true);

        assertEquals(9.7D, leftMuzzle.x(), EPSILON);
        assertEquals(10.3D, rightMuzzle.x(), EPSILON);
        assertEquals(0.6D, leftMuzzle.distanceTo(rightMuzzle), EPSILON);
    }

    @Test
    void baseRaysMeetAtTheCentralThirtyTwoBlockPoint() {
        var center = new DroneGatlingBallistics.Vector3(0.0D, 5.0D, 0.0D);
        var aim = new DroneGatlingBallistics.Vector3(0.0D, 0.0D, -1.0D);
        var right = new DroneGatlingBallistics.Vector3(1.0D, 0.0D, 0.0D);
        var convergence = DroneGatlingBallistics.convergencePoint(center, aim);

        for (boolean rightGun : new boolean[]{false, true}) {
            var muzzle = DroneGatlingBallistics.muzzle(center, right, rightGun);
            var direction = DroneGatlingBallistics.convergingDirection(muzzle, convergence);
            var reached = muzzle.add(direction.scale(muzzle.distanceTo(convergence)));
            assertEquals(0.0D, reached.distanceTo(convergence), EPSILON);

            double inwardDegrees = Math.toDegrees(Math.acos(direction.dot(aim)));
            assertTrue(Math.abs(inwardDegrees - 0.537D) < 0.01D);
        }
    }

    @Test
    void spreadGrowsLinearlyFromQuarterToPointEightFiveDegrees() {
        assertEquals(0.25D, DroneGatlingBallistics.spreadDegrees(0), EPSILON);
        assertEquals(0.55D, DroneGatlingBallistics.spreadDegrees(60), EPSILON);
        assertEquals(0.85D, DroneGatlingBallistics.spreadDegrees(120), EPSILON);
    }

    @Test
    void diskSamplingUsesSquareRootRadiusAndStaysWithinCone() {
        var forward = new DroneGatlingBallistics.Vector3(0.0D, 0.0D, -1.0D);
        var halfRadius = DroneGatlingBallistics.spreadDirection(
                forward, 0.85D, 0.25D, 0.0D);
        double halfRadiusDegrees = Math.toDegrees(Math.acos(forward.dot(halfRadius)));
        assertEquals(0.425D, halfRadiusDegrees, 1.0E-7D);

        for (double radial : new double[]{0.0D, 0.1D, 0.5D, 1.0D}) {
            for (double angular : new double[]{0.0D, 0.25D, 0.5D, 0.75D}) {
                var sampled = DroneGatlingBallistics.spreadDirection(
                        forward, 0.85D, radial, angular);
                double degrees = Math.toDegrees(Math.acos(forward.dot(sampled)));
                assertTrue(degrees <= 0.8500001D);
                assertEquals(1.0D, sampled.length(), EPSILON);
            }
        }
    }
}
