package net.ixdarklord.ultimine_addition.common.recipe.ingredient;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class MCIngredient implements Predicate<ItemStack> {
    public static final MCIngredient EMPTY = new MCIngredient(Stream.empty());
    public static final Codec<MCIngredient> CODEC;
    public static final Codec<MCIngredient> CODEC_NONEMPTY;
    public static final StreamCodec<RegistryFriendlyByteBuf, MCIngredient> CONTENTS_STREAM_CODEC;

    private final Value[] values;
    @Nullable
    private ItemStack[] itemStacks;
    private Optional<MiningSkillCardItem.Tier> tier;
    @Nullable
    private IntList stackingIds;

    private MCIngredient(Stream<? extends Value> stream) {
        this.values = stream.toArray(Value[]::new);
    }

    private MCIngredient(Value[] values) {
        this.values = values;
    }

    public ItemStack[] getItems() {
        this.dissolve();
        return this.itemStacks;
    }

    public Optional<MiningSkillCardItem.Tier> getTier() {
        this.dissolve();
        return this.tier;
    }

    private void dissolve() {
        if (this.itemStacks == null) {
            this.itemStacks = Arrays.stream(this.values).flatMap(value -> value.getItems().stream()).distinct().toArray(ItemStack[]::new);
        }
        this.tier = Arrays.stream(this.values).map(Value::getTier).toList().getFirst();
    }

    public boolean test(@Nullable ItemStack stack) {
        if (stack == null) return false;
        this.dissolve();
        if (this.itemStacks.length == 0) return stack.isEmpty();

        ItemStack[] stacks = this.itemStacks;
        for (ItemStack itemStack : stacks) {
            if (itemStack.is(stack.getItem())) {
                if (this.tier.isPresent()) {
                    var cardData = MiningSkillCardData.loadData(stack);
                    return this.tier.get().equals(cardData.getTier());
                } else return true;
            }
        }
        return false;
    }

    public IntList getStackingIds() {
        if (this.stackingIds == null) {
            ItemStack[] itemStacks = this.getItems();
            this.stackingIds = new IntArrayList(itemStacks.length);
            for (ItemStack itemStack : itemStacks) {
                this.stackingIds.add(StackedContents.getStackingIndex(itemStack));
            }

            this.stackingIds.sort(IntComparators.NATURAL_COMPARATOR);
        }

        return this.stackingIds;
    }

    public boolean isEmpty() {
        return this.values.length == 0;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof MCIngredient ingredient) {
            return Arrays.equals(this.values, ingredient.values);
        } else {
            return false;
        }
    }

    private static MCIngredient fromValues(Stream<? extends Value> stream) {
        MCIngredient mcIngredient = new MCIngredient(stream);
        return mcIngredient.values.length == 0 ? EMPTY : mcIngredient;
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
        return fromValues(stacks.filter(itemStack -> !itemStack.isEmpty()).map(stack -> new ItemValue(stack, Optional.ofNullable(tier))));
    }

    public static MCIngredient of(MiningSkillCardItem.Tier tier, TagKey<Item> tag) {
        return fromValues(Stream.of(new TagValue(tag, Optional.ofNullable(tier))));
    }

    public static NonNullList<Ingredient> toNormal(NonNullList<MCIngredient> inputs) {
        NonNullList<Ingredient> result = NonNullList.create();

        // Item Values
        result.addAll(inputs.stream()
                .filter(ingredient -> !Arrays.stream(ingredient.values)
                        .filter(value -> value instanceof ItemValue).toList().isEmpty())
                .map(ingredient -> {
                    ItemStack[] items = Arrays.stream(ingredient.getItems()).toArray(ItemStack[]::new);
                    return Ingredient.of(items);
                })
                .toList()
        );

        // Tag Values
        result.addAll(inputs.stream()
                .filter(ingredient -> !Arrays.stream(ingredient.values).filter(value -> value instanceof TagValue).toList().isEmpty())
                .map(ingredient -> {
                    Optional<TagKey<Item>> optional = Arrays.stream(ingredient.values)
                            .filter(value -> value instanceof TagValue)
                            .map(value -> ((TagValue) value).tag)
                            .findFirst();
                    return optional.map(Ingredient::of).orElse(Ingredient.EMPTY);
                })
                .toList()
        );
        return result;
    }

    private static Codec<MCIngredient> codec(boolean allowEmpty) {
        Codec<Value[]> codec = Codec.list(Value.CODEC)
                .comapFlatMap((list) -> !allowEmpty && list.isEmpty()
                        ? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined")
                        : DataResult.success(list.toArray(new Value[0])), List::of);

        return Codec.either(codec, Value.CODEC)
                .flatComapMap((either) -> either.map(MCIngredient::new, (value) -> new MCIngredient(new Value[]{value})),
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
            public @NotNull MCIngredient decode(RegistryFriendlyByteBuf buf) {
                return MCIngredient.of(
                        ByteBufCodecs.optional(MiningSkillCardItem.Tier.STREAM_CODEC).decode(buf).orElse(null),
                        ItemStack.LIST_STREAM_CODEC.decode(buf).stream());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, MCIngredient ingredient) {
                ByteBufCodecs.optional(MiningSkillCardItem.Tier.STREAM_CODEC).encode(buf, ingredient.getTier());
                ItemStack.LIST_STREAM_CODEC.encode(buf, Arrays.stream(ingredient.getItems()).toList());
            }
        };

        CODEC = codec(true);

        CODEC_NONEMPTY = codec(false);
    }

    private interface Value {
        Codec<Value> CODEC = Codec.xor(ItemValue.CODEC, TagValue.CODEC).xmap(either -> either.map(itemValue -> itemValue, tagValue -> tagValue), value -> {
            if (value instanceof ItemValue itemValue) {
                return Either.left(itemValue);
            } else if (value instanceof TagValue tagValue) {
                return Either.right(tagValue);
            } else throw new UnsupportedOperationException("This is neither an item value nor a tag value.");
        });

        Collection<ItemStack> getItems();

        Optional<MiningSkillCardItem.Tier> getTier();
    }

    private record TagValue(TagKey<Item> tag, Optional<MiningSkillCardItem.Tier> tier) implements Value {
        static final Codec<TagValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TagValue::tag),
                MiningSkillCardItem.Tier.CODEC.optionalFieldOf("card_tier").forGetter(TagValue::getTier)
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
            for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
                ItemStack stack = new ItemStack(item);
                if (this.tier.isPresent() && stack.getItem() instanceof MiningSkillCardItem cardItem) {
                    cardItem.getData(stack).setTier(this.tier.get()).saveData(stack);
                }
                list.add(stack);
            }
            return list;
        }

        @Override
        public Optional<MiningSkillCardItem.Tier> getTier() {
            return this.tier;
        }

        @Override
        public TagKey<Item> tag() {
            return this.tag;
        }
    }

    private record ItemValue(ItemStack stack, Optional<MiningSkillCardItem.Tier> tier) implements Value {
        static final Codec<ItemValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.SIMPLE_ITEM_CODEC.fieldOf("item").forGetter(ItemValue::stack),
                MiningSkillCardItem.Tier.CODEC.optionalFieldOf("card_tier").forGetter(ItemValue::getTier)
        ).apply(instance, ItemValue::new));

        public Collection<ItemStack> getItems() {
            if (this.tier.isPresent() && this.stack.getItem() instanceof MiningSkillCardItem cardItem) {
                cardItem.getData(this.stack).setTier(this.tier.get()).saveData(this.stack);
            }
            return Collections.singleton(this.stack);
        }

        @Override
        public Optional<MiningSkillCardItem.Tier> getTier() {
            return this.tier;
        }
    }
}
