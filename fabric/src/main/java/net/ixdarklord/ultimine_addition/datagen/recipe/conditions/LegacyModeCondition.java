package net.ixdarklord.ultimine_addition.datagen.recipe.conditions;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

// Fabric API's 1.20.1 resource conditions: a JSON provider for datagen and a registered JSON predicate.
public record LegacyModeCondition(boolean value) implements ConditionJsonProvider {
    public static final ResourceLocation ID = FTBUltimineAddition.id("legacy_mode");

    public static void register() {
        ResourceConditions.register(ID, object -> GsonHelper.getAsBoolean(object, "value", false) == PlaystyleModes.isLegacy());
    }

    @Override
    public ResourceLocation getConditionId() {
        return ID;
    }

    @Override
    public void writeParameters(JsonObject object) {
        object.addProperty("value", this.value);
    }
}
