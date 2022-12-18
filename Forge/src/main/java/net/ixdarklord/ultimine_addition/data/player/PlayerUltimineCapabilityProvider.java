package net.ixdarklord.ultimine_addition.data.player;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerUltimineCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerUltimineData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    private PlayerUltimineData capability = null;
    private final LazyOptional<PlayerUltimineData> optional = LazyOptional.of(this::createCapability);

    private PlayerUltimineData createCapability() {
        if (this.capability == null) {
            this.capability = new PlayerUltimineData();
        }
        return this.capability;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag NBT = new CompoundTag();
        createCapability().saveNBTData(NBT);
        return NBT;
    }

    @Override
    public void deserializeNBT(CompoundTag NBT) {
        createCapability().loadNBTData(NBT);
    }
}
