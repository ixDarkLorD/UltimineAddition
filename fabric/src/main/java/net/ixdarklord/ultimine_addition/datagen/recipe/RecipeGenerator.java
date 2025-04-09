package net.ixdarklord.ultimine_addition.datagen.recipe;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.tag.ModItemTags;
import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.datagen.recipe.builder.ItemStorageDataRecipeBuilder;
import net.ixdarklord.ultimine_addition.datagen.recipe.builder.MCRecipeBuilder;
import net.ixdarklord.ultimine_addition.datagen.recipe.conditions.LegacyModeCondition;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class RecipeGenerator extends FabricRecipeProvider {

    public RecipeGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.CARD_BLUEPRINT.get())
                .define('A', Items.AMETHYST_SHARD)
                .define('P', Items.PAPER)
                .define('L', ConventionalItemTags.LAPIS_GEMS)
                .pattern("ALA")
                .pattern("LPL")
                .pattern("ALA")
                .group(FTBUltimineAddition.MOD_ID)
                .unlockedBy("has_amethyst_shard", inventoryTrigger(ItemPredicate.Builder.item().of(Items.AMETHYST_SHARD).build()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.MINING_SKILL_CARD_EMPTY.get(), 2)
                .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                .requires(Registration.CARD_BLUEPRINT.get())
                .group(FTBUltimineAddition.MOD_ID)
                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                .save(withConditions(output, new LegacyModeCondition(false)));


        MCRecipeBuilder.create(RecipeCategory.MISC, Registration.MINING_SKILL_CARD_PICKAXE.get())
                .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                .requires(ItemTags.PICKAXES)
                .group(FTBUltimineAddition.MOD_ID)
                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                .save(withConditions(output, new LegacyModeCondition(false)));

        MCRecipeBuilder.create(RecipeCategory.MISC, Registration.MINING_SKILL_CARD_AXE.get())
                .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                .requires(ItemTags.AXES)
                .group(FTBUltimineAddition.MOD_ID)
                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                .save(withConditions(output, new LegacyModeCondition(false)));

        MCRecipeBuilder.create(RecipeCategory.MISC, Registration.MINING_SKILL_CARD_SHOVEL.get())
                .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                .requires(ItemTags.SHOVELS)
                .group(FTBUltimineAddition.MOD_ID)
                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                .save(withConditions(output, new LegacyModeCondition(false)));

        MCRecipeBuilder.create(RecipeCategory.MISC, Registration.MINING_SKILL_CARD_HOE.get())
                .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                .requires(ItemTags.HOES)
                .group(FTBUltimineAddition.MOD_ID)
                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                .save(withConditions(output, new LegacyModeCondition(false)));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.SHAPE_SELECTOR.get())
                .define('N', Items.IRON_NUGGET)
                .define('I', Items.IRON_INGOT)
                .define('C', Items.GRAY_CONCRETE)
                .define('M', Registration.CARD_BLUEPRINT.get())
                .pattern("NIN")
                .pattern("CMC")
                .pattern("NCN")
                .group(FTBUltimineAddition.MOD_ID)
                .unlockedBy("has_card_blueprint", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.CARD_BLUEPRINT.get()).build()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.SKILLS_RECORD.get())
                .define('N', Items.IRON_NUGGET)
                .define('I', Items.IRON_INGOT)
                .define('C', Items.YELLOW_CONCRETE)
                .define('M', Registration.MINING_SKILL_CARD_EMPTY.get())
                .pattern("NIN")
                .pattern("CMC")
                .pattern("NCN")
                .group(FTBUltimineAddition.MOD_ID)
                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                .save(withConditions(output, new LegacyModeCondition(false)));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.INK_CHAMBER.get())
                .define('I', Items.IRON_INGOT)
                .define('N', Items.IRON_NUGGET)
                .define('R', Items.RED_DYE)
                .define('G', Items.GREEN_DYE)
                .define('B', Items.BLUE_DYE)
                .pattern("INI")
                .pattern("RGB")
                .pattern("INI")
                .group(FTBUltimineAddition.MOD_ID)
                .unlockedBy("has_skills_record", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.SKILLS_RECORD.get()).build()))
                .save(withConditions(output, new LegacyModeCondition(false)));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.PEN.get())
                .define('G', Items.GOLD_INGOT)
                .define('S', PlatformTags.get().SLIME())
                .define('C', Registration.INK_CHAMBER.get())
                .define('I', Items.IRON_NUGGET)
                .pattern(" GS")
                .pattern("GCG")
                .pattern("IG ")
                .group(FTBUltimineAddition.MOD_ID)
                .unlockedBy("has_ink_chamber", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.INK_CHAMBER.get()).build()))
                .save(withConditions(output, new LegacyModeCondition(false)));

        ItemStorageDataRecipeBuilder.create(RecipeCategory.MISC, Registration.PEN.get())
                .storage("ink_chamber")
                .requires(ModItemTags.MORE_VALUABLE_PIGMENT, 50)
                .requires(ModItemTags.LESS_VALUABLE_PIGMENT, 10)
                .group(FTBUltimineAddition.MOD_ID)
                .unlockedBy("has_pen", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.PEN.get()).build()))
                .save(withConditions(output, new LegacyModeCondition(false)), FTBUltimineAddition.rl("refill"));

        MCRecipeBuilder.create(RecipeCategory.MISC, Registration.MINER_CERTIFICATE.get())
                .requires(Items.PAPER)
                .requires(Registration.MINING_SKILL_CARD_PICKAXE.get(), MiningSkillCardItem.Tier.Mastered)
                .requires(Registration.MINING_SKILL_CARD_AXE.get(), MiningSkillCardItem.Tier.Mastered)
                .requires(Registration.MINING_SKILL_CARD_SHOVEL.get(), MiningSkillCardItem.Tier.Mastered)
                .requires(Registration.MINING_SKILL_CARD_HOE.get(), MiningSkillCardItem.Tier.Mastered)
                .group(FTBUltimineAddition.MOD_ID)
                .unlockedBy("has_skills_record", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.SKILLS_RECORD.get()).build()))
                .save(withConditions(output, new LegacyModeCondition(false)));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.MINER_CERTIFICATE.get())
                .define('P', Items.PAPER)
                .define('1', Items.DIAMOND_PICKAXE).define('2', Items.IRON_AXE)
                .define('3', Items.GOLDEN_HOE).define('4', Items.STONE_SHOVEL)
                .pattern(" 1 ")
                .pattern("2P3")
                .pattern(" 4 ")
                .unlockedBy("has_miner_certificate", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINER_CERTIFICATE.get()).build()))
                .save(withConditions(output, new LegacyModeCondition(true)), FTBUltimineAddition.rl("miner_certificate_legacy"));
    }
}
