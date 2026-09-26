package net.ixdarklord.ultimine_addition.common.recipe;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.MCIngredient;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MCRecipe extends NormalCraftingRecipe {
    final ItemStackTemplate result;
    final NonNullList<MCIngredient> ingredients;

    public MCRecipe(String group, CraftingBookCategory category, ItemStackTemplate result, NonNullList<MCIngredient> ingredients) {
        this(new Recipe.CommonInfo(true), new CraftingRecipe.CraftingBookInfo(category, group), result, ingredients);
    }

    public MCRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ItemStackTemplate result, NonNullList<MCIngredient> ingredients) {
        super(commonInfo, bookInfo);
        this.result = result;
        this.ingredients = ingredients;
    }

    @Override
    public @NotNull RecipeSerializer<MCRecipe> getSerializer() {
        return Registration.MC_RECIPE_SERIALIZER.get();
    }

    @Override
    protected @NotNull PlacementInfo createPlacementInfo() {
        return PlacementInfo.create(MCIngredient.toNormal(this.ingredients));
    }

    public @NotNull ItemStack getResultItem() {
        return this.result.create();
    }

    public @NotNull NonNullList<MCIngredient> getMCIngredients() {
        return ingredients;
    }

    @Override
    public boolean matches(CraftingInput input, @NotNull Level level) {
        if (input.ingredientCount() != this.ingredients.size()) {
            return false;
        } else {
            return input.size() == 1 && this.ingredients.size() == 1 ? this.ingredients.getFirst().test(input.getItem(0)) : input.stackedContents().canCraft(this, null);
        }
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input) {
        ItemStack stack = this.result.create();
        if (stack.getItem() instanceof MiningSkillCardItem item) {
            NonNullList<ItemStack> inputs = NonNullList.create();
            for (ItemStack itemStack : input.items()) {
                if (!itemStack.isEmpty() && !(itemStack.getItem() instanceof MiningSkillCardItem))
                    inputs.add(itemStack.copy());
            }

            if (!inputs.isEmpty()) {
                item.getData(stack).setDisplayItem(inputs.getFirst());
            }
            item.getData(stack).initChallenges().save();
        }
        return stack;
    }

    @Override
    public @NotNull List<RecipeDisplay> display() {
        return List.of(new ShapelessCraftingRecipeDisplay(
                MCIngredient.toNormal(this.ingredients).stream().map(Ingredient::display).toList(),
                new SlotDisplay.ItemStackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
        ));
    }

    public static class Serializer {
        public static final MapCodec<MCRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
                CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
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

        public static final StreamCodec<RegistryFriendlyByteBuf, MCRecipe> STREAM_CODEC = StreamCodec.composite(
                Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
                CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
                ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
                MCIngredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)), MCRecipe::getMCIngredients,
                MCRecipe::new
        );

        public static final RecipeSerializer<MCRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
