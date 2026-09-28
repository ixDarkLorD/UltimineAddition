package net.ixdarklord.ultimine_addition.common.progression;

import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

// Extra challenge points: every Nth block of a quick streak, and the occasional lucky find.
public final class ChallengeBoosts {
    private static final int MIN_SHOWN_STREAK = 3;
    private static final Map<UUID, Streak> STREAKS = new HashMap<>();

    private ChallengeBoosts() {}

    public static int bonusPoints(ServerPlayer player, Identifier challengeId, BlockPos pos, ItemStack icon) {
        long now = player.level().getServer().getTickCount();
        Streak streak = STREAKS.computeIfAbsent(player.getUUID(), uuid -> new Streak());
        if (now - streak.lastTick > UAServerConfig.STREAK_WINDOW.get() * 20L) streak.count = 0;
        streak.count++;
        streak.lastTick = now;

        int interval = UAServerConfig.STREAK_BONUS_INTERVAL.get();
        boolean streakBonus = interval > 0 && streak.count % interval == 0;
        boolean lucky = ThreadLocalRandom.current().nextDouble() < UAServerConfig.LUCKY_FIND_CHANCE.get();

        Component challenge = Component.translatable("challenge.%s.%s.name".formatted(challengeId.getNamespace(), challengeId.getPath().replace('/', '.'))).withStyle(ChatFormatting.YELLOW);
        if (lucky) {
            player.level().sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.35, 0.35, 0.35, 0.0);
            player.level().playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, player.getSoundSource(), 1.0F, 1.4F);
            new UltimineNotice(UltimineNotice.Kind.LUCKY, Component.translatable("info.ultimine_addition.notice.lucky"),
                    List.of(Component.translatable("info.ultimine_addition.notice.lucky.info", challenge).withStyle(ChatFormatting.GRAY)), icon).send(player);
        } else if (interval > 0 && streak.count >= MIN_SHOWN_STREAK) {
            if (streakBonus) player.level().playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, player.getSoundSource(), 0.5F, 0.8F + Math.min(streak.count, 60) / 60.0F);
            int left = interval - streak.count % interval;
            Component info = streakBonus
                    ? Component.translatable("info.ultimine_addition.notice.streak.bonus", challenge).withStyle(ChatFormatting.GOLD)
                    : Component.translatable("info.ultimine_addition.notice.streak.next", left).withStyle(ChatFormatting.GRAY);
            new UltimineNotice(UltimineNotice.Kind.STREAK, Component.translatable("info.ultimine_addition.notice.streak", streak.count), List.of(info), icon).send(player);
        }
        return (streakBonus ? 1 : 0) + (lucky ? 1 : 0);
    }

    public static void forget(Player player) {
        STREAKS.remove(player.getUUID());
    }

    private static final class Streak {
        private long lastTick;
        private int count;
    }
}
