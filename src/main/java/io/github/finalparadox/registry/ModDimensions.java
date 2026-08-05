package io.github.finalparadox.registry;

import io.github.finalparadox.FinalParadox;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class ModDimensions {
    public static final ResourceKey<Level> ARENA_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(FinalParadox.MOD_ID, "arena_dimension"));

    private ModDimensions() {
    }
}
