package net.ixdarklord.ultimine_addition.datagen.recipe.conditions;

import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class LegacyModeCondition implements ICondition {
    private final boolean state;

    public LegacyModeCondition(boolean state) {
        this.state = state;
    }

    @Override
    public ResourceLocation getID() {
        return Serializer.NAME;
    }

    @Override
    public boolean test(IContext context) {
        boolean isLegacyMode = ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY;
        return isLegacyMode == state;
    }

    public static class Serializer implements IConditionSerializer<LegacyModeCondition> {
        private static final ResourceLocation NAME = Constants.getLocation("legacy_mode");
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, LegacyModeCondition value) {
            json.addProperty("state", value.state);
        }

        @Override
        public LegacyModeCondition read(JsonObject json) {
            return new LegacyModeCondition(GsonHelper.getAsBoolean(json, "state"));
        }

        @Override
        public ResourceLocation getID() {
            return NAME;
        }
    }
}
