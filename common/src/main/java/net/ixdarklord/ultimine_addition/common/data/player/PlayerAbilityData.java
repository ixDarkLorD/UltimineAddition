package net.ixdarklord.ultimine_addition.common.data.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.coolcatlib.api.data.DataComponent;
import net.ixdarklord.coolcatlib.api.utils.CodecUtils;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class PlayerAbilityData extends DataComponent<PlayerAbilityData> {
    public static final ResourceLocation DATA_ID = FTBUltimineAddition.id("ultimine_ability");
    public static final Codec<PlayerAbilityData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.fieldOf("is_unlocked").forGetter(PlayerAbilityData::getAbility)
    ).apply(inst, PlayerAbilityData::new));

    private PlayerAbilityData(boolean isUnlocked) {
        super(DATA_ID, CODEC);
        this.isUnlocked = isUnlocked;
    }

    public static PlayerAbilityData create() {
        return new PlayerAbilityData(false);
    }

    private boolean isUnlocked;

    public boolean getAbility() {
        return isUnlocked;
    }

    public PlayerAbilityData setAbility(boolean unlocked) {
        isUnlocked = unlocked;
        return this;
    }

    public void copyFrom(PlayerAbilityData source) {
        this.isUnlocked = source.isUnlocked;
    }

    public void save(CompoundTag compoundTag) {
        super.save(compoundTag);
    }

    public void load(CompoundTag compoundTag) {
        CompoundTag tag = compoundTag.getCompound(DATA_ID.toString());
        if (tag.isEmpty()) return;
        this.copyFrom(CodecUtils.decode(this.codec, tag));
    }
}
