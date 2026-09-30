package net.ixdarklord.ultimine_addition.client.gui.tooltip;

import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class SkillsRecordTooltip implements TooltipComponent {
    private final NonNullList<ItemStack> items;
    // The record's dye, for its look.
    private final @Nullable DyeColor color;

    public SkillsRecordTooltip(NonNullList<ItemStack> nonNullList, @Nullable DyeColor color) {
        this.items = nonNullList;
        this.color = color;
    }

    public @Nullable DyeColor getColor() {
        return this.color;
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

}
