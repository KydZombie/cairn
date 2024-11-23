package io.github.kydzombie.cairntest.gui;

import io.github.kydzombie.cairn.api.gui.SyncField;
import io.github.kydzombie.cairn.api.gui.Syncable;
import io.github.kydzombie.cairn.api.gui.SyncedBlockEntity;
import io.github.kydzombie.cairntest.block.entity.SimpleStorageBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

@Syncable
public class SimpleStorageScreenHandler extends ScreenHandler {
    @SyncedBlockEntity
    private final SimpleStorageBlockEntity blockEntity;

    @SyncField("progress")
    public int progress;

    @SyncField("privateProgress")
    private int privateProgress;

    public SimpleStorageScreenHandler(PlayerInventory playerInventory, SimpleStorageBlockEntity blockEntity) {
        this.blockEntity = blockEntity;

        addSlot(new Slot(blockEntity, 0, 0, 0));

        int var3;
        for (var3 = 0; var3 < 3; ++var3) {
            for (int var4 = 0; var4 < 9; ++var4) {
                this.addSlot(new Slot(playerInventory, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
            }
        }

        for (var3 = 0; var3 < 9; ++var3) {
            this.addSlot(new Slot(playerInventory, var3, 8 + var3 * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return blockEntity.canPlayerUse(player);
    }
}
