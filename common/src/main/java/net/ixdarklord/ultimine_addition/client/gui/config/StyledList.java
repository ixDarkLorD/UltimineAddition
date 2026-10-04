package net.ixdarklord.ultimine_addition.client.gui.config;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

// The config popup's scrolling list (from CoolCatLib: Core's config screens). Vanilla's lists give every row the same height; these give each row its own,
// laying the rows out top to bottom, and each row knows its own bounds. No background or separators are drawn.
public abstract class StyledList<E extends StyledList.Entry<E>> extends ContainerObjectSelectionList<E> {
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller");
    private static final ResourceLocation SCROLLER_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller_background");
    private static final int SCROLLBAR_WIDTH = 6;

    private @Nullable E hoveredEntry;

    protected StyledList(Minecraft minecraft, int width, int height, int y, int defaultEntryHeight) {
        super(minecraft, width, height, y, defaultEntryHeight);
    }

    // --- Entries and layout ---

    @Override
    protected int addEntry(E entry) {
        return this.addEntry(entry, this.itemHeight);
    }

    protected int addEntry(E entry, int height) {
        entry.height = height;
        int index = super.addEntry(entry);
        this.repositionEntries();
        return index;
    }

    @Override
    public void replaceEntries(Collection<E> entries) {
        this.clearEntries();
        for (E entry : entries) this.addEntry(entry);
    }

    @Override
    protected boolean removeEntry(E entry) {
        boolean removed = super.removeEntry(entry);
        if (removed) this.repositionEntries();
        return removed;
    }

    /** Places every row under the one before it, for the current position and scroll. */
    protected void repositionEntries() {
        int y = this.getY() + 2 - (int) this.getScrollAmount();
        for (E child : this.children()) {
            child.x = this.getRowLeft();
            child.y = y;
            child.width = this.getRowWidth();
            y += child.height;
        }
    }

    public void updateSizeAndPosition(int width, int height, int x, int y) {
        this.setSize(width, height);
        this.setPosition(x, y);
        this.repositionEntries();
        this.clampScrollAmount();
    }

    @Override
    public void updateSizeAndPosition(int width, int height, int y) {
        this.updateSizeAndPosition(width, height, 0, y);
    }

    @Override
    public int getRowLeft() {
        return this.getX() + this.width / 2 - this.getRowWidth() / 2;
    }

    @Override
    protected int getRowTop(int index) {
        return this.children().get(index).y;
    }

    @Override
    protected int getRowBottom(int index) {
        E child = this.children().get(index);
        return child.y + child.height;
    }

    /** The rows' total height. */
    protected int contentHeight() {
        int total = 0;
        for (E child : this.children()) total += child.height;
        return total + 4;
    }

    @Override
    protected int getMaxPosition() {
        return this.contentHeight();
    }

    // --- Scrolling ---

    @Override
    public int getMaxScroll() {
        return Math.max(0, this.contentHeight() - this.height);
    }

    public int maxScrollAmount() {
        return this.getMaxScroll();
    }

    public double scrollAmount() {
        return this.getScrollAmount();
    }

    @Override
    public void setClampedScrollAmount(double scroll) {
        super.setClampedScrollAmount(scroll);
        this.repositionEntries();
    }

    protected boolean scrollable() {
        return this.getMaxScroll() > 0;
    }

    protected int scrollBarX() {
        return this.getRowRight() + SCROLLBAR_WIDTH + 2;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.scrollBarX();
    }

    protected int scrollerHeight() {
        return Mth.clamp((int) ((float) (this.height * this.height) / this.contentHeight()), 32, this.height - 8);
    }

    protected int scrollBarY() {
        int max = this.getMaxScroll();
        return max == 0 ? this.getY() : Math.max(this.getY(), (int) this.getScrollAmount() * (this.height - this.scrollerHeight()) / max + this.getY());
    }

    private boolean isOverScrollbar(double x, double y) {
        return this.scrollable() && x >= this.scrollBarX() && x <= this.scrollBarX() + SCROLLBAR_WIDTH && y >= this.getY() && y < this.getBottom();
    }

    @Override
    protected void ensureVisible(E entry) {
        int topDelta = entry.y - this.getY() - 2;
        if (topDelta < 0) this.setScrollAmount(this.getScrollAmount() + topDelta);
        int bottomDelta = this.getBottom() - entry.y - entry.height - 2;
        if (bottomDelta < 0) this.setScrollAmount(this.getScrollAmount() - bottomDelta);
    }

    @Override
    protected void centerScrollOn(E entry) {
        int y = 0;
        for (E child : this.children()) {
            if (child == entry) {
                y += child.height / 2;
                break;
            }
            y += child.height;
        }
        this.setScrollAmount(y - this.height / 2.0);
    }

    // --- Input ---

    /** The row under a point, if any. */
    protected @Nullable E entryAt(double x, double y) {
        for (E child : this.children()) {
            if (child.isMouseOver(x, y)) return child;
        }
        return null;
    }

    // Rows get every mouse button (a selector steps back on right-click); only the left one drags the scrollbar.
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.repositionEntries();
        this.updateScrollingState(mouseX, mouseY, button);
        if (!this.isMouseOver(mouseX, mouseY)) return false;
        E entry = this.entryAt(mouseX, mouseY);
        if (entry != null && entry.mouseClicked(mouseX, mouseY, button)) {
            E focused = this.getFocused();
            if (focused != entry && focused instanceof ContainerEventHandler container) container.setFocused(null);
            this.setFocused(entry);
            this.setDragging(true);
            return true;
        }
        return button == 0 && this.isOverScrollbar(mouseX, mouseY);
    }

    // --- Drawing ---

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.repositionEntries();
        this.hoveredEntry = this.isMouseOver(mouseX, mouseY) ? this.entryAt(mouseX, mouseY) : null;
        this.renderListBackground(graphics);
        this.enableScissor(graphics);
        for (E child : this.children()) {
            if (child.y + child.height >= this.getY() && child.y <= this.getBottom()) {
                child.renderContent(graphics, mouseX, mouseY, child == this.hoveredEntry, partialTick);
            }
        }
        graphics.disableScissor();
        this.renderListSeparators(graphics);
        this.renderScrollbar(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderListBackground(GuiGraphics graphics) {}

    @Override
    protected void renderListSeparators(GuiGraphics graphics) {}

    /** Vanilla's scrollbar, while the rows don't fit. */
    protected void renderScrollbar(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!this.scrollable()) return;
        RenderSystem.enableBlend();
        graphics.blitSprite(SCROLLER_BACKGROUND_SPRITE, this.scrollBarX(), this.getY(), SCROLLBAR_WIDTH, this.getHeight());
        graphics.blitSprite(SCROLLER_SPRITE, this.scrollBarX(), this.scrollBarY(), SCROLLBAR_WIDTH, this.scrollerHeight());
        RenderSystem.disableBlend();
    }

    @Override
    protected @Nullable E getHovered() {
        return this.hoveredEntry;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.isFocused()) return NarratableEntry.NarrationPriority.FOCUSED;
        return this.hoveredEntry != null ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
    }

    /** A row, with its own height; the list sets its bounds as it lays the rows out. */
    public abstract static class Entry<E extends Entry<E>> extends ContainerObjectSelectionList.Entry<E> {
        int x;
        int y;
        int width;
        int height;

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        /** Draws the row within its bounds. */
        public abstract void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float partialTick);

        @Override
        public final void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY,
                                 boolean hovering, float partialTick) {
            this.renderContent(graphics, mouseX, mouseY, hovering, partialTick);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
        }
    }
}
