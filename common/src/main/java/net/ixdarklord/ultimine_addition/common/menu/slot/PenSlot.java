package net.ixdarklord.ultimine_addition.common.menu.slot;

import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PenSlot extends CustomSlot {
    public PenSlot(Container container, int slotID, int x, int y) {
        super(container, slotID, x, y);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return stack.getItem() == ModItems.PEN;
    }
}
