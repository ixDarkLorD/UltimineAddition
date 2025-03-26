package net.ixdarklord.ultimine_addition.core.neoforge;

import net.ixdarklord.ultimine_addition.client.gui.screens.ShapeSelectorScreen;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.gui.tooltip.ClientSkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.gui.tooltip.SkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.particle.CelebrateParticle;
import net.ixdarklord.ultimine_addition.core.ClientSetup;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@Mod(value = FTBUltimineAddition.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeClientSetup {
    public NeoForgeClientSetup() {
        ClientSetup.init();
    }

    @EventBusSubscriber(modid = FTBUltimineAddition.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class EventBus {
        @SubscribeEvent
        private static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(ClientSetup::setup);
        }

        @SubscribeEvent
        private static void onParticleProvidersRegister(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(Registration.CELEBRATE_PARTICLE.get(), CelebrateParticle.Provider::new);
        }

        @SubscribeEvent
        private static void onMenuScreensRegister(RegisterMenuScreensEvent event) {
            event.register(Registration.SKILLS_RECORD_CONTAINER.get(), SkillsRecordScreen::new);
            event.register(Registration.SHAPE_SELECTOR_CONTAINER.get(), ShapeSelectorScreen::new);
        }

        @SubscribeEvent
        private static void onClientTooltipComponentRegister(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(SkillsRecordTooltip.class, ClientSkillsRecordTooltip::new);
            event.register(SkillsRecordTooltip.Option.class, ClientSkillsRecordTooltip.Option::new);
        }
    }
}
