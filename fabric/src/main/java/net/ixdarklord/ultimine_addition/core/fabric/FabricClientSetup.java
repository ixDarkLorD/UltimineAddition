package net.ixdarklord.ultimine_addition.core.fabric;

import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.ItemModels;
import net.ixdarklord.ultimine_addition.client.renderer.item.UAItemModels;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.ixdarklord.ultimine_addition.client.particle.CelebrateParticle;
import net.ixdarklord.ultimine_addition.core.Registration;

public class FabricClientSetup implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Fabric API has no item model type registry; register into vanilla's mappers (access-widened).
        UAItemModels.registerModels(ItemModels.ID_MAPPER::put);
        UAItemModels.registerConditionalProperties(ConditionalItemModelProperties.ID_MAPPER::put);
        ParticleProviderRegistry.getInstance().register(Registration.CELEBRATE_PARTICLE.get(), CelebrateParticle.Provider::new);
    }
}
