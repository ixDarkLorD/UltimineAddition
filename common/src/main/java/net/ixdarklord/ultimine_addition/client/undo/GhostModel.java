package net.ixdarklord.ultimine_addition.client.undo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

// A block's model reduced to what the undo ghosts draw: its quads, each with its final colour (tint included),
// gathered once instead of every frame. Faces hidden against a neighbour are left out when built, and faces turned
// away from the camera are skipped when drawn.
record GhostModel(BakedQuad[] quads, int[] colors) {
    private static final Direction[] FACES = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};
    private static final GhostModel EMPTY = new GhostModel(new BakedQuad[0], new int[0]);
    // Scratch space for drawing; only used on the render thread.
    private static final Vector3f[] VERTICES = {new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
    private static final Vector3f NORMAL = new Vector3f();

    /**
     * @param hiddenFace faces (by cull direction) not to keep, e.g. those against another ghost; null keeps all
     */
    static GhostModel build(BlockState state, BlockPos pos, int color, BlockAndTintGetter level, @Nullable Predicate<Direction> hiddenFace) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockStateModel model = minecraft.getModelManager().getBlockStateModelSet().get(state);
        List<BlockStateModelPart> parts = new ArrayList<>();
        RandomSource random = RandomSource.create(state.getSeed(pos));
        model.collectParts(random, parts);
        if (parts.isEmpty()) return EMPTY;

        // Tinted quads (grass, leaves, water...) take the block's tint at pos, as in the world.
        List<BlockTintSource> tints = minecraft.getBlockColors().getTintSources(state);
        int[] tinted = new int[tints.size()];
        for (int i = 0; i < tinted.length; i++) tinted[i] = ARGB.multiply(color, ARGB.opaque(tints.get(i).colorInWorld(state, level, pos)));

        List<BakedQuad> quads = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (BlockStateModelPart part : parts) {
            for (Direction face : FACES) {
                if (face != null && hiddenFace != null && hiddenFace.test(face)) continue;
                for (BakedQuad quad : part.getQuads(face)) {
                    int tintIndex = quad.materialInfo().tintIndex();
                    quads.add(quad);
                    colors.add(tintIndex >= 0 && tintIndex < tinted.length ? tinted[tintIndex] : color);
                }
            }
        }
        int[] colorArray = new int[colors.size()];
        for (int i = 0; i < colorArray.length; i++) colorArray[i] = colors.get(i);
        return new GhostModel(quads.toArray(BakedQuad[]::new), colorArray);
    }

    boolean isEmpty() {
        return this.quads.length == 0;
    }

    /**
     * Draws the model at the pose's origin. The pose is camera-relative (the camera at its origin), so a quad faces
     * the camera when its normal points back towards the origin; the others are skipped.
     */
    void draw(VertexConsumer buffer, PoseStack.Pose pose, int light) {
        this.draw(buffer, pose, light, false, 1.0F);
    }

    /**
     * @param backFaces also draw the faces turned away, from behind (when the camera is inside the ghosts)
     * @param alpha     multiplies each quad's own alpha
     */
    void draw(VertexConsumer buffer, PoseStack.Pose pose, int light, boolean backFaces, float alpha) {
        Matrix4f matrix = pose.pose();
        for (int q = 0; q < this.quads.length; q++) {
            BakedQuad quad = this.quads[q];
            for (int vertex = 0; vertex < 4; vertex++) matrix.transformPosition(quad.position(vertex), VERTICES[vertex]);
            Vector3f normal = pose.transformNormal(quad.direction().getUnitVec3f(), NORMAL);
            // The quad's own plane (angled quads, like crossed plants, aren't along their direction), turned to the
            // side its direction says is the front.
            Vector3f v0 = VERTICES[0];
            float ax = VERTICES[1].x - v0.x, ay = VERTICES[1].y - v0.y, az = VERTICES[1].z - v0.z;
            float bx = VERTICES[2].x - v0.x, by = VERTICES[2].y - v0.y, bz = VERTICES[2].z - v0.z;
            float px = ay * bz - az * by, py = az * bx - ax * bz, pz = ax * by - ay * bx;
            if (px * normal.x + py * normal.y + pz * normal.z < 0.0F) {
                px = -px;
                py = -py;
                pz = -pz;
            }
            boolean back = px * v0.x + py * v0.y + pz * v0.z >= 0.0F;
            if (back && !backFaces) continue;
            int color = alpha == 1.0F ? this.colors[q] : ARGB.multiplyAlpha(this.colors[q], alpha);
            // The back side: vertices in reverse (so it isn't culled) and the normal flipped.
            float sign = back ? -1.0F : 1.0F;
            for (int i = 0; i < 4; i++) {
                int vertex = back ? 3 - i : i;
                Vector3f p = VERTICES[vertex];
                long uv = quad.packedUV(vertex);
                buffer.addVertex(p.x(), p.y(), p.z(), color, UVPair.unpackU(uv), UVPair.unpackV(uv), OverlayTexture.NO_OVERLAY, light, sign * normal.x(), sign * normal.y(), sign * normal.z());
            }
        }
    }
}
