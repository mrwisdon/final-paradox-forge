package io.github.finalparadox.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArenaPlayerRespawnTest {
    private static final ResourceLocation OVERWORLD =
            ResourceLocation.fromNamespaceAndPath("minecraft", "overworld");
    private static final ResourceLocation NETHER =
            ResourceLocation.fromNamespaceAndPath("minecraft", "the_nether");
    private static final ResourceLocation ARENA =
            ResourceLocation.fromNamespaceAndPath("finalparadox", "arena_dimension");

    @Test
    void snapshotRoundTripsWithPosition() {
        ArenaPlayerRespawn.RespawnSnapshot expected = new ArenaPlayerRespawn.RespawnSnapshot(
                NETHER, new BlockPos(12, 64, -8), 37.5F, true);

        assertEquals(expected, ArenaPlayerRespawn.RespawnSnapshot.read(expected.write()));
    }

    @Test
    void snapshotRoundTripsWithoutPosition() {
        ArenaPlayerRespawn.RespawnSnapshot expected = new ArenaPlayerRespawn.RespawnSnapshot(
                OVERWORLD, null, 0.0F, false);

        ArenaPlayerRespawn.RespawnSnapshot decoded =
                ArenaPlayerRespawn.RespawnSnapshot.read(expected.write());
        assertEquals(expected, decoded);
        assertNull(decoded.position());
    }

    @Test
    void malformedDimensionFallsBackToOverworld() {
        ArenaPlayerRespawn.RespawnSnapshot original = new ArenaPlayerRespawn.RespawnSnapshot(
                NETHER, new BlockPos(1, 2, 3), 10.0F, false);
        CompoundTag tag = original.write();
        tag.putString("dimension", "not a valid resource location");

        ArenaPlayerRespawn.RespawnSnapshot decoded =
                ArenaPlayerRespawn.RespawnSnapshot.read(tag);
        assertEquals(OVERWORLD, decoded.dimension());
    }

    @Test
    void inactiveSnapshotDoesNotDecode() {
        assertNull(ArenaPlayerRespawn.RespawnSnapshot.read(new CompoundTag()));
    }

    @Test
    void firstSnapshotWinsAcrossConsecutiveFights() {
        CompoundTag root = new CompoundTag();
        ArenaPlayerRespawn.RespawnSnapshot original = new ArenaPlayerRespawn.RespawnSnapshot(
                OVERWORLD, new BlockPos(40, 70, 40), 90.0F, false);
        ArenaPlayerRespawn.RespawnSnapshot later = new ArenaPlayerRespawn.RespawnSnapshot(
                NETHER, new BlockPos(1, 2, 3), 0.0F, true);

        assertEquals(original, ArenaPlayerRespawn.firstOrExisting(root, original));
        assertEquals(original, ArenaPlayerRespawn.firstOrExisting(root, later));
        assertEquals(original, ArenaPlayerRespawn.snapshotIn(root));
    }

    @Test
    void corruptExistingSnapshotIsReplaced() {
        CompoundTag root = new CompoundTag();
        root.put(ArenaPlayerRespawn.SNAPSHOT_KEY, new CompoundTag());
        ArenaPlayerRespawn.RespawnSnapshot current = new ArenaPlayerRespawn.RespawnSnapshot(
                OVERWORLD, null, 0.0F, false);

        assertEquals(current, ArenaPlayerRespawn.firstOrExisting(root, current));
        assertEquals(current, ArenaPlayerRespawn.snapshotIn(root));
    }

    @Test
    void onlyLeavingArenaRequestsRestore() {
        assertTrue(ArenaPlayerRespawn.isArenaLocation(ARENA));
        assertFalse(ArenaPlayerRespawn.isArenaLocation(OVERWORLD));
        assertFalse(ArenaPlayerRespawn.isArenaLocation(NETHER));
    }
}
