package net.ixdarklord.ultimine_addition.mixin.client;

import net.ixdarklord.ultimine_addition.client.renderer.ItemAlpha;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Tints the item atlas blit by the item's alpha; the pipeline is premultiplied, so every channel is scaled.
// Also keeps a scaled item (e.g. in the zoomed card viewer) from showing a sliver of its atlas neighbour: its atlas
// rectangle is pulled in by half a texel, so nearest sampling at its edges stays inside its own cell.
@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Unique
    private float ua$insetU, ua$insetV;
    @Unique
    private static final String BLIT_INIT = "Lnet/minecraft/client/renderer/state/gui/BlitRenderState;<init>(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;Lorg/joml/Matrix3x2f;IIIIFFFFILnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V";
    @Unique
    private float ua$itemAlpha = 1.0F;
    @Unique
    private int ua$itemTint = ItemAlpha.NO_TINT;

    @Inject(method = "submitBlitFromItemAtlas", at = @At("HEAD"))
    private void UA$captureItemAlpha(GuiItemRenderState itemState, @Coerce Object slotView, CallbackInfo ci) {
        this.ua$itemAlpha = ((ItemAlpha.Holder) (Object) itemState).ua$getAlpha();
        this.ua$itemTint = ((ItemAlpha.Holder) (Object) itemState).ua$getTint();
        Matrix3x2f pose = itemState.pose();
        boolean scaled = Math.abs(pose.m00() - 1.0F) > 1.0E-4F || Math.abs(pose.m11() - 1.0F) > 1.0E-4F || pose.m01() != 0.0F || pose.m10() != 0.0F;
        if (scaled && slotView instanceof GuiItemAtlas.SlotView view) {
            float texels = 16.0F * (float) Minecraft.getInstance().getWindow().getGuiScale();
            this.ua$insetU = Math.abs(view.u1() - view.u0()) * 0.5F / texels;
            this.ua$insetV = Math.abs(view.v1() - view.v0()) * 0.5F / texels;
        } else {
            this.ua$insetU = this.ua$insetV = 0.0F;
        }
    }

    @ModifyArg(method = "submitBlitFromItemAtlas", at = @At(value = "INVOKE", target = BLIT_INIT), index = 7)
    private float UA$insetU0(float u) {
        return u + this.ua$insetU;
    }

    @ModifyArg(method = "submitBlitFromItemAtlas", at = @At(value = "INVOKE", target = BLIT_INIT), index = 8)
    private float UA$insetU1(float u) {
        return u - this.ua$insetU;
    }

    @ModifyArg(method = "submitBlitFromItemAtlas", at = @At(value = "INVOKE", target = BLIT_INIT), index = 9)
    private float UA$insetV0(float v) {
        return v + this.ua$insetV;
    }

    @ModifyArg(method = "submitBlitFromItemAtlas", at = @At(value = "INVOKE", target = BLIT_INIT), index = 10)
    private float UA$insetV1(float v) {
        return v - this.ua$insetV;
    }

    @ModifyArg(method = "submitBlitFromItemAtlas", at = @At(value = "INVOKE", target = BLIT_INIT), index = 11)
    private int UA$fadeItem(int color) {
        float alpha = this.ua$itemAlpha;
        int tint = this.ua$itemTint;
        if (alpha >= 1.0F && tint == ItemAlpha.NO_TINT) return color;
        // The item atlas is premultiplied: the colour carries the alpha in every channel, times the tint.
        return ARGB.colorFromFloat(alpha, alpha * ARGB.red(tint) / 255.0F, alpha * ARGB.green(tint) / 255.0F, alpha * ARGB.blue(tint) / 255.0F);
    }
}
