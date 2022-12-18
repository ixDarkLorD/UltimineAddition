package net.ixdarklord.ultimine_addition.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeModeTabsList {
    public static final CreativeModeTab ULTIMINE_ADDITION_TAB = new CreativeModeTab("ultimine_addition.tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemsList.MINER_CERTIFICATE);
        }
    };
}
