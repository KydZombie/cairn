package io.github.kydzombie.cairntest.block.entity;

import io.github.kydzombie.cairn.api.block.entity.UpdatePacketReceiver;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Random;

public class UpdatePacketTesterBlockEntity extends BlockEntity implements UpdatePacketReceiver<TestUpdateData> {
    int value = 0;
    String text;

    ItemStack stack = null;

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
                if (stack == null) {
                    stack = new ItemStack(Item.DIAMOND, 1);
                } else if (stack.count < stack.getMaxCount()) {
                    stack.count++;
                }
                System.out.println("Updated stack: " + stack);
            }
            markDirty();
        }
    }

    @Override
    public void receiveUpdateData(TestUpdateData data) {
        System.out.println("Received update data: " + data.value() + ", " + data.stack() + ", " + data.text());
        this.stack = data.stack();
        this.value = data.value();
        this.text = data.text();
    }

    @Override
    public TestUpdateData createUpdateData() {
        return new TestUpdateData(value, stack, text);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        text = nbt.getString("text");
        if (nbt.getBoolean("hasStack")) {
            NbtCompound stackNbt = nbt.getCompound("stack");
            stack = new ItemStack(stackNbt);
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putString("text", text);
        if (stack != null) {
            nbt.putBoolean("hasStack", true);
            NbtCompound stackNbt = new NbtCompound();
            stack.writeNbt(stackNbt);
            nbt.put("stack", stackNbt);
        } else {
            nbt.putBoolean("hasStack", false);
        }
    }
}

