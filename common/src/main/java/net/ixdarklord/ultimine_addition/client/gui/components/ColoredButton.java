package net.ixdarklord.ultimine_addition.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.ixdarklord.coolcatlib.api.client.gui.components.ColorableImageButton;
import net.ixdarklord.coolcatlib.api.client.gui.components.widgets.WidgetSprites;
import net.ixdarklord.coolcatlib.api.client.utils.NineSliceInfo;
import net.ixdarklord.coolcatlib.api.client.utils.NineSliceInfo.SliceBounds;
import net.ixdarklord.coolcatlib.api.client.utils.NineSliceInfo.TextureInfo;
import net.ixdarklord.coolcatlib.api.client.utils.NineSliceInfo.TextureRegion;
import net.ixdarklord.coolcatlib.api.client.utils.RenderUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ColoredButton extends ColorableImageButton {
    private final @Nullable Consumer<TooltipInfo> tooltipInfoConsumer;
    private final int sliceSize;

    public ColoredButton(int x, int y, int width, int height, WidgetSprites sprites, int textureWidth, int textureHeight, Button.OnPress onPress, Component component) {
        this(x, y, width, height, 1, sprites, textureWidth, textureHeight, onPress, component);
    }

    public ColoredButton(int x, int y, int width, int height, int sliceSize, WidgetSprites sprites, int textureWidth, int textureHeight, Button.OnPress onPress, Component component) {
        this(x, y, width, height, sliceSize, sprites, textureWidth, textureHeight, onPress, component, null);
    }

    public ColoredButton(int x, int y, int width, int height, WidgetSprites sprites, int textureWidth, int textureHeight, Button.OnPress onPress, Component component, Consumer<TooltipInfo> consumer) {
        this(x, y, width, height, 1, sprites, textureWidth, textureHeight, onPress, component, consumer);
    }

    public ColoredButton(int x, int y, int width, int height, int sliceSize, WidgetSprites sprites, int textureWidth, int textureHeight, Button.OnPress onPress, Component component, @Nullable Consumer<TooltipInfo> consumer) {
        super(x, y, width, height, 0, 0, 0, sprites, textureWidth, textureHeight, onPress, component);
        this.sliceSize = sliceSize;
        this.tooltipInfoConsumer = consumer;
    }

    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        TextColor color = this.getMessage().getStyle().getColor();
        color = color == null ? TextColor.fromRgb(Color.WHITE.getRGB()) : color;
        this.renderString(guiGraphics, Minecraft.getInstance().font, color.getValue());
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    public void renderTexture(@NotNull GuiGraphics guiGraphics, @NotNull ResourceLocation texture, int x, int y, int uOffset, int vOffset, int textureDifference, int width, int height, int textureWidth, int textureHeight) {
        if (this.color != null) {
            RenderSystem.setShaderColor((float) this.color.getRed() / 255.0F, (float) this.color.getGreen() / 255.0F, (float) this.color.getBlue() / 255.0F, (float) this.color.getAlpha() / 255.0F);
        }

        RenderSystem.enableDepthTest();
        this.renderButton(guiGraphics, this.sprites.get(this.isActive(), this.isHoveredOrFocused()), x, y, width, height, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected void renderButton(@NotNull GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height, int textureWidth, int textureHeight) {
        NineSliceInfo.TextureInfo textureInfo = TextureInfo.of(texture, textureWidth, textureHeight);
        NineSliceInfo.SliceBounds slice = SliceBounds.uniform(this.sliceSize);
        NineSliceInfo.TextureRegion region = TextureRegion.region(textureWidth, textureHeight);
        RenderUtils.blitNineSliced(guiGraphics, textureInfo, x, y, width, height, slice, region);
    }

    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.isHovered() && this.isActive() && this.tooltipInfoConsumer != null) {
            MutableComponent component = this.getTooltipInfo().component.copy();
            Optional<TooltipComponent> tooltipComponent = this.getTooltipInfo().getTooltipComponent();
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            int y = mouseY + 9 - tooltipComponent.map((c) -> ClientTooltipComponent.create(c).getHeight() / 2).orElse(0);
            guiGraphics.renderTooltip(Minecraft.getInstance().font, List.of(component.withStyle(ChatFormatting.ITALIC)), tooltipComponent, mouseX, y);
        }
    }

    public TooltipInfo getTooltipInfo() {
        TooltipInfo tooltipInfo = new TooltipInfo();
        if (this.tooltipInfoConsumer != null) {
            this.tooltipInfoConsumer.accept(tooltipInfo);
        }
        return tooltipInfo;
    }
}
