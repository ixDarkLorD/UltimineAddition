package net.ixdarklord.ultimine_addition.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public abstract class UAItemRenderer extends BlockEntityWithoutLevelRenderer {

    public UAItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public abstract void render(ItemStack itemStack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay);

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        this.render(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
    }
}
