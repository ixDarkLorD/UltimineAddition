package net.ixdarklord.ultimine_addition.client.undo;

import net.ixdarklord.ultimine_addition.client.gui.GuiDraw;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.ixdarklord.ultimine_addition.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.Util;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// Where the undo preview was, once it's confirmed: a radial bar and percentage following the blocks going back
// (UndoPayload.Progress from the server), which lingers a moment at 100% and fades.
public final class UndoProgressHud {
    public static final UndoProgressHud INSTANCE = new UndoProgressHud();
    private static final long FADE_IN = 150L, HOLD = 1200L, FADE_OUT = 300L;
    // How quickly the drawn progress catches up with the reported one.
    private static final float CATCH_UP_MS = 90.0F;
    private static final int OUTER = 13, INNER = 10, RING = OUTER * 2;
    private static final float PERCENT_SCALE = 0.75F;
    // The ring's pixels (offsets from its top-left) with each one's angle clockwise from the top, as a 0..1 turn.
    private static final List<int[]> RING_CELLS = new ArrayList<>();
    private static final List<Float> RING_TURNS = new ArrayList<>();

    static {
        for (int dy = -OUTER; dy < OUTER; dy++) {
            for (int dx = -OUTER; dx < OUTER; dx++) {
                float px = dx + 0.5F, py = dy + 0.5F;
                float distance = Mth.sqrt(px * px + py * py);
                if (distance > OUTER || distance < INNER) continue;
                float turn = (float) (Math.atan2(px, -py) / (Math.PI * 2.0));
                RING_CELLS.add(new int[]{dx + OUTER, dy + OUTER});
                RING_TURNS.add(turn < 0.0F ? turn + 1.0F : turn);
            }
        }
    }

    private int placed, total, queued;
    private float shown;
    private long startedAt, doneAt, lastFrame;
    private boolean active;
    // The growth timeline of this undo (UndoGrowthClient), when animated: the bar follows the blocks as they grow.
    private @Nullable List<Double> timeline;
    private int growTicks;

    private UndoProgressHud() {}

    public void update(int placed, int total, int queued) {
        long now = Util.getMillis();
        // The next queued undo starting: the panel stays up and starts over for it.
        boolean nextJob = this.active && (placed < this.placed || total != this.total);
        if (nextJob) {
            this.shown = 0.0F;
            this.timeline = null;
            this.doneAt = 0L;
            this.lastFrame = now;
        }
        this.queued = queued;
        if (!this.active) {
            this.active = true;
            this.startedAt = now;
            this.shown = 0.0F;
            this.lastFrame = now;
            this.timeline = null;
        }
        this.placed = placed;
        this.total = Math.max(1, total);
        this.doneAt = placed >= total ? (this.doneAt == 0L ? now : this.doneAt) : 0L;
    }

    public void follow(List<Double> starts, int growTicks) {
        this.timeline = starts;
        this.growTicks = growTicks;
    }

    public void clear() {
        this.active = false;
        this.doneAt = 0L;
        this.timeline = null;
    }

    public void render(GuiGraphics graphics) {
        if (!this.active) return;
        Minecraft minecraft = Minecraft.getInstance();
        long now = Util.getMillis();
        int grown = this.placed;
        List<Double> timeline = this.timeline;
        if (timeline != null && !timeline.isEmpty()) {
            // Same timeline as the growing blocks: each block adds its own growth, so the bar moves with the wave.
            float partial = minecraft.getTimer().getGameTimeDeltaPartialTick(false);
            float sum = 0.0F;
            grown = 0;
            for (double start : timeline) {
                float progress = UndoGrowthClient.progress(start, this.growTicks, partial);
                sum += progress;
                if (progress >= 1.0F) grown++;
            }
            this.shown = sum / timeline.size();
            this.lastFrame = now;
        } else {
            // No animation (or not seen yet): ease towards what the server reports.
            float target = (float) this.placed / this.total;
            float step = 1.0F - (float) Math.exp(-(now - this.lastFrame) / CATCH_UP_MS);
            this.lastFrame = now;
            this.shown = this.shown + (target - this.shown) * step;
            if (Math.abs(target - this.shown) < 0.002F) this.shown = target;
        }
        // Done once the server has placed every block and the bar has caught up.
        boolean done = this.doneAt != 0L && this.shown >= 1.0F && this.queued == 0;

        float alpha = Mth.clamp((now - this.startedAt) / (float) FADE_IN, 0.0F, 1.0F);
        if (done) {
            long since = now - this.doneAt - HOLD;
            if (since > FADE_OUT) {
                this.clear();
                return;
            }
            if (since > 0) alpha = Math.min(alpha, 1.0F - since / (float) FADE_OUT);
        }
        if (minecraft.options.hideGui || alpha < 0.02F) return;
        int a = Math.round(alpha * 255);
        Font font = minecraft.font;

        int accent = done ? 0x8FE08F : 0x8FC8FF;
        Component title = done ? Component.translatable("gui.ultimine_addition.undo.progress.done")
                : UndoHudTheme.withRevertIcon(Component.translatable("gui.ultimine_addition.undo.progress"));
        Component blocks = Component.translatable("gui.ultimine_addition.undo.progress.blocks",
                Component.literal(String.valueOf(Math.min(grown, this.total))).withStyle(ChatFormatting.WHITE), this.total).withStyle(ChatFormatting.GRAY);

        Component queuedLine = Component.translatable("gui.ultimine_addition.undo.progress.queued",
                Component.literal(String.valueOf(this.queued)).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY);
        int left = UndoHudTheme.PAD;
        int textWidth = Math.max(font.width(blocks), this.queued > 0 ? font.width(queuedLine) : 0);
        int width = Math.max(font.width(title) + 24, left + RING + 7 + textWidth + left);
        int height = UndoHudTheme.TITLE_HEIGHT + 2 + RING + 7;
        UndoHudTheme.begin(graphics, width);
        int top = UndoHudTheme.frame(graphics, width, height, title, accent, alpha);

        // The ring: a dim track, filled clockwise from the top, with the percentage inside.
        int ringX = left, ringY = top;
        int track = ARGB.color(a / 3, 0x000000), fill = ARGB.color(a, accent);
        for (int i = 0; i < RING_CELLS.size(); i++) {
            int[] cell = RING_CELLS.get(i);
            int color = RING_TURNS.get(i) < this.shown ? fill : track;
            graphics.fill(ringX + cell[0], ringY + cell[1], ringX + cell[0] + 1, ringY + cell[1] + 1, color);
        }
        String percent = Math.round(this.shown * 100.0F) + "%";
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(ringX + OUTER, ringY + OUTER - 3.0F, 0.0F);
        pose.scale(PERCENT_SCALE, PERCENT_SCALE, 1.0F);
        GuiDraw.text(graphics, font, percent, -font.width(percent) / 2, 0, ARGB.color(a, 0xFFFFFF), true);
        pose.popPose();

        int textX = ringX + RING + 7;
        if (this.queued > 0) {
            GuiDraw.text(graphics, font, blocks, textX, ringY + OUTER - 9, ARGB.color(a, UndoHudTheme.TEXT), true);
            GuiDraw.text(graphics, font, queuedLine, textX, ringY + OUTER + 1, ARGB.color(a, UndoHudTheme.TEXT), true);
        } else {
            GuiDraw.text(graphics, font, blocks, textX, ringY + OUTER - 4, ARGB.color(a, UndoHudTheme.TEXT), true);
        }
        UndoHudTheme.end(graphics);
    }
}
