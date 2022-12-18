package net.ixdarklord.ultimine_addition.data.item;

import net.minecraft.nbt.CompoundTag;

public class MinerCertificateData {
    private int requiredAmount;
    private int minedBlocks;
    private boolean isAccomplished;

    public int getRequiredAmount() {
        return requiredAmount;
    }

    public int getMinedBlocks() {
        return minedBlocks;
    }

    public boolean isAccomplished() {
        return isAccomplished;
    }

    public void setRequiredAmount(int requiredAmount) {
        this.requiredAmount = requiredAmount;
    }

    public void addMinedBlocks(int add) {
        this.minedBlocks = Math.min(minedBlocks + add, requiredAmount);
    }
    public void setMinedBlocks(int num) {
        this.minedBlocks = num;
    }

    public void setAccomplished(boolean accomplished) {
        isAccomplished = accomplished;
    }

    public void saveNBTData(CompoundTag NBT) {
        NBT.putInt("required_amount", requiredAmount);
        NBT.putInt("mined_blocks", minedBlocks);
        NBT.putBoolean("is_accomplished", isAccomplished);
    }
    public void loadNBTData(CompoundTag NBT) {
        requiredAmount = NBT.getInt("required_amount");
        minedBlocks = NBT.getInt("mined_blocks");
        isAccomplished = NBT.getBoolean("is_accomplished");
    }
}
