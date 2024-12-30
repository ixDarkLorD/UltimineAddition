package net.ixdarklord.ultimine_addition.datagen.recipe.builder;

import net.ixdarklord.ultimine_addition.common.recipe.ItemStorageDataRecipe;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.DataIngredient;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ItemStorageDataRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private String storageName;
    private final NonNullList<DataIngredient> ingredients = NonNullList.create();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    private ItemStorageDataRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
        this.category = category;
        this.result = result.asItem();
        this.count = count;
    }

    public static ItemStorageDataRecipeBuilder create(RecipeCategory category, ItemLike item) {
        return new ItemStorageDataRecipeBuilder(category, item, 1);
    }

    public static ItemStorageDataRecipeBuilder create(RecipeCategory category, ItemLike item, int count) {
        return new ItemStorageDataRecipeBuilder(category, item, count);
    }

    public ItemStorageDataRecipeBuilder storage(String name) {
        this.storageName = name;
        return this;
    }

    @SuppressWarnings("unused")
    public ItemStorageDataRecipeBuilder requires(TagKey<Item> tag, int amount) {
        return this.requires(DataIngredient.of(tag, amount));
    }

    public ItemStorageDataRecipeBuilder requires(ItemLike item, int amount) {
        this.requires(DataIngredient.of(amount, item));
        return this;
    }

    public ItemStorageDataRecipeBuilder requires(DataIngredient ingredient) {
        return this.requires(ingredient, 1);
    }

    public ItemStorageDataRecipeBuilder requires(DataIngredient ingredient, int count) {
        for (int i = 0; i < count; ++i) {
            this.ingredients.add(ingredient);
        }
        return this;
    }

    @Override
    public @NotNull RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    public @NotNull ItemStorageDataRecipeBuilder group(@Nullable String name) {
        this.group = name;
        return this;
    }

    public @NotNull Item getResult() {
        return this.result;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation actualId) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(actualId.getNamespace(), Objects.requireNonNull(Registration.ITEMS.getRegistrar().getId(this.result)).getPath() + "_" + actualId.getPath());
        this.ensureValid(id);

        Advancement.Builder builder = recipeOutput.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(AdvancementRequirements.Strategy.OR);
        Objects.requireNonNull(builder);
        this.criteria.forEach(builder::addCriterion);

        ItemStorageDataRecipe recipe = new ItemStorageDataRecipe(Objects.requireNonNullElse(this.group, ""), RecipeBuilder.determineBookCategory(this.category), new ItemStack(this.result, this.count), this.storageName, this.ingredients);
        recipeOutput.accept(id, recipe, builder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }
}
