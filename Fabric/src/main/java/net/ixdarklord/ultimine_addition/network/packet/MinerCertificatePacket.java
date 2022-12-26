package net.ixdarklord.ultimine_addition.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.ixdarklord.ultimine_addition.data.IDataHandler;
import net.ixdarklord.ultimine_addition.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class MinerCertificatePacket {
    public static class DataSyncS2C {
        public static void receive(Minecraft client, ClientPacketListener ignored1,
                                   FriendlyByteBuf buf, PacketSender ignored2) {
            final ItemStack stack = buf.readItem();
            final int[] intList = buf.readVarIntArray(115);
            client.execute(() -> {
                CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(IDataHandler.NBT_PATH);
                MinerCertificateData data = new MinerCertificateData();
                if (NBT == null) NBT = new CompoundTag();

                data.loadNBTData(NBT);
                data.setRequiredAmount(intList[0]);
                data.setMinedBlocks(intList[1]);
                data.setAccomplished(ItemUtils.IntArrayMaker.getBoolean(intList[2]));
                data.saveNBTData(NBT);
                stack.getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);
            });
        }
    }
}
