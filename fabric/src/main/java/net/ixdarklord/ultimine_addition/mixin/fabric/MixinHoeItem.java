package net.ixdarklord.ultimine_addition.mixin.fabric;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.architectury.event.CompoundEventResult;
import net.ixdarklord.ultimine_addition.common.event.impl.BlockToolModificationEvent;
import net.ixdarklord.ultimine_addition.util.ToolActions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(HoeItem.class)
abstract class MixinHoeItem {
    @ModifyReturnValue(method = "changeIntoState", at = @At("RETURN"))
    private static Consumer<UseOnContext> UA$Redirect$changeIntoState(Consumer<UseOnContext> original, BlockState state) {
        return (context) -> {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            CompoundEventResult<BlockState> result = BlockToolModificationEvent.EVENT.invoker().modify(level.getBlockState(context.getClickedPos()), context, ToolActions.HOE_TILL, false);
            BlockState modified = result.object() != null ? result.object() : state;
            level.setBlock(pos, modified, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, Context.of(context.getPlayer(), modified));
        };
    }

    @ModifyReturnValue(method = "changeIntoStateAndDropItem", at = @At("RETURN"))
    private static Consumer<UseOnContext> UA$Redirect$changeIntoStateAndDropItem(Consumer<UseOnContext> original, BlockState state, ItemLike itemToDrop) {
        return (context) -> {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            CompoundEventResult<BlockState> result = BlockToolModificationEvent.EVENT.invoker().modify(level.getBlockState(context.getClickedPos()), context, ToolActions.HOE_TILL, false);
            BlockState modified = result.object() != null ? result.object() : state;
            level.setBlock(pos, modified, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, Context.of(context.getPlayer(), modified));
            Block.popResourceFromFace(level, pos, context.getClickedFace(), new ItemStack(itemToDrop));
        };
    }
}
