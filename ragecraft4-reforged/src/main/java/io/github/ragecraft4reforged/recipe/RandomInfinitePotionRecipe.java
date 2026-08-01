package io.github.ragecraft4reforged.recipe;

import io.github.ragecraft4reforged.registry.ModItems;
import io.github.ragecraft4reforged.registry.ModRecipeSerializers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;

public final class RandomInfinitePotionRecipe extends CustomRecipe {
    private static final int REQUIRED_POTIONS = 4;

    public RandomInfinitePotionRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int potionCount = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (!stack.is(Items.POTION) || PotionUtils.getPotion(stack) != Potions.AWKWARD) {
                return false;
            }
            potionCount++;
        }
        return potionCount == REQUIRED_POTIONS;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        List<ItemStack> results = ModItems.ALL_INFINITE_POTIONS.values().stream()
                .map(entry -> entry.get().getDefaultInstance())
                .toList();
        return results.get(RandomSource.create().nextInt(results.size()));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= REQUIRED_POTIONS;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.RANDOM_INFINITE_POTION.get();
    }
}
