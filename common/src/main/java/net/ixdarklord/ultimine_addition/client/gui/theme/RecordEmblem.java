package net.ixdarklord.ultimine_addition.client.gui.theme;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.ixdarklord.ultimine_addition.util.ARGB;
import net.ixdarklord.ultimine_addition.client.gui.GuiDraw;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * How a dyed record's emblem moves in the card viewer's background: large and faint, each in its own way (a snowflake
 * spinning, a pickaxe mining, a bee on its rounds...). Computed from the time alone.
 */
public enum RecordEmblem {
    /** Turning slowly, bobbing a little: a snowflake. */
    SPIN,
    /** Glowing unevenly, like a jack o'lantern's candle. */
    GLOW,
    /** Swaying from its foot, in the breeze: a flower, a tree. */
    SWAY,
    /** Sailing across the view: a cloud. */
    DRIFT,
    /** Flying figure eights, turning to face its way: a bee. */
    FLY,
    /** Hopping, squashing as it lands: a slime. */
    BOUNCE,
    /** Falling from the top, spinning and turning over: a petal. */
    FLUTTER,
    /** Striking down and raising again, from its handle: a pickaxe. */
    MINE,
    /** Falling, stretching, and splashing at the bottom: a raindrop. */
    DROP,
    /** Swimming back and forth, turning at each end: a fish. */
    SWIM,
    /** Growing and fading in a slow pulse: a crystal. */
    PULSE,
    /** Spinning like a coin: a gem. */
    COIN,
    /** Breathing, taller and narrower in turn: a mushroom. */
    BREATHE,
    /** Flickering from its base: a flame. */
    FLICKER,
    /** Turning and twinkling: a star. */
    TWINKLE;

    private static final float ALPHA = 0.22F;

    // The world is cut into cells; some hold an emblem, of a size and at a spot of their own, moving in its own time.
    private static final int CELL = 120;
    // Whole multiples of the 9px emblem keep its pixels even.
    private static final int[] SIZES = {18, 27, 36, 45};

    /**
     * Draws emblems scattered over the given world area (the caller's pose maps world to screen, so they pan and zoom
     * with the view).
     */
    public void drawScattered(GuiGraphics graphics, double left, double top, double right, double bottom, ResourceLocation sprite, boolean animated) {
        float time = animated ? (System.currentTimeMillis() % 3_600_000L) / 1000.0F : 0.0F;
        int cx0 = Mth.floor((left - CELL) / CELL), cx1 = Mth.floor(right / CELL);
        int cy0 = Mth.floor((top - CELL) / CELL), cy1 = Mth.floor(bottom / CELL);
        for (int cy = cy0; cy <= cy1; cy++) {
            for (int cx = cx0; cx <= cx1; cx++) {
                int seed = Mth.murmurHash3Mixer(cx * 73856093 ^ cy * 19349663 ^ this.ordinal() * 83492791);
                if ((seed >>> 24 & 0xFF) < 115) continue;
                int size = SIZES[seed >>> 4 & 3];
                // The emblem moves within a box of the cell, placed at random.
                float room = CELL - 16 - size;
                float bw = size + room * 0.55F, bh = size + room * 0.55F;
                float bl = cx * CELL + 8 + (room - room * 0.55F) * ((seed >>> 8 & 0xFF) / 255.0F);
                float bt = cy * CELL + 8 + (room - room * 0.55F) * ((seed >>> 16 & 0xFF) / 255.0F);
                float offset = animated ? (seed & 0xFFFF) / 97.0F : 0.0F;
                this.drawOne(graphics, bl, bt, bw, bh, size, sprite, time + offset);
            }
        }
    }

