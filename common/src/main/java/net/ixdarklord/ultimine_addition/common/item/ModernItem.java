package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcatlib.api.item.ComponentItem;
import net.minecraft.world.item.Item;

public class ModernItem extends ComponentItem {
    public ModernItem(Item.Properties properties, ComponentItem.ComponentType componentType) {
        super(properties, componentType);
    }

    public boolean appendToName() {
        return true;
    }
}
