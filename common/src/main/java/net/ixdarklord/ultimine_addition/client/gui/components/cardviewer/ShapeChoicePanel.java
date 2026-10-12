package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.coolcatcanvas.api.client.gui.components.widgets.panel.Panel;
import net.ixdarklord.coolcatcanvas.api.client.utils.RenderUtils;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ShapeCertificateItem;
import net.ixdarklord.ultimine_addition.common.progression.ShapeDiagram;
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
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

// Picking the shape a tier's Shape Certificate teaches. The list has that tier's shapes and any earlier ones not picked
// yet; whatever isn't picked now stays on offer for later tiers (and other cards of the same tool). Three shapes show at
// a time; the mouse wheel and the arrows beside Back scroll through the rest. Each shape is a tile:
// a small diagram of the shape (worked out by the server from FTB Ultimine, see ShapeDiagram) on a sunken plate, its
// name under it, and a bar in the color of the tier it comes from. A tile is a raised plate that lifts and lights up
// under the mouse, and the tiles slide across when the page changes. The header and footer are the choice panels' own
// (ChoiceChrome); dots in the footer say which part of a longer list is showing.
final class ShapeChoicePanel extends Panel {
    private static final int PADDING = ChoiceChrome.PADDING;
    private static final int HEADER = ChoiceChrome.HEADER;
    private static final int FOOTER = ChoiceChrome.FOOTER;
    private static final int GAP = 4;
    private static final int MAX_TILE_WIDTH = 96;
    // How many tiles show at once.
    private static final int VISIBLE = 3;
    private static final int GOLD = ChoiceChrome.GOLD;

    private final CardViewerWidget viewer;
    private final ViewerButton backButton;
    private final ViewerButton previousButton;
    private final ViewerButton nextButton;
    // The first tile shown, and where the row has slid to on its way there (the same once it has arrived).
    private int first;
    private float shownFirst;
    private long lastFrame;
    private final List<ShapeTile> choices = new ArrayList<>();
    private MiningSkillCardItem.@Nullable Tier tier;
    private ItemStack icon = ItemStack.EMPTY;

    ShapeChoicePanel(CardViewerWidget viewer) {
        this.viewer = viewer;
        this.setModal(true);
        this.setVisible(false);
        this.backButton = this.addChild(new ViewerButton(36, Component.translatable("gui.back"), b -> this.setVisible(false)));
        this.previousButton = this.addChild(new ViewerButton(12, Component.literal("<"), b -> this.scroll(-VISIBLE)));
        this.nextButton = this.addChild(new ViewerButton(12, Component.literal(">"), b -> this.scroll(VISIBLE)));
    }

