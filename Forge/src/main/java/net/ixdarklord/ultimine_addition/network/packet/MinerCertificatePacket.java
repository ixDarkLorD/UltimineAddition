package net.ixdarklord.ultimine_addition.network.packet;

import net.ixdarklord.ultimine_addition.data.IDataHandler;
import net.ixdarklord.ultimine_addition.data.item.MinerCertificateData;
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
        private final int requiredAmount;
        private final int minedBlocks;
        private final boolean isAccomplished;
        public DataSyncS2C(ItemStack stack, int requiredAmount, int minedBlocks, boolean isAccomplished) {
            this.stack = stack;
            this.requiredAmount = requiredAmount;
            this.minedBlocks = minedBlocks;
            this.isAccomplished = isAccomplished;
        }

        public DataSyncS2C(FriendlyByteBuf buf) {
            this.stack = buf.readItem();
            this.requiredAmount = buf.readInt();
            this.minedBlocks = buf.readInt();
            this.isAccomplished = buf.readBoolean();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeItem(stack);
            buf.writeInt(requiredAmount);
            buf.writeInt(minedBlocks);
            buf.writeBoolean(isAccomplished);
        }

        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(IDataHandler.NBT_PATH);
                MinerCertificateData data = new MinerCertificateData();
                if (NBT == null) NBT = new CompoundTag();

                data.loadNBTData(NBT);
                data.setRequiredAmount(requiredAmount);
                data.setMinedBlocks(minedBlocks);
                data.setAccomplished(isAccomplished);
                data.saveNBTData(NBT);
                stack.getOrCreateTag().put(IDataHandler.NBT_PATH, NBT);
            }));
            context.setPacketHandled(true);
        }
    }
}
