package io.github.finalparadox.entity;

import net.minecraft.util.Mth;

import java.util.function.IntToDoubleFunction;

/**
 * Literal B9 h4/combo1 and combo2 body/sword frame data.
 */
public final class MarawTharH4Frames {
    private static final int[] RAISE_DURATIONS =
            {5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3};
    private static final int[] LOWER_DURATIONS =
            {2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5};
    private static final float[] RAISE_LEFT =
            {0.85F, 0.85F, 0.85F, 0.85F, 0.85F, 0.85F, 0.8F,
                    0.7F, 0.5F, 0.45F, 0.45F, 0.45F, 0.45F};
    private static final float[] RAISE_UP =
            {-0.3F, -0.3F, -0.3F, -0.3F, -0.3F, -0.1F, 0.1F,
                    0.4F, 0.7F, 0.8F, 0.8F, 0.8F, 0.8F};
    private static final float[] RAISE_PITCH =
            {-100.0F, -99.0F, -97.0F, -95.0F, -90.0F, -80.0F, -70.0F,
                    -40.0F, -10.0F, 2.0F, 5.0F, 8.0F, 10.0F};

    private MarawTharH4Frames() {
    }

    public static int bodyFrame(int combatTick) {
        if (combatTick <= 9 || combatTick >= 108) {
            return 0;
        }
        if (combatTick == 10 || combatTick == 107) {
            return 1;
        }
        if (combatTick == 11 || combatTick == 106) {
            return 2;
        }
        if (combatTick == 12 || combatTick == 105) {
            return 3;
        }
        if (combatTick == 13 || combatTick == 104) {
            return 4;
        }
        return 5;
    }

    public static float headX(int combatTick) {
        return new float[]{10.0F, 8.0F, 6.0F, -10.0F, -20.0F, -40.0F}
                [bodyFrame(combatTick)];
    }

    public static float headX(float combatTick) {
        return interpolate(combatTick, tick -> headX(tick));
    }

    public static float headY(int combatTick) {
        return new float[]{0.0F, -2.0F, -4.0F, -6.0F, -10.0F, -20.0F}
                [bodyFrame(combatTick)];
    }

    public static float headY(float combatTick) {
        return interpolate(combatTick, tick -> headY(tick));
    }

    public static float leftArmZ(int combatTick) {
        return new float[]{-60.0F, -70.0F, -110.0F, -130.0F, -150.0F, -157.0F}
                [bodyFrame(combatTick)];
    }

    public static float leftArmZ(float combatTick) {
        return interpolate(combatTick, tick -> leftArmZ(tick));
    }

    public static float rightArmZ(int combatTick) {
        return new float[]{20.0F, 20.0F, 20.0F, 23.0F, 26.0F, 30.0F}
                [bodyFrame(combatTick)];
    }

    public static float rightArmZ(float combatTick) {
        return interpolate(combatTick, tick -> rightArmZ(tick));
    }

    public static boolean swordVisible(int combatTick) {
        return combatTick >= 1 && combatTick <= 115;
    }

    public static float swordLeft(int combatTick) {
        return RAISE_LEFT[swordFrame(combatTick)];
    }

    public static float swordLeft(float combatTick) {
        return interpolate(combatTick, tick -> swordLeft(tick));
    }

    public static float swordUp(int combatTick) {
        return RAISE_UP[swordFrame(combatTick)];
    }

    public static float swordUp(float combatTick) {
        return interpolate(combatTick, tick -> swordUp(tick));
    }

    public static float swordForward() {
        return 0.1F;
    }

    public static float swordPitch(int combatTick) {
        return RAISE_PITCH[swordFrame(combatTick)];
    }

    public static float swordPitch(float combatTick) {
        return interpolate(combatTick, tick -> swordPitch(tick));
    }

    private static float interpolate(float score, IntToDoubleFunction sampler) {
        int fromTick = Mth.floor(score);
        float delta = Mth.clamp(score - fromTick, 0.0F, 1.0F);
        return (float) Mth.lerp(
                delta, sampler.applyAsDouble(fromTick), sampler.applyAsDouble(fromTick + 1));
    }

    private static int swordFrame(int combatTick) {
        if (combatTick <= 18) {
            return timedFrame(combatTick, RAISE_DURATIONS);
        }
        if (combatTick <= 98) {
            return RAISE_LEFT.length - 1;
        }
        int lowerScore = combatTick - 98;
        int lowerFrame = timedFrame(lowerScore, LOWER_DURATIONS);
        return RAISE_LEFT.length - 1 - lowerFrame;
    }

    private static int timedFrame(int score, int[] durations) {
        int cursor = 0;
        for (int index = 0; index < durations.length; index++) {
            cursor += durations[index];
            if (score <= cursor) {
                return index;
            }
        }
        return durations.length - 1;
    }
}
