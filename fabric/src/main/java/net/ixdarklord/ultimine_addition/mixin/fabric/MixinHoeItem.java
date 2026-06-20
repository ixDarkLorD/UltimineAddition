package net.ixdarklord.ultimine_addition.mixin.fabric;

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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(HoeItem.class)
public abstract class MixinHoeItem {

    @Inject(method = "changeIntoState", at = @At("RETURN"), cancellable = true)
    private static void UA$Redirect$changeIntoState(BlockState state, CallbackInfoReturnable<Consumer<UseOnContext>> cir) {
        cir.setReturnValue((context) -> {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            CompoundEventResult<BlockState> result = BlockToolModificationEvent.EVENT.invoker().modify(level.getBlockState(context.getClickedPos()), context, ToolActions.HOE_TILL, false);
            BlockState modified = result.object() != null ? result.object() : state;
            level.setBlock(pos, modified, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(context.getPlayer(), modified));
        });
    }

    @Inject(method = "changeIntoStateAndDropItem", at = @At("RETURN"), cancellable = true)
    private static void UA$Redirect$changeIntoStateAndDropItem(BlockState state, ItemLike itemToDrop, CallbackInfoReturnable<Consumer<UseOnContext>> cir) {
        cir.setReturnValue((context) -> {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            CompoundEventResult<BlockState> result = BlockToolModificationEvent.EVENT.invoker().modify(level.getBlockState(context.getClickedPos()), context, ToolActions.HOE_TILL, false);
            BlockState modified = result.object() != null ? result.object() : state;
            level.setBlock(pos, modified, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(context.getPlayer(), modified));
            Block.popResourceFromFace(level, pos, context.getClickedFace(), new ItemStack(itemToDrop));
        });
    }
}
