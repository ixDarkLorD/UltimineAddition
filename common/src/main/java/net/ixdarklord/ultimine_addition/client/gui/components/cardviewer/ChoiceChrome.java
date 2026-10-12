package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.coolcatcanvas.api.client.utils.RenderUtils;
import net.ixdarklord.ultimine_addition.client.gui.GuiDraw;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.util.ARGB;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

// What the card viewer's choice panels (a tier's shape, a tier's perk) and its challenge menu share, all in the Skills
// Record's own color: the header with its title, a line under it and the tier as a chip on the right, the footer band,
// the raised plate their choices are drawn on and the sunken well a plate holds its picture in.
final class ChoiceChrome {
    static final int PADDING = 5;
    static final int HEADER = 24;
    static final int FOOTER = 17;
    static final int GOLD = 0xFFFBD25A;
    static final int TITLE = 0xFBF1C1;
    static final int MUTED = 0xFFB4AEA2;
    // How much of a plate's height its lower edge takes: the side of the raised plate, seen from above.
    static final int LIP = 3;
    // Shades of a plate before the record's color is worked in.
    private static final int OUTLINE = 0xFF100F0C, FACE = 0xFF4B463C, FACE_LIT = 0xFF665F51, FACE_OFF = 0xFF38342D, SIDE = 0xFF2A2721, SIDE_LIT = 0xFF3A362E;

    private ChoiceChrome() {
    }

    static int tierColor(MiningSkillCardItem.Tier tier) {
        return switch (tier) {
            case Novice -> 0xFF55FF55;
            case Apprentice -> 0xFF55FFFF;
            case Adept -> 0xFFFF55FF;
            case Mastered -> 0xFFFFAA00;
            default -> 0xFFAAAAAA;
        };
    }

    // A value eased toward its target, for a frame that took this many seconds; it lands once it is close.
    static float approach(float value, float target, float seconds, float rate) {
        float next = value + (target - value) * (1.0F - (float) Math.exp(-rate * seconds));
        return Math.abs(target - next) < 0.01F ? target : next;
    }

    // The panel's body, its header (an item beside the title when there is one, the tier's chip on the right, a line of
    // explanation under the title) and its footer band.
    static void frame(GuiGraphics graphics, CardViewerWidget viewer, Font font, ScreenRectangle b, ItemStack icon, Component title, Component subtitle,
                      MiningSkillCardItem.Tier tier) {
        boolean shadow = viewer.hasTextShadow();
        graphics.fill(b.left(), b.top(), b.right(), b.bottom(), viewer.themed(ChallengeDetailsPanel.PANEL_FILL));
        int headerBottom = b.top() + HEADER - 2;
        graphics.fill(b.left(), b.top(), b.right(), headerBottom, viewer.themed(ChallengeDetailsPanel.FOOTER_FILL));
        graphics.fill(b.left(), headerBottom - 1, b.right(), headerBottom, 0x28000000);
        graphics.fill(b.left(), headerBottom, b.right(), headerBottom + 1, viewer.themed(ChallengeDetailsPanel.FOOTER_EDGE));
        graphics.fill(b.left(), headerBottom + 1, b.right(), headerBottom + 2, 0x20FFFFFF);

        // The tier, as a chip in its color.
        int accent = tierColor(tier);
        Component name = tier.getDisplayName();
        int chipWidth = font.width(name) + 8, chipLeft = b.right() - PADDING - chipWidth, chipTop = b.top() + 2;
        chip(graphics, chipLeft, chipTop, chipWidth, 10, accent);
        GuiDraw.text(graphics, font, name.copy().withStyle(style -> style.withColor(accent & 0xFFFFFF)), chipLeft + 4, chipTop + 1, 0xFFFFFFFF, false);

        int textX = b.left() + PADDING;
        if (!icon.isEmpty()) {
            well(graphics, textX - 1, b.top() + 2, 18, 18);
            graphics.renderItem(icon, textX, b.top() + 3);
            textX += 21;
        }
        CardViewerWidget.drawFittedText(graphics, font, title, width -> title, textX, b.top() + 3, chipLeft - 4 - textX, RenderUtils.textColor(TITLE), shadow);
        // The line under the title has the header's whole width, below the chip; one too long even when shrunk is cut short.
        String plain = subtitle.getString();
        CardViewerWidget.drawFittedText(graphics, font, subtitle, width -> CardViewerWidget.ellipsize(font, plain, width).copy().withStyle(subtitle.getStyle()),
                textX, b.top() + 13, b.right() - PADDING - textX, 0xFFFFFFFF, shadow);

        int footerTop = b.bottom() - FOOTER;
        graphics.fill(b.left(), footerTop, b.right(), b.bottom(), viewer.themed(ChallengeDetailsPanel.FOOTER_FILL));
        graphics.fill(b.left(), footerTop, b.right(), footerTop + 1, viewer.themed(ChallengeDetailsPanel.FOOTER_EDGE));
        graphics.fill(b.left(), footerTop + 1, b.right(), footerTop + 2, 0x28FFFFFF);
    }

