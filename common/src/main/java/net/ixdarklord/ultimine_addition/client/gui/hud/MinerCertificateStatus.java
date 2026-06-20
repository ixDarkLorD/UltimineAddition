package net.ixdarklord.ultimine_addition.client.gui.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.coolcatlib.api.utils.ChatFormattingUtils;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
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

    private MinerCertificateStatus() {
    }

    public void render(GuiGraphics guiGraphics, float ignored) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        LocalPlayer player = minecraft.player;
        if (player != null) {
            ItemStack stack = ItemUtils.findItemInHand(player, Registration.MINER_CERTIFICATE.get());
            if (!stack.isEmpty()) {
                MinerCertificateData data = MinerCertificateData.load(stack);
                Optional<MinerCertificateData.Legacy> optional = data.getLegacy();
                if (!data.isAccomplished() && optional.isPresent()) {
                    MinerCertificateData.Legacy legacy = optional.get();
                    Component values = Component.literal(String.valueOf(legacy.getMinedBlocks())).withStyle(ChatFormattingUtils.getProgressColor(legacy.getMinedBlocks(), legacy.getRequiredAmount())).append(Component.literal("/")).append(Component.literal(String.valueOf(legacy.getRequiredAmount())));
                    MutableComponent component = Component.translatable("tooltip.ultimine_addition.certificate.legacy.quest", values);
                    int textWidth = font.width(component);
                    int x = (guiGraphics.guiWidth() - textWidth) / 2;
                    int y = guiGraphics.guiHeight();
                    y -= 66;
                    if (minecraft.gui.overlayMessageTime > 0) {
                        y -= 22;
                    } else if (minecraft.gui.toolHighlightTimer > 0) {
                        y -= 8;
                    }

                    ScreenRectangle rectangle = new ScreenRectangle(x, y, textWidth, 9);
                    TooltipRenderUtil.renderTooltipBackground(guiGraphics, rectangle.left(), rectangle.top(), rectangle.width(), rectangle.height(), 0);
                    guiGraphics.drawString(font, component, x, y, Color.WHITE.getRGB());
                }
            }
        }
    }
}
