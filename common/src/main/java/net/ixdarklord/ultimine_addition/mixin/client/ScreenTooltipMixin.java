package net.ixdarklord.ultimine_addition.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.ixdarklord.coolcat_lib.common.item.ComponentItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"MissingUnique", "OptionalUsedAsFieldOrParameterType", "unused"})
@Mixin(value = Screen.class)
public abstract class ScreenTooltipMixin {
    @Shadow protected Minecraft minecraft;
    @Shadow public abstract void renderTooltipInternal(PoseStack poseStack, List<ClientTooltipComponent> list, int i, int j);
    @Shadow public abstract List<Component> getTooltipFromItem(ItemStack itemStack);

    @SuppressWarnings("AddedMixinMembersNamePattern")
    public void renderTooltip(PoseStack poseStack, List<Component> list, Optional<TooltipComponent> optional, int i, int j) {
        List<ClientTooltipComponent> list2 = list.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        optional.ifPresent((tooltipComponent) -> {
            if (this.minecraft.screen instanceof AbstractContainerScreen<?> screen && screen.hoveredSlot != null && screen.hoveredSlot.getItem().getItem() instanceof ComponentItem) {
                ItemStack stack = screen.hoveredSlot.getItem();
                int line = 0;
                if (this.minecraft.options.advancedItemTooltips) {
                    if (stack.hasTag()) {
                        line = this.getTooltipFromItem(stack).size()-2;
                    } else line = this.getTooltipFromItem(stack).size()-1;
                    list2.add(line, ClientTooltipComponent.create(tooltipComponent));
                }
                if (line == 0) list2.add(ClientTooltipComponent.create(tooltipComponent));
            } else list2.add(1, ClientTooltipComponent.create(tooltipComponent));
        });
        this.renderTooltipInternal(poseStack, list2, i, j);
    }
}
