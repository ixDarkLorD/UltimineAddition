package net.ixdarklord.ultimine_addition.client.undo;

import net.ixdarklord.ultimine_addition.client.gui.GuiDraw;
import net.ixdarklord.ultimine_addition.client.renderer.ItemAlpha;
import net.ixdarklord.ultimine_addition.client.handler.KeyHandler;
import net.ixdarklord.ultimine_addition.common.undo.UltimineUndo;
import net.ixdarklord.ultimine_addition.config.UAClientConfig;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.UndoPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.ixdarklord.ultimine_addition.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// The undo preview: ghosts of the blocks undo would put back, outlined, and what it costs next to the crosshair.
// Pressing the undo key again confirms (with items missing, after asking: see UndoConfirmScreen); sneaking, waiting
// or walking away cancels.
public final class UndoPreviewClient {
    public static final UndoPreviewClient INSTANCE = new UndoPreviewClient();
    // Client-only display entities are cheap, but a huge operation shouldn't flood the level with them.
    private static final long TIMEOUT_MS = 15_000L;
    // The last part of the timeout, where the HUD fades out; the timer bar runs out when it starts.
    private static final long FADE_MS = 400L;
    private static final double MAX_DISTANCE = 48.0;
    private static final int MAX_ROWS = 6;

    private UndoPayload.@Nullable Preview preview;
    private long openedAt;
    private @Nullable Vec3 center;
    // Held last tick: the key acts on the press only, not on the OS's key repeat while it's held.
    private boolean undoKeyWasDown;

    private UndoPreviewClient() {}

    public boolean isOpen() {
        return this.preview != null;
    }

    public void onUndoKey() {
        UndoPayload.Preview preview = this.preview;
        Minecraft minecraft = Minecraft.getInstance();
        if (preview == null) {
            PayloadHandler.sendToServer(new UndoPayload.Request(false, false));
            return;
        }
        boolean affordable = minecraft.player == null || costRows(preview, minecraft.player).stream().allMatch(Row::ok);
        if (!affordable && preview.restorable() > 0 && UAClientConfig.CONFIRM_MISSING_ITEMS.get()) {
            // Asks first; the preview stays up behind the question.
            minecraft.setScreen(new UndoConfirmScreen(preview, costRows(preview, minecraft.player)));
            return;
        }
        // With nothing it can pay for, the server says so.
        PayloadHandler.sendToServer(new UndoPayload.Request(true, !affordable));
        this.close();
    }

    public void open(UndoPayload.Preview preview) {
        this.close();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || preview.positions().isEmpty()) return;
        this.preview = preview;
        this.openedAt = Util.getMillis();

