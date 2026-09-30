package net.ixdarklord.ultimine_addition.datagen.recipe.builder;

import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.recipe.MCRecipe;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.MCIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import java.util.function.Consumer;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

// 1.20.1 recipe builders hand FinishedRecipes to a consumer (CodecRecipeResult writes them with the serializer's codec).
public class MCRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final NonNullList<MCIngredient> ingredients = NonNullList.create();
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
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
    public @NotNull MCRecipeBuilder unlockedBy(@NotNull String name, @NotNull CriterionTriggerInstance criterion) {
        this.advancement.addCriterion(name, criterion);
        return this;
    }

    @Override
    public @NotNull MCRecipeBuilder group(@Nullable String name) {
        this.group = name;
        return this;
    }

    public @NotNull Item getResult() {
        return this.result;
    }

    @Override
    public void save(@NotNull Consumer<FinishedRecipe> consumer, @NotNull ResourceLocation id) {
        this.ensureValid(id);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(RequirementsStrategy.OR);

        MCRecipe recipe = new MCRecipe(id, Objects.requireNonNullElse(this.group, ""), determineBookCategory(this.category), new ItemStack(this.result, this.count), this.ingredients);
        consumer.accept(new CodecRecipeResult<>(id, MCRecipe.Serializer.INSTANCE, MCRecipe.Serializer.CODEC, recipe, this.advancement, id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation id) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }
}
