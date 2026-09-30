package net.ixdarklord.ultimine_addition.core;

import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

// Stands in when a player has no usable shape for the tool in hand, so FTB Ultimine's shape HUD has something
// to name ("No Shape Learned") and no blocks get selected. Never registered with FTB Ultimine.
public final class NoShape implements Shape {
    public static final NoShape INSTANCE = new NoShape();
    public static final ResourceLocation ID = FTBUltimineAddition.id("no_shape");

    private NoShape() {}

    // FTB Ultimine 2001 names shapes with plain strings and translates "ftbultimine.shape.<name>", which gives the same
    // key as on newer versions ("ftbultimine.shape.ultimine_addition.no_shape").
    @Override
    public String getName() {
        return ID.getNamespace() + "." + ID.getPath();
    }

    @Override
    public List<BlockPos> getBlocks(ShapeContext context) {
        return List.of();
    }
}
