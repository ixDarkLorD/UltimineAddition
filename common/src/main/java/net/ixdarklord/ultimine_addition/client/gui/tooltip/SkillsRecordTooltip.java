package net.ixdarklord.ultimine_addition.client.gui.tooltip;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class SkillsRecordTooltip implements TooltipComponent {
    private final NonNullList<ItemStack> items;

    public SkillsRecordTooltip(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public record Option(int buttonId, Component textComponent) implements TooltipComponent {}
}
