package net.ixdarklord.ultimine_addition.integration.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.ingredients.TypedIngredient;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigIngredientInfoRecipe implements IJeiIngredientInfoRecipe {
    private final List<ConfigValueInfo> description;
    private final List<ITypedIngredient<?>> ingredients;

    public static void addConfigInfo(@NotNull IRecipeRegistration registration, ItemStack itemStack, String translationKey, ModConfigSpec.ConfigValue<?> configValue) {
        IJeiIngredientInfoRecipe recipe = create(registration.getIngredientManager(), List.of(itemStack), VanillaTypes.ITEM_STACK, new ConfigIngredientInfoRecipe.ConfigValueInfo(translationKey, configValue));
        registration.addRecipes(RecipeTypes.INFORMATION, List.of(recipe));
    }

    public static <T> IJeiIngredientInfoRecipe create(
            IIngredientManager ingredientManager,
            List<T> ingredients,
            IIngredientType<T> ingredientType,
            ConfigValueInfo... configValueInfos
    ) {
        List<ITypedIngredient<T>> typedIngredients = TypedIngredient.createAndFilterInvalidNonnullList(ingredientManager, ingredientType, ingredients, true);
        return new ConfigIngredientInfoRecipe(typedIngredients, Arrays.stream(configValueInfos).toList());
    }

    private ConfigIngredientInfoRecipe(List<? extends ITypedIngredient<?>> ingredients, List<ConfigValueInfo> description) {
        this.description = description;
        this.ingredients = Collections.unmodifiableList(ingredients);
    }

    @Override
    public @Unmodifiable @NotNull List<ITypedIngredient<?>> getIngredients() {
        return ingredients;
    }

    @Override
    public @Unmodifiable @NotNull List<FormattedText> getDescription() {
        return description.stream()
                .map(ConfigValueInfo::getFormattedText)
                .toList();
    }

    public record ConfigValueInfo(String translationKey, ModConfigSpec.ConfigValue<?> configValue) {
        public FormattedText getFormattedText() {
            return Component.translatable(translationKey, configValue.get());
        }
    }
}
