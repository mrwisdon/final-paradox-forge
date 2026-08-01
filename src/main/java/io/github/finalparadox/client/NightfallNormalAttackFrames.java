package io.github.finalparadox.client;

import net.minecraft.util.Mth;

/**
 * Three self-contained ordinary-attack clips derived from the three source
 * sections of B9 h1/combo1. Source rotations are preserved while positional
 * travel is reduced around Nightfall's held idle pose so a normal swing does
 * not inherit the boss animation's armor-stand-sized displacement.
 */
final class NightfallNormalAttackFrames {
    static final int ATTACK_COUNT = 3;
    static final NightfallComboFrames.Frame IDLE_FRAME =
            new NightfallComboFrames.Frame(-0.75F, -0.50F, 0.10F, 20.0F, 108.0F);

    private static final float POSITION_SCALE = 0.55F;
    private static final float CHAIN_TRANSITION_END = 0.16F;
    private static final float WRAP_TRANSITION_END = 0.24F;
    private static final int[][] SOURCE_INDICES = {
            {0, 1, 2, 3, 4, 5, 6, 7},
            {8, 9, 10, 11, 12, 13, 14, 15},
            {16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28}
    };
    private static final float[][] SOURCE_TIMES = {
            {0.16F, 0.22F, 0.28F, 0.34F, 0.40F, 0.47F, 0.54F, 0.62F},
            {0.16F, 0.22F, 0.28F, 0.34F, 0.40F, 0.47F, 0.54F, 0.62F},
            {0.14F, 0.19F, 0.24F, 0.29F, 0.33F, 0.37F, 0.41F,
                    0.47F, 0.53F, 0.59F, 0.65F, 0.71F, 0.77F}
    };
    private static final NightfallComboFrames.Frame[][] WORLD_TRACKS = buildWorldTracks();

    private static final FirstPersonPose[][] FIRST_PERSON_TRACKS = {
            {
                    new FirstPersonPose(0.00F,  0.00F,  0.00F,  0.00F,   0.0F,   0.0F,   0.0F),
                    new FirstPersonPose(0.16F,  0.14F, -0.05F,  0.08F, -18.0F,  16.0F, -38.0F),
                    new FirstPersonPose(0.42F,  0.06F,  0.00F,  0.01F,  -6.0F,  -6.0F, -12.0F),
                    new FirstPersonPose(0.58F, -0.24F,  0.11F, -0.10F,  34.0F, -24.0F,  56.0F),
                    new FirstPersonPose(0.72F, -0.15F,  0.08F, -0.07F,  24.0F, -18.0F,  38.0F),
                    new FirstPersonPose(1.00F,  0.00F,  0.00F,  0.00F,   0.0F,   0.0F,   0.0F)
            },
            {
                    new FirstPersonPose(0.00F,  0.00F,  0.00F,  0.00F,   0.0F,   0.0F,   0.0F),
                    new FirstPersonPose(0.16F, -0.15F, -0.05F,  0.08F,  20.0F,  18.0F,  40.0F),
                    new FirstPersonPose(0.42F, -0.06F,  0.00F,  0.01F,   7.0F,  -5.0F,  13.0F),
                    new FirstPersonPose(0.58F,  0.23F,  0.10F, -0.10F, -32.0F, -22.0F, -54.0F),
                    new FirstPersonPose(0.72F,  0.14F,  0.07F, -0.07F, -23.0F, -16.0F, -36.0F),
                    new FirstPersonPose(1.00F,  0.00F,  0.00F,  0.00F,   0.0F,   0.0F,   0.0F)
            },
            {
                    new FirstPersonPose(0.00F, 0.00F,  0.00F,  0.00F,  0.0F,   0.0F,  0.0F),
                    new FirstPersonPose(0.28F, 0.02F, -0.17F,  0.10F, -4.0F,  72.0F, -8.0F),
                    new FirstPersonPose(0.36F, 0.02F, -0.17F,  0.10F, -4.0F,  72.0F, -8.0F),
                    new FirstPersonPose(0.58F, 0.00F,  0.20F, -0.14F,  2.0F, -64.0F, 10.0F),
                    new FirstPersonPose(0.72F, 0.00F,  0.15F, -0.11F,  2.0F, -48.0F,  8.0F),
                    new FirstPersonPose(1.00F, 0.00F,  0.00F,  0.00F,  0.0F,   0.0F,  0.0F)
            }
    };

