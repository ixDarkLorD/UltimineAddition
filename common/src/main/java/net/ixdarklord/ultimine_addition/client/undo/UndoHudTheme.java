package net.ixdarklord.ultimine_addition.client.undo;

import net.ixdarklord.ultimine_addition.client.gui.theme.RecordTheme;
import net.minecraft.network.chat.FontDescription;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.config.UAClientConfig;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

// The undo panels' look: their own title bar (squared plates held by screws) and body (bracket corners), in the same
// tones as the notices and the challenge panel and tinted with the client's Skills Record background colour, plus the
// card viewer's small slots. Drawn a little smaller than 1:1, centred under the crosshair.
final class UndoHudTheme {
    static final float SCALE = 0.85F;
    static final int TITLE_HEIGHT = 15, SLOT = 20, PAD = 8;
    static final int TEXT = 0xE6E6E6;
    private static final Identifier TITLE_SPRITE = FTBUltimineAddition.id("undo/title");
    private static final Identifier BODY_SPRITE = FTBUltimineAddition.id("undo/body");
    private static final Identifier SLOT_FILL = FTBUltimineAddition.id("container/skills_record/card_viewer/slot_small_fill");
    private static final Identifier SLOT_BORDER = FTBUltimineAddition.id("container/skills_record/card_viewer/slot_small_border");

    private static final FontDescription ICON_FONT = new FontDescription.Resource(FTBUltimineAddition.id("icons"));

    private UndoHudTheme() {}

    // Gap between the crosshair and the panel's top.
    private static final float BELOW_CROSSHAIR = 14.0F;

    // Starts drawing a panel of this (unscaled) width in its own coordinates, (0, 0) at its top-left. Pair with end().
    static void begin(GuiGraphicsExtractor graphics, int width) {
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(graphics.guiWidth() / 2.0F, graphics.guiHeight() / 2.0F + BELOW_CROSSHAIR);
        pose.scale(SCALE, SCALE);
        pose.translate(-width / 2.0F, 0.0F);
    }

    static void end(GuiGraphicsExtractor graphics) {
        graphics.pose().popMatrix();
    }

    // The body first, then the title bar over its top edge with the title centred in the accent colour.
    // Returns where the body's content starts.
    static int frame(GuiGraphicsExtractor graphics, int width, int height, Component title, int accent, float alpha) {
        SkillsRecordScreen.OverlayColor theme = RecordTheme.active().overlay();
        int tint = ARGB.colorFromFloat(theme.alpha() * alpha, theme.red(), theme.green(), theme.blue());
        int bodyTop = TITLE_HEIGHT - 3;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BODY_SPRITE, 2, bodyTop, width - 4, height - bodyTop, tint);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TITLE_SPRITE, 0, 0, width, TITLE_HEIGHT, tint);
        graphics.centeredText(Minecraft.getInstance().font, title, width / 2, 4, ARGB.color(alpha(alpha), accent));
        return TITLE_HEIGHT + 2;
    }

    static void slot(GuiGraphicsExtractor graphics, int x, int y, float alpha) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_FILL, x, y, SLOT, SLOT, ARGB.color(alpha(alpha), 0x2A2A2A));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_BORDER, x, y, SLOT, SLOT, ARGB.color(alpha(alpha), 0x9A9A9A));
    }

    // A thin bar: dark track, filled from the left.
    static void bar(GuiGraphicsExtractor graphics, int x, int y, int width, float fraction, int color, float alpha) {
        graphics.fill(x, y, x + width, y + 2, ARGB.color(alpha(alpha) / 2, 0x000000));
        int filled = Math.round(width * fraction);
        if (filled > 0) graphics.fill(x, y, x + filled, y + 2, ARGB.color(alpha(alpha), color));
    }

    // The revert glyph (font/icons.json, textures/font/revert.png) followed by text. The text is a sibling so it
    // doesn't take the icon font.
    static Component withRevertIcon(Component text) {
        return Component.empty().append(Component.literal("").withStyle(style -> style.withFont(ICON_FONT))).append(" ").append(text);
    }

    static int alpha(float alpha) {
        return Math.round(alpha * 255);
    }
}
