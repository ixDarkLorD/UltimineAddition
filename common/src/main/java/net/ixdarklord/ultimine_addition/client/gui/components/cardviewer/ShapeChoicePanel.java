package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.coolcatcanvas.api.client.gui.components.widgets.panel.Panel;
import net.ixdarklord.coolcatcanvas.api.client.utils.RenderUtils;
import net.ixdarklord.ultimine_addition.client.gui.GuiDraw;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ShapeCertificateItem;
import net.ixdarklord.ultimine_addition.common.progression.ShapeDiagram;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

// Picking the shape a tier's Shape Certificate teaches. The list has that tier's shapes and any earlier ones not picked
// yet; whatever isn't picked now stays on offer for later tiers (and other cards of the same tool). Three shapes show at
// a time; the mouse wheel and the arrows beside Back scroll through the rest. Each shape is a tile:
// a small diagram of the shape (worked out by the server from FTB Ultimine, see ShapeDiagram), its name, and a stripe in
// the color of the tier it comes from.
final class ShapeChoicePanel extends Panel {
    private static final int PADDING = 4;
    private static final int HEADER = 25;
    private static final int FOOTER = 17;
    private static final int GAP = 3;
    private static final int MAX_TILE_WIDTH = 60;
    // How many tiles show at once.
    private static final int VISIBLE = 3;
    private static final int GOLD = 0xFFFBD25A;

    private final CardViewerWidget viewer;
    private final ViewerButton backButton;
    private final ViewerButton previousButton;
    private final ViewerButton nextButton;
    // The first tile shown.
    private int first;
    private final List<ShapeTile> choices = new ArrayList<>();
    private MiningSkillCardItem.@Nullable Tier tier;
    private ItemStack icon = ItemStack.EMPTY;

    ShapeChoicePanel(CardViewerWidget viewer) {
        this.viewer = viewer;
        this.setModal(true);
        this.setVisible(false);
        this.backButton = this.addChild(new ViewerButton(36, Component.translatable("gui.back"), b -> this.setVisible(false)));
        this.previousButton = this.addChild(new ViewerButton(12, Component.literal("<"), b -> this.scroll(-1)));
        this.nextButton = this.addChild(new ViewerButton(12, Component.literal(">"), b -> this.scroll(1)));
    }

