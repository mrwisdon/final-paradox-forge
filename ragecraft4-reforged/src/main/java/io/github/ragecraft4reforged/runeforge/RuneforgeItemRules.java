package io.github.ragecraft4reforged.runeforge;

import io.github.ragecraft4reforged.registry.ModItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.tags.TagKey;

public final class RuneforgeItemRules {
    public static boolean isSupportedBase(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof SwordItem
                || item instanceof AxeItem
                || item instanceof PickaxeItem
                || item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof ShieldItem
                || item instanceof ElytraItem
                || item instanceof ArmorItem
                || java.util.Arrays.stream(PrefixTool.values()).anyMatch(tool -> tool.matches(stack));
    }

    public static boolean isValidForSlot(int slot, ItemStack stack) {
        return switch (slot) {
            case RuneforgeWorkbenchBlockEntity.BASE_SLOT -> isSupportedBase(stack);
            case RuneforgeWorkbenchBlockEntity.PREFIX_SLOT -> stack.getItem() instanceof PrefixSigilItem
                    || matchesCategory(stack, RuneCategory.PREFIX, ModItemTags.PREFIX_RUNES);
            case RuneforgeWorkbenchBlockEntity.UPGRADE_SLOT -> matchesCategory(stack, RuneCategory.UPGRADE, ModItemTags.UPGRADE_RUNES);
            case RuneforgeWorkbenchBlockEntity.SUFFIX_SLOT -> stack.getItem() instanceof SuffixSigilItem
                    || matchesCategory(stack, RuneCategory.SUFFIX, ModItemTags.SUFFIX_RUNES);
            default -> false;
        };
    }

    private static boolean matchesCategory(ItemStack stack, RuneCategory category, TagKey<Item> fallbackTag) {
        if (stack.getItem() instanceof RuneItem runeItem) {
            return runeItem.getCategory() == category;
        }
        return stack.is(fallbackTag);
    }

    private RuneforgeItemRules() {
    }
}
