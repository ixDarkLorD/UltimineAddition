package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.ixdarklord.ultimine_addition.util.ARGB;
import net.ixdarklord.ultimine_addition.client.gui.theme.RecordTheme;
import net.ixdarklord.ultimine_addition.client.gui.GuiDraw;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.coolcatcanvas.api.client.gui.components.widgets.WidgetSprites;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

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
        PoseStack pose = graphics.pose();
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

    /** A small square button drawn like the Skills Record's configuration button, in the record's color. */
    static final class Icon extends AbstractButton {
        static final int SIZE = 10;
        private final Consumer<Icon> onPress;
        private final Supplier<WidgetSprites> sprites;
        private final IntSupplier tint;

        Icon(Component tooltip, Supplier<WidgetSprites> sprites, IntSupplier tint, Consumer<Icon> onPress) {
            super(0, 0, SIZE, SIZE, CommonComponents.EMPTY);
            this.sprites = sprites;
            this.tint = tint;
            this.onPress = onPress;
            this.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip));
        }

        // Canvas's WidgetSprites (1.20.1 has none of its own) take texture paths.
        static WidgetSprites sprites(String name) {
            return new WidgetSprites(GuiDraw.spriteTexture(FTBUltimineAddition.id("container/skills_record/card_viewer_" + name + "_enabled")),
                    GuiDraw.spriteTexture(FTBUltimineAddition.id("container/skills_record/card_viewer_" + name + "_disabled")),
                    GuiDraw.spriteTexture(FTBUltimineAddition.id("container/skills_record/card_viewer_" + name + "_focused")));
        }

        @Override
        public void onPress() {
            this.onPress.accept(this);
        }

        /** Shows the button while its viewer shows, clickable only where it applies (greyed out otherwise). */
        void setState(boolean visible, boolean active) {
            this.visible = visible;
            this.active = active;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.active && this.isHoveredOrFocused();
            this.sprites.get().blit(graphics, this.active, hovered, this.getX(), this.getY(), SIZE, SIZE,
                    ARGB.opaque(this.tint.getAsInt()));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
