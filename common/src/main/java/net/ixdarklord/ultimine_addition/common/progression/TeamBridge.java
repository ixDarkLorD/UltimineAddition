package net.ixdarklord.ultimine_addition.common.progression;

import net.ixdarklord.coolcatcore.api.platform.Platform;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.integration.ftbteams.FTBTeamsCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;

import java.util.ArrayList;
import java.util.List;

// Who a player shares progress with: their FTB Teams party when that mod is there, otherwise their scoreboard team
// (/team). Only the teammates who are online; the player is not among them.
public final class TeamBridge {
    private static boolean ftbTeamsFailed;

    private TeamBridge() {}

    public static List<ServerPlayer> onlineTeammates(ServerPlayer player) {
        List<ServerPlayer> mates = new ArrayList<>();
        if (!ftbTeamsFailed && Platform.isModLoaded("ftbteams")) {
            try {
                mates.addAll(FTBTeamsCompat.onlineTeammates(player));
            } catch (Throwable e) {
                // Another FTB Teams than the one this was built against: fall back to scoreboard teams for good.
                ftbTeamsFailed = true;
                FTBUltimineAddition.LOGGER.warn("FTB Teams could not be asked for teams ({}): using scoreboard teams", e.toString());
            }
        }
        if (mates.isEmpty()) {
            Team team = player.getTeam();
            if (team != null) {
                for (ServerPlayer other : player.level().getServer().getPlayerList().getPlayers()) {
                    if (other.getTeam() == team) mates.add(other);
                }
            }
        }
        mates.remove(player);
        return mates;
    }
}
