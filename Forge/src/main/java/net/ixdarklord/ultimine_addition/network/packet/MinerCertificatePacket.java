package net.ixdarklord.ultimine_addition.network.packet;

import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.data.item.MinerCertificateProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class MinerCertificatePacket {
    private final ItemStack stack;
    private int amount;
    private boolean state;
    private final String operation;

    public MinerCertificatePacket(ItemStack stack, String operation, int amount) {
        this.stack = stack;
        this.operation = operation;
        this.amount = amount;
    }

    public MinerCertificatePacket(ItemStack stack, String operation, boolean state) {
        this.stack = stack;
        this.operation = operation;
        this.state = state;
    }

    public MinerCertificatePacket(FriendlyByteBuf buf) {
        this.stack = buf.readItem();
        this.amount = buf.readInt();
        this.state = buf.readBoolean();
        this.operation = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeItem(stack);
        buf.writeInt(amount);
        buf.writeBoolean(state);
        buf.writeUtf(operation);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            var items = Objects.requireNonNull(context.getSender()).getInventory().items;
            switch (operation) {
                case "setRequiredAmount" -> {
                    for (var item : items) {
                        if (item.sameItem(stack)) {
                            AtomicInteger result = new AtomicInteger();
                            item.getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(c ->
                                    result.set(c.getRequiredAmount()));
                            if (result.get() == 0 || result.get() != ConfigHandler.COMMON.REQUIRED_AMOUNT.get()) {
                                item.getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                                        capability.setRequiredAmount(amount));
                            }
                        }
                    }
                }
                case "setMinedBlocks" -> {
                    for (var item : items) {
                        if (item.sameItem(stack)) {
                            item.getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                                    capability.setMinedBlocks(amount));
                        }
                    }
                }
                case "setAccomplished" -> {
                    for (var item : items) {
                        if (item.sameItem(stack)) {
                            item.getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                                    capability.setAccomplished(state));
                        }
                    }
                }
                case "addMinedBlocks" -> {
                    for (var item : items) {
                        if (item.sameItem(stack)) {
                            item.getCapability(MinerCertificateProvider.CAPABILITY).ifPresent(capability ->
                                    capability.addMinedBlocks(amount));
                        }
                    }
                }
            }
        });
    }
}
