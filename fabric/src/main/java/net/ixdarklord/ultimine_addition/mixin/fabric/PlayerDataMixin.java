package net.ixdarklord.ultimine_addition.mixin.fabric;

import net.ixdarklord.ultimine_addition.common.data.player.IPlayerData;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class)
public abstract class PlayerDataMixin implements IPlayerData {
    @Unique
    private PlayerAbilityData ultimineData;

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public PlayerAbilityData getUltimineData() {
        if (this.ultimineData == null) {
            this.ultimineData = new PlayerAbilityData();
        }
        return ultimineData;
    }

    @Inject(method = "addAdditionalSaveData", at = @At(value = "HEAD"))
    public void writeUltimineData(CompoundTag compound, CallbackInfo ci) {
        if (getUltimineData() != null) {
            CompoundTag NBT = new CompoundTag();
            getUltimineData().saveData(NBT);
            if (!NBT.isEmpty()) compound.put(getUltimineData().getNBTBase(), NBT);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "HEAD"))
    public void readUltimineData(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains(getUltimineData().getNBTBase(), 10)) {
            CompoundTag NBT = compound.getCompound(getUltimineData().getNBTBase());
            getUltimineData().loadData(NBT);
        }
    }
}
