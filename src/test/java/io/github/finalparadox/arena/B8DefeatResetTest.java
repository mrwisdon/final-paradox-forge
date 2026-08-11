package io.github.finalparadox.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class B8DefeatResetTest {
    @Test
    void defeatReturnsDeploymentToKorosGatedRetryState() {
        ArenaDeploymentData data = new ArenaDeploymentData();
        BlockPos anchor = new BlockPos(120, 64, -200);
        data.adoptWorldgen(ArenaDefinitions.B8, anchor);
        data.setActiveBossUuid(UUID.randomUUID());
        data.setKorosUuid(UUID.randomUUID());
        data.setB8Triggered(true);

        data.resetB8ForRetry();

        assertTrue(data.activeBossUuid().isEmpty());
        assertTrue(data.korosUuid().isEmpty());
        assertFalse(data.b8Triggered());
        assertEquals(ArenaDeploymentData.DeploymentState.READY, data.state());
        assertEquals(anchor, data.floorAnchor().orElseThrow());
        assertEquals("b8", data.arenaId());
        assertEquals(ArenaDefinitions.B8.tileCount(), data.nextTile());
    }

    @Test
    void retryResetIsIdempotentAndPersistsNoActiveEncounter() {
        ArenaDeploymentData data = new ArenaDeploymentData();
        data.adoptWorldgen(ArenaDefinitions.B8, BlockPos.ZERO);
        data.setActiveBossUuid(UUID.randomUUID());
        data.setKorosUuid(UUID.randomUUID());
        data.setB8Triggered(true);

        data.resetB8ForRetry();
        data.resetB8ForRetry();
        CompoundTag saved = data.save(new CompoundTag());

        assertFalse(saved.hasUUID("ActiveBoss"));
        assertFalse(saved.hasUUID("Koros"));
        assertFalse(saved.getBoolean("B8Triggered"));
    }
}