package net.ixdarklord.ultimine_addition.common.data.player.forge;

import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
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
   public static Capability<PlayerAbilityData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
   });
   private PlayerAbilityData capability = null;
   private final LazyOptional<PlayerAbilityData> optional = LazyOptional.of(this::createCapability);

   private PlayerAbilityData createCapability() {
      if (this.capability == null) {
         this.capability = PlayerAbilityData.create();
      }

      return this.capability;
   }

   public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
      return cap == CAPABILITY ? this.optional.cast() : LazyOptional.empty();
   }

   public CompoundTag serializeNBT() {
      CompoundTag NBT = new CompoundTag();
      this.createCapability().save(NBT);
      return NBT;
   }

   public void deserializeNBT(CompoundTag NBT) {
      this.createCapability().load(NBT);
   }
}
