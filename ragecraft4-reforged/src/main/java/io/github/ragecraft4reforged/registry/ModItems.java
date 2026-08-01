package io.github.ragecraft4reforged.registry;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import io.github.ragecraft4reforged.content.AccessoryItem;
import io.github.ragecraft4reforged.content.EmptyInfinitePotionItem;
import io.github.ragecraft4reforged.content.InfinitePotionCatalog;
import io.github.ragecraft4reforged.content.InfinitePotionItem;
import io.github.ragecraft4reforged.content.MechanicsWikiItem;
import io.github.ragecraft4reforged.content.WandItem;
import io.github.ragecraft4reforged.runeforge.PrefixSigilItem;
import io.github.ragecraft4reforged.runeforge.PrefixTool;
import io.github.ragecraft4reforged.runeforge.RuneCatalog;
import io.github.ragecraft4reforged.runeforge.RuneDefinition;
import io.github.ragecraft4reforged.runeforge.RuneItem;
import io.github.ragecraft4reforged.runeforge.SuffixSchool;
import io.github.ragecraft4reforged.runeforge.SuffixSigilItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister<Item> REGISTER =
            DeferredRegister.create(ForgeRegistries.ITEMS, Ragecraft4Reforged.MOD_ID);

    public static final RegistryObject<Item> RUNEFORGE_WORKBENCH = REGISTER.register("runeforge_workbench", () ->
            new BlockItem(ModBlocks.RUNEFORGE_WORKBENCH.get(), new Item.Properties()));
    public static final RegistryObject<Item> MECHANICS_WIKI = REGISTER.register("mechanics_wiki", () ->
            new MechanicsWikiItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    /** All 284 rune items in their original prefix/upgrade/suffix and numeric order. */
    public static final Map<String, RegistryObject<Item>> ALL_RUNES = registerRunes();
    public static final Map<PrefixTool, RegistryObject<Item>> ALL_PREFIX_SIGILS = registerPrefixSigils();
    public static final Map<SuffixSchool, RegistryObject<Item>> ALL_SUFFIX_SIGILS = registerSuffixSigils();
    public static final RegistryObject<Item> UNENCHANTED_WAND = REGISTER.register("unenchanted_wand",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SEARING_WAND = wand("searing_wand", 1,
            List.of("item.carrot_on_a_stick.195.name.1"), wandLore("195", 1, 2, 3, 4, 5),
            List.of(wandModifier("generic.attack_damage", 5, "add"), wandModifier("generic.attack_speed", -2, "add")),
            List.of(enchantment("fire_aspect", 3), enchantment("knockback", 2)));
    public static final RegistryObject<Item> TELEPORT_WAND = wand("teleport_wand", 2,
            List.of("item.carrot_on_a_stick.5.name.2"), List.of(
                    "item.carrot_on_a_stick.243.lore.1.2", "item.carrot_on_a_stick.5.lore.2.1",
                    "item.carrot_on_a_stick.243.lore.3.1", "item.carrot_on_a_stick.243.lore.4.1"),
            List.of(wandModifier("generic.attack_damage", 5, "add"), wandModifier("generic.attack_speed", -1.8, "add"),
                    wandModifier("generic.movement_speed", 0.12, "multiply")),
            List.of(enchantment("knockback", 1)));
    public static final RegistryObject<Item> ARCTIC_WAND = wand("arctic_wand", 3,
            List.of("item.carrot_on_a_stick.241.name.1"), List.of(
                    "item.carrot_on_a_stick.241.lore.1.2", "item.carrot_on_a_stick.33.lore.2.1",
                    "item.carrot_on_a_stick.241.lore.3.1", "item.carrot_on_a_stick.125.lore.4.1",
                    "item.carrot_on_a_stick.241.lore.5.1", "item.lingering_potion.91.lore.3.1"),
            List.of(wandModifier("generic.attack_damage", 5, "add"), wandModifier("generic.attack_speed", -2.8, "add"),
                    wandModifier("generic.armor", 2, "add")), List.of(enchantment("knockback", 1)));
    public static final RegistryObject<Item> REAPING_WAND = wand("reaping_wand", 4,
            List.of("item.carrot_on_a_stick.287.name.1"), wandLore("287", 1, 2, 3, 4, 5),
            List.of(wandModifier("generic.attack_damage", 4, "add"), wandModifier("generic.attack_speed", -2, "add")),
            List.of(enchantment("knockback", 1), enchantment("looting", 2), enchantment("smite", 2)));
    public static final RegistryObject<Item> BLIGHTED_BRANCH = wand("blighted_branch", 5,
            List.of("item.carrot_on_a_stick.197.name.1"), wandLore("197", 1, 2, 3, 4, 5, 6),
            List.of(wandModifier("generic.attack_damage", 7, "add"), wandModifier("generic.attack_speed", -2.5, "add")),
            List.of(enchantment("bane_of_arthropods", 2), enchantment("knockback", 1)), "dis_vulnerability");
    public static final RegistryObject<Item> THUNDERFIST = wand("thunderfist", 6,
            List.of("item.carrot_on_a_stick.289.name.1"), wandLore("289", 1, 2, 3, 4, 5),
            List.of(wandModifier("generic.attack_damage", 5, "add"), wandModifier("generic.attack_speed", -1.8, "add"),
                    wandModifier("generic.movement_speed", 0.1, "multiply")), List.of(enchantment("knockback", 3)));
    public static final RegistryObject<Item> VOID_SCEPTER = wand("void_scepter", 7,
            List.of("item.carrot_on_a_stick.125.name.1"), List.of(
                    "item.carrot_on_a_stick.125.lore.1.2", "item.carrot_on_a_stick.33.lore.2.1",
                    "item.carrot_on_a_stick.125.lore.3.1", "item.carrot_on_a_stick.125.lore.4.1",
                    "item.carrot_on_a_stick.125.lore.5.1", "item.carrot_on_a_stick.125.lore.6.1",
                    "item.carrot_on_a_stick.33.lore.8.1"),
            List.of(wandModifier("generic.attack_damage", 7, "add"), wandModifier("generic.attack_speed", -2, "add")),
            List.of(enchantment("knockback", 1)), "dis_decay");
    public static final RegistryObject<Item> CORROSIVE_WAND = wand("corrosive_wand", 8,
            List.of("item.carrot_on_a_stick.123.name.1"), wandLore("123", 1, 2, 3, 4, 5, 6),
            List.of(wandModifier("generic.attack_damage", 8, "add"), wandModifier("generic.attack_speed", -2.2, "add"),
                    wandModifier("generic.armor", -3, "add")), List.of(enchantment("knockback", 1)));
    public static final RegistryObject<Item> INFERNAL_SCEPTER = wand("infernal_scepter", 9,
            List.of("item.carrot_on_a_stick.79.name.1"), wandLore("79", 1, 2, 3, 4, 5, 6, 7),
            List.of(wandModifier("generic.attack_damage", 8, "add"), wandModifier("generic.attack_speed", -2, "add")),
            List.of(enchantment("fire_aspect", 5), enchantment("knockback", 2)));
    public static final RegistryObject<Item> ICEBORNE_SCEPTER = wand("iceborne_scepter", 10,
            List.of("item.carrot_on_a_stick.31.name.1"), wandLore("31", 1, 2, 3, 4, 5, 6),
            List.of(wandModifier("generic.attack_damage", 8, "add"), wandModifier("generic.attack_speed", -2.6, "add")),
            List.of(enchantment("knockback", 1)));
    public static final RegistryObject<Item> EXALTED_WAND = wand("exalted_wand", 11,
            List.of("item.carrot_on_a_stick.33.name.1"), wandLore("33", 1, 2, 3, 4, 5, 6, 7, 8),
            List.of(wandModifier("generic.attack_damage", 8, "add"), wandModifier("generic.attack_speed", -2.2, "add")),
            List.of(enchantment("smite", 3)));
    public static final RegistryObject<Item> DOOMFIST = wand("doomfist", 12,
            List.of("item.carrot_on_a_stick.77.name.1"), wandLore("77", 1, 2, 3, 4, 5),
            List.of(wandModifier("generic.attack_damage", 12, "add"), wandModifier("generic.attack_speed", -3.2, "add")),
            List.of(enchantment("knockback", 2), enchantment("looting", 3)));
    public static final RegistryObject<Item> EMPOWERED_TELEPORT_WAND = wand("empowered_teleport_wand", 13,
            List.of("item.carrot_on_a_stick.169.name.1", "item.carrot_on_a_stick.5.name.2"), List.of(
                    "item.carrot_on_a_stick.169.lore.1.2", "item.carrot_on_a_stick.5.lore.2.1",
                    "item.carrot_on_a_stick.5.lore.3.1", "item.carrot_on_a_stick.169.lore.3.2",
                    "item.carrot_on_a_stick.5.lore.4.1", "item.carrot_on_a_stick.5.lore.4.2",
                    "item.carrot_on_a_stick.169.lore.5.1"),
            List.of(wandModifier("generic.attack_damage", 6, "add"), wandModifier("generic.attack_speed", -2, "add"),
                    wandModifier("generic.movement_speed", 0.15, "multiply")), List.of(enchantment("knockback", 2)));
    public static final RegistryObject<Item> SUPREME_TELEPORT_WAND = wand("supreme_teleport_wand", 14,
            List.of("item.carrot_on_a_stick.5.name.1", "item.carrot_on_a_stick.5.name.2"), List.of(
                    "item.carrot_on_a_stick.5.lore.1.2", "item.carrot_on_a_stick.5.lore.2.1",
                    "item.carrot_on_a_stick.5.lore.3.1", "item.carrot_on_a_stick.5.lore.3.2",
                    "item.carrot_on_a_stick.5.lore.4.1", "item.carrot_on_a_stick.5.lore.4.2",
                    "item.carrot_on_a_stick.5.lore.5.1", "item.carrot_on_a_stick.5.lore.5.2"),
            List.of(wandModifier("generic.attack_damage", 9, "add"), wandModifier("generic.attack_speed", -2, "add"),
                    wandModifier("generic.movement_speed", 0.2, "multiply")), List.of(enchantment("knockback", 3)));

    public static final List<RegistryObject<Item>> ALL_WANDS = List.of(SEARING_WAND, TELEPORT_WAND, ARCTIC_WAND,
            REAPING_WAND, BLIGHTED_BRANCH, THUNDERFIST, VOID_SCEPTER, CORROSIVE_WAND, INFERNAL_SCEPTER,
            ICEBORNE_SCEPTER, EXALTED_WAND, DOOMFIST, EMPOWERED_TELEPORT_WAND, SUPREME_TELEPORT_WAND);

    public static final RegistryObject<Item> DECORATED_ROCK = accessory("decorated_rock", List.of("granite_skin"), "91",
            List.of(), 2);
    public static final RegistryObject<Item> POISON_GLAND = accessory("poison_gland", List.of("toxicology"), "181",
            List.of(modifier("generic.max_health", 2, "add")), 2, 3);
    public static final RegistryObject<Item> LUMINOUS_RUBY = accessory("luminous_ruby", List.of("determination"), "1",
            List.of(modifier("generic.knockback_resistance", 0.2, "add")), 2, 3);
    public static final RegistryObject<Item> ANCIENT_SPELLBOOK = accessory("ancient_spellbook", List.of("spell_shield"), "460",
            List.of(modifier("generic.armor_toughness", 2, "add"), modifier("generic.movement_speed", 0.1, "multiply")), 2, 3);
    public static final RegistryObject<Item> BAG_OF_CRYSTALS = accessory("bag_of_crystals", List.of("deadly_shrapnel"), "151",
            List.of(modifier("generic.attack_speed", 0.2, "multiply")), 2, 3, 4);
    public static final RegistryObject<Item> BALL_LIGHTNING = accessory("ball_lightning", List.of("supercharged"), "580",
            List.of(modifier("generic.movement_speed", 0.2, "multiply"), modifier("generic.attack_speed", 0.2, "multiply")), 2, 3);
    public static final RegistryObject<Item> CELESTIAL_TORCH = accessory("celestial_torch", List.of("eternal_flame"), "31",
            List.of(modifier("generic.movement_speed", 0.15, "multiply")), 2, 3, 4);
    public static final RegistryObject<Item> EFFIGY_OF_PESTILENCE = accessory("effigy_of_pestilence", List.of("blight_orb"), "61",
            List.of(modifier("generic.attack_speed", 0.1, "multiply")), 2, 3, 4, 5, 6);
    public static final RegistryObject<Item> EMPYRIAN_IDOL = accessory("empyrian_idol", List.of("nature_blessing"), "241",
            List.of(modifier("generic.max_health", 4, "add")), 2);
    public static final RegistryObject<Item> GOLDEN_AEGIS = accessory("golden_aegis", List.of("divine_bulwark"), "430",
            "item.iron_horse_armor.1.lore.2.2",
            List.of(modifier("generic.armor_toughness", 3, "add"), modifier("generic.max_health", 2, "add")), 2, 3);
    public static final RegistryObject<Item> OBSIDIAN_PURSE = accessory("obsidian_purse", List.of("obsidian_shards"), "370",
            "item.iron_horse_armor.151.lore.2.2",
            List.of(modifier("generic.armor", 2, "add"), modifier("generic.armor_toughness", 4, "add")), 2, 3, 4, 5, 6, 7);
    public static final RegistryObject<Item> OCCULTISTS_GLOVE = accessory("occultists_glove", List.of("annihilating_curse"), "400",
            List.of(modifier("generic.attack_damage", 1, "add"), modifier("generic.movement_speed", 0.05, "multiply")), 2, 3, 4);
    public static final RegistryObject<Item> RADIANT_SAPPHIRE = accessory("radiant_sapphire", List.of("last_stand"), "340",
            List.of(modifier("generic.armor", 3, "add"), modifier("generic.knockback_resistance", 0.5, "add")), 2, 3);
    public static final RegistryObject<Item> SEARING_TOTEM = accessory("searing_totem", List.of("dis_flammability", "avatar_fire"), "310",
            List.of(modifier("generic.attack_speed", 0.15, "multiply"), modifier("generic.movement_speed", 0.1, "multiply")), 2);
    public static final RegistryObject<Item> VOID_SHARD = accessory("void_shard", List.of("shadow_spikes"), "211",
            "item.cyan_dye.31.lore.6.2", List.of(modifier("generic.attack_damage", 2, "add")), 2, 3, 4);
    public static final RegistryObject<Item> VOID_STONE = accessory("void_stone", List.of("call_void"), "610",
            List.of(modifier("generic.attack_damage", 3, "add")), 2, 3, 4, 5, 6, 7);
    public static final RegistryObject<Item> SHRINKHEAD = accessory("shrinkhead", List.of("summon_fangs"), "271",
            "item.cyan_dye.54.lore.6.2", List.of(modifier("generic.attack_speed", 0.15, "multiply")), 2, 3, 4);
    public static final RegistryObject<Item> HEART_OF_ICE = accessory("heart_of_ice", List.of("cold_as_ice"), "121",
            List.of(modifier("generic.attack_speed", -0.1, "multiply"), modifier("generic.armor_toughness", 4, "add")), 2, 3, 4, 5);

    public static final List<RegistryObject<Item>> ALL_ACCESSORIES = List.of(DECORATED_ROCK, POISON_GLAND,
            LUMINOUS_RUBY, ANCIENT_SPELLBOOK, BAG_OF_CRYSTALS, BALL_LIGHTNING, CELESTIAL_TORCH,
            EFFIGY_OF_PESTILENCE, EMPYRIAN_IDOL, GOLDEN_AEGIS, OBSIDIAN_PURSE, OCCULTISTS_GLOVE,
            RADIANT_SAPPHIRE, SEARING_TOTEM, VOID_SHARD, VOID_STONE, SHRINKHEAD, HEART_OF_ICE);

    /** The original 16 thrown potions followed by the four drinkable elixirs. */
    public static final Map<Integer, RegistryObject<Item>> ALL_INFINITE_POTIONS = registerInfinitePotions();
    public static final RegistryObject<Item> EMPTY_INFINITE_POTION = REGISTER.register("empty_infinite_potion",
            EmptyInfinitePotionItem::new);
    public static final RegistryObject<Item> EMPTY_INFINITE_ELIXIR = REGISTER.register("empty_infinite_elixir",
            EmptyInfinitePotionItem::new);

    // Stable aliases retained for saves and the first migration batch's Java call sites.
    public static final RegistryObject<Item> MASTERFUL_RUNE = rune("masterful_rune");
    public static final RegistryObject<Item> TRANSCENDANT_RUNE = rune("transcendant_rune");
    public static final RegistryObject<Item> TEMPLARS_RUNE = rune("templars_rune");
    public static final RegistryObject<Item> BLOODTHIRSTY_RUNE = rune("bloodthirsty_rune");
    public static final RegistryObject<Item> NECROMANCERS_RUNE = rune("necromancers_rune");
    public static final RegistryObject<Item> RUNE_OF_IMPACT = rune("rune_of_impact");
    public static final RegistryObject<Item> RUNE_OF_COWARDICE = rune("rune_of_cowardice");
    public static final RegistryObject<Item> RUNE_OF_TEMERITY = rune("rune_of_temerity");
    public static final RegistryObject<Item> RUNE_OF_ACCELERATION = rune("rune_of_acceleration");
    public static final RegistryObject<Item> RUNE_OF_IMPATIENCE = rune("rune_of_impatience");
    public static final RegistryObject<Item> RUNE_OF_TOUGHNESS = rune("rune_of_toughness");
    public static final RegistryObject<Item> RUNE_OF_VERSATILITY = rune("rune_of_versatility");
    public static final RegistryObject<Item> RUNE_OF_INERTIA = rune("rune_of_inertia");
    public static final RegistryObject<Item> RUNE_OF_INCONGRUITY = rune("rune_of_incongruity");
    public static final RegistryObject<Item> RUNE_OF_MALICE = rune("rune_of_malice");
    public static final RegistryObject<Item> RUNE_OF_MAGMA = rune("rune_of_magma");
    public static final RegistryObject<Item> RUNE_OF_EVISCERATION = rune("rune_of_evisceration");
    public static final RegistryObject<Item> RUNE_OF_TRAPPING = rune("rune_of_trapping");
    public static final RegistryObject<Item> RUNE_OF_DRAGONFIRE = rune("rune_of_dragonfire");
    public static final RegistryObject<Item> RUNE_OF_FROST = rune("rune_of_frost");

    private static Map<String, RegistryObject<Item>> registerRunes() {
        Map<String, RegistryObject<Item>> result = new LinkedHashMap<>();
        for (RuneDefinition definition : RuneCatalog.all()) {
            result.put(definition.registryName(), REGISTER.register(definition.registryName(), () -> new RuneItem(definition)));
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<SuffixSchool, RegistryObject<Item>> registerSuffixSigils() {
        Map<SuffixSchool, RegistryObject<Item>> result = new java.util.EnumMap<>(SuffixSchool.class);
        for (SuffixSchool school : SuffixSchool.values()) {
            result.put(school, REGISTER.register("suffix_sigil_" + school.id(),
                    () -> new SuffixSigilItem(school)));
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<PrefixTool, RegistryObject<Item>> registerPrefixSigils() {
        Map<PrefixTool, RegistryObject<Item>> result = new java.util.EnumMap<>(PrefixTool.class);
        for (PrefixTool tool : PrefixTool.values()) {
            result.put(tool, REGISTER.register("prefix_sigil_" + tool.id(),
                    () -> new PrefixSigilItem(tool)));
        }
        return Collections.unmodifiableMap(result);
    }

    public static Item runeItem(String registryName) {
        RegistryObject<Item> result = ALL_RUNES.get(registryName);
        if (result == null) {
            throw new IllegalArgumentException("Unknown rune item: " + registryName);
        }
        return result.get();
    }

    public static SuffixSigilItem suffixSigil(SuffixSchool school) {
        return (SuffixSigilItem) ALL_SUFFIX_SIGILS.get(school).get();
    }

    public static PrefixSigilItem prefixSigil(PrefixTool tool) {
        return (PrefixSigilItem) ALL_PREFIX_SIGILS.get(tool).get();
    }

    private static Map<Integer, RegistryObject<Item>> registerInfinitePotions() {
        Map<Integer, RegistryObject<Item>> result = new LinkedHashMap<>();
        InfinitePotionCatalog.ALL.forEach(definition -> result.put(definition.potionType(),
                REGISTER.register(definition.registryName(), () -> new InfinitePotionItem(definition))));
        return Collections.unmodifiableMap(result);
    }

    public static Item infinitePotion(int potionType) {
        RegistryObject<Item> result = ALL_INFINITE_POTIONS.get(potionType);
        if (result == null) {
            throw new IllegalArgumentException("Unknown infinite potion type: " + potionType);
        }
        return result.get();
    }

    private static RegistryObject<Item> rune(String name) {
        RegistryObject<Item> result = ALL_RUNES.get(name);
        if (result == null) {
            throw new IllegalStateException("Missing generated rune registration: " + name);
        }
        return result;
    }

    private static RegistryObject<Item> wand(String registryName, int spellId, List<String> nameKeys,
                                              List<String> lore, List<WandItem.Modifier> modifiers,
                                              List<WandItem.EnchantmentEntry> enchantments, String... abilityTags) {
        return REGISTER.register(registryName, () -> new WandItem(spellId, nameKeys, lore, modifiers,
                enchantments, List.of(abilityTags)));
    }

    private static List<String> wandLore(String translationId, int... loreLines) {
        return java.util.Arrays.stream(loreLines)
                .mapToObj(line -> "item.carrot_on_a_stick." + translationId + ".lore." + line
                        + (line == 1 ? ".2" : ".1"))
                .toList();
    }

    private static RegistryObject<Item> accessory(String registryName, List<String> tags, String translationId,
                                                    List<AccessoryItem.Modifier> modifiers, int... loreLines) {
        return accessory(registryName, tags, translationId,
                "item.iron_horse_armor." + translationId + ".lore.2.2", modifiers, loreLines);
    }

    private static RegistryObject<Item> accessory(String registryName, List<String> tags, String translationId,
                                                    String headingSuffixKey, List<AccessoryItem.Modifier> modifiers,
                                                    int... loreLines) {
        List<String> lore = java.util.Arrays.stream(loreLines)
                .mapToObj(line -> "item.iron_horse_armor." + translationId + ".lore." + line + ".1")
                .toList();
        return REGISTER.register(registryName, () -> new AccessoryItem(tags,
                "item.iron_horse_armor." + translationId + ".name.1", lore, headingSuffixKey, modifiers));
    }

    private static AccessoryItem.Modifier modifier(String attribute, double amount, String operation) {
        return new AccessoryItem.Modifier(net.minecraft.resources.ResourceLocation.tryParse(attribute), amount,
                "multiply".equals(operation)
                        ? net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
                        : net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION);
    }

    private static WandItem.Modifier wandModifier(String attribute, double amount, String operation) {
        return new WandItem.Modifier(net.minecraft.resources.ResourceLocation.tryParse(attribute), amount,
                "multiply".equals(operation)
                        ? net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
                        : net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION);
    }

    private static WandItem.EnchantmentEntry enchantment(String id, int level) {
        return new WandItem.EnchantmentEntry(net.minecraft.resources.ResourceLocation.tryParse(id), level);
    }

    private ModItems() {
    }
}
