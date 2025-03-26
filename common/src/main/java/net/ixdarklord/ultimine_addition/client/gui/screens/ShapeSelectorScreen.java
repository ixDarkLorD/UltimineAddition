package net.ixdarklord.ultimine_addition.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftbultimine.shape.Shape;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.coolcatlib.api.client.gui.components.ColorableImageButton;
import net.ixdarklord.coolcatlib.api.util.ColorUtils;
import net.ixdarklord.coolcatlib.api.util.RenderUtils;
import net.ixdarklord.ultimine_addition.common.data.item.SelectedShapeData;
import net.ixdarklord.ultimine_addition.common.menu.ShapeSelectorMenu;
import net.ixdarklord.ultimine_addition.common.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.common.network.payloads.UpdateItemShapePayload;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.mixin.ShapeRegistryAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
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
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ShapeSelectorScreen extends AbstractContainerScreen<ShapeSelectorMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = FTBUltimineAddition.getGuiTexture("container/shape_selector", "png");
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

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = 6;
        this.titleLabelY = 5;
        this.inventoryLabelX = (this.imageWidth / 2) - (this.font.width(this.playerInventoryTitle) / 2);
        this.inventoryLabelY = this.imageHeight - 96;

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
                    guiGraphics.drawString(font, sequence, centeredX, y, this.getColor());
                    y += font.lineHeight;
                }

            }
        });
        this.emptyString.setColor(Color.LIGHT_GRAY.getRGB());

        this.selectBox = this.addRenderableWidget(new SelectBox(this.leftPos + 61, this.topPos + 16, 102, 54));
        this.selectBox.visible = false;

        this.setButton = this.addRenderableWidget(new ColorableImageButton(this.leftPos + 10, this.topPos + 54, 19, 19, SkillsRecordScreen.BUTTON_SPRITES, button -> {
            SelectBox.ShapeEntry selected = this.selectBox.getSelected();
            if (selected != null) {
                PayloadHandler.sendToServer(new UpdateItemShapePayload(selected.shape.getName()));
            }

        }) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                final ResourceLocation SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/confirm");
                guiGraphics.blitSprite(SPRITE, this.getX() + 1, this.getY(), 18, 18);
            }
        });

        this.clearButton = this.addRenderableWidget(new ColorableImageButton(this.leftPos + 32, this.topPos + 54, 19, 19, SkillsRecordScreen.BUTTON_SPRITES, button -> {
            this.selectBox.setSelected(null);
            PayloadHandler.sendToServer(new UpdateItemShapePayload(""));
        }) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                final ResourceLocation SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/cancel");
                guiGraphics.blitSprite(SPRITE, this.getX() + 1, this.getY(), 18, 18);
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.color = ConfigHandler.CLIENT.BACKGROUND_COLOR.get();
        this.update();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        SelectBox.ShapeEntry entry = this.selectBox.getHovered();
        if (entry == null) return;

        if (entry.isShapeSelected()) {
            List<Component> components = List.of(
                    Component.translatable("gui.ultimine_addition.shape_selector.selected").withColor(0xA0DA3E),
                    Component.literal("- ").append(entry.displayName)
            );
            guiGraphics.renderTooltip(font, components, Optional.empty(), mouseX, mouseY);
            return;
        }

        if (!entry.isAllowed()) {
            guiGraphics.renderTooltip(font, Component.translatable("gui.ultimine_addition.shape_selector.blacklisted").withStyle(ChatFormatting.RED), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.setColor(1F, 1F, 1F, 1F);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Color color = ColorUtils.blendColors(new Color(0, 0, 0), this.color.convert(), 0.75);
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, color.getRGB(), false);
        guiGraphics.fill(this.inventoryLabelX - 1, this.inventoryLabelY - 1, this.inventoryLabelX + this.font.width(this.playerInventoryTitle), this.inventoryLabelY + this.font.lineHeight, ColorUtils.RGBToRGBA(color.getRGB(), 0.5F));
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, color.getRGB(), false);
    }

    private class SelectBox extends ObjectSelectionList<SelectBox.ShapeEntry> {
        public SelectBox(int x, int y, int width, int height) {
            super(Minecraft.getInstance(), width, height, y, 20);
            this.setX(x);

            for (Shape shape : ShapeRegistryAccessor.getShapesList()) {
                this.addEntry(new ShapeEntry(shape));
            }
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
        protected int getScrollbarPosition() {
            return this.getX() + width + 4;
        }

        @Override
        protected void renderListSeparators(GuiGraphics guiGraphics) {
        }

        @Override
        protected void renderListBackground(GuiGraphics guiGraphics) {
        }

        @Override
        protected void renderSelection(GuiGraphics guiGraphics, int top, int width, int height, int outerColor, int innerColor) {
            int i = this.getX() + (this.width - width) / 2;
            ScreenRectangle rectangle = new ScreenRectangle(i, top - 1, width, height + 2);
            RenderUtils.renderHollowRectangle(guiGraphics, rectangle, 1, outerColor);
        }

        @Override
        protected void renderDecorations(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (this.scrollbarVisible()) {
                int i = this.getScrollbarPosition();
                int j = (int)((float)(this.height * this.height) / (float)this.getMaxPosition());
                j = Mth.clamp(j, 32, this.height - 8);
                int k = (int)this.getScrollAmount() * (this.height - j) / this.getMaxScroll() + this.getY();
                if (k < this.getY()) {
                    k = this.getY();
                }

                final ResourceLocation SCROLLER_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller_background");
                final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller");
                RenderSystem.enableBlend();
                guiGraphics.blitSprite(SCROLLER_BACKGROUND_SPRITE, i, this.getY(), 6, this.getHeight());
                ColorUtils color = new ColorUtils(ShapeSelectorScreen.this.color.convert().brighter().getRGB());
                guiGraphics.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
                guiGraphics.blitSprite(SCROLLER_SPRITE, i, k, 6, j);
                guiGraphics.setColor(1F, 1F, 1F, 1F);
                RenderSystem.disableBlend();
            }
        }

        @Environment(EnvType.CLIENT)
        private class ShapeEntry extends ObjectSelectionList.Entry<ShapeEntry> {
            private final Shape shape;
            private final Component displayName;

            private ShapeEntry(Shape shape) {
                this.shape = shape;
                this.displayName = Component.translatable("ftbultimine.shape." + shape.getName());
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
            public void renderBack(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                Color bgColor = new Color(0xBCBCBC);
                Color borderColor = new Color(0xB5B5B5);

                if (!isAllowed()) {
                    bgColor = ColorUtils.blendColors(bgColor.darker(), new Color(0x701111), 0.4);
                    borderColor = ColorUtils.blendColors(borderColor, new Color(0x701111), 0.4);
                } else if (isShapeSelected()) {
                    bgColor = ColorUtils.blendColors(bgColor.darker(), new Color(0xA0DA3E), 0.4);
                    borderColor = ColorUtils.blendColors(borderColor, new Color(0xA0DA3E), 0.4);
                }

                float alpha = (isAllowed() && isHovered) || isShapeSelected() ? 0.8F : 0.5F;
                guiGraphics.fill(left, top, left + width, top + height, ColorUtils.RGBToRGBA(bgColor.getRGB(), alpha));
                RenderUtils.renderHollowRectangle(guiGraphics, new ScreenRectangle(left, top, width, height), 1, borderColor.getRGB());
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                Font font = SelectBox.this.minecraft.font;
                Color color = isAllowed() ? Color.WHITE : new Color(0xD13E3E);
                int ticks = (int) (Util.getMillis() / 10);
                Style style = Style.EMPTY.withStrikethrough(!isAllowed());
                int spacing = isShapeSelected() ? 9 : 0;
                if (isShapeSelected()) {
                    guiGraphics.drawString(font, Component.literal("➤"), left + 3, top + (height / 2) - 4, color.getRGB());
                }
                RenderUtils.renderScrollingString(guiGraphics, ticks, font, displayName.copy().withStyle(style), false, left + spacing, top, width - spacing, height, 3, color.getRGB());
            }

            public @NotNull Component getNarration() {
                return displayName;
            }
        }
    }
}
