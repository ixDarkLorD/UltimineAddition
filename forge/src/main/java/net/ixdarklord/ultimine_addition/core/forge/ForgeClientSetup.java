package net.ixdarklord.ultimine_addition.core.forge;

import dev.architectury.registry.menu.MenuRegistry;
import net.ixdarklord.ultimine_addition.client.gui.components.ClientSkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.gui.components.SkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.client.gui.screen.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.handler.ItemPropertiesHandler;
import net.ixdarklord.ultimine_addition.core.ClientSetup;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ForgeClientSetup {
    public ForgeClientSetup() {
        ClientSetup.init();
    }

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
    }
}
