package net.ixdarklord.ultimine_addition.datagen.recipe.builder;

import net.ixdarklord.ultimine_addition.common.recipe.ItemStorageDataRecipe;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.DataIngredient;
import net.minecraft.core.registries.BuiltInRegistries;
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
public class ItemStorageDataRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private String storageName;
    private final NonNullList<DataIngredient> ingredients = NonNullList.create();
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
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
    public @NotNull RecipeBuilder unlockedBy(@NotNull String name, @NotNull CriterionTriggerInstance criterion) {
        this.advancement.addCriterion(name, criterion);
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
    public void save(@NotNull Consumer<FinishedRecipe> consumer, @NotNull ResourceLocation actualId) {
        ResourceLocation id = new ResourceLocation(actualId.getNamespace(), Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(this.result)).getPath() + "_" + actualId.getPath());
        this.ensureValid(id);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(RequirementsStrategy.OR);

        ItemStorageDataRecipe recipe = new ItemStorageDataRecipe(id, Objects.requireNonNullElse(this.group, ""), determineBookCategory(this.category), new ItemStack(this.result, this.count), this.storageName, this.ingredients);
        consumer.accept(new CodecRecipeResult<>(id, ItemStorageDataRecipe.Serializer.INSTANCE, ItemStorageDataRecipe.Serializer.CODEC, recipe, this.advancement, id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation id) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }
}
