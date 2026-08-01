package io.github.finalparadox.client;

import net.minecraft.util.Mth;

/**
 * The 29 sword-core frames from B9 h1/combo1/gen_espada_frame.mcfunction.
 * Source frame holds total exactly 38 ticks, matching the combo scoreboard.
 */
final class NightfallComboFrames {
    private static final Frame[] FRAMES = {
            new Frame( 0.50F,  0.25F, -0.30F, 300.0F,  290.0F),
            new Frame( 0.50F,  0.25F, -0.30F, 293.0F,  290.0F),
            new Frame( 0.50F,  0.25F, -0.30F, 290.0F,  290.0F),
            new Frame( 0.70F, -0.40F, -0.30F, 320.0F,  290.0F),
            new Frame( 0.60F, -0.40F,  0.00F,   0.0F,  285.0F),
            new Frame( 0.50F, -0.40F,  0.70F, 100.0F,  280.0F),
            new Frame(-0.80F, -0.60F,  0.70F, 200.0F,  280.0F),
            new Frame(-1.20F, -0.40F,  0.70F, 300.0F,  290.0F),
            new Frame(-0.70F,  0.80F, -0.30F,  35.0F,  315.0F),
            new Frame(-0.70F,  0.85F, -0.30F,  40.0F,  310.0F),
            new Frame(-0.70F,  0.90F, -0.30F,  42.0F,  307.0F),
            new Frame(-0.70F,  0.95F, -0.30F,  43.0F,  306.0F),
            new Frame(-0.90F, -0.20F,  0.25F, 330.0F,  270.0F),
            new Frame(-0.90F, -0.40F,  0.25F, 300.0F,  240.0F),
            new Frame( 0.00F, -0.35F,  1.10F, 200.0F,  255.0F),
            new Frame( 1.50F,  0.80F,  0.25F, 120.0F,  290.0F),
            new Frame( 1.70F,  1.00F,  0.25F,  80.0F,    0.0F),
            new Frame( 1.50F,  1.65F,  0.25F,  80.0F,   10.0F),
            new Frame( 0.20F,  1.98F,  0.25F, 100.0F,   50.0F),
            new Frame( 0.20F,  1.98F,  0.25F, 140.0F,   50.0F),
            new Frame( 0.20F,  1.98F,  0.25F, 175.0F,   50.0F),
            new Frame( 0.20F,  1.98F,  0.25F, 190.0F,   50.0F),
            new Frame( 0.20F,  1.98F,  0.25F, 200.0F,   50.0F),
            new Frame( 0.00F,  0.80F,  1.25F, 204.0F,   30.0F),
            new Frame( 0.00F,  0.20F,  1.25F, 205.0F,  -30.0F),
            new Frame( 0.00F, -0.20F,  1.00F, 180.0F, -100.0F),
            new Frame( 0.00F, -0.70F,  1.00F, 178.0F, -103.0F),
            new Frame( 0.00F, -0.77F,  1.00F, 176.0F, -105.0F),
            new Frame( 0.00F, -0.80F,  1.00F, 175.0F, -105.0F)
    };

    private static final int[] HOLDS = {
            7, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4
    };

    private NightfallComboFrames() {
    }

    static Frame sample(int score, float partialTick) {
        float time = Math.max(0.0F, score - 1.0F + partialTick);
        float cursor = 0.0F;
        for (int index = 0; index < FRAMES.length; index++) {
            float end = cursor + HOLDS[index];
            if (time < end || index == FRAMES.length - 1) {
                Frame current = FRAMES[index];
                Frame next = FRAMES[Math.min(index + 1, FRAMES.length - 1)];
                float blend = MarawTharFrameInterpolation.blend(
                        time - cursor, HOLDS[index]);
                return lerp(current, next, blend);
            }
            cursor = end;
        }
        return FRAMES[FRAMES.length - 1];
    }

    static Frame sourceFrame(int index) {
        return FRAMES[Mth.clamp(index, 0, FRAMES.length - 1)];
    }

    static Frame lerp(Frame from, Frame to, float delta) {
        return new Frame(
                Mth.lerp(delta, from.left, to.left),
                Mth.lerp(delta, from.up, to.up),
                Mth.lerp(delta, from.forward, to.forward),
                from.yaw + Mth.wrapDegrees(to.yaw - from.yaw) * delta,
                from.pitch + Mth.wrapDegrees(to.pitch - from.pitch) * delta
        );
    }

    record Frame(float left, float up, float forward, float yaw, float pitch) {
    }
}
