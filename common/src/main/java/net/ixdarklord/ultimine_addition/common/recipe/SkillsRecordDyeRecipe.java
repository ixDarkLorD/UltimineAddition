package net.ixdarklord.ultimine_addition.common.recipe;

import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Skills Record and one dye, anywhere in the grid: the same record (its link, so its contents, stays) in the dye's
 * color. The color is the record's {@code Color} tag (see {@link SkillsRecordItem#getColor}; 26.1.2 uses vanilla's
 * {@code base_color} component), which picks the record's item model and its look.
 */
public class SkillsRecordDyeRecipe extends CustomRecipe {
    public static final RecipeSerializer<SkillsRecordDyeRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(SkillsRecordDyeRecipe::new);

    public SkillsRecordDyeRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    private record Parts(ItemStack record, DyeColor dye) {}

    private static @Nullable Parts find(CraftingContainer input) {
        ItemStack record = ItemStack.EMPTY;
        DyeColor dye = null;
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof SkillsRecordItem) {
                if (!record.isEmpty()) return null;
                record = stack;
            } else {
                if (!(stack.getItem() instanceof DyeItem dyeItem) || dye != null) return null;
                dye = dyeItem.getDyeColor();
            }
        }
        // An undyed record is white already.
        if (record.isEmpty() || dye == null || SkillsRecordItem.getColorOrDefault(record) == dye) return null;
        return new Parts(record, dye);
    }

    @Override
    public boolean matches(@NotNull CraftingContainer input, @NotNull Level level) {
        return find(input) != null;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer input, @NotNull RegistryAccess registries) {
        Parts parts = find(input);
        if (parts == null) return ItemStack.EMPTY;
        ItemStack result = parts.record().copyWithCount(1);
        SkillsRecordItem.setColor(result, parts.dye());
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
