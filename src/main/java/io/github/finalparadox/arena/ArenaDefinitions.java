package io.github.finalparadox.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ArenaDefinitions {
    public static final ArenaDefinition B1 = new ArenaDefinition(
            "b1",
            "arenas/b1_candidate",
            new Vec3i(171, 64, 121),
            new BlockPos(-44, -2, -55),
            48,
            4,
            2,
            3,
            new Vec3i(0, 1, 0));

    public static final ArenaDefinition B2 = new ArenaDefinition(
            "b2",
            "arenas/b2",
            new Vec3i(94, 17, 100),
            new BlockPos(-53, -5, -52),
            48,
            2,
            1,
            3,
            new Vec3i(0, 0, 0));

    public static final ArenaDefinition MARAWTHAR = new ArenaDefinition(
            "marawthar",
            "arenas/b9_core",
            new Vec3i(129, 56, 129),
            new BlockPos(-64, -5, -64),
            48,
            3,
            2,
            3,
            new Vec3i(0, 1, 0));

    public static final ArenaDefinition B5 = new ArenaDefinition(
            "b5",
            "arenas/b5",
            new Vec3i(85, 62, 57),
            new BlockPos(-64, -6, -28),
            48,
            2,
            2,
            2,
            new Vec3i(0, 1, 0));

    /**
     * B8 Zombie Supermatrix arena. The source datapack force-loads the box
     * X -3851..-3804, Z 1388..1436 around the combat diamond at floor Y 78;
     * the structure is a 45x45 decorated combat floor with a central pedestal
     * and a light-blue/glass ring, all inside that box. The floor anchor is
     * the arena center; the matrix core sits at offset (0, 7, 0) above it.
     *
     * <p>The official encounter may be deployed at any anchor; the controller
     * resolves every runtime coordinate (player spawn, containment teleport,
     * spectator platform, hazards, reward pedestal) from the recorded floor
     * anchor, so the source's absolute coordinates are never hard-coded.
     */
    public static final ArenaDefinition B8 = new ArenaDefinition(
            "b8",
            "arenas/b8",
            new Vec3i(48, 14, 49),
            new BlockPos(-23, -9, -24),
            48,
            1,
            1,
            2,
            new Vec3i(0, 7, 0));

    /** Koyomi spawn, relative to the B5 floor anchor (arena center platform top). */
    public static final BlockPos B5_KOYO_OFFSET = new BlockPos(-41, 1, 2);
    /** Gariheuz spawn, relative to the B5 floor anchor. */
    public static final BlockPos B5_GARI_OFFSET = new BlockPos(-41, 1, -2);
    /**
     * Koros interaction hitbox, relative to the B5 floor anchor. The original
     * datapack places the villager hitbox one block below the visible echo.
     */
    public static final BlockPos B5_KOROS_HITBOX_OFFSET = new BlockPos(-30, 1, -5);

    /**
     * Echo of Koros position relative to the B8 floor anchor. The original
     * minikoros sits at -3820 80 1407 against the combat diamond center
     * -3828 78 1412, i.e. offset (+8, +2, -5).
     */
    public static final BlockPos B8_KOROS_OFFSET = new BlockPos(8, 2, -5);

    /**
     * Echo of Koros position relative to the B1 floor anchor. The original
     * minikoros sits at 1316 66 1529 against the floor anchor 1340 67 1537,
     * i.e. offset (-24, -1, -8).
     */
    public static final BlockPos B1_KOROS_OFFSET = new BlockPos(-24, -1, -8);

    /**
     * Echo of Koros position relative to the B2 floor anchor. The original
     * minikoros trigger sits at -1509 52 2291 against the encounter anchor
     * -1505 53.5 2305, i.e. offset (-4, -1, -14).
     */
    public static final BlockPos B2_KOROS_OFFSET = new BlockPos(-4, -1, -14);

    /** Eothar echo position relative to the MarawThar floor anchor. */
    public static final BlockPos MARAWTHAR_EOTHAR_OFFSET = new BlockPos(0, 2, 5);

    /**
     * Original boss-fight respawn points converted to floor-anchor-relative
     * offsets (floorAnchor + offset = respawn absolute coordinate).
     */
    public record ArenaRespawn(BlockPos offset, float yaw) {
    }

    public static final ArenaRespawn B1_RESPAWN = new ArenaRespawn(new BlockPos(-13, 1, 0), 270.0F);
    public static final ArenaRespawn B2_RESPAWN = new ArenaRespawn(new BlockPos(0, 0, -21), 0.0F);
    public static final ArenaRespawn B5_RESPAWN = new ArenaRespawn(new BlockPos(-17, 1, 0), 90.0F);
    public static final ArenaRespawn B8_RESPAWN = new ArenaRespawn(new BlockPos(14, 1, 0), 90.0F);
    public static final ArenaRespawn MARAWTHAR_RESPAWN = new ArenaRespawn(new BlockPos(-3, 1, 0), -90.0F);

    /** All arenas that can exist in one dimension at the same time. */
    public static final List<ArenaDefinition> ALL = List.of(B1, B2, MARAWTHAR, B5, B8);

    private static final Map<String, ArenaDefinition> BY_ID = Map.of(
            B1.id(), B1,
            B2.id(), B2,
            MARAWTHAR.id(), MARAWTHAR,
            "b9", MARAWTHAR,
            B5.id(), B5,
            B8.id(), B8);

    private ArenaDefinitions() {
    }

    public static Optional<ArenaDefinition> find(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static Optional<ArenaRespawn> respawnFor(ArenaDefinition definition) {
        if (definition == B1) return Optional.of(B1_RESPAWN);
        if (definition == B2) return Optional.of(B2_RESPAWN);
        if (definition == MARAWTHAR) return Optional.of(MARAWTHAR_RESPAWN);
        if (definition == B5) return Optional.of(B5_RESPAWN);
        if (definition == B8) return Optional.of(B8_RESPAWN);
        return Optional.empty();
    }
}
