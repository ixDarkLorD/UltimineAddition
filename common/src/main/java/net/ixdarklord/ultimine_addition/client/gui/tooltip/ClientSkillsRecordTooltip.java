package net.ixdarklord.ultimine_addition.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.ultimine_addition.client.gui.components.skills_record.ChallengesInfoPanel;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.Arrays;

@Environment(EnvType.CLIENT)
public class ClientSkillsRecordTooltip implements ClientTooltipComponent {
    public static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/background");
    private final NonNullList<ItemStack> items;

    public ClientSkillsRecordTooltip(SkillsRecordTooltip skillsRecordTooltip) {
        this.items = skillsRecordTooltip.getItems();
    }

    public int getHeight() {
        return this.gridSizeY() * 20 + 2 + 4;
    }

    public int getWidth(Font font) {
        return this.gridSizeX() * 18 + 2;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        int i = this.gridSizeX();
        int j = this.gridSizeY();
        guiGraphics.blitSprite(BACKGROUND_SPRITE, x, y, this.backgroundWidth(), this.backgroundHeight());
        int k = 0;

        for(int l = 0; l < j; ++l) {
            for(int m = 0; m < i; ++m) {
                int n = x + m * 18 + 1;
                int o = y + l * 20 + 1;
                this.renderSlot(n, o, k++, guiGraphics, font);
            }
        }

    }

    private void renderSlot(int x, int y, int itemIndex, GuiGraphics guiGraphics, Font font) {
        if (itemIndex >= this.items.size()) {
            this.blit(guiGraphics, x, y, Texture.SLOT);
        } else {
            ItemStack itemStack = this.items.get(itemIndex);
            this.blit(guiGraphics, x, y, Texture.SLOT);
            guiGraphics.renderItem(itemStack, x + 1, y + 1, itemIndex);
            guiGraphics.renderItemDecorations(font, itemStack, x + 1, y + 1);
        }
    }

    private void blit(GuiGraphics guiGraphics, int x, int y, Texture texture) {
        SkillsRecordScreen.OverlayColor overlayColor = ConfigHandler.CLIENT.BACKGROUND_COLOR.get();
        RenderSystem.setShaderColor(overlayColor.getRed(), overlayColor.getGreen(), overlayColor.getBlue(), overlayColor.getAlpha());
        guiGraphics.blitSprite(texture.sprite, x, y, 0, texture.w, texture.h);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private int backgroundWidth() {
        return this.gridSizeX() * 18 + 2;
    }

    private int backgroundHeight() {
        return this.gridSizeY() * 20 + 2;
    }

    private int gridSizeX() {
        return this.items.size();
    }

    private int gridSizeY() {
        return 1;
    }

    private enum Texture {
        SLOT(ResourceLocation.withDefaultNamespace("container/bundle/slot"), 18, 20);

        public final ResourceLocation sprite;
        public final int w;
        public final int h;

        Texture(final ResourceLocation sprite, final int w, final int h) {
            this.sprite = sprite;
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

        @Override
        public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
            switch (buttonId) {
                case 0 -> this.renderBGColors(font, x, y, guiGraphics);
                case 1 -> this.renderPanelPos(font, x, y, guiGraphics);
            }
        }

        private void renderBGColors(Font font, int x, int y, GuiGraphics guiGraphics) {
            int gridSize = 3;
            int cellSpacing = 2;
            int cellWidth = (getWidth(font) - gridSize * cellSpacing) / gridSize;
            int cellHeight = (getHeight() - gridSize * cellSpacing) / gridSize;

            SkillsRecordScreen.OverlayColor[] colors = SkillsRecordScreen.OverlayColor.values();
            int colorIndex = 0;

            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    int adjuster = 2 + Math.max(0, font.width(textComponent)-getWidth(font))/2;
                    int minX = x + col * (cellWidth + cellSpacing) + adjuster;
                    int minY = y + row * (cellHeight + cellSpacing);
                    int maxX = minX + cellWidth;
                    int maxY = minY + cellHeight;

                    colorIndex = row * gridSize + col;
                    guiGraphics.fill(minX, minY, maxX, maxY, colors[colorIndex].convert().getRGB());

                    ResourceLocation CONFIRM_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/confirm");
                    int textureX = minX + (cellWidth - 18) / 2;
                    int textureY = minY + (cellHeight - 18) / 2;
                    if (colorIndex == ConfigHandler.CLIENT.BACKGROUND_COLOR.get().ordinal())
                        guiGraphics.blitSprite(CONFIRM_SPRITE, textureX + 1, textureY, 18, 18);

                    if (colorIndex >= colors.length-1) break;
                }
                if (colorIndex >= colors.length-1) break;
            }
        }

        private void renderPanelPos(Font font, int x, int y, GuiGraphics guiGraphics) {
            int gridSize = 3;
            int cellSpacing = 2;
            int cellWidth = (getWidth(font) - gridSize * cellSpacing) / gridSize;
            int cellHeight = (getHeight() - gridSize * cellSpacing) / gridSize;
            int[] disabledPositions = new int[]{1, 4, 7};

            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    int adjuster = 2 + Math.max(0, font.width(textComponent)-getWidth(font))/2;
                    int minX = x + col * (cellWidth + cellSpacing) + adjuster;
                    int minY = y + row * (cellHeight + cellSpacing);
                    int maxX = minX + cellWidth;
                    int maxY = minY + cellHeight;

                    int selectedDirection = ConfigHandler.CLIENT.CHALLENGES_PANEL_POSITION.get().getPosIndex();
                    int directionIndex = row * gridSize + col;
                    Color color = selectedDirection == directionIndex ? Color.GREEN : Color.GRAY;
                    if (Arrays.stream(disabledPositions).anyMatch(value ->  value == directionIndex))
                        color = Color.DARK_GRAY.darker();

                    guiGraphics.fill(minX, minY, maxX, maxY, color.getRGB());

                    ResourceLocation CONFIRM_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/confirm");
                    ResourceLocation CANCEL_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/cancel");
                    int textureX = minX + (cellWidth - 18) / 2;
                    int textureY = minY + (cellHeight - 18) / 2;
                    if (directionIndex == selectedDirection)
                        guiGraphics.blitSprite(CONFIRM_SPRITE, textureX + 1, textureY, 18, 18);
                    if (directionIndex == 4 && selectedDirection == ChallengesInfoPanel.Panel.Position.DISABLED.getPosIndex())
                        guiGraphics.blitSprite(CANCEL_SPRITE, textureX + 1, textureY, 18, 18);
                }
            }
        }

        @Override
        public int getHeight() {
            return 64;
        }

        @Override
        public int getWidth(Font font) {
            return 64 + Math.max(0, font.width(textComponent) - 64);
        }
    }
}