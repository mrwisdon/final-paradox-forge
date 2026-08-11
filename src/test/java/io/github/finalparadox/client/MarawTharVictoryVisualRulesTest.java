package io.github.finalparadox.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MarawTharVictoryVisualRulesTest {
    private static final double EPS = 1.0E-9D;

    @Test
    void yCadenceFollowsModuloThreeBeforeCutoff() {
        assertEquals(0.0D, MarawTharVictoryVisualRules.yOffsetAt(0), EPS);
        assertEquals(0.1D, MarawTharVictoryVisualRules.yOffsetAt(1), EPS);
        assertEquals(0.1D, MarawTharVictoryVisualRules.yOffsetAt(2), EPS);
        assertEquals(0.0D, MarawTharVictoryVisualRules.yOffsetAt(3), EPS);
        assertEquals(0.1D, MarawTharVictoryVisualRules.yOffsetAt(4), EPS);
        assertEquals(0.1D, MarawTharVictoryVisualRules.yOffsetAt(5), EPS);
        assertEquals(0.0D, MarawTharVictoryVisualRules.yOffsetAt(6), EPS);
    }

    @Test
    void zCadenceAlternatesEveryTickBeforeCutoff() {
        assertEquals(0.0D, MarawTharVictoryVisualRules.zOffsetAt(0), EPS);
        assertEquals(0.1D, MarawTharVictoryVisualRules.zOffsetAt(1), EPS);
        assertEquals(0.0D, MarawTharVictoryVisualRules.zOffsetAt(2), EPS);
        assertEquals(0.1D, MarawTharVictoryVisualRules.zOffsetAt(3), EPS);
    }

    @Test
    void shakeInterpolatesBetweenIntegerTickOffsetsBeforeCutoff() {
        // age 2 -> y 0.1, age 3 -> y 0.0, so the half-tick sample is 0.05.
        assertEquals(0.05D, MarawTharVictoryVisualRules.shakeY(2, 0, 0.5F), EPS);
        // age 1 -> z 0.1, age 2 -> z 0.0, so the half-tick sample is 0.05.
        assertEquals(0.05D, MarawTharVictoryVisualRules.shakeZ(1, 0, 0.5F), EPS);
        // partialTick 1.0 samples the next integer tick.
        assertEquals(0.1D, MarawTharVictoryVisualRules.shakeY(1, 0, 1.0F), EPS);
        assertEquals(0.0D, MarawTharVictoryVisualRules.shakeY(2, 0, 1.0F), EPS);
    }

    @Test
    void shakeRunsWhilePetrificationScoreIsBelowCutoff() {
        assertTrue(MarawTharVictoryVisualRules.shakeActive(-1));
        assertTrue(MarawTharVictoryVisualRules.shakeActive(0));
        assertTrue(MarawTharVictoryVisualRules.shakeActive(137));
    }

    @Test
    void shakeIsExactlyZeroFromCutoffScoreOnward() {
        assertFalse(MarawTharVictoryVisualRules.shakeActive(138));
        assertFalse(MarawTharVictoryVisualRules.shakeActive(139));
        assertFalse(MarawTharVictoryVisualRules.shakeActive(140));

        // The boundary tick is 138 even when the cadence itself would be non-zero.
        assertEquals(0.0D, MarawTharVictoryVisualRules.shakeY(1, 138, 0.0F), EPS);
        assertEquals(0.0D, MarawTharVictoryVisualRules.shakeY(1, 138, 0.5F), EPS);
        assertEquals(0.0D, MarawTharVictoryVisualRules.shakeY(1, 138, 1.0F), EPS);
        assertEquals(0.0D, MarawTharVictoryVisualRules.shakeZ(1, 138, 0.0F), EPS);

        // Statue fall (score 180) and end (score 260) windows stay at zero.
        assertEquals(0.0D, MarawTharVictoryVisualRules.shakeY(0, 180, 0.5F), EPS);
        assertEquals(0.0D, MarawTharVictoryVisualRules.shakeZ(0, 260, 0.5F), EPS);
        assertEquals(0.0D, MarawTharVictoryVisualRules.shakeY(1, 200, 1.0F), EPS);
    }
}
