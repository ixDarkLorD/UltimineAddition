package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcatlib.api.data.ItemDataComponent;
import net.ixdarklord.coolcatlib.api.item.ComponentItem;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.minecraft.world.item.ItemStack;

public abstract class DataAbstractItem<T extends ItemDataComponent<T>> extends ComponentItem {
    public DataAbstractItem(Properties properties, ComponentType componentType) {
        super(properties, componentType);
    }

    public abstract T getData(ItemStack stack);

    public boolean isLegacyMode() {
        return ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY;
    }

    @Override
    public boolean appendToName() {
        return true;
    }
}
