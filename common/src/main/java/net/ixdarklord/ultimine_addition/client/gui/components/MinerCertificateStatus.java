package net.ixdarklord.ultimine_addition.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.coolcatlib.api.util.ChatFormattingUtils;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class MinerCertificateStatus {
    public static final MinerCertificateStatus INSTANCE = new MinerCertificateStatus();

    private MinerCertificateStatus() {}

    public void render(GuiGraphics guiGraphics, DeltaTracker ignored) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        LocalPlayer player = minecraft.player;
        if (player == null) return;

        ItemStack stack = ItemUtils.findItemInHand(player, ModItems.MINER_CERTIFICATE);
        if (stack.isEmpty()) return;
        MinerCertificateData data = MinerCertificateData.loadData(stack);
        Optional<MinerCertificateData.Legacy> optional = data.getLegacy();
        if (data.isAccomplished() || optional.isEmpty()) return;

        MinerCertificateData.Legacy legacy = optional.get();
        Component values = Component.literal(String.valueOf(legacy.getMinedBlocks())).withStyle(ChatFormattingUtils.get3LevelChatFormatting(legacy.getMinedBlocks(), legacy.getRequiredAmount()))
                .append(Component.literal("/"))
                .append(Component.literal(String.valueOf(legacy.getRequiredAmount())));
        MutableComponent component = Component.translatable("tooltip.ultimine_addition.certificate.legacy.quest", values);

        int textWidth = font.width(component);
        int x = (guiGraphics.guiWidth() - textWidth) / 2;
        int y = guiGraphics.guiHeight();
        y -= 66;

        if (minecraft.gui.overlayMessageTime > 0)
            y -= 22;
        else if (minecraft.gui.toolHighlightTimer > 0)
            y -= 8;

        ScreenRectangle rectangle = new ScreenRectangle(x, y, textWidth, 9);
        TooltipRenderUtil.renderTooltipBackground(guiGraphics, rectangle.left(), rectangle.top(), rectangle.width(), rectangle.height(), 0);
        guiGraphics.drawString(font, component, x, y, Color.WHITE.getRGB());
    }
}
