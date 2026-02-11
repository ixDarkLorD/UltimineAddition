package net.ixdarklord.ultimine_addition.integration.jei;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.ixdarklord.ultimine_addition.common.data.item.StorageItemData;
import net.ixdarklord.ultimine_addition.common.item.PenItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PenInterpreter implements ISubtypeInterpreter<ItemStack> {
    @Override
    public @Nullable Object getSubtypeData(ItemStack stack, UidContext context) {
        return stack.has(StorageItemData.DATA_COMPONENT)
                ? Objects.requireNonNull(stack.get(StorageItemData.DATA_COMPONENT)).isFull()
                : null;
    }

    @Override
    public @NotNull String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
        if (!stack.has(StorageItemData.DATA_COMPONENT)) return "";
        StringBuilder stringBuilder = new StringBuilder(stack.getItem().getDescriptionId());
        if (((PenItem)stack.getItem()).getData(stack).isFull()) {
            stringBuilder.append(":full_capacity");
        } else stringBuilder.append(":empty_capacity");
        return stringBuilder.toString();
    }
}
