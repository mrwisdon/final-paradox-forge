package io.github.ragecraft4reforged.registry;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import io.github.ragecraft4reforged.runeforge.PrefixTool;
import io.github.ragecraft4reforged.runeforge.RuneCategory;
import io.github.ragecraft4reforged.runeforge.RuneItem;
import io.github.ragecraft4reforged.runeforge.SuffixSchool;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Comparator;
import java.util.stream.Stream;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ragecraft4Reforged.MOD_ID);

    public static final RegistryObject<CreativeModeTab> RUNEFORGING = REGISTER.register("runeforging", () ->
            CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .title(Component.translatable("creativetab.ragecraft4reforged.runeforging"))
                    .icon(() -> categoryIcon(RuneCategory.PREFIX))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.MECHANICS_WIKI.get());
                        output.accept(ModItems.RUNEFORGE_WORKBENCH.get());
                        for (PrefixTool tool : PrefixTool.values()) {
                            output.accept(ModItems.prefixSigil(tool));
                        }
                        addRunes(output, RuneCategory.PREFIX);
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> UPGRADE_RUNES = REGISTER.register("upgrade_runes", () ->
            CreativeModeTab.builder()
                    .withTabsAfter(RUNEFORGING.getKey())
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .title(Component.translatable("creativetab.ragecraft4reforged.upgrade_runes"))
                    .icon(() -> categoryIcon(RuneCategory.UPGRADE))
                    .displayItems((parameters, output) -> addRunes(output, RuneCategory.UPGRADE))
                    .build());

    public static final RegistryObject<CreativeModeTab> SUFFIX_RUNES = REGISTER.register("suffix_runes", () ->
            CreativeModeTab.builder()
                    .withTabsAfter(UPGRADE_RUNES.getKey())
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .title(Component.translatable("creativetab.ragecraft4reforged.suffix_runes"))
                    .icon(() -> categoryIcon(RuneCategory.SUFFIX))
                    .displayItems((parameters, output) -> {
                        for (SuffixSchool school : SuffixSchool.values()) {
                            output.accept(ModItems.suffixSigil(school));
                        }
                        addRunes(output, RuneCategory.SUFFIX);
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> WANDS_AND_ACCESSORIES = REGISTER.register("wands_and_accessories", () ->
            CreativeModeTab.builder()
                    .withTabsAfter(SUFFIX_RUNES.getKey())
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .title(Component.translatable("creativetab.ragecraft4reforged.wands_and_accessories"))
                    .icon(() -> ModItems.SEARING_WAND.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.UNENCHANTED_WAND.get());
                        ModItems.ALL_WANDS.forEach(item -> output.accept(item.get().getDefaultInstance()));
                        ModItems.ALL_ACCESSORIES.forEach(item -> output.accept(item.get().getDefaultInstance()));
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> INFINITE_POTIONS = REGISTER.register("infinite_potions", () ->
            CreativeModeTab.builder()
                    .withTabsAfter(WANDS_AND_ACCESSORIES.getKey())
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .title(Component.translatable("creativetab.ragecraft4reforged.infinite_potions"))
                    .icon(() -> ModItems.infinitePotion(1).getDefaultInstance())
                    .displayItems((parameters, output) -> ModItems.ALL_INFINITE_POTIONS.values()
                            .forEach(item -> output.accept(item.get().getDefaultInstance())))
                    .build());

    public static final RegistryObject<CreativeModeTab> ENCHANTMENTS_AND_EFFECTS = REGISTER.register(
            "enchantments_and_effects", () -> CreativeModeTab.builder()
                    .withTabsAfter(INFINITE_POTIONS.getKey())
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .title(Component.translatable("creativetab.ragecraft4reforged.enchantments_and_effects"))
                    .icon(() -> enchantedBook(ModEnchantments.VOLLEY.get(), 2))
                    .displayItems((parameters, output) -> ModEnchantments.ALL.forEach(entry -> {
                        Enchantment enchantment = entry.get();
                        for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
                            output.accept(enchantedBook(enchantment, level));
                        }
                    }))
                    .build());

    private static void addRunes(CreativeModeTab.Output output, RuneCategory category) {
        categoryRunes(category).forEach(output::accept);
    }

    private static Stream<RuneItem> categoryRunes(RuneCategory category) {
        Stream<RuneItem> runes = ModItems.ALL_RUNES.values().stream()
                .map(RegistryObject::get)
                .filter(RuneItem.class::isInstance)
                .map(RuneItem.class::cast)
                .filter(rune -> rune.getCategory() == category);
        if (category == RuneCategory.SUFFIX) {
            return runes.sorted(Comparator
                    .comparingInt((RuneItem rune) -> rune.getDefinition().originalCustomModelData())
                    .thenComparingInt(rune -> rune.getDefinition().sourceId()));
        }
        return runes;
    }

    private static ItemStack categoryIcon(RuneCategory category) {
        return categoryRunes(category)
                .findFirst()
                .map(Item::getDefaultInstance)
                .orElseGet(() -> ModItems.RUNEFORGE_WORKBENCH.get().getDefaultInstance());
    }

    private static ItemStack enchantedBook(Enchantment enchantment, int level) {
        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
    }

    private ModCreativeTabs() {
    }
}
