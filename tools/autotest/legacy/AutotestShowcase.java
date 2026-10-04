package net.ixdarklord.ultimine_addition.client.event;

import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.record.CardProgress;
import net.ixdarklord.ultimine_addition.common.data.record.CardSync;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordClientCache;
import net.ixdarklord.ultimine_addition.common.item.MinerCertificateItem;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ShapeCertificateItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

// The autotest's item showcase for the 1.21.1 and 1.20.1 versions (copied in by tools/autotest/run.py, never committed):
// the redrawn items, large, each with its GUI decorations: the cards with their potion-point pips (the CoolCatLib
// item decorator), the Completion Envelope, the certificates, the Pen and the Ink Chamber. It needs no world: the
// cards' potion points are put straight into the client's card cache.
public final class AutotestShowcase extends Screen {
    public static int frames;
    public static Throwable error;
    private final List<ItemStack> stacks = new ArrayList<>();

    public AutotestShowcase() {
        super(Component.literal("Autotest showcase"));
        this.stacks.add(new ItemStack(item("mining_skill_card_empty")));
        this.stacks.add(card("pickaxe", MiningSkillCardItem.Tier.Unlearned, 0));
        this.stacks.add(card("pickaxe", MiningSkillCardItem.Tier.Novice, 2));       // 2 of 3
        this.stacks.add(card("axe", MiningSkillCardItem.Tier.Apprentice, 1));       // 1 of 2
        this.stacks.add(card("shovel", MiningSkillCardItem.Tier.Adept, 1));         // 1 of 1
        this.stacks.add(card("hoe", MiningSkillCardItem.Tier.Mastered, 0));
        this.stacks.add(new ItemStack(Registration.MINER_CERTIFICATE.get()));        // sealed: the Completion Envelope
        ItemStack earned = new ItemStack(Registration.MINER_CERTIFICATE.get());
        ((MinerCertificateItem) earned.getItem()).getData(earned).setAccomplished(true).save();
        this.stacks.add(earned);
        this.stacks.add(((ShapeCertificateItem) Registration.SHAPE_CERTIFICATE_NOVICE.get()).create(MiningSkillCardItem.Type.PICKAXE));
        this.stacks.add(((ShapeCertificateItem) Registration.SHAPE_CERTIFICATE_APPRENTICE.get()).create(MiningSkillCardItem.Type.AXE));
        this.stacks.add(((ShapeCertificateItem) Registration.SHAPE_CERTIFICATE_ADEPT.get()).create(MiningSkillCardItem.Type.HOE));
        this.stacks.add(new ItemStack(Registration.PEN.get()));
        this.stacks.add(new ItemStack(Registration.INK_CHAMBER.get()));
        this.stacks.add(new ItemStack(Registration.SKILLS_RECORD.get()));
    }

    private static Item item(String name) {
        return BuiltInRegistries.ITEM.get(FTBUltimineAddition.id(name));
    }

    // A card of a tier, its potion points known to the client as if a server had synced them.
    private static ItemStack card(String tool, MiningSkillCardItem.Tier tier, int points) {
        MiningSkillCardItem item = (MiningSkillCardItem) item("mining_skill_card_" + tool);
        ItemStack stack = new ItemStack(item);
        MiningSkillCardData data = item.getData(stack);
        data.setTier(tier);
        data.writeComponent();
        SkillsRecordClientCache.acceptCards(List.of(new CardSync(item.getData(stack).getUUID(), ItemStack.EMPTY,
                new CardProgress(List.of(), points, List.of()), null)));
        return stack;
    }

    public String names() {
        List<String> names = new ArrayList<>();
        for (ItemStack stack : this.stacks) names.add(stack.getHoverName().getString());
        return names.toString();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xFF3A3A40);
        try {
            float scale = Math.max(1.0F, Math.min(this.width / (7 * 20.0F + 8), this.height / (2 * 22.0F + 8)));
            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, 1.0F);
            for (int i = 0; i < this.stacks.size(); i++) {
                int x = 6 + (i % 7) * 20, y = 6 + (i / 7) * 22;
                graphics.renderItem(this.stacks.get(i), x, y);
                graphics.renderItemDecorations(this.font, this.stacks.get(i), x, y);
            }
            graphics.pose().popPose();
            frames++;
        } catch (Throwable e) {
            error = e;
        }
    }
}
