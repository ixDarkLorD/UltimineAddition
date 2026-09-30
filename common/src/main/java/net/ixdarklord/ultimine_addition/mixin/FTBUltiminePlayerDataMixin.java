package net.ixdarklord.ultimine_addition.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import net.ixdarklord.coolcatcore.api.hooks.ServerLifecycleHooks;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(value = FTBUltiminePlayerData.class)
public abstract class FTBUltiminePlayerDataMixin {
    @Shadow private int shapeIndex;

    @Shadow @Final private UUID playerId;

    @ModifyReturnValue(method = "getCurrentShape", at = @At(value = "RETURN"), remap=false)
    private Shape UA$ModifyReturn$getCurrentShape(Shape original) {
        ServerPlayer player = this.UA$getPlayer();
        ShapeRegistry registry = ShapeRegistry.INSTANCE;
        if (player != null && FTBUltimineIntegration.hasToolWithShape(player)) {
            Shape toolShape = FTBUltimineIntegration.getToolShape(player);
            if (FTBUltimineIntegration.getAvailableShapes(player, registry).contains(toolShape)) return toolShape;
        }
        return FTBUltimineIntegration.getAvailableShape(player, registry, this.shapeIndex);
    }

    @Redirect(method = "cycleShape", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/shape/ShapeRegistry;shapeCount()I"), remap=false)
    public int UA$Redirect$cycleShape(ShapeRegistry instance) {
        return FTBUltimineIntegration.getAvailableShapes(this.UA$getPlayer(), instance).size();
    }

    @Unique
    private @Nullable ServerPlayer UA$getPlayer() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.getPlayerList().getPlayer(this.playerId);
    }
}
