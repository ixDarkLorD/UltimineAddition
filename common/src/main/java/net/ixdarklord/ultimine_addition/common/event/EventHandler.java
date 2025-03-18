package net.ixdarklord.ultimine_addition.common.event;

public class EventHandler {
    public static void register() {
        MSCEvents.init();
        ChallengesEvents.init();
        IneligibleBlocksEvents.init();
        SyncEvents.init();
        TradesEvent.init();
        CommandEvents.init();
        BrewingEvents.init();
    }
}
