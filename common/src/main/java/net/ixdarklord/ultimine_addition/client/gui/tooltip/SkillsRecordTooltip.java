package net.ixdarklord.ultimine_addition.client.gui.tooltip;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record SkillsRecordTooltip(NonNullList<ItemStack> items) implements TooltipComponent {
    public record Option(int buttonId, Component textComponent) implements TooltipComponent {
    }
}
