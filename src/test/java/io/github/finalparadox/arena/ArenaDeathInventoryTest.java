package io.github.finalparadox.arena;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArenaDeathInventoryTest {
    @Test
    void protectsOnlyArenaFightParticipants() {
        assertTrue(ArenaDeathInventory.shouldProtect(true, true));
        assertFalse(ArenaDeathInventory.shouldProtect(true, false));
        assertFalse(ArenaDeathInventory.shouldProtect(false, true));
        assertFalse(ArenaDeathInventory.shouldProtect(false, false));
    }
}
