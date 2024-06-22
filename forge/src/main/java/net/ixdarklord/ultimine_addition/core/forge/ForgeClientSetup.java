package net.ixdarklord.ultimine_addition.core.forge;

import dev.architectury.registry.menu.MenuRegistry;
import net.ixdarklord.ultimine_addition.client.event.impl.ClientHudEvent;
import net.ixdarklord.ultimine_addition.client.gui.component.ClientSkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.gui.component.SkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.gui.screen.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.handler.ItemPropertiesHandler;
import net.ixdarklord.ultimine_addition.core.ClientSetup;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


public class ForgeClientSetup {
    public ForgeClientSetup() {
        ClientSetup.init();
    }

    @Mod.EventBusSubscriber(modid = UltimineAddition.MOD_ID, value = Dist.CLIENT)
    public static class Event {
        @SubscribeEvent
        public static void onRenderGameOverlayEvent(final RenderGuiOverlayEvent.Pre event) {
            if (event.getOverlay() == VanillaGuiOverlay.DEBUG_TEXT.type()) {
                ClientHudEvent.RENDER_PRE.invoker().renderHud(event.getPoseStack(), event.getPartialTick());
            }
        }
    }

    @Mod.EventBusSubscriber(modid = UltimineAddition.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class EventBus {
        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemPropertiesHandler.register();
                MenuRegistry.registerScreenFactory(Registration.SKILLS_RECORD_CONTAINER.get(), SkillsRecordScreen::new);
            });
        }

        @SubscribeEvent
        public static void onClientTooltipComponentRegister(final RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(SkillsRecordTooltip.class, ClientSkillsRecordTooltip::new);
            event.register(SkillsRecordTooltip.Option.class, ClientSkillsRecordTooltip.Option::new);
        }
    }
}
