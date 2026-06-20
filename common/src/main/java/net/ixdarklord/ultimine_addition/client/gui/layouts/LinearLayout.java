package net.ixdarklord.ultimine_addition.client.gui.layouts;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class LinearLayout extends AbstractLayout {
    private final GridLayout wrapped;
    private final Orientation orientation;
    private int nextChildIndex;

    private LinearLayout(Orientation orientation) {
        this(0, 0, orientation);
    }

    public LinearLayout(int width, int height, Orientation orientation) {
        super(0, 0, width, height);
        this.nextChildIndex = 0;
        this.wrapped = new GridLayout(width, height);
        this.orientation = orientation;
    }

    public LinearLayout spacing(int spacing) {
        this.orientation.setSpacing(this.wrapped, spacing);
        return this;
    }

    public LayoutSettings newCellSettings() {
        return this.wrapped.newCellSettings();
    }

    public LayoutSettings defaultCellSetting() {
        return this.wrapped.defaultCellSetting();
    }

    public <T extends LayoutElement> T addChild(T child, LayoutSettings layoutSettings) {
        return this.orientation.addChild(this.wrapped, child, this.nextChildIndex++, layoutSettings);
    }

    public <T extends LayoutElement> T addChild(T child) {
        return this.addChild(child, this.newCellSettings());
    }

    public <T extends LayoutElement> T addChild(T child, Consumer<LayoutSettings> layoutSettingsFactory) {
        return this.orientation.addChild(this.wrapped, child, this.nextChildIndex++, Util.make(this.newCellSettings(), layoutSettingsFactory));
    }

    public void visitChildren(Consumer<LayoutElement> visitor) {
        this.wrapped.visitChildren(visitor);
    }

    public void arrangeElements() {
        this.wrapped.arrangeElements();
    }

    public int getWidth() {
        return this.wrapped.getWidth();
    }

    public int getHeight() {
        return this.wrapped.getHeight();
    }

    public void setX(int x) {
        this.wrapped.setX(x);
    }

    public void setY(int y) {
        this.wrapped.setY(y);
    }

    public int getX() {
        return this.wrapped.getX();
    }

    public int getY() {
        return this.wrapped.getY();
    }

    public static LinearLayout vertical() {
        return new LinearLayout(LinearLayout.Orientation.VERTICAL);
    }

    public static LinearLayout horizontal() {
        return new LinearLayout(LinearLayout.Orientation.HORIZONTAL);
    }

    @Environment(EnvType.CLIENT)
    public enum Orientation {
        HORIZONTAL,
        VERTICAL;

        void setSpacing(GridLayout layout, int spacing) {
            switch (this.ordinal()) {
                case 0 -> layout.columnSpacing(spacing);
                case 1 -> layout.rowSpacing(spacing);
            }

        }

        public <T extends LayoutElement> T addChild(GridLayout layout, T element, int index, LayoutSettings layoutSettings) {
            LayoutElement layoutElement;
            layoutElement = switch (this.ordinal()) {
                case 0 -> layout.addChild(element, 0, index, layoutSettings);
                case 1 -> layout.addChild(element, index, 0, layoutSettings);
                default -> throw new IllegalArgumentException(null, null);
            };

            return (T) layoutElement;
        }
    }
}
