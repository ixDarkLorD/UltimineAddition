package net.ixdarklord.ultimine_addition.mixin.forge.client;

import net.ixdarklord.ultimine_addition.client.gui.GuiDraw;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

// Forge 47 draws item tooltips in containers through its own renderTooltip overload (with the stack), which the common
// GuiGraphicsMixin doesn't see: the tooltip image goes at its placeholder line here too.
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
@Mixin(GuiGraphics.class)
abstract class MixinForgeGuiGraphics {
    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void UA$Inject$onRenderStackTooltip(Font font, List<Component> tooltipLines, Optional<TooltipComponent> image, ItemStack stack, int mouseX, int mouseY, CallbackInfo ci) {
        if (GuiDraw.renderTooltipWithImageLine((GuiGraphics) (Object) this, font, tooltipLines, image, mouseX, mouseY)) ci.cancel();
    }
}
