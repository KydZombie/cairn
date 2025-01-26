package io.github.kydzombie.cairntest.block.entity;

import io.github.kydzombie.cairn.api.block.entity.SaveToNbt;
import io.github.kydzombie.cairntest.CairnTest;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Random;

public class RenderTestBlockEntity extends BlockEntity {
    private static final Random RANDOM = new Random();
    @SaveToNbt
    public MyColor myColor = MyColor.RED;

    @SaveToNbt
    public ItemStack itemStack;

    public RenderTestBlockEntity() {
        itemStack = switch (RANDOM.nextInt(4)) {
            case 0 -> new ItemStack(CairnTest.testItem);
            case 1 -> new ItemStack(Item.APPLE);
            case 2 -> new ItemStack(CairnTest.testBlock);
            case 3 -> new ItemStack(Block.STONE);
            default -> throw new IllegalStateException("Unexpected value: " + world.random.nextInt(4));
        };
    }

    public enum MyColor {
        RED,
        GREEN,
        BLUE
    }
}
