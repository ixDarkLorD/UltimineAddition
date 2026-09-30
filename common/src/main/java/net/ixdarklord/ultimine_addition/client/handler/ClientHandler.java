package net.ixdarklord.ultimine_addition.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ClientHandler {
    public static Minecraft instance() {
        return Minecraft.getInstance();
    }

    public static Player getPlayer() {
        return instance().player;
    }

    public static void playSound(SoundEvent sound, float volume, float pitch) {
        instance().getSoundManager().stop(sound.location(), getPlayer().getSoundSource());
        getPlayer().playSound(sound, volume, pitch);
    }

    public static void playAnimation(ItemStack stack) {
        instance().gameRenderer.displayItemActivation(stack);
    }
}
