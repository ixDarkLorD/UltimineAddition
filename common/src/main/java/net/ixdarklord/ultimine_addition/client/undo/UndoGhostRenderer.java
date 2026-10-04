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

// Draws the undo preview's ghosts: the blocks' own models, see-through, inside one connected outline. The blocks an
// undo with missing items wouldn't put back are tinted red, inside a red outline of their own.
// Called from each loader's "after translucent blocks" render hook.
public final class UndoGhostRenderer {
    private static final int MAX_GHOSTS = 512;
    private static final int GHOST_COLOR = ((int) (0.65F * 255) << 24) | 0xFFFFFF;
    // A block the undo can't put back (its items are missing): its ghost in red.
    private static final int MISSING_COLOR = ((int) (0.7F * 255) << 24) | 0xFF4A4A;
    private static final int FULL_BRIGHT = 0xF000F0;

    // The outline only changes with the preview, so it's built once per preview list.
    private static @Nullable List<BlockPos> cachedFor;
    private static BlockPos cachedOrigin = BlockPos.ZERO;
    private static List<float[]> cachedEdges = List.of();
    private static List<float[]> cachedMissingEdges = List.of();
    // The ghosts' models, also per preview list.
    private static @Nullable List<BlockPos> modelsFor;
    private static List<Ghost> models = List.of();
    private static AABB modelsArea = new AABB(BlockPos.ZERO);
    // Empty block entities for the ghosts that have one, also per preview list.
    private static @Nullable List<BlockPos> blockEntitiesFor;
    private static List<BlockEntity> blockEntities = List.of();

    private UndoGhostRenderer() {}

    // Block entities sit just inside their block's space. The ghosts' own models fill it exactly, so neighbouring
    // ghosts meet without a seam and a group reads as one solid shape.
    private static final float BLOCK_ENTITY_SCALE = 0.998F;

    // The ghosts' slow breathing, in their opacity.
    private static float pulse() {
        return 0.925F + 0.075F * Mth.sin(Util.getMillis() / 260.0F);
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
        for (BlockEntity blockEntity : blockEntities) UndoBlockEntities.draw(blockEntity, BLOCK_ENTITY_SCALE, poseStack, collector, camera, partial);
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
            models = buildModels(positions.subList(0, count), states.subList(0, count), preview.ghostComesBack(), minecraft.level);
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
            poseStack.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
            ghost.model.draw(blocks, poseStack.last(), FULL_BRIGHT, inside, pulse);
            poseStack.popPose();
        }

        // One connected outline around the blocks that come back, built like FTB Ultimine's selection outline, and a
        // red one around those that don't.
        if (positions != cachedFor) {
            cachedFor = positions;
            cachedOrigin = positions.getFirst();
            List<Boolean> comesBack = preview.ghostComesBack();
            List<BlockPos> back = new ArrayList<>();
            List<BlockPos> missing = new ArrayList<>();
            for (int i = 0; i < count; i++) (comesBack(comesBack, i) ? back : missing).add(positions.get(i));
            cachedEdges = back.isEmpty() ? List.of() : outerEdges(back, cachedOrigin);
            cachedMissingEdges = missing.isEmpty() ? List.of() : outerEdges(missing, cachedOrigin);
        }
        VertexConsumer lines = buffers.getBuffer(RenderTypes.lines());
        poseStack.pushPose();
        poseStack.translate(cachedOrigin.getX() - cam.x, cachedOrigin.getY() - cam.y, cachedOrigin.getZ() - cam.z);
        Matrix4f m = poseStack.last().pose();
        drawEdges(lines, m, cachedEdges, 127, 212, 255);
        drawEdges(lines, m, cachedMissingEdges, 255, 90, 90);
        poseStack.popPose();
    }

    private static void drawEdges(VertexConsumer lines, Matrix4f m, List<float[]> edges, int red, int green, int blue) {
        for (float[] e : edges) {
            float nx = e[3] - e[0], ny = e[4] - e[1], nz = e[5] - e[2];
            float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
            if (len == 0) continue;
            nx /= len;
            ny /= len;
            nz /= len;
            lines.addVertex(m, e[0], e[1], e[2]).setColor(red, green, blue, 255).setNormal(nx, ny, nz).setLineWidth(2.0F);
            lines.addVertex(m, e[3], e[4], e[5]).setColor(red, green, blue, 255).setNormal(nx, ny, nz).setLineWidth(2.0F);
        }
    }

    // Whether the ghost at an index comes back (true when the preview doesn't say).
    private static boolean comesBack(List<Boolean> comesBack, int index) {
        return index >= comesBack.size() || comesBack.get(index);
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

    // Each ghost's model. A face is left out where the world would hide it: against another ghost (so a solid group
    // only shows its outside) or against a real block that covers it (a face that can't be seen anyway).
    private static List<Ghost> buildModels(List<BlockPos> positions, List<BlockState> states, List<Boolean> comesBack, BlockAndTintGetter level) {
        Map<BlockPos, BlockState> byPos = new HashMap<>();
        for (int i = 0; i < positions.size(); i++) byPos.put(positions.get(i), states.get(i));
        List<Ghost> ghosts = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            BlockPos pos = positions.get(i);
            BlockState state = states.get(i);
            if (state.getRenderShape() != RenderShape.MODEL) continue;
            GhostModel model = GhostModel.build(state, pos, comesBack(comesBack, i) ? GHOST_COLOR : MISSING_COLOR, level, face -> {
                BlockPos next = pos.relative(face);
                BlockState neighbour = byPos.get(next);
                if (neighbour == null) neighbour = level.getBlockState(next);
                return !Block.shouldRenderFace(state, neighbour, face);
            });
            if (!model.isEmpty()) ghosts.add(new Ghost(pos, model));
        }
        return ghosts;
    }

    private record Ghost(BlockPos pos, GhostModel model) {}
}
