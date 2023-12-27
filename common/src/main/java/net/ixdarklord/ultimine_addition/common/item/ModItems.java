package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcat_lib.common.item.ComponentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ModItems {
    public static final Item MINER_CERTIFICATE = new MinerCertificateItem(new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.EPIC));
    public static final Item SKILLS_RECORD = new SkillsRecordItem(new Item.Properties()
            .stacksTo(1));
    public static final Item INK_CHAMBER = new ModernItem(new Item.Properties()
            .stacksTo(64), ComponentItem.ComponentType.CRAFTING);
    public static final Item PEN = new PenItem(new Item.Properties()
            .stacksTo(1));
    public static final Item CARD_BLUEPRINT = new ModernItem(new Item.Properties()
            .stacksTo(16), ComponentItem.ComponentType.CRAFTING);

    public static final Item MINING_SKILL_CARD_EMPTY = new MiningSkillCardItem(new Item.Properties()
            .stacksTo(1), MiningSkillCardItem.Type.EMPTY);
    public static final Item MINING_SKILL_CARD_PICKAXE = new MiningSkillCardItem(new Item.Properties()
            .stacksTo(1), MiningSkillCardItem.Type.PICKAXE);
    public static final Item MINING_SKILL_CARD_AXE = new MiningSkillCardItem(new Item.Properties()
            .stacksTo(1), MiningSkillCardItem.Type.AXE);
    public static final Item MINING_SKILL_CARD_SHOVEL = new MiningSkillCardItem(new Item.Properties()
            .stacksTo(1), MiningSkillCardItem.Type.SHOVEL);
    public static final Item MINING_SKILL_CARD_HOE = new MiningSkillCardItem(new Item.Properties()
            .stacksTo(1), MiningSkillCardItem.Type.HOE);
}
