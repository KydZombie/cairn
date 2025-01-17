package io.github.kydzombie.cairntest.block.entity;

import io.github.kydzombie.cairn.api.block.entity.SaveToNbt;
import io.github.kydzombie.cairn.api.gui.SyncField;
import io.github.kydzombie.cairn.api.gui.SyncGetter;
import io.github.kydzombie.cairn.api.gui.SyncSetter;
import io.github.kydzombie.cairn.api.storage.HasItemStorage;
import io.github.kydzombie.cairn.api.storage.ItemStorage;
import lombok.Getter;
import net.minecraft.block.entity.BlockEntity;

public class SimpleStorageBlockEntity extends BlockEntity implements HasItemStorage {
    @SaveToNbt
    @Getter
    private final ItemStorage itemStorage = new ItemStorage(1);

    @SaveToNbt
    @SyncField
    public int progress = 0;

    @SaveToNbt
    private int privateProgress = 0;

    @SyncGetter("privateProgress")
    public int getPrivateProgress() {
        return privateProgress;
    }

    @SyncSetter("privateProgress")
    public void setPrivateProgress(int privateProgress) {
        this.privateProgress = privateProgress;
    }

    @Override
    public void tick() {
        super.tick();
        if (world.isRemote) return;
        progress++;
        privateProgress += 2;
    }

    @Override
    public String getName() {
        return "Simple Storage";
    }
}
