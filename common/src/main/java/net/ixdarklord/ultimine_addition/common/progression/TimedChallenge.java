package net.ixdarklord.ultimine_addition.common.progression;

import net.ixdarklord.coolcatcore.api.platform.Platform;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.challenge.IneligibleBlocksSavedData;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.common.data.reward.DataRewards;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.tag.ModBlockTags;
import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

// The daily (or weekly) challenge: one of the loaded challenges, the same for everyone on the server, picked anew each
// day (by the server's clock). It needs no card and no Skills Record: doing it with the right tool counts. Finishing it
// pays experience, a while of Mine-Go Juice for the challenge's tool, and what data packs add ("when": "timed").
// Each player's progress is kept with their ability data.
public final class TimedChallenge {
    // How much more than a card's challenge of the same kind it asks for.
    private static final int DAILY_FACTOR = 1, WEEKLY_FACTOR = 5;

    private static @Nullable Current cached;
    private static int cachedChallenges = -1;

    private TimedChallenge() {}

    public record Current(Period mode, long period, Identifier id, ChallengeData data, int amount, List<Block> targets) {
        public Component title() {
            return Component.translatable("info.ultimine_addition.timed.title." + this.mode.name().toLowerCase(Locale.ROOT));
        }
    }

    public enum Period implements net.ixdarklord.coolcatcore.api.config.type.EnumType.Displayable {
        OFF, DAILY, WEEKLY;

        @Override
        public Component displayName() {
            return Component.translatable("ultimine_addition.timed_challenge." + this.name().toLowerCase(Locale.ROOT));
        }
    }

    /** The challenge running now, or null: turned off, the legacy playstyle, or no challenge that fits. */
    public static @Nullable Current current(MinecraftServer server) {
        Period mode = UAServerConfig.TIMED_CHALLENGE.get();
        if (mode == Period.OFF || PlaystyleModes.isLegacy()) return null;
        long day = LocalDate.now().toEpochDay();
        // Weeks start on Monday (day 0 was a Thursday).
        long period = mode == Period.DAILY ? day : Math.floorDiv(day + 3, 7);
        Map<Identifier, ChallengeData> all = ChallengesManager.INSTANCE.getAllChallenges();
        Current current = cached;
        if (current != null && current.mode == mode && current.period == period && cachedChallenges == all.size() && all.get(current.id) == current.data) {
            return current;
        }
        List<Map.Entry<Identifier, ChallengeData>> candidates = new ArrayList<>();
        for (Map.Entry<Identifier, ChallengeData> entry : all.entrySet()) {
            ChallengeData data = entry.getValue();
            MiningSkillCardItem.Type type = data.forCardType();
            boolean action = switch (data.challengeType()) {
                case BREAK_BLOCK, STRIP_BLOCK, FLATTEN_BLOCK, TILLING_BLOCK -> true;
                default -> false;
            };
            if (!action || type == MiningSkillCardItem.Type.EMPTY) continue;
            // Types a data pack no longer has (or that were left out) have no tools to do it with.
            if (type.isData() && !MiningSkillCardItem.Type.getDataTypes().contains(type)) continue;
            if (ChallengesManager.INSTANCE.utilizeTargetedBlocks(data).isEmpty()) continue;
            candidates.add(entry);
        }
        cachedChallenges = all.size();
        if (candidates.isEmpty()) return cached = null;
        // The challenges come sorted by id, so the same day gives the same pick until the data packs change.
        Random random = new Random(period * 31L + mode.ordinal() ^ server.overworld().getSeed());
        Map.Entry<Identifier, ChallengeData> pick = candidates.get(random.nextInt(candidates.size()));
        int amount = Math.max(1, pick.getValue().requiredAmount().getSecond()) * (mode == Period.DAILY ? DAILY_FACTOR : WEEKLY_FACTOR);
        return cached = new Current(mode, period, pick.getKey(), pick.getValue(), amount,
                List.copyOf(ChallengesManager.INSTANCE.utilizeTargetedBlocks(pick.getValue())));
    }

    /** The player's progress in this challenge: an earlier day's (or another challenge's) counts as none. */
    public static PlayerAbilityData.Timed progress(ServerPlayer player, Current current) {
        PlayerAbilityData.Timed timed = ServicePlatform.get().players().getAbilityData(player).getTimed();
        return timed.period() == current.period && timed.challenge().equals(current.id.toString()) ? timed
                : new PlayerAbilityData.Timed(current.period, current.id.toString(), 0, false);
    }

    /** A block the player broke, stripped, flattened or tilled. */
    public static void onAction(ServerPlayer player, BlockState state, BlockPos pos, ChallengeData.Type action) {
        if (player.isCreative() || player.isSpectator() || Platform.isFakePlayer(player)) return;
        Current current = current(player.level().getServer());
        if (current == null || current.data.challengeType() != action || !current.targets.contains(state.getBlock())) return;
        if (!ChallengesManager.INSTANCE.isCorrectTool(player, current.data)) return;
        if (UAServerConfig.IS_PLACED_BY_ENTITY_CONDITION.get() && !state.is(ModBlockTags.DENY_IS_PLACED_BY_ENTITY)
                && IneligibleBlocksSavedData.getOrCreate(player.level()).isBlockPlacedByEntity(pos)) return;
        advance(player, current, 1);
    }

