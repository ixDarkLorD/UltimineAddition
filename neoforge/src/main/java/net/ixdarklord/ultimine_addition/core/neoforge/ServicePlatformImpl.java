package net.ixdarklord.ultimine_addition.core.neoforge;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;

public class ServicePlatformImpl {
    public static void registerConfig() {
        ModList.get().getModContainerById(UltimineAddition.MOD_ID).ifPresent(container -> {
            if (Platform.getEnvironment() == Env.CLIENT)
                container.registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT.SPEC, UltimineAddition.MOD_ID + "/client-config.toml");
            container.registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON.SPEC, UltimineAddition.MOD_ID + "/common-config.toml");
        });
    }
}
