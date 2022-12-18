package net.ixdarklord.ultimine_addition.client;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.item.ItemsList;
import net.ixdarklord.ultimine_addition.item.MinerCertificate;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class ItemsRenderer {
    public static void register() {
        ItemProperties.register(ItemsList.MINER_CERTIFICATE,
                new ResourceLocation(Constants.MOD_ID, "opened"), (stack, level, living, id) -> {
                    return MinerCertificate.isAccomplished(stack) ? 1.0F : 0.0F;
                });
    }
}
