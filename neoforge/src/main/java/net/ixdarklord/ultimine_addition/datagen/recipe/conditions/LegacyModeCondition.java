package net.ixdarklord.ultimine_addition.datagen.recipe.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

public record LegacyModeCondition(boolean value) implements ICondition {
    public static MapCodec<LegacyModeCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.BOOL.fieldOf("value").forGetter(LegacyModeCondition::value)
    ).apply(inst, LegacyModeCondition::new));

    @Override
    public boolean test(@NotNull IContext context) {
        boolean isLegacyMode = ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY;
        return isLegacyMode == value;
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
