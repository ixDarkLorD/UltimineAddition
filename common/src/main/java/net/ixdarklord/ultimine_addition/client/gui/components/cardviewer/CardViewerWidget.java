package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.coolcatlib.api.client.gui.components.widgets.AbstractMultiPanelWidget;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardTree.ChallengeNode;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardTree.ChallengeState;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.record.CardHistory;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
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

/**
 * The Skills Record's card viewer: the selected card's tiers and challenges on a pannable, zoomable map,
 * with a details panel per challenge and status messages on top. It sits in the book's text area and can be
 * expanded into a large window centered on the screen.
 * <p>
 * Panels, bottom to top: tier tree, bottom banner (warnings), full message (no card/none selected),
 * challenge details (modal), configuration preview (modal).
 */
public final class CardViewerWidget extends AbstractMultiPanelWidget {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault());
    private static final Identifier FRAME_SPRITE = FTBUltimineAddition.id("container/skills_record/card_viewer/frame");
    private static final int ICON_SIZE = 11;

    /** Everything the viewer shows, provided by the screen every frame. */
    public record State(boolean hasCards, ItemStack cardStack, @Nullable MiningSkillCardData card, @Nullable CardHistory history,
                        boolean consumeMode, List<ItemStack> missingItems, boolean notEnoughInk, boolean preview,
                        Color accent, boolean animated) {}

    /** What the viewer asks the screen to do. */
    public interface Actions {
        void togglePin(Identifier challengeId);

        void editChallenge(Identifier challengeId);

        void onExpandedChanged(boolean expanded);
    }

    private final Actions actions;
    private ScreenRectangle compactBounds;
    private final TierTreePanel tree;
    private final MessagePanel banner;
    private final MessagePanel message;
    private final ChallengeDetailsPanel details;
    private final MessagePanel preview;
    private ViewerButton.Icon expandButton;
    private ViewerButton.Icon fitButton;
    private boolean expanded;

    private State state = new State(false, ItemStack.EMPTY, null, null, false, List.of(), false, false, Color.WHITE, true);
    private @Nullable UUID shownCard;
    private int treeSignature;

    public CardViewerWidget(int x, int y, int width, int height, Actions actions) {
        super(CommonComponents.EMPTY, x, y, width, height, false);
        this.actions = actions;
        this.compactBounds = new ScreenRectangle(x, y, width, height);
        this.tree = this.addPanel(new TierTreePanel(this));
        this.banner = this.addPanel(new MessagePanel(this, MessagePanel.Mode.BANNER));
        this.message = this.addPanel(new MessagePanel(this, MessagePanel.Mode.FULL));
        this.details = this.addPanel(new ChallengeDetailsPanel(this));
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

    // --- State ---

    /** Updates what is shown; called by the screen before rendering. */
    public void update(State state) {
        this.state = state;
        MiningSkillCardData card = state.card();

        if (!state.hasCards() || card == null) {
            this.tree.setVisible(false);
            this.banner.setVisible(false);
            this.details.setVisible(false);
            this.shownCard = null;
            this.message.show(List.of(state.hasCards()
                    ? Component.translatable("gui.ultimine_addition.skills_record.select_card").withStyle(ChatFormatting.GRAY)
                    : Component.translatable("gui.ultimine_addition.skills_record.no_cards").withStyle(ChatFormatting.RED)), List.of(), 0, true);
        } else {
            this.message.setVisible(false);
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
                this.details.setVisible(false);
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
            this.preview.show(lines, List.of(), ARGB.multiply(0xD8202020, ARGB.opaque(this.state.accent().getRGB())), centered);
        } else {
            this.preview.setVisible(false);
        }
    }

    private void updateBanner(MiningSkillCardData card) {
        boolean hasChallenges = !card.getChallenges().isEmpty();
        if (hasChallenges && !this.state.missingItems().isEmpty()) {
            this.banner.show(List.of(Component.translatable("gui.ultimine_addition.skills_record.missing_items").withStyle(ChatFormatting.RED)),
                    this.state.missingItems(), 0xE04B1818, true);
        } else if (hasChallenges && this.state.notEnoughInk()) {
            this.banner.show(List.of(Component.translatable("gui.ultimine_addition.skills_record.not_enough_ink").withStyle(ChatFormatting.RED)),
                    List.of(), 0xE04B1818, true);
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

    // --- Queries used by the panels ---

    boolean isExpanded() {
        return this.expanded;
    }

    boolean isAnimated() {
        return this.state.animated();
    }

    boolean hasTextShadow() {
        return ConfigHandler.CLIENT.TEXT_SCREEN_SHADOW.get();
    }

    Color getAccent() {
        return this.state.accent();
    }

    int getAccentColor() {
        return this.state.accent().getRGB();
    }

    ItemStack getSelectedCardStack() {
        return this.state.cardStack();
    }

    MiningSkillCardItem.@Nullable Tier getCurrentTier() {
        return this.state.card() == null ? null : this.state.card().getTier();
    }

    boolean canEdit() {
        var player = this.minecraft.player;
        return ConfigHandler.CLIENT.SR_EDIT_MODE.get() && player != null && player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    void openDetails(ChallengeNode node) {
        this.details.show(node);
    }

    void togglePin(Identifier challengeId) {
        this.actions.togglePin(challengeId);
    }

    void editChallenge(Identifier challengeId) {
        this.actions.editChallenge(challengeId);
    }

    /** Title, description, progress and dates of a challenge, as tooltip lines. */
    List<FormattedCharSequence> describeChallenge(ChallengeNode node, boolean asTooltip) {
        List<FormattedCharSequence> result = new ArrayList<>();
        for (Component line : this.describeChallengeLines(node, asTooltip)) {
            result.addAll(line.getString().isEmpty() ? List.of(FormattedCharSequence.EMPTY) : this.font.split(line, 200));
        }
        return result;
    }

    /** Tooltip lines: title, full description (with the block list), progress and status. */
    List<Component> describeChallengeLines(ChallengeNode node, boolean asTooltip) {
        List<Component> lines = new ArrayList<>();
        if (asTooltip) {
            lines.add(Component.translatable("challenge.ultimine_addition.title", node.order()).withStyle(Style.EMPTY.withColor(0xFBF1C1)));
        }
        lines.addAll(this.challengeDescription(node, true));
        lines.add(Component.empty());
        lines.add(this.challengeProgress(node));
        lines.addAll(this.challengeStatus(node));
        if (asTooltip) {
            lines.add(Component.translatable("gui.ultimine_addition.card_viewer.click_for_details").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
        return lines;
    }

    /**
     * What the challenge asks for. Without {@code withBlockList}, a challenge on several blocks only gets its
     * first sentence; the details view shows the blocks as items instead.
     */
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

    /** Consume-mode warning, completion date and pin state. */
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
        if (node.pinned()) {
            lines.add(Component.translatable("gui.ultimine_addition.card_viewer.pinned").withStyle(ChatFormatting.YELLOW));
        }
        return lines;
    }

    static String formatTime(long epochMillis) {
        return TIME_FORMAT.format(Instant.ofEpochMilli(epochMillis));
    }

    // --- Zoom slider (the book's scrollbar) ---

    public boolean isTreeShown() {
        return this.tree.isVisible() && !this.details.isVisible() && !this.preview.isVisible();
    }

    public double getZoomProgress() {
        return this.tree.getZoomProgress();
    }

    public void setZoomProgress(double progress) {
        this.tree.setZoomProgress(progress);
    }

    // --- Expanded window ---

    public boolean isExpandedWindow() {
        return this.expanded;
    }

    /** Resizes the in-book viewer (e.g. when the progression bar takes the bottom rows). */
    public void setCompactHeight(int height) {
        if (this.compactBounds.height() == height) return;
        this.compactBounds = new ScreenRectangle(this.compactBounds.left(), this.compactBounds.top(), this.compactBounds.width(), height);
        if (!this.expanded) this.setBounds(this.compactBounds.left(), this.compactBounds.top(), this.compactBounds.width(), height);
    }

    public void setExpanded(boolean expanded) {
        if (this.expanded == expanded) return;
        this.expanded = expanded;
        if (expanded) {
            int screenW = this.minecraft.getWindow().getGuiScaledWidth(), screenH = this.minecraft.getWindow().getGuiScaledHeight();
            int w = Math.min(screenW - 20, 420), h = Math.min(screenH - 20, 280);
            this.setBounds((screenW - w) / 2, (screenH - h) / 2, w, h);
        } else {
            ScreenRectangle c = this.compactBounds;
            this.setBounds(c.left(), c.top(), c.width(), c.height());
        }
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
        // In the book the buttons sit over the viewer, so they hide while a panel covers the tree.
        boolean covered = !this.expanded && (this.details.isVisible() || this.preview.isVisible());
        boolean treeControls = this.tree.isVisible() && !covered;
        this.expandButton.visible = this.expandButton.active = this.visible && !covered && (treeControls || this.expanded);
        this.fitButton.visible = this.fitButton.active = this.visible && treeControls;
    }

    @Override
    protected void renderBackground(GuiGraphicsExtractor graphics, float partialTick, int mouseX, int mouseY) {
        if (!this.expanded) return;
        // Above the book's slots and items.
        graphics.nextStratum();
        int tint = ARGB.opaque(this.state.accent().getRGB());
        int x0 = this.x, y0 = this.y, x1 = this.x + this.width, y1 = this.y + this.height;

        // The book's own frame, header and recessed screen (cut from its background texture), tinted like it.
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FRAME_SPRITE, x0, y0, this.width, this.height, tint);

        // The card's name on a small recessed plate in the header, in white so it stays readable.
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
        if (this.details.isVisible() && event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.details.setVisible(false);
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
        // The viewer can't be dragged, compact or expanded.
        return ScreenRectangle.empty();
    }

    @Override
    protected @NotNull ScreenRectangle layoutRectangle() {
        if (this.expanded) {
            // Inside the frame sprite's screen (9px left/right, 20px header, 8px bottom).
            return new ScreenRectangle(this.x + 9, this.y + 20, this.width - 18, this.height - 28);
        }
        return new ScreenRectangle(this.x, this.y, this.width, this.height);
    }

}
