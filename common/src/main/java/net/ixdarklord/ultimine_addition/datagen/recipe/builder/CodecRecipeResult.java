package net.ixdarklord.ultimine_addition.datagen.recipe.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// 1.20.1 recipe providers take FinishedRecipes that write their own JSON: this one writes a recipe with its
// serializer's codec (as newer versions' RecipeOutput does), so the file matches what the serializer reads.
record CodecRecipeResult<T>(ResourceLocation id, RecipeSerializer<?> serializer, MapCodec<T> codec, T recipe,
                            Advancement.Builder advancement, ResourceLocation advancementId) implements FinishedRecipe {
    @Override
    public void serializeRecipeData(@NotNull JsonObject json) {
        JsonElement encoded = this.codec.codec().encodeStart(JsonOps.INSTANCE, this.recipe).getOrThrow(false, error -> {});
        encoded.getAsJsonObject().entrySet().forEach(entry -> json.add(entry.getKey(), entry.getValue()));
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getType() {
        return this.serializer;
    }

    @Override
    public @Nullable JsonObject serializeAdvancement() {
        return this.advancement.serializeToJson();
    }

    @Override
    public @Nullable ResourceLocation getAdvancementId() {
        return this.advancementId;
    }
}
