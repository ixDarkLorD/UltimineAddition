package net.ixdarklord.ultimine_addition.mixin.fabric;

import dev.architectury.event.CompoundEventResult;
import net.ixdarklord.ultimine_addition.common.event.impl.BlockToolModificationEvent;
import net.ixdarklord.ultimine_addition.util.ToolActions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AxeItem.class)
abstract class MixinAxeItem {
    @Redirect(method = {"useOn"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean UA$Redirect$useOn(Level level, BlockPos pos, BlockState vanillaState, int flags, UseOnContext context) {
        CompoundEventResult<BlockState> result = BlockToolModificationEvent.EVENT.invoker().modify(level.getBlockState(pos), context, ToolActions.AXE_STRIP, false);
        BlockState modifiedState = result.object() != null ? result.object() : vanillaState;
        return level.setBlock(pos, modifiedState, flags);
    }
}