        Vec3 sum = Vec3.ZERO;
        for (BlockPos pos : preview.positions()) sum = sum.add(pos.getCenter());
        this.center = sum.scale(1.0 / preview.positions().size());
    }

    // What UndoGhostRenderer draws while the preview is open.
    public List<BlockPos> ghostPositions() {
        return this.preview == null ? List.of() : this.preview.positions();
    }

    // For each ghost: whether undoing with what's missing would put it back (the others are drawn in red).
    public List<Boolean> ghostComesBack() {
        return this.preview == null ? List.of() : this.preview.comesBack();
    }

    public List<BlockState> ghostStates() {
        return this.preview == null ? List.of() : this.preview.states();
    }

    public void close() {
        this.preview = null;
        this.center = null;
    }

    public void tick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        while (KeyHandler.KEY_UNDO.consumeClick()) {
            // Clicks include key repeats; only the press below counts.
        }
        boolean down = KeyHandler.KEY_UNDO.isDown();
        if (down && !this.undoKeyWasDown && player != null && minecraft.screen == null && KeyHandler.undoModifierHeld()) this.onUndoKey();
        this.undoKeyWasDown = down;
        if (!this.isOpen()) return;
        // Waits while the player answers the missing-items question.
        if (minecraft.screen instanceof UndoConfirmScreen) return;
        if (player == null || minecraft.level == null || player.isShiftKeyDown() || Util.getMillis() - this.openedAt > TIMEOUT_MS
                || this.center == null || player.position().distanceTo(this.center) > MAX_DISTANCE) {
            this.close();
            return;
        }
    }

    public void render(GuiGraphics graphics, float ignored) {
        UndoPayload.Preview preview = this.preview;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (preview == null) {
            UndoProgressHud.INSTANCE.render(graphics);
            return;
        }
        if (player == null || minecraft.options.hideGui || minecraft.screen instanceof UndoConfirmScreen) return;
        Font font = minecraft.font;

        Component title = UndoHudTheme.withRevertIcon(Component.translatable("gui.ultimine_addition.undo.title", preview.positions().size()));
        List<Row> rows = costRows(preview, player);
        boolean affordable = rows.stream().allMatch(Row::ok);
        int shown = Math.min(rows.size(), MAX_ROWS);

        // Footer: what it costs (free / missing), how to confirm, how to cancel.
        Component keyName = KeyHandler.undoKeyName().copy().withStyle(ChatFormatting.YELLOW);
        List<Component> footer = new ArrayList<>();
        if (preview.free()) footer.add(Component.translatable("gui.ultimine_addition.undo.free").withStyle(ChatFormatting.GREEN));
        if (affordable) footer.add(Component.translatable("gui.ultimine_addition.undo.confirm", keyName).withStyle(ChatFormatting.WHITE));
        else if (preview.restorable() > 0) footer.add(Component.translatable("gui.ultimine_addition.undo.confirm_partial", keyName,
                preview.restorable(), preview.positions().size()).withStyle(ChatFormatting.GOLD));
        else footer.add(Component.translatable("gui.ultimine_addition.undo.missing").withStyle(ChatFormatting.RED));
        footer.add(Component.translatable("gui.ultimine_addition.undo.cancel").withStyle(style -> style.withColor(0xC8C8C8)));
        int footerWidth = footer.stream().mapToInt(font::width).max().orElse(0);

        // How many undos are stored, and how long this one stays undoable (counting down).
        long expiresIn = Math.max(0L, preview.expiresIn() - (Util.getMillis() - this.openedAt));
        Component stored = Component.translatable("gui.ultimine_addition.undo.available",
                Component.literal(String.valueOf(preview.available())).withStyle(ChatFormatting.WHITE), preview.maxHistory()).withStyle(ChatFormatting.GRAY);
        int seconds = Mth.ceil(expiresIn / 1000.0F);
        String timeLeft = seconds >= 60 ? "%d:%02d".formatted(seconds / 60, seconds % 60) : seconds + "s";
        Component expires = Component.translatable("gui.ultimine_addition.undo.expires", timeLeft)
                .withStyle(expiresIn < 5000L ? ChatFormatting.RED : ChatFormatting.GRAY);
        int infoWidth = font.width(stored) + 12 + font.width(expires);

        // Cost grid: each item in a small slot with have/needed beside it.
        int columns = Math.max(1, Math.min(3, shown));
        int cellText = rows.stream().limit(shown).mapToInt(row -> font.width(row.text)).max().orElse(0);
        int cell = UndoHudTheme.SLOT + 4 + cellText + 8;
        int gridRows = (shown + columns - 1) / columns;
        int rowHeight = UndoHudTheme.SLOT + 2;
        int width = Math.max(Math.max(font.width(title) + 24, infoWidth), Math.max(footerWidth, columns * cell - 8)) + UndoHudTheme.PAD * 2;
        int height = UndoHudTheme.TITLE_HEIGHT + 2 + 11 + (gridRows > 0 ? gridRows * rowHeight + 2 : 0) + (rows.size() > MAX_ROWS ? 10 : 0)
                + footer.size() * 10 + 3 + 2 + 6;

        long elapsed = Util.getMillis() - this.openedAt;
        float remaining = Mth.clamp(1.0F - (float) elapsed / (TIMEOUT_MS - FADE_MS), 0.0F, 1.0F);
        float alpha = Mth.clamp((TIMEOUT_MS - elapsed) / (float) FADE_MS, 0.0F, 1.0F);
        int a = UndoHudTheme.alpha(alpha);
        if (a < 4) return;

        UndoHudTheme.begin(graphics, width);
        int left = UndoHudTheme.PAD;
        int y = UndoHudTheme.frame(graphics, width, height, title, affordable ? 0x8FC8FF : 0xF07A70, alpha);
        GuiDraw.text(graphics, font, stored, left, y, ARGB.color(a, UndoHudTheme.TEXT), true);
        GuiDraw.text(graphics, font, expires, width - left - font.width(expires), y, ARGB.color(a, UndoHudTheme.TEXT), true);
        y += 11;
        for (int i = 0; i < shown; i++) {
            Row row = rows.get(i);
            int cx = left + (i % columns) * cell, cy = y + (i / columns) * rowHeight;
            UndoHudTheme.slot(graphics, cx, cy, alpha);
            if (!row.icon.isEmpty()) ItemAlpha.draw(alpha, () -> graphics.renderItem(row.icon, cx + 2, cy + 2));
            else GuiDraw.text(graphics, font, "✦", cx + 7, cy + 6, ARGB.color(a, 0x8FE08F), true);
            GuiDraw.text(graphics, font, row.text, cx + UndoHudTheme.SLOT + 4, cy + 6, ARGB.color(a, row.ok ? 0x8FE08F : 0xE0584F), true);
        }
        if (gridRows > 0) y += gridRows * rowHeight + 2;
        if (rows.size() > MAX_ROWS) {
            GuiDraw.text(graphics, font, Component.translatable("gui.ultimine_addition.undo.more", rows.size() - MAX_ROWS), left, y, ARGB.color(a, 0xAAAAAA), true);
            y += 10;
        }
        for (Component line : footer) {
            GuiDraw.text(graphics, font, line, left, y, ARGB.color(a, UndoHudTheme.TEXT), true);
            y += 10;
        }
        // Timer: drains until the panel starts to fade, turning red in its last quarter.
        UndoHudTheme.bar(graphics, left, y + 3, width - left * 2, remaining, remaining > 0.25F ? 0x8FC8FF : 0xF07A70, alpha);
        UndoHudTheme.end(graphics);
    }

    // What the undo costs, one row per item (and the experience), with what the player has of it.
    static List<Row> costRows(UndoPayload.Preview preview, LocalPlayer player) {
        List<Row> rows = new ArrayList<>();
        if (preview.free()) return rows;
        for (int i = 0; i < preview.cost().size(); i++) {
            ItemStack needed = preview.cost().get(i);
            int have = preview.fromGround().get(i) + UltimineUndo.countInInventory(player, needed);
            rows.add(new Row(needed, Math.min(have, needed.getCount()) + "/" + needed.getCount(), have >= needed.getCount()));
        }
        if (preview.xp() > 0) {
            int have = preview.xpOnGround() ? preview.xp() : player.totalExperience;
            rows.add(new Row(ItemStack.EMPTY, Component.translatable("gui.ultimine_addition.undo.xp", preview.xp()).getString(), have >= preview.xp()));
        }
        return rows;
    }

    record Row(ItemStack icon, String text, boolean ok) {}
}
