package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.coolcatlib.api.client.gui.components.widgets.panel.ScrollPanel;
import net.ixdarklord.coolcatlib.api.client.utils.RenderUtils;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardTree.ChallengeNode;
import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.CardTree.TierNode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class ChallengeDetailsPanel extends ScrollPanel {
    private static final int PADDING = 4;
    private static final int HEADER = 18;
    private static final int DIVIDER_Y = 14;
    private static final int FOOTER = 17;
    private static final int LINE_HEIGHT = 10;
    private static final int ICON_SIZE = TierTreePanel.SMALL_SLOT_SIZE + 2;
    private static final int SECTION_GAP = 4;

    private final CardViewerWidget viewer;
    private final ViewerButton backButton;
    private final ViewerButton pinButton;
    private final ViewerButton editButton;
    private @Nullable ChallengeNode node;
    private List<FormattedCharSequence> description = List.of();
    private List<FormattedCharSequence> progress = List.of();
    private List<FormattedCharSequence> status = List.of();
    private int wrappedWidth = -1;
    private @Nullable ItemStack hoveredTarget;

    ChallengeDetailsPanel(CardViewerWidget viewer) {
        this.viewer = viewer;
        this.setModal(true);
        this.setVisible(false);
        this.setScrollStep(LINE_HEIGHT * 2);
        this.backButton = this.addChild(new ViewerButton(36, Component.translatable("gui.back"), b -> this.setVisible(false)));
        this.pinButton = this.addChild(new ViewerButton(36, Component.translatable("gui.ultimine_addition.card_viewer.pin"), b -> {
            if (this.node != null && this.node.id() != null) viewer.togglePin(this.node.id());
        }));
        this.editButton = this.addChild(new ViewerButton(36, Component.translatable("gui.ultimine_addition.card_viewer.edit"), b -> {
            if (this.node != null && this.node.id() != null) viewer.editChallenge(this.node.id());
        }));
    }

    void show(ChallengeNode node) {
        this.node = node;
        this.wrappedWidth = -1;
        this.setScroll(0);
        this.setVisible(true);
    }

    void refresh(@Nullable CardTree tree) {
        if (this.node == null || !this.isVisible()) return;
        ChallengeNode updated = null;
        if (tree != null) {
            TierNode tier = tree.getTier(this.node.tier());
            for (ChallengeNode candidate : tier.challenges()) {
                if (Objects.equals(candidate.id(), this.node.id())) updated = candidate;
            }
        }
        if (updated == null) {
            this.setVisible(false);
        } else if (!updated.equals(this.node)) {
            this.node = updated;
            this.wrappedWidth = -1;
        }
    }

    @Override
    protected void onResized() {
        this.layoutButtons();
        this.wrappedWidth = -1;
    }

    private void layoutButtons() {
        ScreenRectangle b = this.getBounds();
        int y = b.bottom() - FOOTER + (FOOTER + 2 - 12) / 2;
        int x = b.right() - PADDING;
        for (ViewerButton button : List.of(this.backButton, this.editButton, this.pinButton)) {
            if (!button.visible) continue;
            x -= button.getWidth();
            button.setPosition(x, y);
            x -= 3;
        }
    }

    @Override
    protected ScreenRectangle getScrollArea() {
        ScreenRectangle b = this.getBounds();
        return new ScreenRectangle(b.left() + PADDING, b.top() + HEADER, Math.max(0, b.width() - PADDING * 2), Math.max(0, b.height() - HEADER - FOOTER));
    }

    private void rewrap() {
        int width = this.getContentWidth();
        if (this.node == null || width == this.wrappedWidth) return;
        this.wrappedWidth = width;
        this.description = this.wrap(this.viewer.challengeDescription(this.node, false), width);
        this.progress = this.wrap(List.of(this.viewer.challengeProgress(this.node)), width);
        this.status = this.wrap(this.viewer.challengeStatus(this.node), width);
    }

    private List<FormattedCharSequence> wrap(List<Component> lines, int width) {
        List<FormattedCharSequence> result = new ArrayList<>();
        for (Component line : lines) result.addAll(this.font.split(line, width));
        return result;
    }

    private int iconsHeight(int rows) {
        return rows <= 0 ? 0 : rows * ICON_SIZE - (ICON_SIZE - TierTreePanel.SMALL_SLOT_SIZE);
    }

    private int iconRows(int width) {
        if (this.node == null || this.node.targets().isEmpty()) return 0;
        int perRow = Math.max(1, width / ICON_SIZE);
        return (this.node.targets().size() + perRow - 1) / perRow;
    }

    @Override
    protected int getContentHeight() {
        this.rewrap();
        int rows = this.iconRows(this.getContentWidth());
        return this.description.size() * LINE_HEIGHT
                + this.iconsHeight(rows)
                + (rows > 0 ? SECTION_GAP : 0) + this.progress.size() * LINE_HEIGHT
                + TierTreePanel.PROGRESS_BAR_HEIGHT
                + (this.status.isEmpty() ? 0 : SECTION_GAP + this.status.size() * LINE_HEIGHT);
    }

    @Override
    protected void extractFrame(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ScreenRectangle b = this.getBounds();
        graphics.fill(b.left(), b.top(), b.right(), b.bottom(), 0xF0141414);
        if (this.node == null) return;

        Component tierPart = Component.literal(" · ").append(this.node.tier().getDisplayName()).append("《");
        String name = CardViewerWidget.challengeName(this.node).getString();
        Component title = Component.literal("》").append(name).append(tierPart);
        CardViewerWidget.drawFittedText(graphics, this.font, title,
                width -> Component.literal("》").append(CardViewerWidget.ellipsize(this.font, name, width - this.font.width("》") - this.font.width(tierPart))).append(tierPart),
                b.left() + PADDING, b.top() + 4, b.width() - PADDING * 2, RenderUtils.textColor(0xFBF1C1), this.viewer.hasTextShadow());
        graphics.fill(b.left() + 2, b.top() + DIVIDER_Y, b.right() - 2, b.top() + DIVIDER_Y + 1, 0x30FFFFFF);

        int footerTop = b.bottom() - FOOTER;
        graphics.fill(b.left(), footerTop, b.right(), b.bottom(), 0xFF202020);
        graphics.fill(b.left(), footerTop, b.right(), footerTop + 1, 0xFF0A0A0A);
        graphics.fill(b.left(), footerTop + 1, b.right(), footerTop + 2, 0x28FFFFFF);

        boolean live = this.node.isLive() && this.node.tier() == this.viewer.getCurrentTier();
        this.pinButton.visible = live;
        this.pinButton.setMessage(Component.translatable(this.node.pinned() ? "gui.ultimine_addition.card_viewer.unpin" : "gui.ultimine_addition.card_viewer.pin"));
        this.editButton.visible = live && this.viewer.canEdit();
        this.layoutButtons();
    }

    @Override
    protected void extractScrolled(GuiGraphicsExtractor graphics, int left, int top, int width, int mouseX, int mouseY, float partialTick) {
        this.hoveredTarget = null;
        if (this.node == null) return;
        boolean shadow = this.viewer.hasTextShadow();
        int y = this.drawLines(graphics, this.description, left, top, shadow);

        List<ItemStack> targets = this.node.targets();
        if (!targets.isEmpty()) {
            int perRow = Math.max(1, width / ICON_SIZE);
            for (int i = 0; i < targets.size(); i++) {
                int x = left + (i % perRow) * ICON_SIZE;
                int iy = y + (i / perRow) * ICON_SIZE;
                boolean hovered = this.isInScrollArea(mouseX, mouseY) && mouseX >= x && mouseX < x + TierTreePanel.SMALL_SLOT_SIZE && mouseY >= iy && mouseY < iy + TierTreePanel.SMALL_SLOT_SIZE;
                if (hovered) this.hoveredTarget = targets.get(i);
                TierTreePanel.drawSmallSlot(graphics, x, iy, 0xFF3A3A3A, hovered ? 0xFFFFFFFF : 0xFF9A9A9A);
                graphics.item(targets.get(i), x + 2, iy + 2);
            }
            y += this.iconsHeight(this.iconRows(width)) + SECTION_GAP;
        }

        y = this.drawLines(graphics, this.progress, left, y, shadow);
        TierTreePanel.drawProgressBar(graphics, left, y, width, this.node.progress());
        y += TierTreePanel.PROGRESS_BAR_HEIGHT;
        if (!this.status.isEmpty()) this.drawLines(graphics, this.status, left, y + SECTION_GAP, shadow);

        if (this.hoveredTarget != null) {
            graphics.setTooltipForNextFrame(this.font, this.hoveredTarget.getHoverName(), mouseX, mouseY);
        }
    }

    private int drawLines(GuiGraphicsExtractor graphics, List<FormattedCharSequence> lines, int x, int y, boolean shadow) {
        for (FormattedCharSequence line : lines) {
            graphics.text(this.font, line, x, y, RenderUtils.textColor(0xFFFFFF), shadow);
            y += LINE_HEIGHT;
        }
        return y;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_BACKSPACE) {
            this.setVisible(false);
            return true;
        }
        return false;
    }
}
