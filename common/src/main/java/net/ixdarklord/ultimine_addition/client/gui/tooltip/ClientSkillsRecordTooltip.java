package net.ixdarklord.ultimine_addition.client.gui.tooltip;

import net.ixdarklord.ultimine_addition.client.gui.GuiDraw;
import net.ixdarklord.ultimine_addition.client.gui.theme.RecordTheme;
import net.ixdarklord.ultimine_addition.util.ARGB;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class ClientSkillsRecordTooltip implements ClientTooltipComponent {
    public static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int SLOT_SIZE = 18;
    private static final int ITEM_OFFSET = 1;
    private final NonNullList<ItemStack> items;
    private final RecordTheme theme;

    public ClientSkillsRecordTooltip(SkillsRecordTooltip skillsRecordTooltip) {
        this.items = skillsRecordTooltip.getItems();
        this.theme = RecordTheme.of(skillsRecordTooltip.getColor());
    }

    public int getHeight() {
        return SLOT_SIZE + 2;
    }

    public int getWidth(Font font) {
        return this.items.size() * SLOT_SIZE;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        SkillsRecordScreen.OverlayColor overlayColor = this.theme.overlay();
        int tint = ARGB.colorFromFloat(overlayColor.alpha(), overlayColor.red(), overlayColor.green(), overlayColor.blue());

        for (int i = 0; i < this.items.size(); i++) {
            int slotX = x + i * SLOT_SIZE;
            GuiDraw.blitSprite(guiGraphics, SLOT_SPRITE, slotX, y, SLOT_SIZE, SLOT_SIZE, tint);

            ItemStack itemStack = this.items.get(i);
            guiGraphics.renderItem(itemStack, slotX + ITEM_OFFSET, y + ITEM_OFFSET, i);
            guiGraphics.renderItemDecorations(font, itemStack, slotX + ITEM_OFFSET, y + ITEM_OFFSET);
        }
    }
}
