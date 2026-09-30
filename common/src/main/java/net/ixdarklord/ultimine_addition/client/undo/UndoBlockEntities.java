package net.ixdarklord.ultimine_addition.client.undo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

// The block-entity half of the undo's ghosts and growing blocks (chests, signs, shulker boxes... draw nothing through
// their block model). Each gets an empty client-only block entity, drawn by vanilla's renderer next to the level's own
// block entities, at the same scale as the block it belongs to.
public final class UndoBlockEntities {
    private UndoBlockEntities() {}

    // Called from each loader's hook around the block entities (Fabric AFTER_ENTITIES, Forge AFTER_BLOCK_ENTITIES),
    // with a camera-relative pose; the level renderer draws what lands in its buffers.
    public static void render(PoseStack poseStack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        float partial = minecraft.getFrameTime();
        MultiBufferSource buffers = minecraft.renderBuffers().bufferSource();
        UndoGrowthClient.INSTANCE.renderBlockEntities(poseStack, buffers, partial);
        UndoGhostRenderer.renderBlockEntities(poseStack, buffers, partial);
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
    static <E extends BlockEntity> void draw(E blockEntity, float scale, PoseStack poseStack, MultiBufferSource buffers, float partial) {
        if (scale <= 0.0F) return;
        Minecraft minecraft = Minecraft.getInstance();
        BlockEntityRenderDispatcher dispatcher = minecraft.getBlockEntityRenderDispatcher();
        BlockEntityRenderer<E> renderer = dispatcher.getRenderer(blockEntity);
        if (renderer == null || minecraft.level == null) return;
        Vec3 cam = minecraft.gameRenderer.getMainCamera().getPosition();
        BlockPos pos = blockEntity.getBlockPos();
        poseStack.pushPose();
        poseStack.translate(pos.getX() - cam.x + 0.5, pos.getY() - cam.y + 0.5, pos.getZ() - cam.z + 0.5);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5, -0.5, -0.5);
        renderer.render(blockEntity, partial, poseStack, buffers, LevelRenderer.getLightColor(minecraft.level, pos), OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
