package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.minecraft.util.Util;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.util.ARGB;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.ChatFormatting;
import net.ixdarklord.coolcatcanvas.api.client.gui.components.widgets.panel.ScrollPanel;
import net.ixdarklord.coolcatcanvas.api.client.utils.RenderUtils;
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
    private static final float TIER_SCALE = 0.75F;
    private static final int FOOTER = 17;
    private static final int BUTTON_GAP = 3;
    private static final int LINE_HEIGHT = 10;
    private static final int ICON_SIZE = TierTreePanel.SMALL_SLOT_SIZE + 2;
    private static final int SECTION_GAP = 4;

    private final CardViewerWidget viewer;
    private final ViewerButton backButton;
    private final ViewerButton pinButton;
    private final ViewerButton editButton;
    private final ViewerButton rerollButton;
    private String rerollTooltip = "";
    private @Nullable ChallengeNode node;
    private List<FormattedCharSequence> description = List.of();
    private List<FormattedCharSequence> progress = List.of();
    private List<FormattedCharSequence> status = List.of();
    private int wrappedWidth = -1;
    private @Nullable ItemStack hoveredTarget;

    // Shades of the Skills Record's background color (the viewer's background uses the same card-paper tones).
    static final int PANEL_FILL = 0xF02A2721;
    static final int FOOTER_FILL = 0xFF3A362E;
    static final int FOOTER_EDGE = 0xFF100F0C;
    private static final int SLOT_FILL = 0xFF4A463C;
    // The footer buttons' labels are 6px tall instead of 8px.
    private static final float FOOTER_TEXT_SCALE = 0.75F;
    // The footer buttons' icons, in front of their labels.
    private static final String ICON_BACK = "◀", ICON_PIN = "◎", ICON_UNPIN = "✕", ICON_EDIT = "✎";
    // Drawn from the mod's icons font (a pixel glyph), as the text font has no clear reroll symbol.
    private static final FontDescription ICON_FONT = new FontDescription.Resource(FTBUltimineAddition.id("icons"));
    private static final String ICON_REROLL = "";

    private static Component withIcon(String icon, Component label) {
        return Component.literal(icon + " ").append(label);
    }

    private static Component withFontIcon(String glyph, Component label) {
        return Component.empty().append(Component.literal(glyph).withStyle(style -> style.withFont(ICON_FONT))).append(" ").append(label);
    }

    ChallengeDetailsPanel(CardViewerWidget viewer) {
        this.viewer = viewer;
        this.setModal(true);
        this.setVisible(false);
        this.setScrollStep(LINE_HEIGHT * 2);
        this.backButton = this.addChild(new ViewerButton(36, withIcon(ICON_BACK, Component.translatable("gui.back")), b -> viewer.closeDetails()).withTextScale(FOOTER_TEXT_SCALE));
        this.pinButton = this.addChild(new ViewerButton(36, withIcon(ICON_PIN, Component.translatable("gui.ultimine_addition.card_viewer.pin")), b -> {
            if (this.node != null && this.node.id() != null) viewer.togglePin(this.node.id());
        }).withTextScale(FOOTER_TEXT_SCALE));
        this.editButton = this.addChild(new ViewerButton(36, withIcon(ICON_EDIT, Component.translatable("gui.ultimine_addition.card_viewer.edit")), b -> {
            if (this.node != null && this.node.id() != null) viewer.editChallenge(this.node.id());
        }).withTextScale(FOOTER_TEXT_SCALE));
        this.rerollButton = this.addChild(new ViewerButton(40, withFontIcon(ICON_REROLL, Component.translatable("gui.ultimine_addition.card_viewer.reroll")), b -> {
            if (this.node != null && this.node.id() != null) viewer.rerollChallenge(this.node.id());
        }).withTextScale(FOOTER_TEXT_SCALE));
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
        // The visible buttons as one row, centered in the footer.
        List<ViewerButton> row = new ArrayList<>();
        for (ViewerButton button : List.of(this.rerollButton, this.pinButton, this.editButton, this.backButton)) {
            if (button.visible) row.add(button);
        }
        // Each as wide as its icon and label (the label shrinks further if the row gets too wide).
        for (ViewerButton button : row) {
            button.setWidth(Math.max(28, Math.round(this.font.width(button.getMessage()) * FOOTER_TEXT_SCALE) + 8));
        }
        int width = -BUTTON_GAP;
        for (ViewerButton button : row) width += button.getWidth() + BUTTON_GAP;
        int x = b.left() + (b.width() - width) / 2;
        for (ViewerButton button : row) {
            button.setPosition(x, y);
            x += button.getWidth() + BUTTON_GAP;
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
        graphics.fill(b.left(), b.top(), b.right(), b.bottom(), this.viewer.themed(PANEL_FILL));
        // The scrollbar in the record's color, on a track of a dark shade of it.
        this.setScrollbarColors(ARGB.opaque(this.viewer.getAccentColor()), ARGB.color(0.5F, ARGB.scaleRGB(ARGB.opaque(this.viewer.getAccentColor()), 0.3F)));
        if (this.node == null) return;

        // The challenge's name, then its tier small and muted beside it, on the name's baseline.
        boolean shadow = this.viewer.hasTextShadow();
        Component tier = this.node.tier().getDisplayName();
        int tierWidth = Math.round(this.font.width(tier) * TIER_SCALE);
        String name = CardViewerWidget.challengeName(this.node).getString();
        int nameX = b.left() + PADDING, nameY = b.top() + 4;
        // The challenge's icon in front: its target block, cycling like in the challenge's row.
        if (!this.node.targets().isEmpty()) {
            ItemStack icon = this.node.targets().get((int) (Util.getMillis() / 1000L % this.node.targets().size()));
            var iconPose = graphics.pose();
            iconPose.pushMatrix();
            iconPose.translate(nameX, nameY - 1);
            iconPose.scale(0.625F, 0.625F);
            graphics.item(icon, 0, 0);
            iconPose.popMatrix();
            nameX += 12;
        }
        int room = b.right() - PADDING - nameX - tierWidth - 4;
        Component shown = CardViewerWidget.ellipsize(this.font, name, room);
        graphics.text(this.font, shown, nameX, nameY, RenderUtils.textColor(0xFBF1C1), shadow);
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(nameX + this.font.width(shown) + 4, nameY + 8 - 8 * TIER_SCALE);
        pose.scale(TIER_SCALE, TIER_SCALE);
        graphics.text(this.font, tier, 0, 0, RenderUtils.textColor(0x9A9A9A), shadow);
        pose.popMatrix();
        graphics.fill(b.left() + 2, b.top() + DIVIDER_Y, b.right() - 2, b.top() + DIVIDER_Y + 1, 0x30FFFFFF);

        int footerTop = b.bottom() - FOOTER;
        graphics.fill(b.left(), footerTop, b.right(), b.bottom(), this.viewer.themed(FOOTER_FILL));
        graphics.fill(b.left(), footerTop, b.right(), footerTop + 1, this.viewer.themed(FOOTER_EDGE));
        graphics.fill(b.left(), footerTop + 1, b.right(), footerTop + 2, 0x28FFFFFF);

        boolean live = this.node.isLive() && this.node.tier() == this.viewer.getCurrentTier();
        this.pinButton.visible = live;
        this.pinButton.setMessage(this.node.pinned() ? withIcon(ICON_UNPIN, Component.translatable("gui.ultimine_addition.card_viewer.unpin"))
                : withIcon(ICON_PIN, Component.translatable("gui.ultimine_addition.card_viewer.pin")));
        this.editButton.visible = live && this.viewer.canEdit();
        this.updateRerollButton(live);
        this.layoutButtons();
    }

    private void updateRerollButton(boolean live) {
        CardViewerWidget.RerollInfo info = live && this.node != null ? this.viewer.rerollInfo(this.node) : null;
        this.rerollButton.visible = info != null;
        if (info == null) return;
        this.rerollButton.active = info.blocked() == null;

        Component tooltip = Component.translatable("gui.ultimine_addition.card_viewer.reroll.info")
                .append("\n").append(Component.translatable("gui.ultimine_addition.card_viewer.reroll.cost", info.cost(), info.left()).withStyle(ChatFormatting.GRAY));
        if (info.blocked() != null) tooltip = tooltip.copy().append("\n").append(info.blocked());
        String key = tooltip.getString();
        if (!key.equals(this.rerollTooltip)) {
            this.rerollTooltip = key;
            this.rerollButton.setTooltip(Tooltip.create(tooltip));
        }
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
                TierTreePanel.drawSmallSlot(graphics, x, iy, this.viewer.themed(SLOT_FILL), this.viewer.recordTinted(hovered ? 0xFFFFFFFF : 0xFF9A9A9A));
                graphics.item(targets.get(i), x + 2, iy + 2);
            }
            y += this.iconsHeight(this.iconRows(width)) + SECTION_GAP;
        }

        y = this.drawLines(graphics, this.progress, left, y, shadow);
        TierTreePanel.drawProgressBar(graphics, left, y, width, this.node.progress(), this.viewer.getAccentColor());
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
            this.viewer.closeDetails();
            return true;
        }
        return false;
    }
}
