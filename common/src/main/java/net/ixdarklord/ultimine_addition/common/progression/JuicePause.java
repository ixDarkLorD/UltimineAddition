package net.ixdarklord.ultimine_addition.common.progression;

import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Mine-Go Juice that isn't wasted on the walk to the mine: with progression.juice_idle_pause set, a drunk juice's time
// only runs for that many seconds after each Ultimine. The rest of the time it is topped up, once a second, by the
// second that passed. (The Mastered card's renewed juice and infinite ones are left alone.)
public final class JuicePause {
    private static final int EVERY = 20;
    private static final Map<UUID, Long> LAST_ULTIMINE = new HashMap<>();

    private JuicePause() {}

    public static void onUltimine(ServerPlayer player) {
        LAST_ULTIMINE.put(player.getUUID(), (long) player.level().getServer().getTickCount());
    }

    public static void forget(Player player) {
        LAST_ULTIMINE.remove(player.getUUID());
    }

    /** Once a tick for each player. */
    public static void tick(ServerPlayer player) {
        int seconds = UAServerConfig.JUICE_IDLE_PAUSE.get();
        if (seconds <= 0 || player.tickCount % EVERY != 0) return;
        Long last = LAST_ULTIMINE.get(player.getUUID());
        if (last != null && player.level().getServer().getTickCount() - last < seconds * 20L) return;
        for (MobEffectInstance instance : List.copyOf(player.getActiveEffects())) {
            if (!(instance.getEffect().value() instanceof MineGoJuiceEffect) || !instance.isVisible() || instance.isInfiniteDuration()
                    || instance.getDuration() <= EVERY * 2) continue;
            player.addEffect(new MobEffectInstance(instance.getEffect(), instance.getDuration() + EVERY, instance.getAmplifier(),
                    instance.isAmbient(), instance.isVisible(), instance.showIcon()));
        }
    }
}
