package net.ixdarklord.ultimine_addition.core.fabric;

import net.ixdarklord.ultimine_addition.common.tag.PlatformTags;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class ServicePlatformPlayersImpl implements ServicePlatform.Players {


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
