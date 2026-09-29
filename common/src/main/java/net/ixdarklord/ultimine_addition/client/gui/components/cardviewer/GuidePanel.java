package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.coolcatcanvas.api.client.gui.components.widgets.panel.Panel;
import net.ixdarklord.coolcatcanvas.api.client.utils.RenderUtils;
import net.ixdarklord.coolcatcanvas.api.utils.Easing;
import net.ixdarklord.ultimine_addition.client.gui.components.SlotSelectionOutline;
import net.ixdarklord.ultimine_addition.client.renderer.ItemAlpha;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

import java.util.List;

// Shown in place of the tree while there's no card to show: a plate in the tree's style with a looping animation of
// what to do (a card dropping into a card slot, or a right click on a card that selects it) beside a title and a hint.
final class GuidePanel extends Panel {
    enum Kind {
        NO_CARDS("no_cards", 2400L),
        SELECT_CARD("select_card", 2800L);

        private final Component title;
        private final Component hint;
        private final long loop;

        Kind(String key, long loop) {
            this.title = Component.translatable("gui.ultimine_addition.card_viewer.guide." + key + ".title");
            this.hint = Component.translatable("gui.ultimine_addition.card_viewer.guide." + key + ".hint").withStyle(ChatFormatting.GRAY);
            this.loop = loop;
        }
    }

    private static final Identifier PLATE_FILL = sprite("tier_fill");
    private static final Identifier PLATE_BORDER = sprite("tier_border");
    private static final Identifier SLOT_SELECT = FTBUltimineAddition.id("container/skills_record/slot_select");
    private static final List<ItemStack> CARDS = List.of(new ItemStack(ModItems.MINING_SKILL_CARD_PICKAXE),
            new ItemStack(ModItems.MINING_SKILL_CARD_AXE), new ItemStack(ModItems.MINING_SKILL_CARD_SHOVEL),
            new ItemStack(ModItems.MINING_SKILL_CARD_HOE));

    private static final int STAGE = 44;
    // An inventory slot like the Skills Record's own: 16px inside a bevel.
    private static final int SLOT = 18;
    private static final int PADDING = 8;
    private static final int LINE_HEIGHT = 10;
    private static final float MIN_TEXT_SCALE = 0.5F;
    private static final int PLATE_FILL_COLOR = 0xF0232220;
    private static final int TITLE_COLOR = 0xFFFBF1C1;
    private static final int SELECTED = 0xFF6BCB6B;
    private static final int CLICK = 0xFFFFD86A;

    private final CardViewerWidget viewer;
    private Kind kind = Kind.NO_CARDS;
    private long shownAt;
    private int tint = 0xFFFFFFFF;

    GuidePanel(CardViewerWidget viewer) {
        this.viewer = viewer;
        this.setVisible(false);
    }

    private static Identifier sprite(String name) {
        return FTBUltimineAddition.id("container/skills_record/card_viewer/" + name);
    }

