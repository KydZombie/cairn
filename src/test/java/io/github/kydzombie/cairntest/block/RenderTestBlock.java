package io.github.kydzombie.cairntest.block;

import io.github.kydzombie.cairntest.block.entity.RenderTestBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.world.BlockView;
import net.modificationstation.stationapi.api.template.block.TemplateBlockWithEntity;
import net.modificationstation.stationapi.api.util.Identifier;

public class RenderTestBlock extends TemplateBlockWithEntity {
    public RenderTestBlock(Identifier identifier, Material material) {
        super(identifier, material);
        setTranslationKey(identifier);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new RenderTestBlockEntity();
    }

    @Override
    public boolean isSideVisible(BlockView blockView, int x, int y, int z, int side) {
        return false;
    }

    @Override
    public boolean isOpaque() {
        return false;
    }
}
