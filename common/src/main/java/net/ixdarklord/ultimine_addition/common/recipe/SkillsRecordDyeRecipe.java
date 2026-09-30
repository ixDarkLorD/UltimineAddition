package net.ixdarklord.ultimine_addition.common.recipe;

import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Skills Record and one dye, anywhere in the grid: the same record (its link, so its contents, stays) in the dye's
 * color. The color is vanilla's {@code base_color} component, which picks the record's item model and its look.
 */
public class SkillsRecordDyeRecipe extends CustomRecipe {
    public static final RecipeSerializer<SkillsRecordDyeRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(SkillsRecordDyeRecipe::new);

    public SkillsRecordDyeRecipe(CraftingBookCategory category) {
        super(category);
    }

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
                // 1.21.1 has no dye component: a dye is a DyeItem.
                if (!(stack.getItem() instanceof DyeItem dyeItem) || dye != null) return null;
                dye = dyeItem.getDyeColor();
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
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider registries) {
        Parts parts = find(input);
        if (parts == null) return ItemStack.EMPTY;
        ItemStack result = parts.record().copyWithCount(1);
        result.set(DataComponents.BASE_COLOR, parts.dye());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<SkillsRecordDyeRecipe> getSerializer() {
        return Registration.SKILLS_RECORD_DYE_RECIPE_SERIALIZER.get();
    }
}
