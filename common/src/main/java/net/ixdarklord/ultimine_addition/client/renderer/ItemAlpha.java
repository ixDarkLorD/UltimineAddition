package net.ixdarklord.ultimine_addition.client.renderer;

// GUI items are drawn from an atlas at full opacity; wrap a draw call in draw() to fade it, and optionally tint it
// (its colours multiplied by an RGB, e.g. grey for a placeholder) (see the item mixins).
public final class ItemAlpha {
    public static final int NO_TINT = 0xFFFFFF;

    private static float next = 1.0F;
    private static int nextTint = NO_TINT;

    private ItemAlpha() {}

    public static void draw(float alpha, Runnable draw) {
        draw(alpha, NO_TINT, draw);
    }

    public static void draw(float alpha, int tint, Runnable draw) {
        next = alpha;
        nextTint = tint;
        try {
            draw.run();
        } finally {
            next = 1.0F;
            nextTint = NO_TINT;
        }
    }

    public static float next() {
        return next;
    }

    public static int nextTint() {
        return nextTint;
    }

    public interface Holder {
        float ua$getAlpha();

        void ua$setAlpha(float alpha);

        int ua$getTint();

        void ua$setTint(int tint);
    }
}
