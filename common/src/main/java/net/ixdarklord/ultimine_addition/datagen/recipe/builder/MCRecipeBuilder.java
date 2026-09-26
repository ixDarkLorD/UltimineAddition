package net.ixdarklord.ultimine_addition.datagen.recipe.builder;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.resources.ResourceKey;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.recipe.MCRecipe;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.MCIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class MCRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final NonNullList<MCIngredient> ingredients = NonNullList.create();
    private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();
    @Nullable
    private String group;

    private MCRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
        this.category = category;
        this.result = result.asItem();
        this.count = count;
    }

    public static MCRecipeBuilder create(RecipeCategory category, ItemLike item) {
        return new MCRecipeBuilder(category, item, 1);
    }

    public static MCRecipeBuilder create(RecipeCategory category, ItemLike item, int count) {
        return new MCRecipeBuilder(category, item, count);
    }

    public MCRecipeBuilder requires(ItemLike item) {
        return this.requires(item, null);
    }

    public MCRecipeBuilder requires(TagKey<Item> tagKey) {
        return this.requires(MCIngredient.of(null, tagKey));
    }

    public MCRecipeBuilder requires(ItemLike item, MiningSkillCardItem.Tier tier) {
        return this.requires(MCIngredient.of(tier, item));
    }

    private MCRecipeBuilder requires(MCIngredient ingredient) {
        for (int i = 0; i < 1; ++i) {
            this.ingredients.add(ingredient);
        }
        return this;
    }

    @Override
    public @NotNull MCRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.advancementBuilder.unlockedBy(name, criterion);
        return this;
    }

    @Override
    public @NotNull MCRecipeBuilder group(@Nullable String name) {
        this.group = name;
        return this;
    }

    @Override
    public @NotNull ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(this.result));
    }

    public @NotNull Item getResult() {
        return this.result;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> id) {
        MCRecipe recipe = new MCRecipe(Objects.requireNonNullElse(this.group, ""), RecipeBuilder.determineCraftingBookCategory(this.category), new ItemStackTemplate(this.result, this.count), this.ingredients);
        recipeOutput.accept(id, recipe, this.advancementBuilder.build(recipeOutput, id, this.category));
    }

}
