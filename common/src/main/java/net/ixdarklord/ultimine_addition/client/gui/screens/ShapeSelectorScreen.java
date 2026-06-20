package net.ixdarklord.ultimine_addition.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftbultimine.shape.Shape;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.coolcatlib.api.client.gui.components.ColorableImageButton;
import net.ixdarklord.coolcatlib.api.client.utils.RenderUtils;
import net.ixdarklord.coolcatlib.api.utils.ColorUtils;
import net.ixdarklord.ultimine_addition.client.gui.components.ColoredButton;
import net.ixdarklord.ultimine_addition.common.data.item.SelectedShapeData;
import net.ixdarklord.ultimine_addition.common.menu.ShapeSelectorMenu;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.network.packets.UpdateItemShapePacket;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class ShapeSelectorScreen extends AbstractContainerScreen<ShapeSelectorMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = FTBUltimineAddition.getGuiTexture("container/shape_selector");
    private SkillsRecordScreen.OverlayColor color;
    private AbstractStringWidget emptyString;
    private SelectBox selectBox;
    private ColorableImageButton setButton;
    private ColorableImageButton clearButton;

    public ShapeSelectorScreen(ShapeSelectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 178;
        this.imageHeight = 172;
    }

    protected void init() {
        super.init();
        this.titleLabelX = 6;
        this.titleLabelY = 5;
        this.inventoryLabelX = this.imageWidth / 2 - this.font.width(this.playerInventoryTitle) / 2;
        this.inventoryLabelY = this.imageHeight - 96;
        this.emptyString = this.addRenderableWidget(new AbstractStringWidget(this.leftPos + 61, this.topPos + 16, 102, 54, Component.translatable("gui.ultimine_addition.shape_selector.insert"), this.font) {
            public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                Component component = this.getMessage();
                Font font = this.getFont();
                int width = this.getWidth();
                List<FormattedCharSequence> sequences = font.split(component, width);
                int i = sequences.size();
                Objects.requireNonNull(font);
                int totalTextHeight = i * 9;
                int x = this.getX();
                int y = this.getY() + (this.getHeight() - totalTextHeight) / 2;

                for (FormattedCharSequence sequence : sequences) {
                    int textWidth = font.width(sequence);
                    int centeredX = x + (width - textWidth) / 2;
                    guiGraphics.drawString(font, sequence, centeredX, y, this.getColor());
                    Objects.requireNonNull(font);
                    y += 9;
                }

            }
        });
        this.emptyString.setColor(Color.LIGHT_GRAY.getRGB());
        this.selectBox = this.addRenderableWidget(new SelectBox(this.leftPos + 61, this.topPos + 16, 102, 54));
        this.selectBox.visible = false;
        this.addRenderableWidget(new ColoredButton(this.leftPos + 165, this.topPos + 4, 9, 9, 4, SkillsRecordScreen.CONFIGURATION_BUTTON_SPRITES, 10, 10, (button) -> {
            Filter filter = ConfigHandler.CLIENT.SHAPE_SELECTOR_FILTER.get();
            ConfigHandler.CLIENT.SHAPE_SELECTOR_FILTER.set(Screen.hasShiftDown() ? filter.previous() : filter.next());
            ConfigHandler.CLIENT.SHAPE_SELECTOR_FILTER.save();
            this.selectBox.refreshList();
        }, Component.empty(), (tooltipInfo) -> {
            MutableComponent filterComponent = ConfigHandler.CLIENT.SHAPE_SELECTOR_FILTER.get() == ShapeSelectorScreen.Filter.ALL ? Component.translatable("gui.ultimine_addition.filter.all") : Component.translatable("gui.ultimine_addition.filter.only_enabled");
            tooltipInfo.component = Component.translatable("gui.ultimine_addition.filter").append(": ").append(filterComponent).withStyle(ChatFormatting.WHITE);
        }));
        this.setButton = this.addRenderableWidget(new ColoredButton(this.leftPos + 10, this.topPos + 54, 19, 19, 4, SkillsRecordScreen.BUTTON_SPRITES, 45, 12, (button) -> {
            SelectBox.ShapeEntry selected = this.selectBox.getSelected();
            if (selected != null) {
                PacketHandler.sendToServer(new UpdateItemShapePacket(selected.shape.getName()));
            }

        }, Component.empty()) {
            public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                ResourceLocation SPRITE = new ResourceLocation("textures/gui/container/beacon.png");
                guiGraphics.blit(SPRITE, this.getX() + 1, this.getY(), 90, 220, 18, 18);
            }
        });
        this.clearButton = this.addRenderableWidget(new ColoredButton(this.leftPos + 32, this.topPos + 54, 19, 19, 4, SkillsRecordScreen.BUTTON_SPRITES, 45, 12, (button) -> {
            this.selectBox.setSelected(null);
            PacketHandler.sendToServer(new UpdateItemShapePacket(""));
        }, Component.empty()) {
            public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                ResourceLocation SPRITE = new ResourceLocation("textures/gui/container/beacon.png");
                guiGraphics.blit(SPRITE, this.getX() + 1, this.getY(), 112, 220, 18, 18);
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
        this.clearButton.active = !slotEmpty && SelectedShapeData.hasData(stack);
        this.setButton.active = !slotEmpty && selected != null && !selected.isShapeSelected();
        this.setButton.setTooltip(this.setButton.active ? Tooltip.create(Component.literal("➤ ").append(Component.translatable("gui.ultimine_addition.action.set"))) : null);
        this.clearButton.setTooltip(this.clearButton.active ? Tooltip.create(Component.literal("➤ ").append(Component.translatable("gui.ultimine_addition.action.clear"))) : null);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        this.color = ConfigHandler.CLIENT.BACKGROUND_COLOR.get();
        this.update();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        SelectBox.ShapeEntry entry = this.selectBox.getHovered();
        if (entry != null) {
            List<Component> components = new ArrayList<>();
            if (!entry.isAllowed()) {
                components.add(Component.translatable("gui.ultimine_addition.shape_selector.blacklisted").withStyle(ChatFormatting.RED));
            } else if (entry.isShapeSelected()) {
                components.add(Component.translatable("gui.ultimine_addition.shape_selector.selected").withStyle(Style.EMPTY.withColor(10541630)));
                components.add(Component.literal("- ").append(FTBUltimineIntegration.getShapeDisplayName(entry.shape)));
            }

            Minecraft mc = Objects.requireNonNull(this.minecraft);
            if (mc.options.advancedItemTooltips && hasShiftDown()) {
                if (!components.isEmpty()) {
                    components.add(Component.empty());
                }

                components.add(Component.literal("Shape ID: ").append(entry.shape.getName()).withStyle(ChatFormatting.GRAY));
            }

            if (!components.isEmpty()) {
                guiGraphics.renderTooltip(this.font, components, Optional.empty(), mouseX, mouseY);
            }

        }
    }

    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Color color = ColorUtils.blend(new Color(0, 0, 0), this.color.convert(), 0.25F);
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, color.getRGB(), false);
        int i = this.inventoryLabelX - 1;
        int i2 = this.inventoryLabelY - 1;
        int i3 = this.inventoryLabelX + this.font.width(this.playerInventoryTitle);
        int i4 = this.inventoryLabelY;
        Objects.requireNonNull(this.font);
        guiGraphics.fill(i, i2, i3, i4 + 9, ColorUtils.rgbToRgba(color, 0.5F));
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, color.getRGB(), false);
    }

    public enum Filter {
        ALL,
        ENABLED_SHAPES;

        public Filter next() {
            int nextIndex = (this.ordinal() + 1) % values().length;
            return values()[nextIndex];
        }

        public Filter previous() {
            int prevIndex = (this.ordinal() - 1 + values().length) % values().length;
            return values()[prevIndex];
        }
    }

    private class SelectBox extends ObjectSelectionList<SelectBox.ShapeEntry> {
        private boolean visible;

        public SelectBox(int x, int y, int width, int height) {
            super(Minecraft.getInstance(), width, height, y, y + height, 20);
            this.setLeftPos(x);
            this.refreshList();
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
            this.setRenderHeader(false, 0);
        }

        public void refreshList() {
            this.clearEntries();

            for (Shape shape : FTBUltimineIntegration.getShapesList()) {
                if (ConfigHandler.CLIENT.SHAPE_SELECTOR_FILTER.get() != ShapeSelectorScreen.Filter.ENABLED_SHAPES || FTBUltimineIntegration.getEnabledShapes().contains(shape)) {
                    this.addEntry(new ShapeEntry(shape));
                }
            }

            this.setScrollAmount(this.getScrollAmount());
        }

        public ShapeEntry getHovered() {
            return super.getHovered();
        }

        public int getRowLeft() {
            return this.x0 + this.width / 2 - this.getRowWidth() / 2;
        }

        public int getRowWidth() {
            return this.width;
        }

        protected int getScrollbarPosition() {
            return this.x0 + this.width + 4;
        }

        public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (this.visible) {
                super.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        protected void renderSelection(@NotNull GuiGraphics guiGraphics, int top, int width, int height, int outerColor, int innerColor) {
            int i = this.x0 + (this.width - width) / 2;
            ScreenRectangle rectangle = new ScreenRectangle(i, top - 1, width, height + 2);
            RenderUtils.drawHollowRect(guiGraphics, rectangle, 1, outerColor);
        }

        protected void renderDecorations(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (this.getMaxScroll() > 0) {
                int i = this.getScrollbarPosition();
                int j = (int) ((float) (this.height * this.height) / (float) this.getMaxPosition());
                j = Mth.clamp(j, 32, this.height - 8);
                int k = (int) this.getScrollAmount() * (this.height - j) / this.getMaxScroll() + this.y0;
                if (k < this.y0) {
                    k = this.y0;
                }

                RenderSystem.enableBlend();
                ScreenRectangle scrollBarBg = new ScreenRectangle(i, this.y0, 6, this.height);
                guiGraphics.fill(scrollBarBg.left(), scrollBarBg.top(), scrollBarBg.right(), scrollBarBg.bottom(), Color.BLACK.getRGB());
                ColorUtils color = new ColorUtils(ShapeSelectorScreen.this.color.convert().brighter().getRGB());
                guiGraphics.setColor(color.red(), color.green(), color.blue(), color.alpha());
                ScreenRectangle scrollBar = new ScreenRectangle(i, k, 6, j);
                guiGraphics.fill(scrollBar.left(), scrollBar.top(), scrollBar.right() - 1, scrollBar.bottom() - 1, Color.LIGHT_GRAY.getRGB());
                guiGraphics.fill(scrollBar.left(), scrollBar.top(), scrollBar.right(), scrollBar.bottom(), Color.GRAY.getRGB());
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.disableBlend();
            }

        }

        @Environment(EnvType.CLIENT)
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
                SelectedShapeData shapeData = SelectedShapeData.load(stack);
                return shapeData != null && shapeData.getShape() != null && this.shape.getName().equals(shapeData.getShape().getName());
            }

            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (!SelectBox.this.visible) {
                    return true;
                } else {
                    SoundManager handler = Minecraft.getInstance().getSoundManager();
                    if (!this.isAllowed()) {
                        handler.play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5F, 2.0F));
                        return false;
                    } else {
                        handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 2.0F));
                        return true;
                    }
                }
            }

            public void renderBack(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                Color bgColor = new Color(12369084);
                Color borderColor = new Color(11908533);
                if (!this.isAllowed()) {
                    bgColor = ColorUtils.blend(bgColor.darker(), new Color(7344401), 0.6);
                    borderColor = ColorUtils.blend(borderColor, new Color(7344401), 0.6);
                } else if (this.isShapeSelected()) {
                    bgColor = ColorUtils.blend(bgColor.darker(), new Color(10541630), 0.6);
                    borderColor = ColorUtils.blend(borderColor, new Color(10541630), 0.6);
                }

                float alpha = (!this.isAllowed() || !isHovered) && !this.isShapeSelected() ? 0.5F : 0.8F;
                guiGraphics.fill(left, top, left + width, top + height, ColorUtils.rgbToRgba(bgColor, alpha));
                RenderUtils.drawHollowRect(guiGraphics, new ScreenRectangle(left, top, width, height), 1, borderColor.getRGB());
            }

            public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                Font font = SelectBox.this.minecraft.font;
                Color color = this.isAllowed() ? Color.WHITE : new Color(13712958);
                int ticks = (int) (Util.getMillis() / 10L);
                Style style = Style.EMPTY.withStrikethrough(!this.isAllowed());
                int spacing = this.isShapeSelected() ? 9 : 0;
                if (this.isShapeSelected()) {
                    guiGraphics.drawString(font, Component.literal("➤"), left + 3, top + height / 2 - 4, color.getRGB());
                }

                RenderUtils.drawScrollingString(guiGraphics, ticks, font, FTBUltimineIntegration.getShapeDisplayName(this.shape).copy().withStyle(style), false, new ScreenRectangle(left + spacing, top, width - spacing, height), 3, color.getRGB(), true);
            }

            public @NotNull Component getNarration() {
                return FTBUltimineIntegration.getShapeDisplayName(this.shape);
            }
        }
    }
}
