package net.ixdarklord.ultimine_addition.client.undo;

import net.ixdarklord.ultimine_addition.network.payloads.UndoPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.gui.theme.RecordTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// The Ultimine operations the player can still undo, newest first: how many blocks each puts back, what it costs and
// how long it stays undoable. They are undone in this order, so the button starts the newest one's preview.
public final class UndoHistoryScreen extends Screen {
    private static final int WIDTH = 236;
    private static final int ROW_HEIGHT = UndoHudTheme.SLOT + 4;
    private static final int BUTTON_HEIGHT = 14;

    private final UndoPayload.History history;
    private final long openedAt = Util.getMillis();
    private int left, top, panelHeight, rowsTop;

    public UndoHistoryScreen(UndoPayload.History history) {
        super(Component.translatable("gui.ultimine_addition.undo.history.title", history.entries().size(), history.maxHistory()));
        this.history = history;
    }

    @Override
    protected void init() {
        int pad = UndoHudTheme.PAD;
        this.rowsTop = UndoHudTheme.TITLE_HEIGHT + 4;
        int rows = Math.max(1, this.history.entries().size());
        int buttonsTop = this.rowsTop + rows * ROW_HEIGHT + 4;
        this.panelHeight = buttonsTop + BUTTON_HEIGHT + pad;
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - this.panelHeight) / 2;

        int buttonWidth = (WIDTH - pad * 2 - 6) / 2;
        RecordButton undo = this.addRenderableWidget(new RecordButton(this.left + pad, this.top + buttonsTop, buttonWidth, BUTTON_HEIGHT,
                Component.translatable("gui.ultimine_addition.undo.history.undo"), this::undoNewest));
        undo.active = !this.history.entries().isEmpty() && this.history.entries().getFirst().state() == UndoPayload.HistoryEntry.READY;
        this.addRenderableWidget(new RecordButton(this.left + pad + buttonWidth + 6, this.top + buttonsTop, buttonWidth, BUTTON_HEIGHT,
                CommonComponents.GUI_DONE, this::onClose));
    }

    // Closes and shows the newest operation's preview, as the undo key does.
    private void undoNewest() {
        this.onClose();
        UndoPreviewClient.INSTANCE.close();
        UndoPreviewClient.INSTANCE.onUndoKey();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(this.left, this.top);
        int pad = UndoHudTheme.PAD;
        UndoHudTheme.frame(graphics, WIDTH, this.panelHeight, UndoHudTheme.withRevertIcon(this.title), 0x8FC8FF, 1.0F);
        List<UndoPayload.HistoryEntry> entries = this.history.entries();
        if (entries.isEmpty()) {
            Component empty = Component.translatable(this.history.enabled() ? "info.ultimine_addition.undo.nothing" : "info.ultimine_addition.undo.disabled");
            graphics.centeredText(this.font, empty, WIDTH / 2, this.rowsTop + (ROW_HEIGHT - 8) / 2, ARGB.opaque(0xAAAAAA));
        }
        long elapsed = Util.getMillis() - this.openedAt;
        int textLeft = pad + UndoHudTheme.SLOT + 5;
        int textWidth = WIDTH - pad - textLeft;
        for (int i = 0; i < entries.size(); i++) {
            UndoPayload.HistoryEntry entry = entries.get(i);
            int y = this.rowsTop + i * ROW_HEIGHT;
            // The newest one, the one undo takes next, stands out.
            if (i == 0) graphics.fill(pad - 3, y - 2, WIDTH - pad + 3, y + UndoHudTheme.SLOT + 2, 0x228FC8FF);
            UndoHudTheme.slot(graphics, pad, y, 1.0F);
            if (!entry.icon().isEmpty()) graphics.item(entry.icon(), pad + 2, y + 2);

            long expiresIn = Math.max(0L, entry.expiresIn() - elapsed);
            int seconds = Mth.ceil(expiresIn / 1000.0F);
            String timeLeft = seconds >= 60 ? "%d:%02d".formatted(seconds / 60, seconds % 60) : seconds + "s";
            Component expires = Component.translatable("gui.ultimine_addition.undo.expires", timeLeft);
            int expiresWidth = this.font.width(expires);
            graphics.text(this.font, expires, WIDTH - pad - expiresWidth, y + 2, ARGB.opaque(expiresIn < 5000L ? 0xE0584F : 0xAAAAAA), true);

            MutableComponent blocks = Component.translatable("gui.ultimine_addition.undo.history.blocks", entry.blocks());
            Component state = switch (entry.state()) {
                case UndoPayload.HistoryEntry.TOO_FAR -> Component.translatable("gui.ultimine_addition.undo.history.too_far").withStyle(ChatFormatting.RED);
                case UndoPayload.HistoryEntry.BLOCKED -> Component.translatable("gui.ultimine_addition.undo.history.blocked").withStyle(ChatFormatting.RED);
                default -> i == 0 ? Component.translatable("gui.ultimine_addition.undo.history.next").withStyle(ChatFormatting.YELLOW) : Component.empty();
            };
            Component head = state.getString().isEmpty() ? blocks : blocks.append(Component.literal("  ")).append(state);
            graphics.text(this.font, this.font.substrByWidth(head, textWidth - expiresWidth - 6).getString(), textLeft, y + 2, ARGB.opaque(UndoHudTheme.TEXT), true);
            // Line 1 is cut as plain text, so the state keeps its color drawn over its place when everything fits.
            if (this.font.width(head) <= textWidth - expiresWidth - 6 && !state.getString().isEmpty()) {
                graphics.text(this.font, state, textLeft + this.font.width(head) - this.font.width(state), y + 2, ARGB.opaque(0xFFFFFF), true);
            }

            String cost = cost(entry);
            graphics.text(this.font, this.font.plainSubstrByWidth(cost, textWidth), textLeft, y + 12,
                    ARGB.opaque(entry.free() ? 0x8FE08F : entry.affordable() ? 0xB8E0B8 : 0xE0584F), true);
        }
        pose.popMatrix();
    }

    private static String cost(UndoPayload.HistoryEntry entry) {
        if (entry.free()) return Component.translatable("gui.ultimine_addition.undo.free").getString();
        StringBuilder items = new StringBuilder();
        for (ItemStack stack : entry.cost()) {
            if (!items.isEmpty()) items.append(", ");
            items.append(stack.getCount()).append("× ").append(stack.getHoverName().getString());
        }
        if (entry.xp() > 0) {
            if (!items.isEmpty()) items.append(", ");
            items.append(Component.translatable("gui.ultimine_addition.undo.xp", entry.xp()).getString());
        }
        if (items.isEmpty()) return Component.translatable("gui.ultimine_addition.undo.free").getString();
        return Component.translatable("gui.ultimine_addition.undo.history.cost", items.toString()).getString();
    }

    // The Skills Record's own button, tinted like the undo panels.
    private static final class RecordButton extends AbstractButton {
        private final Runnable onPress;

        private RecordButton(int x, int y, int width, int height, Component message, Runnable onPress) {
            super(x, y, width, height, message);
            this.onPress = onPress;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            this.onPress.run();
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.active && this.isHoveredOrFocused();
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SkillsRecordScreen.BUTTON_SPRITES.get(this.active, hovered),
                    this.getX(), this.getY(), this.getWidth(), this.getHeight(), RecordTheme.hud().overlay().argb());
            graphics.centeredText(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 8) / 2,
                    this.active ? 0xFFFFFFFF : 0xFFA0A0A0);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
