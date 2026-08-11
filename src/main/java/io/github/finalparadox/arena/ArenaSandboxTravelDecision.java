package io.github.finalparadox.arena;

import net.minecraft.core.BlockPos;

/** Pure state-machine policy for a sandbox activation. */
public final class ArenaSandboxTravelDecision {
    private ArenaSandboxTravelDecision() {
    }

    public static Decision decide(
            ArenaDeploymentData.DeploymentState state,
            String recordedArenaId,
            BlockPos recordedAnchor,
            ArenaSlots.ArenaSlot expected
    ) {
        if (state == ArenaDeploymentData.DeploymentState.IDLE) return Decision.START_AND_WAIT;
        if (state == ArenaDeploymentData.DeploymentState.ERROR) return Decision.REJECT_ERROR;
        if (!expected.definition().id().equals(recordedArenaId)
                || recordedAnchor == null
                || !expected.floorAnchor().equals(recordedAnchor)) {
            return Decision.REJECT_STALE_STATE;
        }
        return state == ArenaDeploymentData.DeploymentState.READY
                ? Decision.TELEPORT
                : Decision.WAIT;
    }

    public enum Decision {
        TELEPORT,
        START_AND_WAIT,
        WAIT,
        REJECT_ERROR,
        REJECT_STALE_STATE
    }
}
