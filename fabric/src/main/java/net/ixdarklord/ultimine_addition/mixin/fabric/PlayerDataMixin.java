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

    @Override
    public PlayerAbilityData ua$getUltimineData() {
        if (this.ultimineData == null) {
            this.ultimineData = PlayerAbilityData.create();
        }
        return ultimineData;
    }

    @Inject(method = "addAdditionalSaveData", at = @At(value = "HEAD"))
    public void writeUltimineData(CompoundTag compound, CallbackInfo ci) {
        if (ua$getUltimineData() != null) {
            CompoundTag NBT = new CompoundTag();
            ua$getUltimineData().saveData(NBT);
            if (!NBT.isEmpty()) compound.put(ua$getUltimineData().getNBTBase(), NBT);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "HEAD"))
    public void readUltimineData(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains(ua$getUltimineData().getNBTBase(), 10)) {
            CompoundTag NBT = compound.getCompound(ua$getUltimineData().getNBTBase());
            ua$getUltimineData().loadData(NBT);
        }
    }
}
