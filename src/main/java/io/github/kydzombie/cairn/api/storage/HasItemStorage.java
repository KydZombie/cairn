package io.github.kydzombie.cairn.api.storage;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.Random;

public interface HasItemStorage extends Inventory {
    Random RANDOM = new Random();

    ItemStorage getItemStorage();

    @Override
    default int size() {
        return getItemStorage().size();
    }

    @Override
    default ItemStack getStack(int slot) {
        return getItemStorage().getStack(slot);
    }

    @Override
    default ItemStack removeStack(int slot, int amount) {
        return getItemStorage().removeStack(slot, amount, this);
    }

    @Override
    default void setStack(int slot, ItemStack stack) {
        getItemStorage().setStack(slot, stack, this);
    }

    @Override
    default int getMaxCountPerStack() {
        return getItemStorage().getMaxCountPerStack();
    }

    @Override
    default void markDirty() {
    }

    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        var blockEntity = (BlockEntity) (this);
        if (player.world.getBlockEntity(blockEntity.x, blockEntity.y, blockEntity.z) != blockEntity) {
            return false;
        } else {
            return !(player.getSquaredDistance((double) blockEntity.x + 0.5, (double) blockEntity.y + 0.5, (double) blockEntity.z + 0.5) > 64.0);
        }
    }

    default void dropInventory() {
        BlockEntity blockEntity = (BlockEntity) this;

        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (stack != null) {
                float xOffset = RANDOM.nextFloat() * 0.8F + 0.1F;
                float yOffset = RANDOM.nextFloat() * 0.8F + 0.1F;
                float zOffset = RANDOM.nextFloat() * 0.8F + 0.1F;

                while (stack.count > 0) {
                    int droppedCount = RANDOM.nextInt(21) + 10;
                    if (droppedCount > stack.count) {
                        droppedCount = stack.count;
                    }

                    stack.count -= droppedCount;
                    ItemEntity droppedEntity = new ItemEntity(
                            blockEntity.world, (float) blockEntity.x + xOffset, (float) blockEntity.y + yOffset, (float) blockEntity.z + zOffset, new ItemStack(stack.itemId, droppedCount, stack.getDamage())
                    );
                    float var13 = 0.05F;
                    droppedEntity.velocityX = (float) RANDOM.nextGaussian() * var13;
                    droppedEntity.velocityY = (float) RANDOM.nextGaussian() * var13 + 0.2F;
                    droppedEntity.velocityZ = (float) RANDOM.nextGaussian() * var13;
                    blockEntity.world.spawnEntity(droppedEntity);
                }
            }
        }
    }
}
