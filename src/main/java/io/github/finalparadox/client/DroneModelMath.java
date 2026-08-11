package io.github.finalparadox.client;

import io.github.finalparadox.entity.DroneGatlingCycle;

/**
 * Pure model math for the recon drone, kept free of Minecraft classes so it
 * can be unit-tested without a client environment.
 */
public final class DroneModelMath {
    private static final float MAX_PITCH_RADIANS = 0.75F;

    private DroneModelMath() {
    }

    /**
     * Maps an entity yaw (forward +z at 0) to the model Y rotation that aligns
     * the model-local -z nose/barrels with the entity forward direction.
     */
    public static float modelYawDegrees(float entityYawDegrees) {
        return 180.0F - entityYawDegrees;
    }

    /**
     * Maps an entity pitch in degrees to the model X rotation used by the
     * camera gimbal and both belly guns. The visual pitch is the negated
     * entity pitch, clamped to +-0.75 radians to preserve the designed
     * visual travel limit for the gimbals.
     */
    public static float modelPitchRadians(float entityPitchDegrees) {
        float radians = (float) Math.toRadians(-entityPitchDegrees);
        return Math.max(-MAX_PITCH_RADIANS, Math.min(MAX_PITCH_RADIANS, radians));
    }

    /**
     * Fills {@code out} (length at least 4) with the RGBA glow color for the
     * given synchronized Gatling heat. Zero or negative heat yields zero
     * alpha, so idle and warmup never glow. Heat above the cycle maximum is
     * clamped to the brightest value. The ramp goes from dark red at low
     * positive heat toward bright orange/red at maximum heat. Allocation-free:
     * callers reuse one {@code float[4]} across frames.
     */
    public static void heatGlowColor(int heat, float[] out) {
        float fraction = heat <= 0
                ? 0.0F
                : Math.min(1.0F, heat / (float) DroneGatlingCycle.MAX_HEAT);
        out[0] = 0.4F + 0.6F * fraction;
        out[1] = 0.1F + 0.65F * fraction;
        out[2] = 0.0F;
        out[3] = fraction;
    }

}
