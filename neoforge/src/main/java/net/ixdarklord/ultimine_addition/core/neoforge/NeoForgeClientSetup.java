package net.ixdarklord.ultimine_addition.core.neoforge;

import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.ixdarklord.ultimine_addition.client.undo.UndoBlockEntities;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.ixdarklord.ultimine_addition.client.undo.UndoGhostRenderer;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.ixdarklord.ultimine_addition.client.renderer.item.UAItemModels;
import net.ixdarklord.ultimine_addition.client.particle.CelebrateParticle;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

// NeoForge-only client setup (item models, particles, render hooks). The mod's client side is constructed by
// CoolCatLib (UltimineAdditionClientConstructor, from NeoForgeSetup).
public final class NeoForgeClientSetup {
    private NeoForgeClientSetup() {}

    @EventBusSubscriber(modid = "ultimine_addition", value = Dist.CLIENT)
    public static class Events {
        @SubscribeEvent
        private static void onRenderLevel(RenderLevelStageEvent.AfterTranslucentBlocks event) {
            UndoGhostRenderer.render(event.getPoseStack());
        }

        @SubscribeEvent
        private static void onSubmitGeometry(SubmitCustomGeometryEvent event) {
            UndoBlockEntities.submit(event.getPoseStack(), event.getSubmitNodeCollector(), event.getLevelRenderState().cameraRenderState);
        }

        @SubscribeEvent
        private static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(Registration.CELEBRATE_PARTICLE.get(), CelebrateParticle.Provider::new);
        }

        @SubscribeEvent
        private static void onRegisterItemModels(RegisterItemModelsEvent event) {
            UAItemModels.registerModels(event::register);
        }

        @SubscribeEvent
        private static void onRegisterConditionalItemModelProperties(RegisterConditionalItemModelPropertyEvent event) {
            UAItemModels.registerConditionalProperties(event::register);
        }
    }
}
