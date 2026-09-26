package net.ixdarklord.ultimine_addition.core;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.common.event.EventHandler;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;

public class CommonSetup {
    public static void init() {
        CustomMSCApi.init();
        ConfigHandler.register();
        Registration.register();
        EnvExecutor.runInEnv(Env.CLIENT, () -> ClientSetup::init);
    }

    public static void setup() {
        EventHandler.register();
        PayloadHandler.init();
    }
}
