package net.ixdarklord.ultimine_addition.data.player;

import net.minecraft.nbt.CompoundTag;

public class PlayerUltimineData {
    private boolean isCapable;

    public boolean getCapability() {
        return isCapable;
    }
    public void setCapability(boolean state) {
        isCapable = state;
    }

    public void copyFrom(PlayerUltimineData source) {
        this.isCapable = source.isCapable;
    }
    public void saveNBTData(CompoundTag NBT) {
        NBT.putBoolean("can_ultimine", isCapable);
    }
    public void loadNBTData(CompoundTag NBT) {
        isCapable = NBT.getBoolean("can_ultimine");
    }
}
