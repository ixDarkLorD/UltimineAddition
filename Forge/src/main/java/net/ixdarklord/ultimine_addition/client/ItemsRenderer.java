package net.ixdarklord.ultimine_addition.client;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.item.ItemRegistries;
import net.ixdarklord.ultimine_addition.item.MinerCertificate;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ItemsRenderer {
    public static void register(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ItemRegistries.MINER_CERTIFICATE.get(),
                    new ResourceLocation(Constants.MOD_ID, "opened"), (stack, level, living, id) -> {
                        return MinerCertificate.isAccomplished(stack) ? 1.0F : 0.0F;
            });
        });
    }
}
