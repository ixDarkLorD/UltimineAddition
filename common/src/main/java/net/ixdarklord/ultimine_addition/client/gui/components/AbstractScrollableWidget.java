package net.ixdarklord.ultimine_addition.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.coolcatlib.api.util.ColorUtils;
import net.ixdarklord.coolcatlib.api.util.MouseHelper;
import net.ixdarklord.coolcatlib.api.util.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@Environment(EnvType.CLIENT)
public abstract class AbstractScrollableWidget extends AbstractDraggableWidget {
    protected final int border;
    private boolean scrolling;
    private boolean scrollOpposite;
    private boolean scrollHorizontally;
    protected double scrollOffsetX;
    protected double scrollOffsetY;

    private final @Nullable WidgetSprites barSprites;
    protected final int scrollWidth;
    protected final int scrollHeight;
    protected final int barLength;
    private final @Nullable WidgetSprites bgSprites;
    private int borderColor = 0x40000000;
    private int scrollBgColor = new Color(0, 0, 0).getRGB();
    private int scrollColor = new Color(174, 174, 174).getRGB();

    public AbstractScrollableWidget(int x, int y, int width, int height, int border, boolean isMovable) {
        this(x, y, width, height, border, null, isMovable);
    }

    public AbstractScrollableWidget(int x, int y, int width, int height, int border, @Nullable WidgetSprites bgSprites, boolean isMovable) {
        this(x, y, width, height, border, null, 6, 0, height, bgSprites, isMovable);
    }

    public AbstractScrollableWidget(int x, int y, int width, int height, int border, @Nullable WidgetSprites barSprites, int scrollWidth, int scrollHeight, int barLength, boolean isMovable) {
        this(x, y, width, height, border, barSprites, scrollWidth, scrollHeight, barLength, null, isMovable);
    }

    public AbstractScrollableWidget(int x, int y, int width, int height, int border, @Nullable WidgetSprites barSprites, int scrollWidth, int scrollHeight, int barLength, @Nullable WidgetSprites bgSprites, boolean isMovable) {
        super(CommonComponents.EMPTY, x, y, width, height, isMovable);
        this.border = Math.abs(border);
        this.barSprites = barSprites;
        this.scrollWidth = scrollWidth;
        this.scrollHeight = scrollHeight > 0 ? scrollHeight : height - border * 2;
        this.barLength = barLength;
        this.bgSprites = bgSprites;
    }

    @Override
    protected void updateChildren() {
        super.updateChildren();
        this.layout.setPosition(this.getRelativePosition().x(), this.getRelativePosition().y());
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if (this.bgSprites != null)
            guiGraphics.blitSprite(this.bgSprites.get(this.visible, this.isFocused()), this.x, this.y, this.width, this.height);
        else {
            Screen.renderMenuBackgroundTexture(guiGraphics, Screen.MENU_BACKGROUND, this.x, this.y, 0, 0, this.width, this.height);
            RenderUtils.renderHollowRectangleOrThrow(guiGraphics, this.getRectangle(), this.border, this.border < 0, this.borderColor);
        }
    }

    protected void renderScrollableContents(GuiGraphics guiGraphics, float partialTick, int relativeX, int relativeY, int mouseX, int mouseY) {
        super.renderContents(guiGraphics, partialTick, mouseX, mouseY);
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderUtils.renderInRectangle(guiGraphics, this.layoutRectangle(), !this.isDebug(), () -> {
            int relativeX = this.layoutRectangle().left() - (int) this.scrollOffsetX;
            int relativeY = this.layoutRectangle().top() - (int) this.scrollOffsetY;
            this.renderScrollableContents(guiGraphics, partialTick, relativeX, relativeY, mouseX, mouseY);
        });
        this.renderScrollbar(guiGraphics, mouseX, mouseY);
    }

