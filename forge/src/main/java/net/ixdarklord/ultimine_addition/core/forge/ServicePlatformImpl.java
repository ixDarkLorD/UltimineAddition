package net.ixdarklord.ultimine_addition.core.forge;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;

public final class ServicePlatformImpl implements ServicePlatform {
    public static ServicePlatform get() {
        return new ServicePlatformImpl();
    }

    public void registerConfig() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            ModLoadingContext.get().registerConfig(Type.CLIENT, ConfigHandler.CLIENT.SPEC, "%s/client-config.toml".formatted(FTBUltimineAddition.MOD_ID));
        }
        ModLoadingContext.get().registerConfig(Type.COMMON, ConfigHandler.COMMON.SPEC, "%s/common-config.toml".formatted(FTBUltimineAddition.MOD_ID));
        ModLoadingContext.get().registerConfig(Type.SERVER, ConfigHandler.SERVER.SPEC, "%s/server-config.toml".formatted(FTBUltimineAddition.MOD_ID));
    }

    public ServicePlatform.SlotAPI slotAPI() {
        return new ServicePlatformSlotAPIImpl();
    }

    public ServicePlatform.Players players() {
        return new ServicePlatformPlayersImpl();
    }
}
