package net.ixdarklord.ultimine_addition.datagen.recipe.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

public record LegacyModeCondition(boolean value) implements ResourceCondition {
    public static final MapCodec<LegacyModeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("value").forGetter(LegacyModeCondition::value)
    ).apply(instance, LegacyModeCondition::new));
    public static final ResourceConditionType<LegacyModeCondition> RESOURCE_CONDITION_TYPE = ResourceConditionType.create(FTBUltimineAddition.getLocation("legacy_mode"), CODEC);

    @Override
    public ResourceConditionType<?> getType() {
        return RESOURCE_CONDITION_TYPE;
    }

    @Override
    public boolean test(@Nullable HolderLookup.Provider registryLookup) {
        boolean isLegacyMode = ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY;
        return value == isLegacyMode;
    }
}
