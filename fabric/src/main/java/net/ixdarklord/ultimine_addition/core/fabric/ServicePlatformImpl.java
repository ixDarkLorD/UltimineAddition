package net.ixdarklord.ultimine_addition.core.fabric;

import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@SuppressWarnings("unused")
public class ServicePlatformImpl {
    public static void registerConfig() {
        ModLoadingContext.registerConfig(UltimineAddition.MOD_ID, ModConfig.Type.CLIENT, ConfigHandler.CLIENT.SPEC, UltimineAddition.MOD_ID + "/client-config.toml");
        ModLoadingContext.registerConfig(UltimineAddition.MOD_ID, ModConfig.Type.COMMON, ConfigHandler.COMMON.SPEC, UltimineAddition.MOD_ID + "/common-config.toml");
    }
}
