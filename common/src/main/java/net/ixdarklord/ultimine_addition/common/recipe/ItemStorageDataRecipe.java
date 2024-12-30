package net.ixdarklord.ultimine_addition.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.data.item.ItemStorageData;
import net.ixdarklord.ultimine_addition.common.item.StorageDataAbstractItem;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.DataIngredient;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemStorageDataRecipe extends CustomRecipe {
    final String group;
    final CraftingBookCategory category;
    final ItemStack result;
    final String storageName;
    final NonNullList<DataIngredient> ingredients;

    public ItemStorageDataRecipe(String group, CraftingBookCategory category, ItemStack result, String storageName, NonNullList<DataIngredient> ingredients) {
        super(category);
        this.group = group;
        this.category = category;
        this.result = result;
        this.storageName = storageName;
        this.ingredients = ingredients;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> items = NonNullList.withSize(input.items().size(), ItemStack.EMPTY);
        List<ItemUtils.ItemSorter> sorterList = new ArrayList<>();
        int sameItems = 0;

        for (int i = 0; i < items.size(); ++i) {
            if (input.getItem(i).getItem() instanceof StorageDataAbstractItem item) {
                var data = ItemStorageData.loadData(this.storageName, item.getMaxCapacity(), input.getItem(i));
                sorterList.add(new ItemUtils.ItemSorter(input.getItem(i), i, data.getCapacity()));
                sameItems++;
            }
        }
        if (sameItems <= 1) return items;
        sorterList.sort((o1, o2) -> Integer.compare(o2.order(), o1.order()));

        for (int i = 1; i < sorterList.size(); i++) {
            ItemStack stack = sorterList.get(i).item().copy();
            var item = (StorageDataAbstractItem) stack.getItem();
            ItemStorageData.loadData(this.storageName, item.getMaxCapacity(), stack).setCapacity(0).saveData(stack);
            items.set(sorterList.get(i).slotId(), stack);
        }
        return items;
    }

    @Override
    public boolean matches(CraftingInput input, @NotNull Level level) {
        int capacity = 0;
        int matchedValue = 0;
        List<ItemUtils.ItemSorter> sorterList = new ArrayList<>();
        for (ItemStack itemStack : input.items()) {
            ItemStack stack = itemStack.copy();
            if (stack.getItem() instanceof StorageDataAbstractItem item) {
                var data = ItemStorageData.loadData(this.storageName, item.getMaxCapacity(), stack);
                sorterList.add(new ItemUtils.ItemSorter(stack, 0, data.getCapacity()));
                capacity += data.getCapacity();
                matchedValue++;
            } else for (DataIngredient ingredient : this.ingredients) {
                if (ingredient.test(stack)) {
                    capacity += ingredient.getAmount();
                    matchedValue++;
                }
            }
        }

        if (sorterList.isEmpty()) return false;
        sorterList.sort((o1, o2) -> Integer.compare(o2.order(), o1.order()));
        if (sorterList.size() > 1)
            for (int i = 1; i < sorterList.size(); i++) {
                var data = ItemStorageData.loadData(this.storageName, 0, sorterList.get(i).item());
                if (data.getCapacity() == 0) return false;
            }

        int size = input.items().stream().filter(stack -> !stack.isEmpty()).toList().size();
        var data = ItemStorageData.loadData(this.storageName, 0, sorterList.getFirst().item());
        boolean isValid = matchedValue >= 2 && size == matchedValue;
        boolean isOverflow = capacity > data.getMaxCapacity();
        return isValid && !isOverflow;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        int amount = 0;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;

            if (stack.is(this.result.getItem())) {
                amount += ItemStorageData.loadData(this.storageName, 0, stack).getCapacity();
            } else for (DataIngredient ingredient : this.ingredients) {
                if (ingredient.test(stack)) amount += ingredient.getAmount();
            }
        }
        ItemStack stack = this.result.copy();
        ItemStorageData.loadData(this.storageName, ((StorageDataAbstractItem) stack.getItem()).getMaxCapacity(), stack).setCapacity(amount).saveData(stack);
        return stack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.ingredients.size();
    }

    public CraftingBookCategory getCategory() {
        return category;
    }

    public ItemStack getResultItem() {
        return result;
    }

    public @NotNull NonNullList<DataIngredient> getDataIngredients() {
        return ingredients;
    }

    public String getStorageName() {
        return this.storageName;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Registration.ITEM_DATA_STORAGE_RECIPE_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<ItemStorageDataRecipe> {
        public static final MapCodec<ItemStorageDataRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ItemStorageDataRecipe::getGroup),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ItemStorageDataRecipe::category),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(dataRecipe -> dataRecipe.result),
                Codec.STRING.fieldOf("storage_name").forGetter(ItemStorageDataRecipe::getStorageName),
                DataIngredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(list -> {
                    DataIngredient[] ingredients = list.stream().filter(ingredient -> !ingredient.isEmpty()).toArray(DataIngredient[]::new);
                    if (ingredients.length == 0) {
                        return DataResult.error(() -> "No ingredients for ItemStorageDataRecipe");
                    } else {
                        return ingredients.length > 9
                                ? DataResult.error(() -> "Too many ingredients for ItemStorageDataRecipe")
                                : DataResult.success(NonNullList.of(DataIngredient.EMPTY, ingredients));
                    }
                }, DataResult::success).forGetter(ItemStorageDataRecipe::getDataIngredients)
        ).apply(instance, ItemStorageDataRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemStorageDataRecipe> STREAM_CODEC = StreamCodec.of(ItemStorageDataRecipe.Serializer::toNetwork, ItemStorageDataRecipe.Serializer::fromNetwork);


        @Override
        public @NotNull MapCodec<ItemStorageDataRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ItemStorageDataRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static @NotNull ItemStorageDataRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            String group = buf.readUtf();
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);

            int i = buf.readVarInt();
            NonNullList<DataIngredient> nonnulllist = NonNullList.withSize(i, DataIngredient.EMPTY);
            nonnulllist.replaceAll(ignored -> DataIngredient.CONTENTS_STREAM_CODEC.decode(buf));

            ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
            String storageName = buf.readUtf();
            return new ItemStorageDataRecipe(group, category, stack, storageName, nonnulllist);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, ItemStorageDataRecipe recipe) {
            buf.writeUtf(recipe.group);
            buf.writeEnum(recipe.category);
            buf.writeVarInt(recipe.ingredients.size());

            for (DataIngredient ingredient : recipe.ingredients) {
                DataIngredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
            }

            ItemStack.STREAM_CODEC.encode(buf, recipe.result);
            buf.writeUtf(recipe.getStorageName());
        }
    }
}
