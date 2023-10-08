package net.ixdarklord.ultimine_addition.common.data.item;

import net.ixdarklord.ultimine_addition.client.handler.ClientMinerCertificateHandler;
import net.ixdarklord.ultimine_addition.common.data.DataHandler;
import net.ixdarklord.ultimine_addition.common.item.MinerCertificateItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class MinerCertificateData extends DataHandler<MinerCertificateData, ItemStack> {
    private ItemStack stack;
    private boolean isAccomplished;
    private boolean isCelebration;
    private boolean pickUpSound;

    @Override
    public ItemStack get() {
        return this.stack;
    }

    public boolean isAccomplished() {
        return this.isAccomplished;
    }

    public MinerCertificateData setAccomplished(boolean state) {
        this.isAccomplished = state;
        return this;
    }

    public MinerCertificateData createCelebration(boolean state) {
        isCelebration = state;
        return this;
    }

    public MinerCertificateData pickUpSound(boolean state) {
        this.pickUpSound = state;
        return this;
    }

    @Override
    public void clientUpdate() {
        if (this.stack.getItem() instanceof MinerCertificateItem && this.pickUpSound) {
            ClientMinerCertificateHandler.playClientSound();
        }
        if (this.stack.getItem() instanceof MinerCertificateItem && this.isCelebration) {
            ClientMinerCertificateHandler.sendClientMessage();
            ClientMinerCertificateHandler.playAnimation(this.stack);
        }
    }

    @Override
    public void saveData(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(this.NBTBase);
        if (NBT == null) NBT = new CompoundTag();

        NBT.putBoolean("is_accomplished", this.isAccomplished);
        stack.getOrCreateTag().put(this.NBTBase, NBT);
        super.saveData(stack);
    }

    @Override
    public MinerCertificateData loadData(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(this.NBTBase);
        if (NBT == null) NBT = new CompoundTag();

        this.isAccomplished = NBT.getBoolean("is_accomplished");
        this.stack = stack;
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeItem(this.stack);
        buf.writeBoolean(this.pickUpSound);
        buf.writeBoolean(this.isAccomplished);
        buf.writeBoolean(this.isCelebration);
    }
    public static MinerCertificateData fromNetwork(FriendlyByteBuf buf) {
        return new MinerCertificateData().loadData(buf.readItem())
                .pickUpSound(buf.readBoolean())
                .setAccomplished(buf.readBoolean())
                .createCelebration(buf.readBoolean());
    }
}
