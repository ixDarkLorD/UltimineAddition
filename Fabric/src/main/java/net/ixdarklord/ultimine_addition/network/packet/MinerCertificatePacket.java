package net.ixdarklord.ultimine_addition.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ixdarklord.ultimine_addition.data.IDataHandler;
import net.ixdarklord.ultimine_addition.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
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

        final ItemStack stack = buf.readItem();
        final int[] intList = buf.readVarIntArray(113);
        final String operation = buf.readUtf();

        server.execute(() -> {
            var items = serverPlayer.getInventory().items;
            for (int i = 0; i < items.size(); i++) {
                if (i == intList[0] && items.get(i).sameItem(stack)) {
                    CompoundTag NBT = (CompoundTag) items.get(i).getOrCreateTag().get(IDataHandler.NBT_PATH);
                    MinerCertificateData data = new MinerCertificateData();
                    if (NBT == null) NBT = new CompoundTag();

                    switch (operation) {
                        case "setRequiredAmount" -> {
                            data.loadNBTData(NBT);
                            if (data.getRequiredAmount() == 0) data.setRequiredAmount(intList[1]);
                            data.saveNBTData(NBT);
                            items.get(i).getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);
                        }
                        case "setMinedBlocks" -> {
                            data.loadNBTData(NBT);
                            data.setMinedBlocks(intList[1]);
                            data.saveNBTData(NBT);
                            items.get(i).getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);
                        }
                        case "setAccomplished" -> {
                            data.loadNBTData(NBT);
                            data.setAccomplished(ItemUtils.IntArrayMaker.getBoolean(intList[2]));
                            data.saveNBTData(NBT);
                            items.get(i).getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);
                        }
                        case "addMinedBlocks" -> {
                            data.loadNBTData(NBT);
                            data.addMinedBlocks(intList[1]);
                            data.saveNBTData(NBT);
                            items.get(i).getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);
                        }
                    }

                    int[] newValues = new int[]{intList[0], data.getRequiredAmount(), data.getMinedBlocks(), data.isAccomplished() ? 1 : 0};
                    var newBuf = PacketByteBufs.create();
                    newBuf.writeItem(items.get(i));
                    newBuf.writeVarIntArray(newValues);
                    ServerPlayNetworking.send(serverPlayer, PacketHandler.MINER_CERTIFICATE_SYNC_ID, newBuf);
                }
            }
        });
    }

    public static class DataSyncS2C {
        public static void receive(Minecraft client, ClientPacketListener ignored1,
                                   FriendlyByteBuf buf, PacketSender ignored2) {
            final ItemStack stack = buf.readItem();
            final int[] intList = buf.readVarIntArray();
            client.execute(() -> {
                assert client.player != null;
                var items = client.player.getInventory().items;
                for (int i = 0; i < items.size(); i++) {
                    if (i == intList[0] && items.get(i).sameItem(stack)) {
                        CompoundTag NBT = (CompoundTag) items.get(i).getOrCreateTag().get(IDataHandler.NBT_PATH);
                        MinerCertificateData data = new MinerCertificateData();
                        if (NBT == null) NBT = new CompoundTag();

                        data.loadNBTData(NBT);
                        data.setRequiredAmount(intList[1]);
                        data.setMinedBlocks(intList[2]);
                        data.setAccomplished(ItemUtils.IntArrayMaker.getBoolean(intList[3]));
                        data.saveNBTData(NBT);
                        items.get(i).getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);
                    }
                }
            });
        }
    }
}
