package net.ixdarklord.ultimine_addition.client.gui.screens;

import net.ixdarklord.coolcatcore.api.config.type.EnumType;
import net.ixdarklord.ultimine_addition.config.UAClientConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.ARGB;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
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
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
import net.minecraft.resources.Identifier;
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
    private static final Identifier BACKGROUND_TEXTURE = FTBUltimineAddition.getGuiTexture("container/shape_selector", "png");
    private SkillsRecordScreen.OverlayColor color;

    private ColoredButton filterButton;
    private AbstractStringWidget emptyString;
    private SelectBox selectBox;
    private ColorableImageButton setButton;
    private ColorableImageButton clearButton;

    public ShapeSelectorScreen(ShapeSelectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 178, 172);
    }

    @Override
    protected void init() {
        // Set before the first frame: 26.1 draws the background before extractRenderState runs.
        this.color = UAClientConfig.backgroundColor();
        super.init();

        this.titleLabelX = 6;
        this.titleLabelY = 5;
        this.inventoryLabelX = (this.imageWidth / 2) - (this.font.width(this.playerInventoryTitle) / 2);
        this.inventoryLabelY = this.imageHeight - 96;

        this.filterButton = this.addWidget(new ColoredButton(this.leftPos + 165, this.topPos + 4, 9, 9, SkillsRecordScreen.CONFIGURATION_BUTTON_SPRITES,
                button -> {
                    Filter filter = UAClientConfig.SHAPE_SELECTOR_FILTER.get();
                    UAClientConfig.SHAPE_SELECTOR_FILTER.set(Minecraft.getInstance().hasShiftDown() ? filter.previous() : filter.next());
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
            public void visitLines(ActiveTextCollector output) {
            }

            @Override
            public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
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
                    guiGraphics.text(font, sequence, centeredX, y, Color.LIGHT_GRAY.getRGB());
                    y += font.lineHeight;
                }

            }
        });

        this.selectBox = this.addRenderableWidget(new SelectBox(this.leftPos + 61, this.topPos + 16, 102, 54));
        this.selectBox.visible = false;

        this.setButton = this.addRenderableWidget(new ColorableImageButton(this.leftPos + 10, this.topPos + 54, 19, 19, SkillsRecordScreen.BUTTON_SPRITES, button -> {
            SelectBox.ShapeEntry selected = this.selectBox.getSelected();
            if (selected != null) {
                PayloadHandler.sendToServer(new UpdateItemShapePayload(selected.shape.getName().toString()));
            }

        }) {
            @Override
            public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.extractContents(guiGraphics, mouseX, mouseY, partialTick);
                final Identifier SPRITE = Identifier.withDefaultNamespace("container/beacon/confirm");
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE, this.getX() + 1, this.getY(), 18, 18);
            }
        });

        this.clearButton = this.addRenderableWidget(new ColorableImageButton(this.leftPos + 32, this.topPos + 54, 19, 19, SkillsRecordScreen.BUTTON_SPRITES, button -> {
            this.selectBox.setSelected(null);
            PayloadHandler.sendToServer(new UpdateItemShapePayload(""));
        }) {
            @Override
            public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.extractContents(guiGraphics, mouseX, mouseY, partialTick);
                final Identifier SPRITE = Identifier.withDefaultNamespace("container/beacon/cancel");
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE, this.getX() + 1, this.getY(), 18, 18);
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
        this.clearButton.active = !slotEmpty && stack.has(Registration.SELECTED_SHAPE_COMPONENT.get());
        this.setButton.active = !slotEmpty && selected != null && !selected.isShapeSelected();

        this.setButton.setTooltip(this.setButton.active ? Tooltip.create(Component.literal("➤ ").append(
                Component.translatable("gui.ultimine_addition.action.set"))) : null);
        this.clearButton.setTooltip(this.clearButton.active ? Tooltip.create(Component.literal("➤ ").append(
                Component.translatable("gui.ultimine_addition.action.clear"))) : null);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.color = UAClientConfig.backgroundColor();
        this.update();
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        this.filterButton.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    private Player mc() {
        return Objects.requireNonNull(Objects.requireNonNull(this.minecraft).player);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics, mouseX, mouseY);
        SelectBox.ShapeEntry entry = this.selectBox.getHovered();
        if (entry == null) return;

        List<Component> components = new ArrayList<>();

        if (!entry.isAllowed()) {
            components.add(Component.translatable("gui.ultimine_addition.shape_selector.blacklisted").withStyle(ChatFormatting.RED));
        } else if (entry.isShapeSelected()) {
            components.add(Component.translatable("gui.ultimine_addition.shape_selector.selected").withColor(0xA0DA3E));
            components.add(Component.literal("- ").append(entry.shape.getDisplayName()));
        }
        if (entry.isAllowed() && FTBUltimineIntegration.isShapeNotLearned(mc(), this.menu.getSlot(0).getItem(), entry.shape)) {
            components.add(Component.translatable("gui.ultimine_addition.shape_selector.not_learned").withStyle(ChatFormatting.GOLD));
        }

        Minecraft mc = Objects.requireNonNull(minecraft);
        if (mc.options.advancedItemTooltips && mc.hasShiftDown()) {
            if (!components.isEmpty()) components.add(Component.empty());
            components.add(Component.literal("Shape ID: ").append(entry.shape.getName().toString()).withStyle(ChatFormatting.GRAY));
        }

        if (!components.isEmpty())
            guiGraphics.setComponentTooltipForNextFrame(font, components, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // AbstractContainerScreen doesn't pass scrolling on to its widgets.
        if (this.selectBox.visible && this.selectBox.isMouseOver(mouseX, mouseY)
                && this.selectBox.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256,
                ARGB.colorFromFloat(color.alpha(), color.red(), color.green(), color.blue()));
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        Color color = ColorUtils.blend(new Color(0, 0, 0), this.color.convert(), 0.25);
        guiGraphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, color.getRGB(), false);
        guiGraphics.fill(this.inventoryLabelX - 1, this.inventoryLabelY - 1, this.inventoryLabelX + this.font.width(this.playerInventoryTitle), this.inventoryLabelY + this.font.lineHeight, ColorUtils.rgbToRgba(color.getRGB(), 0.5F));
        guiGraphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, color.getRGB(), false);
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

    private class SelectBox extends ObjectSelectionList<SelectBox.ShapeEntry> {
        // Empty space below each entry's box; rows are laid out back to back, so it's taken out of the row height.
        private static final int ENTRY_GAP = 4;

        public SelectBox(int x, int y, int width, int height) {
            super(Minecraft.getInstance(), width, height, y, 20);
            this.setX(x);
            this.refreshList();
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
            this.refreshScrollAmount();
        }

        @Override
        public @Nullable ShapeEntry getHovered() {
            return super.getHovered();
        }

        @Override
        public int getRowLeft() {
            return this.getX() + this.width / 2 - this.getRowWidth() / 2;
        }

        @Override
        public int getRowWidth() {
            return this.width;
        }

        @Override
        protected int scrollBarX() {
            return this.getX() + width + 4;
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor guiGraphics) {
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor guiGraphics) {
        }

        @Override
        protected void extractSelection(GuiGraphicsExtractor guiGraphics, ShapeEntry entry, int outlineColor) {
            ScreenRectangle rectangle = new ScreenRectangle(entry.getX(), entry.getY() - 1, entry.getWidth(), entry.boxHeight() + 2);
            RenderUtils.drawHollowRect(guiGraphics, rectangle, 1, outlineColor);
        }

        @Override
        protected void extractScrollbar(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
            if (this.scrollable()) {
                final Identifier SCROLLER_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("widget/scroller_background");
                final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace("widget/scroller");
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_BACKGROUND_SPRITE, this.scrollBarX(), this.getY(), 6, this.getHeight());
                int color = ShapeSelectorScreen.this.color.convert().brighter().getRGB();
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, this.scrollBarX(), this.scrollBarY(), 6, this.scrollerHeight(), color);
            }
        }

        private class ShapeEntry extends ObjectSelectionList.Entry<ShapeEntry> {
            private final Shape shape;

            private ShapeEntry(Shape shape) {
                this.shape = shape;
            }

            private int boxHeight() {
                return this.getHeight() - ENTRY_GAP;
            }

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                return new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.boxHeight()).containsPoint((int) mouseX, (int) mouseY);
            }

            private boolean isAllowed() {
                return FTBUltimineIntegration.getEnabledShapes().contains(this.shape);
            }

            private boolean isShapeSelected() {
                ItemStack stack = ShapeSelectorScreen.this.menu.getSlot(0).getItem();
                SelectedShapeData shapeData = stack.get(Registration.SELECTED_SHAPE_COMPONENT.get());
                return shapeData != null && shape.getName().equals(shapeData.shape().getName());
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                SoundManager handler = Minecraft.getInstance().getSoundManager();
                if (!isAllowed()) {
                    handler.play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS, .5F));
                    return false;
                }
                handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
                return super.mouseClicked(event, doubleClick);
            }

            @Override
            public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                int left = this.getX();
                int top = this.getY();
                int width = this.getWidth();
                int height = this.boxHeight();
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
                    guiGraphics.text(font, Component.literal("➤"), left + 3, top + (height / 2) - 4, color.getRGB());
                }
                RenderUtils.drawScrollingString(guiGraphics, ticks, font, this.shape.getDisplayName().copy().withStyle(style), false, new ScreenRectangle(left + spacing, top, width - spacing, height), 3, color.getRGB(), true);
            }

            public @NotNull Component getNarration() {
                return shape.getDisplayName();
            }
        }
    }
}
