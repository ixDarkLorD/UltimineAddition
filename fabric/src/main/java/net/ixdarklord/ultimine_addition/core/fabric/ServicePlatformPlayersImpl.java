package net.ixdarklord.ultimine_addition.core.fabric;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.network.packets.PlayerAbilityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class ServicePlatformPlayersImpl implements ServicePlatform.Players {
    public boolean isPlayerUltimineCapable(Player player) {
        return player.getUltimineData$UA().getAbility();
    }

    public void setPlayerUltimineCapability(Player player, boolean state) {
        PlayerAbilityData data = player.getUltimineData$UA().setAbility(state);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToPlayer(new PlayerAbilityPacket(data.getAbility()), serverPlayer);
        }

    }

    public boolean isCorrectToolForBlock(ItemStack stack, BlockState blockState) {
        return stack.isCorrectToolForDrops(blockState);
    }

    public boolean isToolPaxel(ItemStack stack) {
        if (!stack.is(PlatformTags.get().PAXELS()) && !stack.is(PlatformTags.get().TOOLS_PAXELS())) {
            return this.isCorrectToolForBlock(stack, Blocks.STONE.defaultBlockState()) && this.isCorrectToolForBlock(stack, Blocks.NOTE_BLOCK.defaultBlockState()) && this.isCorrectToolForBlock(stack, Blocks.DIRT.defaultBlockState()) && this.isCorrectToolForBlock(stack, Blocks.SPONGE.defaultBlockState());
        } else {
            return true;
        }
    }

    public double getBlockReachAttribute(Player player) {
        double value = player.isCreative() ? (double) 5.0F : (double) 4.5F;
        return FabricLoader.getInstance().isModLoaded("reach-entity-attributes") ? ReachEntityAttributes.getReachDistance(player, value) : value;
    }
}
