package net.ixdarklord.ultimine_addition.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapeRegistry.class)
abstract class ShapeRegistryMixin {
    @Inject(method = "getShape", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void UA$Inject$GetShape(int idx, CallbackInfoReturnable<Shape> cir) {
        cir.setReturnValue(FTBUltimineIntegration.getEnabledShapes(idx));
    }

    @ModifyReturnValue(method = "shapeCount", at = @At(value = "RETURN"), remap = false)
    private static int UA$ModifyReturn$ShapeCount(int original) {
        return FTBUltimineIntegration.getEnabledShapes().size();
    }
}
