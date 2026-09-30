package net.ixdarklord.ultimine_addition.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.ItemCollection;
import net.ixdarklord.ultimine_addition.common.undo.UltimineUndo;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;

// FTB Ultimine 2001: the break handler is blockBroken (an Architectury block-break listener). It breaks each block with
// the player's ServerPlayerGameMode.destroyBlock, and collects the drops of an operation in a fresh ItemCollection.
// (The injection targets name Minecraft classes, so they're remapped through the refmap; only the shadow isn't.)
@Mixin(value = FTBUltimine.class)
public abstract class FTBUltimineMixin {
    @Shadow(remap = false) private ItemCollection tempBlockDropsList;

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

    // FTB's blockBroken takes and returns Architectury types (IntValue, EventResult); only the arguments used are
    // captured here, so this mod doesn't refer to Architectury itself.
    @Inject(method = "blockBroken", at = @At(value = "NEW", target = "dev/ftb/mods/ftbultimine/ItemCollection"))
    private void UA$undoBegin(CallbackInfoReturnable<?> cir, @Local(argsOnly = true) ServerPlayer player, @Local(argsOnly = true) BlockPos origPos,
                              @Share("undoRan") LocalBooleanRef ran) {
        ran.set(true);
        UltimineUndo.begin(player, origPos);
    }

    @WrapOperation(method = "blockBroken", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean UA$undoRecordBlock(ServerPlayerGameMode gameMode, BlockPos pos, Operation<Boolean> original, @Local(argsOnly = true) ServerPlayer player) {
        BlockState before = player.level().getBlockState(pos);
        // A container's contents, and the drops this one break adds to FTB's collection, so undo can leave them out.
        List<ItemStack> contents = new ArrayList<>();
        if (player.level().getBlockEntity(pos) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (!container.getItem(i).isEmpty()) contents.add(container.getItem(i).copy());
            }
        }
        List<ItemStack> collected = ((ItemCollectorAccessor) this.tempBlockDropsList).ua$getItems();
        int collectedBefore = collected.size();
        BlockState[] neighbours = UltimineUndo.neighbours(player.serverLevel(), pos);
        boolean result = original.call(gameMode, pos);
        List<ItemStack> blockDrops = collected.size() > collectedBefore ? List.copyOf(collected.subList(collectedBefore, collected.size())) : List.of();
        UltimineUndo.recordBlock(player, pos, before, neighbours, contents, blockDrops);
        return result;
    }

    // The merged drops, and the item entities FTB Ultimine spawns for them at the origin.
    @WrapOperation(method = "blockBroken", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/ItemCollection;drop(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void UA$undoRecordDrops(ItemCollection collector, Level level, BlockPos pos, Operation<Void> original) {
        List<ItemStack> drops = ((ItemCollectorAccessor) collector).ua$getItems().stream().map(ItemStack::copy).toList();
        AABB area = new AABB(pos).inflate(1.5);
        Set<ItemEntity> before = new HashSet<>(level.getEntitiesOfClass(ItemEntity.class, area));
        original.call(collector, level, pos);
        List<ItemEntity> spawned = level.getEntitiesOfClass(ItemEntity.class, area, entity -> !before.contains(entity));
        UltimineUndo.recordDrops(drops, spawned);
    }

    // Whether an operation ran: FTB returns pass() on every check before building its drop collector, and
    // interruptFalse() once it has (where undoBegin marks this call). The nested block-break events an operation
    // causes are calls of their own, unmarked.
    @Inject(method = "blockBroken", at = @At("RETURN"))
    private void UA$undoFinish(CallbackInfoReturnable<?> cir, @Local(argsOnly = true) ServerPlayer player, @Share("undoRan") LocalBooleanRef ran) {
        UltimineUndo.finish(player, ran.get());
    }
}
