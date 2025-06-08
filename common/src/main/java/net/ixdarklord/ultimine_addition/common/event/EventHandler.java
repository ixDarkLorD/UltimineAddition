package net.ixdarklord.ultimine_addition.common.event;

public class EventHandler {
    public static void register() {
        DevEvents.init();
        MSCEvents.init();
        ChallengesEvents.init();
        IneligibleBlocksEvents.init();
        SyncEvents.init();
        CertificateEvents.init();
        TradesEvent.init();
        CommandEvents.init();
        BrewingEvents.init();
    }
}