    // A small plate in a color: a faint wash of it inside a frame of it, its corners cut.
    static void chip(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, ARGB.multiplyAlpha(color, 0.18F));
        int edge = ARGB.multiplyAlpha(color, 0.75F);
        graphics.fill(x + 1, y, x + width - 1, y + 1, edge);
        graphics.fill(x + 1, y + height - 1, x + width - 1, y + height, edge);
        graphics.fill(x, y + 1, x + 1, y + height - 1, edge);
        graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, edge);
    }

    // A raised plate, in the record's color: its shadow on the panel under it, a dark outline with the corners cut, the
    // face lit along its top and left and shaded along its right and bottom, and under the face the plate's side (its
    // lower LIP rows), which is what makes it stand off the panel. As hover goes to 1 the face lightens and the outline
    // turns gold.
    static void plate(GuiGraphics graphics, CardViewerWidget viewer, int x, int y, int width, int height, float hover, boolean active) {
        int right = x + width, bottom = y + height;
        graphics.fill(x + 2, bottom, right + 1, bottom + 2, 0x58000000);
        graphics.fill(right, y + 3, right + 2, bottom, 0x38000000);

        int outline = ARGB.srgbLerp(hover, viewer.themed(OUTLINE), GOLD);
        graphics.fill(x + 1, y, right - 1, bottom, outline);
        graphics.fill(x, y + 1, right, bottom - 1, outline);

        int faceBottom = bottom - LIP;
        graphics.fill(x + 1, faceBottom, right - 1, bottom - 1, viewer.themed(ARGB.srgbLerp(hover, SIDE, SIDE_LIT)));
        graphics.fill(x + 1, faceBottom, right - 1, faceBottom + 1, 0x50000000);
        graphics.fill(x + 1, y + 1, right - 1, faceBottom, viewer.themed(active ? ARGB.srgbLerp(hover, FACE, FACE_LIT) : FACE_OFF));
        graphics.fill(x + 2, y + 1, right - 2, y + 2, ARGB.color(Math.round(0x30 + 0x30 * hover), 0xFFFFFF));
        graphics.fill(x + 1, y + 2, x + 2, faceBottom - 1, 0x20FFFFFF);
        graphics.fill(right - 2, y + 2, right - 1, faceBottom - 1, 0x30000000);
        graphics.fill(x + 2, faceBottom - 1, right - 2, faceBottom, 0x28000000);
    }

    // A sunken well in a plate: dark, shaded along its top and left and lit along its bottom and right.
    static void well(GuiGraphics graphics, int x, int y, int width, int height) {
        int right = x + width, bottom = y + height;
        graphics.fill(x, y, right, bottom, 0x78000000);
        graphics.fill(x, y, right, y + 1, 0x60000000);
        graphics.fill(x, y + 1, x + 1, bottom, 0x40000000);
        graphics.fill(x + 1, bottom - 1, right, bottom, 0x20FFFFFF);
        graphics.fill(right - 1, y + 1, right, bottom - 1, 0x16FFFFFF);
    }

    // The words on as few lines as fit the width, never split inside a word; with a line limit, what doesn't fit ends
    // in an ellipsis.
    static List<String> wrap(Font font, String text, int maxWidth, int maxLines) {
        List<String> lines = new ArrayList<>();
        String line = "";
        for (String word : text.trim().split("\\s+")) {
            String longer = line.isEmpty() ? word : line + " " + word;
            if (line.isEmpty() || font.width(longer) <= maxWidth) {
                line = longer;
            } else {
                lines.add(line);
                line = word;
            }
        }
        if (!line.isEmpty()) lines.add(line);
        if (maxLines > 0 && lines.size() > maxLines) {
            String last = lines.get(maxLines - 1);
            while (!last.isEmpty() && font.width(last + "...") > maxWidth) last = last.substring(0, last.length() - 1);
            lines = new ArrayList<>(lines.subList(0, maxLines));
            lines.set(maxLines - 1, last.stripTrailing() + "...");
        }
        return lines;
    }
}
