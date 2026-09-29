package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.coolcatcanvas.api.client.gui.components.widgets.panel.Panel;
import net.ixdarklord.coolcatcanvas.api.client.utils.RenderUtils;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ShapeCertificateItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

// Picking the shape a tier's Shape Certificate teaches. The list has that tier's shapes and any earlier ones not picked
// yet; whatever isn't picked now stays on offer for later tiers (and other cards of the same tool).
final class ShapeChoicePanel extends Panel {
    private static final int PADDING = 4;
    private static final int HEADER = 30;
    private static final int FOOTER = 17;
    private static final int ROW = 15;

    private final CardViewerWidget viewer;
    private final ViewerButton backButton;
    private final List<ViewerButton> choices = new ArrayList<>();
    private MiningSkillCardItem.@Nullable Tier tier;
    private ItemStack icon = ItemStack.EMPTY;

    ShapeChoicePanel(CardViewerWidget viewer) {
        this.viewer = viewer;
        this.setModal(true);
        this.setVisible(false);
        this.backButton = this.addChild(new ViewerButton(36, Component.translatable("gui.back"), b -> this.setVisible(false)));
    }

    void show(MiningSkillCardItem.Tier tier, List<Identifier> pool, ItemStack icon) {
        this.choices.forEach(this::removeChild);
        this.choices.clear();
        this.tier = tier;
        this.icon = icon;
        for (Identifier shape : pool) {
            var ftbShape = FTBUltimineIntegration.getShape(shape);
            Component name = ftbShape == null ? Component.literal(shape.toString()) : ftbShape.getDisplayName();
            ViewerButton button = this.addChild(new ViewerButton(80, name, b -> {
                if (this.tier != null) this.viewer.claimCertificate(this.tier, shape);
                this.setVisible(false);
            }));
            MiningSkillCardItem.Tier from = ShapeCertificateItem.shapeTier(shape);
            Component origin = from == null ? Component.empty()
                    : from == tier ? Component.translatable("gui.ultimine_addition.card_viewer.certificate.from_this_tier").withStyle(ChatFormatting.GRAY)
                    : Component.translatable("gui.ultimine_addition.card_viewer.certificate.from_earlier_tier", from.getDisplayName()).withStyle(ChatFormatting.GRAY);
            button.setTooltip(Tooltip.create(Component.translatable("gui.ultimine_addition.card_viewer.certificate.pick", name).withStyle(ChatFormatting.GREEN)
                    .append("\n").append(origin)));
            this.choices.add(button);
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
        int width = Math.max(40, b.width() - PADDING * 2);
        int y = b.top() + HEADER;
        boolean room = this.viewer.hasRoomFor(this.icon);
        for (ViewerButton button : this.choices) {
            button.setWidth(width);
            button.setPosition(b.left() + PADDING, y);
            // Rows that don't fit stay hidden; the default lists are short.
            button.visible = y + 12 <= b.bottom() - FOOTER - 2;
            button.active = room;
            y += ROW;
        }
        this.backButton.setPosition(b.left() + (b.width() - this.backButton.getWidth()) / 2, b.bottom() - FOOTER + (FOOTER + 2 - 12) / 2);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ScreenRectangle b = this.getBounds();
        graphics.fill(b.left(), b.top(), b.right(), b.bottom(), this.viewer.themed(ChallengeDetailsPanel.PANEL_FILL));
        if (this.tier == null) return;
        boolean shadow = this.viewer.hasTextShadow();

        if (!this.icon.isEmpty()) graphics.item(this.icon, b.left() + PADDING, b.top() + 3);
        int textX = b.left() + PADDING + 20;
        Component title = Component.literal("》").append(Component.translatable("gui.ultimine_addition.card_viewer.certificate.choose"))
                .append(Component.literal(" · ")).append(this.tier.getDisplayName()).append("《");
        CardViewerWidget.drawFittedText(graphics, this.font, title, width -> title, textX, b.top() + 4, b.right() - PADDING - textX,
                RenderUtils.textColor(0xFBF1C1), shadow);
        boolean room = this.viewer.hasRoomFor(this.icon);
        Component sub = room ? Component.translatable("gui.ultimine_addition.card_viewer.certificate.others_stay").withStyle(ChatFormatting.GRAY)
                : Component.translatable("gui.ultimine_addition.card_viewer.certificate.no_space").withStyle(ChatFormatting.RED);
        CardViewerWidget.drawFittedText(graphics, this.font, sub, width -> sub, textX, b.top() + 14, b.right() - PADDING - textX, 0xFFFFFFFF, shadow);
        graphics.fill(b.left() + 2, b.top() + HEADER - 4, b.right() - 2, b.top() + HEADER - 3, 0x30FFFFFF);

        int footerTop = b.bottom() - FOOTER;
        graphics.fill(b.left(), footerTop, b.right(), b.bottom(), this.viewer.themed(ChallengeDetailsPanel.FOOTER_FILL));
        graphics.fill(b.left(), footerTop, b.right(), footerTop + 1, this.viewer.themed(ChallengeDetailsPanel.FOOTER_EDGE));
        graphics.fill(b.left(), footerTop + 1, b.right(), footerTop + 2, 0x28FFFFFF);
        for (ViewerButton button : this.choices) button.active = room;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_BACKSPACE || event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.setVisible(false);
            return true;
        }
        return false;
    }
}
