package io.github.finalparadox.arena;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArenaDimensionPolicyTest {
    @Test
    void recognizesActiveAndLegacyArenaDimensions() {
        assertTrue(ArenaDimensionPolicy.isArenaId("finalparadox:arena_void"));
        assertTrue(ArenaDimensionPolicy.isArenaId("finalparadox:arena_dimension"));
        assertTrue(ArenaDimensionPolicy.isActiveId("finalparadox:arena_void"));
        assertFalse(ArenaDimensionPolicy.isActiveId("finalparadox:arena_dimension"));
        assertFalse(ArenaDimensionPolicy.isArenaId("minecraft:the_nether"));
    }
}
