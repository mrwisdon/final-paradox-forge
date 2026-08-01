package io.github.finalparadox.registry;

import io.github.finalparadox.FinalParadox;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;

public final class ModTags {
    private ModTags() {
    }

    public static final class EntityTypes {
        public static final TagKey<EntityType<?>> GLAIVORUS_TARGETS = TagKey.create(
                Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(FinalParadox.MOD_ID, "glaivorus_targets"));
        public static final TagKey<EntityType<?>> NIGHTFALL_TARGETS = TagKey.create(
                Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(FinalParadox.MOD_ID, "nightfall_targets"));

        private EntityTypes() {
        }
    }

    public static final class Biomes {
        public static final TagKey<Biome> SANCTUARY = TagKey.create(
                Registries.BIOME, ResourceLocation.fromNamespaceAndPath(FinalParadox.MOD_ID, "sanctuary"));

        private Biomes() {
        }
    }

    public static final class Items {
        public static final TagKey<Item> ECHOING_SHIELD_BREAKABLE_ARMOR = TagKey.create(
                Registries.ITEM, ResourceLocation.fromNamespaceAndPath(FinalParadox.MOD_ID, "echoing_shield_breakable_armor"));

        private Items() {}
    }
}
