package net.ixdarklord.ultimine_addition.common.menu;

import net.ixdarklord.coolcatlib.api.data.DataComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public abstract class DataAbstractContainerMenu<T extends DataComponent<T>> extends AbstractContainerMenu {
    protected DataAbstractContainerMenu(@Nullable MenuType<?> menuType, int windowId) {
        super(menuType, windowId);
    }

    public abstract T getData();
}
