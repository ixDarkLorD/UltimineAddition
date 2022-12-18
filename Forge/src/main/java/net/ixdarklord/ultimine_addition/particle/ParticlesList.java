package net.ixdarklord.ultimine_addition.particle;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ParticlesList {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister
            .create(ForgeRegistries.PARTICLE_TYPES, Constants.MOD_ID);

    public static final RegistryObject<SimpleParticleType> CELEBRATE_PARTICLE = PARTICLE_TYPES
            .register("celebrate_particle", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
