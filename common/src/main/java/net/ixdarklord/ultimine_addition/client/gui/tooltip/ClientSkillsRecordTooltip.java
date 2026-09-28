package net.ixdarklord.ultimine_addition.client.gui.tooltip;

import net.ixdarklord.ultimine_addition.config.UAClientConfig;
import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.RenderPipelines;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
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
        SkillsRecordScreen.OverlayColor overlayColor = UAClientConfig.backgroundColor();
        int tint = ARGB.colorFromFloat(overlayColor.alpha(), overlayColor.red(), overlayColor.green(), overlayColor.blue());

        for (int i = 0; i < this.items.size(); i++) {
            int slotX = x + i * SLOT_SIZE;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, slotX, y, SLOT_SIZE, SLOT_SIZE, tint);

            ItemStack itemStack = this.items.get(i);
            guiGraphics.item(itemStack, slotX + ITEM_OFFSET, y + ITEM_OFFSET, i);
            guiGraphics.itemDecorations(font, itemStack, slotX + ITEM_OFFSET, y + ITEM_OFFSET);
        }
    }
}
