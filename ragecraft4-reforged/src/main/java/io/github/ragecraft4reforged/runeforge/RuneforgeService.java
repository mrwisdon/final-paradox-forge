package io.github.ragecraft4reforged.runeforge;

import io.github.ragecraft4reforged.registry.ModEnchantments;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RuneforgeService {
    private static final int HIDE_ARMOR_TRIM = 1 << 7;

    public record Result(boolean success, Component message) {
    }

    public static Result forge(ServerPlayer player, RuneforgeWorkbenchBlockEntity workbench) {
        return forge(player, workbench, null, null);
    }

    public static Result forge(ServerPlayer player, RuneforgeWorkbenchBlockEntity workbench,
                               RuneItem selectedSuffix) {
        return forge(player, workbench, null, selectedSuffix);
    }

    public static Result forge(ServerPlayer player, RuneforgeWorkbenchBlockEntity workbench,
                               RuneItem selectedPrefix, RuneItem selectedSuffix) {
        ItemStackHandler inventory = workbench.getInventory();
        ItemStack base = inventory.getStackInSlot(RuneforgeWorkbenchBlockEntity.BASE_SLOT);
        if (base.isEmpty()) {
            return fail("empty");
        }
        if (base.isDamaged()) {
            return fail("damaged");
        }

        List<RuneItem> runes = selectedRunes(inventory, selectedPrefix, selectedSuffix);
        if (runes.isEmpty()) {
            return fail("select_rune");
        }
        int requiredPower = runes.stream().mapToInt(RuneItem::getRunePower).max().orElse(0);
        if (requiredPower > RuneProgression.get(player)) {
            return fail("power_locked");
        }
        for (RuneItem rune : runes) {
            if (!rune.isCompatible(base)) {
                return fail("incompatible");
            }
            if (RuneforgeData.has(base, rune.getCategory())) {
                return fail("occupied");
            }
        }

        int lapis = cost(inventory, RuneCurrency.LAPIS, selectedPrefix, selectedSuffix);
        int amethyst = cost(inventory, RuneCurrency.AMETHYST, selectedPrefix, selectedSuffix);
        if (!player.getAbilities().instabuild) {
            if (count(player, Items.LAPIS_LAZULI.getDefaultInstance()) < lapis) {
                return fail("lapis");
            }
            if (count(player, Items.AMETHYST_SHARD.getDefaultInstance()) < amethyst) {
                return fail("amethyst");
            }
        }

        ItemStack result = base.copy();
        RuneforgeData.rememberOriginalName(result);
        for (RuneItem rune : runes) {
            applyRune(result, rune);
        }
        refreshForgedName(result);
        if (!player.getAbilities().instabuild) {
            consume(player, Items.LAPIS_LAZULI.getDefaultInstance(), lapis);
            consume(player, Items.AMETHYST_SHARD.getDefaultInstance(), amethyst);
        }
        inventory.setStackInSlot(RuneforgeWorkbenchBlockEntity.BASE_SLOT, result);
        workbench.setChanged();
        if (workbench.getLevel() != null) {
            workbench.getLevel().sendBlockUpdated(workbench.getBlockPos(), workbench.getBlockState(),
                    workbench.getBlockState(), 3);
        }
        return new Result(true, Component.translatable("message.ragecraft4reforged.runeforge.success", runes.size()));
    }

    public static ItemStack preview(ItemStack base, ItemStackHandler inventory) {
        return preview(base, inventory, null, null);
    }

    public static ItemStack preview(ItemStack base, ItemStackHandler inventory, RuneItem selectedSuffix) {
        return preview(base, inventory, null, selectedSuffix);
    }

    public static ItemStack preview(ItemStack base, ItemStackHandler inventory,
                                    RuneItem selectedPrefix, RuneItem selectedSuffix) {
        if (base.isEmpty() || base.isDamaged()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = base.copy();
        RuneforgeData.rememberOriginalName(result);
        for (RuneItem rune : selectedRunes(inventory, selectedPrefix, selectedSuffix)) {
            if (!rune.isCompatible(result) || RuneforgeData.has(result, rune.getCategory())) {
                return base.copy();
            }
            applyRune(result, rune);
        }
        refreshForgedName(result);
        return result;
    }

    public static int cost(ItemStackHandler inventory, RuneCurrency currency) {
        return cost(inventory, currency, null, null);
    }

    public static int cost(ItemStackHandler inventory, RuneCurrency currency, RuneItem selectedSuffix) {
        return cost(inventory, currency, null, selectedSuffix);
    }

    public static int cost(ItemStackHandler inventory, RuneCurrency currency,
                           RuneItem selectedPrefix, RuneItem selectedSuffix) {
        return selectedRunes(inventory, selectedPrefix, selectedSuffix).stream()
                .filter(rune -> rune.getCurrency() == currency)
                .mapToInt(RuneItem::getCost)
                .sum();
    }

    public static int requiredPower(ItemStackHandler inventory) {
        return requiredPower(inventory, null, null);
    }

    public static int requiredPower(ItemStackHandler inventory, RuneItem selectedSuffix) {
        return requiredPower(inventory, null, selectedSuffix);
    }

    public static int requiredPower(ItemStackHandler inventory,
                                    RuneItem selectedPrefix, RuneItem selectedSuffix) {
        return selectedRunes(inventory, selectedPrefix, selectedSuffix).stream()
                .mapToInt(RuneItem::getRunePower).max().orElse(0);
    }

    private static List<RuneItem> selectedRunes(ItemStackHandler inventory,
                                                RuneItem selectedPrefix, RuneItem selectedSuffix) {
        List<RuneItem> result = new ArrayList<>(3);
        for (int slot = RuneforgeWorkbenchBlockEntity.PREFIX_SLOT;
             slot <= RuneforgeWorkbenchBlockEntity.SUFFIX_SLOT; slot++) {
            if (inventory.getStackInSlot(slot).getItem() instanceof RuneItem rune) {
                result.add(rune);
            }
        }
        if (selectedPrefix != null && result.stream().noneMatch(rune -> rune.getCategory() == RuneCategory.PREFIX)) {
            result.add(selectedPrefix);
        }
        if (selectedSuffix != null && result.stream().noneMatch(rune -> rune.getCategory() == RuneCategory.SUFFIX)) {
            result.add(selectedSuffix);
        }
        return result;
    }

    private static void applyRune(ItemStack result, RuneItem rune) {
        RuneforgeData.addRepairCost(result, rune.getCost());
        RuneforgeData.set(result, rune);
        RuneDefinition definition = rune.getDefinition();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(result);
        for (Map.Entry<String, Integer> entry : definition.enchantments().entrySet()) {
            Enchantment enchantment = net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS.getValue(
                    new ResourceLocation("minecraft", entry.getKey()));
            if (enchantment != null) {
                enchantments.merge(enchantment, entry.getValue(), Integer::sum);
            }
        }
        for (Map.Entry<String, Integer> entry : definition.stats().entrySet()) {
            Enchantment enchantment = ModEnchantments.byStat(entry.getKey());
            if (enchantment != null) {
                enchantments.merge(enchantment, entry.getValue(), Integer::sum);
            }
        }
        EnchantmentHelper.setEnchantments(enchantments, result);
        definition.stats().forEach((name, amount) -> RuneforgeData.addStat(result, name, amount));
        RuneforgeData.applyRuntimeTags(result);
        if (definition.category() == RuneCategory.SUFFIX && definition.modelOffset() != 0) {
            applyForgedModel(result, definition.modelOffset());
        }
        if (!definition.trimMaterial().isEmpty() && !definition.trimPattern().isEmpty()) {
            CompoundTag trim = new CompoundTag();
            trim.putString("material", definition.trimMaterial());
            trim.putString("pattern", definition.trimPattern());
            result.getOrCreateTag().put("Trim", trim);
            result.getOrCreateTag().putInt("HideFlags", result.getOrCreateTag().getInt("HideFlags") | HIDE_ARMOR_TRIM);
        }
    }

    private static void refreshForgedName(ItemStack result) {
        result.setHoverName(forgedName(result));
    }

    public static Component forgedName(ItemStack result) {
        RuneRarity rarity = RuneRarity.COMMON;
        for (RuneCategory category : RuneCategory.values()) {
            RuneDefinition definition = RuneforgeData.getDefinition(result, category);
            if (definition != null && definition.rarity().ordinal() > rarity.ordinal()) {
                rarity = definition.rarity();
            }
        }

        MutableComponent name = Component.empty();
        appendAffix(name, RuneforgeData.getDefinition(result, RuneCategory.PREFIX));
        name.append(RuneforgeData.getOriginalName(result));
        appendAffix(name, RuneforgeData.getDefinition(result, RuneCategory.SUFFIX));
        int color = rarity.getNameColor();
        return name.withStyle(style -> style.withColor(color).withBold(true).withItalic(false));
    }

    private static void appendAffix(MutableComponent target, RuneDefinition definition) {
        if (definition == null || definition.affixNameJson().isEmpty()) {
            return;
        }
        Component affix = Component.Serializer.fromJson(definition.affixNameJson());
        if (affix != null) {
            target.append(affix);
        }
    }

    private static void applyForgedModel(ItemStack result, int offset) {
        int base = result.getOrCreateTag().getInt("CustomModelData");
        if (base == 0 && result.getItem() instanceof AxeItem) {
            if (result.is(Items.STONE_AXE)) {
                base = 30;
            } else if (result.is(Items.IRON_AXE)) {
                base = 90;
            } else if (result.is(Items.DIAMOND_AXE)) {
                base = 150;
            } else if (result.is(Items.NETHERITE_AXE)) {
                base = 190;
            }
        }
        result.getOrCreateTag().putInt("CustomModelData", base + offset);
    }

    private static int count(ServerPlayer player, ItemStack wanted) {
        int total = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (ItemStack.isSameItemSameTags(stack, wanted)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void consume(ServerPlayer player, ItemStack wanted, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (ItemStack.isSameItemSameTags(stack, wanted)) {
                int removed = Math.min(remaining, stack.getCount());
                stack.shrink(removed);
                remaining -= removed;
            }
        }
        player.getInventory().setChanged();
    }

    private static Result fail(String reason) {
        return new Result(false, Component.translatable("message.ragecraft4reforged.runeforge." + reason));
    }

    private RuneforgeService() {
    }
}
