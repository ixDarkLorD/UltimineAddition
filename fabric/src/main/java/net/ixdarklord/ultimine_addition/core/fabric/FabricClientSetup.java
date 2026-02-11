package net.ixdarklord.ultimine_addition.core.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.ixdarklord.ultimine_addition.client.particle.CelebrateParticle;
import net.ixdarklord.ultimine_addition.core.Registration;

public class FabricClientSetup implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(Registration.CELEBRATE_PARTICLE.get(), CelebrateParticle.Provider::new);
    }
}
