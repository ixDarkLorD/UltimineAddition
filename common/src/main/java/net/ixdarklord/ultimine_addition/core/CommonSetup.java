package net.ixdarklord.ultimine_addition.core;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.ftb.mods.ftbultimine.integration.FTBUltiminePlugin;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.common.event.EventHandler;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.network.PacketHandler;

public final class CommonSetup {
   public static void init() {
      FTBUltiminePlugin.register(new FTBUltimineIntegration());
      CustomMSCApi.init();
      ConfigHandler.register();
      Registration.register();
      EnvExecutor.runInEnv(Env.CLIENT, () -> ClientSetup::init);
   }

   public static void setup() {
      EventHandler.register();
      PacketHandler.register();
   }
}
