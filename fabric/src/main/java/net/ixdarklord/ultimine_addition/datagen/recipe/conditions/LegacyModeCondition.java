package net.ixdarklord.ultimine_addition.datagen.recipe.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public record LegacyModeCondition(boolean value) implements ConditionJsonProvider {
   public static final ResourceLocation ID = FTBUltimineAddition.id("legacy_mode");
   public static final MapCodec<LegacyModeCondition> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(Codec.BOOL.fieldOf("value").forGetter(LegacyModeCondition::value)).apply(instance, LegacyModeCondition::new));

   public static void register() {
      ResourceConditions.register(ID, (object) -> {
         boolean expectedValue = GsonHelper.getAsBoolean(object, "value", false);
         boolean isLegacyMode = ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY;
         return isLegacyMode == expectedValue;
      });
   }

   public ResourceLocation getConditionId() {
      return ID;
   }

   public void writeParameters(JsonObject jsonObject) {
      DataResult<JsonElement> result = CODEC.codec().encodeStart(JsonOps.INSTANCE, this);
      result.result().ifPresent((element) -> {
         if (element.isJsonObject()) {
            JsonObject encoded = element.getAsJsonObject();
            encoded.entrySet().forEach((entry) -> jsonObject.add(entry.getKey(), entry.getValue()));
         }

      });
   }
}
