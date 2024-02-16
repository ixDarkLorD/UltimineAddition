package net.ixdarklord.ultimine_addition.core.fabric;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraftforge.fml.config.ModConfig;

@SuppressWarnings("unused")
public class ServicePlatformImpl {
    public static void registerConfig() {
        ForgeConfigRegistry.INSTANCE.register(UltimineAddition.MOD_ID, ModConfig.Type.CLIENT, ConfigHandler.CLIENT.SPEC, UltimineAddition.MOD_ID + "/client-config.toml");
        ForgeConfigRegistry.INSTANCE.register(UltimineAddition.MOD_ID, ModConfig.Type.COMMON, ConfigHandler.COMMON.SPEC, UltimineAddition.MOD_ID + "/common-config.toml");
    }
}
