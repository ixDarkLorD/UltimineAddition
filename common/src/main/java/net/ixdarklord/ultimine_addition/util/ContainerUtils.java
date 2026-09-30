package net.ixdarklord.ultimine_addition.util;

import net.ixdarklord.coolcatcore.api.utils.ItemStackHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class ContainerUtils {
    public static boolean equals(Container container, Container container1) {
        if (container.getContainerSize() != container1.getContainerSize())
            return false;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            ItemStack stack1 = container1.getItem(i);
            if (!ItemStack.isSameItemSameTags(stack, stack1))
                return false;
        }
        return true;
    }

    public static int hashCode(Container container) {
        int code = 31 + container.getContainerSize();
        for (int i = 0; i < container.getContainerSize(); i++) {
            code += (ItemStackHelper.hashItemAndTags(container.getItem(i)) * 2);
        }
        return code;
    }
}
