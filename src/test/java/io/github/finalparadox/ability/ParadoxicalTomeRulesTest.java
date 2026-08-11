package io.github.finalparadox.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ParadoxicalTomeRulesTest {
    @Test
    void usesOriginalLevelThreeTimings() {
        assertEquals(400, ParadoxicalTomeRules.TIMELINE_TICKS);
        assertEquals(500, ParadoxicalTomeRules.COOLDOWN_TICKS);
        assertEquals(5, ParadoxicalTomeRules.DOUBLE_TAP_WINDOW_TICKS);
    }

    @Test
    void holdingSneakDoesNotTriggerARegression() {
        ParadoxicalTomeRules.SneakStep step = ParadoxicalTomeRules.tickSneak(0, false, true);
        assertFalse(step.triggered());
        for (int tick = 0; tick < 8; tick++) {
            step = ParadoxicalTomeRules.tickSneak(step.windowTicks(), step.wasSneaking(), true);
            assertFalse(step.triggered());
        }
    }

    @Test
    void releaseAndSecondPressInsideWindowTriggers() {
        ParadoxicalTomeRules.SneakStep first = ParadoxicalTomeRules.tickSneak(0, false, true);
        ParadoxicalTomeRules.SneakStep release =
                ParadoxicalTomeRules.tickSneak(first.windowTicks(), first.wasSneaking(), false);
        ParadoxicalTomeRules.SneakStep second =
                ParadoxicalTomeRules.tickSneak(release.windowTicks(), release.wasSneaking(), true);

        assertTrue(second.triggered());
    }

    @Test
    void secondPressAfterWindowExpiresDoesNotTrigger() {
        ParadoxicalTomeRules.SneakStep step = ParadoxicalTomeRules.tickSneak(0, false, true);
        step = ParadoxicalTomeRules.tickSneak(step.windowTicks(), step.wasSneaking(), false);
        for (int tick = 0; tick < 4; tick++) {
            step = ParadoxicalTomeRules.tickSneak(step.windowTicks(), step.wasSneaking(), false);
        }
        assertEquals(0, step.windowTicks());

        step = ParadoxicalTomeRules.tickSneak(step.windowTicks(), step.wasSneaking(), true);
        assertFalse(step.triggered());
        assertEquals(5, step.windowTicks());
    }

    @Test
    void dimensionsMustMatchExactly() {
        assertTrue(ParadoxicalTomeRules.sameDimension("minecraft:overworld", "minecraft:overworld"));
        assertFalse(ParadoxicalTomeRules.sameDimension("minecraft:overworld", "minecraft:the_nether"));
        assertFalse(ParadoxicalTomeRules.sameDimension(null, "minecraft:overworld"));
    }

    @Test
    void healingUsesOriginalStrictThresholds() {
        assertEquals(new ParadoxicalTomeRules.HealingPlan(0, false),
                ParadoxicalTomeRules.healingPlan(11.0F, 10.0F));
        assertEquals(new ParadoxicalTomeRules.HealingPlan(0, true),
                ParadoxicalTomeRules.healingPlan(11.01F, 10.0F));
        assertEquals(new ParadoxicalTomeRules.HealingPlan(4, true),
                ParadoxicalTomeRules.healingPlan(13.01F, 10.0F));
        assertEquals(new ParadoxicalTomeRules.HealingPlan(12, true),
                ParadoxicalTomeRules.healingPlan(17.01F, 10.0F));
    }

    @Test
    void restorationNeverLowersHealthOrExceedsMaximum() {
        assertEquals(18.0F, ParadoxicalTomeRules.restoredHealth(10.0F, 18.0F, 20.0F));
        assertEquals(20.0F, ParadoxicalTomeRules.restoredHealth(40.0F, 10.0F, 20.0F));
    }

    @Test
    void finalTimelineTickCleansUp() {
        int remaining = ParadoxicalTomeRules.tickTimeline(1);
        assertEquals(0, remaining);
        assertFalse(ParadoxicalTomeRules.timelineActive(remaining));
    }
}
