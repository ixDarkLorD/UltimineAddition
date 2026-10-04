package net.ixdarklord.ultimine_addition.common.recipe;

import com.mojang.serialization.MapCodec;
import net.ixdarklord.ultimine_addition.common.item.GenericMiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An empty Mining Skill Card and one tool of a data pack card type, anywhere in the grid: that type's card. It is the
 * built-in cards' recipe (an empty card and a pickaxe, axe...) for the types data packs define, whose tools are only
 * known once the data packs load.
 */
public class DataCardRecipe extends CustomRecipe {
    public static final DataCardRecipe INSTANCE = new DataCardRecipe();
    public static final MapCodec<DataCardRecipe> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, DataCardRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<DataCardRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private DataCardRecipe() {}

    private static MiningSkillCardItem.@Nullable Type find(CraftingInput input) {
        if (PlaystyleModes.isLegacy()) return null;
        boolean card = false;
        ItemStack tool = ItemStack.EMPTY;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            if (stack.is(ModItems.MINING_SKILL_CARD_EMPTY)) {
                if (card) return null;
                card = true;
            } else {
                if (!tool.isEmpty()) return null;
                tool = stack;
            }
        }
        if (!card || tool.isEmpty()) return null;
        for (MiningSkillCardItem.Type type : MiningSkillCardItem.Type.getDataTypes()) {
            if (type.utilizeRequiredTools().contains(tool.getItem())) return type;
        }
        return null;
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return find(input) != null;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input) {
        MiningSkillCardItem.Type type = find(input);
        return type == null ? ItemStack.EMPTY : GenericMiningSkillCardItem.create(type);
    }

    @Override
    public @NotNull RecipeSerializer<DataCardRecipe> getSerializer() {
        return Registration.DATA_CARD_RECIPE_SERIALIZER.get();
    }
}
