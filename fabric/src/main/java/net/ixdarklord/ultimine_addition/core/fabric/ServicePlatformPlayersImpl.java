package net.ixdarklord.ultimine_addition.core.fabric;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.ixdarklord.ultimine_addition.common.data.player.IPlayerData;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.common.network.packet.PlayerAbilityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

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
    public static double getReachAttribute(Player player) {
        double value = player.isCreative() ? 5.0 : 4.5;
        if (FabricLoader.getInstance().isModLoaded("reach-entity-attributes"))
            return ReachEntityAttributes.getReachDistance(player, value);
        return value;
    }
    public static boolean isCorrectToolForBlock(ItemStack stack, BlockState blockState) {
        return stack.isCorrectToolForDrops(blockState);
    }
}
