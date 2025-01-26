package io.github.kydzombie.cairn.api.client;

import io.github.kydzombie.cairn.Cairn;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.modificationstation.stationapi.api.client.StationRenderAPI;
import net.modificationstation.stationapi.api.client.render.RendererAccess;
import net.modificationstation.stationapi.api.client.render.model.BakedModel;
import net.modificationstation.stationapi.api.client.render.model.VanillaBakedModel;
import net.modificationstation.stationapi.api.client.render.model.json.ModelTransformation;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.client.texture.SpriteAtlasTexture;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.api.util.math.Vec3d;
import net.modificationstation.stationapi.impl.client.arsenic.renderer.render.ArsenicItemRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import static org.lwjgl.opengl.GL11.*;

// TODO: Rotation does not match RectRenderContext
public class ItemRenderContext {
    private static final BlockRenderManager BLOCK_RENDER_MANAGER = ((Minecraft) FabricLoader.getInstance().getGameInstance()).worldRenderer.blockRenderManager;
    private final ItemStack itemStack;
    private Vec3d pos = null;
    private Vec3d rotation = null;
    private double scale = 1;
    private boolean shouldRender3D = true;
    private float brightness = 1f;

    protected ItemRenderContext(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemRenderContext at(double x, double y, double z) {
        this.pos = new Vec3d(x, y, z);
        return this;
    }

    public ItemRenderContext withRotation(double x, double y, double z) {
        this.rotation = new Vec3d(x, y, z);
        return this;
    }

    public ItemRenderContext withScale(double scale) {
        this.scale = scale;
        return this;
    }


    // TODO: 2D rendering for baked models

    /**
     * If the item is rendered in 2D, rotation will be ignored
     * 2D rendering only works for non-block items without
     * a baked model
     */
    public ItemRenderContext render2D() {
        this.shouldRender3D = false;
        return this;
    }

    public ItemRenderContext render3D() {
        this.shouldRender3D = true;
        return this;
    }

    public ItemRenderContext withBrightness(float brightness) {
        this.brightness = brightness;
        return this;
    }

    public void draw() {
        if (pos == null) {
            throw new IllegalStateException("Position not set");
        }

//        if (!shouldRender3D) {
//            Cairn.LOGGER.warn("2D rendering is not supported yet, will use 3D rendering instead");
//            shouldRender3D = true;
//        }

        Tessellator tessellator = Tessellator.INSTANCE;
        SpriteAtlasTexture atlas = StationRenderAPI.getBakedModelManager().getAtlas(Atlases.GAME_ATLAS_TEXTURE);

        BakedModel model = RendererAccess.INSTANCE.getRenderer().bakedModelRenderer().getModel(
                itemStack,
                null,
                null,
                0
        );

        if (model instanceof VanillaBakedModel) {
            drawVanilla(tessellator, atlas, (VanillaBakedModel) model);
        } else if (!model.isBuiltin()) {
            drawBakedModel(tessellator, atlas, model);
        }
    }

    private void drawVanilla(Tessellator tessellator, SpriteAtlasTexture atlas, VanillaBakedModel model) {
        double depth = (1 / 16.0) / 2.0;
        GL11.glPushMatrix();
        GL11.glTranslated(pos.x, pos.y, pos.z);


        GL11.glColor4f(1f, 1f, 1f, 1f);
        int textureId = itemStack.getTextureId();
        atlas.bindTexture();
        Sprite sprite = atlas.getSprite(itemStack.getItem().getAtlas().getTexture(textureId).getId());
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        Block block;
        if (itemStack.getItem() instanceof BlockItem blockItem && BlockRenderManager.isSideLit((block = blockItem.getBlock()).getRenderType())) {
            if (rotation != null) {
                GL11.glRotated(rotation.x, 1, 0, 0);
                GL11.glRotated(rotation.y, 0, 1, 0);
                GL11.glRotated(rotation.z, 0, 0, 1);
            }

            if (scale != 2) {
                GL11.glScaled(scale / 2, scale / 2, scale / 2);
            }

            if (block.isFullCube() || block.id == Block.SLAB.id || block.getRenderType() == 16) {
                float blockScale = .5f;
                glScalef(blockScale, blockScale, blockScale);
            }

            glPushMatrix();
            BLOCK_RENDER_MANAGER.render(block, itemStack.getDamage(), 1f);
            glPopMatrix();
        } else if (shouldRender3D) {
            if (rotation != null) {
                GL11.glRotated(rotation.x, 1, 0, 0);
                GL11.glRotated(rotation.y, 0, 1, 0);
                GL11.glRotated(rotation.z, 0, 0, 1);
            }

            if (scale != 2) {
                GL11.glScaled(scale / 2, scale / 2, scale / 2);
            }

            GL11.glTranslated(-0.5, -0.5, 0);

            // Front
            tessellator.startQuads();
            tessellator.normal(0f, 0f, 1f);
            tessellator.vertex(0.0, 0.0, +depth, sprite.getMaxU(), sprite.getMaxV());
            tessellator.vertex(1.0, 0.0, +depth, sprite.getMinU(), sprite.getMaxV());
            tessellator.vertex(1.0, 1.0, +depth, sprite.getMinU(), sprite.getMinV());
            tessellator.vertex(0.0, 1.0, +depth, sprite.getMaxU(), sprite.getMinV());
            tessellator.draw();

            tessellator.startQuads();
            tessellator.normal(0f, 0f, -1f);
            tessellator.vertex(0.0, 1.0, -depth, sprite.getMaxU(), sprite.getMinV());
            tessellator.vertex(1.0, 1.0, -depth, sprite.getMinU(), sprite.getMinV());
            tessellator.vertex(1.0, 0.0, -depth, sprite.getMinU(), sprite.getMaxV());
            tessellator.vertex(0.0, 0.0, -depth, sprite.getMaxU(), sprite.getMaxV());
            tessellator.draw();

            int width = sprite.getContents().getWidth();
            int height = sprite.getContents().getHeight();
            float xDiff = sprite.getMinU() - sprite.getMaxU();
            float yDiff = sprite.getMinV() - sprite.getMaxV();
            float xSub = 0.5f * (sprite.getMaxU() - sprite.getMinU()) / width;
            float ySub = 0.5f * (sprite.getMaxV() - sprite.getMinV()) / height;

            // Right
            tessellator.startQuads();
            tessellator.normal(-1f, 0f, 0f);
            for (int i = 0; i < width; i++) {
                double pos = i / (double) width;
                double iconPos = sprite.getMaxU() + xDiff * pos - xSub;

                tessellator.vertex(pos, 0.0, -depth, iconPos, sprite.getMaxV());
                tessellator.vertex(pos, 0.0, +depth, iconPos, sprite.getMaxV());
                tessellator.vertex(pos, 1.0, +depth, iconPos, sprite.getMinV());
                tessellator.vertex(pos, 1.0, -depth, iconPos, sprite.getMinV());
            }
            tessellator.draw();

            // Left
            tessellator.startQuads();
            tessellator.normal(1f, 0f, 0f);
            double inverseWidth = 1.0 / width;
            for (int i = 0; i < width; i++) {
                double pos = i / (double) width;
                double iconPos = sprite.getMaxU() + xDiff * pos - xSub;
                double posEnd = pos + inverseWidth;

                tessellator.vertex(posEnd, 1.0, -depth, iconPos, sprite.getMinV());
                tessellator.vertex(posEnd, 1.0, +depth, iconPos, sprite.getMinV());
                tessellator.vertex(posEnd, 0.0, +depth, iconPos, sprite.getMaxV());
                tessellator.vertex(posEnd, 0.0, -depth, iconPos, sprite.getMaxV());
            }
            tessellator.draw();

            // Bottom
            tessellator.startQuads();
            tessellator.normal(0f, -1f, 0f);
            for (int i = 0; i < height; i++) {
                double pos = i / (double) height;
                double iconPos = sprite.getMaxV() + yDiff * pos - ySub;

                tessellator.vertex(1.0, pos, +depth, sprite.getMinU(), iconPos);
                tessellator.vertex(0.0, pos, +depth, sprite.getMaxU(), iconPos);
                tessellator.vertex(0.0, pos, -depth, sprite.getMaxU(), iconPos);
                tessellator.vertex(1.0, pos, -depth, sprite.getMinU(), iconPos);
            }
            tessellator.draw();

            // Top
            tessellator.startQuads();
            tessellator.normal(0f, 1f, 0f);
            double inverseHeight = 1.0 / height;
            for (int i = 0; i < height; i++) {
                double pos = i / (double) height;
                double iconPos = sprite.getMaxV() + yDiff * pos - ySub;
                double posEnd = pos + inverseHeight;

                tessellator.vertex(0.0, posEnd, +depth, sprite.getMaxU(), iconPos);
                tessellator.vertex(1.0, posEnd, +depth, sprite.getMinU(), iconPos);
                tessellator.vertex(1.0, posEnd, -depth, sprite.getMinU(), iconPos);
                tessellator.vertex(0.0, posEnd, -depth, sprite.getMaxU(), iconPos);
            }
            tessellator.draw();

        } else {
            if (scale != 2) {
                GL11.glScaled(scale / 2, scale / 2, scale / 2);
            }

            float var31 = 1.0F;
            float var32 = 0.5F;
            float var22 = 0.25F;

            GL11.glRotatef(180.0F - EntityRenderDispatcher.INSTANCE.yaw, 0.0f, 1.0f, 0.0f);
            tessellator.startQuads();
            tessellator.normal(0.0f, 1.0f, 0.0f);
            tessellator.vertex(0.0 - var32, 0.0 - var22, 0.0, sprite.getMinU(), sprite.getMaxV());
            tessellator.vertex(var31 - var32, 0.0 - var22, 0.0, sprite.getMaxU(), sprite.getMaxV());
            tessellator.vertex(var31 - var32, 1.0 - var22, 0.0, sprite.getMaxU(), sprite.getMinV());
            tessellator.vertex(0.0 - var32, 1.0 - var22, 0.0, sprite.getMinU(), sprite.getMinV());
            tessellator.draw();
        }



        // TODO: Rest of rendering

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);

        GL11.glPopMatrix();
    }

    private void drawBakedModel(Tessellator tessellator, SpriteAtlasTexture atlas, BakedModel model) {
        GL11.glPushMatrix();

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        GL11.glTranslated(pos.x, pos.y, pos.z);
        if (rotation != null) {
            GL11.glRotated(rotation.x, 1, 0, 0);
            GL11.glRotated(rotation.y, 0, 1, 0);
            GL11.glRotated(rotation.z, 0, 0, 1);
        }
        if (scale != 2) {
            GL11.glScaled(scale / 2, scale / 2, scale / 2);
        }

        atlas.bindTexture();

        tessellator.startQuads();
        RendererAccess.INSTANCE.getRenderer().bakedModelRenderer().renderItem(
                itemStack,
                ModelTransformation.Mode.FIXED,
                brightness,
                model
        );
        tessellator.draw();

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);

        GL11.glPopMatrix();
    }
}
