package io.github.ragecraft4reforged.runeforge;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.Locale;

public enum SuffixSchool {
    ARCANE("arcane", 0x9A63FF),
    BLOOD("blood", 0xE33B3B),
    CHAOS("chaos", 0xE34ACF),
    FIRE("fire", 0xFF8A2B),
    HOLY("holy", 0xF4D36A),
    ICE("ice", 0x65DCE8),
    LIGHTNING("lightning", 0xF4E84A),
    POISON("poison", 0x75D94A);

    private final String id;
    private final int color;
    private final TagKey<EntityType<?>> sourceTag;

    SuffixSchool(String id, int color) {
        this.id = id;
        this.color = color;
        this.sourceTag = TagKey.create(Registries.ENTITY_TYPE,
                new ResourceLocation(Ragecraft4Reforged.MOD_ID, "sigil_sources/" + id));
    }

    public String id() {
        return id;
    }

    public int color() {
        return color;
    }

    public TagKey<EntityType<?>> sourceTag() {
        return sourceTag;
    }

    public static SuffixSchool fromDefinition(RuneDefinition definition) {
        String model = definition.iconModel();
        String name = model.substring(model.lastIndexOf('/') + 1)
                .toLowerCase(Locale.ROOT)
                .replaceFirst("^r_", "")
                .replaceFirst("_2$", "");
        for (SuffixSchool school : values()) {
            if (school.id.equals(name)) {
                return school;
            }
        }
        throw new IllegalArgumentException("Suffix rune has no recognized school: "
                + definition.registryName() + " (" + model + ")");
    }
}
