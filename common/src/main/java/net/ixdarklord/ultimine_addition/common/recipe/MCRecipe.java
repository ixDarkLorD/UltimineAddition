package net.ixdarklord.ultimine_addition.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.MCIngredient;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MCRecipe extends ShapelessRecipe {
    private final ResourceLocation id;
    final String group;
    final ItemStack result;
    final NonNullList<MCIngredient> ingredients;

    public MCRecipe(ResourceLocation id, String group, ItemStack result, NonNullList<MCIngredient> ingredients) {
        super(id, group, result, MCIngredient.toNormal(ingredients));
        this.id = id;
        this.group = group;
        this.result = result;
        this.ingredients = ingredients;
    }

    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public boolean matches(@NotNull CraftingContainer container, @NotNull Level level) {
        StackedContents stackedContents = new StackedContents();
        int matchedValue = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack itemStack = container.getItem(i);
            if (!itemStack.isEmpty() && !itemStack.isDamaged() && !this.ingredients.stream()
                    .filter(ingredient -> ingredient.test(itemStack))
                    .toList()
                    .isEmpty()) {
                matchedValue++;
                stackedContents.accountStack(itemStack, 1);
            }
        }

        return matchedValue == this.ingredients.size() && stackedContents.canCraft(this, null);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer container) {
        ItemStack stack = getResultItem().copy();
        if (stack.getItem() instanceof MiningSkillCardItem item) {
            NonNullList<ItemStack> inputs = NonNullList.create();
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (!container.getItem(i).isEmpty() && !(container.getItem(i).getItem() instanceof MiningSkillCardItem))
                    inputs.add(container.getItem(i));
            }
            item.getData(stack).setDisplayTool(inputs.get(0)).saveData(stack);
        }
        return stack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.ingredients.size();
    }

    @Override
    public @NotNull ItemStack getResultItem() {
        return this.result;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.id;
    }

    @Override
    public @NotNull String getGroup() {
        return group;
    }

    public @NotNull NonNullList<MCIngredient> getMCIngredients() {
        return ingredients;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Registration.MC_RECIPE_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<MCRecipe> {
        public static final ResourceLocation NAME = UltimineAddition.getLocation("mc_recipe");
        public @NotNull MCRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject pJson) {
            String group = GsonHelper.getAsString(pJson, "group", "");
            NonNullList<MCIngredient> nonnulllist = itemsFromJson(GsonHelper.getAsJsonArray(pJson, "ingredients"));
            if (nonnulllist.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            } else {
                ItemStack stack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
                return new MCRecipe(recipeId, group, stack, nonnulllist);
            }
        }

        private static NonNullList<MCIngredient> itemsFromJson(JsonArray pIngredientArray) {
            NonNullList<MCIngredient> nonnulllist = NonNullList.create();
            for(int i = 0; i < pIngredientArray.size(); ++i) {
                MCIngredient ingredient = MCIngredient.fromJson(pIngredientArray.get(i));
                nonnulllist.add(ingredient);
            }
            return nonnulllist;
        }

        public @NotNull MCRecipe fromNetwork(@NotNull ResourceLocation pRecipeId, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            int i = buffer.readVarInt();
            NonNullList<MCIngredient> nonnulllist = NonNullList.withSize(i, MCIngredient.EMPTY);
            nonnulllist.replaceAll(ignored -> MCIngredient.fromNetwork(buffer));
            ItemStack stack = buffer.readItem();
            return new MCRecipe(pRecipeId, group, stack, nonnulllist);
        }

        public void toNetwork(FriendlyByteBuf buffer, MCRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeVarInt(recipe.getIngredients().size());
            for(MCIngredient ingredient : recipe.getMCIngredients()) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(recipe.getResultItem());
        }
    }
}
