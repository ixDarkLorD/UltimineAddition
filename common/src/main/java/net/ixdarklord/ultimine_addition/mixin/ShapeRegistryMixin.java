package net.ixdarklord.ultimine_addition.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.ShapeRegistryAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ShapeRegistry.class)
abstract class ShapeRegistryMixin implements ShapeRegistryAccessor {
    @Shadow @Final private List<Shape> shapesList;

    @Shadow private Shape defaultShape;

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public List<Shape> getShapesList() {
        return shapesList;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public Shape getDefaultShape() {
        return defaultShape;
    }

    @Inject(method = "getShape", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void UA$Inject$GetShape(int idx, CallbackInfoReturnable<Shape> cir) {
        cir.setReturnValue(FTBUltimineIntegration.getEnabledShapes(idx));
    }

    @ModifyReturnValue(method = "shapeCount", at = @At(value = "RETURN"), remap = false)
    private int UA$ModifyReturn$ShapeCount(int original) {
        return FTBUltimineIntegration.getEnabledShapes().size();
    }
}
