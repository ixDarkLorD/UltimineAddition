package net.ixdarklord.ultimine_addition.item;

import net.ixdarklord.ultimine_addition.helper.Services;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ItemsList {
    public static Item MINER_CERTIFICATE = new MinerCertificate(new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.EPIC)
            .tab(Services.PLATFORM.getCreativeModeTab()));
}
