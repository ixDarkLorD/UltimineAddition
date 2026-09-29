package net.ixdarklord.ultimine_addition.common.recipe;

import com.mojang.serialization.MapCodec;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Skills Record and one dye, anywhere in the grid: the same record (its link, so its contents, stays) in the dye's
 * color. The color is vanilla's {@code base_color} component, which picks the record's item model and its look.
 */
public class SkillsRecordDyeRecipe extends CustomRecipe {
    public static final SkillsRecordDyeRecipe INSTANCE = new SkillsRecordDyeRecipe();
    public static final MapCodec<SkillsRecordDyeRecipe> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillsRecordDyeRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<SkillsRecordDyeRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private SkillsRecordDyeRecipe() {}

    private record Parts(ItemStack record, DyeColor dye) {}

    private static @Nullable Parts find(CraftingInput input) {
        ItemStack record = ItemStack.EMPTY;
        DyeColor dye = null;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof SkillsRecordItem) {
                if (!record.isEmpty()) return null;
                record = stack;
            } else {
                DyeColor color = stack.get(DataComponents.DYE);
                if (color == null || dye != null) return null;
                dye = color;
            }
        }
        // An undyed record is white already.
        if (record.isEmpty() || dye == null || record.getOrDefault(DataComponents.BASE_COLOR, DyeColor.WHITE) == dye) return null;
        return new Parts(record, dye);
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return find(input) != null;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input) {
        Parts parts = find(input);
        if (parts == null) return ItemStack.EMPTY;
        ItemStack result = parts.record().copyWithCount(1);
        result.set(DataComponents.BASE_COLOR, parts.dye());
        return result;
    }

    @Override
    public @NotNull RecipeSerializer<SkillsRecordDyeRecipe> getSerializer() {
        return Registration.SKILLS_RECORD_DYE_RECIPE_SERIALIZER.get();
    }
}
