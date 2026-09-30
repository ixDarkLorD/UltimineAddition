package net.ixdarklord.ultimine_addition.datagen.advancement;

import net.ixdarklord.ultimine_addition.common.advancement.UltimineObtainTrigger;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static net.ixdarklord.ultimine_addition.common.advancement.AdvancementTriggers.*;

public class AdvancementGenerator extends AdvancementProvider {

    public AdvancementGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, List.of(new Contents()));
    }

    public static class Contents implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<Advancement> consumer) {
            Advancement root = Advancement.Builder.advancement().display(
                            ModItems.MINER_CERTIFICATE,
                            Component.translatable(String.format("itemGroup.%s.tab", FTBUltimineAddition.MOD_ID)),
                            Component.translatable(String.format("advancement.%s.root.desc", FTBUltimineAddition.MOD_ID)),
                            FTBUltimineAddition.getGuiTexture("advancement/adv_background", "png"),
                            FrameType.TASK,
                            false, false, false)
                    .addCriterion("has_early_items", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(Items.DIRT, Items.STONE).of(ItemTags.LOGS).build()))
                    .addCriterion("killed_by_something", KilledTrigger.TriggerInstance.entityKilledPlayer())
                    .addCriterion("killed_something", KilledTrigger.TriggerInstance.playerKilledEntity())
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, FTBUltimineAddition.id("root").toString());

            Advancement shapeSelector = Advancement.Builder.advancement().parent(root).display(
                            ModItems.SHAPE_SELECTOR,
                            Component.translatable(String.format("advancement.%s.craft.shape_selector", FTBUltimineAddition.MOD_ID)),
                            Component.translatable(String.format("advancement.%s.craft", FTBUltimineAddition.MOD_ID), Component.translatable(ModItems.SHAPE_SELECTOR.getDescriptionId())),
                            null,
                            FrameType.TASK,
                            true, true, false)
                    .addCriterion("has_shape_selector", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SHAPE_SELECTOR))
                    .requirements(RequirementsStrategy.AND)
                    .save(consumer, FTBUltimineAddition.id("shape_selector").toString());

            Advancement slime = Advancement.Builder.advancement().parent(root).display(
                            Items.SLIME_BALL,
                            Component.translatable(String.format("advancement.%s.obtain.slime_balls", FTBUltimineAddition.MOD_ID)),
                            Component.translatable(String.format("advancement.%s.obtain", FTBUltimineAddition.MOD_ID), Component.translatable(Items.SLIME_BALL.getDescriptionId())),
                            null,
                            FrameType.TASK,
                            true, true, false)
                    .addCriterion("has_slime_balls", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SLIME_BALL))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, FTBUltimineAddition.id("slime_balls").toString());

            Advancement pen = Advancement.Builder.advancement().parent(slime).display(
                            ModItems.PEN,
                            Component.translatable(String.format("advancement.%s.craft.pen", FTBUltimineAddition.MOD_ID)),
                            Component.translatable(String.format("advancement.%s.craft", FTBUltimineAddition.MOD_ID), Component.translatable(ModItems.PEN.getDescriptionId())),
                            null,
                            FrameType.TASK,
                            true, true, false)
                    .addCriterion("slime_adv", advancementTrigger(slime))
                    .addCriterion("has_pen", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.PEN))
                    .requirements(RequirementsStrategy.AND)
                    .save(consumer, FTBUltimineAddition.id("pen").toString());

            Advancement emptyCard = Advancement.Builder.advancement().parent(root).display(
                            ModItems.MINING_SKILL_CARD_EMPTY,
                            Component.translatable(String.format("advancement.%s.obtain.card.empty", FTBUltimineAddition.MOD_ID)),
                            Component.translatable(String.format("advancement.%s.obtain", FTBUltimineAddition.MOD_ID), Component.translatable(ModItems.MINING_SKILL_CARD_EMPTY.getDescriptionId())),
                            null,
                            FrameType.TASK,
                            true, true, false)
                    .addCriterion("trade_for_empty_card", tradedWithVillager(ItemPredicate.Builder.item().of(ModItems.MINING_SKILL_CARD_EMPTY).build()))
                    .addCriterion("has_empty_card", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MINING_SKILL_CARD_EMPTY))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, FTBUltimineAddition.id("empty_card").toString());

            Advancement skillsRecord = Advancement.Builder.advancement().parent(emptyCard).display(
                            ModItems.SKILLS_RECORD,
                            Component.translatable(String.format("advancement.%s.craft.skills_record", FTBUltimineAddition.MOD_ID)),
                            Component.translatable(String.format("advancement.%s.craft", FTBUltimineAddition.MOD_ID), Component.translatable(ModItems.SKILLS_RECORD.getDescriptionId())),
                            null,
                            FrameType.GOAL,
                            true, true, false)
                    .addCriterion("has_skills_record", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SKILLS_RECORD))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, FTBUltimineAddition.id("skills_record").toString());

            Advancement pickaxeCard = Advancement.Builder.advancement().parent(emptyCard).display(
                            ModItems.MINING_SKILL_CARD_PICKAXE,
                            Component.translatable(String.format("advancement.%s.craft.card.pickaxe", FTBUltimineAddition.MOD_ID)),
                            Component.translatable(String.format("advancement.%s.craft", FTBUltimineAddition.MOD_ID), Component.translatable(ModItems.MINING_SKILL_CARD_PICKAXE.getDescriptionId())),
                            null,
                            FrameType.TASK,
                            true, true, false)
                    .addCriterion("has_pickaxe_card", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MINING_SKILL_CARD_PICKAXE))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, FTBUltimineAddition.id("pickaxe_card").toString());

            Advancement axeCard = Advancement.Builder.advancement().parent(emptyCard).display(
                            ModItems.MINING_SKILL_CARD_AXE,
                            Component.translatable(String.format("advancement.%s.craft.card.axe", FTBUltimineAddition.MOD_ID)),
                            Component.translatable(String.format("advancement.%s.craft", FTBUltimineAddition.MOD_ID), Component.translatable(ModItems.MINING_SKILL_CARD_AXE.getDescriptionId())),
                            null,
                            FrameType.TASK,
                            true, true, false)
                    .addCriterion("has_axe_card", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MINING_SKILL_CARD_AXE))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, FTBUltimineAddition.id("axe_card").toString());

            Advancement shovelCard = Advancement.Builder.advancement().parent(emptyCard).display(
                            ModItems.MINING_SKILL_CARD_SHOVEL,
                            Component.translatable(String.format("advancement.%s.craft.card.shovel", FTBUltimineAddition.MOD_ID)),
                            Component.translatable(String.format("advancement.%s.craft", FTBUltimineAddition.MOD_ID), Component.translatable(ModItems.MINING_SKILL_CARD_SHOVEL.getDescriptionId())),
                            null,
                            FrameType.TASK,
                            true, true, false)
                    .addCriterion("has_shovel_card", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MINING_SKILL_CARD_SHOVEL))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, FTBUltimineAddition.id("shovel_card").toString());

            Advancement hoeCard = Advancement.Builder.advancement().parent(emptyCard).display(
                            ModItems.MINING_SKILL_CARD_HOE,
                            Component.translatable(String.format("advancement.%s.craft.card.hoe", FTBUltimineAddition.MOD_ID)),
                            Component.translatable(String.format("advancement.%s.craft", FTBUltimineAddition.MOD_ID), Component.translatable(ModItems.MINING_SKILL_CARD_HOE.getDescriptionId())),
                            null,
                            FrameType.TASK,
                            true, true, false)
                    .addCriterion("has_hoe_card", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MINING_SKILL_CARD_HOE))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, FTBUltimineAddition.id("hoe_card").toString());

            Advancement ultiminePower = Advancement.Builder.advancement().parent(skillsRecord).display(
                            Util.make(new ItemStack(ModItems.MINER_CERTIFICATE), stack ->
                                    MinerCertificateData.DATA_COMPONENT.set(stack, MinerCertificateData.create().setAccomplished(true))),
                            Component.translatable(String.format("advancement.%s.ultimine_ability", FTBUltimineAddition.MOD_ID)),
                            Component.translatable(String.format("advancement.%s.ultimine_ability.desc", FTBUltimineAddition.MOD_ID), Component.translatable(ModItems.MINER_CERTIFICATE.getDescriptionId())),
                            null,
                            FrameType.CHALLENGE,
                            true, true, false)
                    .addCriterion("has_ultimine_ability", UltimineObtainTrigger.Instance.obtain())
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, FTBUltimineAddition.id("ultimine_ability").toString());
        }
    }
}