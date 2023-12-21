package net.ixdarklord.ultimine_addition.core;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.ftb.mods.ftbultimine.integration.FTBUltiminePlugin;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.api.UAApi;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.event.EventHandler;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.core.plugin.FTBUltimineIntegration;

public class CommonSetup {
    public static void init() {
        FTBUltiminePlugin.register(new FTBUltimineIntegration());
        CustomMSCApi.init();
        ConfigHandler.register();

        Registration.register();
        EventHandler.register();
        LifecycleEvent.SETUP.register(() -> {
            UAApi.init();
            PacketHandler.register();
        });
    }
}
