package net.ixdarklord.ultimine_addition.core;

import dev.ftb.mods.ftbultimine.integration.FTBUltiminePlugin;
import net.fabricmc.api.ModInitializer;
import net.ixdarklord.ultimine_addition.core.plugin.FTBUltimatePlugin;
import net.ixdarklord.ultimine_addition.event.EventsHandler;
import net.ixdarklord.ultimine_addition.item.ItemsRegistries;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.particle.ParticlesList;

public class FabricSetup implements ModInitializer {
    @Override
    public void onInitialize() {
        new CommonSetup();

        ItemsRegistries.register();
        ParticlesList.register();

        EventsHandler.register();
        PacketHandler.registerC2SPackets();
    }
}
