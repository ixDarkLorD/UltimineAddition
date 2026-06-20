package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcatlib.api.item.ComponentItem.ComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ModItems {
    public static final Item MINER_CERTIFICATE;
    public static final SkillsRecordItem SKILLS_RECORD;
    public static final ShapeSelectorItem SHAPE_SELECTOR;
    public static final Item INK_CHAMBER;
    public static final PenItem PEN;
    public static final Item CARD_BLUEPRINT;
    public static final MiningSkillCardItem MINING_SKILL_CARD_EMPTY;
    public static final MiningSkillCardItem MINING_SKILL_CARD_PICKAXE;
    public static final MiningSkillCardItem MINING_SKILL_CARD_AXE;
    public static final MiningSkillCardItem MINING_SKILL_CARD_SHOVEL;
    public static final MiningSkillCardItem MINING_SKILL_CARD_HOE;

    static {
        MINER_CERTIFICATE = new MinerCertificateItem((new Item.Properties()).stacksTo(1).rarity(Rarity.EPIC));
        SKILLS_RECORD = new SkillsRecordItem((new Item.Properties()).stacksTo(1));
        SHAPE_SELECTOR = new ShapeSelectorItem((new Item.Properties()).stacksTo(1));
        INK_CHAMBER = new ModernItem((new Item.Properties()).stacksTo(64), ComponentType.CRAFTING);
        PEN = new PenItem((new Item.Properties()).stacksTo(1));
        CARD_BLUEPRINT = new ModernItem((new Item.Properties()).stacksTo(16), ComponentType.CRAFTING);
        MINING_SKILL_CARD_EMPTY = new MiningSkillCardItem((new Item.Properties()).stacksTo(16), MiningSkillCardItem.Type.EMPTY);
        MINING_SKILL_CARD_PICKAXE = new MiningSkillCardItem((new Item.Properties()).stacksTo(1), MiningSkillCardItem.Type.PICKAXE);
        MINING_SKILL_CARD_AXE = new MiningSkillCardItem((new Item.Properties()).stacksTo(1), MiningSkillCardItem.Type.AXE);
        MINING_SKILL_CARD_SHOVEL = new MiningSkillCardItem((new Item.Properties()).stacksTo(1), MiningSkillCardItem.Type.SHOVEL);
        MINING_SKILL_CARD_HOE = new MiningSkillCardItem((new Item.Properties()).stacksTo(1), MiningSkillCardItem.Type.HOE);
    }
}
