package net.ixdarklord.ultimine_addition.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.coolcat_lib.util.MathUtils;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

import static net.ixdarklord.ultimine_addition.client.handler.ModelHandler.*;

@Environment(EnvType.CLIENT)
public class MiningSkillCardItemRenderer extends UAItemRenderer {
    @Override
    public void render(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        var data = new MiningSkillCardData().loadData(stack);
        ModelResourceLocation modelLocation = switch (data.getTier()) {
            case Unlearned -> UNLEARNED_ID;
            case Novice -> TIER_1_ID;
            case Apprentice -> TIER_2_ID;
            case Adept -> TIER_3_ID;
            case Mastered -> MASTERED_ID;
        };
        BakedModel base = renderer.getItemModelShaper().getModelManager().getModel(modelLocation);

        poseStack.pushPose();
        if (transformType == ItemTransforms.TransformType.FIXED) {
            poseStack.translate(1, 0, 1);
            float scale = 1.0F;
            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(180));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(180));
        } else if (transformType == ItemTransforms.TransformType.GUI){
            float scale = 1.0F;
            poseStack.scale(scale, scale, scale);
        } else if (transformType == ItemTransforms.TransformType.GROUND || transformType == ItemTransforms.TransformType.HEAD) {
            poseStack.translate(1, 1, 0);
            float scale = 0.5F;
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-1.5F, -0.5F, 0.5F);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
        } else {
            poseStack.translate(1, 1, 0);
            float scale = 0.5F;
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-1.5F, -1.0F, 0.5F);
        }

        renderer.renderModelLists(base, stack, light, overlay, poseStack, ItemRenderer.getFoilBufferDirect(bufferSource, ItemBlockRenderTypes.getRenderType(stack, true), true, false));
        bufferSource.endBatch();
        poseStack.popPose();

        if (stack.getItem() instanceof MiningSkillCardItem) {
            this.renderDisplayItem(stack, transformType, poseStack, bufferSource, light, overlay);
        }
    }

    private void renderDisplayItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int light, int overlay) {
        int tickCount = Objects.requireNonNull(Minecraft.getInstance().player).tickCount;
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        var data = new MiningSkillCardData().loadData(stack);
        
        poseStack.pushPose();
        if (transformType == ItemTransforms.TransformType.FIXED) {
            poseStack.translate(1, 0, 1);
            float scale = 0.5F;
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.1F, 0.2F, -0.58F);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(180));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(180));
        } else if (transformType == ItemTransforms.TransformType.GUI){
            float scale = 0.5F;
            poseStack.scale(scale, scale, scale);
            poseStack.translate(0.1F, 0.2F, 0.58F);
        } else if (transformType == ItemTransforms.TransformType.HEAD) {
            poseStack.translate(1, 1, 0);
            float scale = 1.0F;
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.5F, -0.5F, 0.5F);
        } else if (transformType == ItemTransforms.TransformType.GROUND) {
            poseStack.translate(1, 1, 0);
            float scale = 0.5F;
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-1.5F, -0.75F, 0.5F);
        } else {
            poseStack.translate(1, 1, 0);
            float scale = 0.25F;
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-2.9F, -1.8F, 1.56F);
        }
        if (transformType != ItemTransforms.TransformType.GUI) {
            float animScale = MathUtils.cycledBetweenValues(0.95F, 1.0F, 0.8F, tickCount / 20.0F, false);
            poseStack.scale(animScale, animScale, animScale);
        }

        BakedModel base = renderer.getItemModelShaper().getItemModel(data.getDisplayItem());
        renderer.renderModelLists(base, data.getDisplayItem(), light, overlay, poseStack, ItemRenderer.getFoilBufferDirect(bufferSource, ItemBlockRenderTypes.getRenderType(stack, true), true, false));
        bufferSource.endBatch();
        poseStack.popPose();
    }
}
