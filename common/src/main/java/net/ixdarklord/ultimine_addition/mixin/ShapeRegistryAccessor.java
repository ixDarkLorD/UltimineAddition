package net.ixdarklord.ultimine_addition.mixin;

import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

// FTB Ultimine 2001's shape registry is static, so its list and default shape are reached through static accessors.
@Mixin(value = ShapeRegistry.class, remap = false)
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
