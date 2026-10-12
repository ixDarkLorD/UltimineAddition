package net.ixdarklord.ultimine_addition.common.advancement;

import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

// One trigger for the mod's simple "did it once" advancements: each names its milestone ("undo", "daily", "perk",
// "big_ultimine", "thousand_blocks"), and the code that reaches one calls MilestoneTrigger.reach.
public class MilestoneTrigger extends SimpleCriterionTrigger<MilestoneTrigger.Instance> {
    public static final ResourceLocation ID = FTBUltimineAddition.id("milestone");
    public static final String UNDO = "undo", DAILY = "daily", PERK = "perk", BIG_ULTIMINE = "big_ultimine", THOUSAND_BLOCKS = "thousand_blocks";

    public static void reach(ServerPlayer player, String milestone) {
        Registration.MILESTONE_TRIGGER.get().trigger(player, instance -> instance.milestone().equals(milestone));
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    @Override
    protected @NotNull Instance createInstance(@NotNull JsonObject json, @NotNull ContextAwarePredicate player, @NotNull DeserializationContext context) {
        return new Instance(player, GsonHelper.getAsString(json, "milestone"));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final String milestone;

        public Instance(ContextAwarePredicate player, String milestone) {
            super(ID, player);
            this.milestone = milestone;
        }

        public String milestone() {
            return this.milestone;
        }

        public static Instance of(String milestone) {
            return new Instance(ContextAwarePredicate.ANY, milestone);
        }

        @Override
        public @NotNull JsonObject serializeToJson(@NotNull SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("milestone", this.milestone);
            return json;
        }
    }
}
