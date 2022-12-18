package net.ixdarklord.ultimine_addition.item;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemRegistries {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

    public static void register(IEventBus eventBus) {
        ITEMS.register("miner_certificate", () -> ItemsList.MINER_CERTIFICATE);
        ITEMS.register(eventBus);
    }
}
