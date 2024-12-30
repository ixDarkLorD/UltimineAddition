package net.ixdarklord.ultimine_addition.mixin;

import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ShapeRegistry.class)
public interface ShapeRegistryAccessor {

    @Accessor("LIST")
    static List<Shape> getShapesList() {
        throw new AssertionError();
    }

    @Accessor("defaultShape")
    static Shape getDefaultShape() {
        throw new AssertionError();
    }

    @Accessor("defaultShape")
    static void setDefaultShape(Shape defaultShape) {
        throw new AssertionError();
    }
}
