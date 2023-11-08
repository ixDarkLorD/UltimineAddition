package net.ixdarklord.ultimine_addition.mixin.fabric.client;

import net.ixdarklord.ultimine_addition.client.event.impl.ClientHudEvent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Gui.class)
public class GuiMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHotbar(FLnet/minecraft/client/gui/GuiGraphics;)V"))
    private void inject$onRender(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        ClientHudEvent.RENDER_PRE.invoker().renderHud(guiGraphics, partialTick);
    }
}