    /** Adds to the player's progress in the running challenge (the command's way in); false when it is already done. */
    public static boolean advance(ServerPlayer player, Current current, int amount) {
        PlayerAbilityData.Timed before = progress(player, current);
        if (before.done()) return false;
        int progress = Math.min(current.amount, before.progress() + amount);
        boolean done = progress >= current.amount;
        PlayerAbilityData.Timed now = new PlayerAbilityData.Timed(current.period, current.id.toString(), progress, done);
        ServicePlatform.get().players().modifyAbilityData(player, data -> data.setTimed(now));
        if (done) reward(player, current);
        // Started, and halfway there.
        else if (before.progress() == 0 || (before.progress() < current.amount / 2 && progress >= current.amount / 2)) describe(current, now).send(player);
        return true;
    }

    private static void reward(ServerPlayer player, Current current) {
        MiningSkillCardItem.Type type = current.data.forCardType();
        List<Component> lines = new ArrayList<>();
        int xp = UAServerConfig.TIMED_CHALLENGE_EXPERIENCE.get() * (current.mode == Period.WEEKLY ? 4 : 1);
        if (xp > 0) {
            player.giveExperiencePoints(xp);
            lines.add(Component.translatable("info.ultimine_addition.timed.reward.experience", xp).withStyle(ChatFormatting.GRAY));
        }
        int seconds = UAServerConfig.TIMED_CHALLENGE_JUICE.get() * (current.mode == Period.WEEKLY ? 4 : 1);
        if (seconds > 0 && MineGoJuiceEffect.holder(type).isPresent() && MineGoJuiceEffect.grant(player, type, seconds * 20, 0)) {
            lines.add(Component.translatable("info.ultimine_addition.timed.reward.juice", MineGoJuiceEffect.juiceName(type).withStyle(ChatFormatting.YELLOW), time(seconds))
                    .withStyle(ChatFormatting.GRAY));
        }
        DataRewards.onTimedChallenge(player, type, current.id);
        new UltimineNotice(UltimineNotice.Kind.REWARD, Component.translatable("info.ultimine_addition.timed.completed", current.title()), lines, type.iconStack()).send(player);
        player.level().sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.6, 0.8, 0.6, 0.1);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, player.getSoundSource(), 0.8F, 1.4F);
    }

    /** Tells the player about the running challenge (after joining, or when asked): what to do, how far they are. */
    public static boolean announce(ServerPlayer player, boolean evenWhenDone) {
        Current current = current(player.level().getServer());
        if (current == null) return false;
        PlayerAbilityData.Timed timed = progress(player, current);
        if (timed.done() && !evenWhenDone) return true;
        describe(current, timed).send(player);
        return true;
    }

    private static UltimineNotice describe(Current current, PlayerAbilityData.Timed timed) {
        List<Component> lines = new ArrayList<>(describeLines(current, timed));
        return new UltimineNotice(UltimineNotice.Kind.STREAK, current.title(), lines, current.data.forCardType().iconStack());
    }

    /** The challenge in words: what to do, with which tool, the progress, and when the next one comes. */
    public static List<Component> describeLines(Current current, PlayerAbilityData.Timed timed) {
        List<Component> lines = new ArrayList<>();
        List<Component> what = ChallengesManager.INSTANCE.createChallengeDescription(current.id, Style.EMPTY.withColor(ChatFormatting.GRAY));
        // Long lists of blocks are cut short: the notice is a few lines.
        for (int i = 0; i < Math.min(what.size(), 4); i++) lines.add(what.get(i));
        if (what.size() > 4) lines.add(Component.literal("…").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("info.ultimine_addition.timed.tool", current.data.forCardType().displayName().copy().withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GRAY));
        if (timed.done()) {
            lines.add(Component.translatable("info.ultimine_addition.timed.done").withStyle(ChatFormatting.GREEN));
        } else {
            lines.add(Component.translatable("info.ultimine_addition.timed.progress", timed.progress(), current.amount).withStyle(ChatFormatting.WHITE));
        }
        lines.add(Component.translatable("info.ultimine_addition.timed.resets", time(secondsLeft(current.mode))).withStyle(ChatFormatting.DARK_GRAY));
        return lines;
    }

    private static long secondsLeft(Period mode) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate next = mode == Period.WEEKLY ? now.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY)) : now.toLocalDate().plusDays(1);
        return Math.max(0L, Duration.between(now, next.atStartOfDay()).getSeconds());
    }

    private static String time(long seconds) {
        if (seconds >= 86400) return "%dd %dh".formatted(seconds / 86400, seconds % 86400 / 3600);
        if (seconds >= 3600) return "%dh %02dm".formatted(seconds / 3600, seconds % 3600 / 60);
        if (seconds >= 60) return "%dm %02ds".formatted(seconds / 60, seconds % 60);
        return seconds + "s";
    }
}
