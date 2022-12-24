package net.ixdarklord.ultimine_addition.item;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ItemsRegistries {
    public static final CreativeModeTab ULTIMINE_ADDITION_TAB = FabricItemGroupBuilder.build(
            new ResourceLocation(Constants.MOD_ID, "tab"), () -> new ItemStack(ItemsList.MINER_CERTIFICATE)
    );

    public static void register() {
        Registry.register(Registry.ITEM, new ResourceLocation(Constants.MOD_ID, "miner_certificate"), ItemsList.MINER_CERTIFICATE);
    }
}
