package net.ixdarklord.ultimine_addition.mixin;

import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapeRegistry.class)
public abstract class MixinShapeRegistry {
    @Inject(method = "getShape", at = @At("HEAD"), remap = false, cancellable = true)
    private static void UA$Inject$GetShape(int idx, CallbackInfoReturnable<Shape> cir) {
        cir.setReturnValue(FTBUltimineIntegration.getEnabledShapes(idx));
    }

    @Inject(method = "shapeCount", at = @At("HEAD"), remap = false, cancellable = true)
    private static void UA$Inject$ShapeCount(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(FTBUltimineIntegration.getEnabledShapes().size());
    }
}
