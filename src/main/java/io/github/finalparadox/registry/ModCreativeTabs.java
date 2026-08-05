package io.github.finalparadox.registry;

import io.github.finalparadox.FinalParadox;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FinalParadox.MOD_ID);

    public static final RegistryObject<CreativeModeTab> FORGED_ITEMS = REGISTER.register("forged_items", () ->
            CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .title(Component.translatable("creativetab.finalparadox.forged_items"))
                    .icon(() -> ModItems.GLAIVORUS.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        // Keep the datapack forge order and use each item's initialized default stack.
                        output.accept(ModItems.GLAIVORUS.get().getDefaultInstance());
                        output.accept(ModItems.STYGIAN_POINT.get().getDefaultInstance());
                        output.accept(ModItems.BOUNCING_BOOTS.get().getDefaultInstance());
                        output.accept(ModItems.QUEEN_BEE_GREATBOW.get().getDefaultInstance());
                        output.accept(ModItems.PALADIN_HAMMER.get().getDefaultInstance());
                        output.accept(ModItems.WINDCUTTER.get().getDefaultInstance());
                        output.accept(ModItems.SACRED_SHIELD.get().getDefaultInstance());
                        output.accept(ModItems.LETHAL_BLOOD_DAGGER.get().getDefaultInstance());
                        output.accept(ModItems.RAPID_BLOOD_DAGGER.get().getDefaultInstance());
                        output.accept(ModItems.POLYMORPHIC_INJECTOR.get().getDefaultInstance());
                        output.accept(ModItems.COSMIC_EXTINCTION_HAMMER.get().getDefaultInstance());
                        output.accept(ModItems.VALYRIAN_STEEL_TOE_CAPS.get().getDefaultInstance());
                        output.accept(ModItems.LAST_SPARK_OF_HOPE.get().getDefaultInstance());
                        output.accept(ModItems.TYRANNICAL_DECAPITATOR.get().getDefaultInstance());
                        output.accept(ModItems.SOULLESS_EDGE.get().getDefaultInstance());
                        output.accept(ModItems.REPULSOR_GREATBOW.get().getDefaultInstance());
                        output.accept(ModItems.KALAMED_THUNDER_BOW.get().getDefaultInstance());
                        output.accept(ModItems.THORN_KNIGHT_AEGIS.get().getDefaultInstance());
                        output.accept(ModItems.FROSTSCALE_LEGGINGS.get().getDefaultInstance());
                        output.accept(ModItems.PICOMERANG.get().getDefaultInstance());
                        output.accept(ModItems.TACHEOROS_SOUL_SPLITTER.get().getDefaultInstance());
                        output.accept(ModItems.HARVESTER_SCYTHE.get().getDefaultInstance());
                        output.accept(ModItems.DRATAGA.get().getDefaultInstance());
                        output.accept(ModItems.PUMPKIN_MAUL.get().getDefaultInstance());
                        output.accept(ModItems.VOID_ARMOR.get().getDefaultInstance());
                        output.accept(ModItems.WINTER_LAMENT.get().getDefaultInstance());
                        output.accept(ModItems.RUTHLESS_RIPPER.get().getDefaultInstance());
                        output.accept(ModItems.HEAVY_ARBALEST.get().getDefaultInstance());
                        output.accept(ModItems.ELECTRIC_HAMMER.get().getDefaultInstance());
                        output.accept(ModItems.RUIN_CREATOR.get().getDefaultInstance());
                        output.accept(ModItems.ARCANE_MASTER_BLADE.get().getDefaultInstance());
                        output.accept(ModItems.ECTRON_CHESTPLATE.get().getDefaultInstance());
                        output.accept(ModItems.ECHOING_AMETHYST_SHIELD.get().getDefaultInstance());
                        output.accept(ModItems.PROFANE_ARCANE_BLADE.get().getDefaultInstance());
                        output.accept(ModItems.PREMIUM_SMUGGLING_INSIGNIA.get().getDefaultInstance());
                        output.accept(ModItems.ADAPTIVE_DEFENSE_MATRIX.get().getDefaultInstance());
                        output.accept(ModItems.KOYOMI_OMEGA_TRIDENT.get().getDefaultInstance());
                        output.accept(ModItems.STEPS_OF_EOTHAR.get().getDefaultInstance());
                        output.accept(ModItems.REFORGED_OMEGA_TRIDENT.get().getDefaultInstance());
                        output.accept(ModItems.GREAT_HOOK.get().getDefaultInstance());
                        output.accept(ModItems.BAMBOOMERANG.get().getDefaultInstance());
                        output.accept(ModItems.NIGHTFALL.get().getDefaultInstance());
                        output.accept(ModItems.ARENA_COMPASS.get().getDefaultInstance());
                        output.accept(ModItems.ATACROM_GAUNTLET.get().getDefaultInstance());
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
