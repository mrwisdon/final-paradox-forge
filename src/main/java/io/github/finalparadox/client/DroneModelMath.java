package io.github.finalparadox.client;

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

}
