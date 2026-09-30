package net.ixdarklord.ultimine_addition.client.undo;

import net.ixdarklord.coolcatcanvas.api.utils.Easing;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.ixdarklord.ultimine_addition.network.payloads.UndoPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Blocks growing back after an undo, drawn by the client (like Building Gadgets' placing): the scale follows the
// client's own ticks plus the frame's partial tick, so it's smooth whatever the server or network does. The server
// places the real block when the growth has finished; the ghost goes as soon as that block is there.
public final class UndoGrowthClient {
    public static final UndoGrowthClient INSTANCE = new UndoGrowthClient();
    // How long a finished ghost waits for its real block before giving up (the spot was taken, the packet was lost).
    private static final int LINGER_TICKS = 40;

    private final List<Growing> growing = new ArrayList<>();
    private long ticks;

    private UndoGrowthClient() {}

    public void add(UndoPayload.Grow grow) {
        Minecraft minecraft = Minecraft.getInstance();
        int duration = Math.max(1, grow.growTicks());
        List<Double> starts = new ArrayList<>();
        for (int i = 0; i < grow.positions().size(); i++) {
            double start = this.ticks + grow.starts().get(i);
            starts.add(start);
            BlockState state = grow.states().get(i);
            BlockPos pos = grow.positions().get(i);
            // Chests, signs and the like draw through their block entity instead of (or as well as) their model.
            BlockEntity blockEntity = UndoBlockEntities.create(pos, state);
            GhostModel model = state.getRenderShape() == RenderShape.MODEL && minecraft.level != null
                    ? GhostModel.build(state, pos, 0xFFFFFFFF, minecraft.level, null) : null;
            if (model != null && model.isEmpty()) model = null;
            if (model == null && blockEntity == null) continue;
            this.growing.add(new Growing(pos, state, start, duration, model, blockEntity));
        }
        // The player who undid it follows the same timeline in the progress HUD.
        if (minecraft.player != null && minecraft.player.getUUID().equals(grow.owner())) UndoProgressHud.INSTANCE.follow(starts, duration);
    }

    // Growth progress (0..1) of a block starting at start, on this client's timeline.
    static float progress(double start, int duration, float partial) {
        return (float) Mth.clamp((INSTANCE.ticks - start + partial) / duration, 0.0, 1.0);
    }

    public void clear() {
        this.growing.clear();
    }

    public void tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            this.growing.clear();
            return;
        }
        this.ticks++;
        Iterator<Growing> it = this.growing.iterator();
        while (it.hasNext()) {
            Growing g = it.next();
            double age = this.ticks - g.start;
            // The real block is in: it takes over from the ghost.
            if (age >= 0 && level.getBlockState(g.pos) == g.state) it.remove();
            else if (age > g.duration + LINGER_TICKS) it.remove();
        }
    }

    void render(PoseStack poseStack) {
        if (this.growing.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) return;
        Vec3 cam = minecraft.gameRenderer.getMainCamera().getPosition();
        float partial = minecraft.getTimer().getGameTimeDeltaPartialTick(false);

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        RenderType type = RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
        VertexConsumer buffer = buffers.getBuffer(type);
        for (Growing g : this.growing) {
            if (g.model == null) continue;
            float scale = scale(g, partial);
            if (scale <= 0.0F) continue;
            poseStack.pushPose();
            poseStack.translate(g.pos.getX() - cam.x + 0.5, g.pos.getY() - cam.y + 0.5, g.pos.getZ() - cam.z + 0.5);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.5, -0.5, -0.5);
            g.model.draw(buffer, poseStack.last(), LevelRenderer.getLightColor(level, g.pos));
            poseStack.popPose();
        }
        // Drawn now: the level renderer has already drawn its buffers at this point.
        buffers.endBatch(type);
    }

    // Ease-out: quick at first, settling gently into full size.
    private static float scale(Growing g, float partial) {
        return Easing.CUBIC_OUT.applyClamped(progress(g.start, g.duration, partial));
    }

    // The growing blocks' block entities, at the same scale as their blocks. See UndoBlockEntities.
    void renderBlockEntities(PoseStack poseStack, MultiBufferSource buffers, float partial) {
        for (Growing g : this.growing) {
            if (g.blockEntity != null) UndoBlockEntities.draw(g.blockEntity, scale(g, partial), poseStack, buffers, partial);
        }
    }

    private record Growing(BlockPos pos, BlockState state, double start, int duration, @Nullable GhostModel model, @Nullable BlockEntity blockEntity) {}
}
