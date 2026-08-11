package io.github.finalparadox.client;

/**
 * Pure shake timing/offset rules for the Maraw'Thar victory statue visual.
 *
 * <p>Amplitudes and cadence match the source datapack
 * {@code run_marawthar_iddle_anim.mcfunction}: Z is +0.1 on odd statue ages and 0 on even;
 * Y is +0.1 for statue ages whose modulo-3 phase is 1 or 2 and 0 for phase 0. The source
 * schedules that function every tick and teleports the statue by those offsets, so each value
 * is an absolute offset relative to the statue spawn position, not an accumulating drift.
 *
 * <p>Petrification score is {@code floor(victoryTick) - PETRIFICATION_START_TICK} with the
 * start at 50*20 ticks, matching {@code MarawTharVictoryVisualRenderer}. The source
 * {@code petrificar_anim/frame_pack_1.mcfunction} clears the idle schedule at score 138, so
 * shake is exactly zero for every petrification score of 138 or above. That keeps the statue
 * still from victory tick 1138 onward through the fall at 59*20 and the end at 63*20.
 *
 * <p>The shake is applied as one root {@code PoseStack} translate in the renderer. The pose
 * stack reaches the statue render already positioned at the entity with no extra yaw rotation,
 * so translate({@code 0, y, z}) moves in the same world axes the source {@code tp} uses; no
 * axis correction is required.
 */
final class MarawTharVictoryVisualRules {
    static final int PETRIFICATION_SHAKE_CUTOFF_SCORE = 138;

    private MarawTharVictoryVisualRules() {
    }

    static boolean shakeActive(int petrificationTick) {
        return petrificationTick < PETRIFICATION_SHAKE_CUTOFF_SCORE;
    }

    static double shakeY(int statueAge, int petrificationTick, float partialTick) {
        if (!shakeActive(petrificationTick)) {
            return 0.0D;
        }
        return lerp(yOffsetAt(statueAge), yOffsetAt(statueAge + 1), partialTick);
    }

    static double shakeZ(int statueAge, int petrificationTick, float partialTick) {
        if (!shakeActive(petrificationTick)) {
            return 0.0D;
        }
        return lerp(zOffsetAt(statueAge), zOffsetAt(statueAge + 1), partialTick);
    }

    static double yOffsetAt(int statueAge) {
        int phase = Math.floorMod(statueAge, 3);
        return phase == 1 || phase == 2 ? 0.1D : 0.0D;
    }

    static double zOffsetAt(int statueAge) {
        return (statueAge & 1) == 1 ? 0.1D : 0.0D;
    }

    private static double lerp(double start, double end, float delta) {
        return start + (end - start) * delta;
    }
}
