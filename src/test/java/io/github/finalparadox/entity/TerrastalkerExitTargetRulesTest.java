package io.github.finalparadox.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TerrastalkerExitTargetRulesTest {
    @Test
    void acceptsCurrentVehicle() {
        assertTrue(TerrastalkerExitTargetRules.canRecover(true, false, false));
    }

    @Test
    void acceptsRoverThatStillListsPlayerAsPassenger() {
        assertTrue(TerrastalkerExitTargetRules.canRecover(false, true, false));
    }

    @Test
    void acceptsOwnedRoverForClientOnlyStalePassengerState() {
        assertTrue(TerrastalkerExitTargetRules.canRecover(false, false, true));
    }

    @Test
    void rejectsUnrelatedRover() {
        assertFalse(TerrastalkerExitTargetRules.canRecover(false, false, false));
    }

    @Test
    void directlyRemovesOneSidedPassengerEntry() {
        assertTrue(TerrastalkerExitTargetRules.shouldRemoveStalePassenger(false, true));
    }

    @Test
    void doesNotDirectlyRemoveConsistentPassengerEntry() {
        assertFalse(TerrastalkerExitTargetRules.shouldRemoveStalePassenger(true, true));
    }
}
