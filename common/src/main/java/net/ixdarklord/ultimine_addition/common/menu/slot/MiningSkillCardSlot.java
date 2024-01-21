package net.ixdarklord.ultimine_addition.common.menu.slot;

import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MiningSkillCardSlot extends CustomSlot {
    public MiningSkillCardSlot(Container container, int slotID, int x, int y) {
        super(container, slotID, x, y);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return 1;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof MiningSkillCardItem card) {
            return card.getType() != MiningSkillCardItem.Type.EMPTY;
        }
        return false;
    }
}
