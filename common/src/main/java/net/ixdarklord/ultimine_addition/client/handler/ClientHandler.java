package net.ixdarklord.ultimine_addition.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

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
