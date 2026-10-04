package net.ixdarklord.ultimine_addition.client.renderer.item;

import net.ixdarklord.coolcatcore.api.client.gui.ItemDecorator;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

// The Mining Skill Card's tool, drawn on the card's plate in GUIs (MiningSkillCardItem hands it to CoolCatLib as a
// DecoratedItem): the card textures leave the plate empty, and the tool's own item is drawn there, small. The built-in
// cards show their netherite tool; a data pack card shows the item its type names as "icon".
public final class CardToolIcon implements ItemDecorator {
    public static final CardToolIcon INSTANCE = new CardToolIcon();
    // The plate, in the card texture's pixels (32x32, half a GUI pixel a texel): the icon is 12 of them wide.
    private static final float PLATE_LEFT = 10.0F;
    private static final float PLATE_TOP = 10.5F;
    private static final float ICON_SIZE = 12.0F;
    private final Map<String, ItemStack> icons = new HashMap<>();

    private CardToolIcon() {}

    /** Draws the card's tool over a card drawn outside a slot, where decorators don't run. */
    public static void drawOver(GuiGraphicsExtractor graphics, ItemStack card, int x, int y) {
        graphics.pose().pushMatrix();
        INSTANCE.extract(graphics, Minecraft.getInstance().font, card, x, y);
        graphics.pose().popMatrix();
    }

    @Override
    public void extract(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y) {
        if (!(stack.getItem() instanceof MiningSkillCardItem item)) return;
        MiningSkillCardItem.Type type = item.getType(stack);
        if (type == MiningSkillCardItem.Type.EMPTY) return;
        // One stack per type and icon: a data pack reload can give a type another icon.
        ItemStack icon = this.icons.computeIfAbsent(type.getId() + "|" + type.getIcon(), key -> type.iconStack());
        if (icon.isEmpty()) return;

        var pose = graphics.pose();
        pose.translate(x + PLATE_LEFT / 2.0F, y + PLATE_TOP / 2.0F);
        // An item is 16 GUI pixels; the plate's icon is 12 texels, so 6.
        float scale = ICON_SIZE / 2.0F / 16.0F;
        pose.scale(scale, scale);
        graphics.item(icon, 0, 0);
    }
}
