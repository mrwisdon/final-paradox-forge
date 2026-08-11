package io.github.finalparadox.registry;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.arena.ArenaDimensionPolicy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class ModDimensions {
    /** Clean active dimension used by every sandbox and new encounter. */
    public static final ResourceKey<Level> ARENA_VOID = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(FinalParadox.MOD_ID, "arena_void"));
    /** Old flat/worldgen dimension retained so existing saves and players remain recoverable. */
    public static final ResourceKey<Level> LEGACY_ARENA_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(FinalParadox.MOD_ID, "arena_dimension"));

    /** Compatibility alias for code that means the active arena dimension. */
    public static final ResourceKey<Level> ARENA_DIMENSION = ARENA_VOID;

    public static boolean isArena(ResourceKey<Level> dimension) {
        return ArenaDimensionPolicy.isArenaId(dimension.location().toString());
    }

    public static boolean isActiveArena(ResourceKey<Level> dimension) {
        return ArenaDimensionPolicy.isActiveId(dimension.location().toString());
    }

    private ModDimensions() {
    }
}
