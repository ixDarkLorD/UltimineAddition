package net.ixdarklord.ultimine_addition.core.neoforge;

import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.common.network.packet.PlayerAbilityPacket;
import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.List;

public class ServicePlatformPlayersImpl {
    public static boolean isPlayerUltimineCapable(Player player) {
        return player.hasData(NeoForgeSetup.PLAYER_ABILITY_DATA)
                && player.getData(NeoForgeSetup.PLAYER_ABILITY_DATA).getAbility();
    }

    public static void setPlayerUltimineCapability(Player player, boolean state) {
        PlayerAbilityData data = PlayerAbilityData.create();
        player.setData(NeoForgeSetup.PLAYER_ABILITY_DATA, data.setAbility(state));
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToPlayer(new PlayerAbilityPacket(data.getAbility()), serverPlayer);
        }
    }

    public static double getReachAttribute(Player player) {
        return player.isCreative() ? 5.0 : 4.5;
    }

    public static boolean isCorrectToolForBlock(ItemStack stack, BlockState state) {
        return stack.isCorrectToolForDrops(state);
    }

    public static boolean isToolPaxel(ItemStack stack) {
        // Solution #1
        // Lookup for paxel tag
        if (stack.is(PlatformTags.get().PAXELS()) || stack.is(PlatformTags.get().TOOLS_PAXELS())) {
            return true;
        }

        // Solution #2
        // This is used by Mekanism
        final ItemAbility PAXEL_DIG = ItemAbility.get("paxel_dig");
        if (stack.canPerformAction(PAXEL_DIG)) {
            return true;
        }

        // Solution #3
        // Checking if the tool able to preform these actions
        List<ItemAbility> ACTIONS = List.of(
                ItemAbilities.PICKAXE_DIG,
                ItemAbilities.AXE_DIG,
                ItemAbilities.SHOVEL_DIG,
                ItemAbilities.HOE_DIG
        );

        for (ItemAbility action : ACTIONS) {
            if (!stack.canPerformAction(action)) {
                return false;
            }
        }

        return true;
    }
}
