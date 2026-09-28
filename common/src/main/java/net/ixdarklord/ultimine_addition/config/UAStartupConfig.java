package net.ixdarklord.ultimine_addition.config;

import net.ixdarklord.coolcatcore.api.config.Config;
import net.ixdarklord.coolcatcore.api.config.ConfigBuilder;
import net.ixdarklord.coolcatcore.api.config.ConfigScope;
import net.ixdarklord.coolcatcore.api.config.ConfigValue;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;

// Read while registering content and loading recipes, before server configs exist; changes apply after a restart.
// A client joining a server with another playstyle mode is stopped before entering the world and offered the server's
// (CoolCatLib's startup sync): both sides built their items and recipes from it.
public final class UAStartupConfig {
    private static final ConfigBuilder BUILDER = Config.builder(FTBUltimineAddition.MOD_ID, ConfigScope.STARTUP)
            .comment(FTBUltimineAddition.MOD_NAME + " startup settings.", "These settings are read at startup; restart the game after changing them.");

    public static final ConfigValue<PlaystyleMode> PLAYSTYLE_MODE = BUILDER.enumValue("playstyle_mode", PlaystyleMode.MODERN)
            .comment("Defines the playstyle mode for the mod:",
                    "modern: Modern playstyle with Mining Skill Cards, the Skills Record and Shape Certificates.",
                    "one_tier_only [WIP]: Single-tier Mining Skill Card that upgrades to Mastered upon challenge completion.",
                    "legacy: Restores mechanics from v0.1.0 (only one miner certificate and one challenge).",
                    "Players joining a server must use the server's mode.")
            .build();

    public static final Config CONFIG = BUILDER.build();

    private UAStartupConfig() {}
}
