package net.ixdarklord.ultimine_addition.datagen.recipe.conditions;

import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import org.jetbrains.annotations.NotNull;

// Forge 47's recipe conditions have a JSON serializer (registered in ForgeSetup) instead of a codec.
public record LegacyModeCondition(boolean value) implements ICondition {
    @Override
    public ResourceLocation getID() {
        return Serializer.NAME;
    }

    @Override
    public boolean test(@NotNull IContext context) {
        boolean isLegacyMode = PlaystyleModes.isLegacy();
        return value == isLegacyMode;
    }

    public static class Serializer implements IConditionSerializer<LegacyModeCondition> {
        public static final ResourceLocation NAME = FTBUltimineAddition.id("legacy_mode");
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, LegacyModeCondition value) {
            json.addProperty("value", value.value);
        }

        @Override
        public LegacyModeCondition read(JsonObject json) {
            return new LegacyModeCondition(GsonHelper.getAsBoolean(json, "value"));
        }

        @Override
        public ResourceLocation getID() {
            return NAME;
        }
    }
}
