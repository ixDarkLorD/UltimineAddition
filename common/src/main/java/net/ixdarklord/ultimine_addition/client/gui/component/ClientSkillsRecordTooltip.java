package net.ixdarklord.ultimine_addition.client.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.ultimine_addition.client.gui.screen.ChallengesInfoPanel;
import net.ixdarklord.ultimine_addition.client.gui.screen.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.Arrays;

@Environment(EnvType.CLIENT)
public class ClientSkillsRecordTooltip implements ClientTooltipComponent {
    public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/container/bundle.png");
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

    public void renderImage(Font font, int x, int y, PoseStack stack, ItemRenderer itemRenderer, int o) {
        int i = this.gridSizeX();
        int j = this.gridSizeY();
        int k = 0;

        for(int l = 0; l < j; ++l) {
            for(int i1 = 0; i1 < i; ++i1) {
                int j1 = x + i1 * 18 + 1;
                int k1 = y + l * 20 + 1;
                this.renderSlot(j1, k1, k++, font, stack, itemRenderer, o);
            }
        }

        this.drawBorder(x, y, i, j, stack, o);
    }

    private void renderSlot(int x, int y, int i, Font font, PoseStack stack, ItemRenderer itemRenderer, int o) {
        if (i >= this.items.size()) {
            this.blit(stack, x, y, o, ClientSkillsRecordTooltip.Texture.SLOT);
        } else {
            ItemStack itemstack = this.items.get(i);
            this.blit(stack, x, y, o, ClientSkillsRecordTooltip.Texture.SLOT);
            itemRenderer.renderAndDecorateItem(itemstack, x + 1, y + 1, i);
            itemRenderer.renderGuiItemDecorations(font, itemstack, x + 1, y + 1);
        }
    }

    private void drawBorder(int x, int y, int xSize, int ySize, PoseStack stack, int o) {
        this.blit(stack, x, y, o, ClientSkillsRecordTooltip.Texture.BORDER_CORNER_TOP);
        this.blit(stack, x + xSize * 18 + 1, y, o, ClientSkillsRecordTooltip.Texture.BORDER_CORNER_TOP);

        for(int i = 0; i < xSize; ++i) {
            this.blit(stack, x + 1 + i * 18, y, o, ClientSkillsRecordTooltip.Texture.BORDER_HORIZONTAL_TOP);
            this.blit(stack, x + 1 + i * 18, y + ySize * 20, o, ClientSkillsRecordTooltip.Texture.BORDER_HORIZONTAL_BOTTOM);
        }

        for(int j = 0; j < ySize; ++j) {
            this.blit(stack, x, y + j * 20 + 1, o, ClientSkillsRecordTooltip.Texture.BORDER_VERTICAL);
            this.blit(stack, x + xSize * 18 + 1, y + j * 20 + 1, o, ClientSkillsRecordTooltip.Texture.BORDER_VERTICAL);
        }

        this.blit(stack, x, y + ySize * 20, o, ClientSkillsRecordTooltip.Texture.BORDER_CORNER_BOTTOM);
        this.blit(stack, x + xSize * 18 + 1, y + ySize * 20, o, ClientSkillsRecordTooltip.Texture.BORDER_CORNER_BOTTOM);
    }

    private void blit(PoseStack stack, int x, int y, int o, ClientSkillsRecordTooltip.Texture texture) {
        SkillsRecordScreen.BGColor bgColor = ConfigHandler.CLIENT.BACKGROUND_COLOR.get();
        RenderSystem.setShaderColor(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), bgColor.getAlpha());
        RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);
        GuiComponent.blit(stack, x, y, o, (float)texture.x, (float)texture.y, texture.w, texture.h, 128, 128);
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

        @Override
        public void renderImage(Font font, int x, int y, PoseStack stack, ItemRenderer itemRenderer, int o) {
            switch (buttonId) {
                case 0 -> this.renderBGColors(font, x, y, stack);
                case 1 -> this.renderPanelPos(font, x, y, stack);
            }
        }

        private void renderBGColors(Font font, int x, int y, PoseStack poseStack) {
            int gridSize = 3;
            int cellSpacing = 2;
            int cellWidth = (getWidth(font) - gridSize * cellSpacing) / gridSize;
            int cellHeight = (getHeight() - gridSize * cellSpacing) / gridSize;

            SkillsRecordScreen.BGColor[] colors = SkillsRecordScreen.BGColor.values();
            int colorIndex = 0;

            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    int adjuster = 2 + Math.max(0, font.width(textComponent)-getWidth(font))/2;
                    int minX = x + col * (cellWidth + cellSpacing) + adjuster;
                    int minY = y + row * (cellHeight + cellSpacing);
                    int maxX = minX + cellWidth;
                    int maxY = minY + cellHeight;

                    colorIndex = row * gridSize + col;
                    GuiComponent.fill(poseStack, minX, minY, maxX, maxY, colors[colorIndex].convert().getRGB());

                    ResourceLocation texture = new ResourceLocation("textures/gui/container/beacon.png");
                    int textureX = minX + (cellWidth - 18) / 2;
                    int textureY = minY + (cellHeight - 18) / 2;
                    if (colorIndex == ConfigHandler.CLIENT.BACKGROUND_COLOR.get().ordinal()) {
                        RenderSystem.setShader(GameRenderer::getPositionTexShader);
                        RenderSystem.setShaderTexture(0, texture);
                        GuiComponent.blit(poseStack, textureX + 1, textureY, 90, 220, 18, 18, 256, 256);
                    }

                    if (colorIndex >= colors.length-1) break;
                }
                if (colorIndex >= colors.length-1) break;
            }
        }

        private void renderPanelPos(Font font, int x, int y, PoseStack poseStack) {
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

                    GuiComponent.fill(poseStack, minX, minY, maxX, maxY, color.getRGB());

                    ResourceLocation texture = new ResourceLocation("textures/gui/container/beacon.png");
                    int textureX = minX + (cellWidth - 18) / 2;
                    int textureY = minY + (cellHeight - 18) / 2;
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderTexture(0, texture);

                    if (directionIndex == selectedDirection)
                        GuiComponent.blit(poseStack, textureX + 1, textureY, 90, 220 ,18, 18, 256, 256);
                    if (directionIndex == 4 && selectedDirection == ChallengesInfoPanel.Panel.Position.DISABLED.getPosIndex())
                        GuiComponent.blit(poseStack, textureX + 1, textureY, 112, 220 ,18, 18, 256, 256);
                }
            }
        }

        @Override
        public int getHeight() {
            return 64;
        }

        @Override
        public int getWidth(Font font) {
            return 64;
        }
    }
}