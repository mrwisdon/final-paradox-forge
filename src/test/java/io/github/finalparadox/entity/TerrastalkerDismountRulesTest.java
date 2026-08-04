package io.github.finalparadox.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TerrastalkerDismountRulesTest {
    @Test
    void allowsDedicatedDismountKeyOutsideCombat() {
        assertTrue(TerrastalkerDismountRules.shouldExit(false));
    }

    @Test
    void blocksDedicatedDismountKeyDuringB8Combat() {
        assertFalse(TerrastalkerDismountRules.shouldExit(true));
    }

    @Test
    void cancelsUnauthorizedDismountForLivingRider() {
        assertTrue(TerrastalkerDismountRules.shouldCancel(true, false, false));
    }

    @Test
    void allowsExplicitlyAuthorizedDismount() {
        assertFalse(TerrastalkerDismountRules.shouldCancel(true, false, true));
    }

    @Test
    void allowsDeathCleanupDismount() {
        assertFalse(TerrastalkerDismountRules.shouldCancel(false, false, false));
    }

    @Test
    void allowsRemovedPlayerCleanupDismount() {
        assertFalse(TerrastalkerDismountRules.shouldCancel(true, true, false));
    }
}
