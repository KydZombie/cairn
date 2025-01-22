package io.github.kydzombie.cairntest.client;

import io.github.kydzombie.cairn.api.client.RenderHelper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import org.lwjgl.opengl.GL11;

public class RenderTestBlockEntityRenderer extends BlockEntityRenderer {
    @Override
    public void render(BlockEntity blockEntity, double x, double y, double z, float tickDelta) {
        GL11.glPushMatrix();

        long time = System.currentTimeMillis();
        float angle = (time % 3600) / 10.0f;

        double scale = 0.5f + 0.1f * Math.sin(System.currentTimeMillis() / 500.0);

        float r = (float) Math.sin(System.currentTimeMillis() / 1000.0) * 0.5f + 0.5f;
        float g = (float) Math.sin((System.currentTimeMillis() / 1000.0) + 2) * 0.5f + 0.5f;
        float b = (float) Math.sin((System.currentTimeMillis() / 1000.0) + 4) * 0.5f + 0.5f;

        RenderHelper.startRect().centeredAt(x, y, z).withColor(r, g, b, 0.8f).withRotation(0, angle, angle).withSize(scale).draw();

        RenderHelper.startRect().at(x, y, z).withSize(0.2f).draw();
        GL11.glPopMatrix();
    }
}
