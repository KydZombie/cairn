package io.github.kydzombie.cairn.api.storage;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class ItemStorage {
    ItemStack[] inventory;
    
    public ItemStorage(int size) {
        inventory = new ItemStack[size];
    }

    public int size() {
        return inventory.length;
    }

    public ItemStack getStack(int slot) {
        return inventory[slot];
    }

    public ItemStack removeStack(int slot, int amount, Inventory dirtyable) {
        if (inventory[slot] != null) {
            ItemStack stack;
            if (inventory[slot].count <= amount) {
                stack = inventory[slot];
                inventory[slot] = null;
            } else {
                stack = inventory[slot].split(amount);
                if (inventory[slot].count == 0) {
                    inventory[slot] = null;
                }
            }
            dirtyable.markDirty();
            return stack;
        } else {
            return null;
        }
    }

    public void setStack(int slot, ItemStack stack, Inventory dirtyable) {
        inventory[slot] = stack;
        if (stack != null && stack.count > getMaxCountPerStack()) {
            stack.count = getMaxCountPerStack();
        }
        dirtyable.markDirty();
    }

    public int getMaxCountPerStack() {
        return 64;
    }

    public void readNbt(NbtCompound nbt) {
        NbtList inventoryList = nbt.getList("items");
        inventory = new ItemStack[size()];

        for (int var3 = 0; var3 < inventoryList.size(); ++var3) {
            NbtCompound itemNbt = (NbtCompound) inventoryList.get(var3);
            byte slot = itemNbt.getByte("slot");
            if (slot >= 0 && slot < inventory.length) {
                inventory[slot] = new ItemStack(itemNbt);
            }
        }
    }

    public void writeNbt(NbtCompound nbt) {
        NbtList inventoryList = new NbtList();

        for (int slot = 0; slot < inventory.length; ++slot) {
            if (inventory[slot] != null) {
                NbtCompound itemNbt = new NbtCompound();
                itemNbt.putByte("slot", (byte) slot);
                inventory[slot].writeNbt(itemNbt);
                inventoryList.add(itemNbt);
            }
        }

        nbt.put("items", inventoryList);
    }
}
