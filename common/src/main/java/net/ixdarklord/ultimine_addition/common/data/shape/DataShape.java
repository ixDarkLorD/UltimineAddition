package net.ixdarklord.ultimine_addition.common.data.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// An Ultimine shape defined by a data pack (data/<namespace>/ultimine_shapes/<name>.json). Its blocks are offsets from
// the block being broken, as the player sees it: right, up, and depth (into the block). With "repeat": "depth" the same
// blocks are taken again one step deeper, over and over, until the block limit: a tunnel.
//
//   { "name": "Wide Cut", "pattern": ["#####", "##o##", "#####"] }
//   { "blocks": [[0, 1, 0], [0, -1, 0]], "repeat": "depth" }
//
// "pattern" draws the blocks facing the player (o is the broken block, # a block, anything else nothing); "blocks" lists
// [right, up, depth] offsets. Both may be given. On a client the shape is only a name: the server works out the blocks.
public final class DataShape implements Shape {
    private static final int MAX_OFFSET = 32;

    public static final Codec<Definition> CODEC = RecordCodecBuilder.<Definition>create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name").forGetter(Definition::name),
            Codec.STRING.listOf().optionalFieldOf("pattern", List.of()).forGetter(Definition::pattern),
            Codec.INT.listOf().listOf().optionalFieldOf("blocks", List.of()).forGetter(Definition::blocks),
            Codec.STRING.optionalFieldOf("repeat", "none").forGetter(Definition::repeat)
    ).apply(instance, Definition::new)).flatXmap(DataShape::validate, DataResult::success);

    public record Definition(Optional<String> name, List<String> pattern, List<List<Integer>> blocks, String repeat) {
    }

    private final ResourceLocation id;
    private final String name;
    // [right, up, depth] offsets from the broken block, which comes first.
    private final List<int[]> offsets;
    private final boolean repeatDepth;

    private DataShape(ResourceLocation id, String name, List<int[]> offsets, boolean repeatDepth) {
        this.id = id;
        this.name = name;
        this.offsets = offsets;
        this.repeatDepth = repeatDepth;
    }

    /** The shape a data pack file describes. */
    public static DataShape of(ResourceLocation id, Definition definition) {
        List<int[]> offsets = new ArrayList<>();
        Set<Long> seen = new LinkedHashSet<>();
        add(offsets, seen, 0, 0, 0);
        List<String> pattern = definition.pattern();
        int originRow = pattern.size() / 2, originColumn = pattern.isEmpty() ? 0 : pattern.get(0).length() / 2;
        for (int row = 0; row < pattern.size(); row++) {
            int column = pattern.get(row).indexOf('o');
            if (column >= 0) {
                originRow = row;
                originColumn = column;
            }
        }
        for (int row = 0; row < pattern.size(); row++) {
            String line = pattern.get(row);
            for (int column = 0; column < line.length(); column++) {
                if (line.charAt(column) == '#') add(offsets, seen, column - originColumn, originRow - row, 0);
            }
        }
        for (List<Integer> block : definition.blocks()) add(offsets, seen, block.get(0), block.get(1), block.get(2));
        // Nearest first (the broken block stays first): a low block limit takes the blocks around it, not a corner.
        offsets.sort(Comparator.comparingInt(offset -> Math.abs(offset[0]) + Math.abs(offset[1]) + Math.abs(offset[2])));
        return new DataShape(id, definition.name().orElse(""), offsets, definition.repeat().equals("depth"));
    }

    /** The client's stand-in for a server's shape: its id and name, to list and pick it. */
    public static DataShape named(ResourceLocation id, String name) {
        return new DataShape(id, name, List.of(), false);
    }

    private static void add(List<int[]> offsets, Set<Long> seen, int right, int up, int depth) {
        if (seen.add(((long) right & 0xFFFFF) << 40 | ((long) up & 0xFFFFF) << 20 | ((long) depth & 0xFFFFF))) offsets.add(new int[]{right, up, depth});
    }

    private static DataResult<Definition> validate(Definition definition) {
        if (!definition.repeat().equals("none") && !definition.repeat().equals("depth")) {
            return DataResult.error(() -> "\"repeat\" must be \"none\" or \"depth\", not \"" + definition.repeat() + "\"");
        }
        if (definition.pattern().isEmpty() && definition.blocks().isEmpty()) {
            return DataResult.error(() -> "A shape needs a \"pattern\" or \"blocks\"");
        }
        if (definition.pattern().size() > MAX_OFFSET * 2 + 1 || definition.pattern().stream().anyMatch(line -> line.length() > MAX_OFFSET * 2 + 1)) {
            return DataResult.error(() -> "\"pattern\" is larger than " + (MAX_OFFSET * 2 + 1) + " x " + (MAX_OFFSET * 2 + 1));
        }
        for (List<Integer> block : definition.blocks()) {
            if (block.size() != 3) return DataResult.error(() -> "Each of \"blocks\" is [right, up, depth], not " + block);
            if (block.stream().anyMatch(value -> Math.abs(value) > MAX_OFFSET)) {
                return DataResult.error(() -> "Offsets in \"blocks\" go up to " + MAX_OFFSET + ", not " + block);
            }
        }
        return DataResult.success(definition);
    }

    public String name() {
        return this.name;
    }

    public ResourceLocation id() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.id.toString();
    }

    // A data pack has no language file: the name it gives is shown as it is, in this mod's screens. FTB Ultimine's own
    // shape display on this version only knows the key ftbultimine.shape.<namespace>:<path>, for a resource pack to
    // translate.
    public MutableComponent getDisplayName() {
        return this.name.isEmpty() ? Component.translatable("ftbultimine.shape." + this.id) : Component.literal(this.name);
    }

    @Override
    public List<BlockPos> getBlocks(ShapeContext context) {
        // "Depth" goes into the clicked face. On a wall "up" is up; on a floor or ceiling it is the way the player faces.
        Direction into = context.face().getOpposite();
        Direction up = into.getAxis().isVertical() ? context.player().getDirection() : Direction.UP;
        Direction right = into.getAxis().isVertical() ? up.getClockWise() : into.getClockWise();

        Set<BlockPos> blocks = new LinkedHashSet<>();
        int limit = context.maxBlocks();
        for (int step = 0; blocks.size() < limit; step++) {
            boolean any = false;
            for (int[] offset : this.offsets) {
                if (blocks.size() >= limit) break;
                BlockPos pos = context.pos().relative(right, offset[0]).relative(up, offset[1]).relative(into, offset[2] + step);
                if (!blocks.contains(pos) && context.check(pos)) {
                    blocks.add(pos);
                    any = true;
                }
            }
            // A layer with nothing to take ends a tunnel.
            if (!this.repeatDepth || !any) break;
        }
        return new ArrayList<>(blocks);
    }
}
