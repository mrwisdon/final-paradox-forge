package io.github.ragecraft4reforged.content;

import java.util.List;

/** Immutable reconstruction data for one of Ragecraft IV's reusable potions. */
public record InfinitePotionDefinition(String registryName, int potionType, boolean drinkable,
                                       int liquidColor, int nameColor, String nameKey,
                                       List<String> loreKeys, int triggerIndex) {
    public InfinitePotionDefinition {
        loreKeys = List.copyOf(loreKeys);
    }
}
