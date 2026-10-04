package net.ixdarklord.ultimine_addition.client.gui.config;

import net.ixdarklord.coolcatcore.api.config.ConfigColorScheme;
import net.ixdarklord.coolcatcore.api.config.ConfigTheme;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

// The config popups' look, copied from Glazed Menu's ConfigStyle (so the Skills Record's settings don't need Glazed):
// shapes with their corner pixels cut, drawn in the current theme's dark colors, and the text helpers.
public final class PopupStyle {
    private static ConfigTheme theme = ConfigTheme.DEFAULT;

    private PopupStyle() {}

    /** Makes a theme the one widgets draw with; popups set theirs before drawing. */
    public static void use(ConfigTheme newTheme) {
        theme = newTheme;
    }

    public static ConfigColorScheme colors() {
        return theme.colors();
    }

    public static int accent() {
        return colors().accent();
    }

    // --- Colors ---

    public static int withAlpha(int color, int alpha) {
        return (alpha & 0xFF) << 24 | color & 0xFFFFFF;
    }

    public static int mix(int from, int to, float t) {
        int a = Math.round((from >>> 24) + ((to >>> 24) - (from >>> 24)) * t);
        int r = Math.round((from >> 16 & 0xFF) + ((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * t);
        int g = Math.round((from >> 8 & 0xFF) + ((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * t);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return a << 24 | r << 16 | g << 8 | b;
    }

    /** Dark text on light colors, light text on dark ones. */
    public static int readableOn(int color) {
        double luminance = 0.2126 * (color >> 16 & 0xFF) + 0.7152 * (color >> 8 & 0xFF) + 0.0722 * (color & 0xFF);
        return luminance > 150 ? 0xFF0B0E14 : 0xFFFFFFFF;
    }

    // --- Shapes ---

    public static void rect(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        if (width <= 2 || height <= 2) {
            graphics.fill(x, y, x + width, y + height, color);
            return;
        }
        graphics.fill(x + 1, y, x + width - 1, y + height, color);
        graphics.fill(x, y + 1, x + 1, y + height - 1, color);
        graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    public static void outline(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x + 1, y, x + width - 1, y + 1, color);
        graphics.fill(x + 1, y + height - 1, x + width - 1, y + height, color);
        graphics.fill(x, y + 1, x + 1, y + height - 1, color);
        graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    // --- Text ---

    public static void text(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int maxWidth, int color) {
        graphics.text(font, ellipsize(font, text, maxWidth), x, y, color, false);
    }

    public static void centeredText(GuiGraphicsExtractor graphics, Font font, Component text, int centerX, int y, int maxWidth, int color) {
        FormattedCharSequence line = ellipsize(font, text, maxWidth);
        graphics.text(font, line, centerX - font.width(line) / 2, y, color, false);
    }

    public static FormattedCharSequence ellipsize(Font font, Component text, int width) {
        if (font.width(text) <= width) return text.getVisualOrderText();
        FormattedText cut = font.substrByWidth(text, Math.max(0, width - font.width("…")));
        return Language.getInstance().getVisualOrder(FormattedText.composite(cut, FormattedText.of("…")));
    }

    // White 64x64 textures (textures/gui/config/icons/) with linear filtering, so they shrink smoothly; tinted as drawn.
    public record Icon(Identifier texture) {
        public static final Icon RESET = of("reset");
        public static final Icon CHECK = of("check");
        public static final Icon CHEVRON = of("chevron");
        public static final Icon CHEVRON_LEFT = of("chevron_left");
        public static final Icon CLIENT = of("client");

        /** The size icons are drawn at, in GUI pixels. */
        public static final int SIZE = 10;
        private static final int TEXTURE_SIZE = 64;

        private static Icon of(String name) {
            return new Icon(FTBUltimineAddition.id("textures/gui/config/icons/" + name + ".png"));
        }

        public void draw(GuiGraphicsExtractor graphics, int x, int y, int color) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, x, y, 0, 0, SIZE, SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, color);
        }

        /** Draws the icon centered in a box. */
        public void drawCentered(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
            this.draw(graphics, x + (width - SIZE) / 2, y + (height - SIZE) / 2, color);
        }
    }
}
