package net.ixdarklord.ultimine_addition.network.packet;

import net.ixdarklord.ultimine_addition.data.item.MinerCertificateProvider;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class MinerCertificatePacket {
    private final ItemStack stack;
    private final int[] intList;
    private final String operation;

    public MinerCertificatePacket(ItemStack stack, String operation, int[] intList) {
        this.stack = stack;
        this.operation = operation;
        this.intList = intList;
    }

    public MinerCertificatePacket(FriendlyByteBuf buf) {
        this.stack = buf.readItem();
        this.intList = buf.readVarIntArray();
        this.operation = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeItem(stack);
        buf.writeVarIntArray(intList);
        buf.writeUtf(operation);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            var items = Objects.requireNonNull(context.getSender()).getInventory().items;
            for (int i = 0; i < items.size(); i++) {
                if (i == intList[0] && items.get(i).sameItem(stack)) {
                    switch (operation) {
                        case "setRequiredAmount" -> {
                            AtomicInteger result = new AtomicInteger();
                            items.get(i).getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(c ->
                                    result.set(c.getRequiredAmount()));

                            if (result.get() == 0) {
                                items.get(i).getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                                        capability.setRequiredAmount(intList[1]));
                            }
                        }
                        case "setMinedBlocks" ->
                                items.get(i).getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                                        capability.setMinedBlocks(intList[1]));

                        case "setAccomplished" ->
                                items.get(i).getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                                        capability.setAccomplished(ItemUtils.IntArrayMaker.getBoolean(intList[2])));

                        case "addMinedBlocks" ->
                                items.get(i).getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                                        capability.addMinedBlocks(intList[1]));
                    }

                    AtomicInteger requiredAmount = new AtomicInteger(0);
                    AtomicInteger minedBlocks = new AtomicInteger(0);
                    AtomicBoolean isAccomplished = new AtomicBoolean(false);
                    items.get(i).getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                            requiredAmount.set(capability.getRequiredAmount()));
                    items.get(i).getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                            minedBlocks.set(capability.getMinedBlocks()));
                    items.get(i).getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                            isAccomplished.set(capability.isAccomplished()));

                    int[] newValues = new int[]{intList[0], requiredAmount.get(), minedBlocks.get(), isAccomplished.get() ? 1 : 0};
                    PacketHandler.sendToPlayer(new DataSyncS2C(stack, newValues), context.getSender());
                }
            }
        });
        context.setPacketHandled(true);
    }

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
            context.enqueueWork(() -> {
                LocalPlayer player = Minecraft.getInstance().player;
                assert player != null;
                var items = player.getInventory().items;
                for (int i = 0; i < items.size(); i++) {
                    if (i == intList[0] && items.get(i).sameItem(stack)) {
                        items.get(i).getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                                capability.setRequiredAmount(intList[1]));

                        items.get(i).getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                                capability.setMinedBlocks(intList[2]));

                        items.get(i).getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                                capability.setAccomplished(ItemUtils.IntArrayMaker.getBoolean(intList[3])));
                    }
                }
            });
            context.setPacketHandled(true);
        }
    }
}
