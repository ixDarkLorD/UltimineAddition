package net.ixdarklord.ultimine_addition.core.neoforge;

import net.ixdarklord.ultimine_addition.client.undo.UndoBlockEntities;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.ixdarklord.ultimine_addition.client.undo.UndoGhostRenderer;
import net.ixdarklord.ultimine_addition.client.renderer.item.UAItemModels;
import net.ixdarklord.ultimine_addition.client.particle.CelebrateParticle;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

// NeoForge-only client setup (item properties, particles, render hooks). The mod's client side is constructed by
// CoolCatLib (UltimineAdditionClientConstructor, from NeoForgeSetup).
public final class NeoForgeClientSetup {
    private NeoForgeClientSetup() {}

    @EventBusSubscriber(modid = "ultimine_addition", value = Dist.CLIENT)
    public static class Events {
        @SubscribeEvent
        private static void onRenderLevel(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
                UndoGhostRenderer.render(event.getPoseStack());
            } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
                UndoBlockEntities.render(event.getPoseStack());
            }
        }

        @SubscribeEvent
        private static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(Registration.CELEBRATE_PARTICLE.get(), CelebrateParticle.Provider::new);
        }

        // The generic Mine-Go Juice, in its card type's color.
        @SubscribeEvent
        private static void onRegisterItemColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event) {
            event.register(net.ixdarklord.ultimine_addition.common.item.GenericMineGoJuiceItem::tint, net.ixdarklord.ultimine_addition.common.item.ModItems.MINE_GO_JUICE_GENERIC);
        }

        @SubscribeEvent
        private static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> UAItemModels.registerProperties(ItemProperties::register));
        }
    }
}
