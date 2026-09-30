package net.ixdarklord.ultimine_addition.client.gui.hud;

import net.ixdarklord.ultimine_addition.client.gui.theme.RecordTheme;
import net.ixdarklord.ultimine_addition.client.gui.GuiDraw;
import net.ixdarklord.coolcatcanvas.api.utils.Easing;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.renderer.ItemAlpha;
import net.ixdarklord.ultimine_addition.common.progression.UltimineNotice;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.ixdarklord.ultimine_addition.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// Notices above the hotbar (Ultimine locked, tier-up rewards, streaks, lucky finds), in their own box: a ribbon title
// bar with pointed ends over a body with an inner highlight line, in the challenge panel's tones.
public class UltimineNoticeHud {
    public static final UltimineNoticeHud INSTANCE = new UltimineNoticeHud();
    private static final ResourceLocation TITLE_SPRITE = FTBUltimineAddition.id("notice/title");
    private static final ResourceLocation BODY_SPRITE = FTBUltimineAddition.id("notice/body");
    private static final ResourceLocation SLOT_FILL = FTBUltimineAddition.id("container/skills_record/card_viewer/slot_small_fill");
    private static final ResourceLocation SLOT_BORDER = FTBUltimineAddition.id("container/skills_record/card_viewer/slot_small_border");
    private static final int SLOT = 20;
    private static final long FADE_IN = 220, FADE_OUT = 320;
    private static final int TITLE_HEIGHT = 15, LINE_HEIGHT = 10, ICON = 16, MIN_WIDTH = 120, MAX_WIDTH = 320;
    // The title sprite ends in points (its bottom edge starts BODY_INSET in from each side); the body hangs from that
    // edge, so it never shows past the points where the two overlap.
    private static final int BODY_INSET = 7;
    // Body inset and sprite border, plus breathing room on the right of the text.
    private static final int TEXT_RIGHT_PAD = BODY_INSET + 6;
    private static final float CAPTION_SCALE = 0.75F;
    private static final int CAPTION_HEIGHT = 7;

    // How far a notice rises while it fades out, and how far below its place it starts when it comes in.
    private static final int SLIDE_UP = 14;
    private static final int SLIDE_IN = 10;

    private @Nullable Entry current;
    // Notices pushed out by a new one, finishing their slide-up fade.
    private final List<Entry> leaving = new ArrayList<>();

    private UltimineNoticeHud() {}

    public void show(UltimineNotice notice) {
        long now = Util.getMillis();
        Entry current = this.current;
        // Still waiting for the previous one to leave: take its place in the queue (the newest notice wins).
        if (current != null && now < current.shownAt) {
            this.current = new Entry(notice, current.shownAt);
            return;
        }
        // Same kind while still up (e.g. a growing streak): swap the text without replaying the entrance.
        if (current != null && current.notice.kind() == notice.kind() && now < current.hideAt) {
            current.notice = notice;
            current.hideAt = now + notice.kind().duration();
            return;
        }
        // A different notice: the one showing slides up and fades out first, then the new one slides in.
        long start = now;
        if (current != null && now < current.hideAt + FADE_OUT) {
            current.hideAt = Math.min(current.hideAt, now);
            this.leaving.add(current);
        }
        for (Entry entry : this.leaving) start = Math.max(start, entry.hideAt + FADE_OUT);
        this.current = new Entry(notice, start);
    }

    // Skips the rest of the countdown and starts fading out now.
    public void dismiss() {
        long now = Util.getMillis();
        if (this.current != null && now < this.current.hideAt) this.current.hideAt = now;
    }

    public void clear() {
        this.current = null;
        this.leaving.clear();
    }

    public void render(GuiGraphics graphics, float ignored) {
        long now = Util.getMillis();
        this.leaving.removeIf(entry -> now > entry.hideAt + FADE_OUT);
        if (this.current != null && now > this.current.hideAt + FADE_OUT) this.current = null;
        // Outgoing notices first, each on its own layer, so the newest draws on top.
        for (Entry entry : this.leaving) {
            this.draw(graphics, entry, now);
            GuiDraw.nextStratum(graphics);
        }
        if (this.current != null) this.draw(graphics, this.current, now);
    }

