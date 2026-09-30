package net.ixdarklord.ultimine_addition.mixin.forge;

import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Migration: players saved by the earlier 1.20.1 releases (Forge) kept their ability data (the Miner Certificate
// unlock, "is_unlocked") in the "ultimine_addition:properties" capability, saved as
// ForgeCaps."ultimine_addition:properties"."ultimine_addition:ftb_ultimine_ability". That capability isn't attached
// anymore, so Forge drops it on the next save; it's read here once. The attachment (loaded right after this, when the
// player has one) wins.
@Mixin(Player.class)
public abstract class MixinLegacyPlayerData {
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readLegacyUltimineData(CompoundTag compound, CallbackInfo ci) {
        CompoundTag capability = compound.getCompound("ForgeCaps").getCompound(FTBUltimineAddition.id("properties").toString());
        CompoundTag tag = capability.getCompound(PlayerAbilityData.DATA_ID.toString());
        if (tag.isEmpty()) return;
        PlayerAbilityData.CODEC.parse(NbtOps.INSTANCE, tag).result()
                .ifPresent(data -> Registration.PLAYER_ABILITY.set((Player) (Object) this, data));
    }
}
