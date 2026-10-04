package net.ixdarklord.ultimine_addition.client.gui.config;import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.ixdarklord.coolcatcore.api.config.ConfigTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

// A yes/no question over the current screen (from Glazed Menu). Closing it any other way answers no.
public final class ConfirmPopup extends StyledPopup {
    private static final int WIDTH = 240;

    private final Component message;
    private final Component yes;
    private final boolean destructive;
    private final BooleanConsumer answer;
    private List<FormattedCharSequence> lines = List.of();

    public ConfirmPopup(Screen parent, ConfigTheme theme, Component title, Component message, Component yes, boolean destructive, BooleanConsumer answer) {
        super(parent, title, theme);
        this.message = message;
        this.yes = yes;
        this.destructive = destructive;
        this.answer = answer;
    }

    @Override
    protected void initPopup() {
        this.lines = this.font.split(this.message, WIDTH - PADDING * 2);
        this.setPanel(WIDTH, PADDING + TITLE_HEIGHT + this.lines.size() * 10 + 4 + FOOTER + PADDING);
        int buttonWidth = (this.contentWidth() - 4) / 2;
        int y = this.footerButtonY();
        FlatButton no = this.addRenderableWidget(FlatButton.of(CommonComponents.GUI_CANCEL, buttonWidth, button -> this.answer.accept(false)));
        no.setPosition(this.contentLeft(), y);
        FlatButton confirm = this.addRenderableWidget(FlatButton.of(this.yes, buttonWidth, button -> this.answer.accept(true))
                .style(this.destructive ? FlatButton.Style.DANGER : FlatButton.Style.PRIMARY));
        confirm.setPosition(this.contentLeft() + this.contentWidth() - buttonWidth, y);
    }

    @Override
    protected void renderPopup(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        // Centered in the body: as far from the title band above as from the footer band below.
        int y = this.contentTop() + 2;
        for (FormattedCharSequence line : this.lines) {
            graphics.drawString(this.font, line, this.contentLeft(), y, PopupStyle.colors().textDim(), false);
            y += 10;
        }
    }

    @Override
    public void onClose() {
        this.answer.accept(false);
    }
}