    protected void renderScrollbar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.scrollHorizontally) return;

        if (barSprites != null) {
            double delta = this.getScrollDistanceDelta(ScreenAxis.VERTICAL);
            int relativeY = (int) Mth.lerp(delta, this.getScrollbarPosition().y(), this.getScrollbarPosition().y() + (this.barLength - this.scrollHeight));
            guiGraphics.blitSprite(barSprites.get(this.canScrollVertically(), false), this.getScrollbarPosition().x(), relativeY, this.scrollWidth, this.scrollHeight);
            return;
        }

        if (this.canScrollVertically()) {
            double delta = this.getScrollDistanceDelta(ScreenAxis.VERTICAL);
            float alpha = this.scrolling && !this.scrollOpposite || this.isMouseOverScrollbar(mouseX, mouseY) ? 0.85F : this.scrolling || this.isMouseOverLayoutRectangle(mouseX, mouseY) ? 0.2F : 0.1F;
            int barLeft = this.layoutRectangle().right() - this.scrollWidth;
            int barHeight = this.getScrollbarSize(ScreenAxis.VERTICAL);

            guiGraphics.fill(barLeft, layoutRectangle().top(), layoutRectangle().right(), layoutRectangle().bottom(), ColorUtils.RGBToRGBA(this.scrollBgColor, alpha));

            double endPoint = layoutRectangle().bottom() - barHeight;
            int barTop = (int) Mth.lerp(delta, layoutRectangle().top(), endPoint);
            guiGraphics.fill(barLeft + 1, barTop + 1, layoutRectangle().right() - 1, barTop + barHeight - 1, ColorUtils.RGBToRGBA(this.scrollColor, alpha));

            int brighterColor = new Color(this.scrollColor).brighter().getRGB();
            int darkerColor = new Color(this.scrollColor).darker().getRGB();
            guiGraphics.fill(barLeft, barTop, layoutRectangle().right(), barTop + 1, ColorUtils.RGBToRGBA(brighterColor, alpha));
            guiGraphics.fill(barLeft, barTop + 1, barLeft + 1, barTop + barHeight, ColorUtils.RGBToRGBA(brighterColor, alpha));
            guiGraphics.fill(layoutRectangle().right() - 1, barTop, layoutRectangle().right(), barTop + barHeight, ColorUtils.RGBToRGBA(darkerColor, alpha));
            guiGraphics.fill(barLeft, barTop + barHeight - 1, layoutRectangle().right() - 1, barTop + barHeight, ColorUtils.RGBToRGBA(darkerColor, alpha));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button))
            return true;

        if (this.isMouseOverLayoutRectangle(mouseX, mouseY)) {
            this.scrolling = true;
            this.scrollOpposite = this.scrollHorizontally || !this.isMouseOverScrollbar(mouseX, mouseY);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.scrolling) {
            this.scrolling = this.scrollOpposite = false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY))
            return true;

        if (this.scrolling) {
            double moved;
            if (this.scrollHorizontally) {
                moved = (-dragX) / this.getScrollbarSize(ScreenAxis.HORIZONTAL);
                this.scrollOffsetX += this.getMaxScrollDistance(ScreenAxis.HORIZONTAL) * moved;
            }
            double nDragY = this.scrollOpposite ? -dragY : dragY;
            moved = nDragY / this.getScrollbarSize(ScreenAxis.VERTICAL);
            this.scrollOffsetY += this.getMaxScrollDistance(ScreenAxis.VERTICAL) * moved;
            this.applyScrollLimits();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double dragX, double dragY) {
        if (super.mouseScrolled(mouseX, mouseY, dragX, dragY))
            return true;

        if (this.scrollHorizontally && dragX != 0) {
            this.scrollOffsetX += (float) (-dragX * this.getScrollStep(ScreenAxis.HORIZONTAL));
            this.applyScrollLimits();
            return true;
        }

        if (dragY != 0) {
            this.scrollOffsetY += (float) (-dragY * this.getScrollStep(ScreenAxis.VERTICAL));
            this.applyScrollLimits();
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.scrolling || super.isMouseOver(mouseX, mouseY);
    }

    public void setBorderColor(int color) {
        this.borderColor = color;
    }

    public void setScrollBgColor(int color) {
        this.scrollBgColor = color;
    }

    public void setScrollColor(int color) {
        this.scrollColor = color;
    }

    @Override
    protected @NotNull ScreenRectangle layoutRectangle() {
        return this.applyIfVisible(new ScreenRectangle(this.x + border, this.y + border, this.width - (border * 2), this.height - (border * 2)));
    }

    protected final ScreenPosition getRelativePosition() {
        return new ScreenPosition(this.layoutRectangle().left() - (int) this.scrollOffsetX, this.layoutRectangle().top() - (int) this.scrollOffsetY);
    }

    protected ScreenPosition getScrollbarPosition() {
        return new ScreenPosition(Math.max(0, layoutRectangle().right() - scrollWidth), layoutRectangle().top());
    }

    protected final boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        return MouseHelper.isMouseOver(mouseX, mouseY, getScrollbarPosition().x(), getScrollbarPosition().y(), this.scrollWidth, this.scrollHeight);
    }

    public void enableScrollingHorizontally() {
        this.scrollHorizontally = true;
    }

    protected final boolean canScrollHorizontally() {
        return this.canScroll(ScreenAxis.HORIZONTAL);
    }

    protected final boolean canScrollVertically() {
        return this.canScroll(ScreenAxis.VERTICAL);
    }

    protected final boolean canScroll(ScreenAxis axis) {
        return this.getExceededContentSize(axis) > 0;
    }

    protected int getScrollStep(ScreenAxis axis) {
        return Math.max(20, this.getMaxScrollDistance(axis) / 4);
    }

    protected final double getScrollDistanceDelta(ScreenAxis axis) {
        return axis == ScreenAxis.HORIZONTAL ? this.scrollOffsetX : this.scrollOffsetY / Math.max(1, this.getExceededContentSize(axis));
    }

    protected final int getMaxScrollDistance(ScreenAxis axis) {
        return Math.max(0, this.getContentSize(axis) - (axis == ScreenAxis.HORIZONTAL ? layoutRectangle().width() : layoutRectangle().height()));
    }

    private void applyScrollLimits() {
        int maxX = this.getMaxScrollDistance(ScreenAxis.HORIZONTAL);
        int maxY = this.getMaxScrollDistance(ScreenAxis.VERTICAL);
        if (maxX < 0) maxX /= 2;
        if (maxY < 0) maxY /= 2;
        this.scrollOffsetX = this.scrollHorizontally ? Math.max(0, Math.min(this.scrollOffsetX, maxX)) : 0;
        this.scrollOffsetY = Math.max(0, Math.min(this.scrollOffsetY, maxY));
    }

    private int getScrollbarSize(ScreenAxis axis) {
        int size = axis == ScreenAxis.HORIZONTAL ? layoutRectangle().width() : layoutRectangle().height();
        int barSize = (size * size) / Math.max(1, this.getContentSize(axis));
        return Math.min(size, Math.max(barSize, 32));
    }

    protected int getContentSize(ScreenAxis axis) {
        return axis == ScreenAxis.HORIZONTAL ? this.layout.getWidth() : this.layout.getHeight();
    }

    private int getExceededContentSize(ScreenAxis axis) {
        return this.getContentSize(axis) - (axis == ScreenAxis.HORIZONTAL ? this.layoutRectangle().width() : this.layoutRectangle().height());
    }
}
