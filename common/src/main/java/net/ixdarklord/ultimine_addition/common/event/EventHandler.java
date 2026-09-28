package net.ixdarklord.ultimine_addition.common.event;

import net.ixdarklord.ultimine_addition.common.undo.UltimineUndo;

public class EventHandler {
    public static void register() {
        DevEvents.init();
        MSCEvents.init();
        ChallengesEvents.init();
        IneligibleBlocksEvents.init();
        CertificateEvents.init();
        CommandEvents.init();
        BrewingEvents.init();
        UltimineUndo.init();
    }
}
