package net.ixdarklord.ultimine_addition.item;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class ItemsRegistries {

    public static void register() {
        Registry.register(Registry.ITEM, new ResourceLocation(Constants.MOD_ID, "miner_certificate"), ItemsList.MINER_CERTIFICATE);
    }
}
