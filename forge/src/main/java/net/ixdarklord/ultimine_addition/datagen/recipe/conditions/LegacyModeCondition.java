package net.ixdarklord.ultimine_addition.datagen.recipe.conditions;

import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public record LegacyModeCondition(boolean value) implements ICondition {
    public ResourceLocation getID() {
        return LegacyModeCondition.Serializer.NAME;
    }

    public boolean test(ICondition.IContext context) {
        boolean isLegacyMode = ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY;
        return isLegacyMode == this.value;
    }

    public static class Serializer implements IConditionSerializer<LegacyModeCondition> {
        private static final ResourceLocation NAME = FTBUltimineAddition.id("legacy_mode");
        public static final Serializer INSTANCE = new Serializer();

        public void write(JsonObject json, LegacyModeCondition value) {
            json.addProperty("value", value.value);
        }

        public LegacyModeCondition read(JsonObject json) {
            return new LegacyModeCondition(GsonHelper.getAsBoolean(json, "value"));
        }

        public ResourceLocation getID() {
            return NAME;
        }
    }
}
