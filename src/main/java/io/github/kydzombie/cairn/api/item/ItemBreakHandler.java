package io.github.kydzombie.cairn.api.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public interface ItemBreakHandler {
    ItemBreakResult onItemBroken(ItemStack stack, Entity user);
}
