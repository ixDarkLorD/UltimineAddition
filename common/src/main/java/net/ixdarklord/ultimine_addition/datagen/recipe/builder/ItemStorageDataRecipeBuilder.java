package net.ixdarklord.ultimine_addition.datagen.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.DataIngredient;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ItemStorageDataRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private String storageName;
    private final List<DataIngredient> ingredients = new ArrayList<>();
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

    public ItemStorageDataRecipeBuilder storage(String name, int size) {
        this.storageName = name;
        return this;
    }

    @SuppressWarnings("unused")
    public ItemStorageDataRecipeBuilder requires(TagKey<Item> tag, int amount) {
        return this.requires(DataIngredient.of(tag, amount));
    }

    public ItemStorageDataRecipeBuilder requires(ItemLike item) {
        return this.requires(item, 1);
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

    public @NotNull ItemStorageDataRecipeBuilder unlockedBy(@NotNull String key, @NotNull CriterionTriggerInstance criterionTriggerInstance) {
        this.advancement.addCriterion(key, criterionTriggerInstance);
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
    public void save(Consumer<FinishedRecipe> consumer, @NotNull ResourceLocation actionLocation) {
        ResourceLocation recipeId = new ResourceLocation(actionLocation.getNamespace(), Objects.requireNonNull(Registration.ITEMS.getRegistrar().getId(this.result)).getPath() + "_" + actionLocation.getPath());
        this.ensureValid(recipeId);
        this.advancement.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).requirements(RequirementsStrategy.OR);
        consumer.accept(new Result(recipeId, this.result, this.count, this.storageName, this.group == null ? "" : this.group, determineBookCategory(this.category), this.ingredients, this.advancement, recipeId.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation p_126208_) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + p_126208_);
        }
    }

    public static class Result extends CraftingRecipeBuilder.CraftingResult {
        private final ResourceLocation id;
        private final Item result;
        private final int count;
        private final String storageName;
        private final String group;
        private final List<DataIngredient> ingredients;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation id, Item result, int count, String storageName, String group, CraftingBookCategory category, List<DataIngredient> ingredients, Advancement.Builder advancement, ResourceLocation advancementId) {
            super(category);
            this.id = id;
            this.result = result;
            this.count = count;
            this.storageName = storageName;
            this.group = group;
            this.ingredients = ingredients;
            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        public void serializeRecipeData(@NotNull JsonObject json) {
            super.serializeRecipeData(json);
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }

            JsonArray jsonarray = new JsonArray();
            for(DataIngredient ingredient : this.ingredients) {
                jsonarray.add(ingredient.toJson());
            }
            json.add("ingredients", jsonarray);

            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("item", Objects.requireNonNull(Registration.ITEMS.getRegistrar().getId(this.result)).toString());
            if (this.count > 1)
                jsonobject.addProperty("count", this.count);
            if (this.storageName != null)
                jsonobject.addProperty("storage_name", this.storageName);
            json.add("result", jsonobject);
        }

        public @NotNull RecipeSerializer<?> getType() {
            return Registration.ITEM_DATA_STORAGE_RECIPE_SERIALIZER.get();
        }

        public @NotNull ResourceLocation getId() {
            return this.id;
        }

        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}
