package net.ixdarklord.ultimine_addition.client.undo;

import net.minecraft.world.level.block.Block;
import java.util.Map;
import java.util.HashMap;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ftb.mods.ftbultimine.utils.ShapeMerger;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

// Draws the undo preview's ghosts: the blocks' own models, see-through, inside one connected outline.
// Called from each loader's "after translucent blocks" render hook.
public final class UndoGhostRenderer {
    private static final int MAX_GHOSTS = 512;
    private static final int GHOST_COLOR = ((int) (0.45F * 255) << 24) | 0xFFFFFF;
    private static final int FULL_BRIGHT = 0xF000F0;

    // The outline only changes with the preview, so it's built once per preview list.
    private static @Nullable List<BlockPos> cachedFor;
    private static BlockPos cachedOrigin = BlockPos.ZERO;
    private static List<float[]> cachedEdges = List.of();
    // The ghosts' models, also per preview list.
    private static @Nullable List<BlockPos> modelsFor;
    private static List<Ghost> models = List.of();
    private static AABB modelsArea = new AABB(BlockPos.ZERO);
    // Empty block entities for the ghosts that have one, also per preview list.
    private static @Nullable List<BlockPos> blockEntitiesFor;
    private static List<BlockEntity> blockEntities = List.of();

    private UndoGhostRenderer() {}

    // The ghosts' slow breathing scale, just inside the block's space.
    private static float pulse() {
        return 0.9F + 0.03F * Mth.sin(Util.getMillis() / 260.0F);
    }

    // The ghosts' block entities (chests, signs...), solid but at the ghosts' scale. See UndoBlockEntities.
    static void submitBlockEntities(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, float partial) {
        UndoPreviewClient preview = UndoPreviewClient.INSTANCE;
        List<BlockPos> positions = preview.ghostPositions();
        if (positions.isEmpty()) {
            blockEntitiesFor = null;
            blockEntities = List.of();
            return;
        }
        if (positions != blockEntitiesFor) {
            blockEntitiesFor = positions;
            List<BlockState> states = preview.ghostStates();
            List<BlockEntity> created = new ArrayList<>();
            for (int i = 0; i < Math.min(MAX_GHOSTS, positions.size()); i++) {
                BlockEntity blockEntity = UndoBlockEntities.create(positions.get(i), states.get(i));
                if (blockEntity != null) created.add(blockEntity);
            }
            blockEntities = created;
        }
        float pulse = pulse();
        for (BlockEntity blockEntity : blockEntities) UndoBlockEntities.draw(blockEntity, pulse, poseStack, collector, camera, partial);
    }

    public static void render(PoseStack poseStack) {
        UndoGrowthClient.INSTANCE.render(poseStack);
        UndoPreviewClient preview = UndoPreviewClient.INSTANCE;
        List<BlockPos> positions = preview.ghostPositions();
        if (positions.isEmpty()) return;
        List<BlockState> states = preview.ghostStates();
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.getEntityRenderDispatcher().camera;
        if (camera == null) return;
        Vec3 cam = camera.position();

        int count = Math.min(MAX_GHOSTS, positions.size());
        if (positions != modelsFor && minecraft.level != null) {
            modelsFor = positions;
            models = buildModels(positions.subList(0, count), states.subList(0, count), minecraft.level);
            AABB area = new AABB(positions.getFirst());
            for (int i = 1; i < count; i++) area = area.minmax(new AABB(positions.get(i)));
            modelsArea = area.inflate(0.5);
        }
        // Standing in the mined area, the ghosts are seen from inside: their faces are drawn from behind too.
        boolean inside = modelsArea.contains(cam);

        float pulse = pulse();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer blocks = buffers.getBuffer(RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        for (Ghost ghost : models) {
            BlockPos pos = ghost.pos;
            poseStack.pushPose();
            // Scaled around the block's centre so the ghost sits just inside the block's space.
            poseStack.translate(pos.getX() - cam.x + 0.5, pos.getY() - cam.y + 0.5, pos.getZ() - cam.z + 0.5);
            poseStack.scale(pulse, pulse, pulse);
            poseStack.translate(-0.5, -0.5, -0.5);
            ghost.model.draw(blocks, poseStack.last(), FULL_BRIGHT, inside);
            poseStack.popPose();
        }

        // One connected outline around the whole group, built like FTB Ultimine's selection outline.
        if (positions != cachedFor) {
            cachedFor = positions;
            cachedOrigin = positions.getFirst();
            cachedEdges = outerEdges(positions.subList(0, count), cachedOrigin);
        }
        VertexConsumer lines = buffers.getBuffer(RenderTypes.lines());
        poseStack.pushPose();
        poseStack.translate(cachedOrigin.getX() - cam.x, cachedOrigin.getY() - cam.y, cachedOrigin.getZ() - cam.z);
        Matrix4f m = poseStack.last().pose();
        for (float[] e : cachedEdges) {
            float nx = e[3] - e[0], ny = e[4] - e[1], nz = e[5] - e[2];
            float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
            if (len == 0) continue;
            nx /= len;
            ny /= len;
            nz /= len;
            lines.addVertex(m, e[0], e[1], e[2]).setColor(127, 212, 255, 255).setNormal(nx, ny, nz).setLineWidth(2.0F);
            lines.addVertex(m, e[3], e[4], e[5]).setColor(127, 212, 255, 255).setNormal(nx, ny, nz).setLineWidth(2.0F);
        }
        poseStack.popPose();
    }

    // The outer edges of the blocks merged into one shape, relative to origin.
    private static List<float[]> outerEdges(List<BlockPos> positions, BlockPos origin) {
        VoxelShape combined = Shapes.empty();
        for (AABB aabb : ShapeMerger.merge(positions, origin)) {
            combined = Shapes.joinUnoptimized(combined, Shapes.create(aabb.inflate(0.005D)), BooleanOp.OR);
        }
        List<float[]> edges = new ArrayList<>();
        combined.optimize().forAllEdges((x1, y1, z1, x2, y2, z2) -> edges.add(new float[]{(float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2}));
        return edges;
    }

    // Each ghost's model; a face against another ghost that would hide it (as in the world) is left out, so a
    // solid group only shows its outside.
    private static List<Ghost> buildModels(List<BlockPos> positions, List<BlockState> states, BlockAndTintGetter level) {
        Map<BlockPos, BlockState> byPos = new HashMap<>();
        for (int i = 0; i < positions.size(); i++) byPos.put(positions.get(i), states.get(i));
        List<Ghost> ghosts = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            BlockPos pos = positions.get(i);
            BlockState state = states.get(i);
            if (state.getRenderShape() != RenderShape.MODEL) continue;
            GhostModel model = GhostModel.build(state, pos, GHOST_COLOR, level, face -> {
                BlockState neighbour = byPos.get(pos.relative(face));
                return neighbour != null && !Block.shouldRenderFace(state, neighbour, face);
            });
            if (!model.isEmpty()) ghosts.add(new Ghost(pos, model));
        }
        return ghosts;
    }

    private record Ghost(BlockPos pos, GhostModel model) {}
}
