package net.ixdarklord.ultimine_addition.core;

import dev.ftb.mods.ftbultimine.integration.FTBUltiminePlugin;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.plugin.FTBUltimatePlugin;

public class CommonSetup {
    public CommonSetup() {
        ConfigHandler.register();
        FTBUltiminePlugin.register(new FTBUltimatePlugin());
    }
}
