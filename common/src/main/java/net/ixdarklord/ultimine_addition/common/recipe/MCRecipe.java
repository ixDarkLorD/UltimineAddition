package net.ixdarklord.ultimine_addition.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.recipe.ingredient.MCIngredient;
import net.ixdarklord.ultimine_addition.core.Registration;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MCRecipe extends ShapelessRecipe {
    // Recipes read by the codec before they know their id (1.20.1 recipes carry one) get it through withId.
    private static final ResourceLocation UNNAMED = FTBUltimineAddition.id("unnamed");
    // The result as 1.20.1's crafting recipes write it ({"item": ..., "count": ...}); these results carry no NBT.
    public static final Codec<ItemStack> RESULT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ItemStack::getItem),
            Codec.INT.optionalFieldOf("count", 1).forGetter(ItemStack::getCount)
    ).apply(instance, (item, count) -> new ItemStack(item, count)));

    final String group;
    final CraftingBookCategory category;
    final ItemStack result;
    final NonNullList<MCIngredient> ingredients;

    public MCRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<MCIngredient> ingredients) {
        this(UNNAMED, group, category, result, ingredients);
    }

    public MCRecipe(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result, NonNullList<MCIngredient> ingredients) {
        super(id, group, category, result, MCIngredient.toNormal(ingredients));
        this.group = group;
        this.category = category;
        this.result = result;
        this.ingredients = ingredients;
    }

    private MCRecipe withId(ResourceLocation id) {
        return new MCRecipe(id, this.group, this.category, this.result, this.ingredients);
    }

    @Override
    public @NotNull RecipeSerializer<MCRecipe> getSerializer() {
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
    public @NotNull ItemStack getResultItem(RegistryAccess registries) {
        return this.result;
    }

    public @NotNull ItemStack getResultItem() {
        return this.result.copy();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return MCIngredient.toNormal(this.ingredients);
    }

    public @NotNull NonNullList<MCIngredient> getMCIngredients() {
        return ingredients;
    }

    @Override
    public boolean matches(CraftingContainer input, @NotNull Level level) {
        // 1.20.1 has no CraftingInput: the non-empty stacks of the grid.
        List<ItemStack> stacks = input.getItems().stream().filter(stack -> !stack.isEmpty()).toList();
        if (stacks.size() != this.ingredients.size()) {
            return false;
        } else if (stacks.size() == 1 && this.ingredients.size() == 1) {
            return this.ingredients.get(0).test(stacks.get(0));
        } else {
            StackedContents contents = new StackedContents();
            stacks.forEach(stack -> contents.accountStack(stack, 1));
            return contents.canCraft(this, null);
        }
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        ItemStack stack = this.result.copy();
        if (stack.getItem() instanceof MiningSkillCardItem item) {
            MiningSkillCardData data = item.getData(stack);
            // Component only: this also runs for the crafting preview. The card is stored (and rolls its challenges)
            // once the player carries it.
            data.writeComponent();
        }
        return stack;
    }

    public static class Serializer implements RecipeSerializer<MCRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<MCRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(MCRecipe::getGroup),
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(MCRecipe::category),
                RESULT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
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

        public static final StreamCodec<FriendlyByteBuf, MCRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, MCRecipe::getGroup,
                ByteBufCodecs.idMapper(id -> CraftingBookCategory.values()[id], Enum::ordinal), MCRecipe::category,
                ByteBufCodecs.ITEM_STACK, recipe -> recipe.result,
                MCIngredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)), MCRecipe::getMCIngredients,
                MCRecipe::new
        );

        // 1.20.1 serializers read JSON and the network themselves: through the codecs above.
        @Override
        public @NotNull MCRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
            return CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow(false, error -> {}).withId(id);
        }

        @Override
        public @NotNull MCRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
            return STREAM_CODEC.decode(buf).withId(id);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf, @NotNull MCRecipe recipe) {
            STREAM_CODEC.encode(buf, recipe);
        }
    }
}
