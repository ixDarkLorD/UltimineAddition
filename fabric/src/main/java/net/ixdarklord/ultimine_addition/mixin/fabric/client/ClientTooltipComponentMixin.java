package net.ixdarklord.ultimine_addition.mixin.fabric.client;

import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {
    @Inject(at = @At("HEAD"), method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;", cancellable = true)
    private static void onCreate(TooltipComponent tooltipComponent, CallbackInfoReturnable<ClientTooltipComponent> cir) {
        ClientTooltipComponent component = TooltipComponentCallback.EVENT.invoker().getComponent(tooltipComponent);
        if (component != null) {
            cir.setReturnValue(component);
        }
    }
}
