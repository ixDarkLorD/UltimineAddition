package net.ixdarklord.ultimine_addition.client.undo;

import java.util.Map;
import java.util.HashMap;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ftb.mods.ftbultimine.utils.ShapeMerger;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.Util;
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
    private static final int GHOST_COLOR = ((int) (0.65F * 255) << 24) | 0xFFFFFF;
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

    // Block entities sit just inside their block's space. The ghosts' own models fill it exactly, so neighbouring
    // ghosts meet without a seam and a group reads as one solid shape.
    private static final float BLOCK_ENTITY_SCALE = 0.998F;

    // The ghosts' slow breathing, in their opacity.
    private static float pulse() {
        return 0.925F + 0.075F * Mth.sin(Util.getMillis() / 260.0F);
    }

    // The ghosts' block entities (chests, signs...), solid but at the ghosts' scale. See UndoBlockEntities.
    static void renderBlockEntities(PoseStack poseStack, MultiBufferSource buffers, float partial) {
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
        for (BlockEntity blockEntity : blockEntities) UndoBlockEntities.draw(blockEntity, BLOCK_ENTITY_SCALE, poseStack, buffers, partial);
    }

    public static void render(PoseStack poseStack) {
        UndoGrowthClient.INSTANCE.render(poseStack);
        UndoPreviewClient preview = UndoPreviewClient.INSTANCE;
        List<BlockPos> positions = preview.ghostPositions();
        if (positions.isEmpty()) return;
        List<BlockState> states = preview.ghostStates();
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cam = camera.getPosition();

        int count = Math.min(MAX_GHOSTS, positions.size());
        if (positions != modelsFor && minecraft.level != null) {
            modelsFor = positions;
            models = buildModels(positions.subList(0, count), states.subList(0, count), minecraft.level);
            AABB area = new AABB(positions.get(0));
            for (int i = 1; i < count; i++) area = area.minmax(new AABB(positions.get(i)));
            modelsArea = area.inflate(0.5);
        }
        // Standing in the mined area, the ghosts are seen from inside: their faces are drawn from behind too.
        boolean inside = modelsArea.contains(cam);

        float pulse = pulse();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        RenderType ghostType = RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
        VertexConsumer blocks = buffers.getBuffer(ghostType);
        for (Ghost ghost : models) {
            BlockPos pos = ghost.pos;
            poseStack.pushPose();
            poseStack.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
            ghost.model.draw(blocks, poseStack.last(), FULL_BRIGHT, inside, pulse);
            poseStack.popPose();
        }
        // Drawn now: the level renderer has already drawn its buffers at this point.
        buffers.endBatch(ghostType);

        // One connected outline around the whole group, built like FTB Ultimine's selection outline.
        if (positions != cachedFor) {
            cachedFor = positions;
            cachedOrigin = positions.get(0);
            cachedEdges = outerEdges(positions.subList(0, count), cachedOrigin);
        }
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
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
            lines.vertex(m, e[0], e[1], e[2]).color(127, 212, 255, 255).normal(poseStack.last().normal(), nx, ny, nz).endVertex();
            lines.vertex(m, e[3], e[4], e[5]).color(127, 212, 255, 255).normal(poseStack.last().normal(), nx, ny, nz).endVertex();
        }
        poseStack.popPose();
        buffers.endBatch(RenderType.lines());
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
    private static List<Ghost> buildModels(List<BlockPos> positions, List<BlockState> states, BlockAndTintGetter level) {
        Map<BlockPos, BlockState> byPos = new HashMap<>();
        for (int i = 0; i < positions.size(); i++) byPos.put(positions.get(i), states.get(i));
        List<Ghost> ghosts = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            BlockPos pos = positions.get(i);
            BlockState state = states.get(i);
            if (state.getRenderShape() != RenderShape.MODEL) continue;
            GhostModel model = GhostModel.build(state, pos, GHOST_COLOR, level, face -> {
                BlockPos next = pos.relative(face);
                BlockState neighbour = byPos.get(next);
                if (neighbour == null) neighbour = level.getBlockState(next);
                return !shouldRenderFace(state, neighbour, face);
            });
            if (!model.isEmpty()) ghosts.add(new Ghost(pos, model));
        }
        return ghosts;
    }

    // Block.shouldRenderFace, against a recorded neighbour instead of the one in the level (1.20.1's reads the level).
    private static boolean shouldRenderFace(BlockState state, BlockState neighbour, Direction face) {
        if (state.skipRendering(neighbour, face)) return false;
        if (!neighbour.canOcclude()) return true;
        VoxelShape own = state.getFaceOcclusionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, face);
        if (own.isEmpty()) return true;
        VoxelShape other = neighbour.getFaceOcclusionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, face.getOpposite());
        return Shapes.joinIsNotEmpty(own, other, BooleanOp.ONLY_FIRST);
    }

    private record Ghost(BlockPos pos, GhostModel model) {}
}
