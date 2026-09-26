package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.ixdarklord.coolcatlib.api.item.ComponentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ModItems {
    public static final MinerCertificateItem MINER_CERTIFICATE = new MinerCertificateItem(properties("miner_certificate")
            .stacksTo(1)
            .rarity(Rarity.EPIC));
    public static final SkillsRecordItem SKILLS_RECORD = new SkillsRecordItem(properties("skills_record")
            .stacksTo(1));
    public static final ShapeSelectorItem SHAPE_SELECTOR = new ShapeSelectorItem(properties("shape_selector")
            .stacksTo(1));
    public static final ModernItem INK_CHAMBER = new ModernItem(properties("ink_chamber")
            .stacksTo(64), ComponentItem.ComponentType.CRAFTING);
    public static final PenItem PEN = new PenItem(properties("pen")
            .stacksTo(1));
    public static final ModernItem CARD_BLUEPRINT = new ModernItem(properties("card_blueprint")
            .stacksTo(16), ComponentItem.ComponentType.CRAFTING);

    public static final MiningSkillCardItem MINING_SKILL_CARD_EMPTY = new MiningSkillCardItem(MiningSkillCardItem.Type.EMPTY, properties("mining_skill_card_empty")
            .stacksTo(16));
    public static final MiningSkillCardItem MINING_SKILL_CARD_PICKAXE = new MiningSkillCardItem(MiningSkillCardItem.Type.PICKAXE, properties("mining_skill_card_pickaxe")
            .stacksTo(1));
    public static final MiningSkillCardItem MINING_SKILL_CARD_AXE = new MiningSkillCardItem(MiningSkillCardItem.Type.AXE, properties("mining_skill_card_axe")
            .stacksTo(1));
    public static final MiningSkillCardItem MINING_SKILL_CARD_SHOVEL = new MiningSkillCardItem(MiningSkillCardItem.Type.SHOVEL, properties("mining_skill_card_shovel")
            .stacksTo(1));
    public static final MiningSkillCardItem MINING_SKILL_CARD_HOE = new MiningSkillCardItem(MiningSkillCardItem.Type.HOE, properties("mining_skill_card_hoe")
            .stacksTo(1));

    public static Item.Properties properties(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, FTBUltimineAddition.id(name)));
    }
}
