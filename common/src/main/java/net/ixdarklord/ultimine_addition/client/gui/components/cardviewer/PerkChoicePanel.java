package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.coolcatcanvas.api.client.gui.components.widgets.panel.Panel;
import net.ixdarklord.coolcatcanvas.api.client.utils.RenderUtils;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.progression.CardPerk;
import net.ixdarklord.ultimine_addition.util.ARGB;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

// Picking a tier's perk: the two the card offers, side by side. Each is a tile with the perk's icon and name; hovering
// one says what it does. The one not picked may be offered again by a later tier or another card of the same tool.
final class PerkChoicePanel extends Panel {
    private static final int PADDING = 4;
    private static final int HEADER = 25;
    private static final int FOOTER = 17;
    private static final int GAP = 4;
    private static final int MAX_TILE_WIDTH = 78;
    private static final int GOLD = 0xFFFBD25A;
    private static final int ACCENT = 0xFFB98CFF;

    private final CardViewerWidget viewer;
    private final ViewerButton backButton;
    private final List<PerkTile> choices = new ArrayList<>();
    private MiningSkillCardItem.@Nullable Tier tier;

    PerkChoicePanel(CardViewerWidget viewer) {
        this.viewer = viewer;
        this.setModal(true);
        this.setVisible(false);
        this.backButton = this.addChild(new ViewerButton(36, Component.translatable("gui.back"), b -> this.setVisible(false)));
    }

    void show(MiningSkillCardItem.Tier tier, List<CardPerk> offer) {
        this.choices.forEach(this::removeChild);
        this.choices.clear();
        this.tier = tier;
        for (CardPerk perk : offer) {
            PerkTile tile = this.addChild(new PerkTile(perk, () -> {
                if (this.tier != null) this.viewer.claimPerk(this.tier, perk);
                this.setVisible(false);
            }));
            tile.setTooltip(Tooltip.create(perk.displayName().withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append("\n").append(perk.description().withStyle(ChatFormatting.GRAY))
                    .append("\n\n").append(Component.translatable("gui.ultimine_addition.card_viewer.perk.pick").withStyle(ChatFormatting.GREEN))));
            this.choices.add(tile);
        }
        this.setVisible(true);
        this.layout();
    }

    @Override
    protected void onResized() {
        this.layout();
    }

    private void layout() {
        ScreenRectangle b = this.getBounds();
        int count = Math.max(1, this.choices.size());
        int top = b.top() + HEADER + 1;
        int height = Math.max(12, b.bottom() - FOOTER - 3 - top);
        int space = Math.max(40, b.width() - PADDING * 2);
        int tileWidth = Math.max(20, Math.min(MAX_TILE_WIDTH, (space - GAP * (count - 1)) / count));
        int x = b.left() + (b.width() - (tileWidth * count + GAP * (count - 1))) / 2;
        for (PerkTile tile : this.choices) {
            tile.place(tileWidth, height, x, top);
            x += tileWidth + GAP;
        }
        this.backButton.setPosition(b.left() + (b.width() - this.backButton.getWidth()) / 2, b.bottom() - FOOTER + (FOOTER + 2 - 12) / 2);
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ScreenRectangle b = this.getBounds();
        graphics.fill(b.left(), b.top(), b.right(), b.bottom(), this.viewer.themed(ChallengeDetailsPanel.PANEL_FILL));
        if (this.tier == null) return;
        boolean shadow = this.viewer.hasTextShadow();

        // The header, as the shape choice has it: a darker band closed by a line with a gold stud at its middle.
        int line = b.top() + HEADER - 3;
        graphics.fill(b.left(), b.top(), b.right(), line, 0x50000000);
        graphics.fill(b.left(), line, b.right(), line + 1, ARGB.multiplyAlpha(ACCENT, 0.8F));
        graphics.fill(b.left(), line + 1, b.right(), line + 2, 0x40000000);
        int middle = b.left() + b.width() / 2;
        graphics.fill(middle - 2, line - 1, middle + 2, line + 2, GOLD);
        graphics.fill(middle - 1, line - 2, middle + 1, line + 3, GOLD);
        int textX = b.left() + PADDING + 1;
        Component title = Component.translatable("gui.ultimine_addition.card_viewer.perk.choose").append(Component.literal(" · ")).append(this.tier.getDisplayName());
        CardViewerWidget.drawFittedText(graphics, this.font, title, width -> title, textX, b.top() + 3, b.right() - PADDING - textX,
                RenderUtils.textColor(0xFBF1C1), shadow);
        Component sub = Component.translatable("gui.ultimine_addition.card_viewer.perk.kept").withStyle(ChatFormatting.GRAY);
        CardViewerWidget.drawFittedText(graphics, this.font, sub, width -> sub, textX, b.top() + 12, b.right() - PADDING - textX, 0xFFFFFFFF, shadow);

        int footerTop = b.bottom() - FOOTER;
        graphics.fill(b.left(), footerTop, b.right(), b.bottom(), this.viewer.themed(ChallengeDetailsPanel.FOOTER_FILL));
        graphics.fill(b.left(), footerTop, b.right(), footerTop + 1, this.viewer.themed(ChallengeDetailsPanel.FOOTER_EDGE));
        graphics.fill(b.left(), footerTop + 1, b.right(), footerTop + 2, 0x28FFFFFF);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.setVisible(false);
            return true;
        }
        return false;
    }

    // One perk on offer: its icon on the left, its name beside it on up to two lines.
    private static final class PerkTile extends AbstractButton {
        private final CardPerk perk;
        private final Runnable onPress;

        PerkTile(CardPerk perk, Runnable onPress) {
            super(0, 0, 40, 40, perk.displayName());
            this.perk = perk;
            this.onPress = onPress;
        }

        void place(int width, int height, int x, int y) {
            this.width = width;
            this.height = height;
            this.setPosition(x, y);
        }

        @Override
        public void onPress() {
            this.onPress.run();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.active && this.isHoveredOrFocused();
            // A hovered tile lifts by a pixel.
            int x = this.getX(), y = this.getY() - (hovered ? 1 : 0), width = this.getWidth(), height = this.getHeight();
            int edge = hovered ? GOLD : ARGB.multiplyAlpha(ACCENT, 0.55F);
            graphics.fill(x + 1, y + 2, x + width + 1, y + height + 1, 0x50000000);
            graphics.fill(x, y, x + width, y + height, hovered ? 0xF0303038 : 0xE01C1C22);
            graphics.fill(x + 1, y + 1, x + width - 1, y + 3, ARGB.multiplyAlpha(ACCENT, hovered ? 1.0F : 0.75F));
            graphics.fill(x, y, x + width, y + 1, edge);
            graphics.fill(x, y + height - 1, x + width, y + height, edge);
            graphics.fill(x, y, x + 1, y + height, edge);
            graphics.fill(x + width - 1, y, x + width, y + height, edge);

            int iconY = y + 3 + (height - 3 - 16) / 2;
            graphics.fill(x + 3, iconY - 1, x + 21, iconY + 17, 0x60000000);
            graphics.renderItem(this.perk.icon(), x + 4, iconY);

            Font font = Minecraft.getInstance().font;
            int textX = x + 24, room = Math.max(8, width - 27);
            List<FormattedCharSequence> lines = font.split(this.getMessage(), room);
            if (lines.size() > 2) lines = lines.subList(0, 2);
            int textY = y + 3 + (height - 3 - lines.size() * 9) / 2;
            for (FormattedCharSequence text : lines) {
                graphics.drawString(font, text, textX, textY, hovered ? GOLD : 0xFFFFFFFF, true);
                textY += 9;
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
