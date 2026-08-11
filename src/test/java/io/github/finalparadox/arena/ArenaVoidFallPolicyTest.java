package io.github.finalparadox.arena;

import org.junit.jupiter.api.Test;

import static io.github.finalparadox.arena.ArenaVoidFallPolicy.Action.NORMAL_DEATH;
import static io.github.finalparadox.arena.ArenaVoidFallPolicy.Action.RESCUE_TO_ENTRY;
import static io.github.finalparadox.arena.ArenaVoidFallPolicy.Action.RETURN_TO_OVERWORLD;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ArenaVoidFallPolicyTest {
    @Test
    void fightAlwaysUsesNormalDeathPath() {
        assertEquals(NORMAL_DEATH, ArenaVoidFallPolicy.decide(true, true));
        assertEquals(NORMAL_DEATH, ArenaVoidFallPolicy.decide(true, false));
    }

    @Test
    void nonFightRescuesOnlyWithValidCurrentArena() {
        assertEquals(RESCUE_TO_ENTRY, ArenaVoidFallPolicy.decide(false, true));
        assertEquals(RETURN_TO_OVERWORLD, ArenaVoidFallPolicy.decide(false, false));
    }
}
