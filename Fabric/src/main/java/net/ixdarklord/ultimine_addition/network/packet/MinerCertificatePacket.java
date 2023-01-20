package net.ixdarklord.ultimine_addition.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.ixdarklord.ultimine_addition.data.IDataHandler;
import net.ixdarklord.ultimine_addition.data.item.MinerCertificateData;
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
            final int requiredAmount = buf.readInt();
            final int minedBlocks = buf.readInt();
            final boolean isAccomplished = buf.readBoolean();
            client.execute(() -> {
                CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(IDataHandler.NBT_PATH);
                MinerCertificateData data = new MinerCertificateData();
                if (NBT == null) NBT = new CompoundTag();

                data.loadNBTData(NBT);
                data.setRequiredAmount(requiredAmount);
                data.setMinedBlocks(minedBlocks);
                data.setAccomplished(isAccomplished);
                data.saveNBTData(NBT);
                stack.getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);
            });
        }
    }
}
