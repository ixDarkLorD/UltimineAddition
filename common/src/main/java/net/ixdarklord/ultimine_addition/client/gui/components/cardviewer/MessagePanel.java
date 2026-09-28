package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.coolcatcanvas.api.client.gui.components.widgets.panel.Panel;
import net.ixdarklord.coolcatcanvas.api.client.utils.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class MessagePanel extends Panel {
    enum Mode { FULL, BANNER }

    private static final int LINE_HEIGHT = 10;
    private static final int PADDING = 4;

    private final CardViewerWidget viewer;
    private final Mode mode;
    private List<Component> lines = List.of();
    private List<ItemStack> items = List.of();
    private int background;
    private boolean centered = true;
    private List<FormattedCharSequence> wrapped = List.of();
    private int wrappedWidth = -1;

    MessagePanel(CardViewerWidget viewer, Mode mode) {
        this.viewer = viewer;
        this.mode = mode;
        this.setVisible(false);
    }

    void show(List<Component> lines, List<ItemStack> items, int background, boolean centered) {
        if (!lines.equals(this.lines) || this.centered != centered) {
            this.lines = lines;
            this.wrappedWidth = -1;
        }
        this.items = items;
        this.background = background;
        this.centered = centered;
        this.setVisible(true);
    }

    private void rewrap(int width) {
        if (width == this.wrappedWidth) return;
        this.wrappedWidth = width;
        List<FormattedCharSequence> result = new ArrayList<>();
        for (Component line : this.lines) result.addAll(this.font.split(line, width));
        this.wrapped = result;
    }

    private ScreenRectangle area() {
        ScreenRectangle b = this.getBounds();
        if (this.mode == Mode.FULL) return b;
        this.rewrap(b.width() - PADDING * 2);
        int height = this.wrapped.size() * LINE_HEIGHT + PADDING * 2 + (this.items.isEmpty() ? 0 : 18);
        height = Math.min(height, b.height());
        return new ScreenRectangle(b.left(), b.bottom() - height, b.width(), height);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.area().containsPoint((int) mouseX, (int) mouseY);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ScreenRectangle area = this.area();
        this.rewrap(area.width() - PADDING * 2);
        if (this.background != 0) {
            graphics.fill(area.left(), area.top(), area.right(), area.bottom(), this.background);
            if (this.mode == Mode.BANNER) graphics.fill(area.left(), area.top(), area.right(), area.top() + 1, 0x60FFFFFF);
        }

        int contentHeight = this.wrapped.size() * LINE_HEIGHT + (this.items.isEmpty() ? 0 : 18);
        int y = this.mode == Mode.FULL ? area.top() + Math.max(PADDING, (area.height() - contentHeight) / 2) : area.top() + PADDING;
        boolean shadow = this.viewer.hasTextShadow();
        for (FormattedCharSequence line : this.wrapped) {
            int x = this.centered ? area.left() + (area.width() - this.font.width(line)) / 2 : area.left() + PADDING;
            graphics.text(this.font, line, x, y, RenderUtils.textColor(0xFFFFFF), shadow);
            y += LINE_HEIGHT;
        }

        if (!this.items.isEmpty()) {
            int x = area.left() + (area.width() - this.items.size() * 18) / 2;
            for (ItemStack item : this.items) {
                graphics.item(item, x + 1, y + 1);
                if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                    graphics.setTooltipForNextFrame(this.font, item.getHoverName(), mouseX, mouseY);
                }
                x += 18;
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return this.mode == Mode.BANNER && this.isMouseOver(event.x(), event.y());
    }
}
