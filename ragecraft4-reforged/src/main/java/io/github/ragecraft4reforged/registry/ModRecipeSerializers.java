package io.github.ragecraft4reforged.registry;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import io.github.ragecraft4reforged.recipe.RandomInfinitePotionRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTER =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Ragecraft4Reforged.MOD_ID);

    public static final RegistryObject<RecipeSerializer<RandomInfinitePotionRecipe>> RANDOM_INFINITE_POTION =
            REGISTER.register("random_infinite_potion",
                    () -> new SimpleCraftingRecipeSerializer<>(RandomInfinitePotionRecipe::new));

    private ModRecipeSerializers() {
    }
}
