package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcat_lib.common.item.ComponentItem;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ModItems {
    public static final Item MINER_CERTIFICATE = new MinerCertificateItem(new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.EPIC)
            .arch$tab(Registration.ULTIMINE_ADDITION_TAB));
    public static final Item SKILLS_RECORD = new SkillsRecordItem(new Item.Properties()
            .stacksTo(1)
            .arch$tab(Registration.ULTIMINE_ADDITION_TAB));
    public static final Item INK_CHAMBER = new ComponentItem(new Item.Properties()
            .stacksTo(64)
            .arch$tab(Registration.ULTIMINE_ADDITION_TAB), ComponentItem.ComponentType.CRAFTING);
    public static final Item PEN = new PenItem(new Item.Properties()
            .stacksTo(1)
            .arch$tab(Registration.ULTIMINE_ADDITION_TAB));
    public static final Item CARD_BLUEPRINT = new ComponentItem(new Item.Properties()
            .stacksTo(16)
            .arch$tab(Registration.ULTIMINE_ADDITION_TAB), ComponentItem.ComponentType.CRAFTING);

    public static final Item MINING_SKILL_CARD_EMPTY = new MiningSkillCardItem(new Item.Properties()
            .stacksTo(1)
            .arch$tab(Registration.ULTIMINE_ADDITION_TAB), MiningSkillCardItem.Type.EMPTY);
    public static final Item MINING_SKILL_CARD_PICKAXE = new MiningSkillCardItem(new Item.Properties()
            .stacksTo(1)
            .arch$tab(Registration.ULTIMINE_ADDITION_TAB), MiningSkillCardItem.Type.PICKAXE);
    public static final Item MINING_SKILL_CARD_AXE = new MiningSkillCardItem(new Item.Properties()
            .stacksTo(1)
            .arch$tab(Registration.ULTIMINE_ADDITION_TAB), MiningSkillCardItem.Type.AXE);
    public static final Item MINING_SKILL_CARD_SHOVEL = new MiningSkillCardItem(new Item.Properties()
            .stacksTo(1)
            .arch$tab(Registration.ULTIMINE_ADDITION_TAB), MiningSkillCardItem.Type.SHOVEL);
    public static final Item MINING_SKILL_CARD_HOE = new MiningSkillCardItem(new Item.Properties()
            .stacksTo(1)
            .arch$tab(Registration.ULTIMINE_ADDITION_TAB), MiningSkillCardItem.Type.HOE);
}
