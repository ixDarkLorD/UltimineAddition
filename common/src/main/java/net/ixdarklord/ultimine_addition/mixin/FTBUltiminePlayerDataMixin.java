package net.ixdarklord.ultimine_addition.mixin;

import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.shape.Shape;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FTBUltiminePlayerData.class)
abstract class FTBUltiminePlayerDataMixin {

    @Shadow private int shapeIndex;

    @Inject(method = "getCurrentShape", at = @At(value = "RETURN"), cancellable = true, remap=false)
    private void inject$getCurrentShape(CallbackInfoReturnable<Shape> cir) {
        cir.setReturnValue(FTBUltimineIntegration.getEnabledShapes(this.shapeIndex));
    }

    @Redirect(method = "cycleShape", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/shape/ShapeRegistry;shapeCount()I"), remap=false)
    public int redirect$cycleShape() {
        return FTBUltimineIntegration.getEnabledShapes().size();
    }


}
