package net.ixdarklord.ultimine_addition.datagen.recipe;

import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.Map;

/**
 * The Skills Record's recipes, one per color: the board is that color's concrete, and the record comes out in that
 * color (white concrete makes the plain, white, record). Shared by both loaders' recipe providers. Records can be
 * recolored any number of times with a dye ({@code skills_record_dyeing}).
 */
public final class SkillsRecordRecipes {
    private SkillsRecordRecipes() {}

    /** @param output the output to save to, with the loader's conditions already applied */
    public static void save(RecipeOutput output, Criterion<?> unlock) {
        for (DyeColor dye : DyeColor.values()) {
            Item concrete = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(dye.getSerializedName() + "_concrete"));
            Map<Character, Ingredient> key = Map.of(
                    'N', Ingredient.of(Items.IRON_NUGGET),
                    'I', Ingredient.of(Items.IRON_INGOT),
                    'C', Ingredient.of(concrete),
                    'M', Ingredient.of(Registration.MINING_SKILL_CARD_EMPTY.get()));
            boolean plain = dye == DyeColor.WHITE;
            ItemStackTemplate result = plain ? new ItemStackTemplate(Registration.SKILLS_RECORD.get())
                    : new ItemStackTemplate(Registration.SKILLS_RECORD.get(), DataComponentPatch.builder().set(DataComponents.BASE_COLOR, dye).build());
            ResourceKey<Recipe<?>> id = ResourceKey.create(Registries.RECIPE,
                    FTBUltimineAddition.id(plain ? "skills_record" : "skills_record_" + dye.getSerializedName()));

            ShapedRecipe recipe = new ShapedRecipe(RecipeBuilder.createCraftingCommonInfo(true),
                    RecipeBuilder.createCraftingBookInfo(RecipeCategory.MISC, FTBUltimineAddition.MOD_ID),
                    ShapedRecipePattern.of(key, "NIN", "CMC", "NCN"), result);
            RecipeUnlockAdvancementBuilder advancement = new RecipeUnlockAdvancementBuilder();
            advancement.unlockedBy("has_mining_skill_card", unlock);
            output.accept(id, recipe, advancement.build(output, id, RecipeCategory.MISC));
        }
    }
}
