package net.ixdarklord.ultimine_addition.client.renderer.item;

import net.ixdarklord.coolcatcore.api.client.gui.ItemDecorator;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

// The Mining Skill Card's decorator (MiningSkillCardItem hands it to CoolCatLib as a DecoratedItem): the card's
// potion points, drawn over the card in GUIs as a row of pips along its empty bottom strip (in
// place of a durability bar): one per point the tier holds, filled while it's left. Drawn at the card texture's own
// resolution (32x32, so half a GUI pixel a texel).
public final class PotionPointPips implements ItemDecorator {
    public static final PotionPointPips INSTANCE = new PotionPointPips();
    private static final Identifier FILLED = FTBUltimineAddition.id("item/potion_point");
    private static final Identifier EMPTY = FTBUltimineAddition.id("item/potion_point_empty");
    // The strip under the card's plaque, in the texture's pixels.
    private static final int STRIP_LEFT = 8;
    private static final int STRIP_WIDTH = 16;
    private static final int STRIP_TOP = 25;
    private static final int PIP_WIDTH = 4;
    private static final int PIP_HEIGHT = 3;

    private PotionPointPips() {}

    @Override
    public void extract(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y) {
        if (!(stack.getItem() instanceof MiningSkillCardItem item) || item.getType(stack) == MiningSkillCardItem.Type.EMPTY
                || !stack.has(MiningSkillCardData.DATA_COMPONENT)) return;
        MiningSkillCardData data = item.getData(stack);
        MiningSkillCardItem.Tier tier = data.getTier();
        if (data.isCreativeItem() || tier == MiningSkillCardItem.Tier.Unlearned || tier == MiningSkillCardItem.Tier.Mastered) return;
        if (!data.hasProgress()) return;  // potion points not synced yet
        int max = data.getMaxPotionPoints();
        if (max <= 0) return;
        int left = Math.clamp(data.getPotionPoints(), 0, max);

        // As many pips as the tier holds, centered on the card: every layout is an even number of pixels wide, so it
        // sits exactly on the card's middle. Up to three at full size, more of them narrower, and past eight the strip
        // as one bar of single-pixel pips showing the share that's left.
        int count = max;
        int width, gap;
        if (max <= 3) {
            width = PIP_WIDTH;
            gap = 2;
        } else if (max == 4) {
            width = 2;
            gap = 2;
        } else if (max <= 8) {
            width = 2;
            gap = 0;
        } else {
            count = STRIP_WIDTH;
            width = 1;
            gap = 0;
            left = Math.round(left / (float) max * count);
        }
        int total = count * width + (count - 1) * gap;
        int start = STRIP_LEFT + (STRIP_WIDTH - total) / 2;

        var pose = graphics.pose();
        pose.translate(x, y);
        pose.scale(0.5F, 0.5F);
        for (int i = 0; i < count; i++) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, i < left ? FILLED : EMPTY, start + i * (width + gap), STRIP_TOP, width, PIP_HEIGHT);
        }
    }
}
