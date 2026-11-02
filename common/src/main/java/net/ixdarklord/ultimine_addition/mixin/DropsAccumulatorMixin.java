package net.ixdarklord.ultimine_addition.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ftb.mods.ftblibrary.util.Lazy;
import dev.ftb.mods.ftbultimine.utils.ItemCollector;
import net.ixdarklord.ultimine_addition.common.progression.CardPerk;
import net.ixdarklord.ultimine_addition.common.undo.UltimineInProgress;
import net.ixdarklord.ultimine_addition.common.undo.UltimineUndo;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// FTB Ultimine 26.1.2.6 gathers an operation's drops in this private class of FTBUltimine: the collector each broken
// block's drops go to, and the merged drops it spawns at the origin when the operation ends.
@Mixin(targets = "dev.ftb.mods.ftbultimine.FTBUltimine$DropsAccumulator")
public abstract class DropsAccumulatorMixin {
    @Shadow @Final private Lazy<ItemCollector> tempBlockDropsList;

    @Inject(method = "start", at = @At("TAIL"))
    private void UA$start(CallbackInfo ci) {
        UltimineInProgress.collector = this.tempBlockDropsList.get();
    }

    // The merged drops, and the item entities FTB Ultimine spawns for them at the origin.
    @WrapOperation(method = "finish", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/utils/ItemCollector;drop(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void UA$undoRecordDrops(ItemCollector collector, Level level, BlockPos pos, Operation<Void> original) {
        List<ItemStack> drops = ((ItemCollectorAccessor) collector).ua$getItems().stream().map(ItemStack::copy).toList();
        AABB area = new AABB(pos).inflate(1.5);
        Set<ItemEntity> before = new HashSet<>(level.getEntitiesOfClass(ItemEntity.class, area));
        original.call(collector, level, pos);
        List<ItemEntity> spawned = level.getEntitiesOfClass(ItemEntity.class, area, entity -> !before.contains(entity));
        UltimineUndo.recordDrops(drops, spawned);
        // Straight to the player, where the server has that on: picked up as if walked over, so what doesn't fit stays
        // on the ground (and undo finds the rest in the inventory).
        ServerPlayer player = UltimineInProgress.player;
        if (player != null && !player.isSpectator() && (UAServerConfig.DROPS_TO_INVENTORY.get() || CardPerk.MAGNET.has(player))) {
            for (ItemEntity entity : spawned) {
                if (!entity.isAlive()) continue;
                entity.setNoPickUpDelay();
                entity.playerTouch(player);
            }
        }
    }
}
