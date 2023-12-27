package net.ixdarklord.ultimine_addition.datagen.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.MCIngredient;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
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

public class MCRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final List<MCIngredient> ingredients = new ArrayList<>();
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

    public @NotNull MCRecipeBuilder unlockedBy(@NotNull String key, @NotNull CriterionTriggerInstance criterionTriggerInstance) {
        this.advancement.addCriterion(key, criterionTriggerInstance);
        return this;
    }

    public @NotNull MCRecipeBuilder group(@Nullable String name) {
        this.group = name;
        return this;
    }

    public @NotNull Item getResult() {
        return this.result;
    }

    @Override
    public void save(@NotNull Consumer<FinishedRecipe> consumer) {
        ResourceLocation recipeId = Registration.ITEMS.getRegistrar().getId(this.getResult());
        assert recipeId != null;
        this.save(consumer, recipeId);
    }

    @Override
    public void save(Consumer<FinishedRecipe> consumer, @NotNull ResourceLocation recipeId) {
        this.ensureValid(recipeId);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).requirements(RequirementsStrategy.OR);
        consumer.accept(new Result(recipeId, this.result, this.count, this.group == null ? "" : this.group, determineBookCategory(this.category), this.ingredients, this.advancement, recipeId.withPrefix("recipes/" + this.category.getFolderName() + "/")));
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
        private final String group;
        private final List<MCIngredient> ingredients;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation id, Item result, int count, String group, CraftingBookCategory category, List<MCIngredient> ingredients, Advancement.Builder advancement, ResourceLocation advancementId) {
            super(category);
            this.id = id;
            this.result = result;
            this.count = count;
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
            for(MCIngredient ingredient : this.ingredients) {
                jsonarray.add(ingredient.toJson());
            }
            json.add("ingredients", jsonarray);

            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("item", Objects.requireNonNull(Registration.ITEMS.getRegistrar().getId(this.result)).toString());
            if (this.count > 1)
                jsonobject.addProperty("count", this.count);
            json.add("result", jsonobject);
        }

        public @NotNull RecipeSerializer<?> getType() {
            return Registration.MC_RECIPE_SERIALIZER.get();
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
