package net.ixdarklord.ultimine_addition.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;

// GUI items take no tint; wrap a draw call in draw() to fade it, and optionally tint it (its colours multiplied by an
// RGB, e.g. grey for a placeholder). On 1.21.1 each GUI item is drawn right away with the shader color, so its alpha
// fades the item (translucent item models; cutout ones stay opaque) and its RGB tints it.
public final class ItemAlpha {
    public static final int NO_TINT = 0xFFFFFF;

    private ItemAlpha() {}

    public static void draw(float alpha, Runnable draw) {
        draw(alpha, NO_TINT, draw);
    }

    public static void draw(float alpha, int tint, Runnable draw) {
        if (alpha >= 1.0F && tint == NO_TINT) {
            draw.run();
            return;
        }
        if (alpha <= 0.0F) return;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(((tint >> 16) & 0xFF) / 255.0F, ((tint >> 8) & 0xFF) / 255.0F, (tint & 0xFF) / 255.0F, Math.min(alpha, 1.0F));
        try {
            draw.run();
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
