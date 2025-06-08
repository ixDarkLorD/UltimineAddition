package net.ixdarklord.ultimine_addition.core;

import dev.ftb.mods.ftbultimine.api.shape.Shape;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public interface ShapeRegistryAccessor {
    List<Shape> getShapesList();
    Shape getDefaultShape();
}
