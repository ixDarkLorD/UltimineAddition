package net.ixdarklord.ultimine_addition.core.fabric;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraftforge.fml.config.ModConfig.Type;

public final class ServicePlatformImpl implements ServicePlatform {
    public static ServicePlatform get() {
        return new ServicePlatformImpl();
    }

    public void registerConfig() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            ForgeConfigRegistry.INSTANCE.register(FTBUltimineAddition.MOD_ID, Type.CLIENT, ConfigHandler.CLIENT.SPEC, "%s/client-config.toml".formatted(FTBUltimineAddition.MOD_ID));
        }

        ForgeConfigRegistry.INSTANCE.register(FTBUltimineAddition.MOD_ID, Type.COMMON, ConfigHandler.COMMON.SPEC, "%s/common-config.toml".formatted(FTBUltimineAddition.MOD_ID));
        ForgeConfigRegistry.INSTANCE.register(FTBUltimineAddition.MOD_ID, Type.SERVER, ConfigHandler.SERVER.SPEC, "%s/server-config.toml".formatted(FTBUltimineAddition.MOD_ID));
    }

    public ServicePlatform.SlotAPI slotAPI() {
        return new ServicePlatformSlotAPIImpl();
    }

    public ServicePlatform.Players players() {
        return new ServicePlatformPlayersImpl();
    }
}
