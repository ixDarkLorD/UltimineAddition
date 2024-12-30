package net.ixdarklord.ultimine_addition.client.gui.components;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.coolcatlib.api.util.ColorUtils;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public abstract class AbstractMultiPanelWidget extends AbstractScrollableWidget {
    private final PanelManager manager;
    protected Color dividerColor;

    public AbstractMultiPanelWidget(int x, int y, int width, int height, int amountOfPanels, boolean isMovable) {
        super(x, y, width, height, 0, isMovable);
        this.manager = new PanelManager(this, amountOfPanels);
        this.dividerColor = new Color(0x80000000, true);
    }

    protected <V extends GuiEventListener & NarratableEntry> V addWidget(V listener) {
        this.manager.getSelectedComponent().guiEventListeners.add(listener);
        return super.addWidget(listener);
    }

    public <V extends LayoutElement> V addToHeader(V child) {
        return addToHeader(child, this.manager.getSelectedComponent().headerFrame.defaultChildLayoutSetting());
    }

    public <V extends LayoutElement> V addToHeader(V child, LayoutSettings layoutSettings) {
        this.manager.getSelectedComponent().headerFrame.addChild(child, layoutSettings);
        return child;
    }

    public <V extends LayoutElement> V addToContents(V child) {
        return addToContents(child, this.manager.getSelectedComponent().contentsFrame.defaultChildLayoutSetting());
    }

    public <V extends LayoutElement> V addToContents(V child, LayoutSettings layoutSettings) {
        this.manager.getSelectedComponent().contentsFrame.addChild(child, layoutSettings);
        return child;
    }

    public <V extends LayoutElement> V addToFooter(V child) {
        return addToFooter(child, this.manager.getSelectedComponent().footerFrame.defaultChildLayoutSetting());
    }

    public <V extends LayoutElement> V addToFooter(V child, LayoutSettings layoutSettings) {
        this.manager.getSelectedComponent().footerFrame.addChild(child, layoutSettings);
        return child;
    }

    public void setHeaderDownPadding(int padding) {
        setHeaderDownPadding(this.manager.selectedIndex, padding);
    }

    public void setHeaderDownPadding(int index, int padding) {
        this.validatePanelIndex(index);
        this.manager.components.get(index).headerDownPadding = padding;
    }

    public void setFooterTopPadding(int padding) {
        setFooterTopPadding(this.manager.selectedIndex, padding);
    }

    public void setFooterTopPadding(int index, int padding) {
        this.validatePanelIndex(index);
        this.manager.components.get(index).footerTopPadding = padding;
    }

    public void selectFirstPanel() {
        this.selectPanel(0);
    }

    public void selectLastPanel() {
        this.selectPanel(this.manager.components.size() - 1);
    }

    public void selectPanel(int index) {
        this.validatePanelIndex(index);
        this.manager.selectedIndex = index;

    }

    public void shouldRender(boolean state) {
        this.manager.getSelectedComponent().render = state;
        if (state) for (int i = 0; i < this.manager.components.size(); i++) {
            if (i > this.manager.selectedIndex) {
                this.manager.components.get(i).render = false;
            }
        }
    }

    public boolean isControlling() {
        return this.manager.getSelectedComponent().control;
    }

    public void setControl(boolean state) {
        PanelComponent panel = this.manager.getSelectedComponent();
        panel.control = panel.render && state;

        if (panel.control) {
            this.PreservedPosPanel();
            for (PanelComponent component : this.manager.components) {
                if (!component.equals(panel)) component.control = false;
            }
        }
    }

    public void setDividerColor(Color color) {
        this.dividerColor = color;
    }

    @Override
    protected void postInit() {
        this.layout.addChild(manager);
        super.postInit();
    }

    @Override
    protected void updateChildren() {
        super.updateChildren();
        this.manager.updateSize();
        this.manager.getSelectedComponent().cacheScrollOffsets(this.scrollOffsetX, this.scrollOffsetY);
        for (int i = 0; i < this.manager.components.size(); i++) {
            PanelComponent panel = this.manager.components.get(i);
            if (!this.isDebug()) {
                panel.control = i == this.manager.selectedIndex;
            }
            panel.updateWidgets(this.visible);
        }
    }

    @Override
    protected void renderScrollableContents(GuiGraphics guiGraphics, float partialTick, int relativeX, int relativeY, int mouseX, int mouseY) {
        var components = this.manager.components;
        for (int i = 0; i < components.size(); i++) {
            PanelComponent component = components.get(i);
            for (GuiEventListener listener : component.guiEventListeners) {
                if (listener instanceof Renderable renderable) {
                    renderable.render(guiGraphics, mouseX, mouseY, partialTick);
                }
            }

            int enabledPanels = components.stream().filter(c -> c.render).toList().size();
            if (enabledPanels > 1 && i < components.size() - 1) {
                guiGraphics.fill(layoutRectangle().left(), layoutRectangle().top(), layoutRectangle().right(), layoutRectangle().bottom(), ColorUtils.RGBToRGBA(this.dividerColor.getRGB(), this.dividerColor.getAlpha() / 255F));
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isDebug() && keyCode == GLFW.GLFW_KEY_F3) {
            int i = this.manager.selectedIndex;
            this.selectPanel((this.manager.selectedIndex + 1) % this.manager.components.size());
            this.shouldRender(true);
            this.setControl(true);
            UltimineAddition.LOGGER.info("Cycled index from: %s to: %s".formatted(i, this.manager.selectedIndex));
            return true;
        }
        if (this.isDebug() && keyCode == GLFW.GLFW_KEY_F4) {
            this.setControl(!this.isControlling());
            UltimineAddition.LOGGER.info("Selected Panel: %s | Control: %s".formatted(manager.selectedIndex, this.isControlling()));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected List<Component> getDebugInfo() {
        List<Component> components = Lists.newArrayList();
        components.add(Component.literal("Panels Amount: " + manager.components.size()));
        components.add(Component.literal("Rendered Panels: " + manager.components.stream().filter(c -> c.render).toList().size()));
        components.add(Component.literal("Selected Panel: " + manager.selectedIndex));
        components.add(Component.literal("Panel Control: " + manager.getSelectedComponent().control));
        components.add(Component.literal("Scroll Offset X: " + this.scrollOffsetX));
        components.add(Component.literal("Scroll Offset Y: " + this.scrollOffsetY));
        return components;
    }

    private void PreservedPosPanel() {
        this.scrollOffsetX = this.manager.getSelectedComponent().scrollOffsets[0];
        this.scrollOffsetY = this.manager.getSelectedComponent().scrollOffsets[1];
        this.layout.setPosition(layoutRectangle().left(), layoutRectangle().top());
        this.manager.setPosition(layoutRectangle().left(), layoutRectangle().top());
        this.manager.arrangeElements();
        this.layout.arrangeElements();
    }

    private void validatePanelIndex(int index) {
        if (index < 0 || index >= manager.components.size()) {
            throw new IndexOutOfBoundsException("Panel index is out of range: " + index);
        }
    }

    private static class PanelManager extends AbstractLayout {
        private boolean arranged;
        private int selectedIndex;
        protected List<PanelComponent> components;

        PanelManager(AbstractMultiPanelWidget widget, int amountOfPanels) {
            super(widget.layoutRectangle().left(), widget.layoutRectangle().top(), 0, 0);

            if (amountOfPanels < 1)
                throw new IllegalArgumentException("Panels amount must be greater than 1!");

            this.components = this.createPanels(widget, amountOfPanels);
        }

        @Override
        public void visitChildren(Consumer<LayoutElement> visitor) {
            this.getSelectedComponent().visitChildren(visitor);
        }

        @Override
        public void arrangeElements() {
            if (!arranged) {
                for (PanelComponent component : components) {
                    component.arrangeElements();
                }
                arranged = true;
            } else this.getSelectedComponent().arrangeElements();
            this.updateSize();
        }

        private void updateSize() {
            this.width = this.getSelectedComponent().getWidth();
            this.height = this.getSelectedComponent().getHeight();
        }

        private PanelComponent getSelectedComponent() {
            return this.components.get(this.selectedIndex);
        }

        private List<PanelComponent> createPanels(AbstractMultiPanelWidget widget, int amountOfPanels) {
            return Util.make(() -> {
                List<PanelComponent> list = new ArrayList<>();
                for (int i = 0; i < amountOfPanels; i++) {
                    list.add(new PanelComponent(widget, i, i == 0));
                }
                list.sort(PanelComponent::compareTo);
                return Collections.unmodifiableList(list);
            });
        }
    }

    private static class PanelComponent extends AbstractLayout implements Comparable<PanelComponent> {
        private boolean render;
        private boolean control;
        private final int renderOrder;
        private final List<GuiEventListener> guiEventListeners;
        private final double[] scrollOffsets;
        private final FrameLayout headerFrame;
        private final FrameLayout contentsFrame;
        private final FrameLayout footerFrame;
        private int headerDownPadding;
        private int footerTopPadding;

        PanelComponent(AbstractMultiPanelWidget widget, int renderOrder, boolean shouldRender) {
            super(widget.layoutRectangle().left(), widget.layoutRectangle().top(), 0, 0);
            this.render = shouldRender;
            this.control = render;
            this.renderOrder = renderOrder;
            this.guiEventListeners = Lists.newArrayList();
            this.scrollOffsets = new double[]{0.0, 0.0};

            this.headerDownPadding = 10;
            this.footerTopPadding = 10;
            this.headerFrame = new FrameLayout();
            this.contentsFrame = new FrameLayout();
            this.footerFrame = new FrameLayout();
        }

        @Override
        public void visitChildren(Consumer<LayoutElement> visitor) {
            this.headerFrame.visitChildren(visitor);
            this.contentsFrame.visitChildren(visitor);
            this.footerFrame.visitChildren(visitor);
        }

        @Override
        public void arrangeElements() {
            int headerDownPadding = this.headerDownPadding;
            int footerTopPadding = this.footerTopPadding;

            this.headerFrame.arrangeElements();
            this.contentsFrame.arrangeElements();
            this.footerFrame.arrangeElements();

            if (this.headerFrame.getHeight() <= 0) {
                headerDownPadding = 0;
            }

            if (this.footerFrame.getHeight() <= 0) {
                footerTopPadding = 0;
            }

            int maxWidth = Math.max(this.headerFrame.getWidth(), Math.max(this.contentsFrame.getWidth(), this.footerFrame.getWidth()));
            int maxHeight = this.headerFrame.getHeight() + headerDownPadding + this.contentsFrame.getHeight() + footerTopPadding + this.footerFrame.getHeight();

            this.width = maxWidth;
            this.height = maxHeight;

            this.headerFrame.setMinWidth(maxWidth);
            this.contentsFrame.setMinWidth(maxWidth);
            this.footerFrame.setMinWidth(maxWidth);

            this.headerFrame.arrangeElements();
            this.contentsFrame.arrangeElements();
            this.footerFrame.arrangeElements();

            this.headerFrame.setPosition(this.getX(), this.getY());
            this.contentsFrame.setPosition(this.getX(), this.getY() + this.headerFrame.getHeight() + headerDownPadding);
            this.footerFrame.setPosition(this.getX(), this.getY() + maxHeight - this.footerFrame.getHeight());
        }

        private void updateWidgets(boolean visibility) {
            for (GuiEventListener listener : this.guiEventListeners) {
                if (listener instanceof AbstractWidget widget) {
                    widget.visible = visibility && render;
                    widget.active = widget.visible && control;
                }
            }
        }

        private void cacheScrollOffsets(double offsetX, double offsetY) {
            if (control) {
                this.scrollOffsets[0] = offsetX;
                this.scrollOffsets[1] = offsetY;
            }
        }

        @Override
        public int compareTo(@NotNull AbstractMultiPanelWidget.PanelComponent other) {
            return Integer.compare(this.renderOrder, other.renderOrder);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PanelComponent that)) return false;
            return renderOrder == that.renderOrder;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(renderOrder);
        }
    }
}
