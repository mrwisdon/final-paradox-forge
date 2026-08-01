package io.github.ragecraft4reforged.registry;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/** Extensible target groups used by RC4 abilities instead of hard-coding vanilla monsters. */
public final class ModEntityTags {
    public static final TagKey<EntityType<?>> ABILITY_TARGETS = TagKey.create(
            Registries.ENTITY_TYPE, new ResourceLocation(Ragecraft4Reforged.MOD_ID, "ability_targets"));
    public static final TagKey<EntityType<?>> SIGIL_SOURCES = TagKey.create(
            Registries.ENTITY_TYPE, new ResourceLocation(Ragecraft4Reforged.MOD_ID, "sigil_sources"));

    private ModEntityTags() {
    }
}
