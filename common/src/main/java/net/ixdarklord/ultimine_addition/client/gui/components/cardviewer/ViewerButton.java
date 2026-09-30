package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.ixdarklord.ultimine_addition.util.ARGB;
import net.ixdarklord.ultimine_addition.client.gui.theme.RecordTheme;
import net.ixdarklord.ultimine_addition.client.gui.GuiDraw;
import net.ixdarklord.ultimine_addition.config.UAClientConfig;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

final class ViewerButton extends AbstractButton {
    private final OnPress onPress;
    private float textScale = 1.0F;

    interface OnPress {
        void onPress(ViewerButton button);
    }

    ViewerButton(int width, Component message, OnPress onPress) {
        super(0, 0, width, 12, message);
        this.onPress = onPress;
    }

    // Draws the label smaller (e.g. 0.75 for 6px text instead of 8px), centered as before.
    ViewerButton withTextScale(float scale) {
        this.textScale = scale;
        return this;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.active && this.isHoveredOrFocused();
        // The Skills Record's own button, tinted like the book (as in the edit challenge screen).
        GuiDraw.blitSprite(graphics, SkillsRecordScreen.BUTTON_SPRITES.get(this.active, hovered),
                this.getX(), this.getY(), this.getWidth(), this.getHeight(), RecordTheme.active().overlay().argb());
        int color = !this.active ? 0xFFA0A0A0 : 0xFFFFFFFF;
        Font font = Minecraft.getInstance().font;
        // Labels too long for the button (longer translations) shrink to fit it.
        int textWidth = font.width(this.getMessage());
        float scale = textWidth > 0 ? Math.min(this.textScale, (this.getWidth() - 4) / (float) textWidth) : this.textScale;
        if (scale >= 1.0F) {
            GuiDraw.centeredText(graphics, font, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 8) / 2, color);
            return;
        }
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(this.getX() + this.getWidth() / 2.0F, this.getY() + this.getHeight() / 2.0F, 0.0F);
        pose.scale(scale, scale, 1.0F);
        GuiDraw.centeredText(graphics, font, this.getMessage(), 0, -4, color);
        pose.popPose();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    static void drawFrame(GuiGraphics graphics, int x, int y, int width, int height, boolean hovered) {
        drawFrame(graphics, x, y, width, height, hovered, 1.0F);
    }

    static void drawFrame(GuiGraphics graphics, int x, int y, int width, int height, boolean hovered, float alpha) {
        int outline = ARGB.multiplyAlpha(hovered ? 0xFFFFFFFF : 0xFF8A8A8A, alpha);
        graphics.fill(x, y, x + width, y + height, ARGB.multiplyAlpha(hovered ? 0xE0404040 : 0xC0202020, alpha));
        graphics.fill(x, y, x + width, y + 1, outline);
        graphics.fill(x, y + height - 1, x + width, y + height, outline);
        graphics.fill(x, y, x + 1, y + height, outline);
        graphics.fill(x + width - 1, y, x + width, y + height, outline);
    }

    static final class Icon extends AbstractButton {
        private final Consumer<Icon> onPress;
        private final IconPainter painter;
        // Fades in when shown and out when hidden, instead of popping.
        private static final float FADE_SECONDS = 0.15F;
        private boolean shown;
        private float fade;
        private long lastFrame = -1L;

        interface IconPainter {
            void paint(GuiGraphics graphics, int x, int y, int size, int color);
        }

        Icon(int size, Component tooltip, IconPainter painter, Consumer<Icon> onPress) {
            super(0, 0, size, size, CommonComponents.EMPTY);
            this.painter = painter;
            this.onPress = onPress;
            this.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip));
        }

        @Override
        public void onPress() {
            this.onPress.accept(this);
        }

        /** Shows or hides the button: it's clickable only while shown, and fades (when animated) either way. */
        void setShown(boolean shown, boolean animated) {
            this.shown = shown;
            this.active = shown;
            if (!animated) this.fade = shown ? 1.0F : 0.0F;
            this.visible = shown || this.fade > 0.0F;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            long now = Util.getMillis();
            float dt = this.lastFrame < 0 ? 0.0F : Math.min((now - this.lastFrame) / 1000.0F, 0.1F);
            this.lastFrame = now;
            this.fade = Mth.clamp(this.fade + (this.shown ? dt : -dt) / FADE_SECONDS, 0.0F, 1.0F);
            if (!this.shown && this.fade <= 0.0F) {
                this.visible = false;
                return;
            }
            int x = this.getX(), y = this.getY(), size = this.getWidth();
            boolean hovered = this.shown && this.isHoveredOrFocused();
            drawFrame(graphics, x, y, size, size, hovered, this.fade);
            this.painter.paint(graphics, x, y, size, ARGB.multiplyAlpha(hovered ? 0xFFFFFFFF : 0xFFB0B0B0, this.fade));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }

        static void corners(GuiGraphics g, int x, int y, int size, int color, boolean outwards) {
            int a = 2, b = size - 2, len = 3;
            if (outwards) {
                g.fill(x + a, y + a, x + a + len, y + a + 1, color);
                g.fill(x + a, y + a, x + a + 1, y + a + len, color);
                g.fill(x + b - len, y + a, x + b, y + a + 1, color);
                g.fill(x + b - 1, y + a, x + b, y + a + len, color);
                g.fill(x + a, y + b - 1, x + a + len, y + b, color);
                g.fill(x + a, y + b - len, x + a + 1, y + b, color);
                g.fill(x + b - len, y + b - 1, x + b, y + b, color);
                g.fill(x + b - 1, y + b - len, x + b, y + b, color);
            } else {
                int m = size / 2;
                g.fill(x + m - len, y + m - 2, x + m - 1, y + m - 1, color);
                g.fill(x + m - 2, y + m - len, x + m - 1, y + m - 1, color);
                g.fill(x + m + 1, y + m - 2, x + m + len, y + m - 1, color);
                g.fill(x + m + 1, y + m - len, x + m + 2, y + m - 1, color);
                g.fill(x + m - len, y + m + 1, x + m - 1, y + m + 2, color);
                g.fill(x + m - 2, y + m + 1, x + m - 1, y + m + len, color);
                g.fill(x + m + 1, y + m + 1, x + m + len, y + m + 2, color);
                g.fill(x + m + 1, y + m + 1, x + m + 2, y + m + len, color);
            }
        }

        static void target(GuiGraphics g, int x, int y, int size, int color) {
            int a = 2, b = size - 2;
            g.fill(x + a, y + a, x + b, y + a + 1, color);
            g.fill(x + a, y + b - 1, x + b, y + b, color);
            g.fill(x + a, y + a, x + a + 1, y + b, color);
            g.fill(x + b - 1, y + a, x + b, y + b, color);
            int m = size / 2;
            g.fill(x + m - 1, y + m - 1, x + m + 1, y + m + 1, color);
        }
    }
}