    void show(MiningSkillCardItem.Tier tier, List<ResourceLocation> pool, ItemStack icon) {
        this.choices.forEach(this::removeChild);
        this.choices.clear();
        this.tier = tier;
        this.icon = icon;
        this.first = 0;
        ShapeDiagrams.request(pool);
        for (ResourceLocation shape : pool) {
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

    private void scroll(int by) {
        this.first = Math.max(0, Math.min(this.choices.size() - VISIBLE, this.first + by));
        this.layout();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0.0) this.scroll(scrollY > 0.0 ? -1 : 1);
        return true;
    }

    private void layout() {
        ScreenRectangle b = this.getBounds();
        int count = this.choices.size();
        int top = b.top() + HEADER + 1;
        int height = Math.max(12, b.bottom() - FOOTER - 3 - top);
        int space = Math.max(40, b.width() - PADDING * 2);
        // Room for three tiles, however many there are; the ones shown are centered as a group.
        int tileWidth = Math.max(14, Math.min(MAX_TILE_WIDTH, (space - GAP * (VISIBLE - 1)) / VISIBLE));
        int shown = Math.min(count, VISIBLE);
        this.first = Math.max(0, Math.min(count - VISIBLE, this.first));
        int x = b.left() + (b.width() - (tileWidth * shown + GAP * Math.max(0, shown - 1))) / 2;
        boolean room = this.viewer.hasRoomFor(this.icon);
        for (int i = 0; i < count; i++) {
            ShapeTile tile = this.choices.get(i);
            tile.visible = i >= this.first && i < this.first + VISIBLE;
            tile.active = room;
            if (!tile.visible) continue;
            tile.setRectangle(tileWidth, height, x, top);
            x += tileWidth + GAP;
        }
        int buttonY = b.bottom() - FOOTER + (FOOTER + 2 - 12) / 2;
        int backX = b.left() + (b.width() - this.backButton.getWidth()) / 2;
        this.backButton.setPosition(backX, buttonY);
        // The arrows only show when there is something to scroll to, and grey out at either end.
        boolean scrollable = count > VISIBLE;
        this.previousButton.visible = this.nextButton.visible = scrollable;
        this.previousButton.active = this.first > 0;
        this.nextButton.active = this.first < count - VISIBLE;
        this.previousButton.setPosition(backX - this.previousButton.getWidth() - GAP, buttonY);
        this.nextButton.setPosition(backX + this.backButton.getWidth() + GAP, buttonY);
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
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
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
            graphics.renderItem(this.icon, b.left() + PADDING, b.top() + 3);
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
        if (this.choices.size() > VISIBLE) {
            Component range = Component.literal((this.first + 1) + "-" + (this.first + VISIBLE) + " / " + this.choices.size()).withStyle(ChatFormatting.GRAY);
            graphics.drawString(this.font, range, b.right() - PADDING - this.font.width(range), footerTop + (FOOTER + 2 - 8) / 2, 0xFFFFFFFF, shadow);
        }
        // Its tiles, and panels drawn later, cover the certificate icon.
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

    // One shape on offer: its diagram, its name underneath, and a stripe in the color of the tier it belongs to.
    private static final class ShapeTile extends AbstractButton {
        // The smallest grid drawn: smaller shapes sit in the middle of it.
        private static final int MIN_CELLS = 5;
        private static final float MIN_NAME_SCALE = 0.6F;
        // Shapes that follow the block being mined have no fixed picture: a loose cluster stands in for them.
        private static final String[] CLUSTER = {".##..", "####.", "#o###", ".###.", "..#.."};
        private final ResourceLocation shape;
        private final int color;
        private final Runnable onPress;

        ShapeTile(Component name, ResourceLocation shape, int color, Runnable onPress) {
            super(0, 0, 40, 40, name);
            this.shape = shape;
            this.color = color;
            this.onPress = onPress;
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

            // The name sits along the bottom, on up to two lines, when the tile is wide enough to read it; the diagram
            // takes the rest.
            Font font = Minecraft.getInstance().font;
            boolean named = width >= 26 && height >= 30;
            float scale = 1.0F;
            List<String> lines = List.of();
            if (named) {
                // As large as the longest word allows (down to 60%), wrapped by whole words onto two lines. A name
                // needing more drops what's in brackets (FTB Ultimine's "(3x3)") first, then shrinks to fit.
                int room = width - 4;
                String name = this.getMessage().getString();
                for (int attempt = 0; attempt < 2; attempt++) {
                    int widest = 1;
                    for (String word : name.trim().split("\\s+")) widest = Math.max(widest, font.width(word));
                    scale = Math.max(MIN_NAME_SCALE, Math.min(1.0F, room / (float) widest));
                    lines = wrap(font, name, (int) (room / scale));
                    if (lines.size() <= 2) break;
                    name = name.replaceAll("\\s*\\(.*\\)\\s*$", "");
                }
                while (lines.size() > 2 && scale > MIN_NAME_SCALE) {
                    scale = Math.max(MIN_NAME_SCALE, scale - 0.05F);
                    lines = wrap(font, name, (int) (room / scale));
                }
                if (lines.size() > 2) lines = lines.subList(0, 2);
            }
            int lineHeight = Math.round(8 * scale);
            int diagramHeight = height - 3 - (named ? lines.size() * lineHeight + 1 : 2);
            // The server's diagram of the shape, in a grid of at least 5 x 5 cells; an empty grid until it arrives.
            ShapeDiagram diagram = ShapeDiagrams.get(this.shape);
            boolean cluster = diagram != null && diagram.isIndeterminate();
            int shapeWidth = diagram == null ? 0 : cluster ? MIN_CELLS : diagram.width();
            int shapeHeight = diagram == null ? 0 : cluster ? MIN_CELLS : diagram.height();
            int columns = Math.max(MIN_CELLS, shapeWidth), rows = Math.max(MIN_CELLS, shapeHeight);
            int offsetX = (columns - shapeWidth) / 2, offsetY = (rows - shapeHeight) / 2;
            int cell = Math.max(1, Math.min((width - 6) / columns, diagramHeight / rows) - 1);
            int left = x + (width - ((cell + 1) * columns - 1)) / 2, top = y + 3 + (diagramHeight - ((cell + 1) * rows - 1)) / 2;
            int block = ARGB.multiplyAlpha(tint, hovered ? 1.0F : 0.85F), broken = this.active ? GOLD : 0xFFB0B0B0;
            for (int row = 0; row < rows; row++) {
                for (int column = 0; column < columns; column++) {
                    int dx = column - offsetX, dy = row - offsetY;
                    boolean inside = dx >= 0 && dx < shapeWidth && dy >= 0 && dy < shapeHeight;
                    int fill = 0x18FFFFFF;
                    if (inside && cluster) {
                        char c = CLUSTER[dy].charAt(dx);
                        if (c != '.') fill = c == 'o' ? broken : block;
                    } else if (inside && diagram.has(dx, dy)) {
                        fill = diagram.isOrigin(dx, dy) ? broken : block;
                    }
                    int cx = left + column * (cell + 1), cy = top + row * (cell + 1);
                    graphics.fill(cx, cy, cx + cell, cy + cell, fill);
                }
            }
            int color = this.active ? (hovered ? GOLD : 0xFFFFFFFF) : 0xFFA0A0A0;
            for (int i = 0; i < lines.size(); i++) {
                var pose = graphics.pose();
                pose.pushPose();
                pose.translate(x + width / 2.0F, y + height - 1.0F - (lines.size() - i) * lineHeight, 0.0F);
                pose.scale(scale, scale, 1.0F);
                graphics.drawCenteredString(font, Component.literal(lines.get(i)), 0, 0, color);
                pose.popPose();
            }
        }

        // The words on as few lines as fit the width, never split inside a word.
        private static List<String> wrap(Font font, String text, int maxWidth) {
            List<String> lines = new ArrayList<>();
            String line = "";
            for (String word : text.trim().split("\\s+")) {
                String longer = line.isEmpty() ? word : line + " " + word;
                if (line.isEmpty() || font.width(longer) <= maxWidth) {
                    line = longer;
                } else {
                    lines.add(line);
                    line = word;
                }
            }
            if (!line.isEmpty()) lines.add(line);
            return lines;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
