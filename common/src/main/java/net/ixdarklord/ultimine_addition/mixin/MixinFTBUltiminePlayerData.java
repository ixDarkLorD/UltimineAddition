package net.ixdarklord.ultimine_addition.mixin;

import dev.architectury.utils.GameInstance;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.shape.Shape;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(FTBUltiminePlayerData.class)
public abstract class MixinFTBUltiminePlayerData {

    @Shadow(remap = false)
    @Final
    private UUID playerId;

    @Inject(method = "getCurrentShape", at = @At("HEAD"), remap = false, cancellable = true)
    private void UA$ModifyReturn$getCurrentShape(CallbackInfoReturnable<Shape> cir) {
        MinecraftServer server = GameInstance.getServer();
        assert server != null;
        ServerPlayer player = server.getPlayerList().getPlayer(this.playerId);
        assert player != null;

        if (FTBUltimineIntegration.hasToolWithShape(player)) {
            cir.setReturnValue(FTBUltimineIntegration.getToolShape(player));
        }

    }

    @Redirect(method = "cycleShape", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/shape/ShapeRegistry;shapeCount()I"), remap = false)
    public int UA$Redirect$cycleShape() {
        return FTBUltimineIntegration.getEnabledShapes().size();
    }
}
