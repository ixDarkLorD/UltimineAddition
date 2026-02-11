package net.ixdarklord.ultimine_addition.mixin;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.IntSupplier;

@Mixin(value = FTBUltimine.class)
abstract class FTBUltimineMixin {

    @Redirect(method = "blockBroken", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/config/FTBUltimineServerConfig;getMaxBlocks(Lnet/minecraft/server/level/ServerPlayer;)I"))
    private int UA$Redirect$blockBroken$1(ServerPlayer player) {
        return FTBUltimineIntegration.getMaxBlocks(player);
    }

    @Redirect(method = "blockRightClick", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/config/FTBUltimineServerConfig;getMaxBlocks(Lnet/minecraft/server/level/ServerPlayer;)I"))
    private int UA$Redirect$blockBroken$2(ServerPlayer player) {
        return FTBUltimineIntegration.getMaxBlocks(player);
    }

    @ModifyArgs(method = "playerTick", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/FTBUltiminePlayerData;checkBlocks(Lnet/minecraft/server/level/ServerPlayer;ZLjava/util/function/IntSupplier;)V"))
    private void UA$Redirect$blockBroken$3(Args args) {
        args.set(2, ((IntSupplier) () -> FTBUltimineIntegration.getMaxBlocks(args.get(0))));
    }
}
