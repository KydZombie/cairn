package io.github.kydzombie.cairn.impl.mixin;

import io.github.kydzombie.cairn.api.recipe.CustomCraftingRecipe;
import io.github.kydzombie.cairn.api.recipe.CustomCraftingRecipeManager;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipeManager;
import net.modificationstation.stationapi.api.StationAPI;
import net.modificationstation.stationapi.api.event.recipe.RecipeRegisterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingRecipeManager.class)
public class CraftingRecipeManagerMixin {
    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Collections;sort(Ljava/util/List;Ljava/util/Comparator;)V"
            )
    )
    private void cairn_postRecipeEvents(CallbackInfo ci) {
        StationAPI.EVENT_BUS.post(RecipeRegisterEvent.builder().recipeId(CustomCraftingRecipeManager.RECIPE_ID).build());
    }

    @Inject(
            method = "craft(Lnet/minecraft/inventory/CraftingInventory;)Lnet/minecraft/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cairn_addCustomRecipes(CraftingInventory craftingInventory, CallbackInfoReturnable<ItemStack> cir) {
        for (CustomCraftingRecipe customRecipe : CustomCraftingRecipeManager.RECIPES) {
            ItemStack stack = customRecipe.getOutput(craftingInventory);
            if (stack != null) {
                cir.setReturnValue(stack);
                return;
            }
        }
    }
}
