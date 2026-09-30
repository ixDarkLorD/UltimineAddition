package net.ixdarklord.ultimine_addition.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.architectury.event.EventResult;
import dev.architectury.utils.value.IntValue;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import dev.ftb.mods.ftbultimine.api.util.ItemCollector;
import net.ixdarklord.ultimine_addition.common.undo.UltimineUndo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Shadow;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.IntSupplier;

// FTB Ultimine 2101: the break handler is blockBroken (an Architectury block-break listener), and each operation
// collects its drops in a fresh ItemCollector.
@Mixin(value = FTBUltimine.class)
public abstract class FTBUltimineMixin {
    @Shadow private ItemCollector tempBlockDropsList;

    @Redirect(method = "blockBroken", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/config/FTBUltimineServerConfig;getMaxBlocks(Lnet/minecraft/server/level/ServerPlayer;)I"))
    private int UA$Redirect$blockBroken(ServerPlayer player) {
        return FTBUltimineIntegration.getMaxBlocks(player);
    }

    @Redirect(method = "blockRightClick", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/config/FTBUltimineServerConfig;getMaxBlocks(Lnet/minecraft/server/level/ServerPlayer;)I"))
    private int UA$Redirect$blockRightClick(ServerPlayer player) {
        return FTBUltimineIntegration.getMaxBlocks(player);
    }

    @Redirect(method = "playerTick", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/FTBUltiminePlayerData;checkBlocks(Lnet/minecraft/server/level/ServerPlayer;ZLjava/util/function/IntSupplier;)V"))
    private void UA$Redirect$playerTick(FTBUltiminePlayerData data, ServerPlayer player, boolean sendUpdate, IntSupplier maxBlocks) {
        data.checkBlocks(player, sendUpdate, () -> FTBUltimineIntegration.getMaxBlocks(player));
    }

    // --- Undo recording (see UltimineUndo) ---

    @Inject(method = "blockBroken", at = @At(value = "NEW", target = "dev/ftb/mods/ftbultimine/api/util/ItemCollector"))
    private void UA$undoBegin(Level level, BlockPos origPos, BlockState state, ServerPlayer player, IntValue xp, CallbackInfoReturnable<EventResult> cir) {
        UltimineUndo.begin(player, origPos);
    }

    @WrapOperation(method = "blockBroken", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/FTBUltimine;tryBreakBlock(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Ldev/ftb/mods/ftbultimine/api/shape/Shape;Lnet/minecraft/world/phys/BlockHitResult;)Z"))
    private boolean UA$undoRecordBlock(ServerPlayer player, BlockPos pos, BlockState state, Shape shape, BlockHitResult hit, Operation<Boolean> original) {
        BlockState before = player.level().getBlockState(pos);
        // A container's contents, and the drops this one break adds to FTB's collector, so undo can leave them out.
        List<ItemStack> contents = new ArrayList<>();
        if (player.level().getBlockEntity(pos) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (!container.getItem(i).isEmpty()) contents.add(container.getItem(i).copy());
            }
        }
        List<ItemStack> collected = ((ItemCollectorAccessor) this.tempBlockDropsList).ua$getItems();
        int collectedBefore = collected.size();
        BlockState[] neighbours = UltimineUndo.neighbours(player.serverLevel(), pos);
        boolean result = original.call(player, pos, state, shape, hit);
        List<ItemStack> blockDrops = collected.size() > collectedBefore ? List.copyOf(collected.subList(collectedBefore, collected.size())) : List.of();
        UltimineUndo.recordBlock(player, pos, before, neighbours, contents, blockDrops);
        return result;
    }

    // The merged drops, and the item entities FTB Ultimine spawns for them at the origin.
    @WrapOperation(method = "blockBroken", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/api/util/ItemCollector;drop(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void UA$undoRecordDrops(ItemCollector collector, Level level, BlockPos pos, Operation<Void> original) {
        List<ItemStack> drops = ((ItemCollectorAccessor) collector).ua$getItems().stream().map(ItemStack::copy).toList();
        AABB area = new AABB(pos).inflate(1.5);
        Set<ItemEntity> before = new HashSet<>(level.getEntitiesOfClass(ItemEntity.class, area));
        original.call(collector, level, pos);
        List<ItemEntity> spawned = level.getEntitiesOfClass(ItemEntity.class, area, entity -> !before.contains(entity));
        UltimineUndo.recordDrops(drops, spawned);
    }

    // An operation that ran returns interruptFalse(); the nested block-break events it causes return pass().
    @Inject(method = "blockBroken", at = @At("RETURN"))
    private void UA$undoFinish(Level level, BlockPos origPos, BlockState state, ServerPlayer player, IntValue xp, CallbackInfoReturnable<EventResult> cir) {
        UltimineUndo.finish(player, cir.getReturnValue().interruptsFurtherEvaluation());
    }
}
