package net.ixdarklord.ultimine_addition.common.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class CustomSlot extends Slot {
    private boolean isActive = true;
    public CustomSlot(Container container, int slotID, int x, int y) {
        super(container, slotID, x, y);
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    public void setEnabled(boolean state) {
        this.isActive = state;
    }
}
