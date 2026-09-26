package net.ixdarklord.ultimine_addition.client.gui.tooltip;

import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.RenderPipelines;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.Arrays;

public class ClientSkillsRecordTooltip implements ClientTooltipComponent {
    // The vanilla inventory slot (the bundle's own slot sprites were removed in 1.21.2).
    public static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");
    private static final int SLOT_SIZE = 18;
    private static final int ITEM_OFFSET = 1;
    private final NonNullList<ItemStack> items;

    public ClientSkillsRecordTooltip(SkillsRecordTooltip skillsRecordTooltip) {
        this.items = skillsRecordTooltip.getItems();
    }

    public int getHeight(Font font) {
        return SLOT_SIZE + 2;
    }

    public int getWidth(Font font) {
        return this.items.size() * SLOT_SIZE;
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor guiGraphics) {
        SkillsRecordScreen.OverlayColor overlayColor = ConfigHandler.CLIENT.BACKGROUND_COLOR.get();
        int tint = ARGB.colorFromFloat(overlayColor.alpha(), overlayColor.red(), overlayColor.green(), overlayColor.blue());

        for (int i = 0; i < this.items.size(); i++) {
            int slotX = x + i * SLOT_SIZE;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, slotX, y, SLOT_SIZE, SLOT_SIZE, tint);

            ItemStack itemStack = this.items.get(i);
            guiGraphics.item(itemStack, slotX + ITEM_OFFSET, y + ITEM_OFFSET, i);
            guiGraphics.itemDecorations(font, itemStack, slotX + ITEM_OFFSET, y + ITEM_OFFSET);
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
        public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor guiGraphics) {
            switch (buttonId) {
                case 0 -> this.renderBGColors(font, x, y, guiGraphics);
                case 1 -> this.renderPanelPos(font, x, y, guiGraphics);
            }
        }

        private void renderBGColors(Font font, int x, int y, GuiGraphicsExtractor guiGraphics) {
            int gridSize = 3;
            int cellSpacing = 2;
            int cellWidth = (getWidth(font) - gridSize * cellSpacing) / gridSize;
            int cellHeight = (getHeight(font) - gridSize * cellSpacing) / gridSize;

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

                    Identifier CONFIRM_SPRITE = Identifier.withDefaultNamespace("container/beacon/confirm");
                    int textureX = minX + (cellWidth - 18) / 2;
                    int textureY = minY + (cellHeight - 18) / 2;
                    if (colorIndex == ConfigHandler.CLIENT.BACKGROUND_COLOR.get().ordinal())
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, CONFIRM_SPRITE, textureX + 1, textureY, 18, 18);

                    if (colorIndex >= colors.length-1) break;
                }
                if (colorIndex >= colors.length-1) break;
            }
        }

        private void renderPanelPos(Font font, int x, int y, GuiGraphicsExtractor guiGraphics) {
            int gridSize = 3;
            int cellSpacing = 2;
            int cellWidth = (getWidth(font) - gridSize * cellSpacing) / gridSize;
            int cellHeight = (getHeight(font) - gridSize * cellSpacing) / gridSize;
            int[] disabledPositions = new int[]{1, 4, 7};

            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    int adjuster = 2 + Math.max(0, font.width(textComponent)-getWidth(font))/2;
                    int minX = x + col * (cellWidth + cellSpacing) + adjuster;
                    int minY = y + row * (cellHeight + cellSpacing);
                    int maxX = minX + cellWidth;
                    int maxY = minY + cellHeight;

                    int selectedDirection = ConfigHandler.CLIENT.CHALLENGES_PANEL_ALIGNMENT.get().getPosIndex();
                    int directionIndex = row * gridSize + col;
                    Color color = selectedDirection == directionIndex ? Color.GREEN : Color.GRAY;
                    if (Arrays.stream(disabledPositions).anyMatch(value ->  value == directionIndex))
                        color = Color.DARK_GRAY.darker();

                    guiGraphics.fill(minX, minY, maxX, maxY, color.getRGB());

                    Identifier CONFIRM_SPRITE = Identifier.withDefaultNamespace("container/beacon/confirm");
                    int textureX = minX + (cellWidth - 18) / 2;
                    int textureY = minY + (cellHeight - 18) / 2;
                    if (directionIndex == selectedDirection)
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, CONFIRM_SPRITE, textureX + 1, textureY, 18, 18);
                }
            }
        }

        @Override
        public int getHeight(Font font) {
            return 64;
        }

        @Override
        public int getWidth(Font font) {
            return 64 + Math.max(0, font.width(textComponent) - 64);
        }
    }
}