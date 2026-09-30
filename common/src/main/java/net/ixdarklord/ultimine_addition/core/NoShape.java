package net.ixdarklord.ultimine_addition.core;

import dev.ftb.mods.ftbultimine.api.shape.Shape;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// Stands in when a player has no usable shape for the tool in hand, so FTB Ultimine's shape HUD has something
// to name ("No Shape Learned") and no blocks get selected. Never registered with FTB Ultimine.
public final class NoShape implements Shape {
    public static final NoShape INSTANCE = new NoShape();
    private static final ResourceLocation ID = FTBUltimineAddition.id("no_shape");

    private NoShape() {}

    @Override
    public @NotNull ResourceLocation getName() {
        return ID;
    }

    @Override
    public @NotNull List<BlockPos> getBlocks(@NotNull ShapeContext context) {
        return List.of();
    }
}
