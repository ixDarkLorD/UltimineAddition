package net.ixdarklord.ultimine_addition.mixin;

import net.ixdarklord.ultimine_addition.data.DataHandler;
import net.ixdarklord.ultimine_addition.helper.FabricPlatformHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class)
public class PlayerDataSaverMixin {

    @Inject(method = "readAdditionalSaveData", at = @At(value = "HEAD"))
    public void readUltimineData(CompoundTag compound, CallbackInfo ci) {
        CompoundTag NBT = compound.getCompound(DataHandler.NBT_PATH);
        DataHandler.ultimineData.loadNBTData(NBT);
    }

    @Inject(method = "addAdditionalSaveData", at = @At(value = "HEAD"))
    public void addUltimineData(CompoundTag compound, CallbackInfo ci) {
        CompoundTag NBT = new CompoundTag();
        DataHandler.ultimineData.saveNBTData(NBT);
        compound.put(DataHandler.NBT_PATH, NBT);
    }
}
