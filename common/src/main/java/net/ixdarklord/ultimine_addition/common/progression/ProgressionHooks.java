package net.ixdarklord.ultimine_addition.common.progression;

import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

// What packs and other mods can hang on to as players progress. Each moment below runs the functions of a data pack
// function tag as the player (ultimine_addition:on_challenge_completed, on_tier_reached, on_daily_completed,
// on_perk_learned, on_prestige), and is handed to the listeners registered here: the KubeJS plugin posts them to scripts
// as UltimineAdditionEvents.
public final class ProgressionHooks {
    public enum Kind {
        CHALLENGE_COMPLETED, TIER_REACHED, DAILY_COMPLETED, PERK_LEARNED, PRESTIGE;

        public String id() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * @param cardType  the card type's id ("pickaxe", "mypack:hammer"...)
     * @param tier      the card's tier at that moment (the tier reached, for TIER_REACHED; the prestige count, for PRESTIGE)
     * @param challenge the challenge, where there is one
     * @param detail    the perk's id, for PERK_LEARNED; "" otherwise
     */
    public record Event(Kind kind, ServerPlayer player, String cardType, int tier, @Nullable ResourceLocation challenge, String detail) {
    }

    private static final List<Consumer<Event>> LISTENERS = new CopyOnWriteArrayList<>();

    private ProgressionHooks() {}

    public static void listen(Consumer<Event> listener) {
        LISTENERS.add(listener);
    }

    public static void fire(Kind kind, ServerPlayer player, String cardType, int tier, @Nullable ResourceLocation challenge, String detail) {
        Event event = new Event(kind, player, cardType, tier, challenge, detail);
        for (Consumer<Event> listener : LISTENERS) {
            try {
                listener.accept(event);
            } catch (RuntimeException e) {
                FTBUltimineAddition.LOGGER.error("A listener of {} failed: {}", kind.id(), e.toString());
            }
        }
        MinecraftServer server = player.level().getServer();
        try {
            var functions = server.getFunctions();
            var source = server.createCommandSourceStack().withEntity(player).withPosition(player.position())
                    .withRotation(player.getRotationVector()).withSuppressedOutput();
            for (var function : functions.getTag(FTBUltimineAddition.id("on_" + kind.id()))) functions.execute(function, source);
        } catch (RuntimeException e) {
            FTBUltimineAddition.LOGGER.error("The functions of ultimine_addition:on_{} failed: {}", kind.id(), e.toString());
        }
    }
}
