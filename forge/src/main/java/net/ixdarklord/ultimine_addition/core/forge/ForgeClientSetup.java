package net.ixdarklord.ultimine_addition.core.forge;

import net.ixdarklord.ultimine_addition.client.undo.UndoBlockEntities;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.ixdarklord.ultimine_addition.client.undo.UndoGhostRenderer;
import net.ixdarklord.ultimine_addition.client.renderer.item.UAItemModels;
import net.ixdarklord.ultimine_addition.client.particle.CelebrateParticle;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;

// Forge-only client setup (item properties, particles, render hooks). The mod's client side is constructed by
// CoolCatLib (UltimineAdditionClientConstructor, from ForgeSetup).
public final class ForgeClientSetup {
    private ForgeClientSetup() {}

    @Mod.EventBusSubscriber(modid = "ultimine_addition", value = Dist.CLIENT)
    public static class Events {
        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
                UndoGhostRenderer.render(event.getPoseStack());
            } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
                UndoBlockEntities.render(event.getPoseStack());
            }
        }

    }

    // Mod bus events (Forge 47 keeps them on a separate subscriber).
    @Mod.EventBusSubscriber(modid = "ultimine_addition", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(Registration.CELEBRATE_PARTICLE.get(), CelebrateParticle.Provider::new);
        }

        // The generic Mine-Go Juice, in its card type's color.
        @SubscribeEvent
        public static void onRegisterItemColors(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event) {
            event.register(net.ixdarklord.ultimine_addition.common.item.GenericMineGoJuiceItem::tint, net.ixdarklord.ultimine_addition.common.item.ModItems.MINE_GO_JUICE_GENERIC);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> UAItemModels.registerProperties(ItemProperties::register));
        }
    }
}
