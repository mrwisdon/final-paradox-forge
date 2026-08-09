package io.github.finalparadox.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class DroneModelMathTest {
    private static final double EPS = 1.0E-6D;

    @Test
    void modelYawRotationAlignsNoseWithEntityForward() {
        for (float yaw : new float[]{0.0F, 90.0F, -90.0F, 180.0F}) {
            double rotation = Math.toRadians(DroneModelMath.modelYawDegrees(yaw));
            // Model-local nose (0,0,-1) rotated around +Y.
            double noseX = -Math.sin(rotation);
            double noseZ = -Math.cos(rotation);
            // Entity forward at yaw: (-sin, 0, cos).
            double yawRad = Math.toRadians(yaw);
            assertEquals(-Math.sin(yawRad), noseX, EPS);
            assertEquals(Math.cos(yawRad), noseZ, EPS);
        }
    }

    @Test
    void modelYawMapsAllFourCardinalYaws() {
        assertEquals(180.0F, DroneModelMath.modelYawDegrees(0.0F), 1.0E-4F);
        assertEquals(90.0F, DroneModelMath.modelYawDegrees(90.0F), 1.0E-4F);
        assertEquals(270.0F, DroneModelMath.modelYawDegrees(-90.0F), 1.0E-4F);
        assertEquals(0.0F, DroneModelMath.modelYawDegrees(180.0F), 1.0E-4F);
    }

    @Test
    void modelPitchNegatesEntityPitchInRadians() {
        assertEquals(Math.PI / 6.0, DroneModelMath.modelPitchRadians(-30.0F), EPS);
        assertEquals(0.0, DroneModelMath.modelPitchRadians(0.0F), EPS);
        assertEquals(-Math.PI / 6.0, DroneModelMath.modelPitchRadians(30.0F), EPS);
    }

    @Test
    void modelPitchClampsToThreeQuarterRadians() {
        assertEquals(0.75, DroneModelMath.modelPitchRadians(-90.0F), EPS);
        assertEquals(-0.75, DroneModelMath.modelPitchRadians(90.0F), EPS);
    }

}
