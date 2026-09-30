package net.ixdarklord.ultimine_addition.client.gui.components;

import net.ixdarklord.coolcatcanvas.api.utils.ColorGradient;
import net.minecraft.client.gui.GuiGraphics;
import net.ixdarklord.ultimine_addition.util.ARGB;

// An animated gradient outline for a selected slot and the selection marker standing on it: inside the slot's
// 16 x 16 item area, then up both sides of the marker. It's one path, open where the marker meets the slot and at
// the marker's top, so the two read as a single shape. Colors run along the path, starting at the slot's top left.
public final class SlotSelectionOutline {
    // Colors at or above this luminance have no brighter shade to show, so the outline goes darker instead.
    private static final float BRIGHT = 0.62F;

    private static int cachedBase = -1;
    private static ColorGradient cachedGradient;

    private SlotSelectionOutline() {}

    /**
     * @param x         the slot's item area left (16 wide)
     * @param y         the slot's item area top (16 tall)
     * @param markerX   the marker's left
     * @param markerW   the marker's width
     * @param markerY   the marker's top; it reaches down to the slot
     * @param baseColor the Skills Record's background color; the outline cycles through brighter shades of it (or
     *                  darker ones, when it's already bright)
     */
    public static void draw(GuiGraphics graphics, int x, int y, int markerX, int markerW, int markerY, int baseColor, float alpha) {
        if (alpha <= 0.0F) return;
        ColorGradient gradient = gradient(baseColor);
        int size = 16, x1 = x + size - 1, y1 = y + size - 1;
        int left = markerX - 1, right = markerX + markerW;
        int rise = Math.max(0, y - markerY);
        // Top edge up to the marker, the marker's left side up, its right side down, then the rest of the slot.
        int length = (left - x + 1) + rise + rise + (x1 - right + 1) + (size - 1) + (size - 1) + (size - 2);
        int a = Math.round(alpha * 255);
        int i = 0;
        for (int px = x; px <= left; px++) dot(graphics, gradient, px, y, i++, length, a);
        for (int py = y - 1; py >= markerY; py--) dot(graphics, gradient, left, py, i++, length, a);
        for (int py = markerY; py < y; py++) dot(graphics, gradient, right, py, i++, length, a);
        for (int px = right; px <= x1; px++) dot(graphics, gradient, px, y, i++, length, a);
        for (int py = y + 1; py <= y1; py++) dot(graphics, gradient, x1, py, i++, length, a);
        for (int px = x1 - 1; px >= x; px--) dot(graphics, gradient, px, y1, i++, length, a);
        for (int py = y1 - 1; py > y; py--) dot(graphics, gradient, x, py, i++, length, a);
    }

    private static ColorGradient gradient(int baseColor) {
        int base = baseColor & 0xFFFFFF;
        if (base == cachedBase && cachedGradient != null) return cachedGradient;
        float luminance = (0.2126F * ARGB.red(base) + 0.7152F * ARGB.green(base) + 0.0722F * ARGB.blue(base)) / 255.0F;
        int target = luminance < BRIGHT ? 0xFFFFFF : 0x000000;
        // Toward white for a brighter shade, or toward black when the color is too bright for one.
        float[] steps = luminance < BRIGHT ? new float[]{0.35F, 0.7F, 0.15F} : new float[]{0.35F, 0.6F, 0.15F};
        int[] colors = new int[steps.length];
        for (int i = 0; i < steps.length; i++) colors[i] = ARGB.srgbLerp(steps[i], 0xFF000000 | base, 0xFF000000 | target) & 0xFFFFFF;
        cachedBase = base;
        cachedGradient = ColorGradient.of(colors).withSpeed(0.8F).withSpread(1.0F);
        return cachedGradient;
    }

    private static void dot(GuiGraphics graphics, ColorGradient gradient, int x, int y, int index, int length, int alpha) {
        graphics.fill(x, y, x + 1, y + 1, gradient.color(index / (float) length, alpha));
    }
}
