package net.ixdarklord.ultimine_addition.common.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.Optional;

public class AdvancementTriggers {
    public static Criterion<PlayerTrigger.TriggerInstance> advancementTrigger(AdvancementHolder advancementHolder) {
        return advancementTrigger(advancementHolder.id());
    }

    public static Criterion<PlayerTrigger.TriggerInstance> advancementTrigger(ResourceLocation name) {
        ContextAwarePredicate predicate = ContextAwarePredicate.create(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                EntityPredicate.Builder.entity().subPredicate(PlayerPredicate.Builder.player().checkAdvancementDone(name, true).build())).build());
        return CriteriaTriggers.TICK.createCriterion(new PlayerTrigger.TriggerInstance(Optional.of(predicate)));
    }

    public static Criterion<TradeTrigger.TriggerInstance> tradedWithVillager(ItemPredicate itemPredicate) {
        return CriteriaTriggers.TRADE.createCriterion(new TradeTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(itemPredicate)));
    }
}
