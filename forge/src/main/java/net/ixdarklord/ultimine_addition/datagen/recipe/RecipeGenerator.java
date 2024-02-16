package net.ixdarklord.ultimine_addition.datagen.recipe;

import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.tag.ModItemTags;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.datagen.recipe.builder.ItemStorageDataRecipeBuilder;
import net.ixdarklord.ultimine_addition.datagen.recipe.builder.MCRecipeBuilder;
import net.ixdarklord.ultimine_addition.datagen.recipe.conditions.LegacyModeCondition;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class RecipeGenerator extends RecipeProvider {

    public RecipeGenerator(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        CraftingHelper.register(LegacyModeCondition.Serializer.INSTANCE);
        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer ->
                        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.CARD_BLUEPRINT.get())
                                .define('A', Items.AMETHYST_SHARD)
                                .define('P', Items.PAPER)
                                .define('L', Tags.Items.GEMS_LAPIS)
                                .pattern("ALA")
                                .pattern("LPL")
                                .pattern("ALA")
                                .group(UltimineAddition.MOD_ID)
                                .unlockedBy("has_amethyst_shard", inventoryTrigger(ItemPredicate.Builder.item().of(Items.AMETHYST_SHARD).build()))
                                .save(finishedRecipeConsumer))
                .build(consumer, Registration.CARD_BLUEPRINT.getId());

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer ->
                        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.MINING_SKILL_CARD_EMPTY.get(), 2)
                                .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                                .requires(Registration.CARD_BLUEPRINT.get())
                                .group(UltimineAddition.MOD_ID)
                                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                                .save(finishedRecipeConsumer))
                .build(consumer, Registration.MINING_SKILL_CARD_EMPTY.getId());

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer ->
                        MCRecipeBuilder.create(RecipeCategory.MISC, Registration.MINING_SKILL_CARD_PICKAXE.get())
                                .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                                .requires(ItemTags.PICKAXES)
                                .group(UltimineAddition.MOD_ID)
                                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                                .save(finishedRecipeConsumer))
                .build(consumer, Registration.MINING_SKILL_CARD_PICKAXE.getId());


        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer ->
                        MCRecipeBuilder.create(RecipeCategory.MISC, Registration.MINING_SKILL_CARD_AXE.get())
                                .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                                .requires(ItemTags.AXES)
                                .group(UltimineAddition.MOD_ID)
                                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                                .save(finishedRecipeConsumer))
                .build(consumer, Registration.MINING_SKILL_CARD_AXE.getId());

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer ->
                        MCRecipeBuilder.create(RecipeCategory.MISC, Registration.MINING_SKILL_CARD_SHOVEL.get())
                                .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                                .requires(ItemTags.SHOVELS)
                                .group(UltimineAddition.MOD_ID)
                                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                                .save(finishedRecipeConsumer))
                .build(consumer, Registration.MINING_SKILL_CARD_SHOVEL.getId());

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer ->
                        MCRecipeBuilder.create(RecipeCategory.MISC, Registration.MINING_SKILL_CARD_HOE.get())
                                .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                                .requires(ItemTags.HOES)
                                .group(UltimineAddition.MOD_ID)
                                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                                .save(finishedRecipeConsumer))
                .build(consumer, Registration.MINING_SKILL_CARD_HOE.getId());


        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.SKILLS_RECORD.get())
                        .define('S', ItemTags.WOODEN_SLABS)
                        .define('I', Items.IRON_INGOT)
                        .define('Y', Items.YELLOW_CONCRETE)
                        .define('C', Registration.MINING_SKILL_CARD_EMPTY.get())
                        .pattern("SIS")
                        .pattern("YCY")
                        .pattern("SYS")
                        .group(UltimineAddition.MOD_ID)
                        .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer, Registration.SKILLS_RECORD.getId());

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.INK_CHAMBER.get())
                        .define('I', Items.IRON_INGOT)
                        .define('N', Items.IRON_NUGGET)
                        .define('R', Items.RED_DYE)
                        .define('G', Items.GREEN_DYE)
                        .define('B', Items.BLUE_DYE)
                        .pattern("INI")
                        .pattern("RGB")
                        .pattern("INI")
                        .group(UltimineAddition.MOD_ID)
                        .unlockedBy("has_skills_record", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.SKILLS_RECORD.get()).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer, Registration.INK_CHAMBER.getId());

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.PEN.get())
                        .define('G', Items.GOLD_INGOT)
                        .define('S', Tags.Items.SLIMEBALLS)
                        .define('C', Registration.INK_CHAMBER.get())
                        .define('I', Items.IRON_NUGGET)
                        .pattern(" GS")
                        .pattern("GCG")
                        .pattern("IG ")
                        .group(UltimineAddition.MOD_ID)
                        .unlockedBy("has_ink_chamber", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.INK_CHAMBER.get()).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer, Registration.PEN.getId());

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> ItemStorageDataRecipeBuilder.create(RecipeCategory.MISC, Registration.PEN.get())
                        .storage("ink_chamber", 1)
                        .requires(ModItemTags.MORE_VALUABLE_PIGMENT, 50)
                        .requires(ModItemTags.LESS_VALUABLE_PIGMENT, 10)
                        .group(UltimineAddition.MOD_ID)
                        .unlockedBy("has_pen", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.PEN.get()).build()))
                        .save(finishedRecipeConsumer, UltimineAddition.getLocation("refill")))
                .build(consumer, UltimineAddition.getLocation("%s_refill".formatted(Registration.PEN.getId().getPath())));

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(false))
                .addRecipe(finishedRecipeConsumer -> MCRecipeBuilder.create(RecipeCategory.MISC, Registration.MINER_CERTIFICATE.get())
                        .requires(Items.PAPER)
                        .requires(Registration.MINING_SKILL_CARD_PICKAXE.get(), MiningSkillCardItem.Tier.Mastered)
                        .requires(Registration.MINING_SKILL_CARD_AXE.get(), MiningSkillCardItem.Tier.Mastered)
                        .requires(Registration.MINING_SKILL_CARD_SHOVEL.get(), MiningSkillCardItem.Tier.Mastered)
                        .requires(Registration.MINING_SKILL_CARD_HOE.get(), MiningSkillCardItem.Tier.Mastered)
                        .group(UltimineAddition.MOD_ID)
                        .unlockedBy("has_skills_record", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.SKILLS_RECORD.get()).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer, Registration.MINER_CERTIFICATE.getId());

        ConditionalRecipe.builder()
                .addCondition(new LegacyModeCondition(true))
                .addRecipe(finishedRecipeConsumer -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.MINER_CERTIFICATE.get())
                        .define('P', Items.PAPER)
                        .define('1', Items.DIAMOND_PICKAXE).define('2', Items.IRON_AXE)
                        .define('3', Items.GOLDEN_HOE).define('4', Items.STONE_SHOVEL)
                        .pattern(" 1 ")
                        .pattern("2P3")
                        .pattern(" 4 ")
                        .unlockedBy("has_miner_certificate", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINER_CERTIFICATE.get()).build()))
                        .save(finishedRecipeConsumer))
                .build(consumer, UltimineAddition.getLocation("%s_legacy".formatted(Registration.MINER_CERTIFICATE.getId().getPath())));
    }
}
