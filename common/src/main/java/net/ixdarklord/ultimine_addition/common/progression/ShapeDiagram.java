package net.ixdarklord.ultimine_addition.common.progression;

import dev.ftb.mods.ftbultimine.api.shape.Shape;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// A small picture of an Ultimine shape, for the Skills Record's shape choice: a grid of cells, one bit each (row by row
// from the top), with the cell of the block the player breaks. It isn't drawn by hand: the server asks FTB Ultimine for
// the shape's blocks as if every block around the player matched, and flattens them onto the side that shows the most.
// Shapes that follow the block being mined (shapeless) have no fixed picture: their diagram is empty.
public record ShapeDiagram(Identifier shape, int width, int height, long cells, int origin) {
    // The largest grid: 5 x 5 cells stay readable in a tile's few pixels.
    public static final int MAX_SIZE = 5;
    // Tried in turn until the shape reaches the grid's edge: tunnels are as long as their block limit, and a short one
    // keeps the lookup next to the player.
    private static final int[] BLOCK_LIMITS = {9, 18, 27, 45};

    public boolean isIndeterminate() {
        return this.width <= 0 || this.height <= 0;
    }

    public boolean has(int column, int row) {
        return (this.cells >> (row * this.width + column) & 1L) != 0L;
    }

    public boolean isOrigin(int column, int row) {
        return row * this.width + column == this.origin;
    }

    public static ShapeDiagram indeterminate(Identifier shape) {
        return new ShapeDiagram(shape, 0, 0, 0L, -1);
    }

    /** The shape's diagram, worked out at the player's position (no block is touched, only looked at). */
    public static ShapeDiagram compute(ServerPlayer player, Identifier id, Shape shape) {
        if (shape.isIndeterminateShape()) return indeterminate(id);
        BlockPos origin = player.blockPosition();
        // The block is mined through the face looking back at the player.
        Direction into = player.getDirection();
        Direction right = into.getClockWise();

        int[][] best = null;
        for (int limit : BLOCK_LIMITS) {
            List<BlockPos> blocks;
            try {
                blocks = shape.getBlocks(new ShapeContext(player, origin, into.getOpposite(), Blocks.STONE.defaultBlockState(),
                        (original, state) -> true, limit));
            } catch (RuntimeException e) {
                return indeterminate(id);
            }
            if (blocks == null || blocks.isEmpty()) break;
            // Each block as (right, up, depth) from the broken one.
            int[][] points = new int[blocks.size() + 1][];
            points[0] = new int[]{0, 0, 0};
            int reach = 0;
            for (int i = 0; i < blocks.size(); i++) {
                BlockPos pos = blocks.get(i);
                int dx = pos.getX() - origin.getX(), dy = pos.getY() - origin.getY(), dz = pos.getZ() - origin.getZ();
                int[] point = {dx * right.getStepX() + dz * right.getStepZ(), dy, dx * into.getStepX() + dz * into.getStepZ()};
                points[i + 1] = point;
                reach = Math.max(reach, Math.max(Math.abs(point[0]), Math.max(Math.abs(point[1]), Math.abs(point[2]))));
            }
            boolean grew = best == null || points.length > best.length;
            if (grew) best = points;
            // Reaching the grid's edge, or no longer growing with a higher limit: this is the whole picture.
            if (!grew || reach >= MAX_SIZE - 1) break;
        }
        if (best == null) return indeterminate(id);

        // Front (right, up), side (depth, up) or top (right, depth): the view with the most distinct cells, and among
        // equals the one spreading over the larger area (a staircase is a line from the front, steps from the side).
        int[][] views = {{0, 1}, {2, 1}, {0, 2}};
        int[] view = views[0];
        long most = -1;
        for (int[] candidate : views) {
            Set<Long> distinct = new HashSet<>();
            int lowA = 0, highA = 0, lowB = 0, highB = 0;
            for (int[] point : best) {
                int a = point[candidate[0]], b = point[candidate[1]];
                distinct.add((long) a << 32 | (b & 0xFFFFFFFFL));
                lowA = Math.min(lowA, a);
                highA = Math.max(highA, a);
                lowB = Math.min(lowB, b);
                highB = Math.max(highB, b);
            }
            long score = (long) distinct.size() * 100_000L + (long) (highA - lowA + 1) * (highB - lowB + 1);
            if (score > most) {
                most = score;
                view = candidate;
            }
        }

        // A window of at most 5 x 5 around the broken block, slid to cover as much of the shape as it can.
        int minX = 0, maxX = 0, minY = 0, maxY = 0;
        for (int[] point : best) {
            minX = Math.min(minX, point[view[0]]);
            maxX = Math.max(maxX, point[view[0]]);
            minY = Math.min(minY, point[view[1]]);
            maxY = Math.max(maxY, point[view[1]]);
        }
        int left = window(minX, maxX), bottom = window(minY, maxY);
        int width = Math.min(MAX_SIZE, maxX - left + 1), height = Math.min(MAX_SIZE, maxY - bottom + 1);
        long cells = 0L;
        for (int[] point : best) {
            int column = point[view[0]] - left, row = height - 1 - (point[view[1]] - bottom);
            if (column < 0 || column >= width || row < 0 || row >= height) continue;
            cells |= 1L << (row * width + column);
        }
        return new ShapeDiagram(id, width, height, cells, (height - 1 + bottom) * width - left);
    }

    // Where a 5-wide window starts on an axis whose cells run from min to max (the broken block is at 0): centered on
    // the block when the shape spreads both ways, pushed towards the side it extends to otherwise.
    private static int window(int min, int max) {
        if (max - min + 1 <= MAX_SIZE) return min;
        return Math.max(min, Math.min(-MAX_SIZE / 2, max - MAX_SIZE + 1));
    }
}
