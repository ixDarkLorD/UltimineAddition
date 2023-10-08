package net.ixdarklord.ultimine_addition.common.event;

public class EventHandler {
    public static void register() {
        ChallengeEvents.init();
        ChunkEvents.init();
        WorldEvents.init();
        CommandEvents.init();
    }
}
