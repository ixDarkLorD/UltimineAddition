package net.ixdarklord.ultimine_addition.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.ixdarklord.ultimine_addition.common.progression.ProgressionHooks;

// Loaded by KubeJS (kubejs.plugins.txt) when it is installed: the mod's progression moments as server script events.
//
//   UltimineAdditionEvents.challengeCompleted(event => { event.player.tell(`Done: ${event.challenge}`) })
//   UltimineAdditionEvents.tierReached(event => { if (event.tier == 4) event.player.give('minecraft:diamond') })
//   UltimineAdditionEvents.dailyCompleted / perkLearned / prestige
//
// Each event has player, cardType, tier, challenge (or null) and detail (the perk's id for perkLearned).
public class UAKubeJSPlugin extends KubeJSPlugin {
    public static final EventGroup GROUP = EventGroup.of("UltimineAdditionEvents");
    public static final EventHandler CHALLENGE_COMPLETED = GROUP.server("challengeCompleted", () -> ProgressKubeEvent.class);
    public static final EventHandler TIER_REACHED = GROUP.server("tierReached", () -> ProgressKubeEvent.class);
    public static final EventHandler DAILY_COMPLETED = GROUP.server("dailyCompleted", () -> ProgressKubeEvent.class);
    public static final EventHandler PERK_LEARNED = GROUP.server("perkLearned", () -> ProgressKubeEvent.class);
    public static final EventHandler PRESTIGE = GROUP.server("prestige", () -> ProgressKubeEvent.class);
    private static boolean listening;

    @Override
    public void registerEvents() {
        GROUP.register();
        if (listening) return;
        listening = true;
        ProgressionHooks.listen(event -> {
            EventHandler handler = switch (event.kind()) {
                case CHALLENGE_COMPLETED -> CHALLENGE_COMPLETED;
                case TIER_REACHED -> TIER_REACHED;
                case DAILY_COMPLETED -> DAILY_COMPLETED;
                case PERK_LEARNED -> PERK_LEARNED;
                case PRESTIGE -> PRESTIGE;
            };
            if (handler.hasListeners()) handler.post(ScriptType.SERVER, new ProgressKubeEvent(event));
        });
    }
}
