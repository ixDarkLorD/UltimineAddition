package net.ixdarklord.ultimine_addition.datagen.recipe.conditions;

import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import net.minecraft.resources.RegistryOps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

public record LegacyModeCondition(boolean value) implements ResourceCondition {
    public static final MapCodec<LegacyModeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("value").forGetter(LegacyModeCondition::value)
    ).apply(instance, LegacyModeCondition::new));
    public static final ResourceConditionType<LegacyModeCondition> RESOURCE_CONDITION_TYPE = ResourceConditionType.create(FTBUltimineAddition.id("legacy_mode"), CODEC);

    @Override
    public ResourceConditionType<?> getType() {
        return RESOURCE_CONDITION_TYPE;
    }

    @Override
    public boolean test(@Nullable RegistryOps.RegistryInfoLookup registryLookup) {
        boolean isLegacyMode = PlaystyleModes.isLegacy();
        return value == isLegacyMode;
    }
}
