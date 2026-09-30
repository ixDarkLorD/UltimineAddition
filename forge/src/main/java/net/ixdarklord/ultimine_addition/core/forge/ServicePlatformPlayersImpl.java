package net.ixdarklord.ultimine_addition.core.forge;

import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.List;

public final class ServicePlatformPlayersImpl implements ServicePlatform.Players {

    @Override
    public boolean isCorrectToolForBlock(ItemStack stack, BlockState state) {
        return stack.isCorrectToolForDrops(state);
    }

    @Override
    public boolean isToolPaxel(ItemStack stack) {
        // Solution #1
        // Lookup for paxel tag
        if (stack.is(PlatformTags.get().PAXELS()) || stack.is(PlatformTags.get().TOOLS_PAXELS())) {
            return true;
        }

        // Solution #2
        // This is used by Mekanism
        final ToolAction PAXEL_DIG = ToolAction.get("paxel_dig");
        if (stack.canPerformAction(PAXEL_DIG)) {
            return true;
        }

        // Solution #3
        // Checking if the tool able to preform these actions
        List<ToolAction> ACTIONS = List.of(
                ToolActions.PICKAXE_DIG,
                ToolActions.AXE_DIG,
                ToolActions.SHOVEL_DIG,
                ToolActions.HOE_DIG
        );

        for (ToolAction action : ACTIONS) {
            if (!stack.canPerformAction(action)) {
                return false;
            }
        }

        return true;
    }

    // Forge's reach attribute.
    @Override
    public double getBlockReach(Player player) {
        return player.getBlockReach();
    }
}
