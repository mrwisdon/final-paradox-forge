package io.github.finalparadox.arena;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArenaSlotsTest {
    @Test
    void everyCompleteArenaAndSandboxHasExactlyOneSlot() {
        assertEquals(ArenaDefinitions.ALL.size(), ArenaSlots.ALL.size());
        Set<String> arenaIds = new HashSet<>();
        Set<String> sandboxIds = new HashSet<>();
        for (ArenaSlots.ArenaSlot slot : ArenaSlots.ALL) {
            assertTrue(arenaIds.add(slot.definition().id()));
            assertTrue(sandboxIds.add(slot.sandboxId()));
            assertSame(slot, ArenaSlots.forArenaId(slot.definition().id()).orElseThrow());
            assertSame(slot, ArenaSlots.forSandboxId(slot.sandboxId()).orElseThrow());
        }
        assertSame(ArenaSlots.MARAWTHAR, ArenaSlots.forArenaId("b9").orElseThrow());
        assertTrue(ArenaSlots.forArenaId(ArenaSlots.B10_TIME_NIGHTMARE.arenaId()).isEmpty());
    }

    @Test
    void slotsAreInsideBuildHeightAndVolumesNeverOverlap() {
        for (ArenaSlots.ArenaSlot slot : ArenaSlots.ALL) {
            BlockPos min = slot.definition().minimumCorner(slot.floorAnchor());
            BlockPos max = slot.definition().maximumCorner(slot.floorAnchor());
            assertTrue(min.getY() >= -64, slot.definition().id());
            assertTrue(max.getY() < 320, slot.definition().id());
            assertTrue(slot.entryPosition().getY() >= min.getY());
            assertTrue(slot.entryPosition().getY() <= max.getY());
        }
        for (int first = 0; first < ArenaSlots.ALL.size(); first++) {
            for (int second = first + 1; second < ArenaSlots.ALL.size(); second++) {
                assertFalse(overlaps(ArenaSlots.ALL.get(first), ArenaSlots.ALL.get(second)));
            }
        }
        assertEquals(ArenaSlots.SLOT_SPACING,
                ArenaSlots.B10_TIME_NIGHTMARE.floorAnchor().getX()
                        - ArenaSlots.MARAWTHAR.floorAnchor().getX());
    }

    @Test
    void entriesMatchApprovedSourceRelativeCoordinates() {
        assertEquals(new BlockPos(0, 130, 0), ArenaSlots.B1.entryPosition());
        assertEquals(new BlockPos(4096, 129, -32), ArenaSlots.B2.entryPosition());
        assertEquals(new BlockPos(8201, 129, 0), ArenaSlots.B5.entryPosition());
        assertEquals(new BlockPos(12302, 129, 0), ArenaSlots.B8.entryPosition());
        assertEquals(new BlockPos(16374, 129, 0), ArenaSlots.MARAWTHAR.entryPosition());
    }

    @Test
    void onlyExactFixedAnchorBindsPlayerArenaState() {
        assertSame(ArenaSlots.B1, ArenaSlots.forFixedDeployment(
                ArenaDefinitions.B1, ArenaSlots.B1.floorAnchor()).orElseThrow());
        assertTrue(ArenaSlots.forFixedDeployment(
                ArenaDefinitions.B1, ArenaSlots.B1.floorAnchor().offset(1, 0, 0)).isEmpty());
        assertTrue(ArenaSlots.forFixedDeployment(
                ArenaDefinitions.B2, ArenaSlots.B1.floorAnchor()).isEmpty());
    }

    private static boolean overlaps(ArenaSlots.ArenaSlot first, ArenaSlots.ArenaSlot second) {
        BlockPos firstMin = first.definition().minimumCorner(first.floorAnchor());
        BlockPos firstMax = first.definition().maximumCorner(first.floorAnchor());
        BlockPos secondMin = second.definition().minimumCorner(second.floorAnchor());
        BlockPos secondMax = second.definition().maximumCorner(second.floorAnchor());
        return firstMin.getX() <= secondMax.getX() && firstMax.getX() >= secondMin.getX()
                && firstMin.getY() <= secondMax.getY() && firstMax.getY() >= secondMin.getY()
                && firstMin.getZ() <= secondMax.getZ() && firstMax.getZ() >= secondMin.getZ();
    }
}
