package net.ixdarklord.ultimine_addition.client.renderer.item;

import net.ixdarklord.coolcatcore.api.client.gui.ItemDecorator;
import net.ixdarklord.ultimine_addition.common.data.item.ShapeCertificateData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

// A Shape Certificate for a data pack card type, in GUIs: the built-in tools have a certificate texture each, their
// tool inked on the page, but a data pack type has none, so its certificate is the plain page (the model's fallback)
// with the type's icon item drawn where the tool goes, over a patch of blank paper. ShapeCertificateItem hands it to
// CoolCatLib as a DecoratedItem.
public final class CertificateToolIcon implements ItemDecorator {
    public static final CertificateToolIcon INSTANCE = new CertificateToolIcon();
    // The page's left side, where the tool is inked, in the texture's pixels (32x32, half a GUI pixel a texel).
    private static final int PATCH_LEFT = 6, PATCH_TOP = 9, PATCH_RIGHT = 20, PATCH_BOTTOM = 23;
    private static final int PAPER = 0xFFE9E0C8;
    private static final float ICON_SIZE = 12.0F;
    private final Map<String, ItemStack> icons = new HashMap<>();

    private CertificateToolIcon() {}

    @Override
    public void extract(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y) {
        String tool = ShapeCertificateData.getTool(stack);
        MiningSkillCardItem.Type type = tool == null ? null : MiningSkillCardItem.Type.byId(tool);
        if (type == null || !type.isData()) return;
        ItemStack icon = this.icons.computeIfAbsent(type.getId() + "|" + type.getIcon(), key -> type.iconStack());
        if (icon.isEmpty()) return;

        var pose = graphics.pose();
        pose.translate(x, y);
        pose.scale(0.5F, 0.5F);
        graphics.fill(PATCH_LEFT, PATCH_TOP, PATCH_RIGHT, PATCH_BOTTOM, PAPER);
        // An item is 16 units; the icon is 12 texels.
        pose.translate(PATCH_LEFT + (PATCH_RIGHT - PATCH_LEFT - ICON_SIZE) / 2.0F, PATCH_TOP + (PATCH_BOTTOM - PATCH_TOP - ICON_SIZE) / 2.0F);
        pose.scale(ICON_SIZE / 16.0F, ICON_SIZE / 16.0F);
        graphics.item(icon, 0, 0);
    }
}
