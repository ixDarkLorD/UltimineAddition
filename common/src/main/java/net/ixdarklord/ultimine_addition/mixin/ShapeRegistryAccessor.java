package net.ixdarklord.ultimine_addition.mixin;

import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Internal
@Mixin(ShapeRegistry.class)
public interface ShapeRegistryAccessor {
    @Accessor("LIST")
    static List<Shape> getShapesList$UA() {
        throw new UnsupportedOperationException();
    }

    @Accessor("defaultShape")
    static Shape getDefaultShape$UA() {
        throw new UnsupportedOperationException();
    }
}
