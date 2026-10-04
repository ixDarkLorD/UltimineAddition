package net.ixdarklord.ultimine_addition.client.gui.config;import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;

// A sliding on/off switch with its state written beside it (from Glazed Menu); the knob glides between the two ends.
public final class ToggleSwitch extends FlatButton {
    private static final int TRACK_WIDTH = 26;
    private static final int TRACK_HEIGHT = 12;
    private final BooleanSupplier state;
    private float progress = -1;

    public ToggleSwitch(int width, int height, BooleanSupplier state, OnPress onPress) {
        super(width, height, CommonComponents.EMPTY, onPress);
        this.state = state;
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        boolean on = this.state.getAsBoolean();
        float target = on ? 1 : 0;
        this.progress = this.progress < 0 ? target : this.progress + (target - this.progress) * 0.35F;
        if (Math.abs(target - this.progress) < 0.01F) this.progress = target;

        boolean hovered = this.active && this.isHoveredOrFocused();
        int x = this.getX();
        int y = this.getY() + (this.getHeight() - TRACK_HEIGHT) / 2;
        int off = hovered ? PopupStyle.mix(PopupStyle.colors().toggleOff(), PopupStyle.colors().text(), 0.12F) : PopupStyle.colors().toggleOff();
        int track = this.active ? PopupStyle.mix(off, PopupStyle.accent(), this.progress) : PopupStyle.colors().buttonDisabled();
        PopupStyle.rect(graphics, x, y, TRACK_WIDTH, TRACK_HEIGHT, track);
        // An outline on the track's edge: the panel border when off, a darker shade of the accent as it turns on.
        int border = this.active ? PopupStyle.mix(PopupStyle.colors().panelBorder(), PopupStyle.mix(PopupStyle.accent(), 0xFF000000, 0.35F), this.progress)
                : PopupStyle.colors().panelBorder();
        PopupStyle.outline(graphics, x, y, TRACK_WIDTH, TRACK_HEIGHT, border);
        if (this.isFocused()) PopupStyle.outline(graphics, x - 1, y - 1, TRACK_WIDTH + 2, TRACK_HEIGHT + 2, PopupStyle.withAlpha(PopupStyle.accent(), 0xA0));
        int knobX = x + 1 + Math.round((TRACK_WIDTH - TRACK_HEIGHT) * this.progress);
        PopupStyle.rect(graphics, knobX, y + 1, TRACK_HEIGHT - 2, TRACK_HEIGHT - 2, this.active ? PopupStyle.colors().knob() : PopupStyle.colors().textMuted());

        Component label = on ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
        int color = !this.active ? PopupStyle.colors().textMuted() : on ? PopupStyle.colors().text() : PopupStyle.colors().textDim();
        PopupStyle.text(graphics, Minecraft.getInstance().font, label, x + TRACK_WIDTH + 6, this.getY() + (this.getHeight() - 8) / 2,
                this.getWidth() - TRACK_WIDTH - 6, color);
    }
}
