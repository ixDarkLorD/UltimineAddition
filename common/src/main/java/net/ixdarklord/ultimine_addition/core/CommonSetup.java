package net.ixdarklord.ultimine_addition.core;

import dev.ftb.mods.ftbultimine.api.restriction.RegisterRestrictionHandlerEvent;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.common.event.EventHandler;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;

public class CommonSetup {
    public static void init() {
        RegisterRestrictionHandlerEvent.REGISTER.register(registry -> registry.register(FTBUltimineIntegration.INSTANCE));
        CustomMSCApi.init();
        ConfigHandler.register();
        Registration.register();
    }

    public static void setup() {
        EventHandler.register();
        PayloadHandler.init();
    }
}
