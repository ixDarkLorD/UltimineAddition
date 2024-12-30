package net.ixdarklord.ultimine_addition.mixin.client;

import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import dev.ftb.mods.ftbultimine.shape.Shape;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FTBUltimineClient.class)
public class FTBUltimineClientMixin {

    @Redirect(method = "addPressedInfo", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/shape/ShapeRegistry;getShape(I)Ldev/ftb/mods/ftbultimine/shape/Shape;"), remap=false)
    private Shape redirect$addPressedInfo1(int idx) {
        return FTBUltimineIntegration.getEnabledShapes(idx);
    }

    @Redirect(method = "addPressedInfo", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbultimine/shape/ShapeRegistry;shapeCount()I"), remap=false)
    private int redirect$addPressedInfo2() {
        return FTBUltimineIntegration.getEnabledShapes().size();
    }
}
