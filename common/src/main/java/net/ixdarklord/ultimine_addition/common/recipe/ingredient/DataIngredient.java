package net.ixdarklord.ultimine_addition.common.recipe.ingredient;

import net.minecraft.world.item.Items;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class DataIngredient implements Predicate<ItemStack> {
    public static final DataIngredient EMPTY = new DataIngredient(Stream.empty());
    public static final Codec<DataIngredient> CODEC;
    public static final Codec<DataIngredient> CODEC_NONEMPTY;
    public static final StreamCodec<FriendlyByteBuf, DataIngredient> CONTENTS_STREAM_CODEC;

    private final Value[] values;
    @Nullable
    private ItemStack[] itemStacks;
    private int amount;

    private DataIngredient(Stream<? extends DataIngredient.Value> stream) {
        this.values = stream.toArray(DataIngredient.Value[]::new);
    }

    private DataIngredient(DataIngredient.Value[] values) {
        this.values = values;
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


    public boolean isEmpty() {
        return this.values.length == 0 && (this.itemStacks == null || this.itemStacks.length == 0);
    }

    private static DataIngredient fromValues(Stream<? extends Value> stream) {
        DataIngredient DataIngredient = new DataIngredient(stream);
        return DataIngredient.values.length == 0 ? EMPTY : DataIngredient;
    }

    public static DataIngredient of() {
        return EMPTY;
    }

    public static DataIngredient of(int amount, ItemLike... items) {
        // Built from items (not stacks): item components aren't bound yet during datagen.
        return fromValues(Arrays.stream(items).map(ItemLike::asItem).filter(item -> item != Items.AIR)
                .map(item -> new ItemValue(item, amount)));
    }

    public static DataIngredient of(int amount, ItemStack... stacks) {
        return of(amount, Arrays.stream(stacks));
    }

    public static DataIngredient of(int amount, Stream<ItemStack> stacks) {
        return fromValues(stacks.filter((itemStack) -> !itemStack.isEmpty()).map(stack -> new ItemValue(stack.getItem(), amount)));
    }

    public static DataIngredient of(TagKey<Item> tag, int amount) {
        return fromValues(Stream.of(new TagValue(tag, amount)));
    }

    /** Display stacks for each ingredient, tagged with the amount it adds (in their NBT, as "amount"). */
    public static List<ItemStack> toDisplayStacks(NonNullList<DataIngredient> inputs) {
        return inputs.stream().flatMap(ingredient -> Arrays.stream(ingredient.getItems()).map(stack -> {
            ItemStack copy = stack.copy();
            copy.getOrCreateTag().putInt("amount", ingredient.getAmount());
            return copy;
        })).toList();
    }

    private static Codec<DataIngredient> codec(boolean allowEmpty) {
        Codec<DataIngredient.Value[]> codec = Codec.list(DataIngredient.Value.CODEC)
                .comapFlatMap((list) -> !allowEmpty && list.isEmpty()
                        ? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined")
                        : DataResult.success(list.toArray(new DataIngredient.Value[0])), List::of);

        return Codec.either(codec, DataIngredient.Value.CODEC)
                .flatComapMap((either) -> either.map(DataIngredient::new, (value) -> new DataIngredient(new DataIngredient.Value[]{value})),
                        (ingredient) -> {
                            if (ingredient.values.length == 1)
                                return DataResult.success(Either.right(ingredient.values[0]));
                            else return ingredient.values.length == 0 && !allowEmpty
                                    ? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined")
                                    : DataResult.success(Either.left(ingredient.values));
                        }
                );
    }

    static {
        CONTENTS_STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull DataIngredient decode(FriendlyByteBuf buf) {
                return DataIngredient.of(
                        ByteBufCodecs.INT.decode(buf),
                        ByteBufCodecs.ITEM_STACK.apply(ByteBufCodecs.list()).decode(buf).stream());
            }

            @Override
            public void encode(FriendlyByteBuf buf, DataIngredient ingredient) {
                ByteBufCodecs.INT.encode(buf, ingredient.getAmount());
                ByteBufCodecs.ITEM_STACK.apply(ByteBufCodecs.list()).encode(buf, Arrays.stream(ingredient.getItems()).toList());
            }
        };

        CODEC = codec(true);

        CODEC_NONEMPTY = codec(false);
    }

    interface Value {
        Codec<DataIngredient.Value> CODEC = Codec.either(DataIngredient.ItemValue.CODEC, DataIngredient.TagValue.CODEC).xmap(either -> either.map(itemValue -> itemValue, tagValue -> tagValue), value -> {
            if (value instanceof DataIngredient.ItemValue itemValue) {
                return Either.left(itemValue);
            } else if (value instanceof DataIngredient.TagValue tagValue) {
                return Either.right(tagValue);
            } else throw new UnsupportedOperationException("This is neither an item value nor a tag value.");
        });

        Collection<ItemStack> getItems();
        int getAmount();
    }
    private record TagValue(TagKey<Item> tag, int amount) implements Value {
        static final Codec<TagValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TagValue::tag),
                Codec.INT.optionalFieldOf("increment_amount", 0).forGetter(TagValue::getAmount)
        ).apply(instance, TagValue::new));

        @Override
        public boolean equals(Object object) {
            if (object instanceof TagValue tagValue) {
                return tagValue.tag.location().equals(this.tag.location());
            } else {
                return false;
            }
        }

        public Collection<ItemStack> getItems() {
            List<ItemStack> list = Lists.newArrayList();
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
                list.add(new ItemStack(holder.value()));
            }
            return list;
        }

        @Override
        public int getAmount() {
            return this.amount;
        }

        @Override
        public TagKey<Item> tag() {
            return this.tag;
        }
    }
    private record ItemValue(Item item, int amount) implements Value {
        static final Codec<ItemValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ItemValue::item),
                Codec.INT.optionalFieldOf("increment_amount", 0).forGetter(ItemValue::getAmount)
        ).apply(instance, ItemValue::new));

        public Collection<ItemStack> getItems() {
            return Collections.singleton(new ItemStack(this.item));
        }

        public int getAmount() {
            return this.amount;
        }
    }
}