    private void draw(GuiGraphics graphics, Entry entry, long now) {
        UltimineNotice notice = entry.notice;
        if (now < entry.shownAt) return;
        float in = Mth.clamp((now - entry.shownAt) / (float) FADE_IN, 0.0F, 1.0F);
        float out = now > entry.hideAt ? 1.0F - (now - entry.hideAt) / (float) FADE_OUT : 1.0F;
        out = Mth.clamp(out, 0.0F, 1.0F);
        float alpha = Mth.clamp(Math.min(in, out), 0.0F, 1.0F);
        if (alpha < 0.05F) return;

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        boolean hasIcon = !notice.icon().isEmpty();
        int textLeft = hasIcon ? BODY_INSET + 6 + SLOT + 5 : BODY_INSET + 8;

        // Ultimine Actions notices lead with the action's name as a small caption above the details.
        List<Component> body = notice.lines();
        Component caption = notice.kind() == UltimineNotice.Kind.ACTION && !body.isEmpty() ? body.get(0) : null;
        if (caption != null) body = body.subList(1, body.size());

        int maxWidth = Math.min(MAX_WIDTH, graphics.guiWidth() - 20);
        int width = font.width(notice.title()) + 44;
        if (caption != null) width = Math.max(width, Math.round(font.width(caption) * CAPTION_SCALE) + textLeft + TEXT_RIGHT_PAD);
        for (Component line : body) width = Math.max(width, font.width(line) + textLeft + TEXT_RIGHT_PAD);
        width = Mth.clamp(width, MIN_WIDTH, Math.max(MIN_WIDTH, maxWidth));
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Component line : body) lines.addAll(font.split(line, width - textLeft - TEXT_RIGHT_PAD));
        // Wrapped lines are shorter than the width they were wrapped to: fit the panel to what's actually drawn.
        int fitted = font.width(notice.title()) + 44;
        if (caption != null) fitted = Math.max(fitted, Math.round(font.width(caption) * CAPTION_SCALE) + textLeft + TEXT_RIGHT_PAD);
        for (FormattedCharSequence line : lines) fitted = Math.max(fitted, font.width(line) + textLeft + TEXT_RIGHT_PAD);
        width = Mth.clamp(fitted, MIN_WIDTH, width);
        int captionHeight = caption != null ? CAPTION_HEIGHT : 0;
        int textHeight = captionHeight + lines.size() * LINE_HEIGHT;
        int bodyHeight = textHeight == 0 ? 0 : Math.max(textHeight + 5, hasIcon ? SLOT + 6 : 0);
        int height = TITLE_HEIGHT + (bodyHeight > 0 ? bodyHeight - 3 : 0);

        int bottom = graphics.guiHeight() - 62;
        if (minecraft.gui.overlayMessageTime > 0) bottom -= 12;
        int x = (graphics.guiWidth() - width) / 2;
        // Slides up into place (easing out), and keeps rising as it fades out.
        int y = bottom - height + Math.round((1.0F - Easing.CUBIC_OUT.apply(in)) * SLIDE_IN) - Math.round(Easing.SINE_IN_OUT.apply(1.0F - out) * SLIDE_UP);

        SkillsRecordScreen.OverlayColor theme = RecordTheme.hud().overlay();
        int tint = ARGB.colorFromFloat(theme.alpha() * alpha, theme.red(), theme.green(), theme.blue());
        int accent = ARGB.color(Math.round(alpha * 255), notice.kind().accent());

        if (bodyHeight > 0) {
            int bodyTop = y + TITLE_HEIGHT - 3;
            GuiDraw.blitSprite(graphics, BODY_SPRITE, x + BODY_INSET, bodyTop, width - BODY_INSET * 2, bodyHeight, tint);
            int content = bodyHeight;
            if (hasIcon) {
                int slotY = bodyTop + (content - SLOT) / 2 + 1;
                int slotX = x + BODY_INSET + 6;
                GuiDraw.blitSprite(graphics, SLOT_FILL, slotX, slotY, SLOT, SLOT, ARGB.color(Math.round(alpha * 255), 0x2A2A2A));
                GuiDraw.blitSprite(graphics, SLOT_BORDER, slotX, slotY, SLOT, SLOT, ARGB.color(Math.round(alpha * 255), 0x9A9A9A));
                ItemAlpha.draw(alpha, () -> graphics.renderItem(notice.icon(), slotX + (SLOT - ICON) / 2, slotY + (SLOT - ICON) / 2));
            }
            int lineY = bodyTop + (content - textHeight) / 2 + 2;
            if (caption != null) {
                var pose = graphics.pose();
                pose.pushPose();
                pose.translate(x + textLeft, lineY, 0.0F);
                pose.scale(CAPTION_SCALE, CAPTION_SCALE, 1.0F);
                GuiDraw.text(graphics, font, caption, 0, 0, ARGB.color(Math.round(alpha * 255), 0xE6E6E6), true);
                pose.popPose();
                lineY += captionHeight;
            }
            for (FormattedCharSequence line : lines) {
                GuiDraw.text(graphics, font, line, x + textLeft, lineY, ARGB.color(Math.round(alpha * 255), 0xE6E6E6), true);
                lineY += LINE_HEIGHT;
            }
        }

        GuiDraw.blitSprite(graphics, TITLE_SPRITE, x, y, width, TITLE_HEIGHT, tint);

        GuiDraw.centeredText(graphics, font, notice.title(), x + width / 2, y + 4, accent);
    }

    private static final class Entry {
        private UltimineNotice notice;
        // shownAt drives the entrance; it's in the future while the previous notice is still leaving.
        private final long shownAt;
        private long hideAt;

        private Entry(UltimineNotice notice, long shownAt) {
            this.notice = notice;
            this.shownAt = shownAt;
            this.hideAt = shownAt + notice.kind().duration();
        }
    }
}
