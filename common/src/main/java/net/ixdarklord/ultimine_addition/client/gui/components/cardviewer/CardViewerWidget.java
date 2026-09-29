package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.ultimine_addition.common.item.ShapeCertificateItem;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardTree.TierNode;
import net.ixdarklord.ultimine_addition.client.gui.theme.RecordTheme;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.config.UAClientConfig;
import net.ixdarklord.coolcatcanvas.api.client.gui.components.widgets.AbstractMultiPanelWidget;
import net.ixdarklord.coolcatcanvas.api.utils.Easing;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Util;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardTree.ChallengeNode;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardTree.ChallengeState;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.record.CardHistory;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.IntFunction;

public final class CardViewerWidget extends AbstractMultiPanelWidget {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault());
    private static final Identifier FRAME_SPRITE = FTBUltimineAddition.id("container/skills_record/card_viewer/frame");
    private static final int ICON_SIZE = 11;

    public record State(boolean hasCards, ItemStack cardStack, @Nullable MiningSkillCardData card, @Nullable CardHistory history,
                        boolean consumeMode, List<ItemStack> missingItems, boolean notEnoughInk, boolean preview,
                        RecordTheme theme, boolean animated) {}

    public interface Actions {
        void togglePin(Identifier challengeId);

        void editChallenge(Identifier challengeId);

        void rerollChallenge(Identifier challengeId);

        void claimCertificate(MiningSkillCardItem.Tier tier, Identifier shape);

        int getInkAmount();

        void onExpandedChanged(boolean expanded);
    }

    private final Actions actions;
    private ScreenRectangle compactBounds;
    private final TierTreePanel tree;
    // Behind a requirement banner (missing pen or paper, not enough ink): dims the card above it.
    private static final int REQUIREMENT_BACKDROP = 0x90000000;
    private final MessagePanel banner;
    private final GuidePanel guide;
    // Opening a challenge's details slides the tree up and away while it fades into the viewer's background, then the
    // details rise into place as they fade in; closing them fades them out to the background and the tree fades back in.
    private enum Transition { NONE, TO_DETAILS, TO_TREE }
    private static final long TRANSITION_OUT_MS = 170L;
    private static final long TRANSITION_IN_MS = 230L;
    private Transition transition = Transition.NONE;
    private long transitionStart;
    private @Nullable ChallengeNode pendingDetails;
    private final ChallengeDetailsPanel details;
    private final ShapeChoicePanel shapeChoice;
    private final MessagePanel preview;
    private ViewerButton.Icon expandButton;
    private ViewerButton.Icon fitButton;
    private boolean expanded;

    private State state = new State(false, ItemStack.EMPTY, null, null, false, List.of(), false, false, RecordTheme.WHITE, true);
    private @Nullable UUID shownCard;
    private int treeSignature;

    public CardViewerWidget(int x, int y, int width, int height, Actions actions) {
        super(CommonComponents.EMPTY, x, y, width, height, false);
        this.actions = actions;
        this.compactBounds = new ScreenRectangle(x, y, width, height);
        this.tree = this.addPanel(new TierTreePanel(this));
        this.banner = this.addPanel(new MessagePanel(this, MessagePanel.Mode.BANNER));
        this.guide = this.addPanel(new GuidePanel(this));
        this.details = this.addPanel(new ChallengeDetailsPanel(this));
        this.shapeChoice = this.addPanel(new ShapeChoicePanel(this));
        this.preview = this.addPanel(new MessagePanel(this, MessagePanel.Mode.FULL));
        this.preview.setModal(true);
        this.setOverlayColor(0xA0000000);
    }

    @Override
    protected void init() {
        this.expandButton = this.addRenderableWidget(new ViewerButton.Icon(ICON_SIZE,
                Component.translatable("gui.ultimine_addition.card_viewer.expand"),
                (g, x, y, size, color) -> ViewerButton.Icon.corners(g, x, y, size, color, !this.expanded),
                b -> this.setExpanded(!this.expanded)));
        this.fitButton = this.addRenderableWidget(new ViewerButton.Icon(ICON_SIZE,
                Component.translatable("gui.ultimine_addition.card_viewer.fit"),
                ViewerButton.Icon::target,
                b -> this.tree.requestFit()));
    }

    public void update(State state) {
        this.state = state;
        MiningSkillCardData card = state.card();

        if (!state.hasCards() || card == null) {
            this.cancelTransition();
            this.tree.setVisible(false);
            this.banner.setVisible(false);
            this.details.setVisible(false);
            this.shapeChoice.setVisible(false);
            this.shownCard = null;
            this.guide.show(state.hasCards() ? GuidePanel.Kind.SELECT_CARD : GuidePanel.Kind.NO_CARDS);
        } else {
            this.guide.setVisible(false);
            this.tree.setVisible(true);

            int signature = Objects.hash(card.getUUID(), card.getTier(), card.getChallenges(), state.consumeMode(),
                    state.history() == null ? 0 : System.identityHashCode(state.history()),
                    state.history() == null ? 0 : state.history().getCompletedTiers().size());
            boolean newCard = !card.getUUID().equals(this.shownCard);
            if (signature != this.treeSignature || newCard) {
                this.treeSignature = signature;
                CardTree built = CardTree.build(card, state.history(), state.consumeMode());
                this.tree.setTree(built, newCard);
                this.details.refresh(built);
            }
            if (newCard) {
                this.shownCard = card.getUUID();
                this.cancelTransition();
                this.details.setVisible(false);
                this.shapeChoice.setVisible(false);
                this.tree.focus(card.getTier());
            }
            this.updateBanner(card);
        }

        if (state.preview()) {
            boolean centered = (System.currentTimeMillis() / 1000L) % 2 == 0;
            List<Component> lines = List.of(
                    Component.translatable("gui.ultimine_addition.skills_record.example").append(" A: 001").withStyle(ChatFormatting.WHITE),
                    Component.translatable("gui.ultimine_addition.skills_record.example").append(" B: 002").withStyle(ChatFormatting.GRAY),
                    Component.translatable("gui.ultimine_addition.skills_record.example").append(" C: 003").withStyle(ChatFormatting.DARK_AQUA),
                    Component.translatable("gui.ultimine_addition.skills_record.example").append(" D: 004").withStyle(ChatFormatting.GOLD));
            this.preview.show(lines, List.of(), ARGB.multiply(0xD8202020, this.getAccentColor()), centered);
        } else {
            this.preview.setVisible(false);
        }
    }

    private void updateBanner(MiningSkillCardData card) {
        boolean hasChallenges = !card.getChallenges().isEmpty();
        if (hasChallenges && !this.state.missingItems().isEmpty()) {
            this.banner.show(List.of(Component.translatable("gui.ultimine_addition.skills_record.missing_items").withStyle(ChatFormatting.RED)),
                    this.state.missingItems(), 0xE04B1818, true, REQUIREMENT_BACKDROP);
        } else if (hasChallenges && this.state.notEnoughInk()) {
            this.banner.show(List.of(Component.translatable("gui.ultimine_addition.skills_record.not_enough_ink").withStyle(ChatFormatting.RED)),
                    List.of(), 0xE04B1818, true, REQUIREMENT_BACKDROP);
        } else if (card.getTier() == MiningSkillCardItem.Tier.Mastered) {
            this.banner.show(List.of(Component.translatable("gui.ultimine_addition.skills_record.completed_card").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC)),
                    List.of(), 0xD0302810, true);
        } else if (!hasChallenges) {
            this.banner.show(List.of(Component.translatable("gui.ultimine_addition.skills_record.no_challenges").withStyle(ChatFormatting.GRAY)),
                    List.of(), 0xD0202020, true);
        } else {
            this.banner.setVisible(false);
        }
    }

    boolean isExpanded() {
        return this.expanded;
    }

    boolean isAnimated() {
        return this.state.animated();
    }

    boolean hasTextShadow() {
        return UAClientConfig.TEXT_SCREEN_SHADOW.get();
    }

    Color getAccent() {
        return new Color(this.state.theme().tint(), true);
    }

    int getAccentColor() {
        return this.state.theme().tint();
    }

    RecordTheme getTheme() {
        return this.state.theme();
    }

    // A badge or slot color taken toward the record's color, keeping a hint of its own (a state's green, gold...).
    int recordTinted(int color) {
        int tint = ARGB.srgbLerp(0.3F, ARGB.opaque(this.getAccentColor()), 0xFFFFFFFF);
        return ARGB.multiply(color, tint);
    }

    // A panel shade in the Skills Record's background color (the shade times the color, keeping the shade's alpha).
    int themed(int shade) {
        return ARGB.multiply(shade, ARGB.opaque(this.getAccentColor()));
    }

    ItemStack getSelectedCardStack() {
        return this.state.cardStack();
    }

    MiningSkillCardItem.@Nullable Tier getCurrentTier() {
        return this.state.card() == null ? null : this.state.card().getTier();
    }

    boolean canEdit() {
        var player = this.minecraft.player;
        return UAClientConfig.SR_EDIT_MODE.get() && player != null && player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    void openDetails(ChallengeNode node) {
        if (!this.isAnimated()) {
            this.details.show(node);
            return;
        }
        this.pendingDetails = node;
        this.startTransition(Transition.TO_DETAILS);
    }

    void closeDetails() {
        if (!this.details.isVisible() || this.transition == Transition.TO_TREE) return;
        if (!this.isAnimated()) {
            this.details.setVisible(false);
            return;
        }
        this.startTransition(Transition.TO_TREE);
    }

    private void startTransition(Transition transition) {
        this.transition = transition;
        this.transitionStart = Util.getMillis();
    }

    private void cancelTransition() {
        this.transition = Transition.NONE;
        this.pendingDetails = null;
    }

    boolean isTransitioning() {
        return this.transition != Transition.NONE;
    }

    @Override
    protected void renderContents(GuiGraphicsExtractor graphics, float partialTick, int mouseX, int mouseY) {
        if (this.transition == Transition.NONE) {
            super.renderContents(graphics, partialTick, mouseX, mouseY);
            return;
        }
        long elapsed = Util.getMillis() - this.transitionStart;
        boolean out = elapsed < TRANSITION_OUT_MS;
        float slide = 0.0F, curtain;
        ScreenRectangle r = this.layoutRectangle();
        if (out) {
            float p = Easing.CUBIC_IN.apply(elapsed / (float) TRANSITION_OUT_MS);
            curtain = p;
            if (this.transition == Transition.TO_DETAILS) slide = -p * r.height() * 0.35F;
        } else {
            // Halfway: the panels swap behind the curtain.
            if (this.transition == Transition.TO_DETAILS && this.pendingDetails != null) {
                this.details.show(this.pendingDetails);
                this.pendingDetails = null;
            } else if (this.transition == Transition.TO_TREE && this.details.isVisible()) {
                this.details.setVisible(false);
            }
            float q = Math.min((elapsed - TRANSITION_OUT_MS) / (float) TRANSITION_IN_MS, 1.0F);
            curtain = 1.0F - Easing.CUBIC_OUT.apply(q);
            if (this.transition == Transition.TO_DETAILS) slide = curtain * 16.0F;
            if (q >= 1.0F) this.transition = Transition.NONE;
        }

        graphics.enableScissor(r.left(), r.top(), r.right(), r.bottom());
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(0.0F, slide);
        super.renderContents(graphics, partialTick, -1, -1);
        pose.popMatrix();
        graphics.nextStratum();
        int background = ARGB.multiply(TierTreePanel.BG_DEEP, ARGB.opaque(this.getAccentColor()));
        graphics.fill(r.left(), r.top(), r.right(), r.bottom(), ARGB.color(curtain, background));
        graphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return this.isTransitioning() ? this.isMouseOver(event.x(), event.y()) : super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.isTransitioning() ? this.isMouseOver(mouseX, mouseY) : super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    void togglePin(Identifier challengeId) {
        this.actions.togglePin(challengeId);
    }

    void editChallenge(Identifier challengeId) {
        this.actions.editChallenge(challengeId);
    }

    void rerollChallenge(Identifier challengeId) {
        this.actions.rerollChallenge(challengeId);
    }

    // The Shape Certificate this tier's box hands out, or empty when there's nothing to claim.
    // The shapes this tier's certificate can be picked from; empty when there's nothing to claim.
    List<Identifier> claimPool(TierNode node) {
        MiningSkillCardData card = this.state.card();
        if (card == null || this.minecraft.player == null || !card.hasProgress() || !card.canClaimCertificate(node.tier())) return List.of();
        return ShapeCertificateItem.claimPool(this.minecraft.player, card.getType().getId(), node.tier());
    }

    // The certificate shown on a claimable tier box (its tier and the card's tool), or empty.
    ItemStack claimableCertificate(TierNode node) {
        MiningSkillCardData card = this.state.card();
        ShapeCertificateItem certificate = ShapeCertificateItem.forTier(node.tier());
        if (card == null || certificate == null || this.claimPool(node).isEmpty()) return ItemStack.EMPTY;
        return certificate.create(card.getType());
    }

    void openShapeChoice(TierNode node) {
        List<Identifier> pool = this.claimPool(node);
        if (!pool.isEmpty()) this.shapeChoice.show(node.tier(), pool, this.claimableCertificate(node));
    }

    boolean hasRoomFor(ItemStack stack) {
        return this.minecraft.player != null && ShapeCertificateItem.hasRoomFor(this.minecraft.player, stack);
    }

    void claimCertificate(MiningSkillCardItem.Tier tier, Identifier shape) {
        this.actions.claimCertificate(tier, shape);
    }

    List<FormattedCharSequence> describeCertificate(TierNode node, ItemStack reward) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("✦ ").append(Component.translatable("gui.ultimine_addition.card_viewer.certificate.ready")).withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("gui.ultimine_addition.card_viewer.certificate.choices", this.claimPool(node).size()).withStyle(ChatFormatting.YELLOW));
        lines.add(Component.literal(" "));
        lines.add(this.hasRoomFor(reward)
                ? Component.translatable("gui.ultimine_addition.card_viewer.certificate.click").withStyle(ChatFormatting.GREEN)
                : Component.translatable("gui.ultimine_addition.card_viewer.certificate.no_space").withStyle(ChatFormatting.RED));
        return lines.stream().map(Component::getVisualOrderText).toList();
    }

    // Null when the challenge can't be rerolled at all; otherwise the reason it's blocked (or null reason when allowed).
    @Nullable RerollInfo rerollInfo(ChallengeNode node) {
        MiningSkillCardData card = this.state.card();
        if (card == null || node.id() == null || UAServerConfig.REROLLS_PER_TIER.get() <= 0) return null;
        var challenge = card.getChallenge(node.id());
        if (challenge.isEmpty() || challenge.get().getCurrentPoints() > 0 || card.isCreativeItem()) return null;

        int left = card.getRerollsLeft();
        boolean creative = this.minecraft.player != null && this.minecraft.player.isCreative();
        int cost = creative ? 0 : UAServerConfig.REROLL_INK_COST.get();
        Component blocked = null;
        if (left <= 0) blocked = Component.translatable("gui.ultimine_addition.card_viewer.reroll.used").withStyle(ChatFormatting.RED);
        else if (this.actions.getInkAmount() < cost) blocked = Component.translatable("gui.ultimine_addition.card_viewer.reroll.no_ink", cost).withStyle(ChatFormatting.RED);
        return new RerollInfo(left, cost, blocked);
    }

    record RerollInfo(int left, int cost, @Nullable Component blocked) {}

    static final float MIN_TEXT_SCALE = 0.65F;

    static void drawFittedText(GuiGraphicsExtractor graphics, Font font, Component text, IntFunction<Component> shorten,
                               int x, int y, int maxWidth, int color, boolean shadow) {
        int width = font.width(text);
        if (width <= maxWidth) {
            graphics.text(font, text, x, y, color, shadow);
            return;
        }
        float scale = Math.max(MIN_TEXT_SCALE, maxWidth / (float) width);
        if (width * scale > maxWidth) text = shorten.apply((int) (maxWidth / scale));
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y + font.lineHeight * (1.0F - scale) / 2.0F);
        pose.scale(scale, scale);
        graphics.text(font, text, 0, 0, color, shadow);
        pose.popMatrix();
    }

    static Component ellipsize(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return Component.literal(text);
        return Component.literal(font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width("..."))) + "...");
    }

    static Component challengeName(ChallengeNode node) {
        if (node.id() != null) {
            String key = "challenge.%s.%s.name".formatted(node.id().getNamespace(), node.id().getPath().replace('/', '.'));
            if (Language.getInstance().has(key)) return Component.translatable(key);
        }
        return Component.translatable("challenge.ultimine_addition.title", node.order());
    }

    List<FormattedCharSequence> describeChallenge(ChallengeNode node) {
        List<FormattedCharSequence> header = new ArrayList<>();
        header.add(Component.translatable("challenge.ultimine_addition.title", node.order()).withStyle(ChatFormatting.GRAY).getVisualOrderText());
        header.addAll(this.font.split(Component.literal("\ud83d\udcdd ").append(challengeName(node)).withStyle(Style.EMPTY.withColor(0xFBF1C1)), 200));
        if (node.pinned()) {
            header.add(Component.literal("◎ ").append(Component.translatable("gui.ultimine_addition.card_viewer.pinned")).withStyle(ChatFormatting.YELLOW).getVisualOrderText());
        }

        List<Component> lines = new ArrayList<>(this.challengeStatus(node));
        lines.add(Component.translatable("gui.ultimine_addition.card_viewer.click_for_details").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        List<FormattedCharSequence> body = new ArrayList<>();
        for (Component line : lines) {
            body.addAll(line.getString().isEmpty() ? List.of(FormattedCharSequence.EMPTY) : this.font.split(line, 200));
        }

        // Struck-through spaces draw a clean line.
        int width = 0;
        for (FormattedCharSequence line : header) width = Math.max(width, this.font.width(line));
        for (FormattedCharSequence line : body) width = Math.max(width, this.font.width(line));
        int spaces = Math.max(1, (width + this.font.width(" ") - 1) / this.font.width(" "));
        FormattedCharSequence divider = Component.literal(" ".repeat(spaces)).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.STRIKETHROUGH).getVisualOrderText();

        List<FormattedCharSequence> result = new ArrayList<>(header);
        result.add(divider);
        result.addAll(body);
        return result;
    }

    List<Component> challengeDescription(ChallengeNode node, boolean withBlockList) {
        if (node.id() == null) return List.of();
        List<Component> lines = ChallengesManager.INSTANCE.createChallengeDescription(node.id(), Style.EMPTY.withColor(ChatFormatting.GRAY)).stream()
                .map(c -> (Component) c.copy().withStyle(ChatFormatting.GRAY))
                .toList();
        return withBlockList || lines.isEmpty() ? lines : List.of(lines.getFirst());
    }

    Component challengeProgress(ChallengeNode node) {
        ChatFormatting color = switch (node.state()) {
            case COMPLETED -> ChatFormatting.GREEN;
            case IN_PROGRESS -> ChatFormatting.YELLOW;
            case NEEDS_CONSUME_MODE -> ChatFormatting.RED;
            default -> ChatFormatting.GRAY;
        };
        return Component.translatable("gui.ultimine_addition.card_viewer.progress",
                Component.literal(node.currentPoints() + "/" + node.requiredPoints()).withStyle(color)).withStyle(ChatFormatting.GRAY);
    }

    List<Component> challengeStatus(ChallengeNode node) {
        List<Component> lines = new ArrayList<>();
        if (node.state() == ChallengeState.NEEDS_CONSUME_MODE) {
            lines.add(Component.translatable("challenge.ultimine_addition.consume.info").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        }
        if (node.state() == ChallengeState.COMPLETED) {
            lines.add(node.completedAt().isPresent()
                    ? Component.translatable("gui.ultimine_addition.card_viewer.completed_at", formatTime(node.completedAt().getAsLong())).withStyle(ChatFormatting.DARK_GREEN)
                    : Component.translatable("gui.ultimine_addition.card_viewer.completed").withStyle(ChatFormatting.DARK_GREEN));
        }
        return lines;
    }

    static String formatTime(long epochMillis) {
        return TIME_FORMAT.format(Instant.ofEpochMilli(epochMillis));
    }

    public boolean isTreeShown() {
        return this.tree.isVisible() && !this.isTransitioning() && !this.details.isVisible() && !this.shapeChoice.isVisible() && !this.preview.isVisible();
    }

    public double getZoom() {
        return this.tree.getZoom();
    }

    public double getZoomProgress() {
        return this.tree.getZoomProgress();
    }

    public void setZoomProgress(double progress) {
        this.tree.setZoomProgress(progress);
    }

    public boolean isExpandedWindow() {
        return this.expanded;
    }

    // The screen keeps its viewer across re-inits (resize, edit dialog), so only its place changes.
    public void setCompactBounds(int x, int y, int width, int height) {
        this.compactBounds = new ScreenRectangle(x, y, width, height);
        this.layout();
    }

    private void layout() {
        if (this.expanded) {
            int screenW = this.minecraft.getWindow().getGuiScaledWidth(), screenH = this.minecraft.getWindow().getGuiScaledHeight();
            int w = Math.min(screenW - 20, 420), h = Math.min(screenH - 20, 280);
            this.setBounds((screenW - w) / 2, (screenH - h) / 2, w, h);
        } else {
            ScreenRectangle c = this.compactBounds;
            this.setBounds(c.left(), c.top(), c.width(), c.height());
        }
    }

    public void setExpanded(boolean expanded) {
        if (this.expanded == expanded) return;
        this.expanded = expanded;
        this.layout();
        MiningSkillCardItem.Tier tier = this.getCurrentTier();
        if (tier != null) this.tree.focus(tier);
        this.actions.onExpandedChanged(expanded);
    }

    @Override
    protected void updateChildren() {
        super.updateChildren();
        ScreenRectangle r = this.layoutRectangle();
        if (this.expanded) {
            this.expandButton.setPosition(r.right() - ICON_SIZE, this.y + 5);
            this.fitButton.setPosition(r.right() - ICON_SIZE * 2 - 2, this.y + 5);
        } else {
            this.expandButton.setPosition(r.right() - ICON_SIZE - 1, r.top() + 1);
            this.fitButton.setPosition(r.right() - ICON_SIZE * 2 - 3, r.top() + 1);
        }
        boolean covered = !this.expanded && (this.isTransitioning() || this.details.isVisible() || this.shapeChoice.isVisible() || this.preview.isVisible());
        boolean treeControls = this.tree.isVisible() && !covered;
        this.expandButton.setShown(this.visible && !covered && (treeControls || this.expanded), this.isAnimated());
        this.fitButton.setShown(this.visible && treeControls, this.isAnimated());
    }

    @Override
    protected void renderBackground(GuiGraphicsExtractor graphics, float partialTick, int mouseX, int mouseY) {
        if (!this.expanded) return;
        // Above the book's slots and items.
        graphics.nextStratum();
        int tint = this.getAccentColor();
        int x0 = this.x, y0 = this.y, x1 = this.x + this.width, y1 = this.y + this.height;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FRAME_SPRITE, x0, y0, this.width, this.height, tint);

        if (!this.state.cardStack().isEmpty()) {
            int maxWidth = this.width - 24 - ICON_SIZE * 2 - 8;
            String text = this.state.cardStack().getHoverName().getString();
            if (this.font.width(text) > maxWidth) text = this.font.plainSubstrByWidth(text, maxWidth - this.font.width("...")) + "...";
            int px0 = x0 + 7, py0 = y0 + 4, px1 = px0 + this.font.width(text) + 8, py1 = y0 + 16;
            graphics.fill(px0, py0, px1, py1, ARGB.multiply(0xFFFFFFFF, tint));
            graphics.fill(px0, py0, px1 - 1, py1 - 1, ARGB.multiply(0xFF373737, tint));
            graphics.fill(px0 + 1, py0 + 1, px1 - 1, py1 - 1, ARGB.multiply(0xFF404040, tint));
            graphics.text(this.font, text, px0 + 4, py0 + 2, 0xFFFFFFFF, true);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.isTransitioning()) return event.key() != GLFW.GLFW_KEY_ESCAPE || this.details.isVisible();
        if (this.details.isVisible() && event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.closeDetails();
            return true;
        }
        if (this.expanded && event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.setExpanded(false);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public @NotNull ScreenRectangle getDraggingRectangle() {
        return ScreenRectangle.empty();
    }

    @Override
    protected @NotNull ScreenRectangle layoutRectangle() {
        if (this.expanded) {
            return new ScreenRectangle(this.x + 9, this.y + 20, this.width - 18, this.height - 28);
        }
        return new ScreenRectangle(this.x, this.y, this.width, this.height);
    }

}
