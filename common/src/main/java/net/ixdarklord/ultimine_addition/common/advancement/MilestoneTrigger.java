package net.ixdarklord.ultimine_addition.common.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

// One trigger for the mod's simple "did it once" advancements: each names its milestone ("undo", "daily", "perk",
// "big_ultimine", "thousand_blocks"), and the code that reaches one calls MilestoneTrigger.reach.
public class MilestoneTrigger extends SimpleCriterionTrigger<MilestoneTrigger.Instance> {
    public static final String UNDO = "undo", DAILY = "daily", PERK = "perk", BIG_ULTIMINE = "big_ultimine", THOUSAND_BLOCKS = "thousand_blocks";

    public static void reach(ServerPlayer player, String milestone) {
        Registration.MILESTONE_TRIGGER.get().trigger(player, instance -> instance.milestone().equals(milestone));
    }

    @Override
    public @NotNull Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public record Instance(Optional<ContextAwarePredicate> player, String milestone) implements SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                        Codec.STRING.fieldOf("milestone").forGetter(Instance::milestone))
                .apply(instance, Instance::new));

        public static Criterion<Instance> of(String milestone) {
            return Registration.MILESTONE_TRIGGER.get().createCriterion(new Instance(Optional.empty(), milestone));
        }
    }
}
