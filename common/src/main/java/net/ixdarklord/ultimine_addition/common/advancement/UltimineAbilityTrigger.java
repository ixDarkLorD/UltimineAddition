package net.ixdarklord.ultimine_addition.common.advancement;

import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class UltimineAbilityTrigger extends SimpleCriterionTrigger<UltimineAbilityTrigger.Instance> {
    public static final ResourceLocation ID = UltimineAddition.getLocation("obtain_ultimine");

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    @Override
    public @NotNull Instance createInstance(@NotNull JsonObject json, EntityPredicate.@NotNull Composite player, @NotNull DeserializationContext condition) {
        return new Instance(player);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, (instance) -> true);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        public Instance(EntityPredicate.Composite player) {
            super(UltimineAbilityTrigger.ID, player);
        }
        public static Instance obtain() {
            return new Instance(EntityPredicate.Composite.ANY);
        }
    }
}
