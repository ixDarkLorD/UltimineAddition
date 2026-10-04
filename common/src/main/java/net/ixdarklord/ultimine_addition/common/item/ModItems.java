package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcatcore.api.item.ComponentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ModItems {
    public static final MinerCertificateItem MINER_CERTIFICATE = new MinerCertificateItem(properties("miner_certificate")
            .stacksTo(1)
            .rarity(Rarity.EPIC));
    public static final ShapeCertificateItem SHAPE_CERTIFICATE_NOVICE = new ShapeCertificateItem(MiningSkillCardItem.Tier.Novice, properties("shape_certificate_novice")
            .stacksTo(16)
            .rarity(Rarity.UNCOMMON));
    public static final ShapeCertificateItem SHAPE_CERTIFICATE_APPRENTICE = new ShapeCertificateItem(MiningSkillCardItem.Tier.Apprentice, properties("shape_certificate_apprentice")
            .stacksTo(16)
            .rarity(Rarity.RARE));
    public static final ShapeCertificateItem SHAPE_CERTIFICATE_ADEPT = new ShapeCertificateItem(MiningSkillCardItem.Tier.Adept, properties("shape_certificate_adept")
            .stacksTo(16)
            .rarity(Rarity.RARE));
    public static final SkillsRecordItem SKILLS_RECORD = new SkillsRecordItem(properties("skills_record")
            .stacksTo(1));
    public static final ShapeSelectorItem SHAPE_SELECTOR = new ShapeSelectorItem(properties("shape_selector")
            .stacksTo(1));
    public static final ModernItem INK_CHAMBER = new ModernItem(properties("ink_chamber")
            .stacksTo(64), ComponentItem.ComponentType.CRAFTING);
    public static final PenItem PEN = new PenItem(properties("pen")
            .stacksTo(1));

    public static final MiningSkillCardItem MINING_SKILL_CARD_EMPTY = new MiningSkillCardItem(MiningSkillCardItem.Type.EMPTY, properties("mining_skill_card_empty")
            .stacksTo(16));
    public static final GenericMiningSkillCardItem MINING_SKILL_CARD_GENERIC = new GenericMiningSkillCardItem(properties("mining_skill_card_generic")
            .stacksTo(1));
    public static final GenericMineGoJuiceItem MINE_GO_JUICE_GENERIC = new GenericMineGoJuiceItem(properties("mine_go_juice_generic")
            .stacksTo(1));
    public static final MiningSkillCardItem MINING_SKILL_CARD_PICKAXE = new MiningSkillCardItem(MiningSkillCardItem.Type.PICKAXE, properties("mining_skill_card_pickaxe")
            .stacksTo(1));
    public static final MiningSkillCardItem MINING_SKILL_CARD_AXE = new MiningSkillCardItem(MiningSkillCardItem.Type.AXE, properties("mining_skill_card_axe")
            .stacksTo(1));
    public static final MiningSkillCardItem MINING_SKILL_CARD_SHOVEL = new MiningSkillCardItem(MiningSkillCardItem.Type.SHOVEL, properties("mining_skill_card_shovel")
            .stacksTo(1));
    public static final MiningSkillCardItem MINING_SKILL_CARD_HOE = new MiningSkillCardItem(MiningSkillCardItem.Type.HOE, properties("mining_skill_card_hoe")
            .stacksTo(1));

    // 1.20.1 items don't carry their id in their properties; the name only keeps the declarations as on 26.1.2.
    public static Item.Properties properties(String name) {
        return new Item.Properties();
    }
}
