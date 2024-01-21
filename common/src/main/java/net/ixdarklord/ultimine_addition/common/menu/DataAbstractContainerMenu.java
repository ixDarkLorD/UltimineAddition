package net.ixdarklord.ultimine_addition.common.menu;

import net.ixdarklord.ultimine_addition.common.data.DataHandler;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public abstract class DataAbstractContainerMenu<T extends DataHandler<?, ?>> extends AbstractContainerMenu {
    protected DataAbstractContainerMenu(@Nullable MenuType<?> menuType, int windowId) {
        super(menuType, windowId);
    }

    public abstract T createData();
}
