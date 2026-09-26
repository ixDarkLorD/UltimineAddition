package net.ixdarklord.ultimine_addition.mixin.client;

import net.ixdarklord.coolcatlib.api.item.ComponentItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Places an item's tooltip image at the "ultimine_addition.tooltip_image" placeholder line
 * instead of vanilla's fixed position (second line).
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
@Mixin(value = GuiGraphicsExtractor.class)
abstract class GuiGraphicsMixin {
    @Shadow @Final private Minecraft minecraft;

    @Shadow
    protected abstract void setTooltipForNextFrameInternal(Font font, List<ClientTooltipComponent> lines, int xo, int yo, ClientTooltipPositioner positioner, @Nullable Identifier style, boolean replaceExisting);

    @Inject(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V", at = @At("HEAD"), cancellable = true)
    private void UA$Inject$onSetTooltip(Font font, List<Component> tooltipLines, Optional<TooltipComponent> visualTooltipComponent, int mouseX, int mouseY, @Nullable Identifier style, CallbackInfo ci) {
        if (visualTooltipComponent.isEmpty()) return;
        if (!(this.minecraft.screen instanceof AbstractContainerScreen<?> screen) || screen.hoveredSlot == null || !(screen.hoveredSlot.getItem().getItem() instanceof ComponentItem)) return;

        List<ClientTooltipComponent> list = tooltipLines.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        TooltipComponent tooltipComponent = visualTooltipComponent.get();
        for (int i = 0; i < tooltipLines.size(); i++) {
            if (tooltipLines.get(i).getString().equalsIgnoreCase(FTBUltimineAddition.MOD_ID + ".tooltip_image"))
                list.set(i, ClientTooltipComponent.create(tooltipComponent));
        }

        this.setTooltipForNextFrameInternal(font, list, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, style, false);
        ci.cancel();
    }
}
