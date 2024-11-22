package io.github.kydzombie.cairntest.block.entity;

import io.github.kydzombie.cairn.api.block.entity.UpdatePacketReceiver;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.Random;

public class UpdatePacketTesterBlockEntity extends BlockEntity implements UpdatePacketReceiver<TestUpdateData> {
    int value = 0;
    String text;

    public UpdatePacketTesterBlockEntity() {
        Random random = new Random();
        int length = random.nextInt(4, 8);
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append((char) ('A' + random.nextInt(26)));
        }
        text = builder.toString();
    }

    @Override
    public void tick() {
        super.tick();
        if (!world.isRemote) {
            value++;
            if (value > 100) {
                value = 0;
            }
            markDirty();
        }
    }

    @Override
    public void receiveUpdateData(TestUpdateData data) {
        System.out.println("Received update data: " + data.value() + ", " + data.text());
        this.value = data.value();
        this.text = data.text();
    }

    @Override
    public TestUpdateData createUpdateData() {
        return new TestUpdateData(value, text);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        text = nbt.getString("text");
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putString("text", text);
    }
}

