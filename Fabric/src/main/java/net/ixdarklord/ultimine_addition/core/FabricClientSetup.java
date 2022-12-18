package net.ixdarklord.ultimine_addition.core;

import net.fabricmc.api.ClientModInitializer;
import net.ixdarklord.ultimine_addition.client.ItemsRenderer;
import net.ixdarklord.ultimine_addition.event.ClientEventsHandler;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.particle.ParticlesList;

public class FabricClientSetup implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemsRenderer.register();
        ParticlesList.registerProvider();

        ClientEventsHandler.register();
        PacketHandler.registerS2CPackets();
    }
}
