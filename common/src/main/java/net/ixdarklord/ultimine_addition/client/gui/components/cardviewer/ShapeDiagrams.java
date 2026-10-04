package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.ultimine_addition.common.progression.ShapeDiagram;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.ShapeDiagramPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// The client's copies of the shape diagrams the server worked out. They are asked for whenever the shape choice opens,
// so a changed shape (another server, a reloaded plugin) is picked up; until the answer comes a tile shows an empty grid.
public final class ShapeDiagrams {
    private static final Map<ResourceLocation, ShapeDiagram> DIAGRAMS = new HashMap<>();

    private ShapeDiagrams() {
    }

    static void request(List<ResourceLocation> shapes) {
        if (!shapes.isEmpty()) PayloadHandler.sendToServer(new ShapeDiagramPayload.Request(List.copyOf(shapes)));
    }

    static @Nullable ShapeDiagram get(ResourceLocation shape) {
        return DIAGRAMS.get(shape);
    }

    public static void accept(List<ShapeDiagram> diagrams) {
        for (ShapeDiagram diagram : diagrams) DIAGRAMS.put(diagram.shape(), diagram);
    }
}
