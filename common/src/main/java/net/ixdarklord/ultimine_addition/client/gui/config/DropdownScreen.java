package net.ixdarklord.ultimine_addition.client.gui.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

// A list of choices dropping down from a box (or up, when there's no room below), over the screen it was opened from,
// which stays visible (from Glazed Menu). Clicking a choice picks it; clicking elsewhere or Esc closes it. The arrow
// keys move the highlight, Enter picks it, and the wheel scrolls a long list.
public final class DropdownScreen<T> extends Screen {
    private static final int ROW_HEIGHT = 14;
    private static final int MAX_ROWS = 8;
    private static final int TOOLTIP_WIDTH = 200;
    private static final int KEY_ESCAPE = 256;
    private static final int KEY_ENTER = 257;
    private static final int KEY_NUMPAD_ENTER = 335;
    private static final int KEY_DOWN = 264;
    private static final int KEY_UP = 265;

    private final @Nullable Screen parent;
    private final List<T> values;
    private final Function<T, Component> name;
    private final Function<T, @Nullable Component> description;
    private final Consumer<T> onPick;
    private final int anchorX;
    private final int anchorY;
    private final int anchorWidth;
    private final int anchorHeight;
    private final int current;
    private int highlighted;
    private int scroll;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int rows;

    /**
     * @param anchorX     the box it drops from, in GUI pixels
     * @param description what a value does, shown as a tooltip on its row; null for none
     */
    public DropdownScreen(@Nullable Screen parent, int anchorX, int anchorY, int anchorWidth, int anchorHeight, List<T> values, T current,
                          Function<T, Component> name, Function<T, @Nullable Component> description, Consumer<T> onPick) {
        super(Component.empty());
        this.parent = parent;
        this.values = List.copyOf(values);
        this.name = name;
        this.description = description;
        this.onPick = onPick;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.anchorWidth = anchorWidth;
        this.anchorHeight = anchorHeight;
        this.current = Math.max(0, this.values.indexOf(current));
        this.highlighted = this.current;
    }

    @Override
    protected void init() {
        if (this.parent != null) this.parent.init(this.width, this.height);
        this.rows = Math.min(MAX_ROWS, this.values.size());
        int widest = this.values.stream().mapToInt(value -> this.font.width(this.name.apply(value))).max().orElse(0) + 28;
        this.panelWidth = Math.min(Math.max(this.anchorWidth, widest), this.width - 8);
        this.panelX = Math.clamp(this.anchorX, 4, this.width - 4 - this.panelWidth);
        int height = this.rows * ROW_HEIGHT + 4;
        // Below the box if it fits, else above it.
        this.panelY = this.anchorY + this.anchorHeight + height + 2 <= this.height - 4 ? this.anchorY + this.anchorHeight + 1 : Math.max(4, this.anchorY - height - 1);
        this.scrollTo(this.highlighted);
    }

