package net.ixdarklord.ultimine_addition.integration.jei;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.ixdarklord.ultimine_addition.common.item.PenItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PenInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
    @Override
    public @NotNull String apply(ItemStack ingredient, UidContext context) {
        if (!ingredient.hasTag()) return IIngredientSubtypeInterpreter.NONE;
        StringBuilder stringBuilder = new StringBuilder(ingredient.getItem().getDescriptionId());
        if (((PenItem)ingredient.getItem()).getData(ingredient).isFull()) {
            stringBuilder.append(":full_capacity");
        } else stringBuilder.append(":empty_capacity");
        return stringBuilder.toString();
    }
}
