package net.ixdarklord.ultimine_addition.common.menu.slot;

import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MiningSkillCardSlot extends CustomSlot {
    public MiningSkillCardSlot(Container container, int slotID, int x, int y) {
        super(container, slotID, x, y);
    }

    public int getMaxStackSize() {
        return 1;
    }

    public int getMaxStackSize(@NotNull ItemStack stack) {
        return 1;
    }

    public boolean mayPlace(@NotNull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof MiningSkillCardItem card) {
            return card.getType() != MiningSkillCardItem.Type.EMPTY;
        } else {
            return false;
        }
    }
}
