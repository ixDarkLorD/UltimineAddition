package net.ixdarklord.ultimine_addition.helper;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.data.DataHandler;
import net.ixdarklord.ultimine_addition.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.helper.services.IPlatformHelper;
import net.ixdarklord.ultimine_addition.item.CreativeModeTabsList;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.particle.ParticlesList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Fabric";
    }
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
    @Override
    public void registerConfig() {
        ModLoadingContext.registerConfig(Constants.MOD_ID, ModConfig.Type.CLIENT, ConfigHandler.COMMON.SPEC, Constants.MOD_ID + "/common-config.toml");
    }

    @Override
    public CreativeModeTab getCreativeModeTab() {
        return CreativeModeTabsList.ULTIMINE_ADDITION_TAB;
    }

    @Override
    public boolean isPlayerCapable(Player player) {
        return DataHandler.ultimineData.getCapability();
    }
    @Override
    public void setPlayerCapability(Player player, boolean state) {
        DataHandler.ultimineData.setCapability(state);
    }

    @Override
    public int getRequiredAmount(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(DataHandler.NBT_PATH);
        MinerCertificateData data = new MinerCertificateData();

        if (NBT != null) data.loadNBTData(NBT);
        return data.getRequiredAmount();
    }
    @Override
    public int getMinedBlocks(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(DataHandler.NBT_PATH);
        MinerCertificateData data = new MinerCertificateData();

        if (NBT != null) data.loadNBTData(NBT);
        return data.getMinedBlocks();
    }
    @Override
    public boolean isAccomplished(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(DataHandler.NBT_PATH);
        MinerCertificateData data = new MinerCertificateData();

        if (NBT != null) data.loadNBTData(NBT);
        return data.isAccomplished();
    }

    @Override
    public void setRequiredAmount(ItemStack stack, int amount, Player player) {
        if (ClientPlayNetworking.canSend(PacketHandler.MINER_CERTIFICATE_ID)) {
            var buf = PacketByteBufs.create();
            buf.writeItem(stack);
            buf.writeInt(amount);
            buf.writeBoolean(false);
            buf.writeUtf("setRequiredAmount");
            ClientPlayNetworking.send(PacketHandler.MINER_CERTIFICATE_ID, buf);
        }
    }
    @Override
    public void setMinedBlocks(ItemStack stack, int amount, Player player) {
        if (ClientPlayNetworking.canSend(PacketHandler.MINER_CERTIFICATE_ID)) {
            var buf = PacketByteBufs.create();
            buf.writeItem(stack);
            buf.writeInt(amount);
            buf.writeBoolean(false);
            buf.writeUtf("setMinedBlocks");
            ClientPlayNetworking.send(PacketHandler.MINER_CERTIFICATE_ID, buf);
        }
    }
    @Override
    public void setAccomplished(ItemStack stack, boolean state, Player player) {
        if (ClientPlayNetworking.canSend(PacketHandler.MINER_CERTIFICATE_ID)) {
            var buf = PacketByteBufs.create();
            buf.writeItem(stack);
            buf.writeInt(0);
            buf.writeBoolean(state);
            buf.writeUtf("setAccomplished");
            ClientPlayNetworking.send(PacketHandler.MINER_CERTIFICATE_ID, buf);
        }
    }
    @Override
    public void addMinedBlocks(ItemStack stack, int amount, Player player) {
        if (ClientPlayNetworking.canSend(PacketHandler.MINER_CERTIFICATE_ID)) {
            var buf = PacketByteBufs.create();
            buf.writeItem(stack);
            buf.writeInt(amount);
            buf.writeBoolean(false);
            buf.writeUtf("addMinedBlocks");
            ClientPlayNetworking.send(PacketHandler.MINER_CERTIFICATE_ID, buf);
        }
    }

    @Override
    public ParticleOptions getCelebrateParticle() {
        return ParticlesList.CELEBRATE_PARTICLE;
    }
    @Override
    public void sendCertificateEffect(ItemStack stack, Player player) {
        var buf = PacketByteBufs.create();
        buf.writeItem(stack);
        buf.writeInt(player.getId());
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, PacketHandler.CERTIFICATE_EFFECT_ID, buf);
        }
    }
}
