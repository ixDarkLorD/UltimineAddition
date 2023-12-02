package net.ixdarklord.ultimine_addition.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import net.ixdarklord.coolcat_lib.common.item.ComponentItem;
import net.ixdarklord.ultimine_addition.client.event.impl.ClientTooltipComponentRegister;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
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
public abstract class ScreenMixin {
    @Shadow protected Minecraft minecraft;
    @Shadow public abstract void renderTooltipInternal(PoseStack poseStack, List<ClientTooltipComponent> list, int i, int j);
    @Shadow public abstract List<Component> getTooltipFromItem(ItemStack itemStack);

    @SuppressWarnings({"AddedMixinMembersNamePattern", "ConstantValue"})
    public void renderTooltip(PoseStack poseStack, List<Component> tooltipLines, Optional<TooltipComponent> optional, int i, int j) {
        List<ClientTooltipComponent> list = tooltipLines.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        optional.ifPresent((tooltipComponent) -> {
            ClientTooltipComponent component = ClientTooltipComponentRegister.EVENT.invoker().getComponent(tooltipComponent);
            if (component == null) {
                component = ClientTooltipComponent.create(tooltipComponent);
            }

            if (this.minecraft.screen instanceof AbstractContainerScreen<?> screen && screen.hoveredSlot != null && screen.hoveredSlot.getItem().getItem() instanceof ComponentItem) {
                ItemStack stack = screen.hoveredSlot.getItem();
                if (this.minecraft.options.advancedItemTooltips) {
                    int line;
                    if (stack.hasTag()) {
                        line = this.getTooltipFromItem(stack).size()-2 - ((Platform.isFabric() && ServicePlatform.SlotAPI.isModLoaded()) ? 1 : 0);
                    } else {
                        line = this.getTooltipFromItem(stack).size()-1 - ((Platform.isFabric() && ServicePlatform.SlotAPI.isModLoaded()) ? 1 : 0);
                    }
                    list.add(line, component);
                }  else {
                    if (Platform.isFabric() && ServicePlatform.SlotAPI.isModLoaded()) {
                        int line;
                        if (stack.hasTag()) {
                            line = this.getTooltipFromItem(stack).size()-1;
                        } else {
                            line = this.getTooltipFromItem(stack).size();
                        }
                        list.add(line, component);
                    } else list.add(component);
                }
            } else list.add(1, component);
        });
        this.renderTooltipInternal(poseStack, list, i, j);
    }
}
