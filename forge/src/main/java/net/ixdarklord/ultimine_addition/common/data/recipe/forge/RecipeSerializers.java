package net.ixdarklord.ultimine_addition.common.data.recipe.forge;

import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.common.data.recipe.ItemStorageDataRecipe;
import net.ixdarklord.ultimine_addition.common.data.recipe.MCRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecipeSerializers {
    public static class ItemStorageDataRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ItemStorageDataRecipe> {
        private final ItemStorageDataRecipe.Serializer INSTANCE = new ItemStorageDataRecipe.Serializer();
        @Override
        public @NotNull ItemStorageDataRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject serializedRecipe) {
            return INSTANCE.fromJson(recipeId, serializedRecipe);
        }
        @Nullable
        @Override
        public ItemStorageDataRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            return INSTANCE.fromNetwork(recipeId, buffer);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull ItemStorageDataRecipe recipe) {
            INSTANCE.toNetwork(buffer, recipe);
        }
    }
    public static class MCRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<MCRecipe> {
        private final MCRecipe.Serializer INSTANCE = new MCRecipe.Serializer();
        @Override
        public @NotNull MCRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject serializedRecipe) {
            return INSTANCE.fromJson(recipeId, serializedRecipe);
        }
        @Nullable
        @Override
        public MCRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            return INSTANCE.fromNetwork(recipeId, buffer);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull MCRecipe recipe) {
            INSTANCE.toNetwork(buffer, recipe);
        }
    }
}
