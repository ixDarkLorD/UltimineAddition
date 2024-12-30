package net.ixdarklord.ultimine_addition.client.gui.components;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.coolcatlib.api.util.ColorUtils;
import net.ixdarklord.coolcatlib.api.util.KeysUtils;
import net.ixdarklord.coolcatlib.api.util.MouseHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class AbstractDraggableWidget extends AbstractContainerEventHandler implements Renderable, NarratableEntry, MovableElement {
    protected final Minecraft minecraft;
    protected final Font font;
    private boolean debug;
    protected boolean visible = true;
    private boolean initialized;
    protected StringWidget title;
    protected int x;
    protected int y;
    private final ScreenPosition originalPos;
    private int xMO;
    private int yMO;
    protected int width;
    protected int height;
    protected float blitOffset;
    protected final boolean movable;
    private boolean isDraggingComponent;
    protected float tickCount;
    private float lastStamp;
    private final List<GuiEventListener> children = Lists.newArrayList();
    private final List<Renderable> renderables = Lists.newArrayList();
    private final List<NarratableEntry> narratables = Lists.newArrayList();
    protected final FrameLayout layout;

    public AbstractDraggableWidget(Component title, int x, int y, int width, int height, boolean isMovable) {
        this.minecraft = Minecraft.getInstance();
        this.font = minecraft.font;
        this.title = new StringWidget(title, this.font);
        this.x = x;
        this.y = y;
        this.originalPos = new ScreenPosition(x, y);
        this.width = width;
        this.height = height;
        this.movable = isMovable;
        this.layout = new FrameLayout();
    }

    private void tick(float partialTick) {
        if (partialTick < this.lastStamp) {
            this.tickCount += 1.0F - this.lastStamp;
            this.tickCount += partialTick;
        } else {
            this.tickCount += partialTick - this.lastStamp;
        }
        this.lastStamp = partialTick;
        this.updateChildren();
    }

    protected void updateChildren() {
        this.title.setPosition(this.x, this.y);
        this.layout.setPosition(this.layoutRectangle().left(), this.layoutRectangle().top());
        for (GuiEventListener child : this.children) {
            if (child instanceof AbstractWidget widget) {
                widget.active = this.visible;
                widget.visible = this.visible;
            }
        }
    }

    protected abstract void init();

    protected void postInit() {
        this.layout.setMinDimensions(this.layoutRectangle().width(), this.layoutRectangle().height());
        this.layout.arrangeElements();
    }

    protected abstract void renderBackground(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY);

    protected void renderLabels(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if (this.title.getMessage() != CommonComponents.EMPTY) {
            this.title.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    protected void renderDraggingBoxHighlight(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.isMouseOverDraggingRectangle(mouseX, mouseY)) {
            guiGraphics.setColor(this.getDraggingAreaColor().getRed(), this.getDraggingAreaColor().getGreen(), this.getDraggingAreaColor().getBlue(), this.getDraggingAreaColor().getAlpha());
            guiGraphics.fill(this.getDraggingRectangle().left(), this.getDraggingRectangle().top(), this.getDraggingRectangle().right(), this.getDraggingRectangle().bottom(), ColorUtils.RGBToRGBA(Color.WHITE.getRGB(), 0.25F));
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    protected void renderContents(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    protected void renderDebugInfo(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if (!this.isDebug()) return;
        List<Component> components = Lists.newArrayList(Component.literal("Debug Mode: ON").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        components.addAll(this.getDebugInfo());

        int x = mouseX - 10;
        int minY = mouseY - 10;
        int maxY = minY + (9 * components.size());
        int textWidth = components.stream().mapToInt(this.font::width).max().orElse(this.width);
        guiGraphics.fill(x - textWidth - 2, minY, x - 1, maxY, ColorUtils.RGBToRGBA(Color.GRAY.getRGB(), 0.5F));

        for (int i = 0; i < components.size(); i++) {
            Component component = components.get(i);
            TextColor color = component.getStyle().getColor();
            guiGraphics.drawString(this.font, component, x - textWidth - 1, minY + 1 + (9 * i), color != null ? color.getValue() : Color.WHITE.getRGB(), false);
        }
    }

    public abstract @NotNull ScreenRectangle getDraggingRectangle();

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.initialized) {
            this.rebuildWidgets();
            return;
        }

        if (!this.visible) return;
        this.tick(partialTick);

        PoseStack stack = guiGraphics.pose();
        stack.pushPose();
        stack.translate(0F, 0F, this.blitOffset);
        this.renderBackground(guiGraphics, partialTick, mouseX, mouseY);
        this.renderLabels(guiGraphics, partialTick, mouseX, mouseY);
        this.renderDraggingBoxHighlight(guiGraphics, mouseX, mouseY);
        this.renderContents(guiGraphics, partialTick, mouseX, mouseY);
        this.renderDebugInfo(guiGraphics, partialTick, mouseX, mouseY);
        stack.popPose();
    }

    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        this.renderables.add(widget);
        return this.addWidget(widget);
    }

    protected <T extends Renderable> T addRenderableOnly(T renderable) {
        this.renderables.add(renderable);
        return renderable;
    }

    protected <T extends GuiEventListener & NarratableEntry> T addWidget(T listener) {
        this.children.add(listener);
        this.narratables.add(listener);
        return listener;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.clearFocus();
        if (!this.visible) return false;
        if (super.mouseClicked(mouseX, mouseY, button))
            return true;

        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && this.movable && this.isMouseOverDraggingRectangle(mouseX, mouseY)) {
            this.xMO = (int) Math.abs(this.x - mouseX);
            this.yMO = (int) Math.abs(this.y - mouseY);
            this.isDraggingComponent = true;
            this.playDownSound(this.minecraft.getSoundManager());
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && this.isDraggingComponent) {
            this.xMO = this.yMO = 0;
            this.isDraggingComponent = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!this.visible) return false;
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY))
            return true;

        if (this.visible) {
            if (this.isDraggingComponent) {
                return this.moveTo((int) mouseX - this.xMO, (int) mouseY - this.yMO);
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.visible) return false;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Platform.isDevelopmentEnvironment() && KeysUtils.isHolden3ComboButtons() && keyCode == GLFW.GLFW_KEY_F12) {
            this.debug ^= true;
            return true;
        }
        if (!this.visible) return false;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!this.visible) return false;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.isDraggingComponent || MouseHelper.isMouseOver(mouseX, mouseY, this.x, this.y, this.width, this.height);
    }

    public final boolean isMouseOverLayoutRectangle(double mouseX, double mouseY) {
        if (this.layoutRectangle() == ScreenRectangle.empty())
            return false;
        return MouseHelper.isMouseOver(mouseX, mouseY, this.layoutRectangle().left(), this.layoutRectangle().top(), this.layoutRectangle().width(), this.layoutRectangle().height());
    }

    public final boolean isMouseOverDraggingRectangle(double mouseX, double mouseY) {
        if (this.getDraggingRectangle() == ScreenRectangle.empty())
            return false;

        return MouseHelper.isMouseOver(mouseX, mouseY, this.getDraggingRectangle().left(), this.getDraggingRectangle().top(), this.getDraggingRectangle().width(), this.getDraggingRectangle().height());
    }

    protected void removeWidget(GuiEventListener listener) {
        if (listener instanceof Renderable) {
            this.renderables.remove((Renderable) listener);
        }

        if (listener instanceof NarratableEntry) {
            this.narratables.remove((NarratableEntry) listener);
        }

        this.children.remove(listener);
    }

    protected void clearWidgets() {
        this.renderables.clear();
        this.children.clear();
        this.narratables.clear();
    }

    protected void rebuildWidgets() {
        this.clearWidgets();
        this.clearFocus();
        this.init();
        this.initialized = true;
        this.postInit();
        this.setInitialFocus();
    }

    protected void setInitialFocus() {
        if (this.minecraft.getLastInputType().isKeyboard()) {
            FocusNavigationEvent.TabNavigation tabNavigation = new FocusNavigationEvent.TabNavigation(true);
            ComponentPath componentPath = super.nextFocusPath(tabNavigation);
            if (componentPath != null) {
                this.changeFocus(componentPath);
            }
        }
    }

    protected void setInitialFocus(GuiEventListener listener) {
        ComponentPath componentPath = ComponentPath.path(this, listener.nextFocusPath(new FocusNavigationEvent.InitialFocus()));
        if (componentPath != null) {
            this.changeFocus(componentPath);
        }
    }

    @VisibleForTesting
    protected void changeFocus(ComponentPath path) {
        this.clearFocus();
        path.applyFocus(true);
    }

    public void clearFocus() {
        ComponentPath componentPath = this.getCurrentFocusPath();
        if (componentPath != null) {
            componentPath.applyFocus(false);
        }
    }

    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public boolean isInitialized() {
        return initialized;
    }

    protected final boolean isDebug() {
        return debug;
    }

    protected List<Component> getDebugInfo() {
        return Lists.newArrayList();
    }

    public StringWidget getTitle() {
        return title;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public boolean moveTo(int x, int y) {
        boolean ret = this.x != x || this.y != y;
        this.setX(x);
        this.setY(y);
        return ret;
    }

    public void toggleVisibility() {
        this.toggleVisibility(false);
    }

    public void toggleVisibility(boolean moveToOriginalPos) {
        this.setVisible(!this.isVisible());
        if (!this.isVisible() && moveToOriginalPos)
            this.moveTo(this.originalPos);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        this.clearFocus();
    }

    protected List<LayoutElement> getElements() {
        return this.children.stream()
                .filter(listener -> listener instanceof LayoutElement)
                .map(listener -> (LayoutElement) listener)
                .toList();
    }

    protected List<AbstractButton> getButtons() {
        return this.getElements().stream()
                .filter(listener -> listener instanceof AbstractButton)
                .map(listener -> (AbstractButton) listener)
                .toList();
    }

    public ColorUtils getDraggingAreaColor() {
        return new ColorUtils(Color.WHITE.getRGB());
    }

    @Override
    public @NotNull ScreenRectangle getRectangle() {
        return applyIfVisible(new ScreenRectangle(this.x, this.y, this.width, this.height));
    }

    protected abstract @NotNull ScreenRectangle layoutRectangle();

    protected final ScreenRectangle applyIfVisible(ScreenRectangle rectangle) {
        return this.visible || !this.initialized ? rectangle : ScreenRectangle.empty();
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        return this.visible ? this.isFocused() ? NarrationPriority.FOCUSED : NarrationPriority.HOVERED : NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        Screen.NarratableSearchResult narratableSearchResult = Screen.findNarratableWidget(this.narratables, null);
        if (narratableSearchResult != null) {
            narratableSearchResult.entry.updateNarration(narrationElementOutput.nest());
        }
    }
}
