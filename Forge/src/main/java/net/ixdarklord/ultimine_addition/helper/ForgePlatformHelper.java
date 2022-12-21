package net.ixdarklord.ultimine_addition.helper;

import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.data.item.MinerCertificateProvider;
import net.ixdarklord.ultimine_addition.data.player.PlayerUltimineCapabilityProvider;
import net.ixdarklord.ultimine_addition.helper.services.IPlatformHelper;
import net.ixdarklord.ultimine_addition.item.CreativeModeTabsList;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.network.packet.CertificateEffectPacket;
import net.ixdarklord.ultimine_addition.network.packet.MinerCertificatePacket;
import net.ixdarklord.ultimine_addition.particle.ParticlesList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Forge";
    }
    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get() != null && ModList.get().isLoaded(modId);
    }
    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }
    @Override
    public void registerConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON.SPEC, Constants.MOD_ID + "/common-config.toml");
    }

    @Override
    public CreativeModeTab getCreativeModeTab() {
        return CreativeModeTabsList.ULTIMINE_ADDITION_TAB;
    }

    @Override
    public boolean isPlayerCapable(Player player) {
        AtomicBoolean result = new AtomicBoolean(false);
        if (!player.getLevel().isClientSide) {
            player.getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).ifPresent(capability ->
                    result.set(capability.getCapability()));
        }
        return result.get();
    }
    @Override
    public void setPlayerCapability(Player player, boolean state) {
        if (!player.getLevel().isClientSide) {
            player.getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).ifPresent(capability ->
                    capability.setCapability(state));
        }
    }

    @Override
    public int getRequiredAmount(ItemStack stack) {
        AtomicInteger i = new AtomicInteger(0);
        stack.getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                i.set(capability.getRequiredAmount()));
        return i.get();
    }
    @Override
    public int getMinedBlocks(ItemStack stack) {
        AtomicInteger i = new AtomicInteger(0);
        stack.getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                i.set(capability.getMinedBlocks()));
        return i.get();
    }
    @Override
    public boolean isAccomplished(ItemStack stack) {
        AtomicBoolean i = new AtomicBoolean(false);
        stack.getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                i.set(capability.isAccomplished()));
        return i.get();
    }

    @Override
    public void setRequiredAmount(ItemStack stack, int amount, Player player) {
        if (Minecraft.getInstance().getConnection() != null) {
            PacketHandler.sendToServer(new MinerCertificatePacket(stack, "setRequiredAmount", amount));
        }
    }
    @Override
    public void setMinedBlocks(ItemStack stack, int amount, Player player) {
        if (Minecraft.getInstance().getConnection() != null) {
            PacketHandler.sendToServer(new MinerCertificatePacket(stack, "setMinedBlocks", amount));
        }
    }
    @Override
    public void setAccomplished(ItemStack stack, boolean state, Player player) {
        if (Minecraft.getInstance().getConnection() != null) {
            PacketHandler.sendToServer(new MinerCertificatePacket(stack, "setAccomplished", state));
        }
    }
    @Override
    public void addMinedBlocks(ItemStack stack, int amount, Player player) {
        if (Minecraft.getInstance().getConnection() != null) {
            PacketHandler.sendToServer(new MinerCertificatePacket(stack, "addMinedBlocks", amount));
        }
    }

    @Override
    public ParticleOptions getCelebrateParticle() {
        return ParticlesList.CELEBRATE_PARTICLE.get();
    }
    @Override
    public void sendCertificateEffect(ItemStack stack, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToPlayer(new CertificateEffectPacket(stack, serverPlayer), serverPlayer);
        }
    }
}
