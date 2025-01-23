package io.github.kydzombie.cairntest.block.entity;

import io.github.kydzombie.cairn.api.block.entity.SaveToNbt;
import net.minecraft.block.entity.BlockEntity;

public class RenderTestBlockEntity extends BlockEntity {
    @SaveToNbt
    public MyColor myColor = MyColor.RED;

    public enum MyColor {
        RED,
        GREEN,
        BLUE
    }
}
