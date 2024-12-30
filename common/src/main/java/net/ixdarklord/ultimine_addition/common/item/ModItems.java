package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcatlib.api.item.ComponentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ModItems {
    public static final MinerCertificateItem MINER_CERTIFICATE = new MinerCertificateItem(new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.EPIC));
    public static final SkillsRecordItem SKILLS_RECORD = new SkillsRecordItem(new Item.Properties()
            .stacksTo(1));
    public static final ModernItem INK_CHAMBER = new ModernItem(new Item.Properties()
            .stacksTo(64), ComponentItem.ComponentType.CRAFTING);
    public static final PenItem PEN = new PenItem(new Item.Properties()
            .stacksTo(1));
    public static final ModernItem CARD_BLUEPRINT = new ModernItem(new Item.Properties()
            .stacksTo(16), ComponentItem.ComponentType.CRAFTING);

    public static final MiningSkillCardItem MINING_SKILL_CARD_EMPTY = new MiningSkillCardItem(MiningSkillCardItem.Type.EMPTY, new Item.Properties()
            .stacksTo(16));
    public static final MiningSkillCardItem MINING_SKILL_CARD_PICKAXE = new MiningSkillCardItem(MiningSkillCardItem.Type.PICKAXE, new Item.Properties()
            .stacksTo(1));
    public static final MiningSkillCardItem MINING_SKILL_CARD_AXE = new MiningSkillCardItem(MiningSkillCardItem.Type.AXE, new Item.Properties()
            .stacksTo(1));
    public static final MiningSkillCardItem MINING_SKILL_CARD_SHOVEL = new MiningSkillCardItem(MiningSkillCardItem.Type.SHOVEL, new Item.Properties()
            .stacksTo(1));
    public static final MiningSkillCardItem MINING_SKILL_CARD_HOE = new MiningSkillCardItem(MiningSkillCardItem.Type.HOE, new Item.Properties()
            .stacksTo(1));
}
