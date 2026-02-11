package net.ixdarklord.ultimine_addition.core.neoforge;

import net.ixdarklord.ultimine_addition.client.gui.screens.ShapeSelectorScreen;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.particle.CelebrateParticle;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@Mod(value = FTBUltimineAddition.MOD_ID, dist = Dist.CLIENT)
public final class NeoForgeClientSetup {
    public NeoForgeClientSetup() {}

    @EventBusSubscriber(modid = "ultimine_addition", value = Dist.CLIENT)
    public static class Events {
        @SubscribeEvent
        private static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(Registration.CELEBRATE_PARTICLE.get(), CelebrateParticle.Provider::new);
        }

        @SubscribeEvent
        private static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
            event.register(Registration.SKILLS_RECORD_CONTAINER.get(), SkillsRecordScreen::new);
            event.register(Registration.SHAPE_SELECTOR_CONTAINER.get(), ShapeSelectorScreen::new);
        }
    }
}
