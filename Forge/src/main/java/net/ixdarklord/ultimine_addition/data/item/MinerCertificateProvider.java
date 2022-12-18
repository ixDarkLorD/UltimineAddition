package net.ixdarklord.ultimine_addition.data.item;

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

public class MinerCertificateProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<MinerCertificateData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    private MinerCertificateData capability = null;
    private final LazyOptional<MinerCertificateData> optional = LazyOptional.of(this::createCapability);

    private MinerCertificateData createCapability() {
        if (this.capability == null) {
            this.capability = new MinerCertificateData();
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
