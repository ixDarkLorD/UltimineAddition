package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.ultimine_addition.common.progression.ShapeDiagram;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.ShapeDiagramPayload;
import net.minecraft.client.gui.GuiGraphics;
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

    public static void request(List<ResourceLocation> shapes) {
        if (!shapes.isEmpty()) PayloadHandler.sendToServer(new ShapeDiagramPayload.Request(List.copyOf(shapes)));
    }

    public static @Nullable ShapeDiagram get(ResourceLocation shape) {
        return DIAGRAMS.get(shape);
    }

    // Shapes that follow the block being mined have no fixed picture: a loose cluster stands in for them.
    private static final String[] CLUSTER = {".##..", "####.", "#o###", ".###.", "..#.."};

    /**
     * Draws a shape's diagram in a square of this size, as a grid of at least 5 x 5 cells (an empty grid until the
     * server's answer comes): the shape's blocks in one color, the block being broken in another.
     */
    public static void draw(GuiGraphics graphics, ResourceLocation shape, int x, int y, int size, int blockColor, int originColor, int emptyColor) {
        ShapeDiagram diagram = get(shape);
        boolean cluster = diagram != null && diagram.isIndeterminate();
        int shapeWidth = diagram == null ? 0 : cluster ? 5 : diagram.width();
        int shapeHeight = diagram == null ? 0 : cluster ? 5 : diagram.height();
        int columns = Math.max(5, shapeWidth), rows = Math.max(5, shapeHeight);
        int offsetX = (columns - shapeWidth) / 2, offsetY = (rows - shapeHeight) / 2;
        int pitch = Math.max(1, size / Math.max(columns, rows));
        // Cells keep a pixel between them when there is room for it.
        int cell = pitch >= 3 ? pitch - 1 : pitch;
        int left = x + (size - (pitch * columns - (pitch - cell))) / 2, top = y + (size - (pitch * rows - (pitch - cell))) / 2;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int dx = column - offsetX, dy = row - offsetY;
                boolean inside = dx >= 0 && dx < shapeWidth && dy >= 0 && dy < shapeHeight;
                int fill = emptyColor;
                if (inside && cluster) {
                    char c = CLUSTER[dy].charAt(dx);
                    if (c != '.') fill = c == 'o' ? originColor : blockColor;
                } else if (inside && diagram.has(dx, dy)) {
                    fill = diagram.isOrigin(dx, dy) ? originColor : blockColor;
                }
                graphics.fill(left + column * pitch, top + row * pitch, left + column * pitch + cell, top + row * pitch + cell, fill);
            }
        }
    }

    public static void accept(List<ShapeDiagram> diagrams) {
        for (ShapeDiagram diagram : diagrams) DIAGRAMS.put(diagram.shape(), diagram);
    }
}
