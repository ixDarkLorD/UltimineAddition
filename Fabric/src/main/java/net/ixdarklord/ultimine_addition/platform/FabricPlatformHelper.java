package net.ixdarklord.ultimine_addition.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.data.IDataHandler;
import net.ixdarklord.ultimine_addition.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.item.ItemsRegistries;
import net.ixdarklord.ultimine_addition.item.MinerCertificate;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.particle.ParticlesList;
import net.ixdarklord.ultimine_addition.platform.services.IPlatformHelper;
import net.ixdarklord.ultimine_addition.util.PlayerUtils;
import net.ixdarklord.ultimine_addition.util.TagsUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
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
        return ItemsRegistries.ULTIMINE_ADDITION_TAB;
    }

    @Override
    public boolean isPlayerCapable(Player player) {
        return ((IDataHandler) player).getPlayerUltimineData().getCapability();
    }
    @Override
    public void setPlayerCapability(Player player, boolean state) {
        if (player.getLevel().isClientSide() && ClientPlayNetworking.canSend(PacketHandler.PLAYER_CAPABILITY_ID)) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(state);
            ClientPlayNetworking.send(PacketHandler.PLAYER_CAPABILITY_ID, buf);
        } else {
            PlayerUtils.CapabilityData.set((IDataHandler) player, state);
        }
    }

    @Override
    public int getRequiredAmount(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(IDataHandler.NBT_PATH);
        MinerCertificateData data = new MinerCertificateData();

        if (NBT != null) data.loadNBTData(NBT);
        return data.getRequiredAmount();
    }
    @Override
    public int getMinedBlocks(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(IDataHandler.NBT_PATH);
        MinerCertificateData data = new MinerCertificateData();

        if (NBT != null) data.loadNBTData(NBT);
        return data.getMinedBlocks();
    }
    @Override
    public boolean isAccomplished(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(IDataHandler.NBT_PATH);
        MinerCertificateData data = new MinerCertificateData();

        if (NBT != null) data.loadNBTData(NBT);
        return data.isAccomplished();
    }

    @Override
    public void setRequiredAmount(ItemStack stack, int slotID, int amount, Player player) {
        if (player instanceof ServerPlayer serverPlayer && ServerPlayNetworking.canSend(serverPlayer, PacketHandler.MINER_CERTIFICATE_SYNC_ID)) {
            CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(IDataHandler.NBT_PATH);
            MinerCertificateData data = new MinerCertificateData();
            if (NBT == null) NBT = new CompoundTag();

            data.loadNBTData(NBT);
            if (data.getRequiredAmount() == 0) data.setRequiredAmount(amount);
            data.saveNBTData(NBT);
            stack.getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);

            updateMinerCertificateData(serverPlayer, stack, data);
        }
    }
    @Override
    public void setMinedBlocks(ItemStack stack, int slotID, int amount, Player player) {
        if (player instanceof ServerPlayer serverPlayer && ServerPlayNetworking.canSend(serverPlayer, PacketHandler.MINER_CERTIFICATE_SYNC_ID)) {
            CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(IDataHandler.NBT_PATH);
            MinerCertificateData data = new MinerCertificateData();
            if (NBT == null) NBT = new CompoundTag();

            data.loadNBTData(NBT);
            data.setMinedBlocks(amount);
            data.saveNBTData(NBT);
            stack.getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);

            updateMinerCertificateData(serverPlayer, stack, data);
        }
    }
    @Override
    public void setAccomplished(ItemStack stack, int slotID, boolean state, Player player) {
        if (player instanceof ServerPlayer serverPlayer && ServerPlayNetworking.canSend(serverPlayer, PacketHandler.MINER_CERTIFICATE_SYNC_ID)) {
            CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(IDataHandler.NBT_PATH);
            MinerCertificateData data = new MinerCertificateData();
            if (NBT == null) NBT = new CompoundTag();

            data.loadNBTData(NBT);
            data.setAccomplished(state);
            data.saveNBTData(NBT);
            stack.getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);

            updateMinerCertificateData(serverPlayer, stack, data);
        }
    }
    @Override
    public void addMinedBlocks(ItemStack stack, int slotID, int amount, Player player) {
        if (player instanceof ServerPlayer serverPlayer && ServerPlayNetworking.canSend(serverPlayer, PacketHandler.MINER_CERTIFICATE_SYNC_ID)) {
            CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(IDataHandler.NBT_PATH);
            MinerCertificateData data = new MinerCertificateData();
            if (NBT == null) NBT = new CompoundTag();

            data.loadNBTData(NBT);
            data.addMinedBlocks(amount);
            data.saveNBTData(NBT);
            stack.getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);

            updateMinerCertificateData(serverPlayer, stack, data);
        }
    }

    private void updateMinerCertificateData(ServerPlayer player, ItemStack stack, MinerCertificateData data) {
        var newBuf = PacketByteBufs.create();
        newBuf.writeItem(stack);
        newBuf.writeInt(data.getRequiredAmount());
        newBuf.writeInt(data.getMinedBlocks());
        newBuf.writeBoolean(data.isAccomplished());
        ServerPlayNetworking.send(player, PacketHandler.MINER_CERTIFICATE_SYNC_ID, newBuf);
    }

    @Override
    public ParticleOptions getCelebrateParticle() {
        return ParticlesList.CELEBRATE_PARTICLE;
    }
    @Override
    public void sendCelebrateAction(String actionName, ItemStack stack, Player player) {
        if (player.getLevel().isClientSide() && ClientPlayNetworking.canSend(PacketHandler.CELEBRATE_ACTION_ID)) {
            var buf = PacketByteBufs.create();
            buf.writeUtf(actionName);
            buf.writeItem(stack);
            buf.writeInt(player.getId());
            ClientPlayNetworking.send(PacketHandler.CELEBRATE_ACTION_ID, buf);
        } else if (player instanceof ServerPlayer serverPlayer && ServerPlayNetworking.canSend(serverPlayer, PacketHandler.CELEBRATE_ACTION_SYNC_ID)) {
            if (actionName.equals("obtained")) {
                MinerCertificate.playParticleAndSound(serverPlayer);
            }
            var buffer = PacketByteBufs.create();
            buffer.writeUtf(actionName);
            buffer.writeItem(stack);
            buffer.writeInt(player.getId());
            ServerPlayNetworking.send(serverPlayer, PacketHandler.CELEBRATE_ACTION_SYNC_ID, buffer);
        }
    }

    @Override
    public TagKey<Block> oresTag() {
        return TagsUtils.Blocks.COMMON_ORES;
    }
}
