package net.ixdarklord.ultimine_addition.mixin.fabric;

import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Players saved before the ability data became a CoolCatLib attachment kept it under its own key. Read once: the
// attachment (loaded right after this, when the player has one) wins, and the old key is never written again.
@Mixin(Player.class)
public abstract class MixinLegacyPlayerData {
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readLegacyUltimineData(CompoundTag compound, CallbackInfo ci) {
        CompoundTag tag = compound.getCompound(PlayerAbilityData.DATA_ID.toString());
        if (tag.isEmpty()) return;
        PlayerAbilityData.CODEC.parse(NbtOps.INSTANCE, tag).result()
                .ifPresent(data -> Registration.PLAYER_ABILITY.set((Player) (Object) this, data));
    }
}
