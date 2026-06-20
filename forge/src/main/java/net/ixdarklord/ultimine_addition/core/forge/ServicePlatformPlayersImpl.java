package net.ixdarklord.ultimine_addition.core.forge;

import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.common.data.player.forge.PlayerUltimineCapabilityProvider;
import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.network.packets.PlayerAbilityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.List;

public final class ServicePlatformPlayersImpl implements ServicePlatform.Players {
   public boolean isPlayerUltimineCapable(Player player) {
      return player.getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).map(PlayerAbilityData::getAbility).orElse(false);
   }

   public void setPlayerUltimineCapability(Player player, boolean state) {
      player.getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).ifPresent((capability) -> capability.setAbility(state));
      if (player instanceof ServerPlayer serverPlayer) {
         PacketHandler.sendToPlayer(new PlayerAbilityPacket(ServicePlatform.get().players().isPlayerUltimineCapable(serverPlayer)), serverPlayer);
      }

   }

   public boolean isCorrectToolForBlock(ItemStack stack, BlockState blockState) {
      return stack.isCorrectToolForDrops(blockState);
   }

   public boolean isToolPaxel(ItemStack stack) {
      if (!stack.is(PlatformTags.get().PAXELS()) && !stack.is(PlatformTags.get().TOOLS_PAXELS())) {
         ToolAction PAXEL_DIG = ToolAction.get("paxel_dig");
         if (stack.canPerformAction(PAXEL_DIG)) {
            return true;
         } else {
            for (ToolAction action : List.of(ToolActions.PICKAXE_DIG, ToolActions.AXE_DIG, ToolActions.SHOVEL_DIG, ToolActions.HOE_DIG)) {
               if (!stack.canPerformAction(action)) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return true;
      }
   }

   public double getBlockReachAttribute(Player player) {
      return player.getBlockReach();
   }
}
