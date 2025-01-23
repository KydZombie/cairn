package io.github.kydzombie.cairn.api.block.entity;

import net.minecraft.nbt.NbtElement;

public interface NbtSerializable<T extends NbtElement> {
    T writeNbt();
    void readNbt(T nbt);
}
