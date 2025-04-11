package net.ixdarklord.ultimine_addition.client.gui.components;

import net.ixdarklord.coolcatlib.api.client.gui.components.ColorableImageButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ColoredButton extends ColorableImageButton {
    private final Consumer<TooltipInfo> tooltipInfoConsumer;

    public ColoredButton(int x, int y, int width, int height, WidgetSprites buttonSprites, OnPress onPress, Component component, Consumer<TooltipInfo> consumer) {
        super(x, y, width, height, buttonSprites, onPress, component);
        this.tooltipInfoConsumer = consumer;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!this.isHovered()) return;
        final MutableComponent component = Component.literal("➤ ").withStyle(ChatFormatting.DARK_GRAY).append(getTooltipInfo().component);
        Optional<TooltipComponent> tooltipComponent = getTooltipInfo().getTooltipComponent();

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int y = mouseY + 9 - tooltipComponent.map(c -> ClientTooltipComponent.create(c).getHeight() / 2).orElse(0);
        guiGraphics.renderTooltip(Minecraft.getInstance().font, List.of(component.withStyle(ChatFormatting.ITALIC)), tooltipComponent, mouseX, y);
    }

    public TooltipInfo getTooltipInfo() {
        TooltipInfo tooltipInfo = new TooltipInfo();
        tooltipInfoConsumer.accept(tooltipInfo);
        return tooltipInfo;
    }
}