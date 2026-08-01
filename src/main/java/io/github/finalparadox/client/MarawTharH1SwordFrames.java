package io.github.finalparadox.client;

import io.github.finalparadox.entity.MarawTharBossEntity;
import net.minecraft.util.Mth;

/**
 * Sword-core tracks extracted from B9 h1 combo2-5 gen_espada_frame.
 *
 * <p>Each source frame lives for one tick plus its b4_espada_cd value. Sampling
 * interpolates between source frames so the armor-stand animation remains
 * recognizable without reproducing its one-tick stepping.</p>
 */
final class MarawTharH1SwordFrames {
    private static final Frame[] COMBO_2 = {
            new Frame(0F, 0.1F, 0.7F, 70F, 110F, 4),
            new Frame(0.1F, 0.3F, 0.8F, 40F, 110F, 1),
            new Frame(0.2F, 1.3F, 0.9F, 20F, 110F, 1),
            new Frame(0.3F, 1.4F, 0.9F, -10F, 120F, 1),
            new Frame(0.3F, 1.4F, 0.8F, -10F, 140F, 1),
            new Frame(0.3F, 1.6F, -1.2F, -11F, 240F, 1),
            new Frame(0.3F, 1.7F, -1.2F, -12F, 250F, 1),
            new Frame(0.3F, 1.8F, -1.2F, -13F, 260F, 1),
            new Frame(0.3F, 1.9F, -1.2F, -14F, 270F, 1),
            new Frame(0.3F, 2.7F, -0.5F, -15F, 330F, 1),
            new Frame(0.3F, 1F, 0.5F, -10F, 60F, 1),
            new Frame(0.5F, -0.6F, -0.8F, -230F, 80F, 1),
            new Frame(0.5F, -0.6F, -0.8F, -240F, 60F, 1),
            new Frame(0.5F, -0.6F, -0.8F, -245F, 55F, 1),
            new Frame(0.5F, -0.6F, -0.8F, -248F, 50F, 3),
            new Frame(0.3F, -0.8F, 0.7F, -29F, 105F, 1),
            new Frame(-0.4F, 0F, 0.4F, -10F, 110F, 1),
            new Frame(-0.8F, -0.1F, 0.4F, 40F, 115F, 3),
            new Frame(-0.5F, 0.4F, -0.9F, 160F, 100F, 1),
            new Frame(0.9F, 0.6F, -0.4F, 260F, 60F, 1),
            new Frame(0.9F, 0.7F, 0.5F, 310F, 70F, 1),
            new Frame(-0.9F, -0.7F, 0.1F, 30F, 105F, 1),
            new Frame(-0.9F, -0.7F, 0.1F, 60F, 105F, 1),
            new Frame(-0.9F, -0.75F, 0.1F, 90F, 100F, 1),
            new Frame(-0.9F, -0.78F, 0.1F, 110F, 100F, 1),
            new Frame(-0.9F, -0.8F, 0.1F, 120F, 100F, 3)
    };

    private static final Frame[] COMBO_3 = {
            new Frame(-0.5F, -0.4F, -1F, 0F, -110F, 5),
            new Frame(-0.4F, -0.4F, -0.4F, 0F, -160F, 4),
            new Frame(-0.2F, 0.1F, 0.8F, 0F, -250F, 1),
            new Frame(-0.3F, 2.2F, 0.1F, 0F, -340F, 1),
            new Frame(-0.4F, 2.2F, -0.3F, 0F, -50F, 1),
            new Frame(-0.5F, 2F, -0.4F, 0F, -60F, 1),
            new Frame(-0.5F, 1.9F, -0.5F, 0F, -80F, 1),
            new Frame(-0.5F, 1.8F, -0.5F, 0F, -105F, 1),
            new Frame(-0.5F, 1.75F, -0.5F, 0F, -110F, 1),
            new Frame(-0.5F, 1.75F, -0.5F, 0F, -113F, 1),
            new Frame(-0.5F, 1.75F, -0.5F, 0F, -115F, 7)
    };

