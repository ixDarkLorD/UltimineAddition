package net.ixdarklord.ultimine_addition.helper.services;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public interface IPlatformHelper {
    String getPlatformName();
    boolean isModLoaded(String modId);
    boolean isDevelopmentEnvironment();
    void registerConfig();

    CreativeModeTab getCreativeModeTab();

    boolean isPlayerCapable(Player player);
    void setPlayerCapability(Player player, boolean state);

    int getRequiredAmount(ItemStack stack);
    int getMinedBlocks(ItemStack stack);
    boolean isAccomplished(ItemStack stack);
    void setRequiredAmount(ItemStack stack, int amount, Player player);
    void setMinedBlocks(ItemStack stack, int amount, Player player);
    void setAccomplished(ItemStack stack, boolean state, Player player);
    void addMinedBlocks(ItemStack stack, int amount, Player player);

    ParticleOptions getCelebrateParticle();
    void sendCertificateEffect(ItemStack stack, Player player);
}
