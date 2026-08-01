package io.github.finalparadox.entity;

import net.minecraft.util.Mth;

import java.util.function.IntToDoubleFunction;

/**
 * Literal timing and armor-stand pose data from B9 h3/combo1.
 */
public final class MarawTharH3Frames {
    private static final int[] DURATIONS = {12, 2, 2, 1, 12, 1, 1, 5, 1, 1, 20};
    private static final float[] LEG_Z_LEFT =
            {-10.0F, -10.0F, -10.0F, -10.0F, -10.0F, -10.0F, -5.0F, -5.0F, 0.0F, 0.0F, -20.0F};
    private static final float[] LEG_Z_RIGHT =
            {10.0F, 10.0F, 10.0F, 10.0F, 10.0F, 10.0F, 5.0F, 5.0F, 0.0F, 0.0F, 20.0F};
    private static final float[] ARM_X =
            {-60.0F, -70.0F, -120.0F, -125.0F, -130.0F, -135.0F,
                    -140.0F, -150.0F, -120.0F, -100.0F, -100.0F};
    private static final float[] BODY_UP =
            {1.4F, 1.4F, 1.4F, 1.4F, 1.4F, 1.4F, 1.45F, 1.6F, 0.7F, -0.1F, 0.0F};
    private static final float[] SWORD_UP =
            {1.0F, 1.1F, 1.4F, 1.55F, 1.6F, 1.65F, 1.85F, 2.0F, 0.7F, -0.1F, 0.0F};

    private MarawTharH3Frames() {
    }

    public static int frameIndex(int score) {
        int clamped = Mth.clamp(score, 1, 58);
        int cursor = 0;
        for (int index = 0; index < DURATIONS.length; index++) {
            cursor += DURATIONS[index];
            if (clamped <= cursor) {
                return index;
            }
        }
        return DURATIONS.length - 1;
    }

    public static float leftLegZ(int score) {
        return LEG_Z_LEFT[frameIndex(score)];
    }

    public static float leftLegZ(float score) {
        return interpolate(score, tick -> leftLegZ(tick));
    }

    public static float rightLegZ(int score) {
        return LEG_Z_RIGHT[frameIndex(score)];
    }

    public static float rightLegZ(float score) {
        return interpolate(score, tick -> rightLegZ(tick));
    }

    public static float armX(int score) {
        return ARM_X[frameIndex(score)];
    }

    public static float armX(float score) {
        return interpolate(score, tick -> armX(tick));
    }

    public static float bodyUp(int score) {
        return BODY_UP[frameIndex(score)] + globalUp(score)
                - judgmentPlatformCompensation(score);
    }

    public static float bodyUp(float score) {
        return interpolate(score, tick -> bodyUp(tick));
    }

    public static float swordUp(int score) {
        return SWORD_UP[frameIndex(score)] + globalUp(score)
                - judgmentPlatformCompensation(score);
    }

    public static float swordUp(float score) {
        return interpolate(score, tick -> swordUp(tick));
    }

    private static float interpolate(float score, IntToDoubleFunction sampler) {
        int fromTick = Mth.floor(score);
        float delta = Mth.clamp(score - fromTick, 0.0F, 1.0F);
        return (float) Mth.lerp(
                delta, sampler.applyAsDouble(fromTick), sampler.applyAsDouble(fromTick + 1));
    }

    /**
     * During the darkness charge MarawThar is physically six blocks above the arena
     * on the judgment platform. Remove that platform height from the original
     * airborne frames until the entity switches to the burst center at score 35.
     */
    private static float judgmentPlatformCompensation(int score) {
        return score >= 5 && score < 35 ? 6.0F : 0.0F;
    }

    private static float globalUp(int score) {
        float result = score >= 5 ? 5.25F : 0.0F;
        if (score >= 7) {
            result += (Math.min(score, 35) - 6) * 0.025F;
        }
        if (score >= 30) {
            result += 0.5F;
        }
        if (score >= 36) {
            result -= 6.6F;
        }
        return result;
    }
}
