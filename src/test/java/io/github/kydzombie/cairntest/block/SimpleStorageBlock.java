package io.github.kydzombie.cairntest.block;

import io.github.kydzombie.cairntest.CairnTest;
import io.github.kydzombie.cairntest.block.entity.SimpleStorageBlockEntity;
import io.github.kydzombie.cairntest.gui.SimpleStorageScreenHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.gui.screen.container.GuiHelper;
import net.modificationstation.stationapi.api.template.block.TemplateBlockWithEntity;
import net.modificationstation.stationapi.api.util.Identifier;

public class SimpleStorageBlock extends TemplateBlockWithEntity {
    public SimpleStorageBlock(Identifier identifier, Material material) {
        super(identifier, material);
        setTranslationKey(identifier);
    }

    @Override
    public boolean onUse(World world, int x, int y, int z, PlayerEntity player) {
        SimpleStorageBlockEntity blockEntity = (SimpleStorageBlockEntity) world.getBlockEntity(x, y, z);
        GuiHelper.openGUI(player, CairnTest.NAMESPACE.id("simple_storage"), blockEntity, new SimpleStorageScreenHandler(player.inventory, blockEntity));
        return true;
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new SimpleStorageBlockEntity();
    }
}
