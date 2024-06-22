package net.ixdarklord.ultimine_addition.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.ixdarklord.ultimine_addition.client.event.impl.ClientTooltipComponentRegister;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
@Mixin(value = Screen.class)
public abstract class ScreenMixin {
    @Shadow protected Minecraft minecraft;
    @Shadow public abstract void renderTooltipInternal(PoseStack poseStack, List<ClientTooltipComponent> list, int i, int j);

    @Inject(method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;Ljava/util/Optional;II)V", at = @At("HEAD"), cancellable = true)
    private void onRenderTooltip(PoseStack poseStack, List<Component> tooltips, Optional<TooltipComponent> visualTooltipComponent, int mouseX, int mouseY, CallbackInfo ci) {
        List<ClientTooltipComponent> list = tooltips.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        if (visualTooltipComponent.isPresent()) {
            TooltipComponent tooltipComponent = visualTooltipComponent.get();
            ClientTooltipComponent component = ClientTooltipComponentRegister.EVENT.invoker().getComponent(tooltipComponent);

            if (component != null && this.minecraft.screen instanceof AbstractContainerScreen<?> screen) {
                boolean b = false;
                for (int i = 0; i < tooltips.size(); i++) {
                    if (tooltips.get(i).getString().equalsIgnoreCase(UltimineAddition.MOD_ID + ".tooltip_image")) {
                        list.set(i, component);
                        b = true;
                    }
                }
                if (!b) list.add(component);
                this.renderTooltipInternal(poseStack, list, mouseX, mouseY);
                ci.cancel();
            }
        }
    }
}
