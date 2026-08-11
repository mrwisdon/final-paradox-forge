package io.github.finalparadox.arena;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static io.github.finalparadox.arena.ArenaSandboxTravelDecision.Decision.REJECT_ERROR;
import static io.github.finalparadox.arena.ArenaSandboxTravelDecision.Decision.REJECT_STALE_STATE;
import static io.github.finalparadox.arena.ArenaSandboxTravelDecision.Decision.START_AND_WAIT;
import static io.github.finalparadox.arena.ArenaSandboxTravelDecision.Decision.TELEPORT;
import static io.github.finalparadox.arena.ArenaSandboxTravelDecision.Decision.WAIT;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ArenaSandboxTravelDecisionTest {
    @Test
    void mapsEveryDeploymentStateWithoutFallbacks() {
        ArenaSlots.ArenaSlot slot = ArenaSlots.B1;
        assertEquals(START_AND_WAIT, decide(ArenaDeploymentData.DeploymentState.IDLE, "", null, slot));
        assertEquals(WAIT, decide(ArenaDeploymentData.DeploymentState.DEPLOYING,
                "b1", slot.floorAnchor(), slot));
        assertEquals(TELEPORT, decide(ArenaDeploymentData.DeploymentState.READY,
                "b1", slot.floorAnchor(), slot));
        assertEquals(REJECT_ERROR, decide(ArenaDeploymentData.DeploymentState.ERROR,
                "b1", slot.floorAnchor(), slot));
    }

    @Test
    void rejectsWrongArenaAndWrongAnchorAsStaleState() {
        assertEquals(REJECT_STALE_STATE, decide(ArenaDeploymentData.DeploymentState.READY,
                "b2", ArenaSlots.B1.floorAnchor(), ArenaSlots.B1));
        assertEquals(REJECT_STALE_STATE, decide(ArenaDeploymentData.DeploymentState.READY,
                "b1", ArenaSlots.B1.floorAnchor().offset(1, 0, 0), ArenaSlots.B1));
        assertEquals(REJECT_STALE_STATE, decide(ArenaDeploymentData.DeploymentState.DEPLOYING,
                "b1", null, ArenaSlots.B1));
    }

    private static ArenaSandboxTravelDecision.Decision decide(
            ArenaDeploymentData.DeploymentState state,
            String id,
            BlockPos anchor,
            ArenaSlots.ArenaSlot slot
    ) {
        return ArenaSandboxTravelDecision.decide(state, id, anchor, slot);
    }
}
