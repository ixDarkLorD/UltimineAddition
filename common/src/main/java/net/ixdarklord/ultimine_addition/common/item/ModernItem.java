package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcat_lib.common.item.ComponentItem;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.config.PlaystyleMode;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModernItem extends ComponentItem {
    public ModernItem(Properties properties, ComponentType componentType) {
        super(properties, componentType);
    }

    @Override
    public void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {
        if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) return;
        super.fillItemCategory(category, items);
    }

    @Override
    public boolean appendToName() {
        return true;
    }
}
