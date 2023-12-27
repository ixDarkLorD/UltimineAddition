package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcat_lib.common.item.ComponentItem;

public class ModernItem extends ComponentItem {
    public ModernItem(Properties properties, ComponentType componentType) {
        super(properties, componentType);
    }

    @Override
    public boolean appendToName() {
        return true;
    }
}
