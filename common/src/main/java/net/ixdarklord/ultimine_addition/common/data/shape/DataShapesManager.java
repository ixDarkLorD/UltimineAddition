package net.ixdarklord.ultimine_addition.common.data.shape;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static net.ixdarklord.ultimine_addition.core.FTBUltimineAddition.LOGGER;

// Loads the Ultimine shapes data packs define (data/<namespace>/ultimine_shapes/*.json, see DataShape) and keeps them in
// FTB Ultimine's shape registries, after the shapes mods registered: the server's when data packs load, the client's
// when the server's list arrives. Both lists are in id order, so a shape has the same place on either side (FTB Ultimine
// picks shapes by their place in the list).
public class DataShapesManager extends SimpleJsonResourceReloadListener<JsonElement> {
    public static final DataShapesManager INSTANCE = new DataShapesManager();
    private List<DataShape> shapes = List.of();

    public DataShapesManager() {
        super(ExtraCodecs.JSON, FileToIdConverter.json("ultimine_shapes"));
    }

    @Override
    protected void apply(@NotNull Map<Identifier, JsonElement> files, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        List<DataShape> loaded = new ArrayList<>();
        new TreeMap<>(files).forEach((id, json) -> DataShape.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> LOGGER.error("Skipping the Ultimine shape {}: {}", id, error))
                .ifPresent(definition -> loaded.add(DataShape.of(id, definition))));
        this.shapes = List.copyOf(loaded);
        this.install();
        if (!loaded.isEmpty()) LOGGER.info("Loaded {} Ultimine shape(s) from data packs", loaded.size());
    }

    public List<DataShape> getShapes() {
        return this.shapes;
    }

    /** Puts the loaded shapes at the end of the server's shape list (again: harmless when they are there already). */
    public void install() {
        replace(serverList(), this.shapes);
    }

    /** Gives the client's shape list the server's data pack shapes. */
    public static void installClient(List<DataShape> shapes) {
        replace(clientList(), shapes);
    }

    // FTB Ultimine keeps a shape list per side.
    private static List<Shape> serverList() {
        return FTBUltimineIntegration.getShapesList(ShapeRegistry.getInstance(false));
    }

    private static List<Shape> clientList() {
        return FTBUltimineIntegration.getShapesList(ShapeRegistry.getInstance(true));
    }

    // Swaps a list's data pack shapes for these, leaving the shapes mods registered alone.
    private static void replace(List<Shape> list, List<DataShape> shapes) {
        list.removeIf(shape -> shape instanceof DataShape);
        // A mod's shape of the same id wins.
        for (DataShape shape : shapes) {
            if (list.stream().noneMatch(existing -> existing.getName().equals(shape.getName()))) list.add(shape);
        }
    }
}
