package io.github.ragecraft4reforged.content;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Original potion_type order, colors, names, and lore extracted from slot_0_refill.mcfunction. */
public final class InfinitePotionCatalog {
    private static final int RARE_NAME = 0x7100BD;
    private static final int EPIC_NAME = 0xD700E6;

    public static final List<InfinitePotionDefinition> ALL = List.of(
            lingering("paralyzing_potion", 1, 4_999_042, RARE_NAME, "301", 1,
                    List.of("item.lingering_potion.301.lore.1.1", "item.lingering_potion.301.lore.2.1",
                            "item.lingering_potion.91.lore.3.1"),
                    List.of("item.lingering_potion.301.lore.8.1", "item.lingering_potion.301.lore.9.1")),
            lingering("holy_water", 2, 16_486_911, EPIC_NAME, "151", 2,
                    List.of("item.lingering_potion.1.lore.2.1", "item.lingering_potion.1.lore.3.1",
                            "item.lingering_potion.1.lore.4.1"), List.of("item.lingering_potion.1.lore.9.1")),
            lingering("arcane_grenade", 3, 8_854_556, RARE_NAME, "121", 3,
                    List.of("item.lingering_potion.121.lore.1.1", "item.lingering_potion.121.lore.2.1",
                            "item.lingering_potion.121.lore.3.1"), List.of("item.lingering_potion.121.lore.8.1")),
            lingering("toxic_cocktail", 4, 4_612_382, RARE_NAME, "91", 4,
                    List.of("item.lingering_potion.91.lore.1.1", "item.lingering_potion.91.lore.2.1",
                            "item.lingering_potion.91.lore.3.1"), List.of("item.lingering_potion.91.lore.8.1")),
            lingering("frostburn_potion", 5, 13_626_879, RARE_NAME, "61", 5,
                    List.of("item.lingering_potion.61.lore.1.1", "item.lingering_potion.61.lore.2.1",
                            "item.lingering_potion.61.lore.3.1", "item.lingering_potion.61.lore.4.1"),
                    List.of("item.lingering_potion.61.lore.9.1")),
            lingering("doom_in_a_bottle", 6, 657_674, RARE_NAME, "181", 6,
                    List.of("item.lingering_potion.181.lore.1.1", "item.lingering_potion.181.lore.2.1"),
                    List.of("item.lingering_potion.181.lore.7.1")),
            lingering("cursed_elixir", 7, 6_703_237, RARE_NAME, "331", 7,
                    List.of("item.lingering_potion.331.lore.1.1", "item.lingering_potion.331.lore.2.1",
                            "item.lingering_potion.331.lore.3.1", "item.lingering_potion.331.lore.4.1",
                            "item.lingering_potion.181.lore.2.1"),
                    List.of("item.lingering_potion.331.lore.10.1", "item.lingering_potion.331.lore.11.1")),
            lingering("stink_bomb", 8, 7_049_027, RARE_NAME, "271", 8,
                    List.of("item.lingering_potion.271.lore.1.1", "item.lingering_potion.271.lore.2.1",
                            "item.lingering_potion.271.lore.3.1"), List.of("item.lingering_potion.271.lore.8.1")),
            lingering("unholy_blood", 9, 5_048_328, RARE_NAME, "392", 9,
                    List.of("item.lingering_potion.392.lore.1.1", "item.lingering_potion.392.lore.2.1",
                            "skills.functions.potions.slot_0_refill.89"), List.of("item.lingering_potion.392.lore.8.1")),
            lingering("dreadfire_concentrate", 10, 12_871_950, RARE_NAME, "424", 10,
                    List.of("item.lingering_potion.424.lore.1.1", "item.lingering_potion.424.lore.2.1",
                            "item.lingering_potion.181.lore.2.1"), List.of("item.lingering_potion.424.lore.8.1")),
            lingering("cyclone_in_a_bottle", 11, 9_739_427, RARE_NAME, "454", 11,
                    List.of("item.lingering_potion.454.lore.1.1", "item.lingering_potion.454.lore.2.1",
                            "item.lingering_potion.454.lore.3.1", "item.lingering_potion.91.lore.3.1"),
                    List.of("item.lingering_potion.454.lore.9.1")),
            lingering("sapping_potion", 12, 13_544_163, RARE_NAME, "361", 12,
                    List.of("item.lingering_potion.361.lore.1.1", "item.lingering_potion.361.lore.2.1",
                            "item.lingering_potion.361.lore.3.1"), List.of("item.lingering_potion.361.lore.8.1")),
            lingering("bottled_lightning", 13, 15_130_533, RARE_NAME, "484", 13,
                    List.of("item.lingering_potion.484.lore.1.1", "item.lingering_potion.484.lore.2.1",
                            "item.lingering_potion.484.lore.3.1", "item.lingering_potion.484.lore.4.1"),
                    List.of("item.lingering_potion.484.lore.9.1", "item.lingering_potion.484.lore.10.1")),
            lingering("liquid_nitrogen", 20, 8_561_663, RARE_NAME, "211", 14,
                    List.of("item.lingering_potion.211.lore.1.1", "item.lingering_potion.211.lore.2.1",
                            "item.lingering_potion.211.lore.3.1"), List.of("item.lingering_potion.211.lore.8.1")),
            lingering("bug_spray", 21, 12_910_548, RARE_NAME, "31", 15,
                    List.of("item.lingering_potion.31.lore.1.1", "item.lingering_potion.31.lore.2.1",
                            "item.lingering_potion.31.lore.3.1"), List.of("item.lingering_potion.31.lore.8.1")),
            lingering("exalted_holy_water", 22, 16_486_911, EPIC_NAME, "1", 16,
                    List.of("item.lingering_potion.1.lore.1.1", "item.lingering_potion.1.lore.2.1",
                            "item.lingering_potion.1.lore.3.1", "item.lingering_potion.1.lore.4.1"),
                    List.of("item.lingering_potion.1.lore.9.1")),
            drink("rock_ale", 14, 16_777_082, "2", List.of(
                    "item.potion.2.lore.1.1", "item.lingering_potion.1.lore.5.1",
                    "item.potion.2.lore.3.1", "wtem.space", "item.potion.2.lore.5.1")),
            drink("mana_elixir", 15, 2_533_631, "35", List.of(
                    "item.potion.35.lore.1.1", "item.lingering_potion.1.lore.5.1",
                    "item.lingering_potion.1.lore.6.1", "item.lingering_potion.1.lore.7.1",
                    "wtem.space", "item.potion.35.lore.6.1")),
            drink("metamorph_potion", 16, 7_841_345, "70", List.of(
                    "item.potion.70.lore.1.1", "item.lingering_potion.1.lore.5.1",
                    "item.lingering_potion.1.lore.6.1", "item.lingering_potion.1.lore.7.1",
                    "wtem.space", "item.potion.70.lore.6.1")),
            drink("concentrated_adrenaline", 17, 12_841_719, "107", List.of(
                    "item.potion.107.lore.1.1", "item.potion.107.lore.2.1",
                    "item.lingering_potion.1.lore.5.1", "item.lingering_potion.1.lore.6.1",
                    "item.lingering_potion.1.lore.7.1", "wtem.space", "item.potion.107.lore.7.1"))
    );

