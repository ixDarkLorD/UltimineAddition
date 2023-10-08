package net.ixdarklord.ultimine_addition.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.ixdarklord.ultimine_addition.client.renderer.item.IItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockEntityWithoutLevelRenderer.class, priority = 1600)
public abstract class BlockEntityWithoutLevelRendererMixin {

    @Inject(method = "renderByItem", at = @At(value = "HEAD"))
    private void onRender(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        if (itemStack.getItem() instanceof IItemRenderer itemRenderer) {
            itemRenderer.createItemRenderer().renderByItem(itemStack, transformType, poseStack, buffer, light, overlay);
        }
    }
}
