package net.ixdarklord.ultimine_addition.integration.jei;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.ixdarklord.ultimine_addition.common.data.item.StorageItemData;
import net.ixdarklord.ultimine_addition.common.item.PenItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;


// JEI 15 (1.20.1) subtype interpreters return one string per subtype.
public class PenInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
    @Override
    public @NotNull String apply(ItemStack stack, UidContext context) {
        if (!StorageItemData.DATA_COMPONENT.has(stack)) return "";
        StringBuilder stringBuilder = new StringBuilder(stack.getItem().getDescriptionId());
        if (((PenItem)stack.getItem()).getData(stack).isFull()) {
            stringBuilder.append(":full_capacity");
        } else stringBuilder.append(":empty_capacity");
        return stringBuilder.toString();
    }
}
