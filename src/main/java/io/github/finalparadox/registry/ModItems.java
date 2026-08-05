package io.github.finalparadox.registry;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.item.GlaivorusItem;
import io.github.finalparadox.item.StygianPointItem;
import io.github.finalparadox.item.BouncingBootsItem;
import io.github.finalparadox.item.QueenBeeGreatbowItem;
import io.github.finalparadox.item.PaladinHammerItem;
import io.github.finalparadox.item.WindcutterItem;
import io.github.finalparadox.item.SacredShieldItem;
import io.github.finalparadox.item.LethalBloodDaggerItem;
import io.github.finalparadox.item.RapidBloodDaggerItem;
import io.github.finalparadox.item.PolymorphicInjectorItem;
import io.github.finalparadox.item.CosmicExtinctionHammerItem;
import io.github.finalparadox.item.ValyrianSteelToeCapsItem;
import io.github.finalparadox.item.LastSparkOfHopeItem;
import io.github.finalparadox.item.TyrannicalDecapitatorItem;
import io.github.finalparadox.item.SoullessEdgeItem;
import io.github.finalparadox.item.RepulsorGreatbowItem;
import io.github.finalparadox.item.KalamedThunderBowItem;
import io.github.finalparadox.item.ThornKnightAegisItem;
import io.github.finalparadox.item.FrostscaleLeggingsItem;
import io.github.finalparadox.item.PicomerangItem;
import io.github.finalparadox.item.TacheorosSoulSplitterItem;
import io.github.finalparadox.item.HarvesterScytheItem;
import io.github.finalparadox.item.DratagaItem;
import io.github.finalparadox.item.PumpkinMaulItem;
import io.github.finalparadox.item.VoidArmorItem;
import io.github.finalparadox.item.WinterLamentItem;
import io.github.finalparadox.item.RuthlessRipperItem;
import io.github.finalparadox.item.HeavyArbalestItem;
import io.github.finalparadox.item.ElectricHammerItem;
import io.github.finalparadox.item.GreatHookItem;
import io.github.finalparadox.item.BamboomerangItem;
import io.github.finalparadox.item.RuinCreatorItem;
import io.github.finalparadox.item.ArcaneMasterBladeItem;
import io.github.finalparadox.item.EctronChestplateItem;
import io.github.finalparadox.item.EchoingAmethystShieldItem;
import io.github.finalparadox.item.ProfaneArcaneBladeItem;
import io.github.finalparadox.item.PremiumSmugglingInsigniaItem;
import io.github.finalparadox.item.AdaptiveDefenseMatrixItem;
import io.github.finalparadox.item.KoyomiOmegaTridentItem;
import io.github.finalparadox.item.ReforgedOmegaTridentItem;
import io.github.finalparadox.item.StepsOfEotharItem;
import io.github.finalparadox.item.ApigloBladeItem;
import io.github.finalparadox.item.TharFragmentItem;
import io.github.finalparadox.item.NightfallItem;
import io.github.finalparadox.item.AtacromGauntletItem;
import io.github.finalparadox.item.ArenaCompassItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, FinalParadox.MOD_ID);

    public static final RegistryObject<Item> GLAIVORUS = REGISTER.register("glaivorus", GlaivorusItem::new);
    public static final RegistryObject<Item> STYGIAN_POINT = REGISTER.register("stygian_point", StygianPointItem::new);
    public static final RegistryObject<Item> BOUNCING_BOOTS = REGISTER.register("bouncing_boots", BouncingBootsItem::new);
    public static final RegistryObject<Item> QUEEN_BEE_GREATBOW = REGISTER.register("queen_bee_greatbow", QueenBeeGreatbowItem::new);
    public static final RegistryObject<Item> PALADIN_HAMMER = REGISTER.register("paladin_hammer", PaladinHammerItem::new);
    public static final RegistryObject<Item> WINDCUTTER = REGISTER.register("windcutter", WindcutterItem::new);
    public static final RegistryObject<Item> SACRED_SHIELD = REGISTER.register("sacred_shield", SacredShieldItem::new);
    public static final RegistryObject<Item> LETHAL_BLOOD_DAGGER = REGISTER.register("lethal_blood_dagger", LethalBloodDaggerItem::new);
    public static final RegistryObject<Item> RAPID_BLOOD_DAGGER = REGISTER.register("rapid_blood_dagger", RapidBloodDaggerItem::new);
    public static final RegistryObject<Item> POLYMORPHIC_INJECTOR = REGISTER.register("polymorphic_injector", PolymorphicInjectorItem::new);
    public static final RegistryObject<Item> COSMIC_EXTINCTION_HAMMER = REGISTER.register("cosmic_extinction_hammer", CosmicExtinctionHammerItem::new);
    public static final RegistryObject<Item> VALYRIAN_STEEL_TOE_CAPS = REGISTER.register("valyrian_steel_toe_caps", ValyrianSteelToeCapsItem::new);
    public static final RegistryObject<Item> LAST_SPARK_OF_HOPE = REGISTER.register("last_spark_of_hope", LastSparkOfHopeItem::new);
    public static final RegistryObject<Item> TYRANNICAL_DECAPITATOR = REGISTER.register("tyrannical_decapitator", TyrannicalDecapitatorItem::new);
    public static final RegistryObject<Item> SOULLESS_EDGE = REGISTER.register("soulless_edge", SoullessEdgeItem::new);
    public static final RegistryObject<Item> REPULSOR_GREATBOW = REGISTER.register("repulsor_greatbow", RepulsorGreatbowItem::new);
    public static final RegistryObject<Item> KALAMED_THUNDER_BOW = REGISTER.register("kalamed_thunder_bow", KalamedThunderBowItem::new);
    public static final RegistryObject<Item> THORN_KNIGHT_AEGIS = REGISTER.register("thorn_knight_aegis", ThornKnightAegisItem::new);
    public static final RegistryObject<Item> FROSTSCALE_LEGGINGS = REGISTER.register("frostscale_leggings", FrostscaleLeggingsItem::new);
    public static final RegistryObject<Item> PICOMERANG = REGISTER.register("picomerang", PicomerangItem::new);
    public static final RegistryObject<Item> TACHEOROS_SOUL_SPLITTER = REGISTER.register("tacheoros_soul_splitter", TacheorosSoulSplitterItem::new);
    public static final RegistryObject<Item> HARVESTER_SCYTHE = REGISTER.register("harvester_scythe", HarvesterScytheItem::new);
    public static final RegistryObject<Item> DRATAGA = REGISTER.register("drataga", DratagaItem::new);
    public static final RegistryObject<Item> PUMPKIN_MAUL = REGISTER.register("pumpkin_maul", PumpkinMaulItem::new);
    public static final RegistryObject<Item> VOID_ARMOR = REGISTER.register("void_armor", VoidArmorItem::new);
    public static final RegistryObject<Item> WINTER_LAMENT = REGISTER.register("winter_lament", WinterLamentItem::new);
    public static final RegistryObject<Item> RUTHLESS_RIPPER = REGISTER.register("ruthless_ripper", RuthlessRipperItem::new);
    public static final RegistryObject<Item> HEAVY_ARBALEST = REGISTER.register("heavy_arbalest", HeavyArbalestItem::new);
    public static final RegistryObject<Item> ELECTRIC_HAMMER = REGISTER.register("electric_hammer", ElectricHammerItem::new);
    public static final RegistryObject<Item> GREAT_HOOK = REGISTER.register("great_hook", GreatHookItem::new);
    public static final RegistryObject<Item> BAMBOOMERANG = REGISTER.register("bamboomerang", BamboomerangItem::new);
    public static final RegistryObject<Item> RUIN_CREATOR = REGISTER.register("ruin_creator", RuinCreatorItem::new);
    public static final RegistryObject<Item> ARCANE_MASTER_BLADE = REGISTER.register("arcane_master_blade", ArcaneMasterBladeItem::new);
    public static final RegistryObject<Item> ECTRON_CHESTPLATE = REGISTER.register("ectron_chestplate", EctronChestplateItem::new);
    public static final RegistryObject<Item> ECHOING_AMETHYST_SHIELD = REGISTER.register("echoing_amethyst_shield", EchoingAmethystShieldItem::new);
    public static final RegistryObject<Item> PROFANE_ARCANE_BLADE = REGISTER.register("profane_arcane_blade", ProfaneArcaneBladeItem::new);
    public static final RegistryObject<Item> PREMIUM_SMUGGLING_INSIGNIA = REGISTER.register("premium_smuggling_insignia", PremiumSmugglingInsigniaItem::new);
    public static final RegistryObject<Item> ADAPTIVE_DEFENSE_MATRIX = REGISTER.register("adaptive_defense_matrix", AdaptiveDefenseMatrixItem::new);
    public static final RegistryObject<Item> KOYOMI_OMEGA_TRIDENT = REGISTER.register("koyomi_omega_trident", KoyomiOmegaTridentItem::new);
    public static final RegistryObject<Item> STEPS_OF_EOTHAR = REGISTER.register("steps_of_eothar", StepsOfEotharItem::new);
    public static final RegistryObject<Item> REFORGED_OMEGA_TRIDENT = REGISTER.register("reforged_omega_trident", ReforgedOmegaTridentItem::new);
    public static final RegistryObject<Item> APIGLO_BLADE = REGISTER.register("apiglo_blade", ApigloBladeItem::new);
    public static final RegistryObject<Item> THAR_FRAGMENT = REGISTER.register("thar_fragment", TharFragmentItem::new);
    public static final RegistryObject<Item> NIGHTFALL = REGISTER.register("nightfall", NightfallItem::new);
    public static final RegistryObject<Item> ARENA_COMPASS = REGISTER.register("arena_compass", ArenaCompassItem::new);
    public static final RegistryObject<Item> ATACROM_GAUNTLET =
            REGISTER.register("atacrom_gauntlet", AtacromGauntletItem::new);
    /** Renderer-only handheld sprite used while the guard window is active. */
    public static final RegistryObject<Item> ATACROM_GAUNTLET_DISPLAY =
            REGISTER.register("atacrom_gauntlet_display", () -> new Item(new Item.Properties()));
    /** Renderer-only coherent 3D visual proxy; not exposed in the creative tab. */
    public static final RegistryObject<Item> NIGHTFALL_ICON = REGISTER.register("nightfall_icon",
            () -> new Item(new Item.Properties()));
    /** Renderer-only source sprite for inventory slots; not exposed in the creative tab. */
    public static final RegistryObject<Item> NIGHTFALL_GUI_ICON = REGISTER.register("nightfall_gui_icon",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> APIGLO_SPAWN_EGG = REGISTER.register("apiglo_spawn_egg", () ->
            new ForgeSpawnEggItem(ModEntities.APIGLO, 0x9A5D24, 0xE4B13C, new Item.Properties()));
    public static final RegistryObject<Item> THAR_KROO_SPAWN_EGG = REGISTER.register("thar_kroo_spawn_egg", () ->
            new ForgeSpawnEggItem(ModEntities.THAR_KROO, 0x1F1F1F, 0xB72323, new Item.Properties()));
    public static final RegistryObject<Item> CONQUEROR_SHADOW_SPAWN_EGG = REGISTER.register(
            "conqueror_shadow_spawn_egg", () ->
                    new ForgeSpawnEggItem(ModEntities.CONQUEROR_SHADOW, 0x202020, 0xD10000,
                            new Item.Properties()));
    public static final RegistryObject<Item> KOYOMI_SPAWN_EGG = REGISTER.register("koyo_spawn_egg", () ->
            new ForgeSpawnEggItem(ModEntities.KOYOMI, 0x509E63, 0x2E5E3A, new Item.Properties()));
    public static final RegistryObject<Item> GARI_SPAWN_EGG = REGISTER.register("gari_spawn_egg", () ->
            new ForgeSpawnEggItem(ModEntities.GARI, 0x39434D, 0x8B9AA6, new Item.Properties()));

    private ModItems() {
    }
}
