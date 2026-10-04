package net.ixdarklord.ultimine_addition.client.renderer.item;

import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.ShapeCertificateData;
import net.ixdarklord.ultimine_addition.common.item.MinerCertificateItem;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.item.ShapeCertificateItem;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 1.20.1 has no item model definitions: a card's tier, whether the Miner Certificate is opened, a Shape
// Certificate's tool and a Skills Record's dye are item properties, which the item models' overrides pick their
// model by (see ItemModelDataProvider). Registered per loader.
public final class UAItemModels {
    // A card's tier: its value (Unlearned 0 ... Mastered 4) / 4.
    public static final ResourceLocation MINING_SKILL_CARD_TIER = FTBUltimineAddition.id("tier");
    public static final ResourceLocation CERTIFICATE_OPENED = FTBUltimineAddition.id("certificate_opened");
    // A Shape Certificate's tool: its position in CERTIFICATE_TOOLS / 4, 0 for none (or a custom card type).
    public static final ResourceLocation CERTIFICATE_TOOL = FTBUltimineAddition.id("tool");
    // A Skills Record's dye: its id (white 0 ... black 15) / 16; an undyed record is white.
    public static final ResourceLocation SKILLS_RECORD_DYE = FTBUltimineAddition.id("dye");
    public static final List<MiningSkillCardItem.Type> CERTIFICATE_TOOLS = List.of(MiningSkillCardItem.Type.PICKAXE,
            MiningSkillCardItem.Type.AXE, MiningSkillCardItem.Type.SHOVEL, MiningSkillCardItem.Type.HOE);

    @FunctionalInterface
    public interface PropertyRegistry {
        void register(Item item, ResourceLocation id, ClampedItemPropertyFunction property);
    }

    public static void registerProperties(PropertyRegistry registry) {
        registry.register(ModItems.MINER_CERTIFICATE, CERTIFICATE_OPENED, (stack, level, entity, seed) -> MinerCertificateItem.isAccomplished(stack) ? 1.0F : 0.0F);
        for (MiningSkillCardItem.Type type : MiningSkillCardItem.Type.TYPES) {
            if (type == MiningSkillCardItem.Type.EMPTY) continue;
            Item item = BuiltInRegistries.ITEM.get(type.getRegistryId());
            if (!(item instanceof MiningSkillCardItem)) continue;
            registry.register(item, MINING_SKILL_CARD_TIER, (stack, level, entity, seed) -> tierValue(MiningSkillCardData.load(stack).getTier()));
        }
        // The card of the data pack types: it looks like the built-in cards, its tool drawn over it.
        registry.register(ModItems.MINING_SKILL_CARD_GENERIC, MINING_SKILL_CARD_TIER, (stack, level, entity, seed) -> tierValue(MiningSkillCardData.load(stack).getTier()));
        registry.register(ModItems.SKILLS_RECORD, SKILLS_RECORD_DYE, (stack, level, entity, seed) -> dyeValue(SkillsRecordItem.getColorOrDefault(stack)));
        for (ShapeCertificateItem certificate : ShapeCertificateItem.all()) {
            registry.register(certificate, CERTIFICATE_TOOL, (stack, level, entity, seed) -> toolValue(ShapeCertificateData.getTool(stack)));
        }
    }

    public static float tierValue(MiningSkillCardItem.Tier tier) {
        return tier.getValue() / 4.0F;
    }

    public static float dyeValue(DyeColor dye) {
        return dye.getId() / 16.0F;
    }

    public static float toolValue(@Nullable String tool) {
        for (int i = 0; i < CERTIFICATE_TOOLS.size(); i++) {
            if (CERTIFICATE_TOOLS.get(i).getId().equals(tool)) return (i + 1) / 4.0F;
        }
        return 0.0F;
    }

    private UAItemModels() {}
}
