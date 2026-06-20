package net.ixdarklord.ultimine_addition.mixin;

import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.RightClickHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RightClickHandlers.class)
public abstract class MixinRightClickHandlers {
    @Inject(method = "axeStripping", at = @At("HEAD"), cancellable = true)
    private static void UA$Inject$axeStripping(ServerPlayer player, InteractionHand hand, BlockPos clickPos, FTBUltiminePlayerData data, CallbackInfoReturnable<Integer> cir) {
        int didWork = 0;

        for (BlockPos pos : data.cachedPositions()) {
            BlockHitResult hitResult = new BlockHitResult(Vec3.atBottomCenterOf(pos.above()), Direction.UP, pos, false);
            if (player.getMainHandItem().useOn(new UseOnContext(player, hand, hitResult)).consumesAction()) {
                ++didWork;
            }
        }

        cir.setReturnValue(didWork);
    }

    @Inject(method = "shovelFlattening", at = @At("HEAD"), cancellable = true)
    private static void UA$Inject$shovelFlattening(ServerPlayer player, InteractionHand hand, BlockPos clickPos, FTBUltiminePlayerData data, CallbackInfoReturnable<Integer> cir) {
        int didWork = 0;

        for (BlockPos pos : data.cachedPositions()) {
            BlockHitResult hitResult = new BlockHitResult(Vec3.atBottomCenterOf(pos.above()), Direction.UP, pos, false);
            if (player.getMainHandItem().useOn(new UseOnContext(player, hand, hitResult)).consumesAction()) {
                ++didWork;
            }
        }

        cir.setReturnValue(didWork);
    }

    @Inject(method = "farmlandConversion", at = @At("HEAD"), cancellable = true)
    private static void UA$Inject$farmlandConversion(ServerPlayer player, InteractionHand hand, BlockPos clickPos, FTBUltiminePlayerData data, CallbackInfoReturnable<Integer> cir) {
        int didWork = 0;

        for (BlockPos pos : data.cachedPositions()) {
            BlockHitResult hitResult = new BlockHitResult(Vec3.atBottomCenterOf(pos.above()), Direction.UP, pos, false);
            if (player.getMainHandItem().useOn(new UseOnContext(player, hand, hitResult)).consumesAction()) {
                ++didWork;
            }
        }

        cir.setReturnValue(didWork);
    }
}
