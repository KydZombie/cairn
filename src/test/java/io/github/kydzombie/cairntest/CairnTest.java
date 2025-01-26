package io.github.kydzombie.cairntest;

import io.github.kydzombie.cairn.api.recipe.CustomCraftingRecipeManager;
import io.github.kydzombie.cairntest.block.RenderTestBlock;
import io.github.kydzombie.cairntest.block.SimpleStorageBlock;
import io.github.kydzombie.cairntest.block.UpdatePacketTesterBlock;
import io.github.kydzombie.cairntest.block.entity.RenderTestBlockEntity;
import io.github.kydzombie.cairntest.block.entity.SimpleStorageBlockEntity;
import io.github.kydzombie.cairntest.block.entity.UpdatePacketTesterBlockEntity;
import io.github.kydzombie.cairntest.item.GlassSword;
import net.fabricmc.api.ModInitializer;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.modificationstation.stationapi.api.event.block.entity.BlockEntityRegisterEvent;
import net.modificationstation.stationapi.api.event.recipe.RecipeRegisterEvent;
import net.modificationstation.stationapi.api.event.registry.BlockRegistryEvent;
import net.modificationstation.stationapi.api.event.registry.ItemRegistryEvent;
import net.modificationstation.stationapi.api.item.tool.ToolMaterialFactory;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.modificationstation.stationapi.api.template.block.TemplateBlock;
import net.modificationstation.stationapi.api.template.item.TemplateItem;
import net.modificationstation.stationapi.api.util.Namespace;
import net.modificationstation.stationapi.api.util.Null;
import org.apache.logging.log4j.Logger;

public class CairnTest implements ModInitializer {
    @Entrypoint.Namespace
    public static final Namespace NAMESPACE = Null.get();
    @Entrypoint.Logger
    public static final Logger LOGGER = Null.get();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Cairn Test Mod");
    }

    public static SimpleStorageBlock simpleStorageBlock;
    public static UpdatePacketTesterBlock updatePacketTesterBlock;
    public static RenderTestBlock renderTestBlock;
    public static Block testBlock;

    @EventListener
    private void registerBlocks(BlockRegistryEvent event) {
        simpleStorageBlock = new SimpleStorageBlock(NAMESPACE.id("simple_storage_block"), Material.STONE);
        updatePacketTesterBlock = new UpdatePacketTesterBlock(NAMESPACE.id("update_packet_tester_block"), Material.STONE);
        renderTestBlock = new RenderTestBlock(NAMESPACE.id("render_test_block"), Material.STONE);
        testBlock = new TemplateBlock(NAMESPACE.id("test_block"), Material.STONE).setTranslationKey(NAMESPACE.id("test_block"));
    }

    @EventListener
    private void registerBlockEntities(BlockEntityRegisterEvent event) {
        event.register(SimpleStorageBlockEntity.class, NAMESPACE.id("simple_storage_block").toString());
        event.register(UpdatePacketTesterBlockEntity.class, NAMESPACE.id("update_packet_tester_block").toString());
        event.register(RenderTestBlockEntity.class, NAMESPACE.id("render_test_block").toString());
    }

    public static GlassSword glassSword;
    public static Item testItem;

    @EventListener
    private void registerItems(ItemRegistryEvent event) {
        ToolMaterial glass = ToolMaterialFactory.create(
                "glass",
                0,
                16,
                0.0f,
                4
        );

        glassSword = new GlassSword(NAMESPACE.id("glass_sword"), glass);

        testItem = new TemplateItem(NAMESPACE.id("test_item")).setTranslationKey(NAMESPACE.id("test_item"));
    }

    @EventListener
    private void registerRecipes(RecipeRegisterEvent event) {
        if (event.recipeId == CustomCraftingRecipeManager.RECIPE_ID) {
            LOGGER.info("Registering custom crafting recipes");
            CustomCraftingRecipeManager.registerRecipe((inventory) -> {
                ItemStack toolStack = null;
                int diamondCount = 0;
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack stack = inventory.getStack(i);
                    if (stack == null) continue;
                    if (stack.getItem() == Item.IRON_PICKAXE ||
                            stack.getItem() == Item.IRON_AXE ||
                            stack.getItem() == Item.IRON_SHOVEL ||
                            stack.getItem() == Item.IRON_SWORD ||
                            stack.getItem() == Item.IRON_HOE) {
                        if (toolStack != null) return null;
                        toolStack = stack;
                    }
                    if (stack.getItem() == Item.DIAMOND) {
                        diamondCount += stack.count;
                    }
                }
                if (toolStack == null) return null;
                if (diamondCount != 2 && diamondCount != 3) return null;

                ItemStack newToolStack;
                if (toolStack.getItem() == Item.IRON_PICKAXE) {
                    newToolStack = new ItemStack(Item.DIAMOND_PICKAXE);
                } else if (toolStack.getItem() == Item.IRON_AXE) {
                    newToolStack = new ItemStack(Item.DIAMOND_AXE);
                } else if (toolStack.getItem() == Item.IRON_SHOVEL) {
                    newToolStack = new ItemStack(Item.DIAMOND_SHOVEL);
                } else if (toolStack.getItem() == Item.IRON_SWORD) {
                    newToolStack = new ItemStack(Item.DIAMOND_SWORD);
                } else {
                    newToolStack = new ItemStack(Item.DIAMOND_HOE);
                }

                if (diamondCount == 2) {
                    float damagePercent = (float) toolStack.getDamage() / (float) toolStack.getMaxDamage();
                    newToolStack.setDamage((int) (newToolStack.getMaxDamage() * damagePercent));
                }

                return newToolStack;
            });
        }
    }
}
