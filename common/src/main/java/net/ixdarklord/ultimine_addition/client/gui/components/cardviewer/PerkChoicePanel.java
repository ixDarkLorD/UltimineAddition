package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.coolcatcanvas.api.client.gui.components.widgets.panel.Panel;
import net.ixdarklord.ultimine_addition.client.gui.GuiDraw;
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
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

// Picking a tier's perk: the ones the card offers, one under the other. Each is a raised plate with the perk's icon in
// a well, its name, what it does in small print and the tier it belongs to; it lights up and leans in under the mouse,
// and hovering one says the same in full. The one not picked may be
// offered again by a later tier or another card of the same tool.
final class PerkChoicePanel extends Panel {
    private static final int GAP = 1;
    private static final int MAX_ROW_WIDTH = 230;
    private static final int MAX_ROW_HEIGHT = 38;
    private static final float SMALL = 0.75F;
    // The sizes a perk's description is tried at, largest first: the first that fits the row is used.
    private static final float[] DESCRIPTION_SCALES = {0.75F, 0.65F, 0.55F};

    private final CardViewerWidget viewer;
    private final ViewerButton backButton;
    private final List<PerkRow> choices = new ArrayList<>();
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
            PerkRow row = this.addChild(new PerkRow(this.viewer, perk, perk.tier() != tier, () -> {
                if (this.tier != null) this.viewer.claimPerk(this.tier, perk);
                this.setVisible(false);
            }));
            row.setTooltip(Tooltip.create(perk.displayName().withStyle(ChatFormatting.GOLD)
                    .append("\n").append(perk.description().withStyle(ChatFormatting.GRAY))
                    .append("\n\n").append(Component.translatable("gui.ultimine_addition.card_viewer.perk.pick").withStyle(ChatFormatting.GREEN))));
            this.choices.add(row);
        }
        this.setVisible(true);
        this.layout();
    }

    @Override
    protected void onResized() {
        this.layout();
    }

    // The rows share the room between the header and the footer, as a group in its middle.
    private void layout() {
        ScreenRectangle b = this.getBounds();
        int count = Math.max(1, this.choices.size());
        int top = b.top() + ChoiceChrome.HEADER + 1, bottom = b.bottom() - ChoiceChrome.FOOTER - 1;
        int rowHeight = Math.max(12, Math.min(MAX_ROW_HEIGHT, (bottom - top - GAP * (count - 1)) / count));
        int rowWidth = Math.max(40, Math.min(MAX_ROW_WIDTH, b.width() - ChoiceChrome.PADDING * 2));
        int x = b.left() + (b.width() - rowWidth) / 2;
        int y = top + Math.max(0, (bottom - top - (rowHeight * count + GAP * (count - 1))) / 2);
        for (PerkRow row : this.choices) {
            row.setRectangle(rowWidth, rowHeight, x, y);
            y += rowHeight + GAP;
        }
        this.backButton.setPosition(b.left() + (b.width() - this.backButton.getWidth()) / 2, b.bottom() - ChoiceChrome.FOOTER + (ChoiceChrome.FOOTER + 2 - 12) / 2);
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ScreenRectangle b = this.getBounds();
        if (this.tier == null) {
            graphics.fill(b.left(), b.top(), b.right(), b.bottom(), this.viewer.themed(ChallengeDetailsPanel.PANEL_FILL));
            return;
        }
        ChoiceChrome.frame(graphics, this.viewer, this.font, b, net.minecraft.world.item.ItemStack.EMPTY,
                Component.translatable("gui.ultimine_addition.card_viewer.perk.choose"),
                Component.translatable("gui.ultimine_addition.card_viewer.perk.kept").withStyle(ChatFormatting.GRAY), this.tier);
        // Its rows, and panels drawn later, cover what the panels below drew.
        GuiDraw.nextStratum(graphics);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.setVisible(false);
            return true;
        }
        return false;
    }

    // One perk on offer: its icon, its name over what it does, and its tier on the right.
    private static final class PerkRow extends AbstractButton {
        private final CardViewerWidget viewer;
        private final CardPerk perk;
        private final Runnable onPress;
        // Whether the row names the perk's tier: only for one left over from an earlier tier than the panel's.
        private final boolean showTier;
        // How far the row is into its hovered look (0 to 1), and when it was last drawn.
        private float hover;
        private long lastFrame;

        PerkRow(CardViewerWidget viewer, CardPerk perk, boolean showTier, Runnable onPress) {
            super(0, 0, 40, 20, perk.displayName());
            this.viewer = viewer;
            this.showTier = showTier;
            this.perk = perk;
            this.onPress = onPress;
        }

        @Override
        public void onPress() {
            this.onPress.run();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.active && this.isHoveredOrFocused();
            long now = net.minecraft.Util.getMillis();
            float seconds = Math.min(0.1F, (now - this.lastFrame) / 1000.0F);
            this.lastFrame = now;
            this.hover = this.viewer.isAnimated() ? ChoiceChrome.approach(this.hover, hovered ? 1.0F : 0.0F, seconds, 16.0F) : hovered ? 1.0F : 0.0F;

            int x = this.getX(), y = this.getY(), width = this.getWidth(), height = this.getHeight();
            int face = height - ChoiceChrome.LIP;
            int accent = ChoiceChrome.tierColor(this.perk.tier());
            ChoiceChrome.plate(graphics, this.viewer, x, y, width, height, this.hover, this.active);
            // The perk's tier, as a bar down the row's left edge that widens under the mouse.
            graphics.fill(x + 2, y + 3, x + 4 + Math.round(this.hover), y + face - 2, ARGB.srgbLerp(this.hover, ARGB.multiplyAlpha(accent, 0.7F), accent));

            // What is on the row leans a little to the right under the mouse.
            int lean = Math.round(this.hover * 2.0F);
            // The well sits in the middle of the face, with the same room above and below it.
            int wellY = y + (face - 14) / 2;
            // The icon's well is smaller than an item, so the icon is drawn at three quarters of its size in its middle.
            ChoiceChrome.well(graphics, x + 8 + lean, wellY, 15, 15);
            var iconPose = graphics.pose();
            iconPose.pushPose();
            iconPose.translate(x + 8 + lean + 1.5F, wellY + 1.5F, 0.0F);
            iconPose.scale(0.75F, 0.75F, 1.0F);
            graphics.renderItem(this.perk.icon(), 0, 0);
            iconPose.popPose();

            Font font = Minecraft.getInstance().font;
            int textX = x + 29 + lean;
            // The tier's name, small, in the top right corner.
            Component tier = this.perk.tier().getDisplayName();
            int tierWidth = this.showTier ? Math.round(font.width(tier) * SMALL) : 0;
            var pose = graphics.pose();

            // The name, small, and under it what the perk does: split over the lines the row has room for, at the
            // largest size all of it fits at; only when the smallest still doesn't fit is it cut short.
            int room = x + width - 5 - textX;
            // Two rows of the face are kept clear above the text and at least one below it, over the plate's side.
            int below = face - 3 - 5;
            String text = this.perk.description().getString();
            float scale = DESCRIPTION_SCALES[DESCRIPTION_SCALES.length - 1];
            List<String> description = List.of();
            for (float tried : DESCRIPTION_SCALES) {
                int fits = below / Math.round(9 * tried);
                if (fits <= 0) continue;
                List<String> whole = ChoiceChrome.wrap(font, text, (int) (room / tried), 0);
                scale = tried;
                description = whole.size() <= fits ? whole : ChoiceChrome.wrap(font, text, (int) (room / tried), fits);
                if (whole.size() <= fits) break;
            }
            int lineHeight = Math.round(9 * scale);
            int block = 5 + description.size() * lineHeight;
            int textY = y + Math.max(2, (face - block) / 2);
            // The tier's name sits on the name's line, clear of the description under it.
            if (this.showTier) {
                pose.pushPose();
                pose.translate(x + width - 5 - tierWidth, textY, 0.0F);
                pose.scale(SMALL, SMALL, 1.0F);
                GuiDraw.text(graphics, font, tier, 0, 0, ARGB.multiplyAlpha(accent, 0.9F), false);
                pose.popPose();
            }
            pose.pushPose();
            pose.translate(textX, textY, 0.0F);
            pose.scale(SMALL, SMALL, 1.0F);
            CardViewerWidget.drawFittedText(graphics, font, this.getMessage(), w -> this.getMessage(), 0, 0,
                    (int) ((room - (tierWidth > 0 ? tierWidth + 4 : 0)) / SMALL), ARGB.srgbLerp(this.hover, 0xFFFFFFFF, ChoiceChrome.GOLD), true);
            pose.popPose();
            for (int i = 0; i < description.size(); i++) {
                pose.pushPose();
                pose.translate(textX, textY + 6 + i * lineHeight, 0.0F);
                pose.scale(scale, scale, 1.0F);
                GuiDraw.text(graphics, font, description.get(i), 0, 0, ChoiceChrome.MUTED, false);
                pose.popPose();
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
