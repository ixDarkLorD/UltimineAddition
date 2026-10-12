package net.ixdarklord.ultimine_addition.integration.ftbteams;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

// Only loaded when FTB Teams is there (see TeamBridge): a player's party, as the players of it who are online.
public final class FTBTeamsCompat {
    private FTBTeamsCompat() {}

    public static List<ServerPlayer> onlineTeammates(ServerPlayer player) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayer(player)
                .filter(team -> team.isPartyTeam())
                .map(team -> List.copyOf(team.getOnlineMembers()))
                .orElse(List.of());
    }
}
