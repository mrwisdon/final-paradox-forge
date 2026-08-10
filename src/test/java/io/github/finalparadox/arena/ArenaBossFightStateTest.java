package io.github.finalparadox.arena;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-logic tests for {@link ArenaBossFightState}. The boolean reduction is
 * kept free of Minecraft classes so the OR semantics can be verified without
 * a fragile mock of ServerLevel or the boss entities.
 */
final class ArenaBossFightStateTest {
    @Test
    void noneActiveReturnsFalse() {
        assertFalse(ArenaBossFightState.anyActive(false, false, false, false, false));
    }

    @Test
    void eachStateAloneReturnsTrue() {
        assertTrue(ArenaBossFightState.anyActive(true, false, false, false, false));
        assertTrue(ArenaBossFightState.anyActive(false, true, false, false, false));
        assertTrue(ArenaBossFightState.anyActive(false, false, true, false, false));
        assertTrue(ArenaBossFightState.anyActive(false, false, false, true, false));
        assertTrue(ArenaBossFightState.anyActive(false, false, false, false, true));
    }

    @Test
    void anyMixedCombinationReturnsTrue() {
        assertTrue(ArenaBossFightState.anyActive(true, false, true, false, false));
        assertTrue(ArenaBossFightState.anyActive(false, true, false, true, false));
        assertTrue(ArenaBossFightState.anyActive(true, true, true, true, true));
    }

    @Test
    void b1B2RuleCountsOnlyLivingNonWaiting() {
        assertTrue(ArenaBossFightState.livingNonWaiting(true, false));
        assertFalse(ArenaBossFightState.livingNonWaiting(true, true));
        assertFalse(ArenaBossFightState.livingNonWaiting(false, false));
        assertFalse(ArenaBossFightState.livingNonWaiting(false, true));
    }

    @Test
    void marawRuleCountsLivingBoss() {
        assertTrue(ArenaBossFightState.livingBoss(true));
        assertFalse(ArenaBossFightState.livingBoss(false));
    }
}
