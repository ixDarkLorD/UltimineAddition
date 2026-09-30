package net.ixdarklord.ultimine_addition.datagen.recipe.builder;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.resources.ResourceKey;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.ixdarklord.ultimine_addition.common.recipe.ItemStorageDataRecipe;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.DataIngredient;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ItemStorageDataRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private String storageName;
    private final NonNullList<DataIngredient> ingredients = NonNullList.create();
    private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();
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
        this.advancementBuilder.unlockedBy(name, criterion);
        return this;
    }

    public @NotNull ItemStorageDataRecipeBuilder group(@Nullable String name) {
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
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> actualId) {
        Identifier location = actualId.identifier();
        ResourceKey<Recipe<?>> id = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(location.getNamespace(),
                Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(this.result)).getPath() + "_" + location.getPath()));

        ItemStorageDataRecipe recipe = new ItemStorageDataRecipe(Objects.requireNonNullElse(this.group, ""), RecipeBuilder.determineCraftingBookCategory(this.category), new ItemStackTemplate(this.result, this.count), this.storageName, this.ingredients);
        recipeOutput.accept(id, recipe, this.advancementBuilder.build(recipeOutput, id, this.category));
    }

}
