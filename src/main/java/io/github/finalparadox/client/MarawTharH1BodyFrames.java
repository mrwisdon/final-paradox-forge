package io.github.finalparadox.client;

import io.github.finalparadox.entity.MarawTharBossEntity;
import net.minecraft.util.Mth;

/**
 * Literal body poses and hold durations from the five B9 h1 sword combos.
 * Regenerate with tools/reconstruction/generate_marawthar_h1_body_frames.ps1.
 */
final class MarawTharH1BodyFrames {
    private MarawTharH1BodyFrames() {}

    record Part(float x, float y, float z) {}
    record Frame(Part head, Part body, Part leftArm, Part rightArm,
                 Part leftLeg, Part rightLeg, int holdTicks) {}

    private static final Frame[] COMBO_1 = {
            new Frame(new Part(0.0F, 70F, 0.0F), new Part(0.0F, 6F, 0.0F), new Part(0.0F, 0.0F, 348F), new Part(-110F, -30F, 0.0F), new Part(-10F, 0.0F, 0.0F), new Part(10F, 0.0F, 0.0F), 7),
            new Frame(new Part(0.0F, 60F, 0.0F), new Part(0.0F, 6F, 0.0F), new Part(0.0F, 0.0F, 348F), new Part(-70F, -30F, 0.0F), new Part(-20F, 0.0F, 0.0F), new Part(20F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 40F, 0.0F), new Part(0.0F, 6F, 0.0F), new Part(0.0F, 0.0F, 348F), new Part(-70F, -30F, 0.0F), new Part(-30F, 0.0F, 0.0F), new Part(30F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, -70F, 0.0F), new Part(0.0F, 6F, 0.0F), new Part(0.0F, 0.0F, 348F), new Part(-80F, -30F, -45F), new Part(-35F, 0.0F, 0.0F), new Part(35F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, -65F, 0.0F), new Part(0.0F, 6F, -8F), new Part(-50F, 0.0F, 3F), new Part(-95F, -30F, -50F), new Part(-50F, 0.0F, 0.0F), new Part(10F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, -85F, 0.0F), new Part(-5F, 6F, -10F), new Part(-90F, 0.0F, 320F), new Part(-100F, -30F, -55F), new Part(-70F, 0.0F, 0.0F), new Part(20F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, -88F, 0.0F), new Part(-8F, 6F, -12F), new Part(-95F, 0.0F, 315F), new Part(-105F, -30F, -58F), new Part(-75F, 0.0F, 0.0F), new Part(25F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, -90F, 0.0F), new Part(-10F, 6F, -15F), new Part(-100F, 0.0F, 310F), new Part(-110F, -30F, -60F), new Part(-85F, 0.0F, 0.0F), new Part(30F, 0.0F, 0.0F), 5),
            new Frame(new Part(0.0F, 25F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(225F, 324F, 0.0F), new Part(90F, 250F, 0.0F), new Part(-80F, 0.0F, 0.0F), new Part(30F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 25F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(225F, 324F, 0.0F), new Part(90F, 250F, 0.0F), new Part(-70F, 0.0F, 0.0F), new Part(30F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 30F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(255F, 324F, 0.0F), new Part(60F, 300F, 0.0F), new Part(-25F, 0.0F, 0.0F), new Part(0.0F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 25F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(225F, 324F, 0.0F), new Part(90F, 250F, 0.0F), new Part(-55F, 0.0F, 0.0F), new Part(10F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 22F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(205F, 324F, 0.0F), new Part(120F, 240F, 0.0F), new Part(-90F, 0.0F, 0.0F), new Part(20F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 20F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(200F, 324F, 0.0F), new Part(130F, 240F, 0.0F), new Part(-115F, 0.0F, 0.0F), new Part(35F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 18F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(195F, 324F, 0.0F), new Part(140F, 250F, 0.0F), new Part(-120F, 0.0F, 0.0F), new Part(40F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 18F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(195F, 324F, 0.0F), new Part(150F, 255F, 1F), new Part(-125F, 0.0F, 0.0F), new Part(45F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 18F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(190F, 324F, 0.0F), new Part(160F, 260F, 6F), new Part(-110F, 0.0F, 0.0F), new Part(35F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 19F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(188F, 324F, 0.0F), new Part(162F, 262F, 6F), new Part(-85F, 0.0F, 0.0F), new Part(35F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 20F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(187F, 324F, 0.0F), new Part(163F, 263F, 6F), new Part(-50F, 0.0F, 0.0F), new Part(35F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 21F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(90F, 220F, 0.0F), new Part(90F, 141F, 0.0F), new Part(10F, 0.0F, 10F), new Part(-10F, 0.0F, -10F), 1),
            new Frame(new Part(12F, 6F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(90F, 220F, 0.0F), new Part(90F, 141F, 0.0F), new Part(20F, 0.0F, 10F), new Part(-20F, 0.0F, -10F), 1),
            new Frame(new Part(14F, 4F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(85F, 220F, 0.0F), new Part(85F, 141F, 0.0F), new Part(30F, 0.0F, 10F), new Part(-30F, 0.0F, -10F), 1),
            new Frame(new Part(16F, -2F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(80F, 220F, 0.0F), new Part(80F, 141F, 0.0F), new Part(40F, 0.0F, 20F), new Part(-40F, 0.0F, -20F), 1),
            new Frame(new Part(18F, -8F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(75F, 220F, 0.0F), new Part(75F, 141F, 0.0F), new Part(45F, 0.0F, 25F), new Part(-45F, 0.0F, -25F), 1),
            new Frame(new Part(20F, -10F, 0.0F), new Part(-10F, 6F, 0.0F), new Part(70F, 220F, 0.0F), new Part(70F, 141F, 0.0F), new Part(50F, 0.0F, 30F), new Part(-50F, 0.0F, -30F), 3),
    };

    private static final Frame[] COMBO_2 = {
            new Frame(new Part(32F, 0.0F, 30F), new Part(30F, 0.0F, 30F), new Part(100F, 0.0F, 0.0F), new Part(170F, 0.0F, 0.0F), new Part(0.0F, 0.0F, 0.0F), new Part(20F, 0.0F, 0.0F), 4),
            new Frame(new Part(62F, 0.0F, 30F), new Part(60F, 0.0F, 20F), new Part(100F, 0.0F, 0.0F), new Part(170F, 0.0F, 0.0F), new Part(80F, 0.0F, 0.0F), new Part(-30F, 0.0F, 0.0F), 1),
            new Frame(new Part(92F, 0.0F, 30F), new Part(90F, 0.0F, 20F), new Part(100F, 0.0F, 0.0F), new Part(170F, 0.0F, 0.0F), new Part(130F, 0.0F, 0.0F), new Part(-20F, 0.0F, 0.0F), 1),
            new Frame(new Part(92F, 0.0F, 30F), new Part(90F, 0.0F, 20F), new Part(100F, 0.0F, 0.0F), new Part(170F, 0.0F, 0.0F), new Part(135F, 0.0F, 0.0F), new Part(-15F, 0.0F, 0.0F), 1),
            new Frame(new Part(92F, 0.0F, -10F), new Part(90F, 0.0F, 10F), new Part(0.0F, 0.0F, 0.0F), new Part(160F, 0.0F, 0.0F), new Part(50F, 0.0F, 0.0F), new Part(230F, 0.0F, 0.0F), 1),
            new Frame(new Part(92F, 0.0F, -180F), new Part(90F, 0.0F, -40F), new Part(0.0F, 0.0F, 0.0F), new Part(150F, 0.0F, 0.0F), new Part(0.0F, 0.0F, 0.0F), new Part(170F, 0.0F, 0.0F), 1),
            new Frame(new Part(92F, 0.0F, -210F), new Part(90F, 0.0F, -100F), new Part(0.0F, 0.0F, 0.0F), new Part(140F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), new Part(150F, 0.0F, 0.0F), 1),
            new Frame(new Part(92F, 0.0F, -220F), new Part(90F, 0.0F, -190F), new Part(0.0F, 0.0F, 0.0F), new Part(130F, 0.0F, 0.0F), new Part(-20F, 0.0F, 0.0F), new Part(145F, 0.0F, 0.0F), 1),
            new Frame(new Part(92F, 5F, -230F), new Part(90F, 5F, -200F), new Part(0.0F, 0.0F, 0.0F), new Part(120F, 0.0F, 0.0F), new Part(-25F, 0.0F, 0.0F), new Part(140F, 0.0F, 0.0F), 1),
            new Frame(new Part(92F, 10F, -240F), new Part(90F, 10F, -220F), new Part(0.0F, 0.0F, 0.0F), new Part(110F, 0.0F, 0.0F), new Part(-28F, 0.0F, 0.0F), new Part(138F, 0.0F, 0.0F), 1),
            new Frame(new Part(92F, 20F, -300F), new Part(90F, 30F, -300F), new Part(-179F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(270F, 334F, 0.0F), new Part(36F, 330F, 0.0F), 1),
            new Frame(new Part(92F, 35F, -300F), new Part(90F, 45F, -300F), new Part(-179F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(300F, 334F, 0.0F), new Part(36F, 330F, 0.0F), 1),
            new Frame(new Part(92F, 40F, -297F), new Part(90F, 60F, -305F), new Part(-179F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(305F, 334F, 0.0F), new Part(36F, 330F, 0.0F), 1),
            new Frame(new Part(92F, 43F, -297F), new Part(90F, 63F, -308F), new Part(-179F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(307F, 334F, 0.0F), new Part(39F, 330F, 0.0F), 1),
            new Frame(new Part(92F, 45F, -295F), new Part(90F, 65F, -308F), new Part(180F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(310F, 334F, 0.0F), new Part(41F, 330F, 0.0F), 3),
            new Frame(new Part(92F, 95F, -295F), new Part(90F, 95F, -308F), new Part(180F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(310F, 334F, 0.0F), new Part(0.0F, 330F, 0.0F), 1),
            new Frame(new Part(92F, 110F, -295F), new Part(90F, 110F, -308F), new Part(180F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(300F, 330F, 0.0F), new Part(-10F, 330F, 0.0F), 1),
            new Frame(new Part(0.0F, 0.0F, 340F), new Part(0.0F, 0.0F, 340F), new Part(180F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(330F, 330F, 0.0F), new Part(0.0F, 330F, 0.0F), 3),
            new Frame(new Part(0.0F, 20F, 340F), new Part(0.0F, 20F, 340F), new Part(180F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(330F, 330F, 0.0F), new Part(0.0F, 330F, 0.0F), 1),
            new Frame(new Part(0.0F, 180F, 340F), new Part(0.0F, 180F, 340F), new Part(180F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(20F, 330F, 0.0F), new Part(0.0F, 330F, 0.0F), 1),
            new Frame(new Part(0.0F, 220F, 340F), new Part(0.0F, 220F, 340F), new Part(180F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(20F, 330F, 0.0F), new Part(0.0F, 330F, 0.0F), 1),
            new Frame(new Part(20F, -30F, 0.0F), new Part(0.0F, 385F, 340F), new Part(180F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(20F, 60F, 0.0F), new Part(320F, 50F, 0.0F), 1),
            new Frame(new Part(40F, 0.0F, 0.0F), new Part(30F, 30F, 0.0F), new Part(180F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(40F, 30F, 0.0F), new Part(320F, 20F, 0.0F), 1),
            new Frame(new Part(40F, -30F, 0.0F), new Part(30F, 30F, 0.0F), new Part(180F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(40F, 30F, 0.0F), new Part(320F, 20F, 0.0F), 1),
            new Frame(new Part(40F, -35F, 0.0F), new Part(30F, 30F, 0.0F), new Part(180F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(40F, 30F, 0.0F), new Part(320F, 20F, 0.0F), 1),
            new Frame(new Part(40F, -38F, 0.0F), new Part(30F, 30F, 0.0F), new Part(180F, 0.0F, 0.0F), new Part(179F, 0.0F, 0.0F), new Part(40F, 30F, 0.0F), new Part(320F, 20F, 0.0F), 3),
    };

    private static final Frame[] COMBO_3 = {
            new Frame(new Part(250F, 0.0F, 0.0F), new Part(250F, 0.0F, 0.0F), new Part(65F, 0.0F, 0.0F), new Part(24F, 0.0F, 0.0F), new Part(280F, 0.0F, 0.0F), new Part(320F, 0.0F, 0.0F), 5),
            new Frame(new Part(170F, 0.0F, 0.0F), new Part(170F, 0.0F, 0.0F), new Part(65F, 0.0F, 0.0F), new Part(24F, 0.0F, 0.0F), new Part(220F, 0.0F, 0.0F), new Part(260F, 0.0F, 0.0F), 4),
            new Frame(new Part(110F, 0.0F, 0.0F), new Part(97F, 0.0F, 0.0F), new Part(221F, 0.0F, 0.0F), new Part(290F, 0.0F, 0.0F), new Part(160F, 0.0F, 0.0F), new Part(200F, 0.0F, 0.0F), 1),
            new Frame(new Part(40F, 0.0F, 0.0F), new Part(40F, 0.0F, 0.0F), new Part(0.0F, 0.0F, 0.0F), new Part(210F, 0.0F, 0.0F), new Part(100F, 0.0F, 0.0F), new Part(140F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 0.0F, 0.0F), new Part(0.0F, 0.0F, 0.0F), new Part(281F, 22F, 0.0F), new Part(150F, 6F, 0.0F), new Part(40F, 0.0F, 0.0F), new Part(120F, 0.0F, 0.0F), 1),
            new Frame(new Part(-4F, 0.0F, 0.0F), new Part(-5F, 0.0F, 0.0F), new Part(280F, 22F, 0.0F), new Part(150F, -10F, 0.0F), new Part(20F, 0.0F, 0.0F), new Part(100F, 0.0F, 0.0F), 1),
            new Frame(new Part(-5F, 0.0F, 0.0F), new Part(-8F, 0.0F, 0.0F), new Part(280F, 22F, 0.0F), new Part(130F, -10F, 0.0F), new Part(10F, 0.0F, 0.0F), new Part(80F, 0.0F, 0.0F), 1),
            new Frame(new Part(-6F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), new Part(275F, 22F, 0.0F), new Part(125F, -10F, 0.0F), new Part(0.0F, 0.0F, 0.0F), new Part(70F, 0.0F, 0.0F), 1),
            new Frame(new Part(-7F, 0.0F, 0.0F), new Part(-12F, 0.0F, 0.0F), new Part(270F, 22F, 0.0F), new Part(123F, -10F, 0.0F), new Part(-2F, 0.0F, 0.0F), new Part(65F, 0.0F, 0.0F), 1),
            new Frame(new Part(-8F, 0.0F, 0.0F), new Part(-13F, 0.0F, 0.0F), new Part(270F, 22F, 0.0F), new Part(124F, -10F, 0.0F), new Part(-3F, 0.0F, 0.0F), new Part(60F, 0.0F, 0.0F), 1),
            new Frame(new Part(-9F, 0.0F, 0.0F), new Part(-14F, 0.0F, 0.0F), new Part(270F, 22F, 0.0F), new Part(125F, -10F, 0.0F), new Part(-4F, 0.0F, 0.0F), new Part(58F, 0.0F, 0.0F), 7),
    };

    private static final Frame[] COMBO_4 = {
            new Frame(new Part(0.0F, 305F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(318F, 318F, 0.0F), new Part(104F, 208F, 0.0F), new Part(0.0F, 0.0F, 0.0F), new Part(0.0F, 0.0F, 0.0F), 4),
            new Frame(new Part(0.0F, 305F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(288F, 318F, 0.0F), new Part(144F, 208F, 0.0F), new Part(330F, 0.0F, 0.0F), new Part(30F, 0.0F, 0.0F), 1),
            new Frame(new Part(-5F, 308F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(278F, 318F, 0.0F), new Part(190F, 208F, 0.0F), new Part(320F, 0.0F, 0.0F), new Part(40F, 0.0F, 0.0F), 1),
            new Frame(new Part(-8F, 312F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(270F, 318F, 0.0F), new Part(210F, 208F, 0.0F), new Part(318F, 0.0F, 0.0F), new Part(42F, 0.0F, 0.0F), 1),
            new Frame(new Part(-10F, 315F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(268F, 318F, 0.0F), new Part(220F, 208F, 0.0F), new Part(318F, 0.0F, 0.0F), new Part(42F, 0.0F, 0.0F), 3),
            new Frame(new Part(-10F, 315F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(268F, 318F, 0.0F), new Part(220F, 208F, 0.0F), new Part(318F, 0.0F, 0.0F), new Part(42F, 0.0F, 0.0F), 4),
            new Frame(new Part(22F, 350F, 0.0F), new Part(34F, 0.0F, 0.0F), new Part(193F, 0.0F, 104F), new Part(303F, 0.0F, 0.0F), new Part(50F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), 1),
            new Frame(new Part(46F, 328F, 0.0F), new Part(48F, 0.0F, 20F), new Part(191F, 0.0F, 56F), new Part(320F, 0.0F, 0.0F), new Part(20F, 0.0F, 0.0F), new Part(-20F, 0.0F, 0.0F), 1),
            new Frame(new Part(46F, 328F, 0.0F), new Part(48F, 0.0F, 20F), new Part(191F, 0.0F, 56F), new Part(320F, -10F, 0.0F), new Part(40F, 0.0F, 0.0F), new Part(-30F, 0.0F, 0.0F), 1),
            new Frame(new Part(46F, 328F, 0.0F), new Part(48F, 0.0F, 20F), new Part(191F, 0.0F, 56F), new Part(320F, -30F, 0.0F), new Part(60F, 0.0F, 0.0F), new Part(-30F, 0.0F, 0.0F), 1),
            new Frame(new Part(46F, 328F, 0.0F), new Part(48F, 0.0F, 20F), new Part(191F, 0.0F, 56F), new Part(320F, -40F, 0.0F), new Part(80F, 0.0F, 0.0F), new Part(-30F, 0.0F, 0.0F), 3),
            new Frame(new Part(46F, 328F, 0.0F), new Part(48F, 0.0F, 20F), new Part(191F, 0.0F, 56F), new Part(320F, -40F, 0.0F), new Part(83F, 0.0F, 0.0F), new Part(-30F, 0.0F, 0.0F), 7),
    };

    private static final Frame[] COMBO_5 = {
            new Frame(new Part(0.0F, 70F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(-80F, 0.0F, 0.0F), new Part(-80F, -60F, 0.0F), new Part(10F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), 5),
            new Frame(new Part(0.0F, 80F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(-80F, 0.0F, 0.0F), new Part(-80F, -60F, 0.0F), new Part(10F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), 3),
            new Frame(new Part(0.0F, 85F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(90F, 70F, 0.0F), new Part(-110F, -65F, 0.0F), new Part(10F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), 3),
            new Frame(new Part(0.0F, 90F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(90F, 70F, 0.0F), new Part(-110F, -65F, 0.0F), new Part(10F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), 3),
            new Frame(new Part(0.0F, 95F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(90F, 70F, 0.0F), new Part(-110F, -65F, 0.0F), new Part(10F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 100F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(90F, 70F, 0.0F), new Part(-110F, -65F, 0.0F), new Part(10F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 102F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(90F, 70F, 0.0F), new Part(-110F, -65F, 0.0F), new Part(10F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 104F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(90F, 70F, 0.0F), new Part(-110F, -65F, 0.0F), new Part(10F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), 5),
            new Frame(new Part(0.0F, 104F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(90F, 70F, 0.0F), new Part(-110F, -65F, 0.0F), new Part(10F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), 3),
            new Frame(new Part(0.0F, 104F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(90F, 70F, 0.0F), new Part(-110F, -65F, 0.0F), new Part(10F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 104F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(90F, 70F, 0.0F), new Part(-110F, -65F, 0.0F), new Part(10F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 104F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(90F, 70F, 0.0F), new Part(-110F, -65F, 0.0F), new Part(10F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, 30F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(90F, 70F, 0.0F), new Part(-90F, -45F, 0.0F), new Part(10F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, -10F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(-40F, -90F, 0.0F), new Part(-75F, 35F, 0.0F), new Part(10F, 0.0F, 0.0F), new Part(-10F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, -30F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(-40F, -90F, 0.0F), new Part(-50F, 55F, 0.0F), new Part(-20F, 0.0F, 0.0F), new Part(10F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, -55F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(-60F, -90F, 0.0F), new Part(50F, -35F, 0.0F), new Part(-20F, 0.0F, 0.0F), new Part(10F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, -71F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(-80F, -90F, 0.0F), new Part(70F, 15F, 0.0F), new Part(-20F, 0.0F, 0.0F), new Part(10F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, -76F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(-105F, -60F, 0.0F), new Part(70F, 15F, 0.0F), new Part(-20F, 0.0F, 0.0F), new Part(10F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, -78F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(-105F, 0.0F, 0.0F), new Part(70F, 15F, 0.0F), new Part(-20F, 0.0F, 0.0F), new Part(10F, 0.0F, 0.0F), 1),
            new Frame(new Part(0.0F, -80F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(-105F, 30F, 0.0F), new Part(70F, 15F, 0.0F), new Part(-21F, 0.0F, 0.0F), new Part(11F, 0.0F, 0.0F), 3),
            new Frame(new Part(0.0F, -81F, 0.0F), new Part(0.0F, 14F, 0.0F), new Part(-105F, 32F, 0.0F), new Part(70F, 15F, 0.0F), new Part(-22F, 0.0F, 0.0F), new Part(12F, 0.0F, 0.0F), 7),
    };

    static boolean supports(int animation) {
        return animation == MarawTharBossEntity.ANIMATION_TRIPLE_SLASH
                || animation == MarawTharBossEntity.ANIMATION_H1_COMBO2
                || animation == MarawTharBossEntity.ANIMATION_H1_COMBO3
                || animation == MarawTharBossEntity.ANIMATION_H1_COMBO4
                || animation == MarawTharBossEntity.ANIMATION_BLUE_RUSH;
    }

    static Frame sample(int animation, float score) {
        Frame[] frames = framesFor(animation);
        float time = Math.max(0.0F, score - 1.0F);
        float cursor = 0.0F;
        for (int index = 0; index < frames.length; index++) {
            Frame current = frames[index];
            float end = cursor + current.holdTicks();
            if (time < end || index == frames.length - 1) {
                Frame next = frames[Math.min(index + 1, frames.length - 1)];
                float blend = MarawTharFrameInterpolation.blend(
                        time - cursor, current.holdTicks());
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
            case MarawTharBossEntity.ANIMATION_BLUE_RUSH -> COMBO_5;
            default -> COMBO_1;
        };
    }

    private static Frame lerp(Frame from, Frame to, float delta) {
        return new Frame(
                lerp(from.head(), to.head(), delta),
                lerp(from.body(), to.body(), delta),
                lerp(from.leftArm(), to.leftArm(), delta),
                lerp(from.rightArm(), to.rightArm(), delta),
                lerp(from.leftLeg(), to.leftLeg(), delta),
                lerp(from.rightLeg(), to.rightLeg(), delta),
                from.holdTicks());
    }

    private static Part lerp(Part from, Part to, float delta) {
        return new Part(
                angularLerp(from.x(), to.x(), delta),
                angularLerp(from.y(), to.y(), delta),
                angularLerp(from.z(), to.z(), delta));
    }

    private static float angularLerp(float from, float to, float delta) {
        return from + Mth.wrapDegrees(to - from) * delta;
    }
}
