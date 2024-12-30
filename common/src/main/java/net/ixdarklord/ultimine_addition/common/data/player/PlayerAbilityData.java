package net.ixdarklord.ultimine_addition.common.data.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.data.DataHandler;
import net.minecraft.nbt.CompoundTag;

public class PlayerAbilityData extends DataHandler<PlayerAbilityData, CompoundTag> {
    public static final Codec<PlayerAbilityData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.fieldOf("is_capable").forGetter(PlayerAbilityData::getAbility)
    ).apply(inst, PlayerAbilityData::new));

    private PlayerAbilityData(boolean capable) {
        this.capable = capable;
    }

    public static PlayerAbilityData create() {
        return new PlayerAbilityData(false);
    }

    private boolean capable;

    public boolean getAbility() {
        return capable;
    }

    public PlayerAbilityData setAbility(boolean state) {
        capable = state;
        return this;
    }

    public void copyFrom(PlayerAbilityData source) {
        this.capable = source.capable;
    }

    @Override
    public void saveData(CompoundTag tag) {
        tag.putBoolean("is_capable", capable);
    }

    public void loadData(CompoundTag tag) {
        this.capable = tag.getBoolean("is_capable");
    }
}
