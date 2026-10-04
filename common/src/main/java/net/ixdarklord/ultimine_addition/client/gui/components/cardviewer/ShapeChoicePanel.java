package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.coolcatcanvas.api.client.gui.components.widgets.panel.Panel;
import net.ixdarklord.coolcatcanvas.api.client.utils.RenderUtils;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ShapeCertificateItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

// Picking the shape a tier's Shape Certificate teaches. The list has that tier's shapes and any earlier ones not picked
// yet; whatever isn't picked now stays on offer for later tiers (and other cards of the same tool). Each shape is a tile:
// a small diagram of the shape, its name, and a stripe in the color of the tier it comes from.
final class ShapeChoicePanel extends Panel {
    private static final int PADDING = 4;
    private static final int HEADER = 25;
    private static final int FOOTER = 17;
    private static final int GAP = 3;
    private static final int MAX_TILE_WIDTH = 60;
    private static final int GOLD = 0xFFFBD25A;

    private final CardViewerWidget viewer;
    private final ViewerButton backButton;
    private final List<ShapeTile> choices = new ArrayList<>();
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
            MiningSkillCardItem.Tier from = ShapeCertificateItem.shapeTier(shape);
            ShapeTile button = this.addChild(new ShapeTile(name, shape, tierColor(from == null ? tier : from), () -> {
                if (this.tier != null) this.viewer.claimCertificate(this.tier, shape);
                this.setVisible(false);
            }));
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
        int count = this.choices.size();
        int top = b.top() + HEADER + 1;
        int height = Math.max(12, b.bottom() - FOOTER - 3 - top);
        int space = Math.max(40, b.width() - PADDING * 2);
        // The tiles share the row, as wide as they can be up to a card's width, centered as a group.
        int tileWidth = count == 0 ? 0 : Math.max(14, Math.min(MAX_TILE_WIDTH, (space - GAP * (count - 1)) / count));
        int x = b.left() + (b.width() - (tileWidth * count + GAP * Math.max(0, count - 1))) / 2;
        boolean room = this.viewer.hasRoomFor(this.icon);
        for (ShapeTile tile : this.choices) {
            tile.setRectangle(tileWidth, height, x, top);
            // Tiles that don't fit stay hidden; the default lists are short.
            tile.visible = x >= b.left() && x + tileWidth <= b.right();
            tile.active = room;
            x += tileWidth + GAP;
        }
        this.backButton.setPosition(b.left() + (b.width() - this.backButton.getWidth()) / 2, b.bottom() - FOOTER + (FOOTER + 2 - 12) / 2);
    }

    private static int tierColor(MiningSkillCardItem.Tier tier) {
        return switch (tier) {
            case Novice -> 0xFF55FF55;
            case Apprentice -> 0xFF55FFFF;
            case Adept -> 0xFFFF55FF;
            case Mastered -> 0xFFFFAA00;
            default -> 0xFFAAAAAA;
        };
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ScreenRectangle b = this.getBounds();
        graphics.fill(b.left(), b.top(), b.right(), b.bottom(), this.viewer.themed(ChallengeDetailsPanel.PANEL_FILL));
        if (this.tier == null) return;
        boolean shadow = this.viewer.hasTextShadow();

        // The header: a darker band holding the certificate, closed by a line in the tier's color with a gold stud at its
        // middle.
        int accent = tierColor(this.tier);
        int line = b.top() + HEADER - 3;
        graphics.fill(b.left(), b.top(), b.right(), line, 0x50000000);
        graphics.fill(b.left(), line, b.right(), line + 1, ARGB.multiplyAlpha(accent, 0.8F));
        graphics.fill(b.left(), line + 1, b.right(), line + 2, 0x40000000);
        int middle = b.left() + b.width() / 2;
        graphics.fill(middle - 2, line - 1, middle + 2, line + 2, GOLD);
        graphics.fill(middle - 1, line - 2, middle + 1, line + 3, GOLD);
        if (!this.icon.isEmpty()) {
            graphics.fill(b.left() + PADDING - 1, b.top() + 2, b.left() + PADDING + 17, b.top() + 20, 0x60000000);
            graphics.item(this.icon, b.left() + PADDING, b.top() + 3);
        }
        int textX = b.left() + PADDING + 21;
        Component title = Component.translatable("gui.ultimine_addition.card_viewer.certificate.choose")
                .append(Component.literal(" · ")).append(this.tier.getDisplayName());
        CardViewerWidget.drawFittedText(graphics, this.font, title, width -> title, textX, b.top() + 3, b.right() - PADDING - textX,
                RenderUtils.textColor(0xFBF1C1), shadow);
        boolean room = this.viewer.hasRoomFor(this.icon);
        Component sub = room ? Component.translatable("gui.ultimine_addition.card_viewer.certificate.others_stay").withStyle(ChatFormatting.GRAY)
                : Component.translatable("gui.ultimine_addition.card_viewer.certificate.no_space").withStyle(ChatFormatting.RED);
        CardViewerWidget.drawFittedText(graphics, this.font, sub, width -> sub, textX, b.top() + 12, b.right() - PADDING - textX, 0xFFFFFFFF, shadow);

        int footerTop = b.bottom() - FOOTER;
        graphics.fill(b.left(), footerTop, b.right(), b.bottom(), this.viewer.themed(ChallengeDetailsPanel.FOOTER_FILL));
        graphics.fill(b.left(), footerTop, b.right(), footerTop + 1, this.viewer.themed(ChallengeDetailsPanel.FOOTER_EDGE));
        graphics.fill(b.left(), footerTop + 1, b.right(), footerTop + 2, 0x28FFFFFF);
        for (ShapeTile tile : this.choices) tile.active = room;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_BACKSPACE || event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.setVisible(false);
            return true;
        }
        return false;
    }

    // One shape on offer: its diagram, its name underneath, and a stripe in the color of the tier it belongs to.
    private static final class ShapeTile extends AbstractButton {
        private static final int CELLS = 5;
        private final String[] diagram;
        private final int color;
        private final Runnable onPress;

        ShapeTile(Component name, Identifier shape, int color, Runnable onPress) {
            super(0, 0, 40, 40, name);
            this.diagram = diagramOf(shape.getPath());
            this.color = color;
            this.onPress = onPress;
        }

        // The shape seen from the side it is mined from: '#' a block, 'o' the block you break, '.' nothing.
        private static String[] diagramOf(String shape) {
            return switch (shape) {
                case "small_tunnel" -> new String[]{".....", ".....", "o####", ".....", "....."};
                case "mining_tunnel" -> new String[]{".....", "#####", "o####", ".....", "....."};
                case "large_tunnel" -> new String[]{".....", "#####", "o####", "#####", "....."};
                case "escape_tunnel" -> new String[]{"...##", "..##.", ".##..", "o#...", "....."};
                case "shapeless" -> new String[]{".##..", "####.", "#o###", ".###.", "..#.."};
                // small_square, and the stand-in for shapes from other mods
                default -> new String[]{".....", ".###.", ".#o#.", ".###.", "....."};
            };
        }

        @Override
        public void onPress(InputWithModifiers input) {
            this.onPress.run();
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.active && this.isHoveredOrFocused();
            // A hovered tile lifts by a pixel.
            int x = this.getX(), y = this.getY() - (hovered ? 1 : 0), width = this.getWidth(), height = this.getHeight();
            int tint = this.active ? this.color : 0xFF6A6A6A;
            int edge = hovered ? GOLD : ARGB.multiplyAlpha(tint, 0.55F);

            // The card: a drop shadow, the body, the tier's stripe along the top and a 1px frame (gold while hovered).
            graphics.fill(x + 1, y + 2, x + width + 1, y + height + 1, 0x50000000);
            graphics.fill(x, y, x + width, y + height, hovered ? 0xF0303038 : 0xE01C1C22);
            graphics.fill(x + 1, y + 1, x + width - 1, y + 3, ARGB.multiplyAlpha(tint, hovered ? 1.0F : 0.75F));
            graphics.fill(x, y, x + width, y + 1, edge);
            graphics.fill(x, y + height - 1, x + width, y + height, edge);
            graphics.fill(x, y, x + 1, y + height, edge);
            graphics.fill(x + width - 1, y, x + width, y + height, edge);

            // The name sits along the bottom when the tile is wide enough to read it; the diagram takes the rest.
            boolean named = width >= 30 && height >= 30;
            int diagramHeight = height - 5 - (named ? 11 : 2);
            int cell = Math.max(1, Math.min((width - 6) / CELLS, diagramHeight / CELLS) - 1);
            int size = (cell + 1) * CELLS - 1;
            int left = x + (width - size) / 2, top = y + 5 + (diagramHeight - size) / 2;
            for (int row = 0; row < CELLS; row++) {
                for (int column = 0; column < CELLS; column++) {
                    char c = this.diagram[row].charAt(column);
                    int cx = left + column * (cell + 1), cy = top + row * (cell + 1);
                    int fill = c == 'o' ? (this.active ? GOLD : 0xFFB0B0B0)
                            : c == '#' ? ARGB.multiplyAlpha(tint, hovered ? 1.0F : 0.85F) : 0x18FFFFFF;
                    graphics.fill(cx, cy, cx + cell, cy + cell, fill);
                }
            }
            if (named) {
                Font font = Minecraft.getInstance().font;
                Component name = this.getMessage();
                float scale = Math.min(1.0F, (width - 4) / (float) Math.max(1, font.width(name)));
                var pose = graphics.pose();
                pose.pushMatrix();
                pose.translate(x + width / 2.0F, y + height - 6.0F);
                pose.scale(scale, scale);
                graphics.centeredText(font, name, 0, -4, this.active ? (hovered ? GOLD : 0xFFFFFFFF) : 0xFFA0A0A0);
                pose.popMatrix();
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
