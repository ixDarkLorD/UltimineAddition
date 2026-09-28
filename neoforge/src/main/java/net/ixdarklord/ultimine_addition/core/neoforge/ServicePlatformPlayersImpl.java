package net.ixdarklord.ultimine_addition.core.neoforge;

import net.minecraft.world.level.block.Blocks;
import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

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
        final ItemAbility PAXEL_DIG = ItemAbility.get("paxel_dig");
        if (stack.canPerformAction(PAXEL_DIG)) {
            return true;
        }

        // Solution #3
        // Checking if the tool is correct for these blocks (the per-tool dig abilities no longer exist)
        return isCorrectToolForBlock(stack, Blocks.STONE.defaultBlockState()) &&
                isCorrectToolForBlock(stack, Blocks.NOTE_BLOCK.defaultBlockState()) &&
                isCorrectToolForBlock(stack, Blocks.DIRT.defaultBlockState()) &&
                isCorrectToolForBlock(stack, Blocks.SPONGE.defaultBlockState());
    }
}
