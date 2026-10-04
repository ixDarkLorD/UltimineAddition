package net.ixdarklord.ultimine_addition.common.data.shape;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import net.ixdarklord.coolcatcore.api.platform.Platform;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static net.ixdarklord.ultimine_addition.core.FTBUltimineAddition.LOGGER;

// Loads the Ultimine shapes data packs define (data/<namespace>/ultimine_shapes/*.json, see DataShape) and keeps them in
// FTB Ultimine's shape list, after the shapes mods registered: when data packs load, and on a client of another
// server when that server's list arrives. They are in id order, so a shape has the same place on either side (FTB
// Ultimine picks shapes by their place in the list).
public class DataShapesManager extends SimpleJsonResourceReloadListener {
    public static final DataShapesManager INSTANCE = new DataShapesManager();
    private List<DataShape> shapes = List.of();

    public DataShapesManager() {
        super(new Gson(), "ultimine_shapes");
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> files, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
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
        // One list serves both sides here: with a server in this game, it already holds the real shapes.
        if (Platform.getServer() != null) return;
        replace(clientList(), shapes);
    }

    // This FTB Ultimine keeps one shape list.
    private static List<Shape> serverList() {
        return FTBUltimineIntegration.getShapesList();
    }

    private static List<Shape> clientList() {
        return FTBUltimineIntegration.getShapesList();
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
