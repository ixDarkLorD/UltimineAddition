package net.ixdarklord.ultimine_addition.common.recipe.ingredient;

import com.google.common.collect.Lists;
import com.google.gson.*;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class DataIngredient implements Predicate<ItemStack> {
    public static final DataIngredient EMPTY = new DataIngredient(Stream.empty());
    private final Value[] values;
    @Nullable
    private ItemStack[] itemStacks;
    private int amount;
    @Nullable
    private IntList stackingIds;

    private DataIngredient(Stream<? extends Value> stream) {
        this.values = stream.toArray(Value[]::new);
    }

    public ItemStack[] getItems() {
        this.dissolve();
        return this.itemStacks;
    }

    public int getAmount() {
        this.dissolve();
        return this.amount;
    }

    private void dissolve() {
        if (this.itemStacks == null) {
            this.itemStacks = Arrays.stream(this.values).flatMap((value) ->
                    value.getItems().stream()).distinct().toArray(ItemStack[]::new);
        }
        this.amount = Arrays.stream(this.values).map(Value::getAmount).toList().get(0);

    }

    public boolean test(@Nullable ItemStack stack) {
        if (stack == null) return false;
        this.dissolve();
        if (this.itemStacks.length == 0) return stack.isEmpty();

        ItemStack[] stacks = this.itemStacks;
        for (ItemStack itemStack : stacks) {
            if (itemStack.is(stack.getItem())) {
                return true;
            }
        }
        return false;
    }

    public IntList getStackingIds() {
        if (this.stackingIds == null) {
            this.dissolve();
            this.stackingIds = new IntArrayList(this.itemStacks.length);
            ItemStack[] var1 = this.itemStacks;

            for (ItemStack itemStack : var1) {
                this.stackingIds.add(StackedContents.getStackingIndex(itemStack));
            }

            this.stackingIds.sort(IntComparators.NATURAL_COMPARATOR);
        }

        return this.stackingIds;
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        this.dissolve();
        buffer.writeInt(this.amount);
        buffer.writeCollection(Arrays.asList(this.itemStacks), FriendlyByteBuf::writeItem);
    }

    public JsonElement toJson() {
        if (this.values.length == 1) {
            return this.values[0].serialize();
        } else {
            JsonArray jsonArray = new JsonArray();
            for (Value value : this.values) {
                jsonArray.add(value.serialize());
            }

            return jsonArray;
        }
    }

    public boolean isEmpty() {
        return this.values.length == 0 && (this.itemStacks == null || this.itemStacks.length == 0) && (this.stackingIds == null || this.stackingIds.isEmpty());
    }

    private static DataIngredient fromValues(Stream<? extends Value> stream) {
        DataIngredient DataIngredient = new DataIngredient(stream);
        return DataIngredient.values.length == 0 ? EMPTY : DataIngredient;
    }

    public static DataIngredient of() {
        return EMPTY;
    }

    public static DataIngredient of(int amount, ItemLike... items) {
        return of(amount, Arrays.stream(items).map(ItemStack::new));
    }

    public static DataIngredient of(int amount, ItemStack... stacks) {
        return of(amount, Arrays.stream(stacks));
    }

    public static DataIngredient of(int amount, Stream<ItemStack> stacks) {
        return fromValues(stacks.filter((itemStack) -> !itemStack.isEmpty()).map(stack -> new ItemValue(stack, amount)));
    }

    public static DataIngredient of(TagKey<Item> tag, int amount) {
        return fromValues(Stream.of(new TagValue(tag, amount)));
    }

    public static NonNullList<Ingredient> toNormal(NonNullList<DataIngredient> inputs) {
        NonNullList<Ingredient> result = NonNullList.create();
        result.addAll(inputs.stream().map(ingredient -> {
            ItemStack[] items = Arrays.stream(ingredient.getItems()).peek(stack ->
                    stack.getOrCreateTag().putInt("amount", ingredient.getAmount()))
                    .toArray(ItemStack[]::new);
            return Ingredient.of(items);
        }).toList());
        return result;
    }

    public static DataIngredient fromNetwork(FriendlyByteBuf buffer) {
        int amount = buffer.readInt();
        return fromValues(buffer.readList(FriendlyByteBuf::readItem).stream().map(stack -> new ItemValue(stack, amount)));
    }

    public static DataIngredient fromJson(@Nullable JsonElement json) {
        if (json != null && !json.isJsonNull()) {
            if (json.isJsonObject()) {
                return fromValues(Stream.of(valueFromJson(json.getAsJsonObject())));
            } else if (json.isJsonArray()) {
                JsonArray jsonArray = json.getAsJsonArray();
                if (jsonArray.isEmpty()) {
                    throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
                } else {
                    return fromValues(StreamSupport.stream(jsonArray.spliterator(), false).map((jsonElement) ->
                            valueFromJson(GsonHelper.convertToJsonObject(jsonElement, "item"))));
                }
            } else {
                throw new JsonSyntaxException("Expected item to be object or array of objects");
            }
        } else {
            throw new JsonSyntaxException("Item cannot be null");
        }
    }

    private static Value valueFromJson(JsonObject json) {
        if (json.has("item") && json.has("tag")) {
            throw new JsonParseException("An DataIngredient entry is either a tag or an item, not both");
        } else if (json.has("item")) {
            Item item = ShapedRecipe.itemFromJson(json);
            int amount = GsonHelper.getAsInt(json, "amount");
            return new ItemValue(new ItemStack(item), amount);
        } else if (json.has("tag")) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, resourceLocation);
            int amount = GsonHelper.getAsInt(json, "amount");
            return new TagValue(tagKey, amount);
        } else {
            throw new JsonParseException("An DataIngredient entry needs either a tag or an item");
        }
    }

    interface Value {
        Collection<ItemStack> getItems();
        int getAmount();

        JsonObject serialize();
    }
    static class TagValue implements Value {
        private final TagKey<Item> tag;
        private final int amount;

        TagValue(TagKey<Item> tagKey, int amount) {
            this.tag = tagKey;
            this.amount = amount;
        }

        public Collection<ItemStack> getItems() {
            List<ItemStack> list = Lists.newArrayList();
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
                list.add(new ItemStack(holder));
            }
            return list;
        }

        public int getAmount() {
            return this.amount;
        }

        public JsonObject serialize() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("tag", this.tag.location().toString());
            jsonObject.addProperty("amount", getAmount());
            return jsonObject;
        }
    }
    static class ItemValue implements Value {
        private final ItemStack stack;
        private final int amount;

        ItemValue(ItemStack stack, int amount) {
            this.stack = stack;
            this.amount = amount;
        }

        public Collection<ItemStack> getItems() {
            return Collections.singleton(this.stack);
        }

        public int getAmount() {
            return this.amount;
        }

        public JsonObject serialize() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("item", Objects.requireNonNull(Registration.ITEMS.getRegistrar().getId(this.stack.getItem())).toString());
            jsonObject.addProperty("amount", getAmount());
            return jsonObject;
        }
    }
}
