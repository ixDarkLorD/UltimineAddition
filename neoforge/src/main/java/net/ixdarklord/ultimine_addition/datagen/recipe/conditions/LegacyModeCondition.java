package net.ixdarklord.ultimine_addition.datagen.recipe.conditions;

import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

public record LegacyModeCondition(boolean value) implements ICondition {
    public static MapCodec<LegacyModeCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.BOOL.fieldOf("value").forGetter(LegacyModeCondition::value)
    ).apply(inst, LegacyModeCondition::new));

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(@NotNull IContext context) {
        boolean isLegacyMode = PlaystyleModes.isLegacy();
        return value == isLegacyMode;
    }
}
