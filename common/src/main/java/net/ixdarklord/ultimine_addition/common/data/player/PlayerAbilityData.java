package net.ixdarklord.ultimine_addition.common.data.player;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.coolcatlib.api.data.DataComponent;
import net.ixdarklord.coolcatlib.api.utils.CodecUtils;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public class PlayerAbilityData extends DataComponent<PlayerAbilityData> {
    public static final Identifier DATA_ID = FTBUltimineAddition.id("ultimine_ability");
    public static final MapCodec<PlayerAbilityData> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.BOOL.fieldOf("is_unlocked").forGetter(PlayerAbilityData::getAbility)
    ).apply(inst, PlayerAbilityData::new));
    public static final Codec<PlayerAbilityData> CODEC = MAP_CODEC.codec();

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

    // Entity save data uses ValueInput/ValueOutput now; same key and format as the CompoundTag versions.
    public void save(ValueOutput output) {
        output.store(DATA_ID.toString(), CODEC, this);
    }

    public void load(ValueInput input) {
        input.read(DATA_ID.toString(), CODEC).ifPresent(this::copyFrom);
    }

    public void load(CompoundTag compoundTag) {
        CompoundTag tag = compoundTag.getCompoundOrEmpty(DATA_ID.toString());
        if (tag.isEmpty()) return;
        this.copyFrom(CodecUtils.decode(this.codec, tag));
    }
}
