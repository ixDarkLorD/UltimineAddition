package net.ixdarklord.ultimine_addition.common.progression;

import net.ixdarklord.ultimine_addition.common.advancement.MilestoneTrigger;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData.Stats;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;

// What players have done with Ultimine: blocks mined, the largest operation, undos, daily challenges, card challenges.
// Each player's numbers are kept with their ability data; the leaderboard is of the players seen since the server
// started (it has no file of its own).
public final class UltimineStats {
    public enum Board {
        BLOCKS(Stats::blocks), LARGEST(Stats::largest), CHALLENGES(Stats::challenges), DAILIES(Stats::dailies), UNDOS(Stats::undos);

        private final ToLongFunction<Stats> value;

        Board(ToLongFunction<Stats> value) {
            this.value = value;
        }

        public long of(Stats stats) {
            return this.value.applyAsLong(stats);
        }
    }

    public record Row(String name, Stats stats) {}

    private static final Map<UUID, Row> SEEN = new HashMap<>();

    private UltimineStats() {}

    public static Stats of(ServerPlayer player) {
        return ServicePlatform.get().players().getAbilityData(player).getStats();
    }

    /** Puts a player on the leaderboard (when they join). */
    public static void see(ServerPlayer player) {
        SEEN.put(player.getUUID(), new Row(player.getScoreboardName(), of(player)));
    }

    public static void clear() {
        SEEN.clear();
    }

    private static void change(ServerPlayer player, UnaryOperator<Stats> change) {
        Stats now = change.apply(of(player));
        ServicePlatform.get().players().modifyAbilityData(player, (PlayerAbilityData data) -> data.setStats(now));
        SEEN.put(player.getUUID(), new Row(player.getScoreboardName(), now));
    }

    public static void onUltimine(ServerPlayer player, int blocks) {
        if (blocks <= 0 || player.isCreative()) return;
        change(player, s -> new Stats(s.blocks() + blocks, s.operations() + 1, Math.max(s.largest(), blocks), s.undos(), s.dailies(), s.challenges()));
        if (blocks >= 64) MilestoneTrigger.reach(player, MilestoneTrigger.BIG_ULTIMINE);
        if (of(player).blocks() >= 1000) MilestoneTrigger.reach(player, MilestoneTrigger.THOUSAND_BLOCKS);
    }

    public static void onUndo(ServerPlayer player) {
        change(player, s -> new Stats(s.blocks(), s.operations(), s.largest(), s.undos() + 1, s.dailies(), s.challenges()));
        MilestoneTrigger.reach(player, MilestoneTrigger.UNDO);
    }

    public static void onDaily(ServerPlayer player) {
        change(player, s -> new Stats(s.blocks(), s.operations(), s.largest(), s.undos(), s.dailies() + 1, s.challenges()));
        MilestoneTrigger.reach(player, MilestoneTrigger.DAILY);
    }

    public static void onChallenge(ServerPlayer player) {
        change(player, s -> new Stats(s.blocks(), s.operations(), s.largest(), s.undos(), s.dailies(), s.challenges() + 1));
    }

    /** The best of this board, highest first, leaving out those with nothing. */
    public static List<Row> top(Board board, int count) {
        List<Row> rows = new ArrayList<>(SEEN.values());
        rows.removeIf(row -> board.of(row.stats()) <= 0);
        rows.sort(Comparator.comparingLong((Row row) -> board.of(row.stats())).reversed().thenComparing(Row::name));
        return rows.subList(0, Math.min(count, rows.size()));
    }
}
