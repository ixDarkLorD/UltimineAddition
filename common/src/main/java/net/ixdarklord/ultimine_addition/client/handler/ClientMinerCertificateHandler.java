package net.ixdarklord.ultimine_addition.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ClientMinerCertificateHandler {
    public static void playClientSound() {
        playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0F, 1.0F);
        playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
    }

    public static void playSound(SoundEvent sound, float volume, float pitch) {
        Minecraft MC = Minecraft.getInstance();
        Player player = MC.player;
        if (player == null) return;
        MC.getSoundManager().stop(sound.getLocation(), player.getSoundSource());
        player.playSound(sound, volume, pitch);
    }

    public static void playAnimation(ItemStack stack) {
        Minecraft MC = Minecraft.getInstance();
        MC.gameRenderer.displayItemActivation(stack);
    }
}
