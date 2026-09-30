package net.ixdarklord.ultimine_addition.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// FTB Ultimine 2001's ShapeRegistry is static: the local player's shapes on the client thread, the enabled ones elsewhere
// (the server asks through FTBUltiminePlayerData, see FTBUltiminePlayerDataMixin).
@Mixin(value = ShapeRegistry.class, remap = false)
public abstract class ShapeRegistryMixin {
    @Inject(method = "getShape", at = @At(value = "HEAD"), cancellable = true)
    private static void UA$Inject$GetShape(int idx, CallbackInfoReturnable<Shape> cir) {
        cir.setReturnValue(FTBUltimineIntegration.getAvailableShape(FTBUltimineIntegration.getRegistryPlayer(), idx));
    }

    @ModifyReturnValue(method = "shapeCount", at = @At(value = "RETURN"))
    private static int UA$ModifyReturn$ShapeCount(int original) {
        return FTBUltimineIntegration.getAvailableShapes(FTBUltimineIntegration.getRegistryPlayer()).size();
    }
}
