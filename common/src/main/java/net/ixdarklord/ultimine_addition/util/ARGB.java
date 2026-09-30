package net.ixdarklord.ultimine_addition.util;

import net.minecraft.util.Mth;

/**
 * The ARGB color helpers this mod uses from Minecraft 26.1's {@code net.minecraft.util.ARGB}, which 1.21.1 doesn't
 * have ({@code FastColor.ARGB32} only has a few of them). Same names and results, so the code stays as on 26.1.2.
 */
public final class ARGB {
    private ARGB() {}

    public static int alpha(int color) {
        return color >>> 24;
    }

    public static int red(int color) {
        return color >> 16 & 0xFF;
    }

    public static int green(int color) {
        return color >> 8 & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static int color(int alpha, int red, int green, int blue) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }

    public static int color(int red, int green, int blue) {
        return color(255, red, green, blue);
    }

    public static int color(int alpha, int rgb) {
        return alpha << 24 | rgb & 0xFFFFFF;
    }

    public static int color(float alpha, int rgb) {
        return as8BitChannel(alpha) << 24 | rgb & 0xFFFFFF;
    }

    public static int multiply(int lhs, int rhs) {
        if (lhs == -1) return rhs;
        if (rhs == -1) return lhs;
        return color(alpha(lhs) * alpha(rhs) / 255, red(lhs) * red(rhs) / 255, green(lhs) * green(rhs) / 255, blue(lhs) * blue(rhs) / 255);
    }

    public static int multiplyAlpha(int color, float alphaMultiplier) {
        if (color == 0 || alphaMultiplier <= 0.0F) return 0;
        return alphaMultiplier >= 1.0F ? color : color(alphaFloat(color) * alphaMultiplier, color);
    }

    public static int scaleRGB(int color, float scale) {
        return scaleRGB(color, scale, scale, scale);
    }

    public static int scaleRGB(int color, float scaleR, float scaleG, float scaleB) {
        return color(alpha(color),
                Math.clamp((int) (red(color) * scaleR), 0, 255),
                Math.clamp((int) (green(color) * scaleG), 0, 255),
                Math.clamp((int) (blue(color) * scaleB), 0, 255));
    }

    public static int srgbLerp(float alpha, int p0, int p1) {
        return color(Mth.lerpInt(alpha, alpha(p0), alpha(p1)),
                Mth.lerpInt(alpha, red(p0), red(p1)),
                Mth.lerpInt(alpha, green(p0), green(p1)),
                Mth.lerpInt(alpha, blue(p0), blue(p1)));
    }

    public static int opaque(int color) {
        return color | 0xFF000000;
    }

    public static int white(float alpha) {
        return as8BitChannel(alpha) << 24 | 0xFFFFFF;
    }

    public static int white(int alpha) {
        return alpha << 24 | 0xFFFFFF;
    }

    public static int black(float alpha) {
        return as8BitChannel(alpha) << 24;
    }

    public static int colorFromFloat(float alpha, float red, float green, float blue) {
        return color(as8BitChannel(alpha), as8BitChannel(red), as8BitChannel(green), as8BitChannel(blue));
    }

    public static int as8BitChannel(float value) {
        return Mth.floor(value * 255.0F);
    }

    public static float alphaFloat(int color) {
        return alpha(color) / 255.0F;
    }

    public static float redFloat(int color) {
        return red(color) / 255.0F;
    }

    public static float greenFloat(int color) {
        return green(color) / 255.0F;
    }

    public static float blueFloat(int color) {
        return blue(color) / 255.0F;
    }
}
