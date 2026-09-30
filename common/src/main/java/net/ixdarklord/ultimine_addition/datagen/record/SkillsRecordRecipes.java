package net.ixdarklord.ultimine_addition.datagen.record;

import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.HashMap;
import java.util.Map;

/**
 * The Skills Record's recipes, a clipboard: an iron clip on top, paper around an empty Mining Skill Card, and a board
 * of planks. With plain planks it's the plain (white) record; a dye in the middle of the board makes that color's
 * record. Shared by both loaders' recipe providers. Records can be recolored any number of times with a dye
 * ({@code skills_record_dyeing}).
 * <p>
 * 1.21.1's recipe builders only make plain items, so the recipes (a result with its {@code base_color}) and their
 * unlock advancements are built here the way {@code ShapedRecipeBuilder} builds them.
 */
public final class SkillsRecordRecipes {
    private SkillsRecordRecipes() {}

    /** @param output the output to save to, with the loader's conditions already applied */
    public static void save(RecipeOutput output, Criterion<?> unlock) {
        for (DyeColor dye : DyeColor.values()) {
            boolean plain = dye == DyeColor.WHITE;
            Map<Character, Ingredient> key = new HashMap<>(Map.of(
                    'N', Ingredient.of(Items.IRON_NUGGET),
                    'I', Ingredient.of(Items.IRON_INGOT),
                    'P', Ingredient.of(Items.PAPER),
                    'M', Ingredient.of(Registration.MINING_SKILL_CARD_EMPTY.get()),
                    'W', Ingredient.of(ItemTags.PLANKS)));
            // Any dye of the color, by its common tag (c:dyes/<color>), so other mods' dyes work too.
            if (!plain) key.put('D', Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "dyes/" + dye.getSerializedName()))));
            ItemStack result = new ItemStack(Registration.SKILLS_RECORD.get());
            if (!plain) result.set(DataComponents.BASE_COLOR, dye);
            ResourceLocation id = FTBUltimineAddition.id(plain ? "skills_record" : "skills_record_" + dye.getSerializedName());

            ShapedRecipe recipe = new ShapedRecipe(FTBUltimineAddition.MOD_ID, CraftingBookCategory.MISC,
                    ShapedRecipePattern.of(key, "NIN", "PMP", plain ? "WWW" : "WDW"), result, true);
            Advancement.Builder advancement = output.advancement()
                    .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                    .rewards(AdvancementRewards.Builder.recipe(id))
                    .requirements(AdvancementRequirements.Strategy.OR);
            advancement.addCriterion("has_mining_skill_card", unlock);
            output.accept(id, recipe, advancement.build(id.withPrefix("recipes/misc/")));
        }
    }
}
