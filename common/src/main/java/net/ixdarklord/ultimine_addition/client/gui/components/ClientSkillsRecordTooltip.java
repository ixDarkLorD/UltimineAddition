package net.ixdarklord.ultimine_addition.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        int i = this.gridSizeX();
        int j = this.gridSizeY();
        int k = 0;

        for(int l = 0; l < j; ++l) {
            for(int m = 0; m < i; ++m) {
                int n = x + m * 18 + 1;
                int o = y + l * 20 + 1;
                this.renderSlot(n, o, k++, false, guiGraphics, font);
            }
        }

        this.drawBorder(x, y, i, j, guiGraphics);
    }

    private void renderSlot(int x, int y, int itemIndex, boolean isBundleFull, GuiGraphics guiGraphics, Font font) {
        if (itemIndex >= this.items.size()) {
            this.blit(guiGraphics, x, y, isBundleFull ? Texture.BLOCKED_SLOT : Texture.SLOT);
        } else {
            ItemStack itemStack = this.items.get(itemIndex);
            this.blit(guiGraphics, x, y, Texture.SLOT);
            guiGraphics.renderItem(itemStack, x + 1, y + 1, itemIndex);
            guiGraphics.renderItemDecorations(font, itemStack, x + 1, y + 1);
        }
    }

    private void drawBorder(int x, int y, int slotWidth, int slotHeight, GuiGraphics guiGraphics) {
        this.blit(guiGraphics, x, y, Texture.BORDER_CORNER_TOP);
        this.blit(guiGraphics, x + slotWidth * 18 + 1, y, Texture.BORDER_CORNER_TOP);

        int i;
        for(i = 0; i < slotWidth; ++i) {
            this.blit(guiGraphics, x + 1 + i * 18, y, Texture.BORDER_HORIZONTAL_TOP);
            this.blit(guiGraphics, x + 1 + i * 18, y + slotHeight * 20, Texture.BORDER_HORIZONTAL_BOTTOM);
        }

        for(i = 0; i < slotHeight; ++i) {
            this.blit(guiGraphics, x, y + i * 20 + 1, Texture.BORDER_VERTICAL);
            this.blit(guiGraphics, x + slotWidth * 18 + 1, y + i * 20 + 1, Texture.BORDER_VERTICAL);
        }

        this.blit(guiGraphics, x, y + slotHeight * 20, Texture.BORDER_CORNER_BOTTOM);
        this.blit(guiGraphics, x + slotWidth * 18 + 1, y + slotHeight * 20, Texture.BORDER_CORNER_BOTTOM);
    }

    private void blit(GuiGraphics guiGraphics, int x, int y, Texture texture) {
        guiGraphics.blit(TEXTURE_LOCATION, x, y, 0, (float)texture.x, (float)texture.y, texture.w, texture.h, 128, 128);
    }

    private int gridSizeX() {
        return this.items.size();
    }

    private int gridSizeY() {
        return 1;
    }

    enum Texture {
        SLOT(0, 0, 18, 20),
        BLOCKED_SLOT(0, 40, 18, 20),
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
}