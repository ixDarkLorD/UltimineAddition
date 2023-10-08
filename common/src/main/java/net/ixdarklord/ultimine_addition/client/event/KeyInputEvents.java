package net.ixdarklord.ultimine_addition.client.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import net.ixdarklord.ultimine_addition.core.plugin.FTBUltimineIntegration;

public class KeyInputEvents {
    public static void init() {
        ClientRawInputEvent.KEY_PRESSED.register((instance, keyCode, scanCode, action, modifiers) -> {
            FTBUltimineIntegration.keyEvent(instance.player);
            return EventResult.pass();
        });
    }
}
