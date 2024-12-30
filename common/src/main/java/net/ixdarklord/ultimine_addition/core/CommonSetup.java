package net.ixdarklord.ultimine_addition.core;

import dev.ftb.mods.ftbultimine.integration.FTBUltiminePlugin;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.common.event.EventHandler;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;

public class CommonSetup {
    public static void init() {
        FTBUltiminePlugin.register(new FTBUltimineIntegration());
        CustomMSCApi.init();
        ConfigHandler.register();
        Registration.register();
    }

    public static void setup() {
        ConfigHandler.validate();
        EventHandler.register();
        PacketHandler.init();
    }
}
