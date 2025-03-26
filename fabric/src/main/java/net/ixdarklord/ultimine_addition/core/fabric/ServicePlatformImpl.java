package net.ixdarklord.ultimine_addition.core.fabric;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.neoforged.fml.config.ModConfig;

public final class ServicePlatformImpl implements ServicePlatform {
    public static ServicePlatform get() {
        return new ServicePlatformImpl();
    }

    @Override
    public void registerConfig() {
        if (Platform.getEnvironment() == Env.CLIENT)
            NeoForgeConfigRegistry.INSTANCE.register(FTBUltimineAddition.MOD_ID, ModConfig.Type.CLIENT, ConfigHandler.CLIENT.SPEC, FTBUltimineAddition.MOD_ID + "/client-config.toml");
        NeoForgeConfigRegistry.INSTANCE.register(FTBUltimineAddition.MOD_ID, ModConfig.Type.COMMON, ConfigHandler.COMMON.SPEC, FTBUltimineAddition.MOD_ID + "/common-config.toml");
        NeoForgeConfigRegistry.INSTANCE.register(FTBUltimineAddition.MOD_ID, ModConfig.Type.SERVER, ConfigHandler.SERVER.SPEC, FTBUltimineAddition.MOD_ID + "/server-config.toml");
    }

    @Override
    public SlotAPI slotAPI() {
        return new ServicePlatformSlotAPIImpl();
    }

    @Override
    public Players players() {
        return new ServicePlatformPlayersImpl();
    }
}
