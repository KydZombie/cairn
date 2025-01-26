package io.github.kydzombie.cairn.api.client;

import net.minecraft.item.ItemStack;

public class RenderHelper {
    public static RectRenderContext startRect() {
        return new RectRenderContext();
    }

    public static ItemRenderContext startItem(ItemStack itemStack) {
        return new ItemRenderContext(itemStack);
    }
}
