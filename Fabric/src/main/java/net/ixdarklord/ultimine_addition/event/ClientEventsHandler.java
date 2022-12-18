package net.ixdarklord.ultimine_addition.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.ixdarklord.ultimine_addition.core.plugin.FTBUltimatePlugin;

public class ClientEventsHandler {
    public static void register() {
        onKeyInput();
    }

    private static void onKeyInput() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FTBUltimatePlugin.keyEvent(client.player);
        });
    }
}
