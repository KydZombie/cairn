package io.github.kydzombie.cairn.api.client;

import io.github.kydzombie.cairn.Cairn;
import net.modificationstation.stationapi.api.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class RectRenderContext {
    private Vec3d pos = null;
    private double width = 1, height = 1, depth = 1;
    private Vec3d rotation = null;
    private Color color = Color.WHITE;
    private int glSFactor = GL11.GL_SRC_ALPHA;
    private int glDFactor = GL11.GL_ONE_MINUS_SRC_ALPHA;
    private boolean centered = false;
    private boolean textured = false;

    protected RectRenderContext() {
    }

    public RectRenderContext centeredAt(double x, double y, double z) {
        this.centered = true;
        this.pos = new Vec3d(x, y, z);
        return this;
    }

    public RectRenderContext at(double x, double y, double z) {
        this.centered = false;
        this.pos = new Vec3d(x, y, z);
        return this;
    }

    public RectRenderContext withRotation(double x, double y, double z) {
        this.rotation = new Vec3d(x, y, z);
        return this;
    }

    public RectRenderContext withSize(double width, double height, double depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        return this;
    }

    public RectRenderContext withSize(double size) {
        this.width = size;
        this.height = size;
        this.depth = size;
        return this;
    }

    public RectRenderContext withColor(float r, float g, float b, float a) {
        this.color = new Color(r, g, b, a);
        this.textured = false;
        return this;
    }

    public RectRenderContext withColor(float r, float g, float b) {
        this.color = new Color(r, g, b);
        this.textured = false;
        return this;
    }

    public RectRenderContext withBlendFunc(int glSFactor, int glDFactor) {
        this.glSFactor = glSFactor;
        this.glDFactor = glDFactor;
        return this;
    }

    public void draw() {
        if (pos == null) {
            Cairn.LOGGER.error("RectRenderContext: Position not set!");
        }

        if (color.a() != 1 || glSFactor != GL11.GL_SRC_ALPHA || glDFactor != GL11.GL_ONE_MINUS_SRC_ALPHA) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(glSFactor, glDFactor);
        }
        if (!textured) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
        }
        GL11.glDisable(GL11.GL_LIGHTING);

        if (centered) {
            GL11.glTranslated(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);
        } else {
            GL11.glTranslated(pos.x, pos.y, pos.z);
        }

        if (rotation != null) {
            GL11.glRotated(rotation.x, 1, 0, 0);
            GL11.glRotated(rotation.y, 0, 1, 0);
            GL11.glRotated(rotation.z, 0, 0, 1);
        }

        GL11.glScaled(width, height, depth);

        if (centered) {
            GL11.glTranslated(-0.5, -0.5, -0.5);
        }

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(color.r(), color.g(), color.b(), color.a());

        // Top
        GL11.glVertex3f(1.0f, 1.0f, 0.0f);
        GL11.glVertex3f(0.0f, 1.0f, 0.0f);
        GL11.glVertex3f(0.0f, 1.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);

        // Bottom
        GL11.glVertex3d(1.0f, 0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 0.0f, 0.0f);
        GL11.glVertex3f(1.0f, 0.0f, 0.0f);

        // Front
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);
        GL11.glVertex3f(0.0f, 1.0f, 1.0f);
        GL11.glVertex3f(0.0f, 0.0f, 1.0f);
        GL11.glVertex3f(1.0f, 0.0f, 1.0f);

        // Back
        GL11.glVertex3f(1.0f, 0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 1.0f, 0.0f);

        // Left
        GL11.glVertex3f(0.0f, 1.0f, 1.0f);
        GL11.glVertex3f(0.0f, 1.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.0f, 1.0f);

        // Right
        GL11.glVertex3f(1.0f, 1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);
        GL11.glVertex3f(1.0f, 0.0f, 1.0f);
        GL11.glVertex3f(1.0f, 0.0f, 0.0f);

        GL11.glEnd();
        if (centered) {
            GL11.glTranslated(0.5, 0.5, 0.5);
        }
        GL11.glScaled(-width, -height, -depth);
        if (rotation != null) {
            GL11.glRotated(-rotation.x, 1, 0, 0);
            GL11.glRotated(-rotation.y, 0, 1, 0);
            GL11.glRotated(-rotation.z, 0, 0, 1);
        }
        if (centered) {
            GL11.glTranslated(-(pos.x + 0.5), -(pos.y + 0.5), -(pos.z + 0.5));
        } else {
            GL11.glTranslated(-pos.x, -pos.y, -pos.z);
        }
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
