package net.ixdarklord.ultimine_addition.datagen.advancement;

import net.ixdarklord.ultimine_addition.common.advancement.AdvancementTriggers;
import net.ixdarklord.ultimine_addition.common.advancement.UltimineObtainTrigger;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AdvancementGenerator extends AdvancementProvider {
   public AdvancementGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
      super(output, registries, List.of(new net.ixdarklord.ultimine_addition.datagen.advancement.AdvancementGenerator.Contents()));
   }

   public static class Contents implements AdvancementProvider.AdvancementGenerator {
      public void generate(HolderLookup.Provider registries, Consumer<Advancement> consumer) {
         Advancement root = Builder.advancement().display(Registration.MINER_CERTIFICATE.get(), Component.translatable(String.format("itemGroup.%s.tab", "ultimine_addition")), Component.translatable(String.format("advancement.%s.root.desc", "ultimine_addition")), FTBUltimineAddition.getGuiTexture("advancement/adv_background", "png"), FrameType.TASK, false, false, false).addCriterion("has_early_items", TriggerInstance.hasItems(ItemPredicate.Builder.item().of(new ItemLike[]{Items.DIRT, Items.STONE}).of(ItemTags.LOGS).build())).addCriterion("killed_by_something", net.minecraft.advancements.critereon.KilledTrigger.TriggerInstance.entityKilledPlayer()).addCriterion("killed_something", net.minecraft.advancements.critereon.KilledTrigger.TriggerInstance.playerKilledEntity()).requirements(RequirementsStrategy.OR).save(consumer, FTBUltimineAddition.id("root").toString());
         Advancement amethyst = Builder.advancement().parent(root).display(Items.AMETHYST_SHARD, Component.translatable(String.format("advancement.%s.amethyst_gathering", "ultimine_addition")), Component.translatable(String.format("advancement.%s.obtain", "ultimine_addition"), Items.AMETHYST_SHARD.getDefaultInstance().getHoverName()), null, FrameType.TASK, true, true, false).addCriterion("has_amethyst", TriggerInstance.hasItems(Items.AMETHYST_SHARD)).requirements(RequirementsStrategy.OR).save(consumer, FTBUltimineAddition.id("gathering_amethyst").toString());
         Advancement cardBlueprint = Builder.advancement().parent(amethyst).display(Registration.CARD_BLUEPRINT.get(), Component.translatable(String.format("advancement.%s.craft.card_blueprint", "ultimine_addition")), Component.translatable(String.format("advancement.%s.craft", "ultimine_addition"), Registration.CARD_BLUEPRINT.get().getDefaultInstance().getHoverName()), null, FrameType.TASK, true, true, false).addCriterion("amethyst_adv", AdvancementTriggers.advancementTrigger(amethyst)).addCriterion("has_card_blueprint", TriggerInstance.hasItems(Registration.CARD_BLUEPRINT.get())).requirements(RequirementsStrategy.AND).save(consumer, FTBUltimineAddition.id("card_blueprint").toString());
         Advancement shapeSelector = Builder.advancement().parent(cardBlueprint).display(Registration.SHAPE_SELECTOR.get(), Component.translatable(String.format("advancement.%s.craft.shape_selector", "ultimine_addition")), Component.translatable(String.format("advancement.%s.craft", "ultimine_addition"), Registration.SHAPE_SELECTOR.get().getDefaultInstance().getHoverName()), null, FrameType.TASK, true, true, false).addCriterion("has_card_blueprint", TriggerInstance.hasItems(Registration.CARD_BLUEPRINT.get())).addCriterion("has_shape_selector", TriggerInstance.hasItems(Registration.SHAPE_SELECTOR.get())).requirements(RequirementsStrategy.AND).save(consumer, FTBUltimineAddition.id("shape_selector").toString());
         Advancement slime = Builder.advancement().parent(root).display(Items.SLIME_BALL, Component.translatable(String.format("advancement.%s.obtain.slime_balls", "ultimine_addition")), Component.translatable(String.format("advancement.%s.obtain", "ultimine_addition"), Items.SLIME_BALL.getDefaultInstance().getHoverName()), null, FrameType.TASK, true, true, false).addCriterion("has_slime_balls", TriggerInstance.hasItems(Items.SLIME_BALL)).requirements(RequirementsStrategy.OR).save(consumer, FTBUltimineAddition.id("slime_balls").toString());
         Advancement pen = Builder.advancement().parent(slime).display(Registration.PEN.get(), Component.translatable(String.format("advancement.%s.craft.pen", "ultimine_addition")), Component.translatable(String.format("advancement.%s.craft", "ultimine_addition"), Registration.PEN.get().getDefaultInstance().getHoverName()), null, FrameType.TASK, true, true, false).addCriterion("slime_adv", AdvancementTriggers.advancementTrigger(slime)).addCriterion("has_pen", TriggerInstance.hasItems(Registration.PEN.get())).requirements(RequirementsStrategy.AND).save(consumer, FTBUltimineAddition.id("pen").toString());
         Advancement emptyCard = Builder.advancement().parent(root).display(Registration.MINING_SKILL_CARD_EMPTY.get(), Component.translatable(String.format("advancement.%s.obtain.card.empty", "ultimine_addition")), Component.translatable(String.format("advancement.%s.obtain", "ultimine_addition"), Registration.MINING_SKILL_CARD_EMPTY.get().getDefaultInstance().getHoverName()), null, FrameType.TASK, true, true, false).addCriterion("trade_for_empty_card", AdvancementTriggers.tradedWithVillager(net.minecraft.advancements.critereon.ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()))).addCriterion("has_empty_card", TriggerInstance.hasItems(Registration.MINING_SKILL_CARD_EMPTY.get())).requirements(RequirementsStrategy.OR).save(consumer, FTBUltimineAddition.id("empty_card").toString());
         Advancement skillsRecord = Builder.advancement().parent(emptyCard).display(Registration.SKILLS_RECORD.get(), Component.translatable(String.format("advancement.%s.craft.skills_record", "ultimine_addition")), Component.translatable(String.format("advancement.%s.craft", "ultimine_addition"), Registration.SKILLS_RECORD.get().getDefaultInstance().getHoverName()), null, FrameType.GOAL, true, true, false).addCriterion("has_skills_record", TriggerInstance.hasItems(Registration.SKILLS_RECORD.get())).requirements(RequirementsStrategy.OR).save(consumer, FTBUltimineAddition.id("skills_record").toString());
         Advancement pickaxeCard = Builder.advancement().parent(emptyCard).display(Registration.MINING_SKILL_CARD_PICKAXE.get(), Component.translatable(String.format("advancement.%s.craft.card.pickaxe", "ultimine_addition")), Component.translatable(String.format("advancement.%s.craft", "ultimine_addition"), Registration.MINING_SKILL_CARD_PICKAXE.get().getDefaultInstance().getHoverName()), null, FrameType.TASK, true, true, false).addCriterion("has_pickaxe_card", TriggerInstance.hasItems(Registration.MINING_SKILL_CARD_PICKAXE.get())).requirements(RequirementsStrategy.OR).save(consumer, FTBUltimineAddition.id("pickaxe_card").toString());
         Advancement axeCard = Builder.advancement().parent(emptyCard).display(Registration.MINING_SKILL_CARD_AXE.get(), Component.translatable(String.format("advancement.%s.craft.card.axe", "ultimine_addition")), Component.translatable(String.format("advancement.%s.craft", "ultimine_addition"), Registration.MINING_SKILL_CARD_AXE.get().getDefaultInstance().getHoverName()), null, FrameType.TASK, true, true, false).addCriterion("has_axe_card", TriggerInstance.hasItems(Registration.MINING_SKILL_CARD_AXE.get())).requirements(RequirementsStrategy.OR).save(consumer, FTBUltimineAddition.id("axe_card").toString());
         Advancement shovelCard = Builder.advancement().parent(emptyCard).display(Registration.MINING_SKILL_CARD_SHOVEL.get(), Component.translatable(String.format("advancement.%s.craft.card.shovel", "ultimine_addition")), Component.translatable(String.format("advancement.%s.craft", "ultimine_addition"), Registration.MINING_SKILL_CARD_SHOVEL.get().getDefaultInstance().getHoverName()), null, FrameType.TASK, true, true, false).addCriterion("has_shovel_card", TriggerInstance.hasItems(Registration.MINING_SKILL_CARD_SHOVEL.get())).requirements(RequirementsStrategy.OR).save(consumer, FTBUltimineAddition.id("shovel_card").toString());
         Advancement hoeCard = Builder.advancement().parent(emptyCard).display(Registration.MINING_SKILL_CARD_HOE.get(), Component.translatable(String.format("advancement.%s.craft.card.hoe", "ultimine_addition")), Component.translatable(String.format("advancement.%s.craft", "ultimine_addition"), Registration.MINING_SKILL_CARD_HOE.get().getDefaultInstance().getHoverName()), null, FrameType.TASK, true, true, false).addCriterion("has_hoe_card", TriggerInstance.hasItems(Registration.MINING_SKILL_CARD_HOE.get())).requirements(RequirementsStrategy.OR).save(consumer, FTBUltimineAddition.id("hoe_card").toString());
         Advancement ultiminePower = Builder.advancement().parent(skillsRecord).display(Util.make(() -> {
            ItemStack stack = Registration.MINER_CERTIFICATE.get().getDefaultInstance();
            MinerCertificateData.load(stack).setAccomplished(true).save();
            return stack;
         }), Component.translatable(String.format("advancement.%s.ultimine_ability", "ultimine_addition")), Component.translatable(String.format("advancement.%s.ultimine_ability.desc", "ultimine_addition"), Registration.MINER_CERTIFICATE.get().getDefaultInstance().getHoverName()), null, FrameType.CHALLENGE, true, true, false).addCriterion("has_ultimine_ability", UltimineObtainTrigger.Instance.obtain()).requirements(RequirementsStrategy.OR).save(consumer, FTBUltimineAddition.id("ultimine_ability").toString());
      }
   }
}
