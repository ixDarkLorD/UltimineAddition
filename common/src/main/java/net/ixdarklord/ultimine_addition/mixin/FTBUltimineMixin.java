package net.ixdarklord.ultimine_addition.mixin;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.IntSupplier;

@Mixin(value = FTBUltimine.class)
public abstract class FTBUltimineMixin {

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
}
