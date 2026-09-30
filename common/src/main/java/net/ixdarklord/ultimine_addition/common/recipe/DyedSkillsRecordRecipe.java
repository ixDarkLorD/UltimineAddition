package net.ixdarklord.ultimine_addition.common.recipe;

import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

/**
 * A shaped recipe making a dyed Skills Record: a vanilla shaped recipe with a {@code "color"} (a dye name) the result
 * is dyed in. 26.1.2 writes the color as the result's {@code base_color} component; 1.20.1's crafting results can't
 * carry NBT (Forge reads an {@code nbt} field, Fabric doesn't), so the color is the recipe's own.
 */
public class DyedSkillsRecordRecipe extends ShapedRecipe {
    private final DyeColor color;

    public DyedSkillsRecordRecipe(ShapedRecipe base, DyeColor color) {
        super(base.getId(), base.getGroup(), base.category(), base.getWidth(), base.getHeight(), base.getIngredients(),
                dyed(base.getResultItem(RegistryAccess.EMPTY), color), base.showNotification());
        this.color = color;
    }

    private static ItemStack dyed(ItemStack result, DyeColor color) {
        ItemStack stack = result.copy();
        SkillsRecordItem.setColor(stack, color);
        return stack;
    }

    public DyeColor getColor() {
        return this.color;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Registration.DYED_SKILLS_RECORD_RECIPE_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<DyedSkillsRecordRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public @NotNull DyedSkillsRecordRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
            ShapedRecipe base = RecipeSerializer.SHAPED_RECIPE.fromJson(id, json);
            String name = GsonHelper.getAsString(json, "color");
            DyeColor color = DyeColor.byName(name, null);
            if (color == null) throw new IllegalArgumentException("Unknown dye color: " + name);
            return new DyedSkillsRecordRecipe(base, color);
        }

        @Override
        public @NotNull DyedSkillsRecordRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
            ShapedRecipe base = RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buf);
            return new DyedSkillsRecordRecipe(base, buf.readEnum(DyeColor.class));
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf, @NotNull DyedSkillsRecordRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe);
            buf.writeEnum(recipe.color);
        }
    }
}
