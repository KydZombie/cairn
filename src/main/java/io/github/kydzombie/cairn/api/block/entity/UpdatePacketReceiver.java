package io.github.kydzombie.cairn.api.block.entity;

public interface UpdatePacketReceiver<T extends Record> {
    void receiveUpdateData(T data);
    T createUpdateData();
}
