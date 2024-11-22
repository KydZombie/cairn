package io.github.kydzombie.cairntest.gui.ingame;

import io.github.kydzombie.cairntest.block.entity.SimpleStorageBlockEntity;
import io.github.kydzombie.cairntest.gui.SimpleStorageScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import org.lwjgl.opengl.GL11;

public class SimpleStorageScreen extends HandledScreen {
    SimpleStorageBlockEntity blockEntity;

    public SimpleStorageScreen(PlayerInventory playerInventory, SimpleStorageBlockEntity blockEntity) {
        super(new SimpleStorageScreenHandler(playerInventory, blockEntity));
        this.blockEntity = blockEntity;
    }

    @Override
    protected void drawForeground() {
        this.textRenderer.draw(String.valueOf(blockEntity.progress), 60, 6, 4210752);
        this.textRenderer.draw(String.valueOf(blockEntity.getPrivateProgress()), 60, 32, 4210752);
        this.textRenderer.draw("Inventory", 8, this.backgroundHeight - 96 + 2, 4210752);
    }

    @Override
    protected void drawBackground(float tickDelta) {
        int n2 = this.minecraft.textureManager.getTextureId("/gui/furnace.png");
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.textureManager.bindTexture(n2);
        int n3 = (this.width - this.backgroundWidth) / 2;
        int n4 = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(n3, n4, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }
}
