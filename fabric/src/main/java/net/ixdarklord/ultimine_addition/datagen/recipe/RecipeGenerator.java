package net.ixdarklord.ultimine_addition.datagen.recipe;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.ixdarklord.coolcat_lib.common.crafting.ConditionalRecipe;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.tag.ModItemTags;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.datagen.recipe.builder.ItemStorageDataRecipeBuilder;
import net.ixdarklord.ultimine_addition.datagen.recipe.builder.MCRecipeBuilder;
import net.ixdarklord.ultimine_addition.datagen.recipe.conditions.LegacyModeCondition;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class RecipeGenerator extends FabricRecipeProvider {
    public RecipeGenerator(FabricDataGenerator generator) {
        super(generator);
    }
    @Override
    protected void generateRecipes(Consumer<FinishedRecipe> consumer) {
        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> ShapedRecipeBuilder.shaped(Registration.CARD_BLUEPRINT.get())
                        .define('A', Items.AMETHYST_SHARD)
                        .define('P', Items.PAPER)
                        .define('L', ConventionalItemTags.LAPIS)
                        .pattern("ALA")
                        .pattern("LPL")
                        .pattern("ALA")
                        .group(Constants.MOD_ID)
                        .unlockedBy("has_amethyst_shard", inventoryTrigger(ItemPredicate.Builder.item().of(Items.AMETHYST_SHARD).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer);

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> ShapelessRecipeBuilder.shapeless(Registration.MINING_SKILL_CARD_EMPTY.get(), 2)
                        .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                        .requires(Registration.CARD_BLUEPRINT.get())
                        .group(Constants.MOD_ID)
                        .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer);

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> MCRecipeBuilder.create(Registration.MINING_SKILL_CARD_PICKAXE.get())
                        .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                        .requires(ServicePlatform.Tags.getPickaxes())
                        .group(Constants.MOD_ID)
                        .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer);

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> MCRecipeBuilder.create(Registration.MINING_SKILL_CARD_AXE.get())
                        .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                        .requires(ServicePlatform.Tags.getAxes())
                        .group(Constants.MOD_ID)
                        .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer);

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> MCRecipeBuilder.create(Registration.MINING_SKILL_CARD_SHOVEL.get())
                        .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                        .requires(ServicePlatform.Tags.getShovels())
                        .group(Constants.MOD_ID)
                        .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer);

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> MCRecipeBuilder.create(Registration.MINING_SKILL_CARD_HOE.get())
                        .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                        .requires(ServicePlatform.Tags.getHoes())
                        .group(Constants.MOD_ID)
                        .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer);

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> ShapedRecipeBuilder.shaped(Registration.SKILLS_RECORD.get())
                        .define('S', ItemTags.WOODEN_SLABS)
                        .define('I', Items.IRON_INGOT)
                        .define('Y', Items.YELLOW_CONCRETE)
                        .define('C', Registration.MINING_SKILL_CARD_EMPTY.get())
                        .pattern("SIS")
                        .pattern("YCY")
                        .pattern("SYS")
                        .group(Constants.MOD_ID)
                        .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer);

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> ShapedRecipeBuilder.shaped(Registration.INK_CHAMBER.get())
                        .define('I', Items.IRON_INGOT)
                        .define('N', Items.IRON_NUGGET)
                        .define('R', Items.RED_DYE)
                        .define('G', Items.GREEN_DYE)
                        .define('B', Items.BLUE_DYE)
                        .pattern("INI")
                        .pattern("RGB")
                        .pattern("INI")
                        .group(Constants.MOD_ID)
                        .unlockedBy("has_skills_record", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.SKILLS_RECORD.get()).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer);

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> ShapedRecipeBuilder.shaped(Registration.PEN.get())
                        .define('G', Items.GOLD_INGOT)
                        .define('S', ModItemTags.SLIMEBALLS_FABRIC)
                        .define('C', Registration.INK_CHAMBER.get())
                        .define('I', Items.IRON_NUGGET)
                        .pattern(" GS")
                        .pattern("GCG")
                        .pattern("IG ")
                        .group(Constants.MOD_ID)
                        .unlockedBy("has_ink_chamber", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.INK_CHAMBER.get()).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer);

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> ItemStorageDataRecipeBuilder.create(Registration.PEN.get())
                        .storage("ink_chamber", 1)
                        .requires(ModItemTags.MORE_VALUABLE_PIGMENT, 50)
                        .requires(ModItemTags.LESS_VALUABLE_PIGMENT, 10)
                        .group(Constants.MOD_ID)
                        .unlockedBy("has_pen", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.PEN.get()).build()))
                        .save(finishedRecipeConsumer, Constants.getLocation("refill")))
                .build(consumer);

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> MCRecipeBuilder.create(Registration.MINER_CERTIFICATE.get())
                        .requires(Items.PAPER)
                        .requires(Registration.MINING_SKILL_CARD_PICKAXE.get(), MiningSkillCardItem.Tier.Mastered)
                        .requires(Registration.MINING_SKILL_CARD_AXE.get(), MiningSkillCardItem.Tier.Mastered)
                        .requires(Registration.MINING_SKILL_CARD_SHOVEL.get(), MiningSkillCardItem.Tier.Mastered)
                        .requires(Registration.MINING_SKILL_CARD_HOE.get(), MiningSkillCardItem.Tier.Mastered)
                        .group(Constants.MOD_ID)
                        .unlockedBy("has_skills_record", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.SKILLS_RECORD.get()).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer);

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(true))
                .addRecipe(finishedRecipeConsumer -> ShapedRecipeBuilder.shaped(Registration.MINER_CERTIFICATE.get())
                        .define('P', Items.PAPER)
                        .define('1', Items.DIAMOND_PICKAXE).define('2', Items.IRON_AXE)
                        .define('3', Items.GOLDEN_HOE).define('4', Items.STONE_SHOVEL)
                        .pattern(" 1 ")
                        .pattern("2P3")
                        .pattern(" 4 ")
                        .unlockedBy("has_miner_certificate", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINER_CERTIFICATE.get()).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer, Constants.getLocation("miner_certificate_legacy"));
    }

    @Override
    public @NotNull String getName() {
        return Constants.MOD_NAME + " Recipes";
    }
}