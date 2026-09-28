package net.ixdarklord.ultimine_addition.client.undo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

// The block-entity half of the undo's ghosts and growing blocks (chests, signs, shulker boxes... draw nothing through
// their block model). Each gets an empty client-only block entity, drawn by vanilla's renderer through the loaders'
// "submit" hooks, at the same scale as the block it belongs to.
public final class UndoBlockEntities {
    private UndoBlockEntities() {}

    // Called from each loader's submit hook (Fabric COLLECT_SUBMITS, NeoForge SubmitCustomGeometryEvent).
    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        float partial = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        UndoGrowthClient.INSTANCE.submitBlockEntities(poseStack, collector, camera, partial);
        UndoGhostRenderer.submitBlockEntities(poseStack, collector, camera, partial);
    }

    // An empty block entity for state at pos, or null when the block has none.
    static @Nullable BlockEntity create(BlockPos pos, BlockState state) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !(state.getBlock() instanceof EntityBlock entityBlock)) return null;
        BlockEntity blockEntity = entityBlock.newBlockEntity(pos, state);
        if (blockEntity == null) return null;
        blockEntity.setLevel(level);
        return blockEntity;
    }

    // Draws blockEntity scaled around its block's centre.
    static <E extends BlockEntity, S extends BlockEntityRenderState> void draw(E blockEntity, float scale, PoseStack poseStack, SubmitNodeCollector collector,
                                                                             CameraRenderState camera, float partial) {
        if (scale <= 0.0F) return;
        BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        BlockEntityRenderer<E, S> renderer = dispatcher.getRenderer(blockEntity);
        if (renderer == null) return;
        Vec3 cam = camera.pos;
        S state = renderer.createRenderState();
        renderer.extractRenderState(blockEntity, state, partial, cam, null);
        BlockPos pos = blockEntity.getBlockPos();
        poseStack.pushPose();
        poseStack.translate(pos.getX() - cam.x + 0.5, pos.getY() - cam.y + 0.5, pos.getZ() - cam.z + 0.5);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5, -0.5, -0.5);
        dispatcher.submit(state, poseStack, collector, camera);
        poseStack.popPose();
    }
}
