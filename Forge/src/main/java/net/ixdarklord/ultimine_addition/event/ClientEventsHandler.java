package net.ixdarklord.ultimine_addition.event;

import net.ixdarklord.ultimine_addition.client.ItemsRenderer;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.core.plugin.FTBUltimatePlugin;
import net.ixdarklord.ultimine_addition.particle.CelebrateParticle;
import net.ixdarklord.ultimine_addition.particle.ParticlesList;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEventsHandler {
    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
    public static class Event {}

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class EventBus {
        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event) {
            ItemsRenderer.register(event);
        }

        @SubscribeEvent
        public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
            event.register(ParticlesList.CELEBRATE_PARTICLE.get(), CelebrateParticle.Provider::new);
        }
    }
}
