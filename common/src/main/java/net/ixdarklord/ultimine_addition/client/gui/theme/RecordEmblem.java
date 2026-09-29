package net.ixdarklord.ultimine_addition.client.gui.theme;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;

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

    /** Draws the emblem in the given area (the caller clips to it). */
    public void draw(GuiGraphicsExtractor graphics, ScreenRectangle b, Identifier sprite, boolean animated) {
        if (b.width() <= 0 || b.height() <= 0) return;
        float t = animated ? (System.currentTimeMillis() % 3_600_000L) / 1000.0F : 0.0F;
        // Whole multiples of the 9px emblem keep its pixels even.
        int size = Math.min(b.width(), b.height()) >= 80 ? 36 : 27;
        float half = size / 2.0F;
        float w = b.width(), h = b.height();
        float homeX = b.right() - half - 10, homeY = b.bottom() - half - 10;
        float midX = b.left() + w / 2, midY = b.top() + h / 2;

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
                x = b.left() - half + Mth.frac(t / 30.0F) * (w + size);
                y = b.top() + h * 0.3F + Mth.sin(t * 0.7F) * 3;
                alpha = ALPHA * 0.85F;
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
                y = b.top() - half + fall * (h + size);
                x = b.left() + w * 0.7F + Mth.sin(t * 1.1F) * 16;
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
                    y = Mth.lerp(p * p, b.top() - half, homeY);
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
                x = midX + Mth.sin(t * 0.35F) * Math.max(0, w / 2 - half - 6);
                y = homeY - h * 0.25F + Mth.sin(t * 1.7F) * 3;
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

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x + pivotX, y + pivotY);
        pose.rotate(angle);
        pose.scale(sx, sy);
        pose.translate(-pivotX, -pivotY);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, Math.round(-half), Math.round(-half), size, size,
                ARGB.white(Mth.clamp(alpha, 0.0F, 1.0F)));
        pose.popMatrix();
    }
}
