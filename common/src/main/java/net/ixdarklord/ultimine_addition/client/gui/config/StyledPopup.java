package net.ixdarklord.ultimine_addition.client.gui.config;

import net.ixdarklord.coolcatcore.api.config.ConfigTheme;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

// A panel floating over the screen it was opened from, which stays visible and dimmed underneath (from Glazed Menu).
// Esc or a click outside the panel closes it. Subclasses size the panel with setPanel and add widgets in initPopup.
public abstract class StyledPopup extends Screen {
    protected static final int PADDING = 12;
    protected static final int TITLE_HEIGHT = 18;
    /** The strip along the bottom of the content that holds the buttons; a popup sprite draws its footer band behind it. */
    protected static final int FOOTER = 26;

    protected final @Nullable Screen parent;
    protected final ConfigTheme theme;
    protected int panelX;
    protected int panelY;
    protected int panelWidth;
    protected int panelHeight;

    protected StyledPopup(@Nullable Screen parent, Component title, ConfigTheme theme) {
        super(title);
        this.parent = parent;
        this.theme = theme;
    }

    /** Sizes and centers the panel. */
    protected void setPanel(int width, int height) {
        this.panelWidth = Math.min(width, this.width - 16);
        this.panelHeight = Math.min(height, this.height - 16);
        this.panelX = (this.width - this.panelWidth) / 2;
        this.panelY = (this.height - this.panelHeight) / 2;
    }

    protected int contentLeft() {
        return this.panelX + PADDING;
    }

    /** Where content starts, under the title. */
    protected int contentTop() {
        return this.panelY + PADDING + TITLE_HEIGHT;
    }

    protected int contentWidth() {
        return this.panelWidth - PADDING * 2;
    }

    protected int contentBottom() {
        return this.panelY + this.panelHeight - PADDING;
    }

    /** The top of the footer strip; the body ends here. */
    protected int footerTop() {
        return this.contentBottom() - FOOTER;
    }

    /** Where the footer's 20px tall buttons go. */
    protected int footerButtonY() {
        return this.contentBottom() - 20;
    }

    @Override
    protected final void init() {
        // The screen underneath is drawn too, so it must be laid out for the current size.
        if (this.parent != null) this.parent.init(this.width, this.height);
        this.initPopup();
    }

    protected abstract void initPopup();

    @Override
    protected void repositionElements() {
        this.rebuildWidgets();
    }

    @Override
    public void resize(int width, int height) {
        if (this.parent != null) this.parent.resize(width, height);
        super.resize(width, height);
    }

    @Override
    public void added() {
        PopupStyle.use(this.theme);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.parent != null) {
            this.parent.extractBackground(graphics, -1, -1, a);
            graphics.nextStratum();
            this.parent.extractRenderState(graphics, -1, -1, a);
            graphics.nextStratum();
        }
        PopupStyle.use(this.theme);
        graphics.fill(0, 0, this.width, this.height, PopupStyle.withAlpha(PopupStyle.colors().backdrop(), 0x99));
        // A soft drop shadow, then the theme's panel sprite, or the flat panel with an accent line along its top edge.
        PopupStyle.rect(graphics, this.panelX + 3, this.panelY + 4, this.panelWidth, this.panelHeight, 0x66000000);
        Identifier sprite = this.theme.popupSprite();
        if (sprite != null) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.panelX, this.panelY, this.panelWidth, this.panelHeight);
        } else {
            PopupStyle.rect(graphics, this.panelX, this.panelY, this.panelWidth, this.panelHeight, PopupStyle.colors().popup());
            PopupStyle.outline(graphics, this.panelX, this.panelY, this.panelWidth, this.panelHeight, PopupStyle.colors().panelBorder());
            graphics.fill(this.panelX + 1, this.panelY, this.panelX + this.panelWidth - 1, this.panelY + 2, PopupStyle.accent());
        }
        int titleX = this.contentLeft();
        PopupStyle.Icon icon = this.titleIcon();
        if (icon != null) {
            icon.draw(graphics, titleX, this.panelY + PADDING - 1, PopupStyle.accent());
            titleX += PopupStyle.Icon.SIZE + 5;
        }
        Component note = this.titleNote();
        int noteWidth = note == null ? 0 : Math.min(this.font.width(note), this.contentWidth() / 2);
        if (note != null) {
            PopupStyle.text(graphics, this.font, note, this.contentLeft() + this.contentWidth() - noteWidth, this.panelY + PADDING, noteWidth,
                    PopupStyle.colors().textMuted());
        }
        PopupStyle.text(graphics, this.font, this.title.copy().withStyle(ChatFormatting.BOLD), titleX, this.panelY + PADDING,
                this.contentLeft() + this.contentWidth() - titleX - noteWidth - (note == null ? 0 : 8), PopupStyle.colors().text());
        this.extractPopup(graphics, mouseX, mouseY, a);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        PopupStyle.use(this.theme);
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    /** An icon before the title, if any. */
    protected PopupStyle.@Nullable Icon titleIcon() {
        return null;
    }

    /** Muted text at the right of the title, if any. */
    protected @Nullable Component titleNote() {
        return null;
    }

    /** Draws fixed content inside the panel, under the widgets. */
    protected void extractPopup(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.x() < this.panelX || event.x() >= this.panelX + this.panelWidth || event.y() < this.panelY || event.y() >= this.panelY + this.panelHeight) {
            this.onClose();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return this.parent == null || this.parent.isPauseScreen();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
