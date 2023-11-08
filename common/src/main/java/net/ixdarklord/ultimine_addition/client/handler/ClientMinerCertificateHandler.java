package net.ixdarklord.ultimine_addition.client.handler;

import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ClientMinerCertificateHandler {
    public static void playClientSound() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        player.playSound(SoundEvents.NOTE_BLOCK_BELL, 1.0F, 1.0F);
        player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
    }

    public static void sendClientMessage() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (!ServicePlatform.Players.isPlayerUltimineCapable(player))
            player.displayClientMessage(new TranslatableComponent("info.ultimine_addition.obtain").withStyle(ChatFormatting.GOLD), true);
        else
            player.displayClientMessage(new TranslatableComponent("info.ultimine_addition.obtained_already").withStyle(ChatFormatting.RED), true);

    }

    public static void playAnimation(ItemStack stack) {
        Minecraft MC = Minecraft.getInstance();
        Player player = MC.player;
        if (player == null) return;

        if (!ServicePlatform.Players.isPlayerUltimineCapable(player))
            MC.gameRenderer.displayItemActivation(stack);
    }
}
