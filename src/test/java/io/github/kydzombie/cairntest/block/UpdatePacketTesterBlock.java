package io.github.kydzombie.cairntest.block;

import io.github.kydzombie.cairntest.block.entity.UpdatePacketTesterBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.modificationstation.stationapi.api.template.block.TemplateBlockWithEntity;
import net.modificationstation.stationapi.api.util.Identifier;

public class UpdatePacketTesterBlock extends TemplateBlockWithEntity {
    public UpdatePacketTesterBlock(Identifier identifier, Material material) {
        super(identifier, material);
        setTranslationKey(identifier);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new UpdatePacketTesterBlockEntity();
    }
}
