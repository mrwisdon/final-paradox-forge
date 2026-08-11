package io.github.finalparadox.arena;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArenaReadyMaintenancePolicyTest {
    @Test
    void activeFightNeverMaintainsReady() {
        assertFalse(ArenaReadyMaintenancePolicy.shouldMaintainReady(0L, true));
        assertFalse(ArenaReadyMaintenancePolicy.shouldMaintainReady(40L, true));
        assertFalse(ArenaReadyMaintenancePolicy.shouldMaintainReady(4_000L, true));
    }

    @Test
    void readyMaintenanceRunsEveryTwoSecondsWithoutFight() {
        assertTrue(ArenaReadyMaintenancePolicy.shouldMaintainReady(0L, false));
        assertFalse(ArenaReadyMaintenancePolicy.shouldMaintainReady(5L, false));
        assertFalse(ArenaReadyMaintenancePolicy.shouldMaintainReady(35L, false));
        assertTrue(ArenaReadyMaintenancePolicy.shouldMaintainReady(40L, false));
        assertTrue(ArenaReadyMaintenancePolicy.shouldMaintainReady(4_000L, false));
        assertFalse(ArenaReadyMaintenancePolicy.shouldMaintainReady(4_001L, false));
    }
}