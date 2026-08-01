package io.github.ragecraft4reforged.runeforge;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import io.github.ragecraft4reforged.registry.ModEnchantments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Map;

public final class RuneforgeData {
    public static final String ROOT = "Ragecraft4Reforged";
    private static final String STATS = "Stats";
    private static final String ORIGINAL_NAME = "OriginalName";
    private static final String REPAIR_COST = "RepairCost";

    public static boolean has(ItemStack stack, RuneCategory category) {
        return getId(stack, category) != null;
    }

    @Nullable
    public static ResourceLocation getId(ItemStack stack, RuneCategory category) {
        CompoundTag root = stack.getTagElement(ROOT);
        if (root == null || !root.contains(key(category))) {
            return null;
        }
        return ResourceLocation.tryParse(root.getString(key(category)));
    }

    @Nullable
    public static Item getRune(ItemStack stack, RuneCategory category) {
        ResourceLocation id = getId(stack, category);
        return id == null ? null : ForgeRegistries.ITEMS.getValue(id);
    }

    public static void set(ItemStack stack, RuneItem rune) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(rune);
        if (id != null && Ragecraft4Reforged.MOD_ID.equals(id.getNamespace())) {
            stack.getOrCreateTagElement(ROOT).putString(key(rune.getCategory()), id.toString());
        }
    }

    public static void rememberOriginalName(ItemStack stack) {
        CompoundTag root = stack.getOrCreateTagElement(ROOT);
        if (!root.contains(ORIGINAL_NAME)) {
            root.putString(ORIGINAL_NAME, Component.Serializer.toJson(stack.getHoverName()));
        }
    }

    public static Component getOriginalName(ItemStack stack) {
        CompoundTag root = stack.getTagElement(ROOT);
        if (root != null && root.contains(ORIGINAL_NAME)) {
            Component parsed = Component.Serializer.fromJson(root.getString(ORIGINAL_NAME));
            if (parsed != null) {
                return parsed;
            }
        }
        return stack.getHoverName();
    }

    public static void addStat(ItemStack stack, String name, int amount) {
        CompoundTag stats = stack.getOrCreateTagElement(ROOT).getCompound(STATS);
        stats.putInt(name, stats.getInt(name) + amount);
        stack.getOrCreateTagElement(ROOT).put(STATS, stats);
    }

    public static int getRepairCost(ItemStack stack) {
        CompoundTag root = stack.getTagElement(ROOT);
        if (root != null && root.contains(REPAIR_COST)) {
            return Math.max(0, root.getInt(REPAIR_COST));
        }
        if (stack.hasTag() && stack.getTag().contains("repair_cost")) {
            return Math.max(0, stack.getTag().getInt("repair_cost"));
        }

        long inferred = 0;
        for (RuneCategory category : RuneCategory.values()) {
            RuneDefinition definition = getDefinition(stack, category);
            if (definition != null) {
                inferred += definition.cost();
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, inferred);
    }

    public static void addRepairCost(ItemStack stack, int amount) {
        if (amount <= 0) {
            return;
        }
        CompoundTag root = stack.getOrCreateTagElement(ROOT);
        long updated = (long) getRepairCost(stack) + amount;
        root.putInt(REPAIR_COST, (int) Math.min(Integer.MAX_VALUE, updated));
    }

    public static void applyRuntimeTags(ItemStack stack) {
        CompoundTag root = stack.getTagElement(ROOT);
        if (root == null) {
            return;
        }
        for (RuneCategory category : RuneCategory.values()) {
            RuneDefinition definition = getDefinition(stack, category);
            if (definition != null && !definition.abilityTag().isEmpty()) {
                stack.getOrCreateTag().putBoolean(definition.abilityTag(), true);
            }
        }
        CompoundTag stats = root.getCompound(STATS);
        Map<Enchantment, Integer> enchantments = null;
        for (String name : stats.getAllKeys()) {
            int level = stats.getInt(name);
            Enchantment customEnchantment = ModEnchantments.byStat(name);
            if (customEnchantment == null) {
                stack.getOrCreateTag().putInt(name, level);
                continue;
            }

            // Old RC4 maps used root-level byte/int tags as enchantment stand-ins. Once a real
            // enchantment exists those tags would make the datapack and Java paths trigger twice.
            stack.getOrCreateTag().remove(name);
            if (EnchantmentHelper.getItemEnchantmentLevel(customEnchantment, stack) < level) {
                if (enchantments == null) {
                    enchantments = EnchantmentHelper.getEnchantments(stack);
                }
                enchantments.merge(customEnchantment, level, Math::max);
            }
        }
        if (enchantments != null) {
            EnchantmentHelper.setEnchantments(enchantments, stack);
        }
    }

    public static int getStat(ItemStack stack, String name) {
        CompoundTag root = stack.getTagElement(ROOT);
        return root == null ? 0 : root.getCompound(STATS).getInt(name);
    }

    @Nullable
    public static RuneDefinition getDefinition(ItemStack stack, RuneCategory category) {
        Item item = getRune(stack, category);
        return item instanceof RuneItem rune ? rune.getDefinition() : null;
    }

    public static boolean is(ItemStack stack, RuneCategory category, String runePath) {
        ResourceLocation id = getId(stack, category);
        return id != null && Ragecraft4Reforged.MOD_ID.equals(id.getNamespace()) && runePath.equals(id.getPath());
    }

    private static String key(RuneCategory category) {
        return switch (category) {
            case PREFIX -> "Prefix";
            case UPGRADE -> "Upgrade";
            case SUFFIX -> "Suffix";
        };
    }

    private RuneforgeData() {
    }
}
