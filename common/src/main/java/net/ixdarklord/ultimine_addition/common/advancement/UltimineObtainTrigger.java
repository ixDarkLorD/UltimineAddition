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

// 1.20.1 criterion triggers carry their own id and read their instances from JSON.
public class UltimineObtainTrigger extends SimpleCriterionTrigger<UltimineObtainTrigger.Instance> {
    public static final ResourceLocation ID = FTBUltimineAddition.id("ultimine_obtain");

    public void trigger(ServerPlayer player) {
        this.trigger(player, (instance) -> true);
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    @Override
    protected @NotNull Instance createInstance(@NotNull JsonObject json, @NotNull ContextAwarePredicate player, @NotNull DeserializationContext context) {
        return new Instance(player);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        public Instance(ContextAwarePredicate player) {
            super(ID, player);
        }

        public static Instance obtain() {
            return new Instance(ContextAwarePredicate.ANY);
        }
    }
}
