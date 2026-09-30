package net.ixdarklord.ultimine_addition.core;

import dev.ftb.mods.ftbultimine.integration.FTBUltiminePlugin;
import net.ixdarklord.coolcatcore.api.core.ModConstructor;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.common.event.BrewingEvents;
import net.ixdarklord.ultimine_addition.common.event.EventHandler;
import net.ixdarklord.ultimine_addition.config.UAConfigs;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;

// The mod's common entry point, constructed by CoolCatLib: Core on both sides: the "coolcatcore:common" entrypoint on Fabric,
// ForgeSetup on Forge. Loader-only setup stays in FabricSetup / ForgeSetup.
public final class UltimineAdditionConstructor implements ModConstructor {
    // Registration, payloads and configs: Forge collects registries while mods are constructed.
    @Override
    public void onConstructMod() {
        CustomMSCApi.init();
        UAConfigs.register();
        Registration.register();
        PayloadHandler.init();
        // FTB Ultimine 2001 has no restriction handlers; its plugins can refuse Ultimine the same way.
        FTBUltiminePlugin.register(FTBUltimineIntegration.INSTANCE);
        // CoolCatLib 1.20.1 builds the brewing recipes once, in Forge's common setup (which may run before this mod's
        // onCommonSetup), so the listener goes in now.
        BrewingEvents.init();
    }

    @Override
    public void onCommonSetup() {
        EventHandler.register();
    }
}
