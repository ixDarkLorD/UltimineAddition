package net.ixdarklord.ultimine_addition.integration.jei;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.ixdarklord.ultimine_addition.common.data.item.StorageItemData;
import net.ixdarklord.ultimine_addition.common.item.StorageItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PenInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
   public @NotNull String apply(@NotNull ItemStack stack, @NotNull UidContext context) {
      return StorageItemData.hasData(stack) && ((StorageItem) stack.getItem()).getData(stack).isFull() ? stack.getDescriptionId() + ":full_capacity" : "";
   }
}
