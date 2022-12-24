package net.ixdarklord.ultimine_addition.helper.services;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

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
    void setRequiredAmount(ItemStack stack, int slotID, int amount, Player player);
    void setMinedBlocks(ItemStack stack, int slotID, int amount, Player player);
    void setAccomplished(ItemStack stack, int slotID, boolean state, Player player);
    void addMinedBlocks(ItemStack stack, int slotID, int amount, Player player);

    ParticleOptions getCelebrateParticle();
    void sendCertificateEffect(ItemStack stack, Player player);

    TagKey<Block> oresTag();
}
