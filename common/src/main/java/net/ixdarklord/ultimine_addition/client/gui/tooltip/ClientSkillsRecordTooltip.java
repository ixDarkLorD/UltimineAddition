package net.ixdarklord.ultimine_addition.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;

@Environment(EnvType.CLIENT)
public class ClientSkillsRecordTooltip implements ClientTooltipComponent {
    public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/container/bundle.png");
    private final NonNullList<ItemStack> items;

    public ClientSkillsRecordTooltip(SkillsRecordTooltip skillsRecordTooltip) {
        this.items = skillsRecordTooltip.items();
    }

    public int getHeight() {
        return this.gridSizeY() * 20 + 2 + 4;
    }

    public int getWidth(@NotNull Font font) {
        return this.gridSizeX() * 18 + 2;
    }

    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics guiGraphics) {
        int i = this.gridSizeX();
        int j = this.gridSizeY();
        int k = 0;

        for (int l = 0; l < j; ++l) {
            for (int m = 0; m < i; ++m) {
                int n = x + m * 18 + 1;
                int o = y + l * 20 + 1;
                this.renderSlot(n, o, k++, guiGraphics, font);
            }
        }

        this.drawBorder(x, y, i, j, guiGraphics);
    }

    private void renderSlot(int x, int y, int itemIndex, GuiGraphics guiGraphics, Font font) {
        if (itemIndex >= this.items.size()) {
            this.blit(guiGraphics, x, y, ClientSkillsRecordTooltip.Texture.SLOT);
        } else {
            ItemStack itemStack = this.items.get(itemIndex);
            this.blit(guiGraphics, x, y, ClientSkillsRecordTooltip.Texture.SLOT);
            guiGraphics.renderItem(itemStack, x + 1, y + 1, itemIndex);
            guiGraphics.renderItemDecorations(font, itemStack, x + 1, y + 1);
        }

    }

    private void drawBorder(int x, int y, int slotWidth, int slotHeight, GuiGraphics guiGraphics) {
        this.blit(guiGraphics, x, y, ClientSkillsRecordTooltip.Texture.BORDER_CORNER_TOP);
        this.blit(guiGraphics, x + slotWidth * 18 + 1, y, ClientSkillsRecordTooltip.Texture.BORDER_CORNER_TOP);

        for (int i = 0; i < slotWidth; ++i) {
            this.blit(guiGraphics, x + 1 + i * 18, y, ClientSkillsRecordTooltip.Texture.BORDER_HORIZONTAL_TOP);
            this.blit(guiGraphics, x + 1 + i * 18, y + slotHeight * 20, ClientSkillsRecordTooltip.Texture.BORDER_HORIZONTAL_BOTTOM);
        }

        for (int i2 = 0; i2 < slotHeight; ++i2) {
            this.blit(guiGraphics, x, y + i2 * 20 + 1, ClientSkillsRecordTooltip.Texture.BORDER_VERTICAL);
            this.blit(guiGraphics, x + slotWidth * 18 + 1, y + i2 * 20 + 1, ClientSkillsRecordTooltip.Texture.BORDER_VERTICAL);
        }

        this.blit(guiGraphics, x, y + slotHeight * 20, ClientSkillsRecordTooltip.Texture.BORDER_CORNER_BOTTOM);
        this.blit(guiGraphics, x + slotWidth * 18 + 1, y + slotHeight * 20, ClientSkillsRecordTooltip.Texture.BORDER_CORNER_BOTTOM);
    }

    private void blit(GuiGraphics guiGraphics, int x, int y, Texture texture) {
        SkillsRecordScreen.OverlayColor overlayColor = ConfigHandler.CLIENT.BACKGROUND_COLOR.get();
        RenderSystem.setShaderColor(overlayColor.red(), overlayColor.green(), overlayColor.blue(), overlayColor.alpha());
        guiGraphics.blit(TEXTURE_LOCATION, x, y, 0, (float) texture.x, (float) texture.y, texture.w, texture.h, 128, 128);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private int gridSizeX() {
        return this.items.size();
    }

    private int gridSizeY() {
        return 1;
    }

    enum Texture {
        SLOT(0, 0, 18, 20),
        BORDER_VERTICAL(0, 18, 1, 20),
        BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
        BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
        BORDER_CORNER_TOP(0, 20, 1, 1),
        BORDER_CORNER_BOTTOM(0, 60, 1, 1);

        public final int x;
        public final int y;
        public final int w;
        public final int h;

        Texture(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    public static class Option implements ClientTooltipComponent {
        private final int buttonId;
        private final Component textComponent;

        public Option(SkillsRecordTooltip.Option option) {
            this.buttonId = option.buttonId();
            this.textComponent = option.textComponent();
        }

        public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics guiGraphics) {
            switch (this.buttonId) {
                case 0 -> this.renderBGColors(font, x, y, guiGraphics);
                case 1 -> this.renderPanelPos(font, x, y, guiGraphics);
            }

        }

        private void renderBGColors(Font font, int x, int y, GuiGraphics guiGraphics) {
            int gridSize = 3;
            int cellSpacing = 2;
            int cellWidth = (this.getWidth(font) - gridSize * cellSpacing) / gridSize;
            int cellHeight = (this.getHeight() - gridSize * cellSpacing) / gridSize;
            SkillsRecordScreen.OverlayColor[] colors = SkillsRecordScreen.OverlayColor.values();
            int colorIndex = 0;

            for (int row = 0; row < gridSize; ++row) {
                for (int col = 0; col < gridSize; ++col) {
                    int adjuster = 2 + Math.max(0, font.width(this.textComponent) - this.getWidth(font)) / 2;
                    int minX = x + col * (cellWidth + cellSpacing) + adjuster;
                    int minY = y + row * (cellHeight + cellSpacing);
                    int maxX = minX + cellWidth;
                    int maxY = minY + cellHeight;
                    colorIndex = row * gridSize + col;
                    guiGraphics.fill(minX, minY, maxX, maxY, colors[colorIndex].convert().getRGB());
                    ResourceLocation texture = new ResourceLocation("textures/gui/container/beacon.png");
                    int textureX = minX + (cellWidth - 18) / 2;
                    int textureY = minY + (cellHeight - 18) / 2;
                    if (colorIndex == ConfigHandler.CLIENT.BACKGROUND_COLOR.get().ordinal()) {
                        guiGraphics.blit(texture, textureX + 1, textureY, 90, 220, 18, 18);
                    }

                    if (colorIndex >= colors.length - 1) {
                        break;
                    }
                }

                if (colorIndex >= colors.length - 1) {
                    break;
                }
            }

        }

        private void renderPanelPos(Font font, int x, int y, GuiGraphics guiGraphics) {
            int gridSize = 3;
            int cellSpacing = 2;
            int cellWidth = (this.getWidth(font) - gridSize * cellSpacing) / gridSize;
            int cellHeight = (this.getHeight() - gridSize * cellSpacing) / gridSize;
            int[] disabledPositions = new int[]{1, 4, 7};

            for (int row = 0; row < gridSize; ++row) {
                for (int col = 0; col < gridSize; ++col) {
                    int adjuster = 2 + Math.max(0, font.width(this.textComponent) - this.getWidth(font)) / 2;
                    int minX = x + col * (cellWidth + cellSpacing) + adjuster;
                    int minY = y + row * (cellHeight + cellSpacing);
                    int maxX = minX + cellWidth;
                    int maxY = minY + cellHeight;
                    int selectedDirection = ConfigHandler.CLIENT.CHALLENGES_PANEL_ALIGNMENT.get().getPosIndex();
                    int directionIndex = row * gridSize + col;
                    Color color = selectedDirection == directionIndex ? Color.GREEN : Color.GRAY;
                    if (Arrays.stream(disabledPositions).anyMatch((value) -> value == directionIndex)) {
                        color = Color.DARK_GRAY.darker();
                    }

                    guiGraphics.fill(minX, minY, maxX, maxY, color.getRGB());
                    ResourceLocation texture = new ResourceLocation("textures/gui/container/beacon.png");
                    int textureX = minX + (cellWidth - 18) / 2;
                    int textureY = minY + (cellHeight - 18) / 2;
                    if (directionIndex == selectedDirection) {
                        guiGraphics.blit(texture, textureX + 1, textureY, 90, 220, 18, 18);
                    }
                }
            }

        }

        public int getHeight() {
            return 64;
        }

        public int getWidth(@NotNull Font font) {
            return 64;
        }
    }
}
