package net.ixdarklord.ultimine_addition.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.ixdarklord.ultimine_addition.common.data.item.StorageItemData;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.DataIngredient;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemStorageDataRecipe extends CustomRecipe {
   private final ResourceLocation id;
   final String group;
   final CraftingBookCategory category;
   final ItemStack result;
   final String storageName;
   final NonNullList<DataIngredient> ingredients;

   public ItemStorageDataRecipe(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result, String storageName, NonNullList<DataIngredient> ingredients) {
      super(id, category);
      this.id = id;
      this.group = group;
      this.category = category;
      this.result = result;
      this.storageName = storageName;
      this.ingredients = ingredients;
   }

   public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer container) {
      NonNullList<ItemStack> items = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
      List<ItemUtils.ItemSorter> sorterList = new ArrayList<>();
      int sameItems = 0;

      for (int i = 0; i < items.size(); ++i) {
         if (this.result.is(container.getItem(i).getItem())) {
            StorageItemData data = StorageItemData.load(this.storageName, container.getItem(i));
            sorterList.add(new ItemUtils.ItemSorter(container.getItem(i), i, data.getCapacity()));
            ++sameItems;
         }
      }

      if (sameItems > 1) {
         sorterList.sort((o1, o2) -> Integer.compare(o2.order(), o1.order()));

         for (int i = 1; i < sorterList.size(); ++i) {
            ItemStack stack = sorterList.get(i).item().copy();
            StorageItemData.load(this.storageName, stack).setCapacity(0).save();
            items.set(sorterList.get(i).slotId(), stack);
         }

      }
      return items;
   }

   public boolean matches(@NotNull CraftingContainer container, @NotNull Level level) {
      NonNullList<ItemStack> items = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
      List<ItemUtils.ItemSorter> sorterList = new ArrayList<>();

      for (int i = 0; i < container.getContainerSize(); ++i) {
         if (!container.getItem(i).isEmpty()) {
            items.set(i, container.getItem(i));
         }
      }

      int capacity = 0;
      int matchedValue = 0;

      for (ItemStack input : items) {
         if (input.is(this.result.getItem())) {
            StorageItemData data = StorageItemData.load(this.storageName, input);
            sorterList.add(new ItemUtils.ItemSorter(input, 0, data.getCapacity()));
            capacity += data.getCapacity();
            ++matchedValue;
         }

         for (DataIngredient ingredient : this.ingredients) {
            if (ingredient.test(input)) {
               capacity += ingredient.getAmount();
               ++matchedValue;
            }
         }
      }

      if (sorterList.isEmpty()) {
         return false;
      } else {
         sorterList.sort((o1, o2) -> Integer.compare(o2.order(), o1.order()));
         if (sorterList.size() > 1) {
            for (int i = 1; i < sorterList.size(); ++i) {
               StorageItemData data = StorageItemData.load(this.storageName, sorterList.get(i).item());
               if (data.getCapacity() == 0) {
                  return false;
               }
            }
         }

         int size = items.stream().filter((stack) -> !stack.isEmpty()).toList().size();
         StorageItemData data = StorageItemData.load(this.storageName, sorterList.get(0).item());
         boolean isValid = matchedValue >= 2 && size == matchedValue;
         boolean isOverflow = capacity > data.getMaxCapacity();
         return isValid && !isOverflow;
      }
   }

   public @NotNull ItemStack assemble(CraftingContainer container, @NotNull RegistryAccess registryAccess) {
      int amount = 0;
      ItemStack itemStack = null;

      for (int i = 0; i < container.getContainerSize(); ++i) {
         ItemStack stack = container.getItem(i);
         if (!stack.isEmpty()) {
            if (stack.is(this.result.getItem())) {
               amount += StorageItemData.load(this.storageName, stack).getCapacity();
               itemStack = stack;
            }

            for (DataIngredient ingredient : this.ingredients) {
               if (ingredient.test(stack)) {
                  amount += ingredient.getAmount();
               }
            }
         }
      }

      ItemStack itemStack1 = this.result.copy();
      StorageItemData.load(this.storageName, itemStack == null ? itemStack1 : itemStack).setCapacity(amount).save();
      return itemStack1;
   }

   public boolean canCraftInDimensions(int width, int height) {
      return width * height >= this.ingredients.size();
   }

   public CraftingBookCategory getCategory() {
      return this.category;
   }

   public ItemStack getResultItem() {
      return this.result;
   }

   public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
      return this.result;
   }

   public @NotNull ResourceLocation getId() {
      return this.id;
   }

   public @NotNull NonNullList<DataIngredient> getDataIngredients() {
      return this.ingredients;
   }

   public String getStorageName() {
      return this.storageName;
   }

   public @NotNull RecipeSerializer<?> getSerializer() {
      return Registration.ITEM_DATA_STORAGE_RECIPE_SERIALIZER.get();
   }

   public static class Serializer implements RecipeSerializer<ItemStorageDataRecipe> {
      public static final ResourceLocation NAME = FTBUltimineAddition.id("item_storage_data");

      public @NotNull ItemStorageDataRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
         String group = GsonHelper.getAsString(json, "group", "");
         CraftingBookCategory craftingBookCategory = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(json, "category", null), CraftingBookCategory.MISC);
         NonNullList<DataIngredient> nonnulllist = itemsFromJson(GsonHelper.getAsJsonArray(json, "ingredients"));
         if (nonnulllist.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
         } else {
            ItemStack stack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            String storageName = GsonHelper.getAsString(GsonHelper.getAsJsonObject(json, "result"), "storage_name", "storage");
            return new ItemStorageDataRecipe(id, group, craftingBookCategory, stack, storageName, nonnulllist);
         }
      }

      private static NonNullList<DataIngredient> itemsFromJson(JsonArray pIngredientArray) {
         NonNullList<DataIngredient> nonnulllist = NonNullList.create();

         for (int i = 0; i < pIngredientArray.size(); ++i) {
            DataIngredient ingredient = DataIngredient.fromJson(pIngredientArray.get(i));
            nonnulllist.add(ingredient);
         }

         return nonnulllist;
      }

      public @NotNull ItemStorageDataRecipe fromNetwork(@NotNull ResourceLocation id, FriendlyByteBuf buffer) {
         String group = buffer.readUtf();
         CraftingBookCategory craftingBookCategory = buffer.readEnum(CraftingBookCategory.class);
         int i = buffer.readVarInt();
         NonNullList<DataIngredient> nonnulllist = NonNullList.withSize(i, DataIngredient.EMPTY);
         nonnulllist.replaceAll((ignored) -> DataIngredient.fromNetwork(buffer));
         ItemStack itemstack = buffer.readItem();
         String storageName = buffer.readUtf();
         return new ItemStorageDataRecipe(id, group, craftingBookCategory, itemstack, storageName, nonnulllist);
      }

      public void toNetwork(FriendlyByteBuf buffer, ItemStorageDataRecipe recipe) {
         buffer.writeUtf(recipe.getGroup());
         buffer.writeEnum(recipe.category);
         buffer.writeVarInt(recipe.getDataIngredients().size());

         for (DataIngredient ingredient : recipe.ingredients) {
            ingredient.toNetwork(buffer);
         }

         buffer.writeItem(recipe.result);
         buffer.writeUtf(recipe.getStorageName());
      }
   }
}
