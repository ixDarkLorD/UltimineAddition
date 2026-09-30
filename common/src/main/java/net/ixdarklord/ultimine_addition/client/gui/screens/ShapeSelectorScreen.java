package net.ixdarklord.ultimine_addition.client.gui.screens;

import net.ixdarklord.ultimine_addition.client.gui.theme.RecordTheme;
import net.ixdarklord.ultimine_addition.client.gui.GuiDraw;
import net.ixdarklord.coolcatcore.api.config.type.EnumType;
import net.ixdarklord.ultimine_addition.config.UAClientConfig;
import net.minecraft.world.entity.player.Player;
import net.ixdarklord.ultimine_addition.util.ARGB;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftbultimine.shape.Shape;
import net.ixdarklord.coolcatcanvas.api.client.gui.components.ColorableImageButton;
import net.ixdarklord.coolcatcanvas.api.client.utils.RenderUtils;
import net.ixdarklord.coolcatcore.api.utils.ColorUtils;
import net.ixdarklord.ultimine_addition.client.gui.components.ColoredButton;
import net.ixdarklord.ultimine_addition.common.data.item.SelectedShapeData;
import net.ixdarklord.ultimine_addition.common.menu.ShapeSelectorMenu;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.UpdateItemShapePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ShapeSelectorScreen extends AbstractContainerScreen<ShapeSelectorMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = FTBUltimineAddition.getGuiTexture("container/shape_selector", "png");
    private static final ResourceLocation BEACON_TEXTURE = new ResourceLocation("textures/gui/container/beacon.png");
    private SkillsRecordScreen.OverlayColor color;

    private ColoredButton filterButton;
    private AbstractStringWidget emptyString;
    private SelectBox selectBox;
    private ColorableImageButton setButton;
    private ColorableImageButton clearButton;

    public ShapeSelectorScreen(ShapeSelectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 178;
        this.imageHeight = 172;
    }

    @Override
    protected void init() {
        // Set before the first frame, so the background has its color from the start.
        this.color = RecordTheme.active().overlay();
        super.init();

        this.titleLabelX = 6;
        this.titleLabelY = 5;
        this.inventoryLabelX = (this.imageWidth / 2) - (this.font.width(this.playerInventoryTitle) / 2);
        this.inventoryLabelY = this.imageHeight - 96;

        this.filterButton = this.addWidget(new ColoredButton(this.leftPos + 165, this.topPos + 4, 9, 9, SkillsRecordScreen.CONFIGURATION_BUTTON_SPRITES,
                button -> {
                    Filter filter = UAClientConfig.SHAPE_SELECTOR_FILTER.get();
                    UAClientConfig.SHAPE_SELECTOR_FILTER.set(Screen.hasShiftDown() ? filter.previous() : filter.next());
                    UAClientConfig.CONFIG.save();
                    this.selectBox.refreshList();
                }, Component.empty(), tooltipInfo -> {
            MutableComponent filterComponent = UAClientConfig.SHAPE_SELECTOR_FILTER.get() == Filter.ALL
                    ? Component.translatable("gui.ultimine_addition.filter.all")
                    : Component.translatable("gui.ultimine_addition.filter.only_enabled");

            tooltipInfo.component = Component.translatable("gui.ultimine_addition.filter")
                    .append(": ")
                    .append(filterComponent)
                    .withStyle(ChatFormatting.WHITE);
        }));

        this.emptyString = this.addRenderableWidget(new AbstractStringWidget(this.leftPos + 61, this.topPos + 16, 102, 54,
                Component.translatable("gui.ultimine_addition.shape_selector.insert"), this.font) {

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                Component component = this.getMessage();
                Font font = this.getFont();
                int width = this.getWidth();

                List<FormattedCharSequence> sequences = font.split(component, width);
                int totalTextHeight = sequences.size() * font.lineHeight;

                int x = this.getX();
                int y = this.getY() + (this.getHeight() - totalTextHeight) / 2;

                for (FormattedCharSequence sequence : sequences) {
                    int textWidth = font.width(sequence);
                    int centeredX = x + (width - textWidth) / 2;
                    GuiDraw.text(guiGraphics, font, sequence, centeredX, y, Color.LIGHT_GRAY.getRGB());
                    y += font.lineHeight;
                }

            }
        });

        this.selectBox = this.addRenderableWidget(new SelectBox(this.leftPos + 61, this.topPos + 16, 102, 54));
        this.selectBox.visible = false;

        this.setButton = this.addRenderableWidget(new ColorableImageButton(this.leftPos + 10, this.topPos + 54, 19, 19, SkillsRecordScreen.BUTTON_SPRITES, button -> {
            SelectBox.ShapeEntry selected = this.selectBox.getSelected();
            if (selected != null) {
                PayloadHandler.sendToServer(new UpdateItemShapePayload(FTBUltimineIntegration.shapeId(selected.shape).toString()));
            }

        }) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                // 1.20.1 has no beacon sprites: the icon from the beacon screen's texture.
                GuiDraw.blit(guiGraphics, BEACON_TEXTURE, this.getX() + 1, this.getY(), 90, 220, 18, 18, 256, 256);
            }
        });

        this.clearButton = this.addRenderableWidget(new ColorableImageButton(this.leftPos + 32, this.topPos + 54, 19, 19, SkillsRecordScreen.BUTTON_SPRITES, button -> {
            this.selectBox.setSelected(null);
            PayloadHandler.sendToServer(new UpdateItemShapePayload(""));
        }) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                GuiDraw.blit(guiGraphics, BEACON_TEXTURE, this.getX() + 1, this.getY(), 112, 220, 18, 18, 256, 256);
            }
        });

        this.setButton.active = false;
        this.clearButton.active = false;
    }

    private void update() {
        ItemStack stack = this.menu.getSlot(0).getItem();
        boolean slotEmpty = stack.isEmpty();
        SelectBox.ShapeEntry selected = this.selectBox.getSelected();

        this.setButton.setColor(this.color.convert());
        this.clearButton.setColor(this.color.convert());

        this.emptyString.visible = slotEmpty;
        this.selectBox.visible = !slotEmpty;
        this.clearButton.active = !slotEmpty && Registration.SELECTED_SHAPE_COMPONENT.has(stack);
        this.setButton.active = !slotEmpty && selected != null && !selected.isShapeSelected();

        this.setButton.setTooltip(this.setButton.active ? Tooltip.create(Component.literal("➤ ").append(
                Component.translatable("gui.ultimine_addition.action.set"))) : null);
        this.clearButton.setTooltip(this.clearButton.active ? Tooltip.create(Component.literal("➤ ").append(
                Component.translatable("gui.ultimine_addition.action.clear"))) : null);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.color = RecordTheme.active().overlay();
        this.update();
        // 1.20.1 screens draw their dimmed background themselves.
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.filterButton.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        GuiDraw.renderTooltip(guiGraphics);
    }

    private Player mc() {
        return Objects.requireNonNull(Objects.requireNonNull(this.minecraft).player);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        SelectBox.ShapeEntry entry = this.selectBox.getHovered();
        if (entry == null) return;

        List<Component> components = new ArrayList<>();

        if (!entry.isAllowed()) {
            components.add(Component.translatable("gui.ultimine_addition.shape_selector.blacklisted").withStyle(ChatFormatting.RED));
        } else if (entry.isShapeSelected()) {
            components.add(Component.translatable("gui.ultimine_addition.shape_selector.selected").withStyle(style -> style.withColor(0xA0DA3E)));
            components.add(Component.literal("- ").append(FTBUltimineIntegration.shapeName(entry.shape)));
        }
        if (entry.isAllowed() && FTBUltimineIntegration.isShapeNotLearned(mc(), this.menu.getSlot(0).getItem(), entry.shape)) {
            components.add(Component.translatable("gui.ultimine_addition.shape_selector.not_learned").withStyle(ChatFormatting.GOLD));
        }

        Minecraft mc = Objects.requireNonNull(minecraft);
        if (mc.options.advancedItemTooltips && hasShiftDown()) {
            if (!components.isEmpty()) components.add(Component.empty());
            components.add(Component.literal("Shape ID: ").append(FTBUltimineIntegration.shapeId(entry.shape).toString()).withStyle(ChatFormatting.GRAY));
        }

        if (!components.isEmpty())
            GuiDraw.tooltip(guiGraphics, font, components, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // AbstractContainerScreen doesn't pass scrolling on to its widgets.
        if (this.selectBox.visible && this.selectBox.isMouseOver(mouseX, mouseY)
                && this.selectBox.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        GuiDraw.blit(guiGraphics, BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256,
                ARGB.colorFromFloat(color.alpha(), color.red(), color.green(), color.blue()));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Color color = ColorUtils.blend(new Color(0, 0, 0), this.color.convert(), 0.25);
        GuiDraw.text(guiGraphics, this.font, this.title, this.titleLabelX, this.titleLabelY, color.getRGB(), false);
        guiGraphics.fill(this.inventoryLabelX - 1, this.inventoryLabelY - 1, this.inventoryLabelX + this.font.width(this.playerInventoryTitle), this.inventoryLabelY + this.font.lineHeight, ColorUtils.rgbToRgba(color.getRGB(), 0.5F));
        GuiDraw.text(guiGraphics, this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, color.getRGB(), false);
    }

    public enum Filter implements EnumType.Displayable {
        ALL,
        ENABLED_SHAPES;

        @Override
        public Component displayName() {
            return Component.translatable(this == ALL ? "gui.ultimine_addition.filter.all" : "gui.ultimine_addition.filter.only_enabled");
        }

        public Filter next() {
            int nextIndex = (this.ordinal() + 1) % Filter.values().length;
            return Filter.values()[nextIndex];
        }

        public Filter previous() {
            int prevIndex = (this.ordinal() - 1 + Filter.values().length) % Filter.values().length;
            return Filter.values()[prevIndex];
        }
    }

    // 1.20.1's selection lists have no visibility flag and no background toggles of the newer kind.
    private class SelectBox extends ObjectSelectionList<SelectBox.ShapeEntry> {
        private boolean visible = true;

        public SelectBox(int x, int y, int width, int height) {
            super(Minecraft.getInstance(), width, height, y, y + height, 20);
            this.setLeftPos(x);
            this.refreshList();
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (this.visible) super.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.visible && super.isMouseOver(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.visible && super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return this.visible && super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        public void refreshList() {
            this.clearEntries();
            for (Shape shape : FTBUltimineIntegration.getShapesList()) {
                if (UAClientConfig.SHAPE_SELECTOR_FILTER.get() == Filter.ENABLED_SHAPES) {
                    if (!FTBUltimineIntegration.getEnabledShapes().contains(shape)) {
                        continue;
                    }
                }
                this.addEntry(new ShapeEntry(shape));
            }
            this.setScrollAmount(this.getScrollAmount());
        }

        @Override
        public @Nullable ShapeEntry getHovered() {
            return super.getHovered();
        }

        @Override
        public int getRowLeft() {
            return this.x0 + this.width / 2 - this.getRowWidth() / 2;
        }

        @Override
        public int getRowWidth() {
            return this.width;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.x0 + width + 4;
        }

        @Override
        protected void renderSelection(GuiGraphics guiGraphics, int top, int width, int height, int outlineColor, int innerColor) {
            int left = this.x0 + (this.width - width) / 2;
            ScreenRectangle rectangle = new ScreenRectangle(left, top - 1, width, height + 2);
            RenderUtils.drawHollowRect(guiGraphics, rectangle, 1, outlineColor);
        }

        @Override
        // Drawn over the list's own scrollbar, tinted like the book.
        protected void renderDecorations(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (this.getMaxScroll() > 0) {
                int scrollerHeight = Mth.clamp((int) ((float) (this.height * this.height) / this.getMaxPosition()), 32, this.height - 8);
                int scrollerY = Math.max(this.y0, (int) this.getScrollAmount() * (this.height - scrollerHeight) / this.getMaxScroll() + this.y0);
                // 1.20.1 has no scroller sprites: drawn like its own list scrollbar.
                int x = this.getScrollbarPosition();
                int color = ShapeSelectorScreen.this.color.convert().brighter().getRGB();
                guiGraphics.fill(x, this.y0, x + 6, this.y0 + this.height, ARGB.opaque(0));
                guiGraphics.fill(x, scrollerY, x + 6, scrollerY + scrollerHeight, ARGB.multiply(ARGB.opaque(0x808080), ARGB.opaque(color)));
                guiGraphics.fill(x, scrollerY, x + 5, scrollerY + scrollerHeight - 1, ARGB.multiply(ARGB.opaque(0xC0C0C0), ARGB.opaque(color)));
            }
        }

        private class ShapeEntry extends ObjectSelectionList.Entry<ShapeEntry> {
            private final Shape shape;

            private ShapeEntry(Shape shape) {
                this.shape = shape;
            }

            private boolean isAllowed() {
                return FTBUltimineIntegration.getEnabledShapes().contains(this.shape);
            }

            private boolean isShapeSelected() {
                ItemStack stack = ShapeSelectorScreen.this.menu.getSlot(0).getItem();
                SelectedShapeData shapeData = Registration.SELECTED_SHAPE_COMPONENT.get(stack);
                return shapeData != null && shape == shapeData.shape();
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                SoundManager handler = Minecraft.getInstance().getSoundManager();
                if (!isAllowed()) {
                    handler.play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS, .5F));
                    return false;
                }
                handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            // The list hands each row its box: the row height minus a 4px gap below it.
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                Color bgColor = new Color(0xBCBCBC);
                Color borderColor = new Color(0xB5B5B5);

                if (!isAllowed()) {
                    bgColor = ColorUtils.blend(bgColor.darker(), new Color(0x701111), 0.4);
                    borderColor = ColorUtils.blend(borderColor, new Color(0x701111), 0.4);
                } else if (isShapeSelected()) {
                    bgColor = ColorUtils.blend(bgColor.darker(), new Color(0xA0DA3E), 0.4);
                    borderColor = ColorUtils.blend(borderColor, new Color(0xA0DA3E), 0.4);
                }

                float alpha = (isAllowed() && isHovered) || isShapeSelected() ? 0.8F : 0.5F;
                guiGraphics.fill(left, top, left + width, top + height, ColorUtils.rgbToRgba(bgColor.getRGB(), alpha));
                RenderUtils.drawHollowRect(guiGraphics, new ScreenRectangle(left, top, width, height), 1, borderColor.getRGB());

                Font font = SelectBox.this.minecraft.font;
                Color color = isAllowed() ? Color.WHITE : new Color(0xD13E3E);
                int ticks = (int) (Util.getMillis() / 10);
                Style style = Style.EMPTY.withStrikethrough(!isAllowed());
                int spacing = isShapeSelected() ? 9 : 0;
                if (isShapeSelected()) {
                    GuiDraw.text(guiGraphics, font, Component.literal("➤"), left + 3, top + (height / 2) - 4, color.getRGB());
                }
                RenderUtils.drawScrollingString(guiGraphics, ticks, font, FTBUltimineIntegration.shapeName(this.shape).copy().withStyle(style), false, new ScreenRectangle(left + spacing, top, width - spacing, height), 3, color.getRGB(), true);
            }

            public @NotNull Component getNarration() {
                return FTBUltimineIntegration.shapeName(shape);
            }
        }
    }
}
