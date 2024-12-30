package net.ixdarklord.ultimine_addition.mixin.client;

import net.ixdarklord.coolcatlib.api.item.ComponentItem;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
@Mixin(value = GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Shadow public abstract void renderTooltipInternal(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner tooltipPositioner);
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V", at = @At("HEAD"), cancellable = true)
    private void onRenderTooltip(Font font, List<Component> tooltipLines, Optional<TooltipComponent> visualTooltipComponent, int mouseX, int mouseY, CallbackInfo ci) {
        List<ClientTooltipComponent> list = tooltipLines.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        if (visualTooltipComponent.isPresent()) {
            TooltipComponent tooltipComponent = visualTooltipComponent.get();
            if (this.minecraft.screen instanceof AbstractContainerScreen<?> screen && screen.hoveredSlot != null && screen.hoveredSlot.getItem().getItem() instanceof ComponentItem) {
                for (int i = 0; i < tooltipLines.size(); i++) {
                    if (tooltipLines.get(i).getString().equalsIgnoreCase(UltimineAddition.MOD_ID + ".tooltip_image"))
                        list.set(i, ClientTooltipComponent.create(tooltipComponent));
                }

                this.renderTooltipInternal(font, list, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE);
                ci.cancel();
            }
        }
    }
}
