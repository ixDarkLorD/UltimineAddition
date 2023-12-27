package net.ixdarklord.ultimine_addition.client.event;

public class ClientEventHandler {
    public static void register() {
        ScreenEvents.init();
        KeyInputEvents.init();
    }
}
