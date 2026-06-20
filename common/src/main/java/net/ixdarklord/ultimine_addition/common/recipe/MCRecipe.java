package net.ixdarklord.ultimine_addition.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.MCIngredient;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MCRecipe extends ShapelessRecipe {
   private final ResourceLocation id;
   final String group;
   final CraftingBookCategory category;
   final ItemStack result;
   final NonNullList<MCIngredient> ingredients;

   public MCRecipe(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result, NonNullList<MCIngredient> ingredients) {
      super(id, group, category, result, MCIngredient.toNormal(ingredients));
      this.id = id;
      this.group = group;
      this.category = category;
      this.result = result;
      this.ingredients = ingredients;
   }

   public boolean isSpecial() {
      return false;
   }

   public boolean matches(@NotNull CraftingContainer container, @NotNull Level level) {
      StackedContents stackedContents = new StackedContents();
      int matchedValue = 0;

      for (int i = 0; i < container.getContainerSize(); ++i) {
         ItemStack itemStack = container.getItem(i);
         if (!itemStack.isEmpty() && !itemStack.isDamaged() && !this.ingredients.stream().filter((ingredient) -> ingredient.test(itemStack)).toList().isEmpty()) {
            ++matchedValue;
            stackedContents.accountStack(itemStack, 1);
         }
      }

      return matchedValue == this.ingredients.size() && stackedContents.canCraft(this, null);
   }

   public @NotNull ItemStack assemble(@NotNull CraftingContainer container, @NotNull RegistryAccess registryAccess) {
      ItemStack stack = this.getResultItem(registryAccess).copy();
      Item item2 = stack.getItem();
      if (item2 instanceof MiningSkillCardItem item) {
         NonNullList<ItemStack> inputs = NonNullList.create();

         for (int i = 0; i < container.getContainerSize(); ++i) {
            if (!container.getItem(i).isEmpty() && !(container.getItem(i).getItem() instanceof MiningSkillCardItem)) {
               inputs.add(container.getItem(i));
            }
         }

         if (!inputs.isEmpty()) {
            item.getData(stack).setDisplayItem(inputs.get(0)).save();
         }
      }

      return stack;
   }

   public boolean canCraftInDimensions(int width, int height) {
      return width * height >= this.ingredients.size();
   }

   public CraftingBookCategory getCategory() {
      return this.category;
   }

   public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
      return this.result;
   }

   public @NotNull ResourceLocation getId() {
      return this.id;
   }

   public @NotNull String getGroup() {
      return this.group;
   }

   public @NotNull NonNullList<MCIngredient> getMCIngredients() {
      return this.ingredients;
   }

   public @NotNull RecipeSerializer<?> getSerializer() {
      return Registration.MC_RECIPE_SERIALIZER.get();
   }

   public static class Serializer implements RecipeSerializer<MCRecipe> {
      public static final ResourceLocation NAME = FTBUltimineAddition.id("mc_recipe");

      public @NotNull MCRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
         String group = GsonHelper.getAsString(json, "group", "");
         CraftingBookCategory craftingBookCategory = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(json, "category", null), CraftingBookCategory.MISC);
         NonNullList<MCIngredient> nonnulllist = itemsFromJson(GsonHelper.getAsJsonArray(json, "ingredients"));
         if (nonnulllist.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
         } else {
            ItemStack stack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new MCRecipe(recipeId, group, craftingBookCategory, stack, nonnulllist);
         }
      }

      private static NonNullList<MCIngredient> itemsFromJson(JsonArray pIngredientArray) {
         NonNullList<MCIngredient> nonnulllist = NonNullList.create();

         for (int i = 0; i < pIngredientArray.size(); ++i) {
            MCIngredient ingredient = MCIngredient.fromJson(pIngredientArray.get(i));
            nonnulllist.add(ingredient);
         }

         return nonnulllist;
      }

      public @NotNull MCRecipe fromNetwork(@NotNull ResourceLocation pRecipeId, FriendlyByteBuf buffer) {
         String group = buffer.readUtf();
         CraftingBookCategory craftingBookCategory = buffer.readEnum(CraftingBookCategory.class);
         int i = buffer.readVarInt();
         NonNullList<MCIngredient> nonnulllist = NonNullList.withSize(i, MCIngredient.EMPTY);
         nonnulllist.replaceAll((ignored) -> MCIngredient.fromNetwork(buffer));
         ItemStack stack = buffer.readItem();
         return new MCRecipe(pRecipeId, group, craftingBookCategory, stack, nonnulllist);
      }

      public void toNetwork(FriendlyByteBuf buffer, MCRecipe recipe) {
         buffer.writeUtf(recipe.getGroup());
         buffer.writeEnum(recipe.category);
         buffer.writeVarInt(recipe.getIngredients().size());

         for (MCIngredient ingredient : recipe.getMCIngredients()) {
            ingredient.toNetwork(buffer);
         }

         buffer.writeItem(recipe.result);
      }
   }
}
