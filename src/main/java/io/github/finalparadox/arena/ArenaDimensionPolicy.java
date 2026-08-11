package io.github.finalparadox.arena;

/** Bootstrap-free dimension identity policy, shared by runtime code and unit tests. */
public final class ArenaDimensionPolicy {
    public static final String ACTIVE_ID = "finalparadox:arena_void";
    public static final String LEGACY_ID = "finalparadox:arena_dimension";

    private ArenaDimensionPolicy() {
    }

    public static boolean isArenaId(String dimensionId) {
        return ACTIVE_ID.equals(dimensionId) || LEGACY_ID.equals(dimensionId);
    }

    public static boolean isActiveId(String dimensionId) {
        return ACTIVE_ID.equals(dimensionId);
    }
}
