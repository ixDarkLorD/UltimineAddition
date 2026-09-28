package net.ixdarklord.ultimine_addition.core;

import net.ixdarklord.coolcatcore.api.core.ModConstructor;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.common.event.EventHandler;
import net.ixdarklord.ultimine_addition.config.UAConfigs;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;

// The mod's common entry point, constructed by CoolCatLib: Core on both sides: the "coolcatcore:common" entrypoint on Fabric,
// NeoForgeSetup on NeoForge. Loader-only setup stays in FabricSetup / NeoForgeSetup.
public final class UltimineAdditionConstructor implements ModConstructor {
    // Registration, payloads and configs: NeoForge collects payloads and registries while mods are constructed.
    @Override
    public void onConstructMod() {
        CustomMSCApi.init();
        UAConfigs.register();
        Registration.register();
        PayloadHandler.init();
    }

    @Override
    public void onCommonSetup() {
        EventHandler.register();
    }
}
