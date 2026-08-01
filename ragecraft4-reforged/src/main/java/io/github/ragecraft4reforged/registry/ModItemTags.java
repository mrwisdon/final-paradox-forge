package io.github.ragecraft4reforged.registry;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {
    public static final TagKey<Item> PREFIX_RUNES = create("runes/prefix");
    public static final TagKey<Item> UPGRADE_RUNES = create("runes/upgrade");
    public static final TagKey<Item> SUFFIX_RUNES = create("runes/suffix");

    private static TagKey<Item> create(String path) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(Ragecraft4Reforged.MOD_ID, path));
    }

    private ModItemTags() {
    }
}
