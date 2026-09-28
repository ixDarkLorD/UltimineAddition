package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.minecraft.client.renderer.RenderPipelines;
import net.ixdarklord.ultimine_addition.config.UAClientConfig;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

final class ViewerButton extends AbstractButton {
    private final OnPress onPress;

    interface OnPress {
        void onPress(ViewerButton button);
    }

    ViewerButton(int width, Component message, OnPress onPress) {
        super(0, 0, width, 12, message);
        this.onPress = onPress;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.onPress.onPress(this);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.active && this.isHoveredOrFocused();
        // The Skills Record's own button, tinted like the book (as in the edit challenge screen).
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SkillsRecordScreen.BUTTON_SPRITES.get(this.active, hovered),
                this.getX(), this.getY(), this.getWidth(), this.getHeight(), UAClientConfig.backgroundColor().argb());
        int color = !this.active ? 0xFFA0A0A0 : 0xFFFFFFFF;
        Font font = Minecraft.getInstance().font;
        graphics.centeredText(font, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 8) / 2, color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    static void drawFrame(GuiGraphicsExtractor graphics, int x, int y, int width, int height, boolean hovered) {
        int outline = hovered ? 0xFFFFFFFF : 0xFF8A8A8A;
        graphics.fill(x, y, x + width, y + height, hovered ? 0xE0404040 : 0xC0202020);
        graphics.fill(x, y, x + width, y + 1, outline);
        graphics.fill(x, y + height - 1, x + width, y + height, outline);
        graphics.fill(x, y, x + 1, y + height, outline);
        graphics.fill(x + width - 1, y, x + width, y + height, outline);
    }

    static final class Icon extends AbstractButton {
        private final Consumer<Icon> onPress;
        private final IconPainter painter;

        interface IconPainter {
            void paint(GuiGraphicsExtractor graphics, int x, int y, int size, int color);
        }

        Icon(int size, Component tooltip, IconPainter painter, Consumer<Icon> onPress) {
            super(0, 0, size, size, CommonComponents.EMPTY);
            this.painter = painter;
            this.onPress = onPress;
            this.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip));
        }

        @Override
        public void onPress(InputWithModifiers input) {
            this.onPress.accept(this);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int x = this.getX(), y = this.getY(), size = this.getWidth();
            boolean hovered = this.isHoveredOrFocused();
            drawFrame(graphics, x, y, size, size, hovered);
            this.painter.paint(graphics, x, y, size, hovered ? 0xFFFFFFFF : 0xFFB0B0B0);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }

        static void corners(GuiGraphicsExtractor g, int x, int y, int size, int color, boolean outwards) {
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

        static void target(GuiGraphicsExtractor g, int x, int y, int size, int color) {
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
