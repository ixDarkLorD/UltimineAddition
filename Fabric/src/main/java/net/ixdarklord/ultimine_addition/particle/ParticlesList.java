package net.ixdarklord.ultimine_addition.particle;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;

public class ParticlesList {
    public static final SimpleParticleType CELEBRATE_PARTICLE = FabricParticleTypes.simple();

    public static void register() {
        Registry.register(Registry.PARTICLE_TYPE, new ResourceLocation(Constants.MOD_ID, "celebrate_particle"), CELEBRATE_PARTICLE);
    }
    public static void registerProvider() {
        ParticleFactoryRegistry.getInstance().register(CELEBRATE_PARTICLE, CelebrateParticle.Provider::new);
    }
}