    @Override
    public void resize(int width, int height) {
        if (this.parent != null) this.parent.resize(width, height);
        super.resize(width, height);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.parent != null) {
            this.parent.extractBackground(graphics, -1, -1, a);
            graphics.nextStratum();
            this.parent.extractRenderState(graphics, -1, -1, a);
            graphics.nextStratum();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int height = this.rows * ROW_HEIGHT + 4;
        int accent = PopupStyle.accent();
        PopupStyle.rect(graphics, this.panelX + 2, this.panelY + 3, this.panelWidth, height, 0x66000000);
        PopupStyle.rect(graphics, this.panelX, this.panelY, this.panelWidth, height, PopupStyle.colors().popup());
        PopupStyle.outline(graphics, this.panelX, this.panelY, this.panelWidth, height, accent);
        int hovered = this.rowAt(mouseX, mouseY);
        if (hovered >= 0) this.highlighted = hovered;
        for (int row = 0; row < this.rows; row++) {
            int index = this.scroll + row;
            if (index >= this.values.size()) break;
            int y = this.panelY + 2 + row * ROW_HEIGHT;
            boolean selected = index == this.current;
            if (index == this.highlighted) {
                PopupStyle.rect(graphics, this.panelX + 1, y, this.panelWidth - 2, ROW_HEIGHT, PopupStyle.colors().rowHover());
                PopupStyle.rect(graphics, this.panelX + 1, y + 2, 2, ROW_HEIGHT - 4, accent);
            }
            int textWidth = this.panelWidth - 26;
            PopupStyle.text(graphics, this.font, this.name.apply(this.values.get(index)), this.panelX + 8, y + (ROW_HEIGHT - 8) / 2, textWidth,
                    selected ? accent : index == this.highlighted ? PopupStyle.colors().text() : PopupStyle.colors().textDim());
            if (selected) PopupStyle.Icon.CHECK.draw(graphics, this.panelX + this.panelWidth - 6 - PopupStyle.Icon.SIZE, y + (ROW_HEIGHT - PopupStyle.Icon.SIZE) / 2, accent);
        }
        // A slim scrollbar when the list is longer than it shows.
        if (this.values.size() > this.rows) {
            int trackHeight = this.rows * ROW_HEIGHT;
            int thumb = Math.max(8, trackHeight * this.rows / this.values.size());
            int thumbY = this.panelY + 2 + (trackHeight - thumb) * this.scroll / (this.values.size() - this.rows);
            PopupStyle.rect(graphics, this.panelX + this.panelWidth - 3, thumbY, 2, thumb, PopupStyle.withAlpha(accent, 0xB0));
        }
        // The highlighted value's description, beside the list at its row so it never covers the choices.
        if (this.highlighted >= this.scroll && this.highlighted < Math.min(this.values.size(), this.scroll + this.rows)) {
            Component description = this.description.apply(this.values.get(this.highlighted));
            if (description != null) {
                int rowY = this.panelY + 2 + (this.highlighted - this.scroll) * ROW_HEIGHT;
                graphics.setTooltipForNextFrame(this.font, this.font.split(description, TOOLTIP_WIDTH), this::besideList, 0, rowY, true);
            }
        }
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    // To the right of the list, or to its left when there's no room; level with the row, kept on screen.
    private Vector2ic besideList(int screenWidth, int screenHeight, int x, int y, int width, int height) {
        int left = this.panelX + this.panelWidth + 6;
        if (left + width + 4 > screenWidth) left = Math.max(4, this.panelX - 6 - width);
        int top = Math.clamp(y - 2, 4, Math.max(4, screenHeight - height - 4));
        return new Vector2i(left, top);
    }

    private int rowAt(double mouseX, double mouseY) {
        if (mouseX < this.panelX || mouseX >= this.panelX + this.panelWidth) return -1;
        int row = (int) Math.floor((mouseY - this.panelY - 2) / ROW_HEIGHT);
        if (row < 0 || row >= this.rows) return -1;
        int index = this.scroll + row;
        return index < this.values.size() ? index : -1;
    }

    private void scrollTo(int index) {
        int max = Math.max(0, this.values.size() - this.rows);
        if (index < this.scroll) this.scroll = index;
        else if (index >= this.scroll + this.rows) this.scroll = index - this.rows + 1;
        this.scroll = Math.clamp(this.scroll, 0, max);
    }

    // The value is set before going back, so the screen underneath shows it as it returns.
    private void pick(int index) {
        this.onPick.accept(this.values.get(index));
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int index = this.rowAt(event.x(), event.y());
        if (index >= 0 && event.button() == 0) {
            this.pick(index);
        } else if (index < 0) {
            this.onClose();
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll = Math.clamp(this.scroll - (int) Math.signum(scrollY), 0, Math.max(0, this.values.size() - this.rows));
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        switch (event.key()) {
            case KEY_DOWN -> {
                this.highlighted = Math.min(this.values.size() - 1, this.highlighted + 1);
                this.scrollTo(this.highlighted);
            }
            case KEY_UP -> {
                this.highlighted = Math.max(0, this.highlighted - 1);
                this.scrollTo(this.highlighted);
            }
            case KEY_ENTER, KEY_NUMPAD_ENTER -> this.pick(this.highlighted);
            case KEY_ESCAPE -> this.onClose();
            default -> {
                return super.keyPressed(event);
            }
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return this.parent == null || this.parent.isPauseScreen();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
