package net.ixdarklord.ultimine_addition.client.renderer;

// GUI items are drawn from an atlas at full opacity; wrap a draw call in draw() to fade it (see the item mixins).
public final class ItemAlpha {
    private static float next = 1.0F;

    private ItemAlpha() {}

    public static void draw(float alpha, Runnable draw) {
        next = alpha;
        try {
            draw.run();
        } finally {
            next = 1.0F;
        }
    }

    public static float next() {
        return next;
    }

    public interface Holder {
        float ua$getAlpha();

        void ua$setAlpha(float alpha);
    }
}