    void show(Kind kind) {
        if (kind != this.kind || !this.isVisible()) this.shownAt = Util.getMillis();
        this.kind = kind;
        this.setVisible(true);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ScreenRectangle b = this.getBounds();
        boolean animated = this.viewer.isAnimated();
        int accent = this.viewer.getAccentColor();
        this.tint = ARGB.opaque(accent);
        graphics.enableScissor(b.left(), b.top(), b.right(), b.bottom());
        TierTreePanel.drawGradient(graphics, b, this.viewer.getTheme(), animated);
        TierTreePanel.drawCards(graphics, b.left(), b.top(), b.right(), b.bottom(), 1.0, this.viewer.getTheme(), animated);
        TierTreePanel.drawTheme(graphics, b.left(), b.top(), b.right(), b.bottom(), 1.0, this.viewer.getTheme(), animated);

        long elapsed = Util.getMillis() - this.shownAt;
        // The plate rises in when shown; without animations everything sits at its final frame.
        float intro = animated ? Easing.CUBIC_OUT.apply(Mth.clamp(elapsed / 300.0F, 0.0F, 1.0F)) : 1.0F;
        float phase = animated ? (elapsed % this.kind.loop) / (float) this.kind.loop : 0.0F;
        int cycle = animated ? (int) (elapsed / this.kind.loop) : 0;

        int textWidth = Math.min(150, b.width() - STAGE - PADDING * 3 - 8);
        int plateW = Math.min(b.width() - 8, STAGE + textWidth + PADDING * 3);
        int areaW = Math.max(20, plateW - STAGE - PADDING * 3), areaH = Math.max(LINE_HEIGHT, b.height() - 8 - PADDING * 2);
        // Longer translations shrink the text until the title fits on its line and the hint fits the plate.
        float textScale = 1.0F;
        List<FormattedCharSequence> hint;
        int blockHeight;
        while (true) {
            hint = this.font.split(this.kind.hint, Math.round(areaW / textScale));
            blockHeight = LINE_HEIGHT + 5 + hint.size() * LINE_HEIGHT;
            boolean fits = blockHeight * textScale <= areaH && this.font.width(this.kind.title) * textScale <= areaW;
            if (fits || textScale <= MIN_TEXT_SCALE) break;
            textScale = Math.max(MIN_TEXT_SCALE, textScale - 0.05F);
        }
        int textHeight = Mth.ceil(blockHeight * textScale);
        int plateH = Math.min(b.height() - 8, Math.max(STAGE, textHeight) + PADDING * 2);
        int px = b.left() + (b.width() - plateW) / 2, py = b.top() + (b.height() - plateH) / 2;

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(0, (1.0F - intro) * 8.0F);
        int tint = ARGB.opaque(accent);
        float pulse = animated ? (Mth.sin(elapsed / 1000.0F * 2.4F) + 1.0F) / 2.0F : 0.5F;
        int border = ARGB.multiply(ARGB.srgbLerp(pulse, 0xFF8A8577, 0xFFDCD1B2), tint);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PLATE_FILL, px + 2, py + 2, plateW, plateH, ARGB.color(0.35F * intro, 0x000000));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PLATE_FILL, px, py, plateW, plateH, ARGB.multiplyAlpha(ARGB.multiply(PLATE_FILL_COLOR, tint), intro));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PLATE_BORDER, px, py, plateW, plateH, ARGB.multiplyAlpha(border, intro));

        int sx = px + PADDING, sy = py + (plateH - STAGE) / 2;
        graphics.fill(sx, sy, sx + STAGE, sy + STAGE, ARGB.multiplyAlpha(0x40000000, intro));
        graphics.fill(sx + STAGE + PADDING / 2, py + 6, sx + STAGE + PADDING / 2 + 1, py + plateH - 6, ARGB.multiplyAlpha(0x30FFFFFF, intro));
        ItemStack card = CARDS.get(Math.floorMod(cycle, CARDS.size()));
        if (this.kind == Kind.NO_CARDS) this.drawInsert(graphics, sx, sy, phase, animated, card, intro);
        else this.drawSelect(graphics, sx, sy, phase, animated, card, intro);

        int tx = sx + STAGE + PADDING, ty = py + (plateH - textHeight) / 2;
        boolean shadow = this.viewer.hasTextShadow();
        int width = Math.round(areaW / textScale);
        pose.pushMatrix();
        pose.translate(tx, ty);
        pose.scale(textScale, textScale);
        // Still too long at the smallest size: the title is cut with an ellipsis.
        CardViewerWidget.drawFittedText(graphics, this.font, this.kind.title,
                w -> CardViewerWidget.ellipsize(this.font, this.kind.title.getString(), w),
                0, 0, width, ARGB.multiplyAlpha(TITLE_COLOR, intro), shadow);
        graphics.fill(0, LINE_HEIGHT + 1, width, LINE_HEIGHT + 2, ARGB.multiplyAlpha(0x30FFFFFF, intro));
        int y = LINE_HEIGHT + 5;
        for (FormattedCharSequence line : hint) {
            graphics.text(this.font, line, 0, y, ARGB.multiplyAlpha(RenderUtils.textColor(0xFFFFFF), intro), shadow);
            y += LINE_HEIGHT;
        }
        pose.popMatrix();
        pose.popMatrix();
        graphics.disableScissor();
    }

    // A card comes down from above, eases into the slot, the slot lights up, then everything fades for the next card.
    private void drawInsert(GuiGraphicsExtractor graphics, int sx, int sy, float phase, boolean animated, ItemStack card, float intro) {
        int slotX = sx + (STAGE - SLOT) / 2, slotY = sy + (STAGE - SLOT) / 2;
        float drop = Easing.CUBIC_OUT.apply(Mth.clamp((phase - 0.12F) / 0.4F, 0.0F, 1.0F));
        float landed = Mth.clamp((phase - 0.52F) / 0.12F, 0.0F, 1.0F);
        float glow = phase < 0.52F ? 0.0F : 1.0F - Mth.clamp((phase - 0.52F) / 0.35F, 0.0F, 1.0F);
        float fade = 1.0F - Mth.clamp((phase - 0.86F) / 0.14F, 0.0F, 1.0F);
        float appear = Mth.clamp(phase / 0.12F, 0.0F, 1.0F);
        // Without animations it stays on the card on its way down to the slot.
        if (!animated) { drop = 0.55F; landed = 0.0F; glow = 0.0F; fade = 1.0F; appear = 1.0F; }

        // Once the card is in, the slot is outlined in a light shade of the record's color.
        // It fades away with the card at the end of the loop.
        this.drawSlot(graphics, slotX, slotY, landed * fade, ARGB.srgbLerp(0.35F, this.tint, 0xFFFFFFFF), intro);
        if (glow > 0.0F) {
            int grow = Math.round((1.0F - glow) * 4.0F);
            outline(graphics, slotX - grow, slotY - grow, SLOT + grow * 2, SLOT + grow * 2, ARGB.color(glow * 0.8F * intro, SELECTED));
        }
        // The chevron points at the empty slot until the card arrives.
        float bob = animated ? Mth.sin(phase * Mth.TWO_PI * 2.0F) * 1.5F : 0.0F;
        float arrow = (1.0F - landed) * intro;
        if (arrow > 0.0F) chevron(graphics, slotX + SLOT / 2, slotY - 7 + Math.round(bob), ARGB.color(arrow * 0.8F, 0xDCD1B2));

        float alpha = appear * fade * intro;
        if (alpha <= 0.01F) return;
        // It comes in through the top of the stage's frame.
        float y = Mth.lerp(drop, sy - 16.0F, slotY + (SLOT - 16) / 2.0F);
        graphics.enableScissor(sx, sy, sx + STAGE, sy + STAGE);
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(slotX + (SLOT - 16) / 2.0F, y);
        ItemAlpha.draw(alpha, () -> graphics.item(card, 0, 0));
        pose.popMatrix();
        graphics.disableScissor();
    }

    // The mouse moves onto a card in its slot, its right button flashes with a ripple, and the slot turns selected.
    private void drawSelect(GuiGraphicsExtractor graphics, int sx, int sy, float phase, boolean animated, ItemStack card, float intro) {
        // Centered, a little low to leave room for the selection marker above it (as over the record's card slots).
        int slotX = sx + (STAGE - SLOT) / 2, slotY = sy + (STAGE - SLOT) / 2 + 3;
        float move = Easing.CUBIC_IN_OUT.apply(Mth.clamp((phase - 0.05F) / 0.35F, 0.0F, 1.0F));
        boolean pressed = phase > 0.42F && phase < 0.62F;
        float ripple = Mth.clamp((phase - 0.45F) / 0.3F, 0.0F, 1.0F);
        float selected = Mth.clamp((phase - 0.5F) / 0.1F, 0.0F, 1.0F) * (1.0F - Mth.clamp((phase - 0.88F) / 0.12F, 0.0F, 1.0F));
        // Without animations it stays on the click itself.
        if (!animated) { move = 1.0F; pressed = true; ripple = 0.45F; selected = 1.0F; }

        this.drawSlot(graphics, slotX, slotY, 0.0F, SELECTED, intro);
        graphics.item(card, slotX + (SLOT - 16) / 2, slotY + (SLOT - 16) / 2);
        if (selected > 0.0F) {
            // Standing on the slot's bevel, as over the record's card slots, with the same outline.
            int markerX = slotX + (SLOT - 4) / 2, markerY = slotY - 8;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SELECT, markerX, markerY, 4, 8, ARGB.color(selected * intro, 0xFFFFFF));
            SlotSelectionOutline.draw(graphics, slotX + 1, slotY + 1, markerX, 4, markerY, this.tint, selected * intro);
        }

        // Items draw after fills in a stratum, so the mouse needs its own to sit over the card.
        graphics.nextStratum();
        // The cursor's tip ends at the card's center.
        float mx = Mth.lerp(move, sx + STAGE + 2.0F, slotX + SLOT / 2.0F);
        float my = Mth.lerp(move, sy + STAGE + 2.0F, slotY + SLOT / 2.0F);
        // It comes in from the stage's lower right corner.
        graphics.enableScissor(sx, sy, sx + STAGE, sy + STAGE);
        // The click pulses out from the card's edges, evenly on every side.
        if (ripple > 0.0F && ripple < 1.0F) {
            int grow = Math.round(ripple * 7);
            outline(graphics, slotX + 1 - grow, slotY + 1 - grow, 16 + grow * 2, 16 + grow * 2, ARGB.color((1.0F - ripple) * 0.9F * intro, CLICK));
        }
        cursor(graphics, Math.round(mx), Math.round(my), pressed, intro);
        mouse(graphics, sx + STAGE - 10, sy + STAGE - 13, pressed, intro);
        graphics.disableScissor();
    }

    // The slot's bevel (dark top and left, light bottom and right) in the record's color, with an outline around it
    // for its state (filled, or selected) at the given strength.
    private void drawSlot(GuiGraphicsExtractor graphics, int x, int y, float highlight, int highlightColor, float intro) {
        graphics.fill(x, y, x + SLOT, y + SLOT, ARGB.multiplyAlpha(ARGB.multiply(0xFF8B8B8B, this.tint), intro));
        graphics.fill(x, y, x + SLOT - 1, y + 1, ARGB.multiplyAlpha(ARGB.multiply(0xFF373737, this.tint), intro));
        graphics.fill(x, y + 1, x + 1, y + SLOT - 1, ARGB.multiplyAlpha(ARGB.multiply(0xFF373737, this.tint), intro));
        graphics.fill(x + 1, y + SLOT - 1, x + SLOT, y + SLOT, ARGB.multiplyAlpha(ARGB.multiply(0xFFFFFFFF, this.tint), intro));
        graphics.fill(x + SLOT - 1, y + 1, x + SLOT, y + SLOT - 1, ARGB.multiplyAlpha(ARGB.multiply(0xFFFFFFFF, this.tint), intro));
        if (highlight > 0.0F) outline(graphics, x - 1, y - 1, SLOT + 2, SLOT + 2, ARGB.color(highlight * intro, highlightColor));
    }

    // A pixel cursor arrow with its tip at x, y (pressed nudges it down a pixel, like a click).
    private static final String[] CURSOR = {
            "X.......",
            "XX......",
            "XWX.....",
            "XWWX....",
            "XWWWX...",
            "XWWWWX..",
            "XWWWWWX.",
            "XWWWWWWX",
            "XWWWXXXX",
            "XWWX....",
            "XXX.....",
    };

    private static void cursor(GuiGraphicsExtractor graphics, int x, int y, boolean pressed, float intro) {
        int edge = ARGB.multiplyAlpha(0xFF1A1A1A, intro), fill = ARGB.multiplyAlpha(0xFFF4F4F4, intro);
        if (pressed) y += 1;
        for (int row = 0; row < CURSOR.length; row++) {
            String line = CURSOR[row];
            for (int col = 0; col < line.length(); col++) {
                char c = line.charAt(col);
                if (c != '.') graphics.fill(x + col, y + row, x + col + 1, y + row + 1, c == 'X' ? edge : fill);
            }
        }
    }

    // A small mouse (8 x 11) at x, y, showing which button to press: its right button lights up while pressed.
    private static void mouse(GuiGraphicsExtractor graphics, int x, int y, boolean pressed, float intro) {
        int body = ARGB.multiplyAlpha(0xFFD8D8D8, intro), edge = ARGB.multiplyAlpha(0xFF2A2A2A, intro);
        graphics.fill(x + 1, y, x + 7, y + 11, edge);
        graphics.fill(x, y + 1, x + 8, y + 10, edge);
        graphics.fill(x + 1, y + 1, x + 7, y + 10, body);
        graphics.fill(x + 1, y + 4, x + 7, y + 5, edge);
        graphics.fill(x + 3, y + 1, x + 5, y + 4, edge);
        int right = pressed ? CLICK : 0xFFB0B0B0;
        graphics.fill(x + 5, y + 1, x + 7, y + 4, ARGB.multiplyAlpha(right, intro));
    }

    private static void chevron(GuiGraphicsExtractor graphics, int cx, int y, int color) {
        for (int i = 0; i < 4; i++) {
            graphics.fill(cx - 4 + i, y + i, cx - 2 + i, y + i + 1, color);
            graphics.fill(cx + 2 - i, y + i, cx + 4 - i, y + i + 1, color);
        }
    }

    private static void outline(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y + 1, x + 1, y + h - 1, color);
        graphics.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }
}