    public static final Map<Integer, InfinitePotionDefinition> BY_TYPE = byType();

    private static InfinitePotionDefinition lingering(String registryName, int type, int color, int nameColor,
                                                       String translationId, int triggerIndex,
                                                       List<String> effectLore, List<String> flavorLore) {
        List<String> lore = new ArrayList<>();
        lore.add("item.lingering_potion.1.lore.0.1");
        lore.addAll(effectLore);
        lore.add("item.lingering_potion.1.lore.5.1");
        lore.add("item.lingering_potion.1.lore.6.1");
        lore.add("item.lingering_potion.1.lore.7.1");
        lore.add("wtem.space");
        lore.addAll(flavorLore);
        return new InfinitePotionDefinition(registryName, type, false, color, nameColor,
                "item.lingering_potion." + translationId + ".name.1", lore, triggerIndex);
    }

    private static InfinitePotionDefinition drink(String registryName, int type, int color, String translationId,
                                                   List<String> remainingLore) {
        List<String> lore = new ArrayList<>();
        lore.add("item.potion.2.lore.0.1");
        lore.addAll(remainingLore);
        return new InfinitePotionDefinition(registryName, type, true, color, EPIC_NAME,
                "item.potion." + translationId + ".name.1", lore, type + 6);
    }

    private static Map<Integer, InfinitePotionDefinition> byType() {
        Map<Integer, InfinitePotionDefinition> result = new LinkedHashMap<>();
        ALL.forEach(definition -> result.put(definition.potionType(), definition));
        return Map.copyOf(result);
    }

    private InfinitePotionCatalog() {
    }
}
