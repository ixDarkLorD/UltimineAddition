package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.ultimine_addition.config.UAStartupConfig;
import net.ixdarklord.coolcatcore.api.data.ItemDataComponent;
import net.ixdarklord.coolcatcore.api.item.ComponentItem;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.minecraft.world.item.ItemStack;

public abstract class DataAbstractItem<T extends ItemDataComponent<T>> extends ComponentItem {
    public DataAbstractItem(Properties properties, ComponentType componentType) {
        super(properties, componentType);
    }

    public abstract T getData(ItemStack stack);

    public boolean isLegacyMode() {
        return UAStartupConfig.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY;
    }

    @Override
    public boolean appendToName() {
        return true;
    }
}
