package net.ixdarklord.ultimine_addition.mixin.client;

import dev.architectury.platform.Platform;
import net.ixdarklord.coolcat_lib.common.item.ComponentItem;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"MissingUnique", "OptionalUsedAsFieldOrParameterType", "unused"})
@Mixin(value = GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Shadow public abstract void renderTooltipInternal(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner tooltipPositioner);
    @Shadow @Final private Minecraft minecraft;

    @SuppressWarnings({"ConstantValue"})
    public void renderTooltip(Font font, List<Component> tooltipLines, Optional<TooltipComponent> visualTooltipComponent, int mouseX, int mouseY) {
        List<ClientTooltipComponent> list = tooltipLines.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        visualTooltipComponent.ifPresent((tooltipComponent) -> {
            if (this.minecraft.screen instanceof AbstractContainerScreen<?> screen && screen.hoveredSlot != null && screen.hoveredSlot.getItem().getItem() instanceof ComponentItem) {
                ItemStack stack = screen.hoveredSlot.getItem();
                if (this.minecraft.options.advancedItemTooltips) {
                    int line;
                    if (stack.hasTag()) {
                        line = Screen.getTooltipFromItem(this.minecraft, stack).size()-2 - ((Platform.isFabric() && ServicePlatform.SlotAPI.isModLoaded()) ? 1 : 0);
                    } else {
                        line = Screen.getTooltipFromItem(this.minecraft, stack).size()-1 - ((Platform.isFabric() && ServicePlatform.SlotAPI.isModLoaded()) ? 1 : 0);
                    }
                    list.add(line, ClientTooltipComponent.create(tooltipComponent));
                } else {
                    if (Platform.isFabric() && ServicePlatform.SlotAPI.isModLoaded()) {
                        int line;
                        if (stack.hasTag()) {
                            line = Screen.getTooltipFromItem(this.minecraft, stack).size()-1;
                        } else {
                            line = Screen.getTooltipFromItem(this.minecraft, stack).size();
                        }
                        list.add(line, ClientTooltipComponent.create(tooltipComponent));
                    } else list.add(ClientTooltipComponent.create(tooltipComponent));
                }
            } else list.add(1, ClientTooltipComponent.create(tooltipComponent));
        });
        this.renderTooltipInternal(font, list, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE);
    }
}