    private NightfallNormalAttackFrames() {
    }

    static NightfallComboFrames.Frame sample(int attackIndex, float attackProgress) {
        int clip = Mth.clamp(attackIndex, 0, ATTACK_COUNT - 1);
        float progress = Mth.clamp(attackProgress, 0.0F, 1.0F);
        return sampleWorldTrack(clip, progress);
    }

    static NightfallComboFrames.Frame sample(int attackIndex, float attackProgress,
                                              int chainFromAttack,
                                              float chainFromProgress) {
        int clip = Mth.clamp(attackIndex, 0, ATTACK_COUNT - 1);
        float progress = Mth.clamp(attackProgress, 0.0F, 1.0F);
        if (chainFromAttack < 0 || chainFromProgress < 0.0F) {
            return sampleWorldTrack(clip, progress);
        }

        int previousClip = Mth.clamp(chainFromAttack, 0, ATTACK_COUNT - 1);
        float transitionEnd = transitionEnd(previousClip, clip);
        float sourceStart = SOURCE_TIMES[clip][0];
        if (progress <= transitionEnd) {
            NightfallComboFrames.Frame from =
                    sampleWorldTrack(previousClip, chainFromProgress);
            NightfallComboFrames.Frame to = WORLD_TRACKS[clip][0];
            return NightfallComboFrames.lerp(
                    from, to, smooth(progress / transitionEnd));
        }
        return sampleWorldTrack(
                clip, remapAfterTransition(progress, transitionEnd, sourceStart));
    }

    private static NightfallComboFrames.Frame sampleWorldTrack(int clip, float progress) {
        NightfallComboFrames.Frame[] frames = WORLD_TRACKS[clip];
        float[] times = SOURCE_TIMES[clip];

        if (progress <= times[0]) {
            return NightfallComboFrames.lerp(
                    IDLE_FRAME, frames[0], smooth(progress / times[0]));
        }
        for (int index = 0; index < times.length - 1; index++) {
            if (progress <= times[index + 1]) {
                float linear = (progress - times[index]) / (times[index + 1] - times[index]);
                float blend = isStrikeSegment(clip, index) ? directStrike(linear) : smooth(linear);
                return NightfallComboFrames.lerp(frames[index], frames[index + 1], blend);
            }
        }
        float recovery = (progress - times[times.length - 1])
                / (1.0F - times[times.length - 1]);
        return NightfallComboFrames.lerp(
                frames[frames.length - 1], IDLE_FRAME, smooth(recovery));
    }

    static FirstPersonPose sampleFirstPerson(int attackIndex, float attackProgress) {
        int clip = Mth.clamp(attackIndex, 0, ATTACK_COUNT - 1);
        float progress = Mth.clamp(attackProgress, 0.0F, 1.0F);
        return sampleFirstPersonTrack(clip, progress);
    }

    static FirstPersonPose sampleFirstPerson(int attackIndex, float attackProgress,
                                             int chainFromAttack,
                                             float chainFromProgress) {
        int clip = Mth.clamp(attackIndex, 0, ATTACK_COUNT - 1);
        float progress = Mth.clamp(attackProgress, 0.0F, 1.0F);
        if (chainFromAttack < 0 || chainFromProgress < 0.0F) {
            return sampleFirstPersonTrack(clip, progress);
        }

        int previousClip = Mth.clamp(chainFromAttack, 0, ATTACK_COUNT - 1);
        float transitionEnd = transitionEnd(previousClip, clip);
        FirstPersonPose[] targetTrack = FIRST_PERSON_TRACKS[clip];
        FirstPersonPose target = targetTrack[1];
        if (progress <= transitionEnd) {
            FirstPersonPose from =
                    sampleFirstPersonTrack(previousClip, chainFromProgress);
            return FirstPersonPose.lerp(
                    from, target, smooth(progress / transitionEnd));
        }
        return sampleFirstPersonTrack(
                clip, remapAfterTransition(progress, transitionEnd, target.time()));
    }

