package net.ixdarklord.ultimine_addition.item;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class ItemRegistries {
    public static final CreativeModeTab ULTIMINE_ADDITION_TAB = new CreativeModeTab("ultimine_addition.tab") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(MINER_CERTIFICATE.get());
        }
    };
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

    public static final RegistryObject<Item> MINER_CERTIFICATE = ITEMS.register("miner_certificate", () -> ItemsList.MINER_CERTIFICATE);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
