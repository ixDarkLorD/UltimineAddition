package net.ixdarklord.ultimine_addition.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.data.DataHandler;
import net.ixdarklord.ultimine_addition.data.item.MinerCertificateData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public class MinerCertificatePacket {
    public static void receive(MinecraftServer server, ServerPlayer serverPlayer,
                               ServerGamePacketListenerImpl ignored1, FriendlyByteBuf buf,
                               PacketSender ignored2) {

        final ItemStack item = buf.readItem();
        final int amount = buf.readInt();
        final boolean state = buf.readBoolean();
        final String operation = buf.readUtf();
        var list = serverPlayer.getInventory().items;
        server.execute(() -> {
            switch (operation) {
                case "setRequiredAmount" -> {
                    for (var stack : list) {
                        if (stack.sameItem(item)) {
                            CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(DataHandler.NBT_PATH);
                            MinerCertificateData data = new MinerCertificateData();
                            if (NBT == null) NBT = new CompoundTag();

                            data.loadNBTData(NBT);
                            if (data.getRequiredAmount() == 0 || data.getRequiredAmount() != ConfigHandler.COMMON.REQUIRED_AMOUNT.get()) {
                                data.setRequiredAmount(amount);
                                data.saveNBTData(NBT);
                                stack.getOrCreateTag().put(DataHandler.NBT_PATH, NBT);
                            }
                        }
                    }
                }
                case "setMinedBlocks" -> {
                    for (var stack : list) {
                        if (stack.sameItem(item)) {
                            CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(DataHandler.NBT_PATH);
                            MinerCertificateData data = new MinerCertificateData();
                            if (NBT == null) NBT = new CompoundTag();

                            data.loadNBTData(NBT);
                            if (data.getMinedBlocks() < data.getRequiredAmount()) {
                                data.setMinedBlocks(amount);
                                data.saveNBTData(NBT);
                                stack.getOrCreateTag().put(DataHandler.NBT_PATH, NBT);
                            } else if (data.getMinedBlocks() > data.getRequiredAmount()) {
                                data.setMinedBlocks(data.getRequiredAmount());
                                data.saveNBTData(NBT);
                                stack.getOrCreateTag().put(DataHandler.NBT_PATH, NBT);
                            }
                        }
                    }
                }
                case "setAccomplished" -> {
                    for (var stack : list) {
                        if (stack.sameItem(item)) {
                            CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(DataHandler.NBT_PATH);
                            MinerCertificateData data = new MinerCertificateData();
                            if (NBT == null) NBT = new CompoundTag();

                            data.loadNBTData(NBT);
                            data.setAccomplished(state);
                            data.saveNBTData(NBT);
                            stack.getOrCreateTag().put(DataHandler.NBT_PATH, NBT);
                        }
                    }
                }
                case "addMinedBlocks" -> {
                    for (var stack : list) {
                        if (stack.sameItem(item)) {
                            CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(DataHandler.NBT_PATH);
                            MinerCertificateData data = new MinerCertificateData();
                            if (NBT == null) NBT = new CompoundTag();

                            data.loadNBTData(NBT);
                            if (data.getMinedBlocks() < data.getRequiredAmount()) {
                                data.addMinedBlocks(amount);
                                data.saveNBTData(NBT);
                                stack.getOrCreateTag().put(DataHandler.NBT_PATH, NBT);
                            }
                        }
                    }
                }
            }
        });
    }
}