    private static FirstPersonPose sampleFirstPersonTrack(int clip, float progress) {
        FirstPersonPose[] poses = FIRST_PERSON_TRACKS[clip];
        for (int index = 0; index < poses.length - 1; index++) {
            FirstPersonPose next = poses[index + 1];
            if (progress <= next.time()) {
                FirstPersonPose current = poses[index];
                float linear = (progress - current.time()) / (next.time() - current.time());
                float blend = index == 2 ? directStrike(linear) : smooth(linear);
                return FirstPersonPose.lerp(current, next, blend);
            }
        }
        return poses[poses.length - 1];
    }

    private static float transitionEnd(int previousClip, int clip) {
        return previousClip == ATTACK_COUNT - 1 && clip == 0
                ? WRAP_TRANSITION_END
                : CHAIN_TRANSITION_END;
    }

    private static float remapAfterTransition(float progress, float transitionEnd,
                                              float sourceStart) {
        float remaining = (progress - transitionEnd) / (1.0F - transitionEnd);
        return Mth.lerp(Mth.clamp(remaining, 0.0F, 1.0F), sourceStart, 1.0F);
    }

    private static NightfallComboFrames.Frame[][] buildWorldTracks() {
        NightfallComboFrames.Frame[][] tracks =
                new NightfallComboFrames.Frame[SOURCE_INDICES.length][];
        for (int clip = 0; clip < SOURCE_INDICES.length; clip++) {
            if (SOURCE_INDICES[clip].length != SOURCE_TIMES[clip].length) {
                throw new IllegalStateException("Nightfall normal-attack frame/time mismatch");
            }
            tracks[clip] = new NightfallComboFrames.Frame[SOURCE_INDICES[clip].length];
            for (int frame = 0; frame < SOURCE_INDICES[clip].length; frame++) {
                tracks[clip][frame] = remapPosition(
                        NightfallComboFrames.sourceFrame(SOURCE_INDICES[clip][frame]));
            }
        }
        return tracks;
    }

    private static NightfallComboFrames.Frame remapPosition(
            NightfallComboFrames.Frame source) {
        return new NightfallComboFrames.Frame(
                Mth.lerp(POSITION_SCALE, IDLE_FRAME.left(), source.left()),
                Mth.lerp(POSITION_SCALE, IDLE_FRAME.up(), source.up()),
                Mth.lerp(POSITION_SCALE, IDLE_FRAME.forward(), source.forward()),
                source.yaw(),
                source.pitch()
        );
    }

    private static boolean isStrikeSegment(int clip, int sourceSegment) {
        return clip < 2 ? sourceSegment >= 4 : sourceSegment >= 6 && sourceSegment <= 9;
    }

    private static float smooth(float value) {
        float clamped = Mth.clamp(value, 0.0F, 1.0F);
        return clamped * clamped * (3.0F - 2.0F * clamped);
    }

    private static float directStrike(float value) {
        float clamped = Mth.clamp(value, 0.0F, 1.0F);
        return 1.0F - (1.0F - clamped) * (1.0F - clamped);
    }

    record FirstPersonPose(float time, float sideways, float down, float forward,
                           float yaw, float pitch, float roll) {
        private static FirstPersonPose lerp(FirstPersonPose from, FirstPersonPose to,
                                            float delta) {
            return new FirstPersonPose(
                    Mth.lerp(delta, from.time, to.time),
                    Mth.lerp(delta, from.sideways, to.sideways),
                    Mth.lerp(delta, from.down, to.down),
                    Mth.lerp(delta, from.forward, to.forward),
                    Mth.lerp(delta, from.yaw, to.yaw),
                    Mth.lerp(delta, from.pitch, to.pitch),
                    Mth.lerp(delta, from.roll, to.roll)
            );
        }
    }
}
