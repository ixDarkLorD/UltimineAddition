package net.ixdarklord.ultimine_addition.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.shape.Shape;
import net.ixdarklord.coolcatlib.api.hooks.ServerLifecycleHooks;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(value = FTBUltiminePlayerData.class)
abstract class FTBUltiminePlayerDataMixin {
    @Shadow private int shapeIndex;

    @Shadow @Final private UUID playerId;

    @ModifyReturnValue(method = "getCurrentShape", at = @At(value = "RETURN"), remap=false)
    private Shape UA$ModifyReturn$getCurrentShape(Shape original) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player != null && FTBUltimineIntegration.hasToolWithShape(player)) {
                return FTBUltimineIntegration.getToolShape(player);
            }
        }
        return FTBUltimineIntegration.getEnabledShapes(this.shapeIndex);
    }

    @Redirect(method = "cycleShape", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/shape/ShapeRegistry;shapeCount()I"), remap=false)
    public int UA$Redirect$cycleShape() {
        return FTBUltimineIntegration.getEnabledShapes().size();
    }


}
