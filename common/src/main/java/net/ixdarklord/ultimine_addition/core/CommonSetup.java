package net.ixdarklord.ultimine_addition.core;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.ftb.mods.ftbultimine.integration.FTBUltiminePlugin;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.event.EventHandler;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;

public class CommonSetup {
    public static void init() {
        FTBUltiminePlugin.register(new FTBUltimineIntegration());
        CustomMSCApi.init();
        ConfigHandler.register();
        LifecycleEvent.SETUP.register(() -> {
            ConfigHandler.validate();
            EventHandler.register();
            PacketHandler.register();
        });
        Registration.register();

    }
}
