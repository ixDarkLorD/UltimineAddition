package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.coolcatcanvas.api.utils.ColorGradient;
import net.ixdarklord.coolcatcanvas.api.utils.Easing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.ixdarklord.coolcatcanvas.api.client.gui.components.widgets.panel.ViewportPanel;
import net.ixdarklord.coolcatcanvas.api.client.utils.RenderUtils;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardTree.ChallengeNode;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardTree.ChallengeState;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardTree.TierNode;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardTree.TierState;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.gui.theme.RecordTheme;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class TierTreePanel extends ViewportPanel {
    private static final double DEFAULT_ZOOM = 0.85;
    private static final Identifier TIER_FILL = sprite("tier_fill");
    private static final Identifier TIER_BORDER = sprite("tier_border");
    private static final Identifier ROW_FILL = sprite("row_fill");
    private static final Identifier ROW_BORDER = sprite("row_border");
    private static final Identifier BADGE_FILL = sprite("badge_fill");
    private static final Identifier BADGE_BORDER = sprite("badge_border");
    private static final Identifier STAR_FILL = sprite("star_fill");
    private static final Identifier STAR_BORDER = sprite("star_border");
    private static final int MASTERED_GOLD = 0xFFFFC940;
    private static final Identifier SLOT_FILL = sprite("slot_fill");
    private static final Identifier SLOT_BORDER = sprite("slot_border");
    private static final Identifier SMALL_SLOT_FILL = sprite("slot_small_fill");
    private static final Identifier SMALL_SLOT_BORDER = sprite("slot_small_border");
    private static final Identifier JOINT = sprite("joint");

    static final int PROGRESS_BAR_HEIGHT = 7;
    private static final int BADGE_SIZE = 36;
    // The challenge row's block slot, drawn at its sprite's native size so its edges stay clean; as tall as a row's spacing.
    static final int SLOT_SIZE = 26;
    private static final float SLOT_ITEM_SCALE = 1.125F;
    static final int SMALL_SLOT_SIZE = 20;
    private static final int LINE_DONE = 0xFF4E9A56;
    private static final int LINE_LOCKED = 0xFF4A4A4A;
    private static final int LINE_OPEN = 0xFF7A7A7A;
    private static final int PULSE = 0xFFC8FFC8;
    private static final int PULSE_SPACING = 26;
    private static final float PULSE_SPEED = 32.0F;
    private static final long REVEAL_STEP_MS = 40L;
    private static final long REVEAL_DURATION_MS = 260L;
    // Background shades, multiplied by the Skills Record's background color; taken from the card's texture.
    static final int BG_DEEP = 0xFF16150F;
    private static final int BG_MID = 0xFF35322A;
    private static final int BG_GLOW = 0xFF8A8577;
    private static final int CARD_PAPER = 0xFFDCD1B2;
    private static final int CARD_INK = 0xFFA09881;
    private static final int CARD_WIDTH = 72;
    private static final int CARD_HEIGHT = 48;
    private static final int CARD_GAP = 14;
    // How fast the card pattern drifts to the right, in world pixels a second.
    private static final float CARD_DRIFT = 4.0F;
    private static final int CARD_BLOCK = 256;

    private final CardViewerWidget viewer;
    private final Map<Item, Map<MiningSkillCardItem.Tier, ItemStack>> tierIcons = new HashMap<>();
    private final Map<String, Float> hoverProgress = new HashMap<>();
    private @Nullable CardTree tree;
    private MiningSkillCardItem.@Nullable Tier pendingFocus;
    private @Nullable ChallengeNode hoveredChallenge;
    private @Nullable TierNode hoveredTier;
    private long revealStart = -1L;
    private long lastFrame = -1L;
    private float frameDelta;

    TierTreePanel(CardViewerWidget viewer) {
        this.viewer = viewer;
        this.setZoomLimits(0.4, 2.5);
        this.setMargin(12);
        this.setEdgeFade(14, () -> ARGB.multiply(0xFF404040, ARGB.opaque(viewer.getAccentColor())));
        this.setVignette(18, 0.22F);
    }

    private static Identifier sprite(String name) {
        return FTBUltimineAddition.id("container/skills_record/card_viewer/" + name);
    }

    void setTree(@Nullable CardTree tree, boolean reveal) {
        this.tree = tree;
        if (reveal) {
            this.revealStart = Util.getMillis();
            this.hoverProgress.clear();
        }
    }

    void focus(MiningSkillCardItem.Tier tier) {
        this.pendingFocus = tier;
        if (this.getBounds().width() > 0) this.applyFocus();
    }

    private void applyFocus() {
        if (this.tree == null || this.pendingFocus == null) return;
        ScreenRectangle b = this.getBounds();
        this.zoomTo(DEFAULT_ZOOM, b.left(), b.top(), false);
        TierNode node = this.tree.getTier(this.pendingFocus);
        this.centerOn(node.centerX(), node.y() + (b.height() / 2.0 - 6) / DEFAULT_ZOOM);
        this.pendingFocus = null;
    }

    @Override
    protected void onResized() {
        super.onResized();
        this.applyFocus();
    }

    @Override
    protected ScreenRectangle getContentBounds() {
        if (this.tree == null) return new ScreenRectangle(0, 0, 1, 1);
        ScreenRectangle b = this.tree.bounds;
        int above = Math.max(0, (BADGE_SIZE - CardTree.TIER_HEIGHT) / 2);
        return new ScreenRectangle(b.left() - BADGE_SIZE / 2, b.top() - above, b.width() + BADGE_SIZE / 2, b.height() + above);
    }

    private boolean animated() {
        return this.viewer.isAnimated();
    }

    private static float time() {
        return (Util.getMillis() % 3_600_000L) / 1000.0F;
    }

    @Override
    protected void extractWorld(GuiGraphicsExtractor graphics, double mouseX, double mouseY, boolean mouseInView, float partialTick) {
        long now = Util.getMillis();
        this.frameDelta = this.lastFrame < 0 ? 0.0F : Math.min((now - this.lastFrame) / 1000.0F, 0.1F);
        this.lastFrame = now;
        this.hoveredChallenge = null;
        this.hoveredTier = null;

        this.drawBackground(graphics);
        if (this.tree == null) return;
        this.drawConnections(graphics);

        int index = 0;
        for (TierNode tier : this.tree.tiers) {
            boolean hovered = mouseInView && tier.contains(mouseX, mouseY);
            if (hovered) this.hoveredTier = tier;
            this.drawTier(graphics, tier, this.hover("t" + tier.tier().ordinal(), hovered), this.reveal(index++));

            for (ChallengeNode challenge : tier.challenges()) {
                boolean rowHovered = mouseInView && challenge.contains(mouseX, mouseY);
                if (rowHovered) this.hoveredChallenge = challenge;
                String key = "c" + challenge.tier().ordinal() + ":" + challenge.order();
                this.drawChallenge(graphics, challenge, this.hover(key, rowHovered), this.reveal(index++));
            }
        }
    }

    private float hover(String key, boolean hovered) {
        float target = hovered ? 1.0F : 0.0F;
        if (!this.animated()) {
            this.hoverProgress.put(key, target);
            return target;
        }
        float value = this.hoverProgress.getOrDefault(key, 0.0F);
        value += (target - value) * (1.0F - (float) Math.exp(-14.0 * this.frameDelta));
        this.hoverProgress.put(key, value);
        return value;
    }

    private float reveal(int index) {
        if (!this.animated() || this.revealStart < 0) return 1.0F;
        long elapsed = Util.getMillis() - this.revealStart - index * REVEAL_STEP_MS;
        float t = Mth.clamp(elapsed / (float) REVEAL_DURATION_MS, 0.0F, 1.0F);
        return Easing.CUBIC_OUT.apply(t);
    }

    private void pushNodeTransform(GuiGraphicsExtractor graphics, float centerX, float centerY, float hover, float reveal) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        float scale = 1.0F + 0.04F * hover;
        pose.translate(centerX, centerY - 1.5F * hover + (1.0F - reveal) * 10.0F);
        pose.scale(scale, scale);
        pose.translate(-centerX, -centerY);
    }

    // A soft gradient in the Skills Record's background color, drifting slowly: darker at the edges, with a faint
    // light band sweeping across. Drawn in screen space, so it stays put while the map pans.
    // Also behind the viewer's messages, when no card is shown.
    @Override
    protected void extractViewBackground(GuiGraphicsExtractor graphics) {
        drawGradient(graphics, this.getBounds(), this.viewer.getTheme(), this.animated());
    }

    static void drawGradient(GuiGraphicsExtractor graphics, ScreenRectangle b, RecordTheme theme, boolean animated) {
        drawGradient(graphics, b, theme.tint(), animated);
    }

    // The record's emblems, scattered in different sizes and each moving in its own way, and its theme's effect (snow,
    // falling leaves, fireflies...), over the given world area: they pan and zoom with the map.
    static void drawTheme(GuiGraphicsExtractor graphics, double left, double top, double right, double bottom, double zoom, RecordTheme theme, boolean animated) {
        theme.motion().drawScattered(graphics, left, top, right, bottom, theme.emblem(), animated);
        theme.effect().draw(graphics, left, top, right, bottom, zoom, animated);
    }

    private static void drawGradient(GuiGraphicsExtractor graphics, ScreenRectangle b, int accentColor, boolean animated) {
        int accent = ARGB.opaque(accentColor);
        int deep = ARGB.multiply(BG_DEEP, accent), mid = ARGB.multiply(BG_MID, accent);
        float t = animated ? time() : 0.0F;

        int bands = Math.max(1, b.height() / 12);
        for (int i = 0; i < bands; i++) {
            int y0 = b.top() + b.height() * i / bands, y1 = b.top() + b.height() * (i + 1) / bands;
            graphics.fillGradient(b.left(), y0, b.right(), y1,
                    gradientColor(deep, mid, i / (float) bands, t), gradientColor(deep, mid, (i + 1) / (float) bands, t));
        }

        int glow = ARGB.multiply(BG_GLOW, accent);
        float center = b.left() + b.width() * (0.5F + 0.42F * Mth.sin(t * 0.23F));
        float spread = b.width() * 0.32F;
        for (int x = b.left(); x < b.right(); x += 2) {
            float d = (x + 1 - center) / spread;
            float alpha = 0.16F * (float) Math.exp(-d * d);
            if (alpha > 0.005F) graphics.fill(x, b.top(), Math.min(x + 2, b.right()), b.bottom(), ARGB.color(alpha, glow));
        }
    }

    private static int gradientColor(int deep, int mid, float y, float t) {
        // Brightest a little above the middle, rising and falling slowly.
        float wave = 0.5F + 0.5F * Mth.sin(y * Mth.PI * 1.1F + 0.3F + t * 0.35F);
        float edge = 1.0F - 4.0F * (y - 0.45F) * (y - 0.45F);
        return ARGB.srgbLerp(Mth.clamp(0.25F + 0.5F * wave * edge + 0.25F * edge, 0.0F, 1.0F), deep, mid);
    }

    // Faint Mining Skill Cards tiled behind the tree: the card's frame, its written lines with their bullets, the
    // picture box and the tier badge, in the record's color. A light wave passes over them one after another.
    private void drawBackground(GuiGraphicsExtractor graphics) {
        ScreenRectangle b = this.getBounds();
        double left = this.toWorldX(b.left()), top = this.toWorldY(b.top()), right = this.toWorldX(b.right()), bottom = this.toWorldY(b.bottom());
        drawCards(graphics, left, top, right, bottom, this.getZoom(), this.viewer.getTheme(), this.animated());
        drawTheme(graphics, left, top, right, bottom, this.getZoom(), this.viewer.getTheme(), this.animated());
    }

    // Draws the cards covering the given world area, seen at the given zoom.
    static void drawCards(GuiGraphicsExtractor graphics, double left, double top, double right, double bottom, double zoom, RecordTheme theme, boolean animated) {
        float t = time();
        int accent = theme.tint();
        int paper = ARGB.multiply(CARD_PAPER, accent), ink = ARGB.multiply(CARD_INK, accent);
        boolean detailed = zoom >= 0.6;

        // The whole pattern drifts slowly to the right; the blocks it's laid out in come in from the left.
        float drift = animated ? t * CARD_DRIFT : 0.0F;
        int row0 = Mth.floor(top / CARD_BLOCK), row1 = Mth.floor(bottom / CARD_BLOCK);
        int col0 = Mth.floor((left - drift) / CARD_BLOCK), col1 = Mth.floor((right - drift) / CARD_BLOCK);
        for (int row = row0; row <= row1; row++) {
            for (int col = col0; col <= col1; col++) {
                layoutCards(graphics, col * CARD_BLOCK + drift, row * CARD_BLOCK, CARD_BLOCK, CARD_BLOCK, 0,
                        Mth.murmurHash3Mixer(col * 73856093 ^ row * 19349663), zoom, t, animated, paper, ink);
            }
        }
    }

    // The world is cut into square blocks, and each block is split again and again at uneven points (along its longer
    // side), like a treemap, so the cells come in different sizes and never line up into a regular grid. Each cell
    // holds one card as large as fits, pushed to a random spot of the room left over; a few cells stay empty.
    private static void layoutCards(GuiGraphicsExtractor graphics, float x, float y, float w, float h, int depth, int seed,
                                    double zoom, float t, boolean animated, int paper, int ink) {
        float roll = (seed >>> 24 & 0xFF) / 255.0F;
        boolean split = depth < 4 && Math.max(w, h) >= CARD_WIDTH * 1.1F && (depth <= 1 || roll > 0.35F);
        if (split) {
            float ratio = 0.3F + 0.4F * ((seed & 0xFF) / 255.0F);
            int a = Mth.murmurHash3Mixer(seed + 1), b = Mth.murmurHash3Mixer(seed + 2);
            if (w * CARD_HEIGHT >= h * CARD_WIDTH) {
                float first = w * ratio;
                layoutCards(graphics, x, y, first, h, depth + 1, a, zoom, t, animated, paper, ink);
                layoutCards(graphics, x + first, y, w - first, h, depth + 1, b, zoom, t, animated, paper, ink);
            } else {
                float first = h * ratio;
                layoutCards(graphics, x, y, w, first, depth + 1, a, zoom, t, animated, paper, ink);
                layoutCards(graphics, x, y + first, w, h - first, depth + 1, b, zoom, t, animated, paper, ink);
            }
            return;
        }
        if (roll < 0.06F) return;

        float scale = Math.min(Math.min((w - CARD_GAP) / CARD_WIDTH, (h - CARD_GAP) / CARD_HEIGHT), 2.2F);
        if (scale < 0.3F) return;
        float cw = CARD_WIDTH * scale, ch = CARD_HEIGHT * scale;
        float cx = x + CARD_GAP / 2.0F + (w - CARD_GAP - cw) * (0.2F + 0.6F * (seed >>> 8 & 0xFF) / 255.0F);
        float cy = y + CARD_GAP / 2.0F + (h - CARD_GAP - ch) * (0.2F + 0.6F * (seed >>> 16 & 0xFF) / 255.0F);

        float alpha = 0.05F;
        if (animated) {
            float wave = Math.max(0.0F, Mth.sin((cx + cw / 2 + cy + ch / 2) * 0.006F - t * 0.9F));
            alpha += 0.07F * wave * wave * wave;
        }
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(cx, cy);
        pose.scale(scale, scale);
        drawCard(graphics, (float) Math.max(1.0, 1.0 / (zoom * scale)), alpha, paper, ink, zoom * scale >= 0.6, seed);
        pose.popMatrix();
    }

    // One card at the origin, CARD_WIDTH x CARD_HEIGHT.
    private static void drawCard(GuiGraphicsExtractor graphics, float px, float alpha, int paper, int ink, boolean detailed, int seed) {
        int x = 0, y = 0, w = CARD_WIDTH, h = CARD_HEIGHT;
        int frame = ARGB.color(alpha, paper), inner = ARGB.color(alpha * 0.6F, paper);
        outline(graphics, x, y, w, h, px, frame);
        outline(graphics, x + 3, y + 3, w - 6, h - 6, px, inner);
        if (!detailed) return;

        // The written lines: a bullet, then strokes of varying length.
        int bullet = ARGB.color(alpha * 1.6F, paper), stroke = ARGB.color(alpha * 1.2F, ink);
        for (int line = 0; line < 4; line++) {
            float ly = y + 8 + line * 5;
            rect(graphics, x + 7, ly, 2, px, bullet);
            int r = Mth.murmurHash3Mixer(seed + line * 31);
            float lx = x + 11;
            for (int part = 0; part < 3 && lx < x + w - 10; part++) {
                float len = Math.min(8 + (r >>> (part * 5) & 15), x + w - 8 - lx);
                rect(graphics, lx, ly, len, px, stroke);
                lx += len + 4;
            }
        }
        // The picture box and the tier badge.
        outline(graphics, x + 7, y + 29, 26, h - 35, px, frame);
        graphics.fill(x + 40, y + 31, x + w - 7, y + h - 8, ARGB.color(alpha * 0.5F, paper));
        outline(graphics, x + 40, y + 31, w - 47, h - 39, px, frame);
    }

    private static void outline(GuiGraphicsExtractor graphics, float x, float y, float w, float h, float px, int color) {
        rect(graphics, x, y, w, px, color);
        rect(graphics, x, y + h - px, w, px, color);
        rect(graphics, x, y + px, px, h - 2 * px, color);
        rect(graphics, x + w - px, y + px, px, h - 2 * px, color);
    }

    // A fill at fractional world coordinates, so lines stay at least a pixel thick when zoomed out.
    private static void rect(GuiGraphicsExtractor graphics, float x, float y, float w, float h, int color) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(w, h);
        graphics.fill(0, 0, 1, 1, color);
        pose.popMatrix();
    }

    // Lines grow in with the node they lead to, using the same staggered reveal as the nodes.
    private void drawConnections(GuiGraphicsExtractor graphics) {
        List<TierNode> tiers = this.tree.tiers;
        int midY = CardTree.TIER_HEIGHT / 2;
        float t = time();

        int[] firstIndex = new int[tiers.size()];
        for (int i = 0, index = 0; i < tiers.size(); i++) {
            firstIndex[i] = index;
            index += 1 + tiers.get(i).challenges().size();
        }

        for (int i = 0; i + 1 < tiers.size(); i++) {
            float reveal = this.reveal(firstIndex[i + 1]);
            if (reveal <= 0.0F) continue;
            TierNode from = tiers.get(i), to = tiers.get(i + 1);
            int start = from.x() + CardTree.TIER_WIDTH, end = to.x() - BADGE_SIZE / 2;
            int grown = start + Math.round((end - start) * reveal);
            boolean open = to.state() != TierState.LOCKED;
            if (open) {
                graphics.fill(start, midY - 1, grown, midY + 1, ARGB.multiplyAlpha(LINE_DONE, reveal));
                if (reveal >= 1.0F) this.drawPulses(graphics, start, end, midY - 1, true, t + i * 0.37F);
            } else {
                for (int x = start; x < grown; x += 7) graphics.fill(x, midY - 1, Math.min(x + 4, grown), midY + 1, ARGB.multiplyAlpha(LINE_LOCKED, reveal));
            }
            // Even-sized so it centers on the 2px line; shows once the line has reached the middle.
            float joint = Mth.clamp((reveal - 0.5F) * 2.0F, 0.0F, 1.0F);
            if (joint > 0.0F) {
                int jointX = (start + end) / 2 - 4;
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, JOINT, jointX, midY - 4, 8, 8, ARGB.multiplyAlpha(open ? 0xFF8FE08F : 0xFF5A5A5A, joint));
            }
        }

        for (int i = 0; i < tiers.size(); i++) {
            TierNode tier = tiers.get(i);
            if (tier.challenges().isEmpty()) continue;
            float tierReveal = this.reveal(firstIndex[i]);
            if (tierReveal <= 0.0F) continue;
            // Hangs from the badge's bottom tip, which sits on the box's left edge.
            int trunkX = tier.x() - 1;
            int trunkTop = tier.y() + (CardTree.TIER_HEIGHT + BADGE_SIZE) / 2 - 1;
            boolean done = tier.state() == TierState.COMPLETED;
            boolean current = tier.state() == TierState.CURRENT;
            boolean settled = this.reveal(firstIndex[i] + tier.challenges().size()) >= 1.0F;

            // The trunk reaches down row by row as each row appears.
            int trunkBottom = trunkTop;
            for (int j = 0; j < tier.challenges().size(); j++) {
                float rowReveal = this.reveal(firstIndex[i] + 1 + j);
                if (rowReveal <= 0.0F) break;
                int rowY = tier.challenges().get(j).y() + CardTree.ROW_BOX_HEIGHT / 2 + 1;
                trunkBottom += Math.round((rowY - trunkBottom) * rowReveal);
            }
            if (trunkBottom > trunkTop) {
                graphics.fill(trunkX, trunkTop, trunkX + 2, trunkBottom, ARGB.multiplyAlpha(done ? LINE_DONE : LINE_OPEN, tierReveal));
                if (current && settled) this.drawPulses(graphics, trunkTop, trunkBottom, trunkX, false, t);
            }

            for (int j = 0; j < tier.challenges().size(); j++) {
                ChallengeNode row = tier.challenges().get(j);
                float rowReveal = this.reveal(firstIndex[i] + 1 + j);
                if (rowReveal <= 0.0F) continue;
                int y = row.y() + CardTree.ROW_BOX_HEIGHT / 2 - 1;
                boolean rowDone = row.state() == ChallengeState.COMPLETED;
                int branchEnd = trunkX + Math.round((row.x() + 1 - trunkX) * rowReveal);
                graphics.fill(trunkX, y, branchEnd, y + 2, ARGB.multiplyAlpha(rowDone ? LINE_DONE : LINE_OPEN, rowReveal));
                if (current && settled) this.drawBranchPulse(graphics, trunkX, row.x() + 1, y, y - trunkTop, t);
                graphics.fill(trunkX - 1, y - 1, trunkX + 3, y + 3, ARGB.multiplyAlpha(rowDone ? 0xFF8FE08F : 0xFF9A9A9A, rowReveal));
            }
        }
    }

    private void drawBranchPulse(GuiGraphicsExtractor graphics, int from, int to, int y, int distance, float t) {
        if (!this.animated()) return;
        int length = to - from;
        float p = ((t * PULSE_SPEED - distance) % PULSE_SPACING + PULSE_SPACING) % PULSE_SPACING;
        if (p >= length) return;
        float alpha = Mth.clamp((length - p) / 3.0F, 0.0F, 1.0F);
        int x = from + (int) p;
        graphics.fill(x, y, Math.min(x + 3, to), y + 2, ARGB.color(alpha, PULSE));
    }

    private void drawPulses(GuiGraphicsExtractor graphics, int from, int to, int across, boolean horizontal, float t) {
        if (!this.animated() || to - from < 8) return;
        int length = to - from;
        int spacing = PULSE_SPACING;
        float offset = (t * PULSE_SPEED) % spacing;
        for (float p = offset; p < length; p += spacing) {
            float edge = Math.min(p, length - p) / 6.0F;
            int color = ARGB.color(Mth.clamp(edge, 0.0F, 1.0F), PULSE);
            int pos = from + (int) p;
            if (horizontal) graphics.fill(pos, across, pos + 3, across + 2, color);
            else graphics.fill(across, pos, across + 2, pos + 3, color);
        }
    }

    private void drawTier(GuiGraphicsExtractor graphics, TierNode node, float hover, float reveal) {
        if (reveal <= 0.0F) return;
        int x = node.x(), y = node.y(), w = CardTree.TIER_WIDTH, h = CardTree.TIER_HEIGHT;
        int fill, border;
        switch (node.state()) {
            case COMPLETED -> { fill = 0xFF1E3A22; border = 0xFF6BCB6B; }
            case SKIPPED -> { fill = 0xFF3A3620; border = 0xFFB9A85A; }
            case CURRENT -> {
                fill = 0xFF2A2F3A;
                float pulse = this.animated() ? (Mth.sin(time() * 3.3F) + 1.0F) / 2.0F : 1.0F;
                border = ARGB.srgbLerp(pulse, 0xFF7A8AA0, 0xFFB4D2FF);
            }
            default -> { fill = 0xFF1C1C1C; border = 0xFF454545; }
        }
        fill = ARGB.srgbLerp(0.35F * hover, fill, 0xFF505050);
        border = ARGB.srgbLerp(0.5F * hover, border, 0xFFFFFFFF);
        // The tier's box, border and badge take on the record's color, keeping a hint of the state's.
        fill = this.viewer.recordTinted(fill);
        border = this.viewer.recordTinted(border);

        this.pushNodeTransform(graphics, x + w / 2.0F, y + h / 2.0F, hover, reveal);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TIER_FILL, x + 2, y + 2, w, h, ARGB.color(0.35F * reveal, 0x000000));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TIER_FILL, x, y, w, h, ARGB.multiplyAlpha(fill, reveal));
        if (node.state() == TierState.CURRENT && this.animated()) this.drawShine(graphics, x, y, w, h);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TIER_BORDER, x, y, w, h, ARGB.multiplyAlpha(border, reveal));

        boolean mastered = node.tier() == MiningSkillCardItem.Tier.Mastered;
        int badgeColor = mastered && node.state() != TierState.LOCKED ? this.viewer.recordTinted(ARGB.srgbLerp(0.5F * hover, MASTERED_GOLD, 0xFFFFFFFF)) : border;
        int bx = x - BADGE_SIZE / 2, by = y + (h - BADGE_SIZE) / 2;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, mastered ? STAR_FILL : BADGE_FILL, bx, by, BADGE_SIZE, BADGE_SIZE, ARGB.multiplyAlpha(ARGB.scaleRGB(badgeColor, 0.45F), reveal));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, mastered ? STAR_BORDER : BADGE_BORDER, bx, by, BADGE_SIZE, BADGE_SIZE, ARGB.multiplyAlpha(badgeColor, reveal));
        if (reveal > 0.5F) {
            ItemStack icon = node.state() == TierState.LOCKED ? ItemStack.EMPTY : this.tierIcon(node.tier());
            if (!icon.isEmpty()) graphics.item(icon, bx + (BADGE_SIZE - 16) / 2, by + (BADGE_SIZE - 16) / 2);
            else graphics.centeredText(this.font, "?", bx + BADGE_SIZE / 2, by + (BADGE_SIZE - 8) / 2, ARGB.multiplyAlpha(0xFF8A8A8A, reveal));
        }

        boolean shadow = this.viewer.hasTextShadow();
        int textX = x + BADGE_SIZE / 2 + 4;
        Component name = node.tier().getDisplayName();
        if (node.state() == TierState.LOCKED) name = name.copy().withStyle(ChatFormatting.DARK_GRAY);
        graphics.text(this.font, name, textX, y + 7, ARGB.multiplyAlpha(RenderUtils.textColor(0xFFFFFF), reveal), shadow);
        graphics.text(this.font, this.tierStatus(node), textX, y + 18, ARGB.multiplyAlpha(RenderUtils.textColor(0xAAAAAA), reveal), shadow);

        // An unclaimed Shape Certificate waits on the box's right, glowing gold.
        ItemStack reward = this.viewer.claimableCertificate(node);
        if (!reward.isEmpty()) {
            float glow = this.animated() ? (Mth.sin(time() * 4.0F) + 1.0F) / 2.0F : 1.0F;
            int sx = x + w - SMALL_SLOT_SIZE - 5, sy = y + (h - SMALL_SLOT_SIZE) / 2;
            drawSmallSlot(graphics, sx, sy, ARGB.multiplyAlpha(this.viewer.recordTinted(ARGB.srgbLerp(glow, 0xFF3A2E12, 0xFF6A5420)), reveal),
                    ARGB.multiplyAlpha(this.viewer.recordTinted(ARGB.srgbLerp(glow, 0xFFB08A30, MASTERED_GOLD)), reveal));
            if (reveal > 0.5F) graphics.item(reward, sx + (SMALL_SLOT_SIZE - 16) / 2, sy + (SMALL_SLOT_SIZE - 16) / 2);
        }
        graphics.pose().popMatrix();
    }

    private void drawShine(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        float cycle = (time() % 3.2F) / 3.2F;
        int bandX = -16 + (int) ((w + 32) * cycle);
        for (int i = 0; i < 12; i++) {
            int col = bandX + i;
            if (col < 1 || col >= w - 1) continue;
            int top = Math.max(0, Math.max(8 - col, col - (w - 3))) + 1;
            int bottom = h - Math.max(0, Math.max(col - (w - 9), 2 - col)) - 1;
            if (bottom <= top) continue;
            int alpha = i < 3 || i >= 9 ? 0x10 : 0x22;
            graphics.fill(x + col, y + top, x + col + 1, y + bottom, alpha << 24 | 0xFFFFFF);
        }
    }

    // An in-progress challenge's frame: a gold gradient cycling along it, drawn as narrow slices of the frame sprite,
    // each clipped and colored from the gradient at its spot.
    private static final ColorGradient IN_PROGRESS_GOLD = ColorGradient.of(0xC8962A, 0xFFE27A, 0xFFF6C8, 0xE0B040).withSpeed(0.45F).withSpread(1.0F);
    private static final int GOLD_SLICES = 16;
    private static final int IN_PROGRESS_BORDER = 0xFFE8BE4A;

    private void drawGoldBorder(GuiGraphicsExtractor graphics, int x, int y, int w, int h, float reveal) {
        int alpha = Math.round(255 * reveal);
        // The whole frame in gold first, then the gradient over it in slices. The slices are clipped in screen pixels,
        // computed here from the current (zoomed) pose, so neighbours share exactly the same edge at any zoom.
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ROW_BORDER, x, y, w, h, ARGB.multiplyAlpha(IN_PROGRESS_BORDER, reveal));
        Matrix3x2fStack pose = graphics.pose();
        Matrix3x2f m = new Matrix3x2f(pose);
        int top = Math.round(m.m01() * x + m.m11() * (y - 1) + m.m21());
        int bottom = Math.round(m.m01() * x + m.m11() * (y + h + 1) + m.m21());
        int previous = Math.round(m.m00() * x + m.m10() * y + m.m20());
        for (int k = 0; k < GOLD_SLICES; k++) {
            float edge = x + w * (k + 1) / (float) GOLD_SLICES;
            int next = Math.round(m.m00() * edge + m.m10() * y + m.m20());
            if (next > previous) {
                pose.pushMatrix();
                pose.identity();
                graphics.enableScissor(previous, Math.min(top, bottom), next, Math.max(top, bottom));
                pose.popMatrix();
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ROW_BORDER, x, y, w, h, IN_PROGRESS_GOLD.color(k / (float) GOLD_SLICES, alpha));
                graphics.disableScissor();
            }
            previous = next;
        }
    }

    // A pixel check mark (7 x 6) with a dark edge, its top left at x, y.
    private static final String[] CHECK = {
            "......X",
            ".....XX",
            "X...XX.",
            "XX.XX..",
            ".XXX...",
            "..X....",
    };

    private static void drawCheck(GuiGraphicsExtractor graphics, int x, int y, int color) {
        int edge = ARGB.color(ARGB.alpha(color) / 255.0F * 0.6F, 0x000000);
        for (int pass = 0; pass < 2; pass++) {
            for (int row = 0; row < CHECK.length; row++) {
                for (int col = 0; col < CHECK[row].length(); col++) {
                    if (CHECK[row].charAt(col) != 'X') continue;
                    int px = x + col + (pass == 0 ? 1 : 0), py = y + row + (pass == 0 ? 1 : 0);
                    graphics.fill(px, py, px + 1, py + 1, pass == 0 ? edge : color);
                }
            }
        }
    }

    static void drawSlot(GuiGraphicsExtractor graphics, int x, int y, int fill, int border) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_FILL, x, y, SLOT_SIZE, SLOT_SIZE, fill);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_BORDER, x, y, SLOT_SIZE, SLOT_SIZE, border);
    }

    static void drawSmallSlot(GuiGraphicsExtractor graphics, int x, int y, int fill, int border) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SMALL_SLOT_FILL, x, y, SMALL_SLOT_SIZE, SMALL_SLOT_SIZE, fill);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SMALL_SLOT_BORDER, x, y, SMALL_SLOT_SIZE, SMALL_SLOT_SIZE, border);
    }

    static int progressColor(int progress) {
        return Mth.hsvToRgb(Math.min(progress / 100.0F, 1.0F) / 3.0F, 1.0F, 1.0F);
    }

    // The track is tinted with the Skills Record's color; the fill keeps its own, red to green with progress.
    static void drawProgressBar(GuiGraphicsExtractor graphics, int x, int y, int width, float progress, int recordColor) {
        if (width < 6) return;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SkillsRecordScreen.PROGRESS_BAR_SPRITE, x, y, width, PROGRESS_BAR_HEIGHT, ARGB.opaque(recordColor));
        int fill = Math.round((width - 2) * Math.clamp(progress, 0.0F, 1.0F));
        if (fill > 0) {
            int color = progressColor(Math.round(progress * 100));
            graphics.fillGradient(x + 1, y + 1, x + 1 + fill, y + PROGRESS_BAR_HEIGHT - 1, ARGB.color(0.75F, color), ARGB.color(0.55F, color));
        }
    }

    private Component tierStatus(TierNode node) {
        return switch (node.state()) {
            case COMPLETED -> node.hasRecord()
                    ? Component.translatable("gui.ultimine_addition.card_viewer.tier.completed").withStyle(ChatFormatting.GREEN)
                    : Component.translatable("gui.ultimine_addition.card_viewer.tier.no_record").withStyle(ChatFormatting.DARK_GREEN);
            case SKIPPED -> Component.translatable("gui.ultimine_addition.card_viewer.tier.skipped").withStyle(ChatFormatting.YELLOW);
            case CURRENT -> node.tier() == MiningSkillCardItem.Tier.Mastered
                    ? Component.translatable("gui.ultimine_addition.card_viewer.tier.mastered").withStyle(ChatFormatting.GOLD)
                    : Component.literal(node.completedCount() + "/" + node.challenges().size()).withStyle(ChatFormatting.AQUA);
            case LOCKED -> Component.translatable("gui.ultimine_addition.card_viewer.tier.locked").withStyle(ChatFormatting.DARK_GRAY);
        };
    }

    private void drawChallenge(GuiGraphicsExtractor graphics, ChallengeNode node, float hover, float reveal) {
        if (reveal <= 0.0F) return;
        int x = node.x(), y = node.y(), w = CardTree.ROW_WIDTH, h = CardTree.ROW_BOX_HEIGHT;
        int accent = switch (node.state()) {
            case COMPLETED -> 0xFF6BCB6B;
            case IN_PROGRESS -> 0xFFE0C050;
            case NEEDS_CONSUME_MODE -> 0xFFD14A4A;
            case NOT_STARTED, LOCKED -> 0xFF8A8A8A;
        };
        // The row goes gold while in progress and green once done (like a completed tier), otherwise a dark shade of
        // the record's color; lighter while hovered. Framed and slotted in the record's color, with the count's color
        // and the mark in the corner telling the state too.
        int record = ARGB.opaque(this.viewer.getAccentColor());
        int fill = switch (node.state()) {
            case COMPLETED -> this.viewer.recordTinted(0xFF1E3A22);
            case IN_PROGRESS -> this.viewer.recordTinted(0xFF3E3418);
            default -> ARGB.scaleRGB(record, 0.24F);
        };
        fill = ARGB.srgbLerp(0.35F * hover, fill, ARGB.srgbLerp(0.5F, fill, 0xFF505050));
        int border = this.viewer.recordTinted(ARGB.srgbLerp(0.55F * hover, 0xFF8A8A8A, 0xFFFFFFFF));

        this.pushNodeTransform(graphics, x + w / 2.0F, y + h / 2.0F, hover, reveal);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ROW_FILL, x + 2, y + 2, w, h, ARGB.color(0.3F * reveal, 0x000000));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ROW_FILL, x, y, w, h, ARGB.multiplyAlpha(fill, reveal));
        // In progress: gold, with the gradient cycling around the frame when animated.
        if (node.state() == ChallengeState.IN_PROGRESS && this.animated()) this.drawGoldBorder(graphics, x, y, w, h, reveal);
        else if (node.state() == ChallengeState.IN_PROGRESS) graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ROW_BORDER, x, y, w, h, ARGB.multiplyAlpha(IN_PROGRESS_BORDER, reveal));
        else graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ROW_BORDER, x, y, w, h, ARGB.multiplyAlpha(border, reveal));

        int slotY = y + (h - SLOT_SIZE) / 2;
        // In progress, the slot is gold like the frame: the gradient's color where the slot sits along it.
        int slotBorder = node.state() != ChallengeState.IN_PROGRESS ? border
                : this.animated() ? IN_PROGRESS_GOLD.color((6 + SLOT_SIZE / 2.0F) / w, 255) : IN_PROGRESS_BORDER;
        drawSlot(graphics, x + 6, slotY, ARGB.multiplyAlpha(ARGB.scaleRGB(slotBorder, 0.35F), reveal), ARGB.multiplyAlpha(slotBorder, reveal));
        if (reveal > 0.5F && !node.targets().isEmpty()) {
            ItemStack target = node.targets().get((int) (Util.getMillis() / 1000L % node.targets().size()));
            Matrix3x2fStack itemPose = graphics.pose();
            itemPose.pushMatrix();
            itemPose.translate(x + 6 + SLOT_SIZE / 2.0F, slotY + SLOT_SIZE / 2.0F);
            itemPose.scale(SLOT_ITEM_SCALE, SLOT_ITEM_SCALE);
            graphics.item(target, -8, -8);
            itemPose.popMatrix();
        }

        boolean shadow = this.viewer.hasTextShadow();
        int textX = x + 6 + SLOT_SIZE + 4;
        String title = CardViewerWidget.challengeName(node).getString();
        CardViewerWidget.drawFittedText(graphics, this.font, Component.literal(title), width -> CardViewerWidget.ellipsize(this.font, title, width),
                textX, y + 3, x + w - 4 - textX - (node.state() == ChallengeState.COMPLETED || node.state() == ChallengeState.NEEDS_CONSUME_MODE ? 10 : 0),
                ARGB.multiplyAlpha(RenderUtils.textColor(0xFBF1C1), reveal), shadow);
        if (node.pinned()) graphics.text(this.font, "◎", x + w - 11, y + 12, ARGB.multiplyAlpha(RenderUtils.textColor(0xFFFF55), reveal), shadow);

        String progress = node.state() == ChallengeState.COMPLETED ? node.requiredPoints() + "/" + node.requiredPoints()
                : node.currentPoints() + "/" + node.requiredPoints();
        // A green check once done; a red "!" when it needs Consume Mode.
        if (node.state() == ChallengeState.COMPLETED) drawCheck(graphics, x + w - 12, y + 3, ARGB.multiplyAlpha(0xFF6BCB6B, reveal));
        else if (node.state() == ChallengeState.NEEDS_CONSUME_MODE) {
            graphics.fill(x + w - 9, y + 3, x + w - 7, y + 8, ARGB.multiplyAlpha(0xFFD14A4A, reveal));
            graphics.fill(x + w - 9, y + 9, x + w - 7, y + 11, ARGB.multiplyAlpha(0xFFD14A4A, reveal));
        }
        graphics.text(this.font, progress, textX, y + 12, ARGB.multiplyAlpha(RenderUtils.textColor(accent & 0xFFFFFF), reveal), shadow);
        graphics.pose().popMatrix();
    }

    private ItemStack tierIcon(MiningSkillCardItem.Tier tier) {
        ItemStack card = this.viewer.getSelectedCardStack();
        if (!(card.getItem() instanceof MiningSkillCardItem item)) return ItemStack.EMPTY;
        // Separate stacks, so each tier's model shows without touching the real card.
        return this.tierIcons.computeIfAbsent(item, k -> new EnumMap<>(MiningSkillCardItem.Tier.class))
                .computeIfAbsent(tier, t -> MiningSkillCardData.createForCreativeTab(item, t));
    }

    @Override
    protected void extractViewForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (this.hoveredChallenge != null) {
            graphics.setTooltipForNextFrame(this.font, this.viewer.describeChallenge(this.hoveredChallenge), mouseX, mouseY);
        } else if (this.hoveredTier != null) {
            ItemStack reward = this.viewer.claimableCertificate(this.hoveredTier);
            if (!reward.isEmpty()) graphics.setTooltipForNextFrame(this.font, this.viewer.describeCertificate(this.hoveredTier, reward), mouseX, mouseY);
        }
    }

    @Override
    protected boolean worldClicked(double worldX, double worldY, MouseButtonEvent event) {
        if (this.tree == null) return false;
        for (TierNode tier : this.tree.tiers) {
            if (tier.contains(worldX, worldY)) {
                ItemStack reward = this.viewer.claimableCertificate(tier);
                if (!reward.isEmpty()) {
                    if (this.viewer.hasRoomFor(reward)) {
                        this.viewer.openShapeChoice(tier);
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    }
                    return true;
                }
                ScreenRectangle b = this.getBounds();
                this.centerOn(tier.centerX(), this.toWorldY(b.top() + b.height() / 2.0));
                return true;
            }
            for (ChallengeNode challenge : tier.challenges()) {
                if (challenge.state() != ChallengeState.LOCKED && challenge.contains(worldX, worldY)) {
                    this.viewer.openDetails(challenge);
                    return true;
                }
            }
        }
        return false;
    }
}
