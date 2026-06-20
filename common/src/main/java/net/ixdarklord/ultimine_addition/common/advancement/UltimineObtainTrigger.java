package net.ixdarklord.ultimine_addition.common.advancement;

import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class UltimineObtainTrigger extends SimpleCriterionTrigger<UltimineObtainTrigger.Instance> {
    public static final ResourceLocation ID = FTBUltimineAddition.id("obtain_ultimine");

    public @NotNull ResourceLocation getId() {
        return ID;
    }

    public @NotNull Instance createInstance(@NotNull JsonObject json, ContextAwarePredicate predicate, @NotNull DeserializationContext condition) {
        return new Instance(predicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, (instance) -> true);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        public Instance(ContextAwarePredicate player) {
            super(UltimineObtainTrigger.ID, player);
        }

        public static Instance obtain() {
            return new Instance(ContextAwarePredicate.ANY);
        }
    }
}
