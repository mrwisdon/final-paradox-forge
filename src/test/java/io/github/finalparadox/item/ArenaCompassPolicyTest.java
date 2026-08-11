package io.github.finalparadox.item;

import org.junit.jupiter.api.Test;

import static io.github.finalparadox.item.ArenaCompassPolicy.Action.BLOCKED_BY_FIGHT;
import static io.github.finalparadox.item.ArenaCompassPolicy.Action.DIRECT_TO_SANDBOX;
import static io.github.finalparadox.item.ArenaCompassPolicy.Action.RETURN_TO_OVERWORLD;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ArenaCompassPolicyTest {
    @Test
    void activeFightBlocksOnlyActiveArenaReturn() {
        assertEquals(BLOCKED_BY_FIGHT, ArenaCompassPolicy.decide(true, false, true));
        assertEquals(RETURN_TO_OVERWORLD, ArenaCompassPolicy.decide(false, true, true));
    }

    @Test
    void outsideArenaAlwaysDirectsToSandboxWithoutEntryFallback() {
        assertEquals(DIRECT_TO_SANDBOX, ArenaCompassPolicy.decide(false, false, false));
        assertEquals(DIRECT_TO_SANDBOX, ArenaCompassPolicy.decide(false, false, true));
        assertEquals(RETURN_TO_OVERWORLD, ArenaCompassPolicy.decide(true, false, false));
        assertEquals(RETURN_TO_OVERWORLD, ArenaCompassPolicy.decide(false, true, false));
    }
}
