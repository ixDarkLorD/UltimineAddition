package net.ixdarklord.ultimine_addition.mixin.client;

import net.ixdarklord.ultimine_addition.client.gui.GuiDraw;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

/**
 * Places an item's tooltip image at the "ultimine_addition.tooltip_image" placeholder line
 * instead of vanilla's fixed position (second line).
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
@Mixin(value = GuiGraphics.class)
abstract class GuiGraphicsMixin {
    // (Forge 47 draws container slot tooltips through its own overload: see the forge MixinForgeGuiGraphics.)
    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V", at = @At("HEAD"), cancellable = true)
    private void UA$Inject$onRenderTooltip(Font font, List<Component> tooltipLines, Optional<TooltipComponent> visualTooltipComponent, int mouseX, int mouseY, CallbackInfo ci) {
        if (GuiDraw.renderTooltipWithImageLine((GuiGraphics) (Object) this, font, tooltipLines, visualTooltipComponent, mouseX, mouseY)) ci.cancel();
    }
}
