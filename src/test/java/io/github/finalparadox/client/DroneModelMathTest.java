package io.github.finalparadox.client;

import io.github.finalparadox.entity.DroneGatlingCycle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void heatGlowZeroOrNegativeHasNoGlow() {
        float[] rgba = new float[4];
        DroneModelMath.heatGlowColor(0, rgba);
        assertEquals(0.0F, rgba[3], 1.0E-6F);
        DroneModelMath.heatGlowColor(-5, rgba);
        assertEquals(0.0F, rgba[3], 1.0E-6F);
    }

    @Test
    void heatGlowProgressesFromDarkRedToBrightOrangeRed() {
        float[] low = new float[4];
        float[] mid = new float[4];
        float[] max = new float[4];
        DroneModelMath.heatGlowColor(1, low);
        DroneModelMath.heatGlowColor(DroneGatlingCycle.MAX_HEAT / 2, mid);
        DroneModelMath.heatGlowColor(DroneGatlingCycle.MAX_HEAT, max);
        assertTrue(low[3] > 0.0F, "low positive heat must glow");
        assertTrue(low[0] < mid[0] && mid[0] < max[0], "red rises with heat");
        assertTrue(low[1] < mid[1] && mid[1] < max[1], "green rises with heat");
        assertTrue(low[3] < mid[3] && mid[3] < max[3], "alpha rises with heat");
        assertTrue(low[1] < low[0], "low heat is red-dominant");
    }

    @Test
    void heatGlowClampsAboveMaximum() {
        float[] atMax = new float[4];
        float[] above = new float[4];
        DroneModelMath.heatGlowColor(DroneGatlingCycle.MAX_HEAT, atMax);
        DroneModelMath.heatGlowColor(DroneGatlingCycle.MAX_HEAT * 5, above);
        for (int i = 0; i < 4; i++) {
            assertEquals(atMax[i], above[i], 1.0E-6F);
        }
    }

    @Test
    void heatGlowStaysInValidRgbaBounds() {
        for (int heat : new int[]{0, 1, 30, 60, DroneGatlingCycle.MAX_HEAT, 500}) {
            float[] rgba = new float[4];
            DroneModelMath.heatGlowColor(heat, rgba);
            for (int i = 0; i < 4; i++) {
                assertTrue(rgba[i] >= 0.0F && rgba[i] <= 1.0F,
                        "component " + i + " in bounds for heat " + heat);
            }
        }
    }

}
