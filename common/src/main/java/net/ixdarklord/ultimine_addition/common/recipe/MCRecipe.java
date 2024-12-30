package net.ixdarklord.ultimine_addition.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.MCIngredient;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MCRecipe extends ShapelessRecipe {
    final String group;
    final CraftingBookCategory category;
    final ItemStack result;
    final NonNullList<MCIngredient> ingredients;

    public MCRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<MCIngredient> ingredients) {
        super(group, category, result, MCIngredient.toNormal(ingredients));
        this.group = group;
        this.category = category;
        this.result = result;
        this.ingredients = ingredients;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Registration.MC_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull String getGroup() {
        return this.group;
    }

    @Override
    public @NotNull CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return MCIngredient.toNormal(this.ingredients);
    }

    public @NotNull NonNullList<MCIngredient> getMCIngredients() {
        return ingredients;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != this.ingredients.size()) {
            return false;
        } else {
            return input.size() == 1 && this.ingredients.size() == 1 ? this.ingredients.getFirst().test(input.getItem(0)) : input.stackedContents().canCraft(this, null);
        }
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack stack = this.result.copy();
        if (stack.getItem() instanceof MiningSkillCardItem item) {
            NonNullList<ItemStack> inputs = NonNullList.create();
            for (ItemStack itemStack : input.items()) {
                if (!itemStack.isEmpty() && !(itemStack.getItem() instanceof MiningSkillCardItem))
                    inputs.add(itemStack.copy());
            }

            if (!inputs.isEmpty()) {
                item.getData(stack).setDisplayItem(inputs.getFirst());
            }
            item.getData(stack).initChallenges().saveData(stack);
        }
        return stack;
    }

    public static class Serializer implements RecipeSerializer<MCRecipe> {
        public static final MapCodec<MCRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(MCRecipe::getGroup),
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(MCRecipe::category),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(mcRecipe -> mcRecipe.result),
                MCIngredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(list -> {
                    MCIngredient[] ingredients = list.stream().filter(ingredient -> !ingredient.isEmpty()).toArray(MCIngredient[]::new);
                    if (ingredients.length == 0) {
                        return DataResult.error(() -> "No ingredients for MCRecipe");
                    } else {
                        return ingredients.length > 9
                                ? DataResult.error(() -> "Too many ingredients for MCRecipe")
                                : DataResult.success(NonNullList.of(MCIngredient.EMPTY, ingredients));
                    }
                }, DataResult::success).forGetter(MCRecipe::getMCIngredients)
        ).apply(instance, MCRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MCRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public @NotNull MapCodec<MCRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, MCRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static @NotNull MCRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            String group = buf.readUtf();
            CraftingBookCategory craftingBookCategory = buf.readEnum(CraftingBookCategory.class);
            NonNullList<MCIngredient> nonnulllist = MCIngredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)).decode(buf);
            ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
            return new MCRecipe(group, craftingBookCategory, stack, nonnulllist);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, MCRecipe recipe) {
            buf.writeUtf(recipe.group);
            buf.writeEnum(recipe.category);
            buf.writeVarInt(recipe.ingredients.size());
            MCIngredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)).encode(buf, recipe.getMCIngredients());
            ItemStack.STREAM_CODEC.encode(buf, recipe.result);
        }
    }
}
