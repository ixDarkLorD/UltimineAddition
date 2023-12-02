package net.ixdarklord.ultimine_addition.core.fabric;

import net.ixdarklord.ultimine_addition.common.data.player.IPlayerData;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.common.network.packet.PlayerAbilityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("unused")
public class ServicePlatformPlayersImpl {
    public static boolean isPlayerUltimineCapable(Player player) {
        return ((IPlayerData) player).getUltimineData().getAbility();
    }
    public static void setPlayerUltimineCapability(Player player, boolean state) {
        PlayerAbilityData data = ((IPlayerData) player).getUltimineData().setAbility(state);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToPlayer(new PlayerAbilityPacket(data.getAbility()), serverPlayer);
        }
    }
}
