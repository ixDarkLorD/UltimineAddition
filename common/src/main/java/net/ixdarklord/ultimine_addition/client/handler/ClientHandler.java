package net.ixdarklord.ultimine_addition.client.handler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
    @Environment(EnvType.CLIENT)
    public static Minecraft instance() {
        return Minecraft.getInstance();
    }

    @Environment(EnvType.CLIENT)
    public static Player getPlayer() {
        return instance().player;
    }

    @Environment(EnvType.CLIENT)
    public static void playSound(SoundEvent sound, float volume, float pitch) {
        instance().getSoundManager().stop(sound.getLocation(), getPlayer().getSoundSource());
        getPlayer().playSound(sound, volume, pitch);
    }

    @Environment(EnvType.CLIENT)
    public static void playAnimation(ItemStack stack) {
        instance().gameRenderer.displayItemActivation(stack);
    }

    @Environment(EnvType.CLIENT)
    public static void playConsumeModeEffect(BlockPos pos, BlockState state) {
        Minecraft mc = instance();
        ClientLevel level = mc.level;
        if (level != null) {
            double x = (double)pos.getX() + (double)0.5F;
            double y = (double)pos.getY() + (double)0.5F;
            double z = (double)pos.getZ() + (double)0.5F;
            mc.particleEngine.destroy(pos, state);

            for(int i = 0; i < 40; ++i) {
                double ox = (level.random.nextDouble() - (double)0.5F) * 1.3;
                double oy = (level.random.nextDouble() - (double)0.5F) * 1.3;
                double oz = (level.random.nextDouble() - (double)0.5F) * 1.3;
                double vx = -ox * 0.22;
                double vy = -oy * 0.22;
                double vz = -oz * 0.22;
                level.addParticle(ParticleTypes.REVERSE_PORTAL, x + ox, y + oy, z + oz, vx, vy, vz);
            }

            for(int i = 0; i < 18; ++i) {
                double radius = 0.7;
                double angle = (double)i / (double)18.0F * Math.PI * (double)2.0F;
                double px = x + Math.cos(angle) * radius;
                double pz = z + Math.sin(angle) * radius;
                double vx = (x - px) * 0.18;
                double vz = (z - pz) * 0.18;
                level.addParticle(ParticleTypes.PORTAL, px, y, pz, vx, 0.01, vz);
            }

            for(Direction direction : Direction.values()) {
                int i = ConstantInt.of(4).sample(level.random);

                for(int j = 0; j < i; ++j) {
                    ParticleUtils.spawnParticleOnFace(level, pos, direction, ParticleTypes.SMOKE, Vec3.ZERO.add(0.0F, 0.02, 0.0F), 0.5F);
                }
            }

            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.1F, 1.0F, false);
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.3F, 0.4F, false);
        }
    }
}
