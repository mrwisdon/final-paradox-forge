package io.github.finalparadox.client;

import net.minecraft.util.Mth;

/**
 * Keeps long anticipation holds while spreading their release over a short
 * render-time transition instead of moving the whole pose in one tick.
 */
final class MarawTharFrameInterpolation {
    private static final int MAX_TRANSITION_TICKS = 3;

    private MarawTharFrameInterpolation() {
    }

    static float blend(float timeInFrame, int holdTicks) {
        int transitionTicks = Math.min(MAX_TRANSITION_TICKS, Math.max(1, holdTicks));
        float transitionStart = holdTicks - transitionTicks;
        float linear = Mth.clamp(
                (timeInFrame - transitionStart) / transitionTicks, 0.0F, 1.0F);
        return linear * linear * (3.0F - 2.0F * linear);
    }
}
