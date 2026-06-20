package net.ixdarklord.ultimine_addition.integration.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.ingredients.TypedIngredient;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class ConfigIngredientInfoRecipe implements IJeiIngredientInfoRecipe {
    private final List<ConfigValueInfo> description;
    private final List<ITypedIngredient<?>> ingredients;

    public static void addConfigInfo(@NotNull IRecipeRegistration registration, ItemStack itemStack, String translationKey, ForgeConfigSpec.ConfigValue<?> configValue) {
        IJeiIngredientInfoRecipe recipe = create(registration.getIngredientManager(), List.of(itemStack), VanillaTypes.ITEM_STACK, new ConfigValueInfo(translationKey, configValue));
        registration.addRecipes(RecipeTypes.INFORMATION, List.of(recipe));
    }

    public static <T> IJeiIngredientInfoRecipe create(IIngredientManager ingredientManager, List<T> ingredients, IIngredientType<T> ingredientType, ConfigValueInfo... configValueInfos) {
        List<ITypedIngredient<T>> typedIngredients = createAndFilterInvalidNonnullList(ingredientManager, ingredientType, ingredients);
        return new ConfigIngredientInfoRecipe(typedIngredients, Arrays.stream(configValueInfos).toList());
    }

    private ConfigIngredientInfoRecipe(List<? extends ITypedIngredient<?>> ingredients, List<ConfigValueInfo> description) {
        this.description = description;
        this.ingredients = Collections.unmodifiableList(ingredients);
    }

    private static <T> List<ITypedIngredient<T>> createAndFilterInvalidNonnullList(IIngredientManager ingredientManager, IIngredientType<T> ingredientType, Collection<T> ingredients) {
        IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
        List<ITypedIngredient<T>> results = new ArrayList<>(ingredients.size());

        for (T ingredient : ingredients) {
            Optional<ITypedIngredient<T>> result = TypedIngredient.createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, true);
            Objects.requireNonNull(results);
            result.ifPresent(results::add);
        }

        return results;
    }

    public @Unmodifiable @NotNull List<ITypedIngredient<?>> getIngredients() {
        return this.ingredients;
    }

    public @Unmodifiable @NotNull List<FormattedText> getDescription() {
        return this.description.stream().map(ConfigValueInfo::getFormattedText).toList();
    }

    public record ConfigValueInfo(String translationKey, ForgeConfigSpec.ConfigValue<?> configValue) {
        public FormattedText getFormattedText() {
            return Component.translatable(this.translationKey, this.configValue.get());
        }
    }
}
