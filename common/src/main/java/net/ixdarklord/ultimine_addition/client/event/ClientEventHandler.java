package net.ixdarklord.ultimine_addition.client.event;

import net.ixdarklord.ultimine_addition.client.handler.CustomClientTooltipComponent;

public class ClientEventHandler {
    public static void register() {
        KeyInputEvents.init();
        ScreenEvents.init();
        CustomClientTooltipComponent.init();
    }
}
