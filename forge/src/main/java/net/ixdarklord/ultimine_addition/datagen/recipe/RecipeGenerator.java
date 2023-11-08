package net.ixdarklord.ultimine_addition.datagen.recipe;

import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.tag.ModItemTags;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.datagen.recipe.builder.ItemStorageDataRecipeBuilder;
import net.ixdarklord.ultimine_addition.datagen.recipe.builder.MCRecipeBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class RecipeGenerator extends RecipeProvider implements IConditionBuilder {
    public RecipeGenerator(DataGenerator pGenerator) {
        super(pGenerator);
    }

    @Override
    protected void buildCraftingRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(Registration.CARD_BLUEPRINT.get())
                .define('A', Items.AMETHYST_SHARD)
                .define('P', Items.PAPER)
                .define('L', Tags.Items.GEMS_LAPIS)
                .pattern("ALA")
                .pattern("LPL")
                .pattern("ALA")
                .group(Constants.MOD_ID)
                .unlockedBy("has_amethyst_shard", inventoryTrigger(ItemPredicate.Builder.item().of(Items.AMETHYST_SHARD).build()))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(Registration.MINING_SKILL_CARD_EMPTY.get(), 2)
                .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                .requires(Registration.CARD_BLUEPRINT.get())
                .group(Constants.MOD_ID)
                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                .save(consumer);

        MCRecipeBuilder.create(Registration.MINING_SKILL_CARD_PICKAXE.get())
                .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                .requires(ModItemTags.PICKAXES)
                .group(Constants.MOD_ID)
                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                .save(consumer);

        MCRecipeBuilder.create(Registration.MINING_SKILL_CARD_AXE.get())
                .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                .requires(ModItemTags.AXES)
                .group(Constants.MOD_ID)
                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                .save(consumer);

        MCRecipeBuilder.create(Registration.MINING_SKILL_CARD_SHOVEL.get())
                .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                .requires(ModItemTags.SHOVELS)
                .group(Constants.MOD_ID)
                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                .save(consumer);

        MCRecipeBuilder.create(Registration.MINING_SKILL_CARD_HOE.get())
                .requires(Registration.MINING_SKILL_CARD_EMPTY.get())
                .requires(ModItemTags.HOES)
                .group(Constants.MOD_ID)
                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(Registration.SKILLS_RECORD.get())
                .define('S', ItemTags.WOODEN_SLABS)
                .define('I', Items.IRON_INGOT)
                .define('Y', Items.YELLOW_CONCRETE)
                .define('C', Registration.MINING_SKILL_CARD_EMPTY.get())
                .pattern("SIS")
                .pattern("YCY")
                .pattern("SYS")
                .group(Constants.MOD_ID)
                .unlockedBy("has_mining_skill_card", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.MINING_SKILL_CARD_EMPTY.get()).build()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(Registration.INK_CHAMBER.get())
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
                .save(consumer);

        ShapedRecipeBuilder.shaped(Registration.PEN.get())
                .define('G', Items.GOLD_INGOT)
                .define('S', Tags.Items.SLIMEBALLS)
                .define('C', Registration.INK_CHAMBER.get())
                .define('I', Items.IRON_NUGGET)
                .pattern(" GS")
                .pattern("GCG")
                .pattern("IG ")
                .group(Constants.MOD_ID)
                .unlockedBy("has_ink_chamber", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.INK_CHAMBER.get()).build()))
                .save(consumer);

        ItemStorageDataRecipeBuilder.create(Registration.PEN.get())
                .storage("ink_chamber", 1)
                .requires(ModItemTags.MORE_VALUABLE_PIGMENT, 50)
                .requires(ModItemTags.LESS_VALUABLE_PIGMENT, 10)
                .group(Constants.MOD_ID)
                .unlockedBy("has_pen", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.PEN.get()).build()))
                .save(consumer, Constants.getLocation("refill"));

        MCRecipeBuilder.create(Registration.MINER_CERTIFICATE.get())
                .requires(Items.PAPER)
                .requires(Registration.MINING_SKILL_CARD_PICKAXE.get(), MiningSkillCardItem.Tier.Mastered)
                .requires(Registration.MINING_SKILL_CARD_AXE.get(), MiningSkillCardItem.Tier.Mastered)
                .requires(Registration.MINING_SKILL_CARD_SHOVEL.get(), MiningSkillCardItem.Tier.Mastered)
                .requires(Registration.MINING_SKILL_CARD_HOE.get(), MiningSkillCardItem.Tier.Mastered)
                .group(Constants.MOD_ID)
                .unlockedBy("has_skills_record", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.SKILLS_RECORD.get()).build()))
                .save(consumer);
    }

    @Override
    public @NotNull String getName() {
        return String.format("%s %s", Constants.MOD_NAME, super.getName());
    }
}
