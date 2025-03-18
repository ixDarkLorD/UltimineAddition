package net.ixdarklord.ultimine_addition.core.fabric;

import net.ixdarklord.ultimine_addition.common.data.player.IPlayerData;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.common.network.packet.PlayerAbilityPacket;
import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class ServicePlatformPlayersImpl implements ServicePlatform.Players {

    @Override
    public boolean isPlayerUltimineCapable(Player player) {
        return ((IPlayerData) player).ua$getUltimineData().getAbility();
    }

    @Override
    public void setPlayerUltimineCapability(Player player, boolean state) {
        PlayerAbilityData data = ((IPlayerData) player).ua$getUltimineData().setAbility(state);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToPlayer(new PlayerAbilityPacket(data.getAbility()), serverPlayer);
        }
    }

    @Override
    @SuppressWarnings("CommentedOutCode")
    public double getReachAttribute(Player player) {
        /*if (FabricLoader.getInstance().isModLoaded("reach-entity-attributes")) {
            return ReachEntityAttributes.getReachDistance(player, value);
        }*/
        return player.isCreative() ? 5.0 : 4.5;
    }

    @Override
    public boolean isCorrectToolForBlock(ItemStack stack, BlockState blockState) {
        return stack.isCorrectToolForDrops(blockState);
    }

    @Override
    public boolean isToolPaxel(ItemStack stack) {
        // Solution #1
        // Lookup for paxel tag
        if (stack.is(PlatformTags.get().PAXELS()) || stack.is(PlatformTags.get().TOOLS_PAXELS())) {
            return true;
        }

        // Solution #2
        // Checking if the tool is correct for these blocks
        return isCorrectToolForBlock(stack, Blocks.STONE.defaultBlockState()) &&
                isCorrectToolForBlock(stack, Blocks.NOTE_BLOCK.defaultBlockState()) &&
                isCorrectToolForBlock(stack, Blocks.DIRT.defaultBlockState()) &&
                isCorrectToolForBlock(stack, Blocks.SPONGE.defaultBlockState());
    }
}
