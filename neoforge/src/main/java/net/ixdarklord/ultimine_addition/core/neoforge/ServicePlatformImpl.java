package net.ixdarklord.ultimine_addition.core.neoforge;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;

public class ServicePlatformImpl implements ServicePlatform {
    public static ServicePlatform get() {
        return new ServicePlatformImpl();
    }

    @Override
    public void registerConfig() {
        ModList.get().getModContainerById(UltimineAddition.MOD_ID).ifPresent(container -> {
            if (Platform.getEnvironment() == Env.CLIENT)
                container.registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT.SPEC, UltimineAddition.MOD_ID + "/client-config.toml");
            container.registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON.SPEC, UltimineAddition.MOD_ID + "/common-config.toml");
            container.registerConfig(ModConfig.Type.SERVER, ConfigHandler.SERVER.SPEC, UltimineAddition.MOD_ID + "/server-config.toml");
        });
    }

    @Override
    public ServicePlatform.SlotAPI slotAPI() {
        return new ServicePlatformSlotAPIImpl();
    }

    @Override
    public ServicePlatform.Players players() {
        return new ServicePlatformPlayersImpl();
    }
}
