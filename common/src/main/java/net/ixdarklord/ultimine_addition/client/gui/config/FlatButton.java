package net.ixdarklord.ultimine_addition.client.gui.config;import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

// A flat button in the config popups' style (from Glazed Menu): plain, accent-filled (the main action), red
// (destructive), or ghost (just an icon until hovered). It can show an icon, text, or both.
public class FlatButton extends Button {
    private Style style = Style.NORMAL;
    private PopupStyle.@Nullable Icon icon;

    public FlatButton(int width, int height, Component message, OnPress onPress) {
        super(0, 0, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public static FlatButton of(Component message, int width, OnPress onPress) {
        return new FlatButton(width, 20, message, onPress);
    }

    /** An icon-only button; the tooltip names it. */
    public static FlatButton icon(PopupStyle.Icon icon, Component name, OnPress onPress) {
        FlatButton button = new FlatButton(20, 20, CommonComponents.EMPTY, onPress);
        button.icon = icon;
        button.style = Style.GHOST;
        button.setTooltip(Tooltip.create(name));
        return button;
    }

    public FlatButton style(Style style) {
        this.style = style;
        return this;
    }

    public FlatButton withIcon(PopupStyle.Icon icon) {
        this.icon = icon;
        return this;
    }

    @Override
    protected final void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        this.renderContents(graphics, mouseX, mouseY, a);
    }

    /** Draws the button in place of vanilla's own look and label. */
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        boolean hovered = this.active && this.isHoveredOrFocused();
        int accent = PopupStyle.accent();
        int x = this.getX();
        int y = this.getY();
        int width = this.getWidth();
        int height = this.getHeight();
        int foreground;
        switch (this.style) {
            case PRIMARY -> {
                int fill = !this.active ? PopupStyle.colors().buttonDisabled() : hovered ? PopupStyle.mix(accent, 0xFFFFFFFF, 0.18F) : accent;
                PopupStyle.rect(graphics, x, y, width, height, fill);
                foreground = this.active ? PopupStyle.readableOn(fill) : PopupStyle.colors().textMuted();
            }
            case DANGER -> {
                PopupStyle.rect(graphics, x, y, width, height, hovered ? PopupStyle.withAlpha(PopupStyle.colors().error(), 0x40) : PopupStyle.colors().button());
                PopupStyle.outline(graphics, x, y, width, height, hovered ? PopupStyle.colors().error() : PopupStyle.colors().panelBorder());
                foreground = this.active ? PopupStyle.colors().error() : PopupStyle.colors().textMuted();
            }
            case GHOST -> {
                if (hovered) PopupStyle.rect(graphics, x, y, width, height, PopupStyle.withAlpha(PopupStyle.colors().text(), 0x1F));
                foreground = !this.active ? PopupStyle.withAlpha(PopupStyle.colors().textMuted(), 0x90) : hovered ? PopupStyle.colors().text() : PopupStyle.colors().textDim();
            }
            default -> {
                PopupStyle.rect(graphics, x, y, width, height, !this.active ? PopupStyle.colors().buttonDisabled() : hovered ? PopupStyle.colors().buttonHover() : PopupStyle.colors().button());
                PopupStyle.outline(graphics, x, y, width, height, hovered ? PopupStyle.withAlpha(accent, 0xD0) : PopupStyle.colors().panelBorder());
                foreground = this.active ? PopupStyle.colors().text() : PopupStyle.colors().textMuted();
            }
        }

        Font font = Minecraft.getInstance().font;
        Component message = this.getMessage();
        boolean hasText = !message.getString().isEmpty();
        if (this.icon != null && !hasText) {
            this.icon.drawCentered(graphics, x, y, width, height, foreground);
        } else if (this.icon != null) {
            int textWidth = Math.min(font.width(message), width - PopupStyle.Icon.SIZE - 12);
            int left = x + (width - PopupStyle.Icon.SIZE - 4 - textWidth) / 2;
            this.icon.draw(graphics, left, y + (height - PopupStyle.Icon.SIZE) / 2, foreground);
            PopupStyle.text(graphics, font, message, left + PopupStyle.Icon.SIZE + 4, y + (height - 8) / 2, textWidth, foreground);
        } else {
            PopupStyle.centeredText(graphics, font, message, x + width / 2, y + (height - 8) / 2, width - 8, foreground);
        }
    }

    public enum Style {
        NORMAL,
        PRIMARY,
        DANGER,
        GHOST
    }
}
