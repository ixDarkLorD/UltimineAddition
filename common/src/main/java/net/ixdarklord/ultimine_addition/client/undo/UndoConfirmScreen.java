package net.ixdarklord.ultimine_addition.client.undo;

import net.ixdarklord.ultimine_addition.config.UAClientConfig;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.UndoPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

// Asks before an undo with items missing: only the blocks whose own drops the player has come back. Drawn in the undo
// panel's frame; "don't ask again" turns the question off (undo.confirm_missing_items in the client config).
public final class UndoConfirmScreen extends Screen {
    private static final int WIDTH = 236;
    private static final int MAX_ROWS = 6;
    private static final int BUTTON_HEIGHT = 20;

    private final UndoPayload.Preview preview;
    private final List<UndoPreviewClient.Row> missing;
    private List<FormattedCharSequence> message = List.of();
    private Checkbox dontAsk;
    private int left, top, panelHeight, gridTop;

    UndoConfirmScreen(UndoPayload.Preview preview, List<UndoPreviewClient.Row> rows) {
        super(Component.translatable("gui.ultimine_addition.undo.partial.title"));
        this.preview = preview;
        this.missing = rows.stream().filter(row -> !row.ok()).toList();
    }

    private int columns() {
        return Math.max(1, Math.min(3, Math.min(this.missing.size(), MAX_ROWS)));
    }

    private int gridRows() {
        int shown = Math.min(this.missing.size(), MAX_ROWS);
        return (shown + this.columns() - 1) / this.columns();
    }

    @Override
    protected void init() {
        int pad = UndoHudTheme.PAD;
        Component text = Component.translatable("gui.ultimine_addition.undo.partial.message",
                Component.literal(String.valueOf(this.preview.restorable())).withStyle(ChatFormatting.YELLOW),
                this.preview.positions().size());
        this.message = this.font.split(text, WIDTH - pad * 2);

        int rowHeight = UndoHudTheme.SLOT + 2;
        int contentTop = UndoHudTheme.TITLE_HEIGHT + 4;
        int gridHeight = this.gridRows() * rowHeight + (this.missing.size() > MAX_ROWS ? 10 : 0);
        this.gridTop = contentTop + this.message.size() * 10 + 6 + 11;
        int checkboxTop = this.gridTop + gridHeight + 6;
        int buttonsTop = checkboxTop + Checkbox.getBoxSize(this.font) + 8;
        this.panelHeight = buttonsTop + BUTTON_HEIGHT + pad;
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - this.panelHeight) / 2;

        this.dontAsk = this.addRenderableWidget(Checkbox.builder(Component.translatable("gui.ultimine_addition.undo.partial.dont_ask"), this.font)
                .pos(this.left + pad, this.top + checkboxTop).maxWidth(WIDTH - pad * 2).build());
        int buttonWidth = (WIDTH - pad * 2 - 6) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.ultimine_addition.undo.partial.confirm"), button -> this.confirm())
                .bounds(this.left + pad, this.top + buttonsTop, buttonWidth, BUTTON_HEIGHT).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose())
                .bounds(this.left + pad + buttonWidth + 6, this.top + buttonsTop, buttonWidth, BUTTON_HEIGHT).build());
    }

    private void confirm() {
        if (this.dontAsk.selected()) {
            UAClientConfig.CONFIRM_MISSING_ITEMS.set(false);
            UAClientConfig.CONFIG.save();
        }
        PayloadHandler.sendToServer(new UndoPayload.Request(true, true));
        UndoPreviewClient.INSTANCE.close();
        this.onClose();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(this.left, this.top);
        int pad = UndoHudTheme.PAD;
        int y = UndoHudTheme.frame(graphics, WIDTH, this.panelHeight, UndoHudTheme.withRevertIcon(this.title), 0xF0B060, 1.0F) + 2;
        for (FormattedCharSequence line : this.message) {
            graphics.text(this.font, line, pad, y, ARGB.opaque(UndoHudTheme.TEXT), true);
            y += 10;
        }
        y += 6;
        graphics.text(this.font, Component.translatable("gui.ultimine_addition.undo.partial.missing"), pad, y, ARGB.opaque(0xE0584F), true);

        // What's missing, in the preview's slots: have/needed beside each.
        int columns = this.columns();
        int shown = Math.min(this.missing.size(), MAX_ROWS);
        int cell = (WIDTH - pad * 2 + 8) / columns;
        int rowHeight = UndoHudTheme.SLOT + 2;
        for (int i = 0; i < shown; i++) {
            UndoPreviewClient.Row row = this.missing.get(i);
            int cx = pad + (i % columns) * cell, cy = this.gridTop + (i / columns) * rowHeight;
            UndoHudTheme.slot(graphics, cx, cy, 1.0F);
            if (!row.icon().isEmpty()) graphics.item(row.icon(), cx + 2, cy + 2);
            else graphics.text(this.font, "✦", cx + 7, cy + 6, ARGB.opaque(0x8FE08F), true);
            graphics.text(this.font, row.text(), cx + UndoHudTheme.SLOT + 4, cy + 6, ARGB.opaque(0xE0584F), true);
        }
        if (this.missing.size() > MAX_ROWS) {
            graphics.text(this.font, Component.translatable("gui.ultimine_addition.undo.more", this.missing.size() - MAX_ROWS),
                    pad, this.gridTop + this.gridRows() * rowHeight, ARGB.opaque(0xAAAAAA), true);
        }
        pose.popMatrix();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
