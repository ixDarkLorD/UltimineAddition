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

@Mixin(Player.class)
public abstract class MixinPlayerData implements IPlayerData {
    @Unique
    private PlayerAbilityData ultimineData;

    @Override
    public PlayerAbilityData getUltimineData$UA() {
        if (this.ultimineData == null) {
            this.ultimineData = PlayerAbilityData.create();
        }
        return ultimineData;
    }

    @Inject(method = "addAdditionalSaveData", at = @At(value = "HEAD"))
    public void writeUltimineData(CompoundTag compound, CallbackInfo ci) {
        this.getUltimineData$UA().save(compound);
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "HEAD"))
    public void readUltimineData(CompoundTag compound, CallbackInfo ci) {
        this.getUltimineData$UA().load(compound);
    }
}
