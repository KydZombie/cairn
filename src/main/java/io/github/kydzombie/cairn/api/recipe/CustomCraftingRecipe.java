package io.github.kydzombie.cairn.api.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public interface CustomCraftingRecipe {
    @Nullable
    ItemStack getOutput(CraftingInventory craftingInventory);
}
