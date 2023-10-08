package net.ixdarklord.ultimine_addition.common.data.player;

import net.ixdarklord.ultimine_addition.common.data.DataHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class PlayerAbilityData extends DataHandler<PlayerAbilityData, CompoundTag> {
    private boolean isCapable;

    public boolean getAbility() {
        return isCapable;
    }
    public PlayerAbilityData setAbility(boolean state) {
        isCapable = state;
        return this;
    }
    public void copyFrom(PlayerAbilityData source) {
        this.isCapable = source.isCapable;
    }

    @Override
    public void saveData(CompoundTag data) {
        data.putBoolean("can_ultimine", isCapable);
    }
    @Override
    public PlayerAbilityData loadData(CompoundTag data) {
        isCapable = data.getBoolean("can_ultimine");
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {}
}
