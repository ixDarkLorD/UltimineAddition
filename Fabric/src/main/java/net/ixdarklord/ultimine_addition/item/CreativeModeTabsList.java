package net.ixdarklord.ultimine_addition.item;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeModeTabsList {
    public static final CreativeModeTab ULTIMINE_ADDITION_TAB = FabricItemGroupBuilder.build(
            new ResourceLocation(Constants.MOD_ID, "tab"), () -> new ItemStack(ItemsList.MINER_CERTIFICATE)
    );
}