    private static final Frame[] COMBO_4 = {
            new Frame(-0.7F, 0.15F, -0.3F, 40F, 290F, 5),
            new Frame(-0.15F, 0.45F, -0.3F, 60F, 260F, 1),
            new Frame(-0.1F, 0.45F, -0.35F, 70F, 250F, 1),
            new Frame(-0.15F, 0.45F, -0.4F, 75F, 245F, 1),
            new Frame(0F, 0.45F, -0.45F, 80F, 240F, 3),
            new Frame(0F, 0.45F, -0.45F, 82F, 239F, 4),
            new Frame(-0.3F, 0.45F, -0.25F, 22F, 289F, 1),
            new Frame(0.1F, -0.2F, 0.45F, -110F, 289F, 1),
            new Frame(0.9F, -0.7F, 0.55F, -210F, 260F, 1),
            new Frame(0.6F, -0.7F, -0.75F, -370F, 275F, 1),
            new Frame(0.5F, -0.7F, -0.75F, -390F, 278F, 3),
            new Frame(0.5F, -0.7F, -0.75F, -400F, 280F, 7)
    };

    private static final Frame[] COMBO_5 = {
            new Frame(0.6F, 0.1F, -0.4F, 160F, 0F, 5),
            new Frame(0.5F, 0F, -0.4F, 140F, 20F, 3),
            new Frame(-0.2F, 0F, -0.4F, 70F, 70F, 3),
            new Frame(-0.4F, 0F, -0.4F, 30F, 95F, 3),
            new Frame(-0.55F, 0F, -0.4F, 0F, 98F, 1),
            new Frame(-0.6F, 0F, -0.4F, -8F, 100F, 1),
            new Frame(-0.6F, 0F, -0.4F, -12F, 100F, 1),
            new Frame(-0.6F, 0F, -0.4F, -14F, 100F, 5),
            new Frame(-0.6F, 0F, -0.4F, 0F, 100F, 3),
            new Frame(-0.6F, 0F, -0.4F, 20F, 100F, 1),
            new Frame(-0.5F, 0F, -0.4F, 40F, 100F, 1),
            new Frame(-0.3F, 0F, -0.4F, 80F, 100F, 1),
            new Frame(0.5F, 0F, -0.7F, 120F, 100F, 1),
            new Frame(-0.5F, -0.2F, 0.4F, 300F, 90F, 1),
            new Frame(-0.9F, -0.5F, -0.3F, 450F, 100F, 1),
            new Frame(-0.1F, -0.4F, -0.9F, 470F, 100F, 1),
            new Frame(0.5F, -0.4F, -0.6F, 530F, 110F, 1),
            new Frame(0.8F, -0.4F, -0.35F, 600F, 115F, 1),
            new Frame(0.8F, -0.4F, -0.2F, 625F, 115F, 1),
            new Frame(0.8F, -0.4F, -0.2F, 630F, 115F, 3),
            new Frame(0.8F, -0.4F, -0.2F, 633F, 115F, 7)
    };

    private MarawTharH1SwordFrames() {
    }

    static boolean supports(int animation) {
        return animation == MarawTharBossEntity.ANIMATION_H1_COMBO2
                || animation == MarawTharBossEntity.ANIMATION_H1_COMBO3
                || animation == MarawTharBossEntity.ANIMATION_H1_COMBO4
                || animation == MarawTharBossEntity.ANIMATION_BLUE_RUSH;
    }

    static Frame sample(int animation, int score, float partialTick) {
        Frame[] frames = framesFor(animation);
        float time = Math.max(0.0F, score - 1.0F + partialTick);
        float cursor = 0.0F;
        for (int index = 0; index < frames.length; index++) {
            Frame current = frames[index];
            float end = cursor + current.holdTicks;
            if (time < end || index == frames.length - 1) {
                Frame next = frames[Math.min(index + 1, frames.length - 1)];
                float blend = MarawTharFrameInterpolation.blend(
                        time - cursor, current.holdTicks);
                return lerp(current, next, blend);
            }
            cursor = end;
        }
        return frames[frames.length - 1];
    }

    private static Frame[] framesFor(int animation) {
        return switch (animation) {
            case MarawTharBossEntity.ANIMATION_H1_COMBO2 -> COMBO_2;
            case MarawTharBossEntity.ANIMATION_H1_COMBO3 -> COMBO_3;
            case MarawTharBossEntity.ANIMATION_H1_COMBO4 -> COMBO_4;
            default -> COMBO_5;
        };
    }

    private static Frame lerp(Frame from, Frame to, float delta) {
        return new Frame(
                Mth.lerp(delta, from.left, to.left),
                Mth.lerp(delta, from.up, to.up),
                Mth.lerp(delta, from.forward, to.forward),
                from.yaw + Mth.wrapDegrees(to.yaw - from.yaw) * delta,
                from.pitch + Mth.wrapDegrees(to.pitch - from.pitch) * delta,
                from.holdTicks);
    }

    record Frame(float left, float up, float forward,
                 float yaw, float pitch, int holdTicks) {
    }
}
