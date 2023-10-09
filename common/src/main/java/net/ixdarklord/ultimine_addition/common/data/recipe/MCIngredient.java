package net.ixdarklord.ultimine_addition.common.data.recipe;

import com.google.gson.*;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
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

public final class MCIngredient implements Predicate<ItemStack> {
    public static final MCIngredient EMPTY = new MCIngredient(Stream.empty());
    private final Value[] values;
    @Nullable
    private ItemStack[] itemStacks;
    private MiningSkillCardItem.Tier tier;
    @Nullable
    private IntList stackingIds;

    private MCIngredient(Stream<? extends Value> stream) {
        this.values = stream.toArray(Value[]::new);
    }

    public ItemStack[] getItems() {
        this.dissolve();
        return this.itemStacks;
    }

    public MiningSkillCardItem.Tier getTier() {
        this.dissolve();
        return this.tier;
    }

    private void dissolve() {
        if (this.itemStacks == null) {
            this.itemStacks = Arrays.stream(this.values).flatMap(value ->
                    value.getItems().stream()).distinct().toArray(ItemStack[]::new);
        }
        this.tier = Arrays.stream(this.values).map(Value::getTier).toList().get(0);
    }

    public boolean test(@Nullable ItemStack stack) {
        if (stack == null) return false;
        this.dissolve();
        if (this.itemStacks.length == 0) return stack.isEmpty();

        ItemStack[] stacks = this.itemStacks;
        for (ItemStack itemStack : stacks) {
            if (itemStack.is(stack.getItem())) {
                if (this.tier != null) {
                    if (itemStack.is(stack.getItem())) {
                        var cardData = new MiningSkillCardData().loadData(stack);
                        return this.tier.equals(cardData.getTier());
                    }
                } else return true;
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
        buffer.writeBoolean(this.tier != null);
        if (this.tier != null) buffer.writeInt(this.tier.getValue());
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

    private static MCIngredient fromValues(Stream<? extends Value> stream) {
        MCIngredient DataIngredient = new MCIngredient(stream);
        return DataIngredient.values.length == 0 ? EMPTY : DataIngredient;
    }

    public static MCIngredient of() {
        return EMPTY;
    }

    public static MCIngredient of(MiningSkillCardItem.Tier tier, ItemLike... items) {
        return of(tier, Arrays.stream(items).map(ItemStack::new));
    }

    public static MCIngredient of(MiningSkillCardItem.Tier tier, ItemStack... stacks) {
        return of(tier, Arrays.stream(stacks));
    }

    public static MCIngredient of(MiningSkillCardItem.Tier tier, Stream<ItemStack> stacks) {
        return fromValues(stacks.filter(itemStack -> !itemStack.isEmpty()).map(stack -> new ItemValue(stack, tier)));
    }

    public static MCIngredient of(MiningSkillCardItem.Tier tier, TagKey<Item> tag) {
        return fromValues(Stream.of(new TagValue(tag, tier)));
    }

    public static NonNullList<Ingredient> toNormal(NonNullList<MCIngredient> inputs) {
        NonNullList<Ingredient> result = NonNullList.create();

        // Item Values
        result.addAll(inputs.stream()
                .filter(ingredient -> !Arrays.stream(ingredient.values)
                        .filter(value -> value instanceof ItemValue).toList().isEmpty())
                .map(ingredient -> {
                    ItemStack[] items = Arrays.stream(ingredient.getItems()).peek(stack -> {
                        if (stack.getItem() instanceof MiningSkillCardItem item && ingredient.getTier() != null) {
                            item.getData(stack).setTier(ingredient.getTier()).saveData(stack);
                        }
                    }).toArray(ItemStack[]::new);
                    return Ingredient.of(items);
                })
                .toList()
        );

        // Tag Values
        result.addAll(inputs.stream()
                .filter(ingredient -> !Arrays.stream(ingredient.values).filter(value -> value instanceof TagValue).toList().isEmpty())
                .map(ingredient -> {
                    Stream<Ingredient.TagValue> tagValueStream = Arrays.stream(ingredient.values)
                            .filter(value -> value instanceof TagValue)
                            .map(value -> new Ingredient.TagValue(((TagValue) value).tag));
                    return Ingredient.fromValues(tagValueStream);
                })
                .toList()
        );
        return result;
    }

    public static MCIngredient fromNetwork(FriendlyByteBuf buffer) {
        MiningSkillCardItem.Tier tier = buffer.readBoolean() ? MiningSkillCardItem.Tier.fromInt(buffer.readInt()) : null;
        return fromValues(buffer.readList(FriendlyByteBuf::readItem).stream().map(stack -> new ItemValue(stack, tier)));
    }

    public static MCIngredient fromJson(@Nullable JsonElement json) {
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
            throw new JsonParseException("An MCIngredient entry is either a tag or an item, not both");
        } else if (json.has("item")) {
            Item item = ShapedRecipe.itemFromJson(json);
            MiningSkillCardItem.Tier tier = json.has("tier") ? MiningSkillCardItem.Tier.fromInt(GsonHelper.getAsInt(json, "tier")) : null;
            return new ItemValue(new ItemStack(item), tier);
        } else if (json.has("tag")) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, resourceLocation);
            MiningSkillCardItem.Tier tier = json.has("tier") ? MiningSkillCardItem.Tier.fromInt(GsonHelper.getAsInt(json, "tier")) : null;
            return new TagValue(tagKey, tier);
        } else {
            throw new JsonParseException("An MCIngredient entry needs either a tag or an item");
        }
    }

    interface Value {
        Collection<ItemStack> getItems();
        MiningSkillCardItem.Tier getTier();

        JsonObject serialize();
    }
    static class TagValue implements Value {
        protected final TagKey<Item> tag;
        private final MiningSkillCardItem.Tier tier;

        TagValue(TagKey<Item> tagKey, @Nullable MiningSkillCardItem.Tier tier) {
            this.tag = tagKey;
            this.tier = tier;
        }

        public Collection<ItemStack> getItems() {
            List<ItemStack> list = new ArrayList<>();
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
                ItemStack stack = new ItemStack(holder);
                if (this.tier != null && holder instanceof MiningSkillCardItem item) {
                    item.getData(stack).setTier(this.tier).saveData(stack);
                }
                list.add(stack);
            }
            return list;
        }

        public @Nullable MiningSkillCardItem.Tier getTier() {
            return this.tier;
        }

        public JsonObject serialize() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("tag", this.tag.location().toString());
            if (getTier() != null) jsonObject.addProperty("tier", getTier().getValue());
            return jsonObject;
        }
    }
    static class ItemValue implements Value {
        private final ItemStack stack;
        private final MiningSkillCardItem.Tier tier;

        ItemValue(ItemStack stack, @Nullable MiningSkillCardItem.Tier tier) {
            this.stack = stack;
            this.tier = tier;
        }

        public Collection<ItemStack> getItems() {
            return Collections.singleton(this.stack);
        }

        public @Nullable MiningSkillCardItem.Tier getTier() {
            return this.tier;
        }

        public JsonObject serialize() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("item", Objects.requireNonNull(Registration.ITEMS.getRegistrar().getId(this.stack.getItem())).toString());
            if (getTier() != null) jsonObject.addProperty("tier", getTier().getValue());
            return jsonObject;
        }
    }
}
