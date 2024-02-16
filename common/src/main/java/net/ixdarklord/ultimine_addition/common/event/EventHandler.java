package net.ixdarklord.ultimine_addition.common.event;

public class EventHandler {
    public static void register() {
        MSCEvents.init();
        ChallengeEvents.init();
        IBEvents.init();
        WorldEvents.init();
        CommandEvents.init();
    }
}
