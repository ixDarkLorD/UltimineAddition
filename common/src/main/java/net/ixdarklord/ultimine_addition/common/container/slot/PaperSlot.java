package net.ixdarklord.ultimine_addition.common.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class PaperSlot extends CustomSlot {
    public PaperSlot(Container container, int slotID, int x, int y) {
        super(container, slotID, x, y);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return stack.getItem() == Items.PAPER;
    }
}
