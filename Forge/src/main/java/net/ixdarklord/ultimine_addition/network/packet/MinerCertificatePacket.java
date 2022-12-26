package net.ixdarklord.ultimine_addition.network.packet;

import net.ixdarklord.ultimine_addition.data.IDataHandler;
import net.ixdarklord.ultimine_addition.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MinerCertificatePacket {

    public static class DataSyncS2C {
        private final ItemStack stack;
        private final int[] intList;

        public DataSyncS2C(ItemStack stack, int[] intList) {
            this.stack = stack;
            this.intList = intList;
        }

        public DataSyncS2C(FriendlyByteBuf buf) {
            this.stack = buf.readItem();
            this.intList = buf.readVarIntArray();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeItem(stack);
            buf.writeVarIntArray(intList);
        }

        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(IDataHandler.NBT_PATH);
                MinerCertificateData data = new MinerCertificateData();
                if (NBT == null) NBT = new CompoundTag();

                data.loadNBTData(NBT);
                data.setRequiredAmount(intList[0]);
                data.setMinedBlocks(intList[1]);
                data.setAccomplished(ItemUtils.IntArrayMaker.getBoolean(intList[2]));
                data.saveNBTData(NBT);
                stack.getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);
            }));
            context.setPacketHandled(true);
        }
    }
}
