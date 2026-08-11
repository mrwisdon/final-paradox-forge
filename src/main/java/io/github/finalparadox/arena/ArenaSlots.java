package io.github.finalparadox.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Authoritative deterministic layout for every sandbox-addressable arena. */
public final class ArenaSlots {
    public static final int SLOT_SPACING = 4096;
    public static final int FLOOR_Y = 128;

    public static final ArenaSlot B1 = new ArenaSlot(
            ArenaDefinitions.B1, "apiglo_arena_sandbox", new BlockPos(0, FLOOR_Y, 0),
            new BlockPos(0, 2, 0), -90.0F);
    public static final ArenaSlot B2 = new ArenaSlot(
            ArenaDefinitions.B2, "thar_kroo_arena_sandbox", new BlockPos(SLOT_SPACING, FLOOR_Y, 0),
            new BlockPos(0, 1, -32), 0.0F);
    public static final ArenaSlot B5 = new ArenaSlot(
            ArenaDefinitions.B5, "koyomi_gariheuz_arena_sandbox", new BlockPos(SLOT_SPACING * 2, FLOOR_Y, 0),
            new BlockPos(9, 1, 0), 90.0F);
    public static final ArenaSlot B8 = new ArenaSlot(
            ArenaDefinitions.B8, "zombie_supermatrix_arena_sandbox", new BlockPos(SLOT_SPACING * 3, FLOOR_Y, 0),
            new BlockPos(14, 1, 0), 90.0F);
    public static final ArenaSlot MARAWTHAR = new ArenaSlot(
            ArenaDefinitions.MARAWTHAR, "marawthar_arena_sandbox", new BlockPos(SLOT_SPACING * 4, FLOOR_Y, 0),
            new BlockPos(-10, 1, 0), -90.0F);

    /** Reserved only: B10's boss, template deployment, and sandbox are intentionally absent. */
    public static final ReservedSlot B10_TIME_NIGHTMARE = new ReservedSlot(
            "b10_time_nightmare", new BlockPos(SLOT_SPACING * 5, FLOOR_Y, 0),
            new Vec3i(44, 2, 43));

    public static final List<ArenaSlot> ALL = List.of(B1, B2, B5, B8, MARAWTHAR);
    private static final Map<String, ArenaSlot> BY_ARENA_ID = Map.of(
            ArenaDefinitions.B1.id(), B1,
            ArenaDefinitions.B2.id(), B2,
            ArenaDefinitions.B5.id(), B5,
            ArenaDefinitions.B8.id(), B8,
            ArenaDefinitions.MARAWTHAR.id(), MARAWTHAR);
    private static final Map<String, ArenaSlot> BY_SANDBOX_ID = Map.of(
            B1.sandboxId(), B1,
            B2.sandboxId(), B2,
            B5.sandboxId(), B5,
            B8.sandboxId(), B8,
            MARAWTHAR.sandboxId(), MARAWTHAR);

    private ArenaSlots() {
    }

    public static Optional<ArenaSlot> forArenaId(String arenaId) {
        if ("b9".equals(arenaId)) return Optional.of(MARAWTHAR);
        return Optional.ofNullable(BY_ARENA_ID.get(arenaId));
    }

    public static Optional<ArenaSlot> forDefinition(ArenaDefinition definition) {
        return forArenaId(definition.id()).filter(slot -> slot.definition() == definition);
    }

    /** Resolves only an exact authoritative fixed deployment, never a manual arena anchor. */
    public static Optional<ArenaSlot> forFixedDeployment(
            ArenaDefinition definition,
            BlockPos floorAnchor
    ) {
        return forDefinition(definition)
                .filter(slot -> slot.floorAnchor().equals(floorAnchor));
    }

    public static Optional<ArenaSlot> forSandboxId(String sandboxId) {
        return Optional.ofNullable(BY_SANDBOX_ID.get(sandboxId));
    }

    public record ArenaSlot(
            ArenaDefinition definition,
            String sandboxId,
            BlockPos floorAnchor,
            BlockPos entryOffset,
            float entryYaw
    ) {
        public BlockPos entryPosition() {
            return floorAnchor.offset(entryOffset);
        }
    }

    public record ReservedSlot(String arenaId, BlockPos floorAnchor, Vec3i sourceFootprint) {
    }
}
