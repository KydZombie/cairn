package io.github.kydzombie.cairn.api.item;

import net.minecraft.item.ItemStack;

/**
 * @param stack
 * @param broken Whether a sound should be played & the player's break stat should be increased
 */
public record ItemBreakResult(ItemStack stack, boolean broken) {
    public static final ItemBreakResult BROKEN = new ItemBreakResult(null, true);
}
