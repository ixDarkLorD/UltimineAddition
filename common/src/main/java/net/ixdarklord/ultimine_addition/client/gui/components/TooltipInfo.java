package net.ixdarklord.ultimine_addition.client.gui.components;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TooltipInfo {
    public Component component;
    public @Nullable TooltipComponent tooltipComponent;

    public TooltipInfo() {
        this.component = Component.empty();
    }

    public Optional<TooltipComponent> getTooltipComponent() {
        return Optional.ofNullable(tooltipComponent);
    }
}