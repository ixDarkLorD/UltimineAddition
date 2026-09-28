package net.ixdarklord.ultimine_addition.mixin.client;

import net.ixdarklord.ultimine_addition.client.renderer.ItemAlpha;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Tints the item atlas blit by the item's alpha; the pipeline is premultiplied, so every channel is scaled.
@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Unique
    private float ua$itemAlpha = 1.0F;

    @Inject(method = "submitBlitFromItemAtlas", at = @At("HEAD"))
    private void UA$captureItemAlpha(GuiItemRenderState itemState, @Coerce Object slotView, CallbackInfo ci) {
        this.ua$itemAlpha = ((ItemAlpha.Holder) (Object) itemState).ua$getAlpha();
    }

    @ModifyArg(method = "submitBlitFromItemAtlas", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/state/gui/BlitRenderState;<init>(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;Lorg/joml/Matrix3x2f;IIIIFFFFILnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V"), index = 11)
    private int UA$fadeItem(int color) {
        float alpha = this.ua$itemAlpha;
        return alpha >= 1.0F ? color : ARGB.colorFromFloat(alpha, alpha, alpha, alpha);
    }
}