    private void drawOne(GuiGraphics graphics, float bl, float bt, float w, float h, int size, ResourceLocation sprite, float t) {
        float half = size / 2.0F;
        float homeX = bl + w / 2, homeY = bt + h / 2;
        float midX = homeX, midY = homeY;

        float x = homeX, y = homeY, angle = 0, sx = 1, sy = 1, alpha = ALPHA;
        // Where it turns and scales from, relative to its center.
        float pivotX = 0, pivotY = 0;
        switch (this) {
            case SPIN -> {
                angle = t * 0.5F;
                y += Mth.sin(t * 0.8F) * 2;
            }
            case GLOW -> {
                float s = 1 + 0.04F * Mth.sin(t * 2);
                sx = sy = s;
                alpha = ALPHA * (0.8F + 0.25F * (Mth.sin(t * 7.3F) * 0.5F + Mth.sin(t * 3.1F) * 0.5F));
            }
            case SWAY -> {
                pivotY = half;
                angle = 0.14F * Mth.sin(t * 1.3F);
            }
            case DRIFT -> {
                float across = Mth.frac(t / 30.0F);
                x = bl - half + across * (w + size);
                y = bt + h * 0.3F + Mth.sin(t * 0.7F) * 3;
                // Fading in and out at the ends of its path, so it never pops.
                alpha = ALPHA * 0.85F * Mth.sin(across * Mth.PI);
            }
            case FLY -> {
                x = midX + Mth.sin(t * 0.5F) * Math.max(0, w / 2 - half - 4) * 0.9F;
                y = midY + Mth.sin(t * 1.0F) * Math.max(0, h / 2 - half - 4) * 0.8F;
                // The bee faces left; flipped while it flies right.
                sx = Mth.cos(t * 0.5F) > 0 ? -1 : 1;
                angle = 0.08F * Mth.sin(t * 9);
            }
            case BOUNCE -> {
                float phase = Mth.frac(t * 1.1F);
                float height = 4 * phase * (1 - phase);
                y -= height * 18;
                float squash = Math.max(0, 1 - height * 6);
                pivotY = half;
                sx = 1 + 0.18F * squash;
                sy = 1 - 0.18F * squash;
            }
            case FLUTTER -> {
                float fall = Mth.frac(t / 14.0F);
                y = bt - half + fall * (h + size);
                x = bl + w * 0.5F + Mth.sin(t * 1.1F) * Math.min(16, w / 3);
                alpha = ALPHA * Mth.sin(fall * Mth.PI);
                angle = Mth.sin(t * 0.9F) * 0.6F;
                sx = 0.4F + 0.6F * Math.abs(Mth.cos(t * 1.6F));
            }
            case MINE -> {
                // The handle's end, at the lower left, holds still.
                pivotX = -half;
                pivotY = half;
                float phase = Mth.frac(t * 0.7F);
                if (phase < 0.18F) {
                    float p = phase / 0.18F;
                    angle = Mth.lerp(p * p * p, -0.5F, 0.35F);
                } else {
                    float p = (phase - 0.18F) / 0.82F;
                    angle = Mth.lerp(1 - (1 - p) * (1 - p), 0.35F, -0.5F);
                    // A little shake on the hit.
                    if (p < 0.08F) x += Mth.sin(p * 200) * 0.8F;
                }
            }
            case DROP -> {
                float phase = Mth.frac(t / 2.6F);
                if (phase < 0.7F) {
                    float p = phase / 0.7F;
                    y = Mth.lerp(p * p, bt - half, homeY);
                    alpha = ALPHA * Mth.clamp(p / 0.15F, 0.0F, 1.0F);
                    sy = 1 + 0.15F * p;
                    sx = 1 - 0.08F * p;
                } else {
                    float p = (phase - 0.7F) / 0.3F;
                    pivotY = half;
                    sx = 1 + 0.4F * p;
                    sy = 1 - 0.5F * p;
                    alpha = ALPHA * (1 - p);
                }
            }
            case SWIM -> {
                x = midX + Mth.sin(t * 0.35F) * Math.max(0, w / 2 - half);
                y = homeY + Mth.sin(t * 1.7F) * 3;
                // The fish faces left; flipped while it swims right.
                sx = Mth.cos(t * 0.35F) > 0 ? -1 : 1;
                angle = 0.06F * Mth.sin(t * 6);
            }
            case PULSE -> {
                float wave = (Mth.sin(t * 1.8F) + 1) / 2;
                sx = sy = 1 + 0.08F * (wave * 2 - 1);
                alpha = ALPHA * (0.8F + 0.4F * wave);
                angle = 0.05F * Mth.sin(t * 0.7F);
            }
            case COIN -> {
                float c = Mth.cos(t * 1.4F);
                sx = Math.abs(c) < 0.08F ? Math.copySign(0.08F, c) : c;
                y += Mth.sin(t * 0.9F) * 2;
            }
            case BREATHE -> {
                pivotY = half;
                float s = Mth.sin(t * 1.5F);
                sy = 1 + 0.07F * s;
                sx = 1 - 0.05F * s;
            }
            case FLICKER -> {
                pivotY = half;
                float n = Mth.sin(t * 9) * 0.5F + Mth.sin(t * 13.7F) * 0.3F + Mth.sin(t * 5.1F) * 0.2F;
                sy = 1 + 0.1F * n;
                sx = 1 - 0.05F * n;
                alpha = ALPHA * (0.85F + 0.2F * n);
            }
            case TWINKLE -> {
                float spark = Math.abs(Mth.sin(t * 1.9F));
                angle = t * 0.6F;
                sx = sy = 0.85F + 0.2F * spark;
                alpha = ALPHA * (0.7F + 0.5F * spark);
            }
            default -> {}
        }

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x + pivotX, y + pivotY, 0.0F);
        pose.mulPose(Axis.ZP.rotation(angle));
        pose.scale(sx, sy, 1.0F);
        pose.translate(-pivotX, -pivotY, 0.0F);
        GuiDraw.blitSprite(graphics, sprite, Math.round(-half), Math.round(-half), size, size,
                ARGB.white(Mth.clamp(alpha, 0.0F, 1.0F)));
        pose.popPose();
    }
}
