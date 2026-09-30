package net.ixdarklord.ultimine_addition.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.ixdarklord.ultimine_addition.util.ARGB;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import net.ixdarklord.coolcatcore.internal.config.client.gui.style.GuiSprites;
import org.lwjgl.opengl.GL11;

import net.ixdarklord.coolcatcore.api.item.ComponentItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The 26.1 GUI calls this mod's screens are written with, on 1.20.1's {@link GuiGraphics}:
 * <ul>
 *     <li>GUI sprites: 1.20.1 has no GUI sprite atlas, so a sprite id ({@code ns:path}) is the texture
 *     {@code ns:textures/gui/sprites/path.png}, scaled as its {@code .mcmeta} says (stretch, tile, nine_slice), as on
 *     newer versions. Drawn by CoolCatLib: Core's sprite loader (the one its config screens use), which also takes a
 *     full {@code .png} texture path.</li>
 *     <li>Blits take an ARGB tint (1.20.1 tints through the shader color, set around the call here).</li>
 *     <li>Tooltips are drawn once the screen is done ({@link #renderTooltip}), as on 26.1: 1.20.1 draws them right away,
 *     where a scissored area (scroll panels, the card viewer) would cut them off and later strata cover them.</li>
 *     <li>{@link #nextStratum} puts what's drawn next over everything before, as on 26.1: 1.20.1 orders the GUI by depth,
 *     with items at z 150 and more drawing over later fills and text, so it clears the depth buffer.</li>
 *     <li>Text whose color has no alpha isn't drawn, as on 26.1 (1.20.1 would draw it opaque), so faded text disappears
 *     instead of popping back. Plain RGB colors must be made opaque ({@code ARGB.opaque}, {@code RenderUtils.textColor}),
 *     as on 26.1.</li>
 * </ul>
 */
public final class GuiDraw {
    private GuiDraw() {}

    private static @Nullable Runnable tooltip;

    private static boolean invisible(int color) {
        return (color & 0xFC000000) == 0;
    }

    public static int text(GuiGraphics graphics, Font font, @Nullable String text, int x, int y, int color) {
        return text(graphics, font, text, x, y, color, true);
    }

    public static int text(GuiGraphics graphics, Font font, @Nullable String text, int x, int y, int color, boolean dropShadow) {
        if (text == null || invisible(color)) return x;
        return graphics.drawString(font, text, x, y, color, dropShadow);
    }

    public static int text(GuiGraphics graphics, Font font, Component text, int x, int y, int color) {
        return text(graphics, font, text, x, y, color, true);
    }

    public static int text(GuiGraphics graphics, Font font, Component text, int x, int y, int color, boolean dropShadow) {
        if (invisible(color)) return x;
        return graphics.drawString(font, text, x, y, color, dropShadow);
    }

    public static int text(GuiGraphics graphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        return text(graphics, font, text, x, y, color, true);
    }

    public static int text(GuiGraphics graphics, Font font, FormattedCharSequence text, int x, int y, int color, boolean dropShadow) {
        if (invisible(color)) return x;
        return graphics.drawString(font, text, x, y, color, dropShadow);
    }

    public static void centeredText(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        text(graphics, font, text, x - font.width(text) / 2, y, color);
    }

    public static void centeredText(GuiGraphics graphics, Font font, Component text, int x, int y, int color) {
        FormattedCharSequence sequence = text.getVisualOrderText();
        text(graphics, font, sequence, x - font.width(sequence) / 2, y, color);
    }

    public static void centeredText(GuiGraphics graphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        text(graphics, font, text, x - font.width(text) / 2, y, color);
    }

    public static void blitSprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        blitSprite(graphics, sprite, x, y, width, height, -1);
    }

    public static void blitSprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height, int color) {
        if (ARGB.alpha(color) == 0) return;
        GuiSprites.blitSprite(graphics, sprite, x, y, width, height, color);
    }

    /** The texture of a GUI sprite id ({@code ns:textures/gui/sprites/<path>.png}), for Canvas's WidgetSprites. */
    public static ResourceLocation spriteTexture(ResourceLocation sprite) {
        return GuiSprites.texture(sprite);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        blit(graphics, texture, x, y, u, v, width, height, textureWidth, textureHeight, -1);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color) {
        tinted(graphics, color, () -> graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight));
    }

    public static void tooltip(GuiGraphics graphics, Font font, Component text, int mouseX, int mouseY) {
        tooltipLines(graphics, font, List.of(text.getVisualOrderText()), mouseX, mouseY);
    }

    public static void tooltip(GuiGraphics graphics, Font font, List<Component> lines, int mouseX, int mouseY) {
        tooltipLines(graphics, font, lines.stream().map(Component::getVisualOrderText).toList(), mouseX, mouseY);
    }

    // Kept for renderTooltip, which the mod's screens call once they're drawn (the last one set wins, as on 26.1).
    public static void tooltipLines(GuiGraphics graphics, Font font, List<FormattedCharSequence> lines, int mouseX, int mouseY) {
        tooltip = () -> graphics.renderTooltip(font, lines, mouseX, mouseY);
    }

    /**
     * Draws an item tooltip with its image at the "ultimine_addition.tooltip_image" placeholder line instead of vanilla's
     * fixed position (second line), for the mod's items in a container; false when it doesn't apply.
     */
    public static boolean renderTooltipWithImageLine(GuiGraphics graphics, Font font, List<Component> tooltipLines, Optional<TooltipComponent> image, int mouseX, int mouseY) {
        if (image.isEmpty()) return false;
        if (!(Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) || screen.hoveredSlot == null || !(screen.hoveredSlot.getItem().getItem() instanceof ComponentItem)) return false;

        List<ClientTooltipComponent> list = tooltipLines.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        for (int i = 0; i < tooltipLines.size(); i++) {
            if (tooltipLines.get(i).getString().equalsIgnoreCase(FTBUltimineAddition.MOD_ID + ".tooltip_image"))
                list.set(i, ClientTooltipComponent.create(image.get()));
        }

        graphics.renderTooltipInternal(font, list, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE);
        return true;
    }

    public static void renderTooltip(GuiGraphics graphics) {
        Runnable pending = tooltip;
        tooltip = null;
        if (pending == null) return;
        nextStratum(graphics);
        pending.run();
    }

    /** 26.1's GuiGraphicsExtractor.nextStratum: what's drawn next covers everything drawn before (within the scissor). */
    public static void nextStratum(GuiGraphics graphics) {
        graphics.flush();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
    }

    /** Runs blits tinted by an ARGB color (with blending, so its alpha fades them). */
    public static void tinted(GuiGraphics graphics, int color, Runnable draw) {
        if (ARGB.alpha(color) == 0) return;
        RenderSystem.enableBlend();
        graphics.setColor(ARGB.redFloat(color), ARGB.greenFloat(color), ARGB.blueFloat(color), ARGB.alphaFloat(color));
        try {
            draw.run();
        } finally {
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
