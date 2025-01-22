package io.github.kydzombie.cairn.api.client;

public record Color(float r, float g, float b, float a) {
    public static final Color WHITE = new Color(1, 1, 1);
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color RED = new Color(1, 0, 0);
    public static final Color GREEN = new Color(0, 1, 0);
    public static final Color BLUE = new Color(0, 0, 1);

    public Color(float r, float g, float b) {
        this(r, g, b, 1);
    }
}
