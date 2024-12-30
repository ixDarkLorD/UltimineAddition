package net.ixdarklord.ultimine_addition.mixin;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.mixin.AxeItemAccess;
import dev.ftb.mods.ftbultimine.mixin.ShovelItemAccess;
import net.ixdarklord.ultimine_addition.common.event.ChallengeEvents;
import net.ixdarklord.ultimine_addition.common.event.impl.ToolAction;
import net.ixdarklord.ultimine_addition.common.event.impl.ToolActions;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = FTBUltimine.class)
abstract class FTBUltimineMixin {

    @Redirect(method = "blockBroken", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/config/FTBUltimineServerConfig;getMaxBlocks(Lnet/minecraft/server/level/ServerPlayer;)I"))
    private int redirect$MaxBlocks$1(ServerPlayer player) {
        return FTBUltimineIntegration.getMaxBlocks(player);
    }
    @Redirect(method = "blockRightClick", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/config/FTBUltimineServerConfig;getMaxBlocks(Lnet/minecraft/server/level/ServerPlayer;)I"))
    private int redirect$MaxBlocks$2(ServerPlayer player) {
        return FTBUltimineIntegration.getMaxBlocks(player);
    }
    @Redirect(method = "playerTick", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/config/FTBUltimineServerConfig;getMaxBlocks(Lnet/minecraft/server/level/ServerPlayer;)I"))
    private int redirect$MaxBlocks$3(ServerPlayer player) {
        return FTBUltimineIntegration.getMaxBlocks(player);
    }

    @Inject(method = "blockRightClick", at = @At(value = "HEAD"))
    private void redirect$RightClickEvent(Player pl, InteractionHand hand, BlockPos clickPos, Direction face, CallbackInfoReturnable<EventResult> cir) {
        if (pl instanceof ServerPlayer player) {
            ItemStack stack = player.getItemInHand(hand);
            Level level = player.level();
            BlockState originalState = level.getBlockState(clickPos);
            BlockState finalState = originalState;
            HitResult result = FTBUltiminePlayerData.rayTrace(player);
            if (result instanceof BlockHitResult hitResult) {
                UseOnContext context = new UseOnContext(player, hand, hitResult);
                ToolAction toolAction = null;
                if (stack.getItem() instanceof AxeItem item && ((AxeItemAccess) item).invokeGetStripped(originalState).isPresent()) {
                    finalState = ((AxeItemAccess) item).invokeGetStripped(originalState).get();
                    toolAction = ToolActions.AXE_STRIP;
                }
                //noinspection ConstantValue
                if (stack.getItem() instanceof ShovelItem && ShovelItemAccess.getFlattenables().get(originalState.getBlock()) != null) {
                    finalState = ShovelItemAccess.getFlattenables().get(originalState.getBlock());
                    toolAction = ToolActions.SHOVEL_FLATTEN;
                }
                if (stack.getItem() instanceof HoeItem) {
                    /* TODO: I need to find a way to get the other BlockState from the HoeItem#TILLABLES */
                    Block block = originalState.getBlock();
                    if ((block == Blocks.GRASS_BLOCK
                            || block == Blocks.DIRT_PATH
                            || block == Blocks.DIRT
                            || block == Blocks.COARSE_DIRT)
                            && context.getLevel().getBlockState(context.getClickedPos().above()).isAir()) {
                        finalState = block == Blocks.COARSE_DIRT ? Blocks.DIRT.defaultBlockState() : Blocks.FARMLAND.defaultBlockState();
                    }
                    toolAction = ToolActions.HOE_TILL;
                }

                FTBUltiminePlayerData playerData = FTBUltimine.instance.getOrCreatePlayerData(player);
                playerData.clearCache();
                playerData.updateBlocks(player, context.getClickedPos(), context.getClickedFace(), false, FTBUltimineIntegration.getMaxBlocks((ServerPlayer) player));
                List<BlockPos> blockPosList = new ArrayList<>();
                if (playerData.isPressed() && playerData.cachedPositions() != null && !playerData.cachedPositions().isEmpty()) {
                    blockPosList.addAll(playerData.cachedPositions().stream().filter(pos -> context.getLevel().getBlockState(pos).is(originalState.getBlock())).toList());
                }

                for (BlockPos pos : blockPosList) {
                    CompoundEventResult<BlockState> eventResult = ChallengeEvents.onBlockToolModificationEvent(originalState, finalState, context, toolAction, false);
                    if (eventResult.isPresent() && eventResult.object() != null) {
                        level.setBlockAndUpdate(pos, eventResult.object());
                    }
                }
            }
        }
    }
}
