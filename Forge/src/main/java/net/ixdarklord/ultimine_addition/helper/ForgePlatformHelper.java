package net.ixdarklord.ultimine_addition.helper;

import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.data.IDataHandler;
import net.ixdarklord.ultimine_addition.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.data.player.PlayerUltimineCapabilityProvider;
import net.ixdarklord.ultimine_addition.helper.services.IPlatformHelper;
import net.ixdarklord.ultimine_addition.item.ItemRegistries;
import net.ixdarklord.ultimine_addition.item.MinerCertificate;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.network.packet.CelebrateActionPacket;
import net.ixdarklord.ultimine_addition.network.packet.MinerCertificatePacket;
import net.ixdarklord.ultimine_addition.network.packet.PlayerCapabilityPacket;
import net.ixdarklord.ultimine_addition.particle.ParticlesList;
import net.ixdarklord.ultimine_addition.util.TagsUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.concurrent.atomic.AtomicBoolean;

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
        return ItemRegistries.ULTIMINE_ADDITION_TAB;
    }

    @Override
    public boolean isPlayerCapable(Player player) {
        AtomicBoolean result = new AtomicBoolean(false);
        player.getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).ifPresent(capability ->
                result.set(capability.getCapability()));
        return result.get();
    }
    @Override
    public void setPlayerCapability(Player player, boolean state) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).ifPresent(capability ->
                    capability.setCapability(state));
            PacketHandler.sendToPlayer(new PlayerCapabilityPacket.DataSyncS2C(state), serverPlayer);
        } else if (Minecraft.getInstance().getConnection() != null) {
            PacketHandler.sendToServer(new PlayerCapabilityPacket(player, state));
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
        if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(IDataHandler.NBT_PATH);
            MinerCertificateData data = new MinerCertificateData();
            if (NBT == null) NBT = new CompoundTag();

            data.loadNBTData(NBT);
            if (data.getRequiredAmount() == 0) data.setRequiredAmount(amount);
            data.saveNBTData(NBT);
            stack.getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);

            int[] newValues = new int[]{data.getRequiredAmount(), data.getMinedBlocks(), data.isAccomplished() ? 1 : 0};
            PacketHandler.sendToPlayer(new MinerCertificatePacket.DataSyncS2C(stack, newValues), serverPlayer);
        }
    }
    @Override
    public void setMinedBlocks(ItemStack stack, int slotID, int amount, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(IDataHandler.NBT_PATH);
            MinerCertificateData data = new MinerCertificateData();
            if (NBT == null) NBT = new CompoundTag();

            data.loadNBTData(NBT);
            data.setMinedBlocks(amount);
            data.saveNBTData(NBT);
            stack.getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);

            int[] newValues = new int[]{data.getRequiredAmount(), data.getMinedBlocks(), data.isAccomplished() ? 1 : 0};
            PacketHandler.sendToPlayer(new MinerCertificatePacket.DataSyncS2C(stack, newValues), serverPlayer);
        }
    }
    @Override
    public void setAccomplished(ItemStack stack, int slotID, boolean state, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(IDataHandler.NBT_PATH);
            MinerCertificateData data = new MinerCertificateData();
            if (NBT == null) NBT = new CompoundTag();

            data.loadNBTData(NBT);
            data.setAccomplished(state);
            data.saveNBTData(NBT);
            stack.getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);

            int[] newValues = new int[]{data.getRequiredAmount(), data.getMinedBlocks(), data.isAccomplished() ? 1 : 0};
            PacketHandler.sendToPlayer(new MinerCertificatePacket.DataSyncS2C(stack, newValues), serverPlayer);
        }
    }
    @Override
    public void addMinedBlocks(ItemStack stack, int slotID, int amount, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(IDataHandler.NBT_PATH);
            MinerCertificateData data = new MinerCertificateData();
            if (NBT == null) NBT = new CompoundTag();

            data.loadNBTData(NBT);
            data.addMinedBlocks(amount);
            data.saveNBTData(NBT);
            stack.getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);

            int[] newValues = new int[]{data.getRequiredAmount(), data.getMinedBlocks(), data.isAccomplished() ? 1 : 0};
            PacketHandler.sendToPlayer(new MinerCertificatePacket.DataSyncS2C(stack, newValues), serverPlayer);
        }
    }

    @Override
    public ParticleOptions getCelebrateParticle() {
        return ParticlesList.CELEBRATE_PARTICLE.get();
    }
    @Override
    public void sendCelebrateAction(String actionName, ItemStack stack, Player player) {
        if (player.getLevel().isClientSide() && Minecraft.getInstance().getConnection() != null) {
            PacketHandler.sendToServer(new CelebrateActionPacket(actionName, stack, player));
        } else if (player instanceof ServerPlayer serverPlayer){
            if (actionName.equals("obtained")) {
                MinerCertificate.playParticleAndSound(serverPlayer);
            }
            PacketHandler.sendToPlayer(new CelebrateActionPacket.Play2Client(actionName, stack, serverPlayer), serverPlayer);
        }
    }

    @Override
    public TagKey<Block> oresTag() {
        return TagsUtils.Blocks.FORGE_ORES;
    }
}
