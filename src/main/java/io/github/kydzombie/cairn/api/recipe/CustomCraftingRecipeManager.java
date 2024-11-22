package io.github.kydzombie.cairn.api.recipe;

import io.github.kydzombie.cairn.Cairn;
import net.modificationstation.stationapi.api.util.Identifier;

import java.util.ArrayList;

public class CustomCraftingRecipeManager {
    public static final Identifier RECIPE_ID = Cairn.NAMESPACE.id("custom_crafting");
    public static final ArrayList<CustomCraftingRecipe> RECIPES = new ArrayList<>();

    public static void registerRecipe(CustomCraftingRecipe recipe) {
        RECIPES.add(recipe);
    }
}
