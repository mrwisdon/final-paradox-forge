package io.github.finalparadox.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MarawTharDefeatResetTest {
    @Test
    void defeatReturnsDeploymentToEotharGatedRetryState() {
        ArenaDeploymentData data = new ArenaDeploymentData();
        BlockPos anchor = new BlockPos(80, 52, -120);
        data.adoptWorldgen(ArenaDefinitions.MARAWTHAR, anchor);
        data.setActiveBossUuid(UUID.randomUUID());
        data.setEotharUuid(UUID.randomUUID());
        data.setMarawTharTriggered(true);

        data.resetMarawTharForRetry();

        assertTrue(data.activeBossUuid().isEmpty());
        assertTrue(data.eotharUuid().isEmpty());
        assertFalse(data.marawTharTriggered());
        assertEquals(ArenaDeploymentData.DeploymentState.READY, data.state());
        assertEquals(anchor, data.floorAnchor().orElseThrow());
    }

    @Test
    void retryResetIsIdempotentAndPersistsNoActiveEncounter() {
        ArenaDeploymentData data = new ArenaDeploymentData();
        data.adoptWorldgen(ArenaDefinitions.MARAWTHAR, BlockPos.ZERO);
        data.setActiveBossUuid(UUID.randomUUID());
        data.setEotharUuid(UUID.randomUUID());
        data.setMarawTharTriggered(true);

        data.resetMarawTharForRetry();
        data.resetMarawTharForRetry();
        CompoundTag saved = data.save(new CompoundTag());

        assertFalse(saved.hasUUID("ActiveBoss"));
        assertFalse(saved.hasUUID("Eothar"));
        assertFalse(saved.getBoolean("MarawTharTriggered"));
    }
}