    void show(MiningSkillCardItem.Tier tier, List<Identifier> pool, ItemStack icon) {
        this.choices.forEach(this::removeChild);
        this.choices.clear();
        this.tier = tier;
        this.icon = icon;
        this.first = 0;
        this.shownFirst = 0.0F;
        ShapeDiagrams.request(pool);
        for (Identifier shape : pool) {
            var ftbShape = FTBUltimineIntegration.getShape(shape);
            Component name = ftbShape == null ? Component.literal(shape.toString()) : ftbShape.getDisplayName();
            MiningSkillCardItem.Tier from = ShapeCertificateItem.shapeTier(shape);
            ShapeTile button = this.addChild(new ShapeTile(this.viewer, name, shape, tierColor(from == null ? tier : from), () -> {
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
        // A tile is no taller than its diagram and name need: in a big viewer the row sits in the middle of the room.
        int tileHeight = Math.min(height, tileWidth + 22);
        top += (height - tileHeight) / 2;
        height = tileHeight;
        this.first = Math.max(0, Math.min(count - VISIBLE, this.first));
        int x = b.left() + (b.width() - (tileWidth * shown + GAP * Math.max(0, shown - 1))) / 2;
        boolean room = this.viewer.hasRoomFor(this.icon);
        // The row slides to its page: every tile that is in or entering the window is placed, and the row clips them.
        if (!this.viewer.isAnimated()) this.shownFirst = this.first;
        int pitch = tileWidth + GAP, rowRight = x + tileWidth * shown + GAP * Math.max(0, shown - 1);
        for (int i = 0; i < count; i++) {
            ShapeTile tile = this.choices.get(i);
            float place = i - this.shownFirst;
            tile.visible = place > -1.0F && place < VISIBLE;
            tile.active = room;
            tile.clip(x, rowRight);
            if (tile.visible) tile.setRectangle(tileWidth, height, x + Math.round(place * pitch), top);
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
        return ChoiceChrome.tierColor(tier);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ScreenRectangle b = this.getBounds();
        if (this.tier == null) {
            graphics.fill(b.left(), b.top(), b.right(), b.bottom(), this.viewer.themed(ChallengeDetailsPanel.PANEL_FILL));
            return;
        }
        long now = Util.getMillis();
        float seconds = Math.min(0.1F, (now - this.lastFrame) / 1000.0F);
        this.lastFrame = now;
        if (this.shownFirst != this.first) {
            this.shownFirst = ChoiceChrome.approach(this.shownFirst, this.first, seconds, 14.0F);
            this.layout();
        }
        boolean room = this.viewer.hasRoomFor(this.icon);
        Component sub = room ? Component.translatable("gui.ultimine_addition.card_viewer.certificate.others_stay").withStyle(ChatFormatting.GRAY)
                : Component.translatable("gui.ultimine_addition.card_viewer.certificate.no_space").withStyle(ChatFormatting.RED);
        ChoiceChrome.frame(graphics, this.viewer, this.font, b, this.icon, Component.translatable("gui.ultimine_addition.card_viewer.certificate.choose"), sub, this.tier);
        for (ShapeTile tile : this.choices) tile.active = room;

        // A dot for every shape on the footer's right, the ones showing lit.
        if (this.choices.size() > VISIBLE) {
            int count = this.choices.size(), dotsWidth = count * 4 - 1;
            int x = b.right() - PADDING - dotsWidth, y = b.bottom() - FOOTER + (FOOTER + 2 - 3) / 2;
            for (int i = 0; i < count; i++) {
                boolean showing = i >= this.first && i < this.first + VISIBLE;
                graphics.fill(x + i * 4, y, x + i * 4 + 3, y + 3, showing ? GOLD : 0x50FFFFFF);
            }
        }
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
        // The smallest grid drawn: smaller shapes sit in the middle of it.
        private static final int MIN_CELLS = 5;
        private static final float MIN_NAME_SCALE = 0.6F;
        // Shapes that follow the block being mined have no fixed picture: a loose cluster stands in for them.
        private static final String[] CLUSTER = {".##..", "####.", "#o###", ".###.", "..#.."};
        private final CardViewerWidget viewer;
        private final Identifier shape;
        private final int color;
        private final Runnable onPress;
        // How far the tile is into its hovered look (0 to 1), and when it was last drawn.
        private float hover;
        private long lastFrame;
        // The row's ends on the screen: a tile sliding in or out is cut off there.
        private int clipLeft, clipRight;

        void clip(int left, int right) {
            this.clipLeft = left;
            this.clipRight = right;
        }

        ShapeTile(CardViewerWidget viewer, Component name, Identifier shape, int color, Runnable onPress) {
            super(0, 0, 40, 40, name);
            this.viewer = viewer;
            this.shape = shape;
            this.color = color;
            this.onPress = onPress;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            this.onPress.run();
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.active && this.isHoveredOrFocused();
            long now = Util.getMillis();
            float seconds = Math.min(0.1F, (now - this.lastFrame) / 1000.0F);
            this.lastFrame = now;
            this.hover = this.viewer.isAnimated() ? ChoiceChrome.approach(this.hover, hovered ? 1.0F : 0.0F, seconds, 16.0F) : hovered ? 1.0F : 0.0F;
            // A hovered tile lifts off the panel.
            int x = this.getX(), y = this.getY() - Math.round(this.hover * 2.0F), width = this.getWidth(), height = this.getHeight();
            int tint = this.active ? this.color : 0xFF6A6A6A;
            graphics.enableScissor(this.clipLeft - 1, this.getY() - 4, this.clipRight + 3, this.getY() + height + 4);

            // The plate, with the tier's bar along its top.
            ChoiceChrome.plate(graphics, this.viewer, x, y, width, height, this.hover, this.active);
            graphics.fill(x + 3, y + 2, x + width - 3, y + 3, ARGB.srgbLerp(this.hover, ARGB.multiplyAlpha(tint, 0.7F), tint));
            // What follows is laid out on the plate's face, above its side.
            height -= ChoiceChrome.LIP;

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
            int diagramHeight = height - 5 - (named ? lines.size() * lineHeight + 2 : 2);
            // The server's diagram of the shape, in a grid of at least 5 x 5 cells; an empty grid until it arrives.
            ShapeDiagram diagram = ShapeDiagrams.get(this.shape);
            boolean cluster = diagram != null && diagram.isIndeterminate();
            int shapeWidth = diagram == null ? 0 : cluster ? MIN_CELLS : diagram.width();
            int shapeHeight = diagram == null ? 0 : cluster ? MIN_CELLS : diagram.height();
            int columns = Math.max(MIN_CELLS, shapeWidth), rows = Math.max(MIN_CELLS, shapeHeight);
            int offsetX = (columns - shapeWidth) / 2, offsetY = (rows - shapeHeight) / 2;
            int cell = Math.max(1, Math.min((width - 6) / columns, diagramHeight / rows) - 1);
            int left = x + (width - ((cell + 1) * columns - 1)) / 2, top = y + 4 + (diagramHeight - ((cell + 1) * rows - 1)) / 2;
            // The grid sits in a well of its own.
            ChoiceChrome.well(graphics, left - 2, top - 2, (cell + 1) * columns + 3, (cell + 1) * rows + 3);
            int block = ARGB.multiplyAlpha(tint, hovered ? 1.0F : 0.85F), broken = this.active ? GOLD : 0xFFB0B0B0;
            for (int row = 0; row < rows; row++) {
                for (int column = 0; column < columns; column++) {
                    int dx = column - offsetX, dy = row - offsetY;
                    boolean inside = dx >= 0 && dx < shapeWidth && dy >= 0 && dy < shapeHeight;
                    int fill = 0x14FFFFFF;
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
                pose.pushMatrix();
                pose.translate(x + width / 2.0F, y + height - 1.0F - (lines.size() - i) * lineHeight);
                pose.scale(scale, scale);
                graphics.centeredText(font, Component.literal(lines.get(i)), 0, 0, color);
                pose.popMatrix();
            }
            graphics.disableScissor();
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
