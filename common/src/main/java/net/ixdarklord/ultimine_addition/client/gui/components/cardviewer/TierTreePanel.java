package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.coolcatlib.api.client.gui.components.widgets.panel.ViewportPanel;
import net.ixdarklord.coolcatlib.api.client.utils.RenderUtils;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardTree.ChallengeNode;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardTree.ChallengeState;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardTree.TierNode;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardTree.TierState;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
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
    static final int SLOT_SIZE = 24;
    static final int SMALL_SLOT_SIZE = 20;
    private static final int LINE_DONE = 0xFF4E9A56;
    private static final int LINE_LOCKED = 0xFF4A4A4A;
    private static final int LINE_OPEN = 0xFF7A7A7A;
    private static final int PULSE = 0xFFC8FFC8;
    private static final int PULSE_SPACING = 26;
    private static final float PULSE_SPEED = 32.0F;
    private static final long REVEAL_STEP_MS = 40L;
    private static final long REVEAL_DURATION_MS = 260L;

    private final CardViewerWidget viewer;
    private final Map<Item, Map<MiningSkillCardItem.Tier, ItemStack>> tierIcons = new HashMap<>();
    private final Map<String, Float> hoverProgress = new HashMap<>();
    private @Nullable CardTree tree;
    private MiningSkillCardItem.@Nullable Tier pendingFocus;
    private @Nullable ChallengeNode hoveredChallenge;
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

        this.drawBackground(graphics);
        if (this.tree == null) return;
        this.drawConnections(graphics);

        int index = 0;
        for (TierNode tier : this.tree.tiers) {
            boolean hovered = mouseInView && tier.contains(mouseX, mouseY);
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
        return 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t); // ease-out cubic
    }

    private void pushNodeTransform(GuiGraphicsExtractor graphics, float centerX, float centerY, float hover, float reveal) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        float scale = 1.0F + 0.04F * hover;
        pose.translate(centerX, centerY - 1.5F * hover + (1.0F - reveal) * 10.0F);
        pose.scale(scale, scale);
        pose.translate(-centerX, -centerY);
    }

    private void drawBackground(GuiGraphicsExtractor graphics) {
        ScreenRectangle b = this.getBounds();
        double zoom = this.getZoom();
        int step = zoom < 0.7 ? 32 : 16;
        float dot = (float) Math.max(1.0, 1.0 / zoom);
        float t = time();
        boolean animated = this.animated();

        int x0 = Mth.floor(this.toWorldX(b.left()) / step) * step, x1 = Mth.ceil(this.toWorldX(b.right()));
        int y0 = Mth.floor(this.toWorldY(b.top()) / step) * step, y1 = Mth.ceil(this.toWorldY(b.bottom()));
        Matrix3x2fStack pose = graphics.pose();
        for (int y = y0; y <= y1; y += step) {
            for (int x = x0; x <= x1; x += step) {
                float alpha = 0.07F;
                if (animated) {
                    float wave = Math.max(0.0F, Mth.sin((x + y) * 0.03F - t * 1.8F));
                    alpha = 0.05F + 0.16F * wave * wave * wave;
                }
                pose.pushMatrix();
                pose.translate(x, y);
                pose.scale(dot, dot);
                graphics.fill(0, 0, 1, 1, ARGB.white(alpha));
                pose.popMatrix();
            }
        }

        if (!animated) return;
        int cell = 56;
        int cx0 = Mth.floor(this.toWorldX(b.left()) / cell), cx1 = Mth.floor(this.toWorldX(b.right()) / cell);
        int cy0 = Mth.floor(this.toWorldY(b.top()) / cell), cy1 = Mth.floor(this.toWorldY(b.bottom()) / cell);
        for (int cy = cy0; cy <= cy1; cy++) {
            for (int cx = cx0; cx <= cx1; cx++) {
                int seed = Mth.murmurHash3Mixer(cx * 73856093 ^ cy * 19349663);
                float speed = 5.0F + (seed >>> 8 & 7);
                float mx = cx * cell + (seed & 0xFFFF) % cell;
                float my = cy * cell + cell - ((t * speed + (seed >>> 16 & 0xFF)) % cell);
                float alpha = 0.18F + 0.18F * Mth.sin(t * 2.2F + (seed & 0xFF));
                float size = dot * 1.5F;
                pose.pushMatrix();
                pose.translate(mx, my);
                pose.scale(size, size);
                graphics.fill(0, 0, 1, 1, ARGB.color(alpha, 0xDDE8FF));
                pose.popMatrix();
            }
        }
    }

    private void drawConnections(GuiGraphicsExtractor graphics) {
        List<TierNode> tiers = this.tree.tiers;
        int midY = CardTree.TIER_HEIGHT / 2;
        float t = time();

        for (int i = 0; i + 1 < tiers.size(); i++) {
            TierNode from = tiers.get(i), to = tiers.get(i + 1);
            int start = from.x() + CardTree.TIER_WIDTH, end = to.x() - BADGE_SIZE / 2;
            boolean open = to.state() != TierState.LOCKED;
            if (open) {
                graphics.fill(start, midY - 1, end, midY + 1, LINE_DONE);
                this.drawPulses(graphics, start, end, midY - 1, true, t + i * 0.37F);
            } else {
                for (int x = start; x < end; x += 7) graphics.fill(x, midY - 1, Math.min(x + 4, end), midY + 1, LINE_LOCKED);
            }
            // Even-sized so it centers on the 2px line.
            int jointX = (start + end) / 2 - 4;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, JOINT, jointX, midY - 4, 8, 8, open ? 0xFF8FE08F : 0xFF5A5A5A);
        }

        for (TierNode tier : tiers) {
            if (tier.challenges().isEmpty()) continue;
            // Hangs from the badge's bottom tip, which sits on the box's left edge.
            int trunkX = tier.x() - 1;
            int trunkTop = tier.y() + (CardTree.TIER_HEIGHT + BADGE_SIZE) / 2 - 1;
            ChallengeNode last = tier.challenges().getLast();
            int trunkBottom = last.y() + CardTree.ROW_BOX_HEIGHT / 2 + 1;
            boolean done = tier.state() == TierState.COMPLETED;
            graphics.fill(trunkX, trunkTop, trunkX + 2, trunkBottom, done ? LINE_DONE : LINE_OPEN);
            if (tier.state() == TierState.CURRENT) this.drawPulses(graphics, trunkTop, trunkBottom, trunkX, false, t);

            for (ChallengeNode row : tier.challenges()) {
                int y = row.y() + CardTree.ROW_BOX_HEIGHT / 2 - 1;
                boolean rowDone = row.state() == ChallengeState.COMPLETED;
                graphics.fill(trunkX, y, row.x() + 1, y + 2, rowDone ? LINE_DONE : LINE_OPEN);
                if (tier.state() == TierState.CURRENT) this.drawBranchPulse(graphics, trunkX, row.x() + 1, y, y - trunkTop, t);
                graphics.fill(trunkX - 1, y - 1, trunkX + 3, y + 3, rowDone ? 0xFF8FE08F : 0xFF9A9A9A);
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
                border = ARGB.srgbLerp(pulse, 0xFF7A8AA0, ARGB.opaque(this.viewer.getAccentColor()));
            }
            default -> { fill = 0xFF1C1C1C; border = 0xFF454545; }
        }
        fill = ARGB.srgbLerp(0.35F * hover, fill, 0xFF505050);
        border = ARGB.srgbLerp(0.5F * hover, border, 0xFFFFFFFF);

        this.pushNodeTransform(graphics, x + w / 2.0F, y + h / 2.0F, hover, reveal);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TIER_FILL, x + 2, y + 2, w, h, ARGB.color(0.35F * reveal, 0x000000));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TIER_FILL, x, y, w, h, ARGB.multiplyAlpha(fill, reveal));
        if (node.state() == TierState.CURRENT && this.animated()) this.drawShine(graphics, x, y, w, h);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TIER_BORDER, x, y, w, h, ARGB.multiplyAlpha(border, reveal));

        boolean mastered = node.tier() == MiningSkillCardItem.Tier.Mastered;
        int badgeColor = mastered && node.state() != TierState.LOCKED ? ARGB.srgbLerp(0.5F * hover, MASTERED_GOLD, 0xFFFFFFFF) : border;
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

    static void drawProgressBar(GuiGraphicsExtractor graphics, int x, int y, int width, float progress) {
        if (width < 6) return;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SkillsRecordScreen.PROGRESS_BAR_SPRITE, x, y, width, PROGRESS_BAR_HEIGHT);
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
        int fill = switch (node.state()) {
            case COMPLETED -> 0xFF1F3323;
            case IN_PROGRESS -> 0xFF3A3420;
            case NEEDS_CONSUME_MODE -> 0xFF3A1E1E;
            case NOT_STARTED, LOCKED -> 0xFF262626;
        };
        fill = ARGB.srgbLerp(0.35F * hover, fill, 0xFF505050);
        int border = ARGB.srgbLerp(0.55F * hover, accent, 0xFFFFFFFF);

        this.pushNodeTransform(graphics, x + w / 2.0F, y + h / 2.0F, hover, reveal);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ROW_FILL, x + 2, y + 2, w, h, ARGB.color(0.3F * reveal, 0x000000));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ROW_FILL, x, y, w, h, ARGB.multiplyAlpha(fill, reveal));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ROW_BORDER, x, y, w, h, ARGB.multiplyAlpha(border, reveal));

        int slotY = y + (h - SLOT_SIZE) / 2;
        drawSlot(graphics, x + 6, slotY, ARGB.multiplyAlpha(ARGB.scaleRGB(accent, 0.35F), reveal), ARGB.multiplyAlpha(border, reveal));
        if (reveal > 0.5F && !node.targets().isEmpty()) {
            ItemStack target = node.targets().get((int) (Util.getMillis() / 1000L % node.targets().size()));
            graphics.item(target, x + 6 + (SLOT_SIZE - 16) / 2, slotY + (SLOT_SIZE - 16) / 2);
        }

        boolean shadow = this.viewer.hasTextShadow();
        int textX = x + 6 + SLOT_SIZE + 2;
        String title = CardViewerWidget.challengeName(node).getString();
        CardViewerWidget.drawFittedText(graphics, this.font, Component.literal(title), width -> CardViewerWidget.ellipsize(this.font, title, width),
                textX, y + 3, x + w - 4 - textX, ARGB.multiplyAlpha(RenderUtils.textColor(0xFBF1C1), reveal), shadow);
        if (node.pinned()) graphics.text(this.font, "◎", x + w - 11, y + 12, ARGB.multiplyAlpha(RenderUtils.textColor(0xFFFF55), reveal), shadow);

        String progress = node.state() == ChallengeState.COMPLETED ? "✔ " + node.requiredPoints() + "/" + node.requiredPoints()
                : node.currentPoints() + "/" + node.requiredPoints();
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
        }
    }

    @Override
    protected boolean worldClicked(double worldX, double worldY, MouseButtonEvent event) {
        if (this.tree == null) return false;
        for (TierNode tier : this.tree.tiers) {
            if (tier.contains(worldX, worldY)) {
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
