package net.ixdarklord.ultimine_addition.core.forge;

import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.common.data.player.forge.PlayerUltimineCapabilityProvider;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.common.network.packet.PlayerAbilityPacket;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("unused")
public class ServicePlatformPlayersImpl {
    public static boolean isPlayerUltimineCapable(Player player) {
        return player.getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).map(PlayerAbilityData::getAbility).orElse(false);
    }
    public static void setPlayerUltimineCapability(Player player, boolean state) {
        player.getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).ifPresent(capability -> capability.setAbility(state));
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToPlayer(new PlayerAbilityPacket(ServicePlatform.Players.isPlayerUltimineCapable(serverPlayer)), serverPlayer);
        }
    }
}
