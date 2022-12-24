package net.ixdarklord.ultimine_addition.mixin;

import net.ixdarklord.ultimine_addition.data.IDataHandler;
import net.ixdarklord.ultimine_addition.data.player.PlayerUltimineData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class)
public abstract class PlayerDataSaverMixin implements IDataHandler {
    private PlayerUltimineData ultimineData;
    @Override
    public PlayerUltimineData getPlayerUltimineData() {
        if (this.ultimineData == null) {
            this.ultimineData = new PlayerUltimineData();
        }
        return ultimineData;
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "HEAD"))
    public void readUltimineData(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains(IDataHandler.NBT_PATH, 10)) {
            CompoundTag NBT = compound.getCompound(IDataHandler.NBT_PATH);
            ultimineData = new PlayerUltimineData();
            ultimineData.loadNBTData(NBT);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At(value = "HEAD"))
    public void writeUltimineData(CompoundTag compound, CallbackInfo ci) {
        if (ultimineData != null) {
            CompoundTag NBT = new CompoundTag();
            ultimineData.saveNBTData(NBT);
            if (!NBT.isEmpty()) compound.put(IDataHandler.NBT_PATH, NBT);
        }
    }
}
