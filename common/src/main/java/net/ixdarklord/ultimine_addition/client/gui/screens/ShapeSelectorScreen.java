package net.ixdarklord.ultimine_addition.client.gui.screens;

import net.ixdarklord.ultimine_addition.client.gui.components.cardviewer.ShapeDiagrams;
import net.ixdarklord.coolcatcore.api.config.type.EnumType;
import net.ixdarklord.ultimine_addition.config.UAClientConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.ARGB;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.renderer.RenderPipelines;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShapeSelectorScreen extends AbstractContainerScreen<ShapeSelectorMenu> {
    // The item's clipboard: a slate board with its clip, holding a blueprint sheet.
    private static final Identifier BACKGROUND_TEXTURE = FTBUltimineAddition.getGuiTexture("container/shape_selector", "png");
    // Blueprint colors: the sheet's light line color, and the shape list's entries and scrollbar drawn in it.
    private static final int BLUEPRINT_LINE = 0xC3E5F5;
    private static final int BLUEPRINT_WASH = 0x7FD3F5;
    // Where things sit on the sheet (the tool slot is placed by the menu): the list in its panel, with its scrollbar
    // close to the panel's edge, and Set / Clear lined up with the panel's bottom.
    private static final int LIST_X = 62, LIST_Y = 50, LIST_WIDTH = 100, LIST_HEIGHT = 57;
    private static final int SCROLLBAR_GAP = 2;
    private static final int BUTTONS_Y = 90;
    // The buttons' pixel icons ('X' pixels), drawn in the blueprint's light color: Set, Clear and the list's filter.
    private static final String[] CHECK_ICON = {
            "........X",
            ".......XX",
            "X.....XX.",
            "XX...XX..",
            ".XX.XX...",
            "..XXX....",
            "...X.....",
    };
    private static final String[] CROSS_ICON = {
            "XX...XX",
            "XXX.XXX",
            ".XXXXX.",
            "..XXX..",
            ".XXXXX.",
            "XXX.XXX",
            "XX...XX",
    };
    private static final String[] FILTER_ICON = {
            "XXXXX",
            ".XXX.",
            "..X..",
            "..X..",
            "..X..",
    };
    // The room for the title inside its field (the field spans x 13 to 104 in the texture).
    private static final int TITLE_ROOM = 86;
    // The shape list's tooltip lines wrap at this width.
    private static final int TOOLTIP_WIDTH = 160;

    private ColoredButton filterButton;
    private AbstractStringWidget emptyString;
    private SelectBox selectBox;
    private ColorableImageButton setButton;
    private ColorableImageButton clearButton;

    public ShapeSelectorScreen(ShapeSelectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 184, 215);
    }

    @Override
    protected void init() {
        super.init();

        // The title in its field on the sheet, below the clip; the inventory's label centered on the board above its slots.
        this.titleLabelX = 16;
        this.titleLabelY = 35;
        this.inventoryLabelX = (this.imageWidth - this.font.width(this.playerInventoryTitle)) / 2;
        this.inventoryLabelY = 120;

        this.filterButton = this.addWidget(new ColoredButton(this.leftPos + 161, this.topPos + 35, 9, 9, SkillsRecordScreen.CONFIGURATION_BUTTON_SPRITES,
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
        }) {
            @Override
            public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
                drawBlueprintButton(guiGraphics, this, FILTER_ICON);
                this.renderTooltip(guiGraphics, mouseX, mouseY);
            }
        });

        this.emptyString = this.addRenderableWidget(new AbstractStringWidget(this.leftPos + LIST_X, this.topPos + LIST_Y, LIST_WIDTH, LIST_HEIGHT,
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
                    guiGraphics.text(font, sequence, centeredX, y, ARGB.opaque(BLUEPRINT_LINE));
                    y += font.lineHeight;
                }

            }
        });

        this.selectBox = this.addRenderableWidget(new SelectBox(this.leftPos + LIST_X, this.topPos + LIST_Y, LIST_WIDTH, LIST_HEIGHT));
        this.selectBox.visible = false;

        this.setButton = this.addRenderableWidget(new ColorableImageButton(this.leftPos + 14, this.topPos + BUTTONS_Y, 19, 19, SkillsRecordScreen.BUTTON_SPRITES, button -> {
            SelectBox.ShapeEntry selected = this.selectBox.getSelected();
            if (selected != null) {
                PayloadHandler.sendToServer(new UpdateItemShapePayload(selected.shape.getName().toString()));
            }

        }) {
            @Override
            public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
                drawBlueprintButton(guiGraphics, this, CHECK_ICON);
            }
        });

        this.clearButton = this.addRenderableWidget(new ColorableImageButton(this.leftPos + 35, this.topPos + BUTTONS_Y, 19, 19, SkillsRecordScreen.BUTTON_SPRITES, button -> {
            this.selectBox.setSelected(null);
            PayloadHandler.sendToServer(new UpdateItemShapePayload(""));
        }) {
            @Override
            public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
                drawBlueprintButton(guiGraphics, this, CROSS_ICON);
            }
        });

        this.setButton.active = false;
        this.clearButton.active = false;
    }

    private void update() {
        ItemStack stack = this.menu.getSlot(0).getItem();
        boolean slotEmpty = stack.isEmpty();
        SelectBox.ShapeEntry selected = this.selectBox.getSelected();


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
            guiGraphics.setTooltipForNextFrame(font, wrapTooltip(components), mouseX, mouseY);
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
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // White lettering on the blueprint; the inventory's label in the board's light slate.
        // Shrunk to fit its field when too long (e.g. in other languages).
        float scale = Math.min(1.0F, TITLE_ROOM / (float) Math.max(1, this.font.width(this.title)));
        var pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(this.titleLabelX, this.titleLabelY + 4.0F - 4.0F * scale);
        pose.scale(scale, scale);
        guiGraphics.text(this.font, this.title, 0, 0, 0xFFFFFFFF, true);
        pose.popMatrix();
        guiGraphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFD3D8E6, false);
    }

    // A button drawn on the blueprint: a blue plate framed in its line color, lit along the top and shaded along the
    // bottom, lighter while hovered and dimmed when it can't be used, with a light pixel icon in the middle.
    private static void drawBlueprintButton(GuiGraphicsExtractor graphics, AbstractWidget button, String[] icon) {
        int x = button.getX(), y = button.getY(), w = button.getWidth(), h = button.getHeight();
        boolean active = button.active, hovered = active && button.isHoveredOrFocused();
        graphics.fill(x, y, x + w, y + h, active ? ARGB.opaque(BLUEPRINT_LINE) : 0xFF6FB3D6);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, !active ? 0xFF2A8FC4 : hovered ? 0xFF3A9FD8 : 0xFF2586BD);
        graphics.fill(x + 1, y + 1, x + w - 1, y + 2, active ? 0xFF55B4E6 : 0xFF3C9ACC);
        graphics.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 0xFF1C6FA0);
        int ix = x + (w - icon[0].length()) / 2, iy = y + (h - icon.length) / 2;
        int color = active ? 0xFFFFFFFF : 0xFF8FC8E6;
        for (int row = 0; row < icon.length; row++) {
            for (int col = 0; col < icon[row].length(); col++) {
                if (icon[row].charAt(col) == 'X') graphics.fill(ix + col, iy + row, ix + col + 1, iy + row + 1, color);
            }
        }
    }

    // Each line split to fit TOOLTIP_WIDTH (a blank line kept blank).
    private List<FormattedCharSequence> wrapTooltip(List<Component> components) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Component component : components) {
            if (component.getString().isEmpty()) lines.add(FormattedCharSequence.EMPTY);
            else lines.addAll(this.font.split(component, TOOLTIP_WIDTH));
        }
        return lines;
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
        private static final int DIAGRAM_SIZE = 15;

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
            // Each row shows its shape's diagram, worked out by the server.
            ShapeDiagrams.request(this.children().stream().map(entry -> entry.shape.getName()).toList());
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
            return this.getX() + width + SCROLLBAR_GAP;
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
                // Drawn in the blueprint: a dark blue track and a thumb in its line color.
                int x = this.scrollBarX(), top = this.scrollBarY(), bottom = top + this.scrollerHeight();
                guiGraphics.fill(x, this.getY(), x + 6, this.getY() + this.getHeight(), 0xFF1C6FA0);
                guiGraphics.fill(x, top, x + 6, bottom, ARGB.opaque(BLUEPRINT_LINE));
                guiGraphics.fill(x + 1, top + 1, x + 5, bottom - 1, ARGB.opaque(BLUEPRINT_WASH));
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
                // Drawn in the blueprint's lines: a light wash framed in its line color.
                Color bgColor = new Color(BLUEPRINT_WASH);
                Color borderColor = new Color(BLUEPRINT_LINE);

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
                Style style = Style.EMPTY.withStrikethrough(!isAllowed());
                int spacing = isShapeSelected() ? 9 : 0;
                if (isShapeSelected()) {
                    guiGraphics.text(font, Component.literal("➤"), left + 3, top + (height / 2) - 4, color.getRGB());
                }
                // Shrunk to fit when too long (e.g. in other languages), centered in the box's height.
                Component name = this.shape.getDisplayName().copy().withStyle(style);
                int room = width - spacing - 6 - DIAGRAM_SIZE - 3;
                float scale = Math.min(1.0F, room / (float) Math.max(1, font.width(name)));
                var pose = guiGraphics.pose();
                pose.pushMatrix();
                pose.translate(left + spacing + 3, top + height / 2.0F - 4.0F * scale);
                pose.scale(scale, scale);
                guiGraphics.text(font, name, 0, 0, color.getRGB(), true);
                pose.popMatrix();

                // The shape's diagram, at the row's right end.
                int tint = isAllowed() ? 0xFFFFFFFF : 0xFFD13E3E;
                ShapeDiagrams.draw(guiGraphics, this.shape.getName(), left + width - DIAGRAM_SIZE - 3, top + (height - DIAGRAM_SIZE) / 2, DIAGRAM_SIZE,
                        tint, 0xFFFBD25A, 0x50103A5C);
            }

            public @NotNull Component getNarration() {
                return shape.getDisplayName();
            }
        }
    }
}
