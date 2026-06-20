package net.ixdarklord.ultimine_addition.common.data.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.coolcatlib.api.data.DataComponent;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public final class PlayerAbilityData extends DataComponent<PlayerAbilityData> {
    public static final ResourceLocation DATA_ID = FTBUltimineAddition.id("ftb_ultimine_ability");
    public static final Codec<PlayerAbilityData> CODEC = RecordCodecBuilder.create((inst) -> inst.group(Codec.BOOL.fieldOf("is_unlocked").forGetter(PlayerAbilityData::getAbility)).apply(inst, PlayerAbilityData::new));
    private boolean capable;

    private PlayerAbilityData(boolean capable) {
        super(DATA_ID, CODEC);
        this.capable = capable;
    }

    public static PlayerAbilityData create() {
        return new PlayerAbilityData(false);
    }

    public boolean getAbility() {
        return this.capable;
    }

    public PlayerAbilityData setAbility(boolean state) {
        this.capable = state;
        return this;
    }

    public void copyFrom(PlayerAbilityData source) {
        this.capable = source.capable;
    }

    public void save(CompoundTag compoundTag) {
        super.save(compoundTag);
    }

    public void load(CompoundTag compoundTag) {
        CompoundTag tag = compoundTag.getCompound(DATA_ID.toString());
        this.capable = tag.getBoolean("is_unlocked");
    }
}
