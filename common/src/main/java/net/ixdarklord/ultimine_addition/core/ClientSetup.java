package net.ixdarklord.ultimine_addition.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.ultimine_addition.client.event.ClientEventHandler;
import net.ixdarklord.ultimine_addition.client.handler.KeyHandler;

@Environment(EnvType.CLIENT)
public class ClientSetup {
    public static void init() {
        Registration.registerParticleProviders();
        KeyHandler.register();
        ClientEventHandler.register();
    }
}
